'use strict';

let children = [];
let selectedChildId = null;
let currentSection = 'dashboard';

document.addEventListener('DOMContentLoaded', async () => {
  if (!initPage('ROLE_PARENT')) return;
  const dateEl = document.getElementById('p-date');
  if (dateEl) dateEl.textContent = new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' });
  await loadChildren();
  if (selectedChildId) await loadDashboard();
});

const sections = ['dashboard','attendance','marks','homework','fees','timetable','notices'];
function showSection(name, navEl) {
  sections.forEach(s => { const el = document.getElementById('sec-' + s); if (el) el.style.display = s === name ? '' : 'none'; });
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (navEl) navEl.classList.add('active');
  currentSection = name;
  const titleMap = { dashboard:'Dashboard', attendance:'Attendance', marks:'Marks & Results', homework:'Homework', fees:'Fees', timetable:'Timetable', notices:'Notices' };
  const tt = document.getElementById('topbar-title');
  if (tt) tt.textContent = titleMap[name] || name;
  if (!selectedChildId) return;
  if (name === 'attendance') loadAttendance();
  if (name === 'marks')      loadMarks();
  if (name === 'homework')   loadHomework();
  if (name === 'fees')       loadFees();
  if (name === 'timetable')  loadTimetable();
  if (name === 'notices')    loadNotices();
}

async function loadChildren() {
  const res = await ParentAPI.children();
  if (!res.success || !res.data?.length) {
    document.getElementById('no-child-msg').style.display = 'flex';
    const sel = document.getElementById('child-selector');
    sel.innerHTML = '<option value="">No children linked</option>';
    return;
  }
  children = res.data;
  const sel = document.getElementById('child-selector');
  sel.innerHTML = children.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  selectedChildId = children[0].id;
  displayChildCard(children[0]);
}

function switchChild(id) {
  if (!id) return;
  selectedChildId = parseInt(id);
  const child = children.find(c => c.id === selectedChildId);
  if (child) displayChildCard(child);
  loadDashboard();
  if (currentSection !== 'dashboard') showSection(currentSection, null);
}

function displayChildCard(child) {
  document.getElementById('child-info-card').style.display = '';
  document.getElementById('child-name').textContent = child.name || '—';
  document.getElementById('child-class').textContent = (child.className || '') + (child.sectionName ? ' — Section ' + child.sectionName : '');
  document.getElementById('child-id').textContent = 'ID: ' + (child.studentId || '—');
  const initials = (child.name || '?').split(' ').map(w => w[0]).slice(0,2).join('').toUpperCase();
  document.getElementById('child-avatar').textContent = initials;
}

async function loadDashboard() {
  if (!selectedChildId) return;
  const [dashRes, noticeRes] = await Promise.all([ParentAPI.dashboard(), ParentAPI.notices()]);

  if (dashRes.success) {
    const d = dashRes.data;
    // Find this child's data
    const childDash = (d.children || []).find(c => c.childId === selectedChildId) || d;
    const attPct = parseFloat(childDash.attendancePercentage || 0);

    document.getElementById('c-att').textContent = attPct.toFixed(1) + '%';
    document.getElementById('c-avg').textContent = childDash.averageMarksPercent ? childDash.averageMarksPercent.toFixed(1) + '%' : '—';
    document.getElementById('c-fees').textContent = childDash.feesDue ? '₹' + Number(childDash.feesDue).toLocaleString('en-IN') : '₹0';
    document.getElementById('c-hw').textContent  = childDash.pendingHomework ?? '—';

    if (attPct > 0 && attPct < 75) {
      const w = document.getElementById('p-att-warning');
      w.style.display = 'flex';
      document.getElementById('p-att-warning-text').textContent =
        `⚠️ Your child's attendance is ${attPct.toFixed(1)}% — below the 75% minimum requirement.`;
    }

    const marksEl = document.getElementById('dash-marks');
    const marks = childDash.recentMarks || [];
    if (!marks.length) { marksEl.innerHTML = UI.emptyState('No marks yet', 'bi-clipboard2-x'); }
    else {
      marksEl.innerHTML = marks.slice(0,3).map(m => `
        <div class="d-flex justify-content-between align-items-center mb-2 p-2 rounded" style="background:var(--clr-surface-2);">
          <div><div style="font-weight:600;font-size:.875rem;">${m.examName||''}</div><div style="font-size:.75rem;color:var(--clr-text-muted);">${UI.fmtDate(m.examDate)}</div></div>
          <div style="font-size:1.1rem;font-weight:800;${m.passed?'color:var(--clr-success)':'color:var(--clr-danger)'};">${m.marksObtained ?? '—'}<span style="font-size:.75rem;font-weight:500;color:var(--clr-text-muted);">/${m.maxMarks}</span></div>
        </div>`).join('');
    }
  }

  if (noticeRes.success) {
    const list = noticeRes.data || [];
    const el = document.getElementById('dash-notices');
    if (!list.length) { el.innerHTML = UI.emptyState('No notices', 'bi-megaphone'); }
    else {
      el.innerHTML = list.slice(0,3).map(n => `
        <div class="notice-item">
          <div class="notice-title">${n.title}</div>
          <div class="notice-meta"><i class="bi bi-clock me-1"></i>${UI.fmtDate(n.createdAt)}</div>
          <div class="notice-body">${(n.content||'').substring(0,80)}…</div>
        </div>`).join('');
    }
  }
}

