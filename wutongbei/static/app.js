const fileInput = document.getElementById("fileInput");
const btnAnalyze = document.getElementById("btnAnalyze");
const btnDownload = document.getElementById("btnDownload");
const btnReset = document.getElementById("btnReset");

const report = document.getElementById("report");
const errorBox = document.getElementById("errorBox");
const sysStatus = document.getElementById("sysStatus");

const progressWrap = document.getElementById("progressWrap");
const progressBar = document.getElementById("progressBar");
const progressText = document.getElementById("progressText");

const riskPill = document.getElementById("riskPill");
const metrics = document.getElementById("metrics");
const typeCounts = document.getElementById("typeCounts");
const topSrc = document.getElementById("topSrc");
const topDst = document.getElementById("topDst");
const topPath = document.getElementById("topPath");
const highTable = document.getElementById("highTable");

function showError(msg){
  errorBox.textContent = msg;
  errorBox.classList.remove("hidden");
}
function clearError(){
  errorBox.textContent = "";
  errorBox.classList.add("hidden");
}

function setProgress(p, text){
  progressWrap.classList.remove("hidden");
  progressBar.style.width = `${p}%`;
  progressText.textContent = text || "";
}
function hideProgress(){
  progressWrap.classList.add("hidden");
  progressBar.style.width = "0%";
  progressText.textContent = "";
}

function kvList(container, obj, maxItems=20){
  container.innerHTML = "";
  if(!obj) return;
  const entries = Object.entries(obj).slice(0, maxItems);
  if(entries.length === 0){
    container.innerHTML = `<div class="muted">（无数据）</div>`;
    return;
  }
  for(const [k,v] of entries){
    const div = document.createElement("div");
    div.className = "kv";
    div.innerHTML = `<div class="k">${escapeHtml(k)}</div><div class="v">${escapeHtml(String(v))}</div>`;
    container.appendChild(div);
  }
}

function renderMetrics(stats){
  const items = [
    ["总告警数", stats.total],
    ["高风险", `${stats.high} (${(stats.high_ratio*100).toFixed(2)}%)`],
    ["中风险", `${stats.mid} (${(stats.mid_ratio*100).toFixed(2)}%)`],
    ["低风险/可疑", stats.low],
    ["正常访问", stats.normal],
  ];
  metrics.innerHTML = "";
  for(const [k,v] of items){
    const div = document.createElement("div");
    div.className = "metric";
    div.innerHTML = `<div class="k">${escapeHtml(k)}</div><div class="v">${escapeHtml(String(v))}</div>`;
    metrics.appendChild(div);
  }
}

function renderHighRiskTable(rows){
  const thead = highTable.querySelector("thead");
  const tbody = highTable.querySelector("tbody");
  thead.innerHTML = "";
  tbody.innerHTML = "";

  if(!rows || rows.length === 0){
    thead.innerHTML = `<tr><th>提示</th></tr>`;
    tbody.innerHTML = `<tr><td class="muted">（无高风险样本）</td></tr>`;
    return;
  }

  const cols = Object.keys(rows[0]);
  const ths = cols.map(c => `<th>${escapeHtml(c)}</th>`).join("");
  thead.innerHTML = `<tr>${ths}</tr>`;

  for(const r of rows){
    const tds = cols.map(c => `<td>${escapeHtml(String(r[c] ?? ""))}</td>`).join("");
    tbody.innerHTML += `<tr>${tds}</tr>`;
  }
}

function escapeHtml(s){
  return s.replace(/[&<>"']/g, (m) => ({
    "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
  }[m]));
}

async function analyze(){
  clearError();
  const f = fileInput.files && fileInput.files[0];
  if(!f){
    showError("请先选择一个 CSV/XLSX/XLS 文件。");
    return;
  }

  btnAnalyze.disabled = true;
  btnDownload.disabled = true;
  report.classList.add("hidden");

  try{
    setProgress(15, "上传并分析中...");
    const fd = new FormData();
    fd.append("file", f);

    const res = await fetch("/api/analyze", { method:"POST", body: fd });
    if(!res.ok){
      const t = await res.text();
      throw new Error(`后端返回错误：${t}`);
    }

    setProgress(80, "生成报告中...");
    const data = await res.json();

    riskPill.textContent = `总体风险等级：${data.overall_risk_grade}`;
    renderMetrics(data.stats);

    kvList(typeCounts, data.type_counts, 50);
    kvList(topSrc, data.top_src_ip, 10);
    kvList(topDst, data.top_dst_ip, 10);
    kvList(topPath, data.top_url_path, 10);
    renderHighRiskTable(data.high_risk_preview);

    report.classList.remove("hidden");
    btnDownload.disabled = false;

    setProgress(100, "完成");
    setTimeout(hideProgress, 400);
  }catch(e){
    hideProgress();
    showError(e.message || String(e));
  }finally{
    btnAnalyze.disabled = false;
  }
}

async function downloadCsv(){
  clearError();
  const f = fileInput.files && fileInput.files[0];
  if(!f){
    showError("请先选择一个文件，再下载预测结果。");
    return;
  }
  btnDownload.disabled = true;

  try{
    setProgress(20, "生成可下载 CSV...");
    const fd = new FormData();
    fd.append("file", f);

    const res = await fetch("/api/download_csv", { method:"POST", body: fd });
    if(!res.ok){
      const t = await res.text();
      throw new Error(`下载失败：${t}`);
    }
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);

    // 尝试从 header 获取 filename
    const cd = res.headers.get("Content-Disposition") || "";
    let filename = "pred_result.csv";
    const m = cd.match(/filename="([^"]+)"/);
    if(m && m[1]) filename = m[1];

    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);

    setProgress(100, "已下载");
    setTimeout(hideProgress, 400);
  }catch(e){
    hideProgress();
    showError(e.message || String(e));
  }finally{
    btnDownload.disabled = false;
  }
}

async function checkHealth(){
  try{
    const res = await fetch("/api/health");
    if(!res.ok) throw new Error();
    const data = await res.json();
    sysStatus.textContent = data.status === "ok" ? "在线 · 模型已加载" : "异常";
  }catch{
    sysStatus.textContent = "离线或未启动";
  }
}

// events
btnAnalyze.addEventListener("click", analyze);
btnDownload.addEventListener("click", downloadCsv);
btnReset.addEventListener("click", () => {
  fileInput.value = "";
  report.classList.add("hidden");
  btnDownload.disabled = true;
  clearError();
});

// init
checkHealth();
