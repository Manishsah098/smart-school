/* ============================================================
   SmartSchool — Admin App Logic
   ============================================================ */
'use strict';

let allStudents = [];
let auditPage   = 0;
let resetTarget = null;  // { type: 'student'|'teacher'|'parent', id }

/* ---- Boot ---- */
document.addEventListener('DOMContentLoaded', async () => {
  if (!initPage('ROLE_ADMIN')) return;

  // Set greeting
  const user = Auth.getUser();
  const nameEl = document.getElementById('greeting-name');
  if (nameEl) nameEl.textContent = (user?.displayName || user?.username || 'Admin').split(' ')[0];
  const dateEl = document.getElementById('greeting-date');
  if (dateEl) dateEl.textContent = new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' });

  await loadDashboard();
  await preloadSections();
});

/* ---- Section switching ---- */
const sections = ['dashboard','students','teachers','parents','classes','fees','notices','events','audit'];
function showSection(name, navEl) {
  sections.forEach(s => {
    const el = document.getElementById('sec-' + s);
    if (el) el.style.display = s === name ? '' : 'none';
  });
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (navEl) navEl.classList.add('active');
  const titleMap = { dashboard:'Dashboard', students:'Students', teachers:'Teachers', parents:'Parents', classes:'Classes & Sections', fees:'Fee Reports', notices:'Notices', events:'Events', audit:'Audit Logs' };
  const tt = document.getElementById('topbar-title');
  if (tt) tt.textContent = titleMap[name] || name;
  // Lazy-load
  if (name === 'students')  loadStudents();
  if (name === 'teachers')  loadTeachers();
  if (name === 'parents')   loadParents();
  if (name === 'classes')   loadClasses();
  if (name === 'fees')      loadFeeReport();
  if (name === 'notices')   loadNotices();
  if (name === 'events')    loadEvents();
  if (name === 'audit')     loadAudit(0);
}

/* ---- Dashboard ---- */
async function loadDashboard() {
  const res = await AdminAPI.dashboard();
  if (!res.success) return;
  const d = res.data;
  document.getElementById('s-students').textContent = d.totalStudents ?? '—';
  document.getElementById('s-teachers').textContent = d.totalTeachers ?? '—';
  document.getElementById('s-classes').textContent  = d.totalSections ?? '—';
  const feeAmt = d.totalFeeCollected != null ? '₹' + Number(d.totalFeeCollected).toLocaleString('en-IN') : '—';
  document.getElementById('s-fees').textContent = feeAmt;
  renderRecentNotices(d.recentNotices || []);
  renderRecentEvents(d.recentEvents || []);
}

function renderRecentNotices(notices) {
  const el = document.getElementById('recent-notices');
  if (!notices.length) { el.innerHTML = UI.emptyState('No recent notices', 'bi-megaphone'); return; }
  el.innerHTML = notices.slice(0,4).map(n => `
    <div class="notice-item">
      <div class="d-flex justify-content-between align-items-start">
        <div class="notice-title">${n.title}</div>
        <span class="badge-ss badge-info ms-2">${n.audience || ''}</span>
      </div>
      <div class="notice-meta"><i class="bi bi-clock me-1"></i>${UI.fmtDate(n.createdAt)}</div>
      <div class="notice-body">${(n.content || '').substring(0,120)}${(n.content||'').length>120?'…':''}</div>
    </div>`).join('');
}

function renderRecentEvents(events) {
  const el = document.getElementById('recent-events');
  if (!events.length) { el.innerHTML = UI.emptyState('No upcoming events', 'bi-calendar-x'); return; }
  el.innerHTML = events.slice(0,5).map(ev => `
    <div class="d-flex align-items-center gap-3 mb-3">
      <div style="min-width:42px;height:42px;background:rgba(79,70,229,.15);border-radius:10px;display:flex;align-items:center;justify-content:center;color:#818cf8;font-size:1.1rem;">
        <i class="bi bi-calendar-event-fill"></i>
      </div>
      <div>
        <div style="font-weight:600;font-size:.875rem;">${ev.title}</div>
        <div style="font-size:.75rem;color:var(--clr-text-muted);">${UI.fmtDate(ev.eventDate)} · <span class="badge-ss badge-muted">${ev.eventType||''}</span></div>
      </div>
    </div>`).join('');
}

