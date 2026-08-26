/* ============================================================
   SmartSchool — API Client & Auth Utilities
   ============================================================ */

const API_BASE = '/api';
const TOKEN_KEY = 'ss_token';
const USER_KEY  = 'ss_user';

/* ---- Token Management ---- */
const Auth = {
  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),
  getToken: ()       => localStorage.getItem(TOKEN_KEY),
  removeToken: ()    => localStorage.removeItem(TOKEN_KEY),
  setUser: (user)    => localStorage.setItem(USER_KEY, JSON.stringify(user)),
  getUser: ()        => { try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; } },
  removeUser: ()     => localStorage.removeItem(USER_KEY),
  isLoggedIn: ()     => !!localStorage.getItem(TOKEN_KEY),

  logout: () => {
    Auth.removeToken();
    Auth.removeUser();
    window.location.href = '/login.html';
  },

  /** Redirect to login if not authenticated */
  requireAuth: (allowedRole) => {
    if (!Auth.isLoggedIn()) {
      window.location.href = '/login.html';
      return false;
    }
    const user = Auth.getUser();
    if (allowedRole && user?.role !== allowedRole) {
      // Redirect to proper dashboard
      Auth.redirectToDashboard(user?.role);
      return false;
    }
    return true;
  },

  redirectToDashboard: (role) => {
    const map = {
      ROLE_ADMIN:   '/admin/index.html',
      ROLE_TEACHER: '/teacher/index.html',
      ROLE_STUDENT: '/student/index.html',
      ROLE_PARENT:  '/parent/index.html',
    };
    window.location.href = map[role] || '/login.html';
  }
};

/* ---- Core HTTP client ---- */
const API = {
  _fetch: async (method, path, body) => {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const opts = { method, headers };
    if (body !== undefined) opts.body = JSON.stringify(body);

    try {
      const res = await fetch(`${API_BASE}${path}`, opts);

      // Unauthenticated → force logout
      if (res.status === 401) {
        Auth.logout();
        return { success: false, message: 'Session expired. Please log in again.' };
      }

      const data = await res.json();
      return data;
    } catch (err) {
      if (!navigator.onLine) {
        return { success: false, message: 'You are offline. Please check your connection.', isOffline: true };
      }
      return { success: false, message: err.message || 'Network error' };
    }
  },

  get:    (path)         => API._fetch('GET',    path),
  post:   (path, body)   => API._fetch('POST',   path, body),
  put:    (path, body)   => API._fetch('PUT',    path, body),
  delete: (path)         => API._fetch('DELETE', path),
  patch:  (path, body)   => API._fetch('PATCH',  path, body),
};

/* ---- Auth API calls ---- */
const AuthAPI = {
  login: (username, password) =>
    API.post('/auth/login', { username, password }),

  changePassword: (currentPassword, newPassword, confirmPassword) =>
    API.post('/auth/change-password', { currentPassword, newPassword, confirmPassword }),

  profile: () => API.get('/auth/me'),
};

/* ---- Admin API ---- */
const AdminAPI = {
  dashboard:        ()          => API.get('/admin/dashboard'),
  allStudents:      (params)    => API.get('/admin/students' + (params ? '?' + new URLSearchParams(params) : '')),
  createStudent:    (data)      => API.post('/admin/students', data),
  updateStudent:    (id, data)  => API.put(`/admin/students/${id}`, data),
  resetPassword:    (id)        => API.post(`/admin/students/${id}/reset-password`),
  allTeachers:      ()          => API.get('/admin/teachers'),
  createTeacher:    (data)      => API.post('/admin/teachers', data),
  resetTeacherPwd:  (id)        => API.post(`/admin/teachers/${id}/reset-password`),
  allClasses:       ()          => API.get('/admin/classes'),
  createClass:      (data)      => API.post('/admin/classes', data),
  allSections:      (classId)   => API.get(`/admin/classes/${classId}/sections`),
  createSection:    (data)      => API.post('/admin/sections', data),
  createParent:     (data)      => API.post('/admin/parents', data),
  allParents:       ()          => API.get('/admin/parents'),
  resetParentPwd:   (id)        => API.post(`/admin/parents/${id}/reset-password`),
  auditLogs:        (page)      => API.get(`/admin/audit-logs?page=${page || 0}`),
  notices:          ()          => API.get('/common/notices'),
  createNotice:     (data)      => API.post('/admin/notices', data),
  events:           ()          => API.get('/common/events'),
  createEvent:      (data)      => API.post('/admin/events', data),
  feeReport:        ()          => API.get('/admin/reports/fees'),
  attendanceReport: (params)    => API.get('/admin/reports/attendance?' + new URLSearchParams(params)),
};

