/* ParkBay - home.js: live availability per floor from GET /api/floors. */
async function load() {
  try {
    const floors = await api('GET', '/api/floors');
    const active = floors.flatMap(f => f.slots).filter(s => s.active);
    const free = active.filter(s => s.observedOccupancy === 'AVAILABLE').length;
    $('#lotsCount').textContent = `${free} of ${active.length} bays free right now`;
    $('#lotGrid').innerHTML = floors.length ? floors.map(f => {
      const act = f.slots.filter(s => s.active);
      const fr = act.filter(s => s.observedOccupancy === 'AVAILABLE').length;
      const unk = act.filter(s => s.observedOccupancy === 'UNKNOWN').length;
      const dot = unk === act.length ? '#6E7075' : (fr > 0 ? '#3D8B5F' : '#C1443C');
      return `<a class="lot-card" href="lot.html?floor=${f.id}">
        <h3>${esc(f.name)}</h3>
        <div class="lot-meta">${act.length} active bays${unk ? `, ${unk} without sensor data` : ''}</div>
        <div class="lot-stats"><span class="availability-badge"><span class="dot" style="background:${dot}"></span>${fr} of ${act.length} free</span></div>
      </a>`;
    }).join('') : '<div class="empty-state"><div class="stencil">No floors yet</div>An administrator needs to set up the parking floors.</div>';
  } catch (e) { showToast(e.message); }
}
document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth()) return;
  load();
  setInterval(load, 15000);   // keep availability fresh
});
