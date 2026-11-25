/* ParkBay - auth.js: route guards and logout. Roles: DRIVER and ADMIN. */
function isLoggedIn() { return !!getToken() && !!getUser(); }
function homeFor(user) { return user && user.role === 'ADMIN' ? 'admin.html' : 'index.html'; }

// Call first on every protected page. Pass 'DRIVER' or 'ADMIN' to restrict the page to one role.
function requireAuth(role) {
  if (!isLoggedIn()) {
    const here = window.location.pathname.split('/').pop() || 'index.html';
    window.location.href = 'login.html?next=' + encodeURIComponent(here);
    return false;
  }
  const user = getUser();
  if (role && user.role !== role) { window.location.href = homeFor(user); return false; }
  return true;
}
function logout() { clearSession(); window.location.href = 'login.html'; }
