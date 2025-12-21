# app.py
# -*- coding: utf-8 -*-

import io
import os
import re
import html
import urllib.parse
from typing import Dict, List, Tuple, Optional

import pandas as pd
import joblib

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse, StreamingResponse, FileResponse
from fastapi.staticfiles import StaticFiles


# =========================================================
# 路径：以 app.py 所在目录为基准（避免 PyCharm 工作目录问题）
# =========================================================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "model_outputs", "model.joblib")
STATIC_DIR = os.path.join(BASE_DIR, "static")

# =========================================================
# 默认文本字段（训练时使用的字段）
# =========================================================
TEXT_COLS_DEFAULT = ["method", "url_path", "request_body", "user_agent"]

# =========================================================
# 风险严重性映射（可按赛题/经验调整）
# 0=无/低，1=低，2=中，3=高
# =========================================================
SEVERITY_MAP = {
    "远程命令执行攻击": 3,
    "文件上传攻击": 3,
    "Java反序列化漏洞利用攻击": 3,

    "SQL注入攻击": 2,
    "文件包含攻击": 2,
    "目录遍历攻击": 2,

    "XSS跨站脚本攻击": 1,
    "CSRF攻击": 1,

    "正常访问": 0,
}
SEVERITY_NAME = {0: "低/无风险", 1: "低风险", 2: "中风险", 3: "高风险"}

# =========================================================
# 同义列映射：CSV/Excel 列名不一致时自动适配
# =========================================================
COLUMN_ALIASES = {
    "method": ["method", "http_method", "request_method"],
    "url_path": ["url_path", "path", "uri", "url", "request_uri", "endpoint"],
    "request_body": ["request_body", "body", "payload", "post_data", "data", "query", "params"],
    "user_agent": ["user_agent", "ua", "agent"],

    "src_ip": ["src_ip", "source_ip", "client_ip", "ip", "remote_addr"],
    "dst_ip": ["dst_ip", "dest_ip", "server_ip", "host_ip"],
    "timestamp": ["timestamp", "time", "datetime", "date"],
}


# =========================================================
# JSON 序列化兜底：Timestamp/numpy/NA 统一转成可 JSON 化
# =========================================================
def to_jsonable(obj):
    import numpy as np
    import pandas as pd
    import datetime as dt

    if obj is None:
        return None

    # pandas 时间
    if isinstance(obj, (pd.Timestamp,)):
        return obj.isoformat()

    # datetime/date
    if isinstance(obj, (dt.datetime, dt.date)):
        return obj.isoformat()

    # pandas NA
    if obj is pd.NA:
        return None

    # numpy scalar
    if isinstance(obj, (np.integer,)):
        return int(obj)
    if isinstance(obj, (np.floating,)):
        return float(obj)
    if isinstance(obj, (np.bool_,)):
        return bool(obj)

    # dict/list/tuple
    if isinstance(obj, dict):
        return {str(k): to_jsonable(v) for k, v in obj.items()}
    if isinstance(obj, (list, tuple)):
        return [to_jsonable(x) for x in obj]

    return obj


# =========================================================
# FastAPI app
# =========================================================
app = FastAPI(title="告警智能研判 API", version="1.1")


# =========================
# 静态文件（前端）
# =========================
if not os.path.isdir(STATIC_DIR):
    raise RuntimeError(f"static 目录不存在：{STATIC_DIR}（请创建 static/index.html 等文件）")

app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")


@app.get("/", include_in_schema=False)
def serve_index():
    return FileResponse(os.path.join(STATIC_DIR, "index.html"))