/* ---- Attendance ---- */
async function loadAttendance() {
  if (!selectedChildId) return;
  const res = await ParentAPI.attendance(selectedChildId);
  if (!res.success) return;
  const d = res.data || {};
  const pct = parseFloat(d.percentage || 0);

  document.getElementById('p-att-summary').innerHTML = `
    <div class="row align-items-center g-3">
      <div class="col-md-4 text-center">
        <div style="font-size:3rem;font-weight:900;${pct < 75 ? 'color:var(--clr-danger)' : 'color:var(--clr-success)'};">${pct.toFixed(1)}%</div>
        <div class="text-muted small">Overall Attendance</div>
        ${pct < 75 ? '<div class="badge-ss badge-danger mt-2 mx-auto d-inline-flex"><i class="bi bi-exclamation-triangle-fill me-1"></i>Below 75%</div>' : '<div class="badge-ss badge-success mt-2 mx-auto d-inline-flex"><i class="bi bi-check-circle-fill me-1"></i>Good</div>'}
      </div>
      <div class="col-md-8">
        <div class="row g-2 text-center">
          <div class="col-4"><div class="fw-bold text-success fs-5">${d.presentDays ?? 0}</div><div class="text-muted small">Present</div></div>
          <div class="col-4"><div class="fw-bold text-danger fs-5">${d.absentDays ?? 0}</div><div class="text-muted small">Absent</div></div>
          <div class="col-4"><div class="fw-bold text-warning fs-5">${d.lateDays ?? 0}</div><div class="text-muted small">Late</div></div>
        </div>
        <div class="progress-bar-track mt-3"><div class="progress-bar-fill ${pct >= 75 ? 'fill-success' : pct >= 60 ? 'fill-warning' : 'fill-danger'}" style="width:${pct}%;"></div></div>
      </div>
    </div>`;

  const records = d.records || [];
  const tbody = document.getElementById('p-att-tbody');
  if (!records.length) { tbody.innerHTML = `<tr><td colspan="3">${UI.emptyState('No records')}</td></tr>`; return; }
  tbody.innerHTML = records.slice(0,60).map(r => `
    <tr>
      <td>${UI.fmtDate(r.date)}</td>
      <td><span class="badge-ss ${r.status==='PRESENT'?'badge-success':r.status==='LATE'?'badge-warning':r.status==='EXCUSED'?'badge-info':'badge-danger'}">${r.status}</span></td>
      <td class="text-muted small">${r.remarks || '—'}</td>
    </tr>`).join('');
}

