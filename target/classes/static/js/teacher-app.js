'use strict';

let teacherSections = [];
let currentExamId   = null;
let currentExamSectionId = null;
let currentHwId     = null;
const attendanceMap = {};  // studentId -> status

document.addEventListener('DOMContentLoaded', async () => {
  if (!initPage('ROLE_TEACHER')) return;

  const user = Auth.getUser();
  const nameEl = document.getElementById('teacher-name');
  if (nameEl) nameEl.textContent = (user?.displayName || user?.username || 'Teacher').split(' ')[0];
  const dateEl = document.getElementById('t-date');
  if (dateEl) dateEl.textContent = new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' });

  // Default attendance date to today
  const attDate = document.getElementById('att-date');
  if (attDate) attDate.value = new Date().toISOString().split('T')[0];

  await loadDashboard();
});

const sections = ['dashboard','attendance','homework','exams','timetable','notices'];
function showSection(name, navEl) {
  sections.forEach(s => {
    const el = document.getElementById('sec-' + s);
    if (el) el.style.display = s === name ? '' : 'none';
  });
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  if (navEl) navEl.classList.add('active');
  const titleMap = { dashboard:'Dashboard', attendance:'Mark Attendance', homework:'Homework', exams:'Exams & Marks', timetable:'My Timetable', notices:'Notices' };
  const tt = document.getElementById('topbar-title');
  if (tt) tt.textContent = titleMap[name] || name;
  if (name === 'attendance') initAttendanceSection();
  if (name === 'homework')   loadHomeworkList();
  if (name === 'exams')      loadExamsList();
  if (name === 'timetable')  loadTimetable();
  if (name === 'notices')    loadNotices();
}

/* ---- Dashboard ---- */
async function loadDashboard() {
  const res = await TeacherAPI.dashboard();
  if (!res.success) return;
  const d = res.data;
  document.getElementById('t-total-students').textContent = d.totalStudents ?? '—';
  document.getElementById('t-sections').textContent        = d.totalSections ?? '—';
  document.getElementById('t-homework').textContent        = d.totalHomework ?? '—';
  document.getElementById('t-exams').textContent           = d.totalExams ?? '—';

  // My classes
  teacherSections = d.sections || [];
  renderMyClasses(teacherSections);
  renderTodayTimetable(d.todaySchedule || []);

  // Pre-populate dropdowns
  if (teacherSections.length) {
    const opt = teacherSections.map(s => `<option value="${s.id}">${s.className || ''} — Section ${s.name}</option>`).join('');
    ['att-section','hw-section','ex-section'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.innerHTML = `<option value="">— Select section —</option>${opt}`;
    });
  }
}

function renderMyClasses(sections) {
  const el = document.getElementById('my-classes-list');
  if (!sections.length) { el.innerHTML = UI.emptyState('No classes assigned yet', 'bi-building-x'); return; }
  el.innerHTML = sections.map(s => `
    <div class="d-flex justify-content-between align-items-center p-3 mb-2 rounded" style="background:var(--clr-surface-2);">
      <div>
        <div class="fw-600">${s.className || ''} — Section ${s.name}</div>
        <div class="text-muted" style="font-size:.8rem;"><i class="bi bi-people me-1"></i>${s.studentCount || 0} students · Room ${s.roomNumber || '—'}</div>
      </div>
      <button class="btn-ghost" style="font-size:.8rem;padding:.3rem .7rem;" onclick="quickAttendance(${s.id})">
        <i class="bi bi-calendar-check"></i>
      </button>
    </div>`).join('');
}

function renderTodayTimetable(schedule) {
  const el = document.getElementById('today-timetable');
  if (!schedule.length) { el.innerHTML = UI.emptyState('No classes today', 'bi-calendar-x'); return; }
  el.innerHTML = schedule.map(t => `
    <div class="d-flex align-items-center gap-3 mb-2 p-2 rounded" style="background:var(--clr-surface-2);">
      <div style="font-size:.75rem;font-weight:700;color:var(--clr-primary-h);min-width:60px;">${t.startTime || ''}</div>
      <div>
        <div style="font-weight:600;font-size:.875rem;">${t.subjectName || ''}</div>
        <div style="font-size:.75rem;color:var(--clr-text-muted);">Section ${t.sectionName || ''}</div>
      </div>
    </div>`).join('');
}

/* ---- Attendance ---- */
function quickAttendance(sectionId) {
  showSection('attendance', document.querySelector('[data-section=attendance]'));
  setTimeout(() => {
    const sel = document.getElementById('att-section');
    if (sel) { sel.value = sectionId; loadAttendanceList(); }
  }, 100);
}

