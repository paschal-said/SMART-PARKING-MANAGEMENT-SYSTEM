/* ParkBay - lot.js: bay map for one floor (live status from the sensors). */
const LABEL = { AVAILABLE: 'Free', OCCUPIED: 'Taken', UNKNOWN: 'No data' };
async function load() {
  const id = Number(new URLSearchParams(window.location.search).get('floor'));
  try {
    const floors = await api('GET', '/api/floors');
    const floor = floors.find(f => f.id === id) || floors[0];
    if (!floor) { $('#bayMap').innerHTML = '<p style="color:#F2F0EA">No floors set up yet.</p>'; return; }
    $('#lotTitle').textContent = floor.name;
    const act = floor.slots.filter(s => s.active);
    $('#lotWindow').textContent = `${act.filter(s => s.observedOccupancy === 'AVAILABLE').length} of ${act.length} bays free, updated ${new Date().toLocaleTimeString()}`;
    $('#bayMap').innerHTML = floor.slots.map(s => {
      const st = !s.active ? 'disabled' : s.observedOccupancy.toLowerCase();
      return `<div class="bay ${st}">${esc(s.slotNumber)}<span class="type">${s.active ? LABEL[s.observedOccupancy] : 'Closed'}</span></div>`;
    }).join('') || '<p style="color:#F2F0EA">No bays on this floor yet.</p>';
  } catch (e) { showToast(e.message); }
}
document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth()) return;
  load();
  setInterval(load, 10000);
});