/* ---- Marks ---- */
async function loadMarks() {
  if (!selectedChildId) return;
  document.getElementById('p-marks-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await ParentAPI.marks(selectedChildId);
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('p-marks-content').innerHTML = UI.emptyState('No results yet'); return; }
  document.getElementById('p-marks-content').innerHTML = `
    <div class="row g-3">
      ${list.map(r => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="mb-0 fw-bold">${r.examName||''}</h6>
              <span class="badge-ss ${r.passed?'badge-success':'badge-danger'}">${r.grade||''}</span>
            </div>
            <div class="text-muted small mb-2"><i class="bi bi-calendar me-1"></i>${UI.fmtDate(r.examDate)}</div>
            <div style="font-size:2rem;font-weight:800;${r.passed?'color:var(--clr-success)':'color:var(--clr-danger)'};">${r.marksObtained??'—'}<span style="font-size:.85rem;font-weight:500;color:var(--clr-text-muted);">/${r.maxMarks}</span></div>
            ${r.marksObtained!=null?`<div class="progress-bar-track mt-2"><div class="progress-bar-fill ${r.passed?'fill-success':'fill-danger'}" style="width:${Math.min(r.percentage||0,100)}%;"></div></div>`:''}
          </div>
        </div>`).join('')}
    </div>`;
}

/* ---- Homework ---- */
async function loadHomework() {
  if (!selectedChildId) return;
  document.getElementById('p-hw-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await ParentAPI.homework(selectedChildId);
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('p-hw-content').innerHTML = UI.emptyState('No homework records'); return; }
  document.getElementById('p-hw-content').innerHTML = `
    <div class="row g-3">
      ${list.map(hw => `
        <div class="col-md-6">
          <div class="card-glass p-4">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="mb-0 fw-bold">${hw.title}</h6>
              <span class="badge-ss ${hw.submissionStatus==='GRADED'?'badge-success':hw.submissionStatus==='SUBMITTED'?'badge-info':'badge-warning'}">${hw.submissionStatus||'Pending'}</span>
            </div>
            <div class="text-muted small mb-1"><i class="bi bi-calendar me-1"></i>Due: ${UI.fmtDate(hw.dueDate)}</div>
            ${hw.marksObtained!=null?`<div class="text-muted small">Score: <strong>${hw.marksObtained}/${hw.maxMarks}</strong></div>`:''}
          </div>
        </div>`).join('')}
    </div>`;
}

/* ---- Fees ---- */
async function loadFees() {
  if (!selectedChildId) return;
  document.getElementById('p-fees-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await ParentAPI.fees(selectedChildId);
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('p-fees-content').innerHTML = UI.emptyState('No fee records'); return; }
  document.getElementById('p-fees-content').innerHTML = `
    <div class="card-glass p-0">
      <div class="table-responsive">
        <table class="ss-table">
          <thead><tr><th>Fee</th><th>Amount</th><th>Due Date</th><th>Status</th><th>Receipt</th></tr></thead>
          <tbody>
            ${list.map(f => `
              <tr>
                <td><strong>${f.feeName||f.feeType||''}</strong></td>
                <td>₹${Number(f.amount||0).toLocaleString('en-IN')}</td>
                <td>${UI.fmtDate(f.dueDate)}</td>
                <td>${UI.feeBadge(f.status)}</td>
                <td>${f.receiptNumber?`<span class="badge-ss badge-muted">${f.receiptNumber}</span>`:'—'}</td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
}

/* ---- Timetable ---- */
async function loadTimetable() {
  if (!selectedChildId) return;
  document.getElementById('p-timetable-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await ParentAPI.timetable(selectedChildId);
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('p-timetable-content').innerHTML = UI.emptyState('No timetable set'); return; }
  const days = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'];
  const byDay = {}; days.forEach(d => byDay[d] = []);
  list.forEach(t => { if (byDay[t.dayOfWeek]) byDay[t.dayOfWeek].push(t); });
  document.getElementById('p-timetable-content').innerHTML = `
    <div class="row g-3">
      ${days.filter(d => byDay[d].length).map(d => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <h6 class="mb-3 fw-bold text-accent">${d}</h6>
            ${byDay[d].sort((a,b) => (a.startTime||'').localeCompare(b.startTime||'')).map(t => `
              <div class="d-flex align-items-center gap-3 mb-2 p-2 rounded" style="background:var(--clr-surface-2);">
                <div style="font-size:.75rem;font-weight:700;color:var(--clr-primary-h);min-width:55px;">${t.startTime||''}</div>
                <div>
                  <div style="font-size:.875rem;font-weight:600;">${t.subjectName||''}</div>
                  <div style="font-size:.73rem;color:var(--clr-text-muted);">${t.teacherName||''}</div>
                </div>
              </div>`).join('')}
          </div>
        </div>`).join('')}
    </div>`;
}

/* ---- Notices ---- */
async function loadNotices() {
  document.getElementById('p-notices-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await ParentAPI.notices();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('p-notices-content').innerHTML = UI.emptyState('No notices'); return; }
  document.getElementById('p-notices-content').innerHTML = list.map(n => `
    <div class="notice-item">
      <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div class="notice-title">${n.title}</div>
        <span class="badge-ss badge-info">${n.audience||''}</span>
      </div>
      <div class="notice-meta"><i class="bi bi-clock me-1"></i>${UI.fmtDate(n.createdAt)}</div>
      <div class="notice-body">${n.content||''}</div>
    </div>`).join('');
}
