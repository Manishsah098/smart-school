'use strict';

let currentSubmitHwId = null;

document.addEventListener('DOMContentLoaded', async () => {
  if (!initPage('ROLE_STUDENT')) return;
  const user = Auth.getUser();
  const nameEl = document.getElementById('stu-name');
  if (nameEl) nameEl.textContent = (user?.displayName || user?.username || 'Student').split(' ')[0];
  const dateEl = document.getElementById('stu-date');
  if (dateEl) dateEl.textContent = new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' });
  await loadDashboard();
  initTabs('[data-tabs]');
});

const sections = ['dashboard','attendance','timetable','homework','marks','fees','notices'];
function showSection(name, navEl) {
  sections.forEach(s => { const el = document.getElementById('sec-' + s); if (el) el.style.display = s === name ? '' : 'none'; });
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (navEl) navEl.classList.add('active');
  const titleMap = { dashboard:'Dashboard', attendance:'Attendance', timetable:'Timetable', homework:'Homework', marks:'Marks & Results', fees:'Fee Details', notices:'Notices' };
  const tt = document.getElementById('topbar-title');
  if (tt) tt.textContent = titleMap[name] || name;
  if (name === 'attendance') loadAttendance();
  if (name === 'timetable')  loadTimetable();
  if (name === 'homework')   loadHomework();
  if (name === 'marks')      loadMarks();
  if (name === 'fees')       loadFees();
  if (name === 'notices')    loadNotices();
}

