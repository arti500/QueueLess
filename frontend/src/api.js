const API = import.meta.env.VITE_API_URL || '/api/v1';
async function request(path, options = {}) {
  const response = await fetch(`${API}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(localStorage.getItem('queueless_access_token') ? { Authorization: `Bearer ${localStorage.getItem('queueless_access_token')}` } : {}), ...(options.headers || {}) },
  });
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (!response.ok) {
    const message = data?.detail || data?.message || data?.error || (typeof data === 'string' ? data : null) || `Request failed (${response.status})`;
    throw new Error(message.replace(/^"|"$/g, ''));
  }
  return data;
}
export const api = {
  login: (body) => request('/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  me: () => request('/auth/me'),
  health: () => request('/health'),
  queues: () => request('/queues'),
  createQueue: (body) => request('/queues', { method: 'POST', body: JSON.stringify(body) }),
  queue: (id) => request(`/queues/${id}`),
  entries: (id) => request(`/queues/${id}/entries`),
  join: (id, body) => request(`/queues/${id}/join`, { method: 'POST', body: JSON.stringify(body) }),
  callNext: (id) => request(`/queues/${id}/call-next`, { method: 'POST' }),
  updateStatus: (queueId, entryId, status) => request(`/queues/${queueId}/entries/${entryId}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
};