/* ---- Teacher API ---- */
const TeacherAPI = {
  dashboard:      ()             => API.get('/teacher/dashboard'),
  myClasses:      ()             => API.get('/teacher/classes'),
  sectionStudents:(secId)        => API.get(`/teacher/classes/${secId}/students`),
  student:        (id)           => API.get(`/teacher/students/${id}`),
  markAttendance: (data)         => API.post('/teacher/attendance', data),
  getAttendance:  (secId, date)  => API.get(`/teacher/attendance/${secId}?date=${date}`),
  createHomework: (data)         => API.post('/teacher/homework', data),
  myHomework:     ()             => API.get('/teacher/homework'),
  submissions:    (hwId)         => API.get(`/teacher/homework/${hwId}/submissions`),
  gradeSubmission:(hwId,subId,d) => API.post(`/teacher/homework/${hwId}/submissions/${subId}/grade`, d),
  createExam:     (data)         => API.post('/teacher/exams', data),
  myExams:        ()             => API.get('/teacher/exams'),
  enterMarks:     (examId, data) => API.post(`/teacher/exams/${examId}/marks`, data),
  timetable:      ()             => API.get('/teacher/timetable'),
  notices:        ()             => API.get('/common/notices'),
};

/* ---- Student API ---- */
const StudentAPI = {
  dashboard:   ()     => API.get('/student/dashboard'),
  timetable:   ()     => API.get('/student/timetable'),
  attendance:  ()     => API.get('/student/attendance'),
  homework:    ()     => API.get('/student/homework'),
  submit:      (hwId, data) => API.post(`/student/homework/${hwId}/submit`, data),
  marks:       ()     => API.get('/student/marks'),
  fees:        ()     => API.get('/student/fees'),
  notices:     ()     => API.get('/common/notices'),
  notifications: ()   => API.get('/common/notifications'),
  markRead:    (id)   => API.post(`/common/notifications/${id}/read`),
};

/* ---- Parent API ---- */
const ParentAPI = {
  dashboard:  ()        => API.get('/parent/dashboard'),
  children:   ()        => API.get('/parent/children'),
  attendance: (childId) => API.get(`/parent/children/${childId}/attendance`),
  marks:      (childId) => API.get(`/parent/children/${childId}/marks`),
  fees:       (childId) => API.get(`/parent/children/${childId}/fees`),
  homework:   (childId) => API.get(`/parent/children/${childId}/homework`),
  timetable:  (childId) => API.get(`/parent/children/${childId}/timetable`),
  notices:    ()        => API.get('/common/notices'),
};

/* ============================================================
   UI Helper Utilities
   ============================================================ */