function initAttendanceSection() { /* nothing extra needed */ }

async function loadAttendanceList() {
  const sectionId = document.getElementById('att-section').value;
  const date      = document.getElementById('att-date').value;
  if (!sectionId) { document.getElementById('attendance-list').innerHTML = '<div class="empty-state"><i class="bi bi-calendar-check"></i><p>Select a section to load students.</p></div>'; return; }

  document.getElementById('attendance-list').innerHTML = '<div class="text-muted">Loading students…</div>';
  const [studRes, existRes] = await Promise.all([
    TeacherAPI.sectionStudents(sectionId),
    date ? TeacherAPI.getAttendance(sectionId, date) : Promise.resolve({ success: false })
  ]);
  if (!studRes.success) { document.getElementById('attendance-list').innerHTML = `<div class="text-danger">${studRes.message}</div>`; return; }

  const students = studRes.data || [];
  // Map existing attendance
  const existing = {};
  if (existRes.success) (existRes.data || []).forEach(a => { existing[a.studentId] = a.status; });

  document.getElementById('attendance-list').innerHTML = `
    <div class="card-glass p-0">
      <div class="d-flex justify-content-between align-items-center p-3 border-bottom" style="border-color:var(--clr-border);">
        <span class="fw-bold">${students.length} Students</span>
        <div class="d-flex gap-2">
          <button class="btn-ghost" style="font-size:.8rem;" onclick="markAll('PRESENT')"><i class="bi bi-check2-all"></i> All Present</button>
          <button class="btn-ghost" style="font-size:.8rem;" onclick="markAll('ABSENT')"><i class="bi bi-x-lg"></i> All Absent</button>
        </div>
      </div>
      <div class="table-responsive">
        <table class="ss-table" id="att-table">
          <thead><tr><th>Student</th><th>Status</th><th>Remark</th></tr></thead>
          <tbody>
            ${students.map(s => `
              <tr>
                <td><strong>${s.name}</strong><br><small class="text-muted">${s.studentId || ''}</small></td>
                <td>
                  <select class="form-input att-status" data-sid="${s.id}" style="width:140px;padding:.4rem .75rem;">
                    <option value="PRESENT" ${(existing[s.id]||'PRESENT')==='PRESENT'?'selected':''}>✅ Present</option>
                    <option value="ABSENT"  ${(existing[s.id]||'')==='ABSENT' ?'selected':''}>❌ Absent</option>
                    <option value="LATE"    ${(existing[s.id]||'')==='LATE'  ?'selected':''}>🕐 Late</option>
                    <option value="EXCUSED" ${(existing[s.id]||'')==='EXCUSED'?'selected':''}>📋 Excused</option>
                  </select>
                </td>
                <td><input type="text" class="form-input att-remark" data-sid="${s.id}" placeholder="Optional remark" style="max-width:200px;"></td>
              </tr>`).join('')}
          </tbody>
        </table>
      </div>
    </div>`;
}

function markAll(status) {
  document.querySelectorAll('.att-status').forEach(sel => sel.value = status);
}

async function submitAttendance() {
  const sectionId = document.getElementById('att-section').value;
  const date      = document.getElementById('att-date').value;
  if (!sectionId || !date) { UI.toast('Please select section and date.', 'warning'); return; }

  const statuses = document.querySelectorAll('.att-status');
  const remarks  = document.querySelectorAll('.att-remark');
  if (!statuses.length) { UI.toast('No students loaded.', 'warning'); return; }

  const items = Array.from(statuses).map((sel, i) => ({
    studentId: parseInt(sel.dataset.sid),
    status: sel.value,
    remarks: remarks[i]?.value || null,
  }));

  const res = await TeacherAPI.markAttendance({ sectionId: parseInt(sectionId), date, items });
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  UI.toast('Attendance saved successfully!', 'success');
}