/* ---- Pre-load sections data ---- */
async function preloadSections() {
  // Load sections for student form dropdown
  try {
    const cr = await AdminAPI.allClasses();
    if (cr.success && cr.data?.length) {
      const classId = cr.data[0].id;
      const sr = await AdminAPI.allSections(classId);
      if (sr.success) {
        UI.fillSelect('s-section',
          sr.data.map(s => ({ value: s.id, label: `${s.className || ''} - ${s.name}` })),
          'Select section');
      }
    }
  } catch {}
}

/* ---- Students ---- */
async function loadStudents() {
  document.getElementById('students-tbody').innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Loading…</td></tr>';
  const res = await AdminAPI.allStudents();
  if (!res.success) { document.getElementById('students-tbody').innerHTML = `<tr><td colspan="5" class="text-center py-4 text-danger">${res.message}</td></tr>`; return; }
  allStudents = res.data || [];
  renderStudentsTable(allStudents);
}

function renderStudentsTable(list) {
  const tbody = document.getElementById('students-tbody');
  if (!list.length) { tbody.innerHTML = `<tr><td colspan="5">${UI.emptyState('No students found')}</td></tr>`; return; }
  tbody.innerHTML = list.map(s => `
    <tr>
      <td><span class="badge-ss badge-primary">${s.studentId || s.admissionNumber || ''}</span></td>
      <td><strong>${s.name}</strong></td>
      <td>${s.className || ''} ${s.sectionName ? '— '+s.sectionName : ''}</td>
      <td><span class="badge-ss ${s.status === 'ACTIVE' ? 'badge-success' : 'badge-muted'}">${s.status||''}</span></td>
      <td>
        <div class="d-flex gap-1">
          <button class="btn-ghost" style="padding:.25rem .55rem;font-size:.8rem;" onclick="openResetModal('student',${s.id})" title="Reset Password"><i class="bi bi-key"></i></button>
        </div>
      </td>
    </tr>`).join('');
}

function filterStudents(q) {
  const lq = q.toLowerCase();
  renderStudentsTable(allStudents.filter(s =>
    (s.name||'').toLowerCase().includes(lq) ||
    (s.studentId||'').toLowerCase().includes(lq) ||
    (s.className||'').toLowerCase().includes(lq)));
}

async function saveStudent() {
  const btn = document.getElementById('btn-save-student');
  const errDiv = document.getElementById('student-add-error');
  const errMsg = document.getElementById('student-add-error-msg');
  errDiv.style.display = 'none';

  const data = {
    name: document.getElementById('s-name').value.trim(),
    dateOfBirth: document.getElementById('s-dob').value || null,
    gender: document.getElementById('s-gender').value || null,
    phone: document.getElementById('s-phone').value.trim() || null,
    sectionId: document.getElementById('s-section').value || null,
    address: document.getElementById('s-address').value.trim() || null,
  };
  if (!data.name || !data.sectionId) {
    errMsg.textContent = 'Name and Section are required.'; errDiv.style.display = 'flex'; return;
  }
  UI.btnLoading(btn, true);
  const res = await AdminAPI.createStudent(data);
  UI.btnLoading(btn, false);
  if (!res.success) { errMsg.textContent = res.message; errDiv.style.display = 'flex'; return; }
  closeModal('modal-add-student');
  UI.toast('Student created. Username: ' + res.data.username + ' | Temp password shown in logs.', 'success', 8000);
  loadStudents();
}

/* ---- Teachers ---- */
async function loadTeachers() {
  document.getElementById('teachers-tbody').innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Loading…</td></tr>';
  const res = await AdminAPI.allTeachers();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('teachers-tbody').innerHTML = `<tr><td colspan="5">${UI.emptyState('No teachers found')}</td></tr>`; return; }
  document.getElementById('teachers-tbody').innerHTML = list.map(t => `
    <tr>
      <td><span class="badge-ss badge-info">${t.employeeId||''}</span></td>
      <td><strong>${t.name}</strong></td>
      <td>${t.subject||'—'}</td>
      <td><span class="badge-ss ${t.status==='ACTIVE'?'badge-success':'badge-muted'}">${t.status||''}</span></td>
      <td>
        <button class="btn-ghost" style="padding:.25rem .55rem;font-size:.8rem;" onclick="openResetModal('teacher',${t.id})" title="Reset Password"><i class="bi bi-key"></i></button>
      </td>
    </tr>`).join('');
}