# =========================================================
# 读取上传文件：支持 xlsx/xls/csv，自动识别编码
# =========================================================
def read_uploaded_table(raw: bytes, filename: str) -> pd.DataFrame:
    name = (filename or "").lower()

    # Excel：扩展名 或 xlsx(zip) 头 'PK'
    if name.endswith((".xlsx", ".xls")) or raw[:2] == b"PK":
        try:
            return pd.read_excel(io.BytesIO(raw), engine="openpyxl")
        except Exception as e:
            raise HTTPException(status_code=400, detail=f"读取 Excel 失败：{e}")

    # CSV：尝试自动检测编码（如果没装 charset-normalizer 也不会挂）
    detected: Optional[str] = None
    try:
        from charset_normalizer import from_bytes
        best = from_bytes(raw).best()
        if best:
            detected = best.encoding
    except Exception:
        detected = None

    candidates: List[str] = []
    if detected:
        candidates.append(detected)
    candidates += ["utf-8-sig", "utf-8", "gb18030", "gbk", "cp1252", "latin1"]

    last_err = None
    for enc in candidates:
        try:
            # engine="python" 更宽容（分号/tab/不规则行也更容易读）
            return pd.read_csv(io.BytesIO(raw), encoding=enc, encoding_errors="strict", engine="python")
        except Exception as e:
            last_err = e

    # 兜底：replace 避免少量坏字符导致整体失败
    try:
        return pd.read_csv(io.BytesIO(raw), encoding=(detected or "utf-8"),
                           encoding_errors="replace", engine="python")
    except Exception as e:
        raise HTTPException(
            status_code=400,
            detail=f"CSV 解析失败（编码尝试均失败）。最后错误：{last_err}；兜底错误：{e}",
        )


# =========================================================
# 文本预处理：与训练保持一致（轻量归一化）
# =========================================================
def normalize_text(x) -> str:
    if x is None:
        return ""
    if isinstance(x, float) and pd.isna(x):
        return ""
    t = str(x)

    # URL decode
    for _ in range(2):
        try:
            t = urllib.parse.unquote_plus(t)
        except Exception:
            break

    # HTML entity decode
    try:
        t = html.unescape(t)
    except Exception:
        pass

    t = t.lower()
    t = re.sub(r"\d{2,}", "<num>", t)
    t = re.sub(r"\b[a-f0-9]{16,}\b", "<hex>", t)
    t = re.sub(r"\s+", " ", t).strip()
    return t


def map_columns(df: pd.DataFrame) -> pd.DataFrame:
    lower_map = {str(c).lower(): c for c in df.columns}

    def find_col(std_name: str) -> Optional[str]:
        for alias in COLUMN_ALIASES.get(std_name, []):
            key = alias.lower()
            if key in lower_map:
                return lower_map[key]
        return None

    out = df.copy()
    for std in COLUMN_ALIASES.keys():
        real = find_col(std)
        if real and real != std:
            out[std] = out[real]
        elif std not in out.columns:
            out[std] = ""
    return out


def build_text(df: pd.DataFrame, text_cols: List[str]) -> pd.Series:
    tmp = df.copy()
    for c in text_cols:
        if c not in tmp.columns:
            tmp[c] = ""

    def row_to_text(row) -> str:
        parts = []
        for c in text_cols:
            if c == "method":
                parts.append(str(row.get(c, "")))
            else:
                parts.append(normalize_text(row.get(c, "")))
        return " ".join([p for p in parts if p])

    return tmp.apply(row_to_text, axis=1)


# =========================================================
# 模型加载（启动时加载一次）
# =========================================================
if not os.path.exists(MODEL_PATH):
    raise RuntimeError(f"模型文件不存在：{MODEL_PATH}（请先训练并生成 model_outputs/model.joblib）")

MODEL_BUNDLE = joblib.load(MODEL_PATH)
PIPELINE = MODEL_BUNDLE["pipeline"]
TEXT_COLS = MODEL_BUNDLE.get("text_cols", TEXT_COLS_DEFAULT)

print("Model loaded:", MODEL_PATH)


# =========================================================
# 风险报告逻辑
# =========================================================
def severity_of(label: str) -> int:
    return int(SEVERITY_MAP.get(label, 2))


