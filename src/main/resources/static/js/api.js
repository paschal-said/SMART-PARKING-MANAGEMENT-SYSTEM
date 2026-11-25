/* ParkBay - api.js: session storage + fetch wrapper for the Spring Boot backend (same origin). */
const TOKEN_KEY = 'parkbay_token', USER_KEY = 'parkbay_user';
function $(s) { return document.querySelector(s); }
function esc(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])); }
function getToken() { try { return localStorage.getItem(TOKEN_KEY); } catch (e) { return null; } }
function getUser() { try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch (e) { return null; } }
function setSession(token, user) { localStorage.setItem(TOKEN_KEY, token); localStorage.setItem(USER_KEY, JSON.stringify(user)); }
function clearSession() { try { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); } catch (e) {} }

async function api(method, path, body) {
  const token = getToken();
  const res = await fetch(path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (res.status === 401 && token) {           // token expired or account disabled
    clearSession();
    window.location.href = 'login.html';
    throw new Error('Your session ended. Please log in again.');
  }
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) {}
  if (!res.ok) {
    throw new Error(data && data.fields ? Object.values(data.fields).join(', ')
      : (data && data.message) || 'Request failed (' + res.status + ')');
  }
  return data;
}