async function saveTeacher() {
  const btn = document.getElementById('btn-save-teacher');
  const errDiv = document.getElementById('teacher-add-error');
  const errMsg = document.getElementById('teacher-add-error-msg');
  errDiv.style.display = 'none';
  const data = {
    name: document.getElementById('t-name').value.trim(),
    email: document.getElementById('t-email').value.trim() || null,
    phone: document.getElementById('t-phone').value.trim() || null,
    qualification: document.getElementById('t-qual').value.trim() || null,
  };
  if (!data.name) { errMsg.textContent = 'Name is required.'; errDiv.style.display = 'flex'; return; }
  UI.btnLoading(btn, true);
  const res = await AdminAPI.createTeacher(data);
  UI.btnLoading(btn, false);
  if (!res.success) { errMsg.textContent = res.message; errDiv.style.display = 'flex'; return; }
  closeModal('modal-add-teacher');
  UI.toast('Teacher created. Employee ID: ' + res.data.employeeId, 'success', 6000);
  loadTeachers();
}

/* ---- Parents ---- */
async function loadParents() {
  document.getElementById('parents-tbody').innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">Loading…</td></tr>';
  const res = await AdminAPI.allParents();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('parents-tbody').innerHTML = `<tr><td colspan="4">${UI.emptyState('No parents found')}</td></tr>`; return; }
  document.getElementById('parents-tbody').innerHTML = list.map(p => `
    <tr>
      <td>${p.phone||''}</td>
      <td><strong>${p.name}</strong></td>
      <td>${(p.childrenNames||[]).join(', ') || '—'}</td>
      <td>
        <button class="btn-ghost" style="padding:.25rem .55rem;font-size:.8rem;" onclick="openResetModal('parent',${p.id})" title="Reset Password"><i class="bi bi-key"></i></button>
      </td>
    </tr>`).join('');
}

async function saveParent() {
  const btn = document.getElementById('btn-save-parent');
  const errDiv = document.getElementById('parent-add-error');
  const errMsg = document.getElementById('parent-add-error-msg');
  errDiv.style.display = 'none';
  const childrenRaw = document.getElementById('p-children').value.trim();
  const data = {
    name: document.getElementById('p-name').value.trim(),
    phone: document.getElementById('p-phone').value.trim(),
    email: document.getElementById('p-email').value.trim() || null,
    childStudentIds: childrenRaw ? childrenRaw.split(',').map(s => s.trim()).filter(Boolean) : [],
  };
  if (!data.name || !data.phone) { errMsg.textContent = 'Name and Phone are required.'; errDiv.style.display = 'flex'; return; }
  UI.btnLoading(btn, true);
  const res = await AdminAPI.createParent(data);
  UI.btnLoading(btn, false);
  if (!res.success) { errMsg.textContent = res.message; errDiv.style.display = 'flex'; return; }
  closeModal('modal-add-parent');
  UI.toast('Parent account created.', 'success');
  loadParents();
}

/* ---- Classes ---- */
async function loadClasses() {
  document.getElementById('classes-list').innerHTML = '<div class="col-12 text-muted">Loading…</div>';
  const res = await AdminAPI.allClasses();
  if (!res.success) { document.getElementById('classes-list').innerHTML = '<div class="col-12 text-danger">' + res.message + '</div>'; return; }
  const classes = res.data || [];
  if (!classes.length) { document.getElementById('classes-list').innerHTML = `<div class="col-12">${UI.emptyState('No classes found')}</div>`; return; }
  document.getElementById('classes-list').innerHTML = classes.map(c => `
    <div class="col-md-6 col-lg-4">
      <div class="card-glass p-4">
        <div class="d-flex align-items-center gap-2 mb-2">
          <i class="bi bi-building text-primary fs-5"></i>
          <h6 class="mb-0 fw-bold">${c.name}</h6>
        </div>
        <div class="text-muted small mb-3">${c.academicYear || ''}</div>
        <div id="sections-${c.id}">
          <button class="btn-ghost" style="font-size:.8rem;padding:.3rem .75rem;" onclick="loadSections(${c.id})">
            <i class="bi bi-eye"></i> View Sections
          </button>
        </div>
      </div>
    </div>`).join('');
}

