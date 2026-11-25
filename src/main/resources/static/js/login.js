/* ParkBay - login.js: real login and driver registration against /api/auth. */
let mode = 'login';
function nextPage(user) {
  const n = new URLSearchParams(window.location.search).get('next');
  return user.role === 'ADMIN' ? 'admin.html' : (n && /^[\w.-]+\.html$/.test(n) && n !== 'admin.html' ? n : 'index.html');
}
function setMode(m) {
  mode = m;
  const reg = m === 'register';
  $('#regFields').style.display = reg ? 'block' : 'none';
  $('#authTitle').textContent = reg ? 'Create your driver account' : 'Welcome back';
  $('#authSub').textContent = reg ? 'Register once, then add your vehicles.' : 'Log in to see live parking availability.';
  $('#submitBtn').textContent = reg ? 'Create account' : 'Log in';
  $('#switchText').textContent = reg ? 'Already registered?' : 'New driver?';
  $('#switchBtn').textContent = reg ? 'Log in' : 'Create an account';
  $('#loginError').classList.remove('show');
}
function init() {
  if (isLoggedIn()) { window.location.href = homeFor(getUser()); return; }
  $('#switchBtn').addEventListener('click', () => setMode(mode === 'login' ? 'register' : 'login'));
  $('#loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const err = $('#loginError'), btn = $('#submitBtn');
    err.classList.remove('show');
    const body = { email: $('#emailInput').value.trim(), password: $('#passwordInput').value };
    if (mode === 'register') { body.fullName = $('#nameInput').value.trim(); body.phone = $('#phoneInput').value.trim(); }
    btn.disabled = true;
    try {
      const res = await api('POST', '/api/auth/' + mode, body);
      setSession(res.token, res.user);
      window.location.href = nextPage(res.user);
    } catch (ex) {
      err.textContent = ex.message; err.classList.add('show'); btn.disabled = false;
    }
  });
}
document.addEventListener('DOMContentLoaded', init);
