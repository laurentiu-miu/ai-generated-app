(() => {
  const root = document.getElementById("import-status");
  if (!root || root.dataset.active !== "true") return;
  const id = root.dataset.importId;
  let failures = 0;
  const setText = (name, value) => { document.getElementById(name).textContent = value; };
  const poll = async () => {
    try {
      const response = await fetch(`/api/imports/${id}/progress`, {headers:{Accept:"application/json"}});
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const data = await response.json(); failures = 0;
      ["status","message","totalRows","processedRows","successfulRows","failedRows","skippedRows"].forEach(key => {
        const elementId = key.replace("Rows", "").replace(/^./, c => c.toLowerCase());
        setText(elementId, data[key]);
      });
      const bar = document.getElementById("progress-bar"); bar.style.width = `${data.percentage}%`; bar.textContent = `${data.percentage}%`;
      if (data.finished) { window.location.reload(); return; }
      window.setTimeout(poll, 1000);
    } catch (_) {
      failures += 1; window.setTimeout(poll, Math.min(5000, 1000 * failures));
    }
  };
  window.setTimeout(poll, 1000);
})();
