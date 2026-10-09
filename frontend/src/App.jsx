import { useCallback, useEffect, useMemo, useState } from 'react';
import { api } from './api';

const fmtTime = (value) => value ? new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '—';
const pretty = (status) => status?.toLowerCase().replace('_', ' ');

export default function App() {
  const [queues, setQueues] = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [entries, setEntries] = useState([]);
  const [queueDetails, setQueueDetails] = useState(null);
  const [createForm, setCreateForm] = useState({ name: '', businessName: '', maxCapacity: '30' });
  const [joinForm, setJoinForm] = useState({ customerName: '', phone: '' });
  const [myToken, setMyToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem('queueless_access_token') || '');
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [staffUser, setStaffUser] = useState(() => localStorage.getItem('queueless_username') || '');
  const selected = useMemo(() => queues.find(q => String(q.id) === String(selectedId)), [queues, selectedId]);

  const refresh = useCallback(async (id = selectedId) => {
    try {
      const list = await api.queues();
      setQueues(list);
      if (!id && list.length) { setSelectedId(String(list[0].id)); id = String(list[0].id); }
      if (id) {
        const [details, rows] = await Promise.all([api.queue(id), api.entries(id)]);
        setQueueDetails(details); setEntries(rows);
      } else { setQueueDetails(null); setEntries([]); }
      setError('');
    } catch (e) { setError(`${e.message}. Check that the backend and PostgreSQL are running.`); }
    finally { setLoading(false); }
  }, [selectedId]);

  useEffect(() => { refresh(selectedId); }, [selectedId]);
  useEffect(() => { const timer = setInterval(() => refresh(selectedId), 5000); return () => clearInterval(timer); }, [refresh, selectedId]);

  async function login(e) {
    e.preventDefault(); setBusy(true); setError(''); setNotice('');
    try { const result = await api.login(loginForm); localStorage.setItem('queueless_access_token', result.accessToken); localStorage.setItem('queueless_username', result.username); setAccessToken(result.accessToken); setStaffUser(result.username); setLoginForm({username:'',password:''}); setNotice(`Signed in as ${result.username} (${result.role}).`); } catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  function logout() { localStorage.removeItem('queueless_access_token'); localStorage.removeItem('queueless_username'); setAccessToken(''); setStaffUser(''); setNotice('Signed out.'); }

  async function createQueue(e) {
    e.preventDefault(); setBusy(true); setError(''); setNotice('');
    try {
      const q = await api.createQueue({ ...createForm, maxCapacity: Number(createForm.maxCapacity) });
      setCreateForm({ name: '', businessName: '', maxCapacity: '30' });
      setSelectedId(String(q.id)); setNotice('Queue created successfully.'); await refresh(String(q.id));
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  async function joinQueue(e) {
    e.preventDefault(); if (!selectedId) return;
    setBusy(true); setError(''); setNotice('');
    try {
      const token = await api.join(selectedId, joinForm);
      setMyToken(token); setJoinForm({ customerName: '', phone: '' }); setNotice(`You're in! Your token is #${token.tokenNumber}.`); await refresh(selectedId);
    } catch (e) { setError(e.message); } finally { setBusy(false); }
  }
  async function action(fn, success) {
    setBusy(true); setError(''); setNotice('');
    try { await fn(); setNotice(success); await refresh(selectedId); }
    catch (e) { setError(e.message); } finally { setBusy(false); }
  }

  const activeEntries = entries.filter(e => ['WAITING', 'CALLED'].includes(e.status));
  const called = activeEntries.find(e => e.status === 'CALLED');
  const waiting = entries.filter(e => e.status === 'WAITING');

  return <div className="app-shell">
    <header className="topbar"><a className="brand" href="#top"><span className="brand-mark">Q</span><span>QueueLess<small>WAIT LESS. LIVE MORE.</small></span></a><div className="top-status"><span className="pulse" /> Virtual queue platform <span className="live-pill">LIVE</span>{accessToken ? <><span>{staffUser}</span><button className="small-primary" onClick={logout}>Sign out</button></> : <span>Customer access</span>}</div></header>
    <main id="top" className="main-layout">
      <section className="hero"><div className="eyebrow"><span className="eyebrow-line"/> SMART QUEUE MANAGEMENT</div><h1>Your time matters.<br/><em>Your queue, simplified.</em></h1><p>Create a queue for your business, let customers join remotely, and keep everyone updated without crowded waiting rooms.</p><div className="hero-stats"><div><strong>{queues.length.toString().padStart(2, '0')}</strong><span>Active queues</span></div><div><strong>{queues.reduce((sum, q) => sum + (q.waitingCount || 0), 0).toString().padStart(2, '0')}</strong><span>People waiting</span></div><div><strong>5<span className="unit"> min</span></strong><span>Default service estimate</span></div></div></section>

      {error && <div className="alert error" role="alert"><span>!</span>{error}<button onClick={() => setError('')} aria-label="Dismiss error">×</button></div>}
      {notice && <div className="alert success" role="status"><span>✓</span>{notice}<button onClick={() => setNotice('')} aria-label="Dismiss message">×</button></div>}

      {!accessToken && <section className="panel" style={{marginBottom: '22px'}}><div className="panel-heading"><div><span className="section-kicker">STAFF ACCESS</span><h2>Staff sign in</h2></div></div><p className="panel-intro">Sign in to create queues and manage customers. Customers can still browse and join queues without an account.</p><form className="join-form" onSubmit={login}><label>Username<input required autoComplete="username" value={loginForm.username} onChange={e=>setLoginForm({...loginForm,username:e.target.value})}/></label><label>Password<input required type="password" autoComplete="current-password" value={loginForm.password} onChange={e=>setLoginForm({...loginForm,password:e.target.value})}/></label><button className="primary-btn" disabled={busy} type="submit">Sign in <span>↗</span></button></form></section>}
      <div className="content-grid">
        <aside className="left-column">
          <section className="panel create-panel"><div className="panel-heading"><div><span className="section-kicker">GET STARTED</span><h2>Create a queue</h2></div><span className="heading-icon">＋</span></div>
            {accessToken ? <form onSubmit={createQueue} className="form-stack">
              <label>Queue name<input required maxLength="100" placeholder="e.g. General Consultation" value={createForm.name} onChange={e => setCreateForm({...createForm, name:e.target.value})}/></label>
              <label>Business name<input required maxLength="150" placeholder="e.g. City Clinic" value={createForm.businessName} onChange={e => setCreateForm({...createForm, businessName:e.target.value})}/></label>
              <label>Maximum active customers<input required type="number" min="1" max="10000" value={createForm.maxCapacity} onChange={e => setCreateForm({...createForm, maxCapacity:e.target.value})}/></label>
              <button className="primary-btn" disabled={busy} type="submit">{busy ? 'Please wait…' : 'Create queue'} <span>↗</span></button>
            </form> : <p className="panel-intro">Sign in as staff to create a queue.</p>}
          </section>
          <section className="panel queue-list-panel"><div className="panel-heading"><div><span className="section-kicker">BROWSE</span><h2>Available queues</h2></div><span className="count-chip">{queues.length}</span></div>
            {loading ? <div className="empty-state">Loading queues…</div> : queues.length === 0 ? <div className="empty-state"><span className="empty-icon">◷</span><strong>No queues yet</strong><span>Create your first queue to get started.</span></div> : <div className="queue-list">{queues.map(q => <button key={q.id} className={`queue-item ${String(q.id) === String(selectedId) ? 'selected' : ''}`} onClick={() => { setSelectedId(String(q.id)); setMyToken(null); }}><span className="queue-avatar">{q.businessName?.slice(0,1).toUpperCase() || 'Q'}</span><span className="queue-item-copy"><strong>{q.name}</strong><small>{q.businessName}</small><small>{q.waitingCount} waiting · {q.maxCapacity} capacity</small></span><span className="chevron">›</span></button>)}</div>}
          </section>
        </aside>

        <section className="right-column">
          {!selected ? <section className="panel no-selection"><div className="large-clock">◷</div><h2>Your queue dashboard awaits</h2><p>Create or select a queue to see its live status and manage customers.</p></section> : <>
            <section className="panel dashboard-panel"><div className="dashboard-title"><div><span className="section-kicker">STAFF DASHBOARD</span><h2>{selected.name}</h2><p>{selected.businessName} <span className="dot-separator">•</span> Queue ID #{selected.id}</p></div><span className="open-badge"><i/> {selected.active ? 'Accepting customers' : 'Paused'}</span></div>
              <div className="metric-grid"><div className="metric-card"><span className="metric-icon purple">◷</span><span className="metric-label">Waiting now</span><strong>{queueDetails?.waitingCount ?? selected.waitingCount}</strong><small>customers in line</small></div><div className="metric-card"><span className="metric-icon green">✓</span><span className="metric-label">Currently called</span><strong>{queueDetails?.calledCount ?? selected.calledCount}</strong><small>being served</small></div><div className="metric-card"><span className="metric-icon orange">⌁</span><span className="metric-label">Est. wait</span><strong>{queueDetails?.estimatedWaitMinutes ?? selected.estimatedWaitMinutes}<span className="unit"> min</span></strong><small>for the waiting line</small></div></div>
              <div className="now-serving"><div className="serving-label"><span className="serving-pulse"/> NOW SERVING</div>{called ? <div className="serving-content"><div className="token-bubble">#{called.tokenNumber}</div><div><strong>{called.customerName}</strong><span>Called at {fmtTime(called.calledAt)}</span></div>{accessToken && <button className="small-primary" disabled={busy} onClick={() => action(() => api.updateStatus(selectedId, called.id, 'COMPLETED'), `Token #${called.tokenNumber} marked completed.`)}>Complete ✓</button>}</div> : <div className="not-serving">No customer is being served right now.</div>}</div>
              {accessToken && <button className="call-next-btn" disabled={busy || waiting.length === 0} onClick={() => action(() => api.callNext(selectedId), 'Next customer has been called.')}><span className="call-icon">↗</span><span><strong>Call next customer</strong><small>{waiting.length ? `Token #${waiting[0].tokenNumber} · ${waiting[0].customerName}` : 'No one is waiting in line'}</small></span><span className="button-arrow">→</span></button>}
            </section>

            <section className="panel join-panel"><div className="panel-heading"><div><span className="section-kicker">CUSTOMER PORTAL</span><h2>Join this queue</h2></div><span className="heading-icon">↗</span></div><p className="panel-intro">Enter your details to get a token. You can keep this page open to track the queue.</p>
              <form onSubmit={joinQueue} className="join-form"><label>Your full name<input required maxLength="120" placeholder="e.g. Asha Patil" value={joinForm.customerName} onChange={e => setJoinForm({...joinForm, customerName:e.target.value})}/></label><label>Phone number <span className="optional">(optional)</span><input maxLength="30" placeholder="e.g. 9876543210" value={joinForm.phone} onChange={e => setJoinForm({...joinForm, phone:e.target.value})}/></label><button className="primary-btn" disabled={busy || !selected.active} type="submit">Get my token <span>↗</span></button></form>
              {myToken && String(myToken.queueId) === String(selectedId) && <div className="my-token"><div><span className="section-kicker">YOUR TOKEN</span><strong>#{myToken.tokenNumber}</strong><span>{myToken.customerName} · {pretty(myToken.status)}</span></div><div className="token-wait"><span>Estimated wait</span><strong>{myToken.estimatedWaitMinutes} min</strong></div></div>}
            </section>

            <section className="panel people-panel"><div className="panel-heading"><div><span className="section-kicker">QUEUE OVERVIEW</span><h2>Customer line</h2></div><span className="count-chip">{activeEntries.length} active</span></div>
              {entries.length === 0 ? <div className="empty-state compact">No customers have joined this queue yet.</div> : <div className="table-wrap"><table><thead><tr><th>TOKEN</th><th>CUSTOMER</th><th>JOINED</th><th>STATUS</th><th>ACTIONS</th></tr></thead><tbody>{entries.map(entry => <tr key={entry.id}><td><span className="table-token">#{entry.tokenNumber}</span></td><td><strong>{entry.customerName}</strong>{entry.phone && <small className="phone-text">{entry.phone}</small>}</td><td>{fmtTime(entry.joinedAt)}</td><td><span className={`status status-${entry.status.toLowerCase()}`}>{pretty(entry.status)}</span></td><td>{accessToken && ['WAITING','CALLED'].includes(entry.status) ? <div className="row-actions"><button disabled={busy} title="Skip customer" onClick={() => action(() => api.updateStatus(selectedId, entry.id, 'SKIPPED'), `Token #${entry.tokenNumber} skipped.`)}>Skip</button><button disabled={busy} title="Cancel token" onClick={() => action(() => api.updateStatus(selectedId, entry.id, 'CANCELLED'), `Token #${entry.tokenNumber} cancelled.`)}>Cancel</button></div> : <span className="muted-dash">—</span>}</td></tr>)}</tbody></table></div>}
              <div className="panel-footnote"><span className="auto-refresh-dot"/> Updates automatically every 5 seconds</div>
            </section>
          </>}
        </section>
      </div>
    </main>
    <footer className="footer"><span>© 2026 QueueLess</span><span>Thoughtful technology for less waiting.</span><span><span className="pulse"/> Service status: local demo</span></footer>
  </div>;
}