const UI = {
  /** Render a toast notification */
  toast: (message, type = 'info', durationMs = 4000) => {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    const el = document.createElement('div');
    const icons = { success: 'bi-check-circle-fill', danger: 'bi-x-circle-fill', warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };
    el.className = `toast-ss ${type}`;
    el.innerHTML = `<i class="bi ${icons[type] || icons.info}"></i><span>${message}</span>`;
    container.appendChild(el);
    setTimeout(() => {
      el.style.animation = 'slideInRight .3s ease reverse';
      setTimeout(() => el.remove(), 300);
    }, durationMs);
  },

  /** Fill a select element from an array of {value, label} */
  fillSelect: (selectId, items, placeholder = 'Select...') => {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = `<option value="">${placeholder}</option>`;
    items.forEach(({ value, label }) => {
      sel.innerHTML += `<option value="${value}">${label}</option>`;
    });
  },

  /** Show or hide an element by ID */
  show: (id) => { const el = document.getElementById(id); if (el) el.style.display = ''; },
  hide: (id) => { const el = document.getElementById(id); if (el) el.style.display = 'none'; },

  /** Set loading state on a button */
  btnLoading: (btn, loading) => {
    if (loading) {
      btn._origText = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status"></span> Loading…`;
    } else {
      btn.disabled = false;
      if (btn._origText) btn.innerHTML = btn._origText;
    }
  },

  /** Format date string for display */
  fmtDate: (d) => d ? new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—',
  fmtDateTime: (d) => d ? new Date(d).toLocaleString('en-IN') : '—',

  /** Render attendance badge */
  attendanceBadge: (pct) => {
    const p = parseFloat(pct) || 0;
    const cls = p >= 75 ? 'badge-success' : p >= 60 ? 'badge-warning' : 'badge-danger';
    return `<span class="badge-ss ${cls}">${p.toFixed(1)}%</span>`;
  },

  /** Status badge for fee */
  feeBadge: (status) => {
    const map = { PAID: 'badge-success', PENDING: 'badge-warning', OVERDUE: 'badge-danger', PARTIAL: 'badge-info' };
    return `<span class="badge-ss ${map[status] || 'badge-muted'}">${status}</span>`;
  },

  /** Render initials avatar */
  avatar: (name, size = 36) => {
    const initials = (name || '?').split(' ').map(w => w[0]).slice(0,2).join('').toUpperCase();
    return `<div class="avatar" style="width:${size}px;height:${size}px;font-size:${size*.35}px;">${initials}</div>`;
  },

  /** Empty state HTML */
  emptyState: (msg = 'No records found', icon = 'bi-inbox') =>
    `<div class="empty-state"><i class="bi ${icon}"></i><p>${msg}</p></div>`,

  /** Confirm dialog using native confirm (can be swapped for custom modal) */
  confirm: (msg) => window.confirm(msg),
};

/* ---- Open/close modal helpers ---- */
function openModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add('open');
}
function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove('open');
}
// Close modal on overlay click
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});

/* ---- Tab switching ---- */
function initTabs(containerSelector) {
  const containers = document.querySelectorAll(containerSelector || '[data-tabs]');
  containers.forEach(container => {
    const btns = container.querySelectorAll('.tab-btn');
    const panels = container.querySelectorAll('.tab-panel');
    btns.forEach(btn => {
      btn.addEventListener('click', () => {
        btns.forEach(b => b.classList.remove('active'));
        panels.forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        const target = document.getElementById(btn.dataset.tab);
        if (target) target.classList.add('active');
      });
    });
    // activate first
    if (btns.length) btns[0].click();
  });
}

/* ---- Sidebar mobile toggle ---- */
function initSidebar() {
  const hamburger = document.getElementById('hamburger');
  const sidebar   = document.getElementById('sidebar');
  const overlay   = document.getElementById('sidebar-overlay');

  if (!hamburger || !sidebar) return;

  hamburger.addEventListener('click', () => {
    sidebar.classList.toggle('open');
    if (overlay) overlay.classList.toggle('open');
  });
  if (overlay) {
    overlay.addEventListener('click', () => {
      sidebar.classList.remove('open');
      overlay.classList.remove('open');
    });
  }
}

/* ---- Logout buttons ---- */
function initLogout() {
  document.querySelectorAll('[data-logout]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (UI.confirm('Are you sure you want to log out?')) Auth.logout();
    });
  });
}

/* ---- Populate user pill in sidebar ---- */
function populateUserPill() {
  const user = Auth.getUser();
  if (!user) return;
  const nameEl = document.getElementById('user-name');
  const roleEl = document.getElementById('user-role');
  const avatarEl = document.getElementById('user-avatar');
  const name = user.displayName || user.username || '';
  const roleMap = { ROLE_ADMIN: 'Administrator', ROLE_TEACHER: 'Teacher', ROLE_STUDENT: 'Student', ROLE_PARENT: 'Parent' };
  if (nameEl) nameEl.textContent = name;
  if (roleEl) roleEl.textContent = roleMap[user.role] || user.role;
  if (avatarEl) {
    const initials = name.split(' ').map(w => w[0]).slice(0,2).join('').toUpperCase() || '?';
    avatarEl.textContent = initials;
  }
}

/* ---- Mark nav-item active by page ---- */
function setActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.remove('active');
    if (item.dataset.page && path.endsWith(item.dataset.page)) item.classList.add('active');
  });
}

/* ---- Page init helper (call at bottom of each dashboard page) ---- */
function initPage(role) {
  if (!Auth.requireAuth(role)) return false;
  initSidebar();
  initLogout();
  populateUserPill();
  setActiveNav();
  // Hide page loader
  const loader = document.getElementById('page-loader');
  if (loader) loader.classList.add('hidden');
  return true;
}