async function loadSections(classId) {
  const el = document.getElementById('sections-' + classId);
  el.innerHTML = '<span class="text-muted small">Loading…</span>';
  const res = await AdminAPI.allSections(classId);
  if (!res.success) { el.innerHTML = `<span class="text-danger small">${res.message}</span>`; return; }
  const sections = res.data || [];
  if (!sections.length) { el.innerHTML = UI.emptyState('No sections', 'bi-buildings'); return; }
  el.innerHTML = sections.map(s => `
    <div class="d-flex justify-content-between align-items-center mb-2">
      <div><span class="badge-ss badge-primary me-1">Section ${s.name}</span> <small class="text-muted">${s.roomNumber||''} · Cap: ${s.capacity||'—'}</small></div>
    </div>`).join('');
}

/* ---- Fee Report ---- */
async function loadFeeReport() {
  document.getElementById('fee-report-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await AdminAPI.feeReport();
  if (!res.success) { document.getElementById('fee-report-content').innerHTML = `<div class="text-danger">${res.message}</div>`; return; }
  const d = res.data || {};
  document.getElementById('fee-report-content').innerHTML = `
    <div class="row g-3 mb-4">
      <div class="col-6 col-md-3"><div class="stat-card"><div class="stat-icon" style="background:rgba(16,185,129,.15);color:#34d399;"><i class="bi bi-check-circle-fill"></i></div><div><div class="stat-value">₹${Number(d.totalCollected||0).toLocaleString('en-IN')}</div><div class="stat-label">Collected</div></div></div></div>
      <div class="col-6 col-md-3"><div class="stat-card"><div class="stat-icon" style="background:rgba(245,158,11,.15);color:#fbbf24;"><i class="bi bi-hourglass-split"></i></div><div><div class="stat-value">₹${Number(d.totalPending||0).toLocaleString('en-IN')}</div><div class="stat-label">Pending</div></div></div></div>
      <div class="col-6 col-md-3"><div class="stat-card"><div class="stat-icon" style="background:rgba(239,68,68,.15);color:#f87171;"><i class="bi bi-exclamation-triangle-fill"></i></div><div><div class="stat-value">₹${Number(d.totalOverdue||0).toLocaleString('en-IN')}</div><div class="stat-label">Overdue</div></div></div></div>
      <div class="col-6 col-md-3"><div class="stat-card"><div class="stat-icon" style="background:rgba(79,70,229,.15);color:#818cf8;"><i class="bi bi-receipt"></i></div><div><div class="stat-value">${d.totalPayments||0}</div><div class="stat-label">Payments Made</div></div></div></div>
    </div>`;
}

/* ---- Notices ---- */
async function loadNotices() {
  document.getElementById('notices-list').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await AdminAPI.notices();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('notices-list').innerHTML = UI.emptyState('No notices published', 'bi-megaphone'); return; }
  document.getElementById('notices-list').innerHTML = list.map(n => `
    <div class="notice-item">
      <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div class="notice-title">${n.title}</div>
        <div class="d-flex gap-1">
          <span class="badge-ss badge-info">${n.audience||''}</span>
          <span class="badge-ss badge-muted">${n.priority||''}</span>
        </div>
      </div>
      <div class="notice-meta"><i class="bi bi-person me-1"></i>${n.authorName||''} · <i class="bi bi-clock me-1"></i>${UI.fmtDate(n.createdAt)}</div>
      <div class="notice-body">${n.content||''}</div>
    </div>`).join('');
}

async function saveNotice() {
  const data = {
    title: document.getElementById('n-title').value.trim(),
    content: document.getElementById('n-content').value.trim(),
    audience: document.getElementById('n-audience').value,
    priority: document.getElementById('n-priority').value,
  };
  if (!data.title || !data.content) { UI.toast('Title and content are required.', 'warning'); return; }
  const res = await AdminAPI.createNotice(data);
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  closeModal('modal-add-notice');
  UI.toast('Notice published successfully.', 'success');
  loadNotices();
}