def overall_grade(df_pred: pd.DataFrame) -> Tuple[str, Dict[str, float]]:
    total = len(df_pred)
    high = int((df_pred["severity"] == 3).sum())
    mid = int((df_pred["severity"] == 2).sum())
    low = int((df_pred["severity"] <= 1).sum())
    normal = int((df_pred["severity"] == 0).sum())

    high_ratio = high / total if total else 0.0
    mid_ratio = mid / total if total else 0.0

    if high_ratio >= 0.05 or high >= 20:
        grade = "高"
    elif (high + mid) >= 30 or (mid_ratio >= 0.10):
        grade = "中"
    else:
        grade = "低"

    stats = {
        "total": float(total),
        "normal": float(normal),
        "low": float(low),
        "mid": float(mid),
        "high": float(high),
        "high_ratio": float(high_ratio),
        "mid_ratio": float(mid_ratio),
    }
    return grade, stats


def analyze_dataframe(df: pd.DataFrame) -> pd.DataFrame:
    df2 = map_columns(df)

    if "row_id" not in df2.columns:
        df2.insert(0, "row_id", list(range(len(df2))))  # list() 避免 IDE 警告

    X = build_text(df2, TEXT_COLS)

    pred = PIPELINE.predict(X)
    df2["attack_type_pred"] = pred

    df2["severity"] = df2["attack_type_pred"].apply(severity_of)
    df2["risk_level"] = df2["severity"].map(SEVERITY_NAME)

    if hasattr(PIPELINE, "predict_proba"):
        proba = PIPELINE.predict_proba(X)
        df2["confidence"] = proba.max(axis=1)
    else:
        df2["confidence"] = None

    return df2


def make_report_payload(df_pred: pd.DataFrame) -> Dict:
    # timestamp 统一转字符串（避免 Timestamp 混入 preview）
    if "timestamp" in df_pred.columns:
        try:
            df_pred["timestamp"] = pd.to_datetime(df_pred["timestamp"], errors="ignore")
        except Exception:
            pass
        df_pred["timestamp"] = df_pred["timestamp"].astype(str)

    grade, stats = overall_grade(df_pred)

    type_counts = df_pred["attack_type_pred"].value_counts().to_dict()
    top_src = df_pred["src_ip"].astype(str).replace({"": "（空）"}).value_counts().head(10).to_dict()
    top_dst = df_pred["dst_ip"].astype(str).replace({"": "（空）"}).value_counts().head(10).to_dict()
    top_path = df_pred["url_path"].astype(str).replace({"": "（空）"}).value_counts().head(10).to_dict()

    high_preview_cols = [
        "timestamp", "src_ip", "dst_ip", "method", "url_path",
        "attack_type_pred", "risk_level", "confidence"
    ]
    for c in high_preview_cols:
        if c not in df_pred.columns:
            df_pred[c] = ""

    high_preview = (
        df_pred[df_pred["severity"] == 3]
        .head(20)[high_preview_cols]
        .to_dict(orient="records")
    )

    payload = {
        "overall_risk_grade": grade,
        "stats": stats,
        "type_counts": type_counts,
        "top_src_ip": top_src,
        "top_dst_ip": top_dst,
        "top_url_path": top_path,
        "high_risk_preview": high_preview,
    }

    # 最后一层兜底：把 numpy/pandas 类型全部转成 JSON 安全类型
    return to_jsonable(payload)


# =========================================================
# API：分析（返回 JSON 报告）
# =========================================================
@app.post("/api/analyze")
async def api_analyze(file: UploadFile = File(...)):
    raw = await file.read()
    df = read_uploaded_table(raw, file.filename)
    df_pred = analyze_dataframe(df)
    payload = make_report_payload(df_pred)
    return JSONResponse(payload)


# =========================================================
# API：下载带预测结果 CSV（UTF-8-SIG 防 Excel 乱码）
# =========================================================
@app.post("/api/download_csv")
async def api_download_csv(file: UploadFile = File(...)):
    raw = await file.read()
    df = read_uploaded_table(raw, file.filename)
    df_pred = analyze_dataframe(df)

    out = io.StringIO()
    df_pred.to_csv(out, index=False, encoding="utf-8-sig")
    out.seek(0)

    base_name = os.path.splitext(file.filename or "result")[0]
    filename = f"pred_{base_name}.csv"

    return StreamingResponse(
        iter([out.getvalue()]),
        media_type="text/csv",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'},
    )


@app.get("/api/health", include_in_schema=False)
def health():
    return {"status": "ok", "model_path": MODEL_PATH}
