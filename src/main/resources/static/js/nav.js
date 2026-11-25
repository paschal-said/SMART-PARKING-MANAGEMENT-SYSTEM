/* ParkBay - nav.js: role-based nav links, account button and toast. */
function buildNav() {
  const user = getUser();
  const links = $('.nav-links');
  if (user && links) {
    const items = user.role === 'ADMIN'
      ? [['index.html', 'Live availability'], ['admin.html', 'Admin']]
      : [['index.html', 'Find parking'], ['vehicles.html', 'My vehicles']];
    const here = window.location.pathname.split('/').pop() || 'index.html';
    links.innerHTML = items.map(([h, l]) => `<a href="${h}" class="${h === here ? 'active' : ''}">${l}</a>`).join('');
  }
  const btn = $('#accountBtn');
  if (btn) {
    btn.textContent = user ? user.fullName + ' - Log out' : 'Log in';
    btn.addEventListener('click', () => user ? logout() : (window.location.href = 'login.html'));
  }
}
function showToast(msg) {
  const t = $('#toast');
  if (!t) return;
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(showToast._t);
  showToast._t = setTimeout(() => t.classList.remove('show'), 3200);
}
document.addEventListener('DOMContentLoaded', buildNav);