/* ---- Homework ---- */
async function loadHomeworkList() {
  document.getElementById('homework-list').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await TeacherAPI.myHomework();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('homework-list').innerHTML = UI.emptyState('No homework assigned yet'); return; }
  document.getElementById('homework-list').innerHTML = `
    <div class="row g-3">
      ${list.map(hw => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="mb-0 fw-bold">${hw.title}</h6>
              <span class="badge-ss badge-info">${hw.sectionName || ''}</span>
            </div>
            <div class="text-muted small mb-2"><i class="bi bi-calendar me-1"></i>Due: ${UI.fmtDate(hw.dueDate)}</div>
            <div class="text-muted small mb-3">${(hw.description || '').substring(0,80)}…</div>
            <div class="d-flex gap-2">
              <span class="badge-ss badge-muted">Max: ${hw.maxMarks || 0} marks</span>
              <button class="btn-ghost ms-auto" style="font-size:.8rem;padding:.3rem .65rem;" onclick="viewSubmissions(${hw.id})">
                <i class="bi bi-eye"></i> Submissions
              </button>
            </div>
          </div>
        </div>`).join('')}
    </div>`;
}

async function saveHomework() {
  const data = {
    title: document.getElementById('hw-title').value.trim(),
    sectionId: document.getElementById('hw-section').value,
    dueDate: document.getElementById('hw-due').value,
    maxMarks: parseInt(document.getElementById('hw-marks').value) || 0,
    description: document.getElementById('hw-desc').value.trim(),
  };
  if (!data.title || !data.sectionId || !data.dueDate) { UI.toast('Title, section and due date are required.', 'warning'); return; }
  const res = await TeacherAPI.createHomework(data);
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  closeModal('modal-hw');
  UI.toast('Homework assigned!', 'success');
  loadHomeworkList();
}

async function viewSubmissions(hwId) {
  currentHwId = hwId;
  document.getElementById('submissions-content').innerHTML = '<div class="text-muted">Loading…</div>';
  openModal('modal-submissions');
  const res = await TeacherAPI.submissions(hwId);
  if (!res.success) { document.getElementById('submissions-content').innerHTML = `<div class="text-danger">${res.message}</div>`; return; }
  const list = res.data || [];
  if (!list.length) { document.getElementById('submissions-content').innerHTML = UI.emptyState('No submissions yet'); return; }
  document.getElementById('submissions-content').innerHTML = `
    <table class="ss-table">
      <thead><tr><th>Student</th><th>Submitted</th><th>Status</th><th>Grade</th><th>Action</th></tr></thead>
      <tbody>
        ${list.map(sub => `
          <tr>
            <td><strong>${sub.studentName || ''}</strong></td>
            <td style="font-size:.8rem;">${UI.fmtDateTime(sub.submittedAt)}</td>
            <td><span class="badge-ss ${sub.status==='GRADED'?'badge-success':'badge-warning'}">${sub.status||''}</span></td>
            <td>${sub.marksObtained ?? '—'} / ${sub.maxMarks ?? '—'}</td>
            <td>${sub.status !== 'GRADED' ? `<button class="btn-ghost" style="font-size:.8rem;padding:.25rem .5rem;" onclick="gradeSubmission(${hwId},${sub.id})"><i class="bi bi-pencil"></i> Grade</button>` : ''}</td>
          </tr>`).join('')}
      </tbody>
    </table>`;
}

async function gradeSubmission(hwId, subId) {
  const marks = prompt('Enter marks obtained:');
  if (marks === null) return;
  const feedback = prompt('Enter feedback (optional):') || '';
  const res = await TeacherAPI.gradeSubmission(hwId, subId, { marksObtained: parseFloat(marks), feedback });
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  UI.toast('Graded successfully!', 'success');
  viewSubmissions(hwId);
}

/* ---- Exams ---- */
async function loadExamsList() {
  document.getElementById('exams-list').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await TeacherAPI.myExams();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('exams-list').innerHTML = UI.emptyState('No exams created yet'); return; }
  document.getElementById('exams-list').innerHTML = `
    <div class="row g-3">
      ${list.map(ex => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <h6 class="mb-0 fw-bold">${ex.name}</h6>
              <span class="badge-ss badge-primary">${ex.sectionName || ''}</span>
            </div>
            <div class="text-muted small mb-1"><i class="bi bi-calendar me-1"></i>${UI.fmtDate(ex.examDate)}</div>
            <div class="text-muted small mb-3">Max: ${ex.maxMarks} · Pass: ${ex.passMarks}</div>
            <button class="btn-ghost w-100 justify-content-center" onclick="openEnterMarks(${ex.id}, ${ex.sectionId})">
              <i class="bi bi-pencil-square"></i> Enter Marks
            </button>
          </div>
        </div>`).join('')}
    </div>`;
}

async function saveExam() {
  const data = {
    name: document.getElementById('ex-name').value.trim(),
    sectionId: document.getElementById('ex-section').value,
    examDate: document.getElementById('ex-date').value,
    maxMarks: parseFloat(document.getElementById('ex-max').value) || 100,
    passMarks: parseFloat(document.getElementById('ex-pass').value) || 35,
    description: document.getElementById('ex-desc').value.trim() || null,
  };
  if (!data.name || !data.sectionId || !data.examDate) { UI.toast('Name, section and date are required.', 'warning'); return; }
  const res = await TeacherAPI.createExam(data);
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  closeModal('modal-exam');
  UI.toast('Exam created!', 'success');
  loadExamsList();
}

async function openEnterMarks(examId, sectionId) {
  currentExamId = examId;
  currentExamSectionId = sectionId;
  document.getElementById('marks-form-content').innerHTML = '<div class="text-muted">Loading students…</div>';
  openModal('modal-marks');
  const res = await TeacherAPI.sectionStudents(sectionId);
  if (!res.success) { document.getElementById('marks-form-content').innerHTML = `<div class="text-danger">${res.message}</div>`; return; }
  const students = res.data || [];
  document.getElementById('marks-form-content').innerHTML = `
    <table class="ss-table">
      <thead><tr><th>Student</th><th>Marks Obtained</th><th>Absent?</th></tr></thead>
      <tbody>
        ${students.map(s => `
          <tr>
            <td><strong>${s.name}</strong><br><small class="text-muted">${s.studentId||''}</small></td>
            <td><input type="number" class="form-input mark-input" data-sid="${s.id}" placeholder="0" min="0" style="width:100px;padding:.4rem;"></td>
            <td><input type="checkbox" class="absent-chk" data-sid="${s.id}" style="width:18px;height:18px;cursor:pointer;"></td>
          </tr>`).join('')}
      </tbody>
    </table>`;
}

async function submitMarks() {
  const inputs = document.querySelectorAll('.mark-input');
  const absentChks = document.querySelectorAll('.absent-chk');
  const marks = Array.from(inputs).map((inp, i) => ({
    studentId: parseInt(inp.dataset.sid),
    marksObtained: absentChks[i]?.checked ? null : (parseFloat(inp.value) || 0),
    absent: absentChks[i]?.checked || false,
  }));
  const res = await TeacherAPI.enterMarks(currentExamId, marks);
  if (!res.success) { UI.toast(res.message, 'danger'); return; }
  closeModal('modal-marks');
  UI.toast('Marks saved!', 'success');
}

/* ---- Timetable ---- */
async function loadTimetable() {
  document.getElementById('timetable-content').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await TeacherAPI.timetable();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('timetable-content').innerHTML = UI.emptyState('No timetable set up'); return; }
  const days = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'];
  const byDay = {};
  days.forEach(d => byDay[d] = []);
  list.forEach(t => { if (byDay[t.dayOfWeek]) byDay[t.dayOfWeek].push(t); });
  document.getElementById('timetable-content').innerHTML = `
    <div class="row g-3">
      ${days.filter(d => byDay[d].length).map(d => `
        <div class="col-md-6 col-lg-4">
          <div class="card-glass p-4">
            <h6 class="mb-3 fw-bold text-accent">${d}</h6>
            ${byDay[d].sort((a,b) => (a.startTime||'').localeCompare(b.startTime||'')).map(t => `
              <div class="d-flex align-items-center gap-3 mb-2">
                <div style="font-size:.75rem;font-weight:700;color:var(--clr-primary-h);min-width:55px;">${t.startTime||''}</div>
                <div>
                  <div style="font-size:.875rem;font-weight:600;">${t.subjectName||''}</div>
                  <div style="font-size:.75rem;color:var(--clr-text-muted);">Sec ${t.sectionName||''}</div>
                </div>
              </div>`).join('')}
          </div>
        </div>`).join('')}
    </div>`;
}

/* ---- Notices ---- */
async function loadNotices() {
  document.getElementById('notices-list').innerHTML = '<div class="text-muted">Loading…</div>';
  const res = await TeacherAPI.notices();
  if (!res.success) return;
  const list = res.data || [];
  if (!list.length) { document.getElementById('notices-list').innerHTML = UI.emptyState('No notices', 'bi-megaphone'); return; }
  document.getElementById('notices-list').innerHTML = list.map(n => `
    <div class="notice-item">
      <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div class="notice-title">${n.title}</div>
        <span class="badge-ss badge-info">${n.audience||''}</span>
      </div>
      <div class="notice-meta"><i class="bi bi-clock me-1"></i>${UI.fmtDate(n.createdAt)}</div>
      <div class="notice-body">${n.content||''}</div>
    </div>`).join('');
}