/* ---- Dashboard ---- */
async function loadDashboard() {
  const [dashRes, noticeRes] = await Promise.all([StudentAPI.dashboard(), StudentAPI.notices()]);
  if (dashRes.success) {
    const d = dashRes.data;
    const pct = parseFloat(d.attendancePercentage || 0);
    document.getElementById('d-att').textContent          = pct.toFixed(1) + '%';
    document.getElementById('d-pending-hw').textContent   = d.pendingHomework ?? '—';
    document.getElementById('d-fees-due').textContent     = d.feesDue ? '₹' + Number(d.feesDue).toLocaleString('en-IN') : '₹0';
    document.getElementById('d-avg-marks').textContent    = d.averageMarksPercent ? d.averageMarksPercent.toFixed(1) + '%' : '—';

    // Attendance warning
    if (pct > 0 && pct < 75) document.getElementById('att-warning').style.display = 'flex';

    // Pending homework
    const hwList = d.pendingHomeworkList || [];
    const hwEl = document.getElementById('dash-hw');
    if (!hwList.length) { hwEl.innerHTML = UI.emptyState('No pending homework', 'bi-check2-all'); }
    else {
      hwEl.innerHTML = hwList.slice(0,3).map(hw => `
        <div class="notice-item">
          <div class="d-flex justify-content-between align-items-start">
            <div class="notice-title">${hw.title}</div>
            <span class="badge-ss ${new Date(hw.dueDate) < new Date() ? 'badge-danger' : 'badge-warning'}">Due ${UI.fmtDate(hw.dueDate)}</span>
          </div>
          <div class="notice-meta">${hw.sectionName||''}</div>
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
  const res = await StudentAPI.attendance();
  if (!res.success) return;
  const d = res.data || {};
  const pct = parseFloat(d.percentage || 0);

  document.getElementById('att-summary-card').innerHTML = `
    <div class="row align-items-center g-3">
      <div class="col-md-4 text-center">
        <div style="font-size:3rem;font-weight:900;${pct < 75 ? 'color:var(--clr-danger)' : 'color:var(--clr-success)'};">${pct.toFixed(1)}%</div>
        <div class="text-muted small">Overall Attendance</div>
        ${pct < 75 ? '<div class="badge-ss badge-danger mt-2 mx-auto d-inline-flex"><i class="bi bi-exclamation-triangle-fill me-1"></i>Below 75%</div>' : '<div class="badge-ss badge-success mt-2 mx-auto d-inline-flex"><i class="bi bi-check-circle-fill me-1"></i>Good Standing</div>'}
      </div>
      <div class="col-md-8">
        <div class="row g-2 text-center">
          <div class="col-4"><div class="fw-bold text-success fs-5">${d.presentDays ?? 0}</div><div class="text-muted small">Present</div></div>
          <div class="col-4"><div class="fw-bold text-danger fs-5">${d.absentDays ?? 0}</div><div class="text-muted small">Absent</div></div>
          <div class="col-4"><div class="fw-bold text-warning fs-5">${d.lateDays ?? 0}</div><div class="text-muted small">Late</div></div>
        </div>
        <div class="progress-bar-track mt-3"><div class="progress-bar-fill ${pct >= 75 ? 'fill-success' : pct >= 60 ? 'fill-warning' : 'fill-danger'}" style="width:${pct}%;"></div></div>
        <div class="d-flex justify-content-between mt-1"><small class="text-muted">0%</small><small class="text-muted">75% min</small><small class="text-muted">100%</small></div>
      </div>
    </div>`;

  const records = d.records || [];
  const tbody = document.getElementById('att-tbody');
  if (!records.length) { tbody.innerHTML = `<tr><td colspan="3">${UI.emptyState('No attendance records')}</td></tr>`; return; }
  tbody.innerHTML = records.slice(0, 60).map(r => `
    <tr>
      <td>${UI.fmtDate(r.date)}</td>
      <td><span class="badge-ss ${r.status==='PRESENT'?'badge-success':r.status==='LATE'?'badge-warning':r.status==='EXCUSED'?'badge-info':'badge-danger'}">${r.status}</span></td>
      <td class="text-muted" style="font-size:.85rem;">${r.remarks || '—'}</td>
    </tr>`).join('');
}

/* ---- Timetable ---- */
async function loadTimetable() {
  document.getElementById('timetable-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await StudentAPI.timetable();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('timetable-content').innerHTML = UI.emptyState('No timetable set'); return; }
  const days = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'];
  const byDay = {}; days.forEach(d => byDay[d] = []);
  list.forEach(t => { if (byDay[t.dayOfWeek]) byDay[t.dayOfWeek].push(t); });
  document.getElementById('timetable-content').innerHTML = `
    <div class="row g-3">
      ${days.filter(d => byDay[d].length).map(d => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <h6 class="mb-3 fw-bold text-accent">${d}</h6>
            ${byDay[d].sort((a,b) => (a.startTime||'').localeCompare(b.startTime||'')).map(t => `
              <div class="d-flex align-items-center gap-3 mb-2 p-2 rounded" style="background:var(--clr-surface-2);">
                <div style="font-size:.75rem;font-weight:700;color:var(--clr-primary-h);min-width:55px;">${t.startTime||''}</div>
                <div><div style="font-size:.875rem;font-weight:600;">${t.subjectName||''}</div><div style="font-size:.73rem;color:var(--clr-text-muted);">${t.teacherName||''}</div></div>
              </div>`).join('')}
          </div>
        </div>`).join('')}
    </div>`;
}

/* ---- Homework ---- */
async function loadHomework() {
  const res = await StudentAPI.homework();
  if (!res.success) return;
  const list = res.data || [];
  const pending  = list.filter(h => h.submissionStatus !== 'GRADED' && h.submissionStatus !== 'SUBMITTED');
  const submitted = list.filter(h => h.submissionStatus === 'SUBMITTED' || h.submissionStatus === 'GRADED');

  const pendEl = document.getElementById('hw-pending');
  const subEl  = document.getElementById('hw-submitted');

  if (!pending.length) { pendEl.innerHTML = UI.emptyState('No pending homework 🎉', 'bi-check2-all'); }
  else {
    pendEl.innerHTML = `<div class="row g-3">${pending.map(hw => `
      <div class="col-md-6">
        <div class="card-glass p-4">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h6 class="mb-0 fw-bold">${hw.title}</h6>
            <span class="badge-ss ${new Date(hw.dueDate) < new Date() ? 'badge-danger' : 'badge-warning'}">Due ${UI.fmtDate(hw.dueDate)}</span>
          </div>
          <div class="text-muted small mb-3">${(hw.description||'').substring(0,100)}…</div>
          <button class="btn-primary-ss w-100 justify-content-center" onclick="openSubmitModal(${hw.id},'${hw.title.replace(/'/g,"\\'")}')">
            <i class="bi bi-send-fill"></i> Submit
          </button>
        </div>
      </div>`).join('')}</div>`;
  }

  if (!submitted.length) { subEl.innerHTML = UI.emptyState('No submitted homework', 'bi-inbox'); }
  else {
    subEl.innerHTML = `<div class="row g-3">${submitted.map(hw => `
      <div class="col-md-6">
        <div class="card-glass p-4">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <h6 class="mb-0 fw-bold">${hw.title}</h6>
            <span class="badge-ss ${hw.submissionStatus==='GRADED'?'badge-success':'badge-info'}">${hw.submissionStatus}</span>
          </div>
          ${hw.marksObtained != null ? `<div class="text-muted small">Marks: <strong>${hw.marksObtained} / ${hw.maxMarks}</strong></div>` : ''}
          ${hw.feedback ? `<div class="text-muted small mt-1">Feedback: ${hw.feedback}</div>` : ''}
        </div>
      </div>`).join('')}</div>`;
  }
  initTabs('[data-tabs]');
}

function openSubmitModal(hwId, title) {
  currentSubmitHwId = hwId;
  document.getElementById('submit-hw-title').textContent = title;
  document.getElementById('submit-hw-text').value = '';
  openModal('modal-submit-hw');
}

async function doSubmitHw() {
  const content = document.getElementById('submit-hw-text').value.trim();
  if (!content) { UI.toast('Please enter your submission.', 'warning'); return; }
  const res = await StudentAPI.submit(currentSubmitHwId, { submissionText: content });
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  closeModal('modal-submit-hw');
  UI.toast('Homework submitted successfully!', 'success');
  loadHomework();
}

/* ---- Marks ---- */
async function loadMarks() {
  document.getElementById('marks-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await StudentAPI.marks();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('marks-content').innerHTML = UI.emptyState('No exam results yet', 'bi-clipboard2-x'); return; }
  document.getElementById('marks-content').innerHTML = `
    <div class="row g-3">
      ${list.map(r => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="mb-0 fw-bold">${r.examName || ''}</h6>
              <span class="badge-ss ${r.passed ? 'badge-success' : 'badge-danger'}">${r.grade || (r.passed ? 'Pass' : 'Fail')}</span>
            </div>
            <div class="text-muted small mb-3"><i class="bi bi-calendar me-1"></i>${UI.fmtDate(r.examDate)}</div>
            <div class="d-flex align-items-center gap-3">
              <div style="font-size:2rem;font-weight:800;${r.passed ? 'color:var(--clr-success)' : 'color:var(--clr-danger)'};">${r.marksObtained ?? '—'}</div>
              <div>
                <div class="text-muted small">out of ${r.maxMarks ?? '—'}</div>
                <div class="text-muted small">Pass: ${r.passMarks ?? '—'}</div>
              </div>
              <div class="ms-auto text-muted small">${r.percentage != null ? r.percentage.toFixed(1) + '%' : ''}</div>
            </div>
            ${r.marksObtained != null ? `<div class="progress-bar-track mt-3"><div class="progress-bar-fill ${r.passed ? 'fill-success' : 'fill-danger'}" style="width:${Math.min(r.percentage||0,100)}%;"></div></div>` : ''}
          </div>
        </div>`).join('')}
    </div>`;
}

/* ---- Fees ---- */
async function loadFees() {
  document.getElementById('fees-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await StudentAPI.fees();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('fees-content').innerHTML = UI.emptyState('No fee records found', 'bi-cash-stack'); return; }
  document.getElementById('fees-content').innerHTML = `
    <div class="card-glass p-0">
      <div class="table-responsive">
        <table class="ss-table">
          <thead><tr><th>Fee Type</th><th>Amount</th><th>Due Date</th><th>Status</th><th>Paid On</th><th>Receipt</th></tr></thead>
          <tbody>
            ${list.map(f => `
              <tr>
                <td><strong>${f.feeName || f.feeType || ''}</strong></td>
                <td>₹${Number(f.amount || 0).toLocaleString('en-IN')}</td>
                <td>${UI.fmtDate(f.dueDate)}</td>
                <td>${UI.feeBadge(f.status)}</td>
                <td>${f.paidOn ? UI.fmtDate(f.paidOn) : '—'}</td>
                <td>${f.receiptNumber ? `<span class="badge-ss badge-muted">${f.receiptNumber}</span>` : '—'}</td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
}

/* ---- Notices ---- */
async function loadNotices() {
  document.getElementById('notices-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await StudentAPI.notices();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('notices-content').innerHTML = UI.emptyState('No notices'); return; }
  document.getElementById('notices-content').innerHTML = list.map(n => `
    <div class="notice-item">
      <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div class="notice-title">${n.title}</div>
        <span class="badge-ss badge-info">${n.audience||''}</span>
      </div>
      <div class="notice-meta"><i class="bi bi-clock me-1"></i>${UI.fmtDate(n.createdAt)}</div>
      <div class="notice-body">${n.content||''}</div>
    </div>`).join('');
}
