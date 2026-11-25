/* ParkBay - vehicles.js: driver's vehicles (/api/vehicles). The registered plate is what the gate camera matches. */
async function load() {
  try {
    const list = await api('GET', '/api/vehicles');
    $('#vehCount').textContent = `${list.length} registered`;
    $('#vehList').innerHTML = list.length ? list.map(v => `
      <div class="booking-item">
        <div class="bi-main"><span class="bi-bay">${esc(v.plateNumber)}</span>
          <span class="bi-meta">${esc(v.vehicleType)}${v.vehicleModel ? ' - ' + esc(v.vehicleModel) : ''}</span></div>
        <div class="row-actions">
          <button class="mini" data-act="edit" data-id="${v.id}" data-m="${esc(v.vehicleModel || '')}">Edit model</button>
          <button class="cancel-btn" data-act="del" data-id="${v.id}">Remove</button>
        </div></div>`).join('')
      : '<div class="empty-state"><div class="stencil">No vehicles yet</div>Add your plate above so the gate can recognize your car.</div>';
  } catch (e) { showToast(e.message); }
}
document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth('DRIVER')) return;
  load();
  $('#vehForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const err = $('#formError'); err.classList.remove('show');
    try {
      await api('POST', '/api/vehicles', { plateNumber: $('#plateInput').value, vehicleType: $('#vehicleType').value, vehicleModel: $('#modelInput').value });
      $('#vehForm').reset(); showToast('Vehicle registered.'); load();
    } catch (ex) { err.textContent = ex.message; err.classList.add('show'); }
  });
  $('#vehList').addEventListener('click', async (e) => {
    const b = e.target.closest('[data-act]'); if (!b) return;
    try {
      if (b.dataset.act === 'edit') {
        const m = prompt('Vehicle model', b.dataset.m); if (m === null) return;
        await api('PUT', '/api/vehicles/' + b.dataset.id, { vehicleModel: m }); showToast('Vehicle updated.');
      } else {
        if (!confirm('Remove this vehicle?')) return;
        await api('DELETE', '/api/vehicles/' + b.dataset.id); showToast('Vehicle removed.');
      }
      load();
    } catch (ex) { showToast(ex.message); }
  });
});
