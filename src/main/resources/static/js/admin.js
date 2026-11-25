/* ParkBay - admin.js: administrator console (floors/bays, users, vehicles) via /api/admin/**. */
let tab = 'floors';
const TABS = [['floors', 'Floors and bays'], ['users', 'Users'], ['vehicles', 'Vehicles']];
const tag = a => `<span class="tag ${a ? 'ok' : 'off'}">${a ? 'Active' : 'Disabled'}</span>`;
const table = (h, rows, empty) => rows.length
  ? `<div class="tbl-wrap"><table class="tbl"><thead><tr>${h.map(x => `<th>${x}</th>`).join('')}</tr></thead><tbody>${rows.map(r => `<tr>${r.map(c => `<td>${c}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`
  : `<div class="empty-state">${empty}</div>`;

const VIEWS = {
  async floors() {
    const fl = await api('GET', '/api/floors');
    return `<form class="inline-form" data-form="floor"><input name="name" placeholder="New floor name" required maxlength="60"><button class="search-btn">Add floor</button></form>` +
      fl.map(f => `<div class="admin-floor"><div class="section-head"><h2>${esc(f.name)}</h2>
        <button class="mini" data-act="rename" data-id="${f.id}" data-n="${esc(f.name)}">Rename floor</button></div>
        <div class="bay-map">${f.slots.map(s => {
          const st = !s.active ? 'disabled' : s.observedOccupancy.toLowerCase();
          return `<div class="bay adm ${st}" data-act="slot" data-id="${s.id}" data-a="${!s.active}" title="Click to ${s.active ? 'disable' : 'enable'}">${esc(s.slotNumber)}<span class="type">${s.active ? s.observedOccupancy.toLowerCase() : 'closed'}</span></div>`;
        }).join('') || '<span style="color:#F2F0EA">No bays yet.</span>'}</div>
        <form class="inline-form" data-form="slot" data-id="${f.id}"><input name="slotNumber" placeholder="New bay, for example C1" required maxlength="20"><button class="mini">Add bay</button></form>
        <p class="hint">Click a bay to disable or enable it.</p></div>`).join('');
  },
  async users() {
    const me = getUser().id, u = await api('GET', '/api/admin/users');
    return table(['Name', 'Email', 'Phone', 'Role', 'Status', 'Actions'], u.map(x => [esc(x.fullName), esc(x.email), esc(x.phone || '-'), x.role === 'ADMIN' ? 'Admin' : 'Driver', tag(x.active),
      (x.id === me ? '<span class="hint">You</span>' : `<button class="mini" data-act="uact" data-id="${x.id}" data-a="${!x.active}">${x.active ? 'Deactivate' : 'Activate'}</button>`) +
      ` <button class="mini" data-act="upw" data-id="${x.id}">Reset password</button>`]), 'No users.');
  },
  async vehicles() {
    const v = await api('GET', '/api/admin/vehicles');
    return table(['Plate', 'Owner ID', 'Type', 'Model', 'Status', 'Actions'], v.map(x => [`<b>${esc(x.plateNumber)}</b>`, x.userId, esc(x.vehicleType), esc(x.vehicleModel || '-'), tag(x.active),
      `<button class="mini" data-act="vact" data-id="${x.id}" data-a="${!x.active}">${x.active ? 'Deactivate' : 'Activate'}</button>`]), 'No vehicles registered.');
  },
};

async function render() {
  $('#tabs').innerHTML = TABS.map(([k, l]) => `<button class="${tab === k ? 'on' : ''}" data-tab="${k}">${l}</button>`).join('');
  try { $('#adminBody').innerHTML = await VIEWS[tab](); } catch (e) { showToast(e.message); }
}
async function act(fn, msg) { try { await fn(); if (msg) showToast(msg); render(); } catch (e) { showToast(e.message); } }

document.addEventListener('DOMContentLoaded', () => {
  if (!requireAuth('ADMIN')) return;
  render();
  document.addEventListener('click', (e) => {
    const t = e.target.closest('[data-tab]'); if (t) { tab = t.dataset.tab; return render(); }
    const b = e.target.closest('[data-act]'); if (!b) return;
    const d = b.dataset, on = d.a === 'true';
    if (d.act === 'rename') { const n = prompt('New floor name', d.n); if (n) act(() => api('PUT', '/api/admin/floors/' + d.id, { name: n }), 'Floor renamed.'); }
    else if (d.act === 'slot') act(() => api('PATCH', '/api/admin/slots/' + d.id + '/active', { active: on }), 'Bay updated.');
    else if (d.act === 'uact') act(() => api('PATCH', '/api/admin/users/' + d.id + '/active', { active: on }), 'User updated.');
    else if (d.act === 'vact') act(() => api('PATCH', '/api/admin/vehicles/' + d.id + '/active', { active: on }), 'Vehicle updated.');
    else if (d.act === 'upw') { const p = prompt('New password (at least 8 characters)'); if (p) act(() => api('POST', '/api/admin/users/' + d.id + '/reset-password', { newPassword: p }), 'Password reset.'); }
  });
  document.addEventListener('submit', (e) => {
    const f = e.target.closest('[data-form]'); if (!f) return;
    e.preventDefault();
    const v = Object.fromEntries(new FormData(f));
    if (f.dataset.form === 'floor') act(() => api('POST', '/api/admin/floors', v), 'Floor added.');
    else act(() => api('POST', '/api/admin/floors/' + f.dataset.id + '/slots', v), 'Bay added.');
  });
});