/* ---- Events ---- */
async function loadEvents() {
  document.getElementById('events-list').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await AdminAPI.events();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('events-list').innerHTML = UI.emptyState('No events', 'bi-calendar-x'); return; }
  document.getElementById('events-list').innerHTML = `
    <div class="row g-3">
      ${list.map(ev => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="mb-0 fw-bold">${ev.title}</h6>
              <span class="badge-ss badge-primary">${ev.eventType||''}</span>
            </div>
            <div class="text-muted small mb-2"><i class="bi bi-calendar-check me-1"></i>${UI.fmtDate(ev.eventDate)}</div>
            <div class="text-muted small">${ev.description||''}</div>
          </div>
        </div>`).join('')}
    </div>`;
}

async function saveEvent() {
  const data = {
    title: document.getElementById('ev-title').value.trim(),
    eventDate: document.getElementById('ev-date').value,
    eventType: document.getElementById('ev-type').value,
    description: document.getElementById('ev-desc').value.trim(),
  };
  if (!data.title || !data.eventDate) { UI.toast('Title and date are required.', 'warning'); return; }
  const res = await AdminAPI.createEvent(data);
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  closeModal('modal-add-event');
  UI.toast('Event created.', 'success');
  loadEvents();
}

/* ---- Audit Logs ---- */
async function loadAudit(page) {
  if (page < 0) return;
  auditPage = page;
  document.getElementById('audit-tbody').innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Loading…</td></tr>';
  const res = await AdminAPI.auditLogs(page);
  if (!res.success) return;
  const list = res.data?.content || res.data || [];
  if (!list.length) { document.getElementById('audit-tbody').innerHTML = `<tr><td colspan="5">${UI.emptyState('No audit logs')}</td></tr>`; return; }
  document.getElementById('audit-tbody').innerHTML = list.map(a => `
    <tr>
      <td style="white-space:nowrap;">${UI.fmtDateTime(a.createdAt)}</td>
      <td>${a.username||''}</td>
      <td><span class="badge-ss badge-info">${a.action||''}</span></td>
      <td>${a.entityType||''}</td>
      <td style="font-size:.8rem;color:var(--clr-text-muted);">${(a.details||'').substring(0,80)}</td>
    </tr>`).join('');
  document.getElementById('audit-page-info').textContent = `Page ${page + 1}`;
  document.getElementById('audit-prev').disabled = page === 0;
  document.getElementById('audit-next').disabled = (res.data?.last ?? list.length < 20);
}

/* ---- Password Reset ---- */
function openResetModal(type, id) {
  resetTarget = { type, id };
  document.getElementById('reset-pwd-result').style.display = 'none';
  document.getElementById('btn-do-reset').disabled = false;
  document.getElementById('btn-do-reset').innerHTML = '<i class="bi bi-key"></i> Reset Now';
  openModal('modal-reset-pwd');
}

async function doReset() {
  if (!resetTarget) return;
  const btn = document.getElementById('btn-do-reset');
  UI.btnLoading(btn, true);

  let res;
  if (resetTarget.type === 'student') res = await AdminAPI.resetPassword(resetTarget.id);
  else if (resetTarget.type === 'teacher') res = await AdminAPI.resetTeacherPwd(resetTarget.id);
  else res = await AdminAPI.resetParentPwd(resetTarget.id);

  UI.btnLoading(btn, false);
  if (!res.success) { UI.toast(res.message, 'danger'); return; }

  const pwdInput = document.getElementById('reset-pwd-value');
  pwdInput.value = res.data?.temporaryPassword || res.data || 'See server logs';
  document.getElementById('reset-pwd-result').style.display = '';
  btn.disabled = true;
  UI.toast('Password reset successfully. Please note the temporary password.', 'success', 8000);
}

function copyPwd() {
  const val = document.getElementById('reset-pwd-value').value;
  navigator.clipboard.writeText(val).then(() => UI.toast('Copied to clipboard!', 'info', 2000));
}

/* ---- Add Class modal ---- */
function openAddClassModal() { openModal('modal-add-class'); }
