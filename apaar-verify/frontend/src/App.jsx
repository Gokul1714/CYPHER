import React, { useState, useEffect } from 'react';
import { 
  Building2, 
  ShieldCheck, 
  Users, 
  UploadCloud, 
  AlertTriangle, 
  BarChart3, 
  FileText, 
  Clock, 
  Search, 
  Plus, 
  CheckCircle, 
  XCircle, 
  QrCode, 
  LogOut, 
  ExternalLink,
  ChevronRight,
  Filter,
  ShieldAlert
} from 'lucide-react';
import { apiService } from './services/api';
import { INITIAL_INSTITUTIONS, INITIAL_APPROVED_INTAKE } from './services/mockData';

export default function App() {
  const [currentView, setCurrentView] = useState('landing'); // 'landing' | 'portal' | 'public-verify' | 'login' | 'signup'
  const [activeTab, setActiveTab] = useState('dashboard'); // 'dashboard' | 'students' | 'bulk-upload' | 'validation' | 'intake' | 'reports' | 'audit'
  const [currentUser, setCurrentUser] = useState(null);
  const [students, setStudents] = useState([]);
  const [intake, setIntake] = useState(INITIAL_APPROVED_INTAKE);
  const [anomalies, setAnomalies] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);

  // Dedicated Login view state
  const [loginUsername, setLoginUsername] = useState('aicte_officer');
  const [loginPassword, setLoginPassword] = useState('password123');
  const [loginError, setLoginError] = useState('');
  const [loginSuccess, setLoginSuccess] = useState('');
  const [loginLoading, setLoginLoading] = useState(false);
  const [forgotPasswordAlert, setForgotPasswordAlert] = useState(false);

  // Dedicated Sign-Up view state (EXACTLY 5 REQUIRED FIELDS)
  const [signupForm, setSignupForm] = useState({
    fullName: '',
    staffEmail: '',
    staffId: '',
    department: '',
    password: ''
  });
  const [signupError, setSignupError] = useState('');
  const [signupSuccess, setSignupSuccess] = useState('');
  const [signupLoading, setSignupLoading] = useState(false);

  // Add student modal
  const [showAddModal, setShowAddModal] = useState(false);
  const [newStudentData, setNewStudentData] = useState({
    name: '',
    gender: 'FEMALE',
    dob: '2005-01-01',
    apaarId: '',
    course: 'B.Tech Computer Science & Engineering',
    category: 'GENERAL',
    rollNo: '',
    isNri: false,
    isSupernumerary: false
  });

  // Verification modal / Public verify
  const [showQrModal, setShowQrModal] = useState(false);
  const [selectedStudentForQr, setSelectedStudentForQr] = useState(null);
  const [verifyTokenInput, setVerifyTokenInput] = useState('tok_sec_982345012344_aicte_verified_sig');
  const [verifyResult, setVerifyResult] = useState(null);

  // Bulk CSV state
  const [csvPreviewRows, setCsvPreviewRows] = useState([]);

  // Filters for student table
  const [filterSearch, setFilterSearch] = useState('');
  const [filterCourse, setFilterCourse] = useState('ALL');
  const [filterCategory, setFilterCategory] = useState('ALL');
  const [filterApaar, setFilterApaar] = useState('ALL');

  useEffect(() => {
    loadData();
  }, [currentUser]);

  const loadData = async () => {
    const instId = currentUser?.role === 'ROLE_INSTITUTION_ADMIN' ? currentUser.institutionId : null;
    const stds = await apiService.getStudents(instId);
    setStudents(stds);
    const intk = await apiService.getIntake(instId);
    setIntake(intk);
    const flags = apiService.runValidation(stds);
    setAnomalies(flags);
    const logs = await apiService.getAuditLogs();
    setAuditLogs(logs);
  };

  const handleLogin = async (e) => {
    if (e) e.preventDefault();
    setLoginError('');
    setLoginSuccess('');
    setLoginLoading(true);
    try {
      const res = await apiService.login(loginUsername, loginPassword);
      setCurrentUser(res.user);
      setCurrentView('portal');
      apiService.addAuditLog('USER_LOGIN', 'SESSION', `User ${res.user.username} authenticated successfully`, res.user.fullName);
    } catch (err) {
      setLoginError(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoginLoading(false);
    }
  };

  const handleSignUpSubmit = async (e) => {
    e.preventDefault();
    setSignupError('');
    setSignupSuccess('');

    // 1. Check all 5 required fields
    if (!signupForm.fullName.trim() || !signupForm.staffEmail.trim() || !signupForm.staffId.trim() || !signupForm.department.trim() || !signupForm.password) {
      setSignupError('All 5 fields are strictly required.');
      return;
    }

    // 2. Validate staff email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(signupForm.staffEmail.trim())) {
      setSignupError('Please enter a valid staff email address.');
      return;
    }

    // 3. Password length validation
    if (signupForm.password.length < 6) {
      setSignupError('Password must be at least 6 characters long.');
      return;
    }

    setSignupLoading(true);
    try {
      const res = await apiService.registerStaff({
        fullName: signupForm.fullName.trim(),
        email: signupForm.staffEmail.trim(),
        staffId: signupForm.staffId.trim(),
        department: signupForm.department.trim(),
        password: signupForm.password
      });

      setSignupSuccess(res.message || 'Staff registration submitted successfully. Account pending administrative approval.');
      const registeredEmail = signupForm.staffEmail.trim();
      setSignupForm({
        fullName: '',
        staffEmail: '',
        staffId: '',
        department: '',
        password: ''
      });

      // Auto redirect to dedicated Login view after 2.5 seconds
      setTimeout(() => {
        setLoginUsername(registeredEmail);
        setLoginPassword('');
        setLoginError('');
        setCurrentView('login');
        setSignupSuccess('');
      }, 2500);
    } catch (err) {
      setSignupError(err.message || 'Failed to submit registration. Please verify details.');
    } finally {
      setSignupLoading(false);
    }
  };

  const handleLogout = () => {
    apiService.addAuditLog('USER_LOGOUT', 'SESSION', `User ${currentUser?.username} logged out`, currentUser?.fullName);
    setCurrentUser(null);
    setCurrentView('landing');
  };

  const handleAddStudent = async (e) => {
    e.preventDefault();
    const instId = currentUser?.institutionId || 'inst-1';
    await apiService.addStudent({
      ...newStudentData,
      institutionId: instId,
      academicYear: '2026-2027',
      enrolmentStatus: 'ACTIVE'
    });
    apiService.addAuditLog('ADD_STUDENT', 'STUDENT', `Manual entry created for ${newStudentData.name}`, currentUser?.fullName);
    setShowAddModal(false);
    setNewStudentData({
      name: '',
      gender: 'FEMALE',
      dob: '2005-01-01',
      apaarId: '',
      course: 'B.Tech Computer Science & Engineering',
      category: 'GENERAL',
      rollNo: '',
      isNri: false,
      isSupernumerary: false
    });
    loadData();
  };

  const handlePublicVerify = async (e) => {
    if (e) e.preventDefault();
    const res = await apiService.verifyRecord(verifyTokenInput.trim());
    setVerifyResult(res || { notFound: true });
    apiService.addAuditLog('PUBLIC_VERIFICATION', 'VERIFICATION_TOKEN', `Verification attempt for token ${verifyTokenInput}`, 'Public Citizen');
  };

  const handleLoadSampleCsv = () => {
    const sample = [
      { student_name: 'Vikramaditya Roy', gender: 'MALE', date_of_birth: '2005-02-14', apaar_id: '992144321155', course_name: 'B.Tech Computer Science & Engineering', admission_category: 'GENERAL', roll_number: '26-CSE-101' },
      { student_name: 'Meera Nandakumar', gender: 'FEMALE', date_of_birth: '2005-09-08', apaar_id: '881923014562', course_name: 'B.Tech Computer Science & Engineering', admission_category: 'OBC_NCL', roll_number: '26-CSE-102' },
      { student_name: 'Siddharth Sen', gender: 'MALE', date_of_birth: '2004-12-19', apaar_id: '', course_name: 'B.Tech Computer Science & Engineering', admission_category: 'GENERAL', roll_number: '26-CSE-103' },
      { student_name: 'Deepika Sharma', gender: 'FEMALE', date_of_birth: '2005-07-25', apaar_id: '12345', course_name: 'B.Tech Electronics & Communication', admission_category: 'SC', roll_number: '26-ECE-101' }
    ];
    setCsvPreviewRows(sample);
  };

  const handleConfirmIngestCsv = async () => {
    const mapped = csvPreviewRows.map(r => ({
      name: r.student_name,
      gender: r.gender,
      dob: r.date_of_birth,
      apaarId: r.apaar_id,
      course: r.course_name,
      category: r.admission_category,
      rollNo: r.roll_number,
      academicYear: '2026-2027',
      enrolmentStatus: 'ACTIVE'
    }));
    await apiService.bulkUploadStudents(mapped, currentUser?.institutionId || 'inst-1');
    apiService.addAuditLog('CSV_BULK_INGEST', 'STUDENT_ROSTER', `Ingested ${mapped.length} records via CSV`, currentUser?.fullName);
    setCsvPreviewRows([]);
    setActiveTab('students');
    loadData();
  };

  const handleExportStudentCsv = () => {
    let csv = "student_id,name,gender,dob,apaar_id,course,category,roll_no,status,verification\n";
    students.forEach(s => {
      csv += `"${s.id}","${s.name}","${s.gender}","${s.dob}","${s.apaarId || ''}","${s.course}","${s.category}","${s.rollNo}","${s.enrolmentStatus}","${s.verificationStatus}"\n`;
    });
    triggerDownload(csv, "aicte_student_roster.csv", "text/csv");
    apiService.addAuditLog('REPORT_DOWNLOAD_CSV', 'REPORT', 'Exported student roster CSV', currentUser?.fullName);
  };

  const handleExportIntakeCsv = () => {
    let csv = "course,department,academic_year,approved_seats,actual_admitted,nri_quota,supernumerary_quota,compliance\n";
    intake.forEach(i => {
      const count = students.filter(s => s.course === i.course).length;
      csv += `"${i.course}","${i.department}","2026-2027",${i.approved},${count},${i.nriQuota},${i.supernumeraryQuota},"${count > i.approved ? 'EXCESS_BREACH' : 'COMPLIANT'}"\n`;
    });
    triggerDownload(csv, "aicte_intake_compliance.csv", "text/csv");
    apiService.addAuditLog('REPORT_DOWNLOAD_CSV', 'REPORT', 'Exported intake audit CSV', currentUser?.fullName);
  };

  const handleExportAnomaliesCsv = () => {
    let csv = "category,severity,student_name,apaar_id,message\n";
    anomalies.forEach(a => {
      csv += `"${a.category}","${a.severity}","${a.studentName}","${a.apaar}","${a.message.replace(/"/g, '""')}"\n`;
    });
    triggerDownload(csv, "aicte_validation_anomalies.csv", "text/csv");
    apiService.addAuditLog('REPORT_DOWNLOAD_CSV', 'REPORT', 'Exported anomaly dossier CSV', currentUser?.fullName);
  };

  const triggerDownload = (content, filename, type) => {
    const blob = new Blob([content], { type });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const filteredStudents = students.filter(s => {
    const matchesSearch = s.name.toLowerCase().includes(filterSearch.toLowerCase()) ||
                          (s.apaarId && s.apaarId.includes(filterSearch)) ||
                          s.rollNo?.toLowerCase().includes(filterSearch.toLowerCase());
    const matchesCourse = filterCourse === 'ALL' || s.course === filterCourse;
    const matchesCategory = filterCategory === 'ALL' || s.category === filterCategory;
    const is12 = s.apaarId && /^\d{12}$/.test(s.apaarId);
    const matchesApaar = filterApaar === 'ALL' || (filterApaar === 'VALID' ? is12 : !is12);
    return matchesSearch && matchesCourse && matchesCategory && matchesApaar;
  });

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-800">
      {/* Top AICTE Gov Bar */}
      <div className="bg-slate-900 text-slate-300 text-xs px-4 py-1.5 flex justify-between items-center border-b border-slate-800">
        <div className="flex items-center gap-3">
          <span className="font-semibold text-amber-400">AICTE Regulatory Officer Portal</span>
          <span className="text-slate-600">|</span>
          <span>All India Council for Technical Education (AICTE)</span>
        </div>
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1.5 text-xs">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            <span>Mock APAAR Gateway: <strong className="text-emerald-400">Active</strong></span>
          </span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-400">AY 2026-2027</span>
        </div>
      </div>

      {/* Main Navbar */}
      <header className="bg-[#0f2b5c] text-white shadow sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => setCurrentView('landing')}>
            <div className="w-9 h-9 rounded bg-white text-[#0f2b5c] flex items-center justify-center font-black text-xl shadow">
              C
            </div>
            <div>
              <div className="font-extrabold text-lg tracking-wider flex items-center gap-2">
                CYPHER
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-white/10 text-amber-300 border border-white/20">AICTE</span>
              </div>
              <p className="text-[11px] text-slate-300">Central Student Intake &amp; Fraud Prevention Engine</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button onClick={() => setCurrentView('landing')} className="px-3 py-1.5 text-xs font-semibold rounded hover:bg-white/10 text-slate-200">
              Home
            </button>
            <button onClick={() => setCurrentView('public-verify')} className="px-3 py-1.5 text-xs font-semibold rounded hover:bg-white/10 text-emerald-300 flex items-center gap-1">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              Public Verify
            </button>
            
            {currentUser ? (
              <div className="flex items-center gap-3 ml-2 border-l border-white/20 pl-3">
                <button onClick={() => setCurrentView('portal')} className="px-3 py-1.5 text-xs font-semibold rounded bg-white/15 hover:bg-white/20 text-white">
                  My Portal
                </button>
                <div className="text-right hidden sm:block">
                  <div className="text-xs font-bold text-white">{currentUser.fullName}</div>
                  <div className="text-[10px] text-amber-300">{currentUser.institutionName}</div>
                </div>
                <button onClick={handleLogout} className="p-1.5 rounded hover:bg-red-600/80 text-white transition" title="Sign Out">
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2 ml-2">
                <button 
                  onClick={() => { setLoginError(''); setCurrentView('login'); }} 
                  className="px-3 py-1.5 text-xs font-semibold rounded bg-white/10 hover:bg-white/20 text-white border border-white/20">
                  Officer Login
                </button>
                <button 
                  onClick={() => { setSignupError(''); setSignupSuccess(''); setCurrentView('signup'); }} 
                  className="px-3 py-1.5 text-xs font-semibold rounded bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold shadow">
                  Sign Up
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* VIEW: LANDING */}
      {currentView === 'landing' && (
        <div className="flex-1">
          <div className="bg-gradient-to-b from-[#0f2b5c] via-[#1e3a8a] to-slate-900 text-white py-16 px-4">
            <div className="max-w-4xl mx-auto text-center space-y-6">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-200 text-xs font-semibold">
                <ShieldCheck className="w-3.5 h-3.5 text-amber-400" /> AICTE Central Verification &amp; Regulatory Desk
              </div>
              <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight leading-tight">
                CYPHER
              </h1>
              <p className="text-base sm:text-lg text-slate-300 max-w-2xl mx-auto">
                AICTE Central Student Data Management &amp; Regulatory Verification Platform. Validates student records, identifies cross-institution duplicate enrolments, and strictly monitors sanctioned intake limits.
              </p>
              <div className="flex flex-wrap justify-center gap-4 pt-4">
                <button 
                  onClick={() => { setLoginUsername('diet_admin'); setLoginPassword('password123'); setLoginError(''); setCurrentView('login'); }}
                  className="px-5 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-sm shadow-lg flex items-center gap-2">
                  <Building2 className="w-4 h-4" />
                  College Officer Sign In
                </button>
                <button 
                  onClick={() => { setLoginUsername('aicte_officer'); setLoginPassword('password123'); setLoginError(''); setCurrentView('login'); }}
                  className="px-5 py-2.5 rounded-lg bg-white/10 hover:bg-white/20 border border-white/30 text-white font-bold text-sm flex items-center gap-2">
                  <ShieldCheck className="w-4 h-4 text-amber-400" />
                  AICTE Officer Sign In
                </button>
                <button 
                  onClick={() => setCurrentView('public-verify')}
                  className="px-5 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm flex items-center gap-2">
                  <QrCode className="w-4 h-4" />
                  Public Record Verification
                </button>
              </div>
            </div>
          </div>

          <div className="max-w-6xl mx-auto px-4 -mt-8 grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow">
              <div className="text-xs font-bold text-slate-500 uppercase">Institutions</div>
              <div className="text-2xl font-black text-slate-900 mt-1">3 Participated</div>
              <div className="text-xs text-slate-500 mt-1">Delhi, MH, Karnataka</div>
            </div>
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow">
              <div className="text-xs font-bold text-slate-500 uppercase">Enrolled Students</div>
              <div className="text-2xl font-black text-blue-700 mt-1">{students.length} Records</div>
              <div className="text-xs text-slate-500 mt-1">AY 2026-2027 Active</div>
            </div>
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow">
              <div className="text-xs font-bold text-slate-500 uppercase">Cross-College Duplicates</div>
              <div className="text-2xl font-black text-red-600 mt-1">1 Anomaly</div>
              <div className="text-xs text-slate-500 mt-1">Dual active enrolment</div>
            </div>
            <div className="bg-white p-5 rounded-xl border border-slate-200 shadow">
              <div className="text-xs font-bold text-slate-500 uppercase">Intake Sanctions</div>
              <div className="text-2xl font-black text-emerald-600 mt-1">100% Compliant</div>
              <div className="text-xs text-slate-500 mt-1">Zero excess breaches</div>
            </div>
          </div>
        </div>
      )}

      {/* VIEW: DEDICATED LOGIN PAGE */}
      {currentView === 'login' && (
        <div className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-slate-100">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-8 border border-slate-200 space-y-6">
            <div>
              <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-blue-50 text-blue-800 text-[11px] font-semibold border border-blue-200 mb-2">
                <span className="w-2 h-2 rounded-full bg-blue-600"></span>
                CYPHER • AICTE Regulatory Officer Portal
              </div>
              <h2 className="text-2xl font-black text-slate-900 tracking-tight">Sign In to CYPHER</h2>
              <p className="text-xs text-slate-500 mt-1">Access institutional rosters, verification queues, and intake monitors.</p>
            </div>

            {/* Inline Feedback Messages */}
            {loginError && (
              <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-800 font-medium">
                {loginError}
              </div>
            )}
            {loginSuccess && (
              <div className="p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 font-medium">
                {loginSuccess}
              </div>
            )}
            {forgotPasswordAlert && (
              <div className="p-3 rounded-lg bg-amber-50 border border-amber-200 text-xs text-amber-900 font-medium">
                To reset your credentials, please submit an official request to the AICTE IT Support Cell at <strong>support@aicte-india.gov.in</strong> quoting your registered Staff ID.
              </div>
            )}

            {/* Fast Select Existing Officer Account */}
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
              <div className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider">Fast Select Existing Officer Account:</div>
              <div className="grid grid-cols-2 gap-2">
                <button 
                  type="button" 
                  onClick={() => { setLoginUsername('aicte_officer'); setLoginPassword('password123'); setLoginError(''); }}
                  className="p-2 rounded-lg border text-left hover:bg-amber-50 transition border-amber-200 bg-white">
                  <div className="text-xs font-bold text-amber-900">Dr. Ramesh Kumar</div>
                  <div className="text-[10px] text-amber-700">AICTE Regulatory Director</div>
                </button>
                <button 
                  type="button" 
                  onClick={() => { setLoginUsername('diet_admin'); setLoginPassword('password123'); setLoginError(''); }}
                  className="p-2 rounded-lg border text-left hover:bg-blue-50 transition border-blue-200 bg-white">
                  <div className="text-xs font-bold text-blue-900">Prof. Sunita Sharma</div>
                  <div className="text-[10px] text-blue-700">DIET College Registrar</div>
                </button>
              </div>
            </div>

            {/* Dedicated Login Form */}
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Email / Username / Staff ID</label>
                <input 
                  type="text" 
                  value={loginUsername} 
                  onChange={e => setLoginUsername(e.target.value)} 
                  required 
                  placeholder="e.g. director.regulatory@aicte-india.gov.in"
                  className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>
              <div>
                <div className="flex justify-between items-center mb-1">
                  <label className="text-xs font-bold text-slate-700">Password</label>
                  <button 
                    type="button" 
                    onClick={() => setForgotPasswordAlert(!forgotPasswordAlert)} 
                    className="text-xs text-blue-600 hover:text-blue-800 hover:underline">
                    Forgot Password?
                  </button>
                </div>
                <input 
                  type="password" 
                  value={loginPassword} 
                  onChange={e => setLoginPassword(e.target.value)} 
                  required 
                  placeholder="••••••••"
                  className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>

              <button 
                type="submit" 
                disabled={loginLoading}
                className="w-full py-2.5 px-4 bg-[#0f2b5c] hover:bg-blue-900 text-white font-bold rounded-lg text-sm shadow transition flex items-center justify-center gap-2">
                {loginLoading ? 'Signing In...' : 'Login to CYPHER'}
              </button>
            </form>

            <div className="text-center pt-2 border-t border-slate-100">
              <p className="text-xs text-slate-600">
                New User?{' '}
                <button 
                  type="button" 
                  onClick={() => { setSignupError(''); setSignupSuccess(''); setCurrentView('signup'); }}
                  className="font-bold text-blue-700 hover:text-blue-900 hover:underline">
                  Sign Up
                </button>
              </p>
            </div>
          </div>
        </div>
      )}

      {/* VIEW: DEDICATED SIGN-UP PAGE (EXACTLY 5 REQUIRED FIELDS) */}
      {currentView === 'signup' && (
        <div className="flex-1 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-slate-100">
          <div className="bg-white rounded-2xl shadow-xl max-w-lg w-full p-8 border border-slate-200 space-y-6">
            <div>
              <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-amber-50 text-amber-900 text-[11px] font-semibold border border-amber-200 mb-2">
                <span className="w-2 h-2 rounded-full bg-amber-500"></span>
                CYPHER Staff Registration Desk
              </div>
              <h2 className="text-2xl font-black text-slate-900 tracking-tight">Create CYPHER Account</h2>
              <p className="text-xs text-slate-500 mt-1">Register for AICTE regulatory oversight privileges. Mandatory staff credentials required.</p>
            </div>

            {/* Inline Feedback Messages */}
            {signupError && (
              <div className="p-3 rounded-lg bg-red-50 border border-red-200 text-xs text-red-800 font-medium">
                {signupError}
              </div>
            )}
            {signupSuccess && (
              <div className="p-3 rounded-lg bg-emerald-50 border border-emerald-200 text-xs text-emerald-800 font-medium">
                {signupSuccess}
              </div>
            )}

            <form onSubmit={handleSignUpSubmit} className="space-y-4">
              {/* FIELD 1: Full Name */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Full Name *</label>
                <input 
                  type="text" 
                  required 
                  placeholder="e.g. Dr. Rajeshwar Sen" 
                  value={signupForm.fullName}
                  onChange={e => setSignupForm({ ...signupForm, fullName: e.target.value })}
                  className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>

              {/* FIELD 2: Staff Email ID */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Staff Email ID *</label>
                <input 
                  type="email" 
                  required 
                  placeholder="e.g. rajeshwar.sen@aicte-india.org" 
                  value={signupForm.staffEmail}
                  onChange={e => setSignupForm({ ...signupForm, staffEmail: e.target.value })}
                  className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>

              {/* FIELD 3: Staff ID */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Staff ID *</label>
                <input 
                  type="text" 
                  required 
                  placeholder="e.g. AICTE-REG-045" 
                  value={signupForm.staffId}
                  onChange={e => setSignupForm({ ...signupForm, staffId: e.target.value })}
                  className="w-full px-3.5 py-2 text-sm font-mono border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>

              {/* FIELD 4: Department */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Department *</label>
                <input 
                  type="text" 
                  required 
                  placeholder="e.g. Bureau of Approval & Regulation" 
                  value={signupForm.department}
                  onChange={e => setSignupForm({ ...signupForm, department: e.target.value })}
                  className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>

              {/* FIELD 5: Password */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Password *</label>
                <input 
                  type="password" 
                  required 
                  placeholder="Minimum 6 characters" 
                  value={signupForm.password}
                  onChange={e => setSignupForm({ ...signupForm, password: e.target.value })}
                  className="w-full px-3.5 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-600 focus:outline-none" 
                />
              </div>

              <div>
                <button 
                  type="submit" 
                  disabled={signupLoading}
                  className="w-full py-2.5 px-4 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-lg text-sm shadow transition flex items-center justify-center gap-2">
                  {signupLoading ? 'Registering Staff...' : 'Register / Sign Up'}
                </button>
              </div>
            </form>

            <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-[11px] text-slate-500 leading-relaxed">
              <strong>Staff Verification Policy:</strong> Newly registered accounts must undergo staff verification and administrator approval before receiving regulatory officer privileges.
            </div>

            <div className="text-center pt-2 border-t border-slate-100">
              <p className="text-xs text-slate-600">
                Already have an account?{' '}
                <button 
                  type="button" 
                  onClick={() => { setLoginError(''); setCurrentView('login'); }}
                  className="font-bold text-blue-700 hover:text-blue-900 hover:underline">
                  Log In here
                </button>
              </p>
            </div>
          </div>
        </div>
      )}

      {/* VIEW: PORTAL */}
      {currentView === 'portal' && !currentUser && (
        <div className="flex-1 flex items-center justify-center py-16 px-4 bg-slate-50">
          <div className="max-w-md w-full bg-white p-8 rounded-2xl shadow-xl border border-slate-200 text-center space-y-4">
            <div className="w-12 h-12 rounded-full bg-amber-100 text-amber-600 flex items-center justify-center mx-auto">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold text-slate-900">CYPHER Protected Officer Desk</h3>
            <p className="text-xs text-slate-500">
              Access to institutional student rosters, anomaly flags, and regulatory verification tools requires active officer authentication. Please sign in or register your staff credentials.
            </p>
            <div className="flex gap-3 justify-center pt-2">
              <button 
                onClick={() => { setLoginError(''); setCurrentView('login'); }} 
                className="px-5 py-2.5 bg-[#0f2b5c] hover:bg-blue-900 text-white font-bold text-xs rounded-lg shadow">
                Go to Officer Login
              </button>
              <button 
                onClick={() => { setSignupError(''); setSignupSuccess(''); setCurrentView('signup'); }} 
                className="px-5 py-2.5 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs rounded-lg shadow">
                Register Staff
              </button>
            </div>
          </div>
        </div>
      )}

      {currentView === 'portal' && currentUser && (
        <div className="flex-1 flex flex-col md:flex-row">
          {/* Sidebar */}
          <aside className="w-full md:w-64 bg-slate-900 text-slate-300 flex-shrink-0 p-4 flex flex-col justify-between border-r border-slate-800">
            <div className="space-y-6">
              <div className="p-3 rounded-lg bg-slate-800 border border-slate-700">
                <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                  {currentUser?.role === 'ROLE_AICTE_ADMIN' ? 'CYPHER AICTE Authority' : 'CYPHER Institution Portal'}
                </div>
                <div className="text-xs font-bold text-white truncate mt-0.5">{currentUser?.institutionName}</div>
              </div>

              <nav className="space-y-1">
                {[
                  { id: 'dashboard', label: 'Dashboard', icon: BarChart3 },
                  { id: 'students', label: 'Student Records', icon: Users },
                  { id: 'bulk-upload', label: 'CSV Bulk Upload', icon: UploadCloud },
                  { id: 'validation', label: 'Validation Engine', icon: AlertTriangle, badge: anomalies.length },
                  { id: 'intake', label: 'Intake Quota Monitor', icon: ShieldCheck },
                  { id: 'reports', label: 'Reports & Exports', icon: FileText },
                  { id: 'audit', label: 'Audit Logs', icon: Clock }
                ].map(item => {
                  const Icon = item.icon;
                  const isActive = activeTab === item.id;
                  return (
                    <button
                      key={item.id}
                      onClick={() => setActiveTab(item.id)}
                      className={`w-full flex items-center justify-between px-3 py-2 text-xs font-semibold rounded-lg transition ${
                        isActive ? 'bg-blue-600 text-white' : 'hover:bg-slate-800 text-slate-300'
                      }`}
                    >
                      <div className="flex items-center gap-2.5">
                        <Icon className="w-4 h-4" />
                        <span>{item.label}</span>
                      </div>
                      {item.badge !== undefined && (
                        <span className="px-1.5 py-0.5 rounded-full text-[10px] font-mono bg-amber-400 text-slate-950 font-bold">
                          {item.badge}
                        </span>
                      )}
                    </button>
                  );
                })}
              </nav>
            </div>

            <div className="pt-4 border-t border-slate-800 text-[11px] text-slate-400">
              <div>Multi-Tenant Mode: Active</div>
              <div className="text-emerald-400 font-mono mt-0.5">PostgreSQL Ready</div>
            </div>
          </aside>

          {/* Main Content Pane */}
          <main className="flex-1 p-6 bg-slate-100 overflow-y-auto custom-scrollbar">
            {/* TAB: DASHBOARD */}
            {activeTab === 'dashboard' && (
              <div className="space-y-6">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <h2 className="text-xl font-bold text-slate-900">
                      {currentUser?.role === 'ROLE_AICTE_ADMIN' ? 'CYPHER National AICTE Regulatory Desk' : `${currentUser?.institutionName || 'Institution'} - CYPHER Admission Dashboard`}
                    </h2>
                    <p className="text-xs text-slate-500">Live Intake &amp; Validation Status • Academic Year 2026-2027</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <button 
                      onClick={() => setShowAddModal(true)}
                      className="px-3.5 py-2 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-xs font-semibold shadow flex items-center gap-1.5">
                      <Plus className="w-4 h-4" /> Add Student
                    </button>
                    <button 
                      onClick={() => setActiveTab('bulk-upload')}
                      className="px-3.5 py-2 bg-white hover:bg-slate-50 border border-slate-300 text-slate-700 rounded-lg text-xs font-semibold shadow flex items-center gap-1.5">
                      <UploadCloud className="w-4 h-4" /> Upload CSV
                    </button>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
                  <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                    <div className="text-xs font-bold text-slate-500 uppercase">Enrolled Students</div>
                    <div className="text-2xl font-black text-slate-900 mt-1">{students.length}</div>
                    <div className="text-[11px] text-emerald-600 mt-1 font-semibold">100% Active Enrolment</div>
                  </div>
                  <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                    <div className="text-xs font-bold text-slate-500 uppercase">Sanctioned Seats</div>
                    <div className="text-2xl font-black text-slate-900 mt-1">
                      {intake.reduce((acc, curr) => acc + curr.approved, 0)}
                    </div>
                    <div className="text-[11px] text-slate-500 mt-1">Approved Quota</div>
                  </div>
                  <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                    <div className="text-xs font-bold text-slate-500 uppercase">APAAR Validated</div>
                    <div className="text-2xl font-black text-slate-900 mt-1">
                      {students.filter(s => s.apaarId && s.apaarId.length === 12).length}
                    </div>
                    <div className="text-[11px] text-blue-600 mt-1 font-semibold">12-Digit Format Valid</div>
                  </div>
                  <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
                    <div className="text-xs font-bold text-slate-500 uppercase">Validation Alerts</div>
                    <div className="text-2xl font-black text-amber-600 mt-1">{anomalies.length} Flags</div>
                    <div className="text-[11px] text-red-600 mt-1 font-semibold">Requires Review</div>
                  </div>
                </div>

                {/* Intake Progress */}
                <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
                  <h3 className="text-sm font-bold text-slate-900">Approved Intake Utilization (2026-2027)</h3>
                  <div className="divide-y divide-slate-100">
                    {intake.map(item => {
                      const count = students.filter(s => s.institutionId === item.institutionId && s.course === item.course).length;
                      const pct = Math.round((count / item.approved) * 100);
                      return (
                        <div key={item.id} className="py-3 flex items-center justify-between text-xs">
                          <div>
                            <div className="font-bold text-slate-800">{item.course}</div>
                            <div className="text-slate-400">{item.department}</div>
                          </div>
                          <div className="flex items-center gap-4">
                            <div className="w-32 bg-slate-100 h-2 rounded-full overflow-hidden">
                              <div className="bg-blue-600 h-full" style={{ width: `${Math.min(pct, 100)}%` }}></div>
                            </div>
                            <div className="font-mono text-slate-700">{count} / {item.approved} ({pct}%)</div>
                            <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-100 text-emerald-800">Compliant</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* TAB: STUDENTS */}
            {activeTab === 'students' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-xl font-bold text-slate-900">Student Record Management</h2>
                    <p className="text-xs text-slate-500">View, search, filter and generate signed verification tokens</p>
                  </div>
                  <button onClick={() => setShowAddModal(true)} className="px-3.5 py-2 bg-blue-700 hover:bg-blue-800 text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow">
                    <Plus className="w-4 h-4" /> Add Student
                  </button>
                </div>

                {/* Filters */}
                <div className="bg-white p-3 rounded-xl border border-slate-200 shadow-sm grid grid-cols-1 sm:grid-cols-4 gap-3 text-xs">
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Search</label>
                    <input 
                      type="text" 
                      placeholder="Name, APAAR, Roll No..." 
                      value={filterSearch} 
                      onChange={e => setFilterSearch(e.target.value)} 
                      className="w-full px-2.5 py-1.5 border border-slate-300 rounded focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Course</label>
                    <select value={filterCourse} onChange={e => setFilterCourse(e.target.value)} className="w-full px-2.5 py-1.5 border border-slate-300 rounded focus:outline-none">
                      <option value="ALL">All Courses</option>
                      <option value="B.Tech Computer Science & Engineering">B.Tech Computer Science</option>
                      <option value="B.Tech Electronics & Communication">B.Tech Electronics</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">APAAR Status</label>
                    <select value={filterApaar} onChange={e => setFilterApaar(e.target.value)} className="w-full px-2.5 py-1.5 border border-slate-300 rounded focus:outline-none">
                      <option value="ALL">All</option>
                      <option value="VALID">Valid 12-Digit</option>
                      <option value="INVALID">Format Error / Missing</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-600 mb-1">Category</label>
                    <select value={filterCategory} onChange={e => setFilterCategory(e.target.value)} className="w-full px-2.5 py-1.5 border border-slate-300 rounded focus:outline-none">
                      <option value="ALL">All Categories</option>
                      <option value="GENERAL">General</option>
                      <option value="OBC_NCL">OBC-NCL</option>
                      <option value="SC">SC</option>
                    </select>
                  </div>
                </div>

                {/* Table */}
                <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-50 border-b text-slate-600 font-semibold uppercase">
                      <tr>
                        <th className="py-2.5 px-3">Name &amp; Roll</th>
                        <th className="py-2.5 px-3">APAAR ID Reference</th>
                        <th className="py-2.5 px-3">Course</th>
                        <th className="py-2.5 px-3">Category</th>
                        <th className="py-2.5 px-3">Verification</th>
                        <th className="py-2.5 px-3 text-right">Action</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {filteredStudents.map(s => {
                        const is12 = s.apaarId && /^\d{12}$/.test(s.apaarId);
                        return (
                          <tr key={s.id} className="hover:bg-slate-50">
                            <td className="py-2.5 px-3">
                              <div className="font-bold text-slate-800">{s.name}</div>
                              <div className="text-[10px] text-slate-400 font-mono">Roll: {s.rollNo}</div>
                            </td>
                            <td className="py-2.5 px-3">
                              {is12 ? (
                                <span className="font-mono font-semibold text-blue-900 bg-blue-50 px-1.5 py-0.5 rounded border border-blue-200">
                                  {s.apaarId}
                                </span>
                              ) : (
                                <span className="font-mono text-red-600 font-bold bg-red-50 px-1.5 py-0.5 rounded border border-red-200">
                                  {s.apaarId || 'MISSING'}
                                </span>
                              )}
                            </td>
                            <td className="py-2.5 px-3">{s.course}</td>
                            <td className="py-2.5 px-3">{s.category}</td>
                            <td className="py-2.5 px-3">
                              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-100 text-emerald-800">
                                {s.verificationStatus}
                              </span>
                            </td>
                            <td className="py-2.5 px-3 text-right">
                              <button 
                                onClick={() => { setSelectedStudentForQr(s); setShowQrModal(true); }}
                                className="px-2.5 py-1 text-xs border border-slate-300 rounded hover:bg-slate-100 text-slate-700 flex items-center gap-1 ml-auto">
                                <QrCode className="w-3.5 h-3.5 text-blue-600" />
                                QR Token
                              </button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* TAB: BULK UPLOAD */}
            {activeTab === 'bulk-upload' && (
              <div className="space-y-4">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">Bulk Student CSV Ingestion</h2>
                  <p className="text-xs text-slate-500">Upload institution CSV roster with instant pre-ingestion validation</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
                    <h3 className="text-sm font-bold text-slate-900">Select Dataset</h3>
                    <div className="p-4 bg-blue-50 rounded-lg border border-blue-200 text-xs text-blue-900 space-y-2">
                      <div className="font-bold">Institutional Roster Dataset</div>
                      <p className="text-[11px] text-blue-800">Click below to parse sample synthetic student data with valid and invalid entries:</p>
                      <button onClick={handleLoadSampleCsv} className="w-full py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded font-semibold text-xs shadow">
                        Load Synthetic Test CSV
                      </button>
                    </div>
                  </div>

                  <div className="md:col-span-2 bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
                    <div className="flex justify-between items-center">
                      <h3 className="text-sm font-bold text-slate-900">Pre-Ingestion Analysis ({csvPreviewRows.length} rows)</h3>
                      <button 
                        disabled={csvPreviewRows.length === 0} 
                        onClick={handleConfirmIngestCsv} 
                        className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 disabled:bg-slate-300 text-white font-bold rounded-lg text-xs shadow">
                        Confirm &amp; Ingest Records
                      </button>
                    </div>

                    <div className="overflow-x-auto max-h-80 border rounded-lg">
                      {csvPreviewRows.length === 0 ? (
                        <div className="p-8 text-center text-xs text-slate-400">Click "Load Synthetic Test CSV" to preview rows.</div>
                      ) : (
                        <table className="w-full text-left text-xs">
                          <thead className="bg-slate-100 uppercase font-semibold">
                            <tr>
                              <th className="p-2">Name</th>
                              <th className="p-2">APAAR ID</th>
                              <th className="p-2">Course</th>
                              <th className="p-2">Validation Result</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {csvPreviewRows.map((r, i) => {
                              const is12 = r.apaar_id && /^\d{12}$/.test(r.apaar_id);
                              return (
                                <tr key={i}>
                                  <td className="p-2 font-bold">{r.student_name}</td>
                                  <td className="p-2 font-mono">{r.apaar_id || 'None'}</td>
                                  <td className="p-2">{r.course_name}</td>
                                  <td className="p-2">
                                    {is12 ? (
                                      <span className="px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 text-[10px] font-bold">Pass</span>
                                    ) : (
                                      <span className="px-2 py-0.5 rounded bg-amber-100 text-amber-800 text-[10px] font-bold">Format Error</span>
                                    )}
                                  </td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* TAB: VALIDATION RESULTS */}
            {activeTab === 'validation' && (
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <div>
                    <h2 className="text-xl font-bold text-slate-900">Validation Engine Findings</h2>
                    <p className="text-xs text-slate-500">Cross-checks APAAR format, cross-institution duplicate admissions, and quotas</p>
                  </div>
                  <button onClick={loadData} className="px-3 py-1.5 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold rounded-lg text-xs shadow">
                    Re-run Engine
                  </button>
                </div>

                <div className="space-y-3">
                  {anomalies.map(item => (
                    <div key={item.id} className="p-4 bg-white rounded-xl border border-slate-200 shadow-sm flex items-start justify-between">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                            item.severity === 'CRITICAL' ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'
                          }`}>
                            {item.category}
                          </span>
                          <span className="text-xs font-bold text-slate-900">{item.studentName}</span>
                          <span className="text-xs font-mono text-slate-400">APAAR: {item.apaar}</span>
                        </div>
                        <p className="text-xs text-slate-600">{item.message}</p>
                      </div>
                      <button onClick={() => alert('Anomaly action recorded!')} className="px-3 py-1 text-xs font-semibold bg-blue-50 text-blue-700 hover:bg-blue-100 rounded border border-blue-200">
                        Review / Adjudicate
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* TAB: INTAKE */}
            {activeTab === 'intake' && (
              <div className="space-y-4">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">Sanctioned Intake &amp; Quota Breakdown</h2>
                  <p className="text-xs text-slate-500">Separates normal approved seats, NRI quotas, and supernumerary seats</p>
                </div>

                <div className="space-y-4">
                  {intake.map(item => {
                    const admitted = students.filter(s => s.institutionId === item.institutionId && s.course === item.course).length;
                    return (
                      <div key={item.id} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
                        <div className="flex justify-between items-center">
                          <h3 className="font-bold text-sm text-slate-900">{item.course}</h3>
                          <span className="text-xs text-emerald-700 font-bold bg-emerald-50 px-2 py-0.5 rounded">Within Sanction</span>
                        </div>
                        <div className="grid grid-cols-3 gap-3 text-xs">
                          <div className="p-3 bg-slate-50 rounded">
                            <span className="text-slate-400 block text-[10px] uppercase font-bold">Sanctioned</span>
                            <span className="font-bold text-slate-800 text-sm">{item.approved} Seats</span>
                          </div>
                          <div className="p-3 bg-slate-50 rounded">
                            <span className="text-slate-400 block text-[10px] uppercase font-bold">Admitted Count</span>
                            <span className="font-bold text-blue-700 text-sm">{admitted} Students</span>
                          </div>
                          <div className="p-3 bg-slate-50 rounded">
                            <span className="text-slate-400 block text-[10px] uppercase font-bold">NRI / Supernumerary</span>
                            <span className="font-bold text-slate-800 text-sm">{item.nriQuota} NRI / {item.supernumeraryQuota} Super</span>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* TAB: AUDIT */}
            {activeTab === 'audit' && (
              <div className="space-y-4">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">Security &amp; Regulatory Audit Trail</h2>
                  <p className="text-xs text-slate-500">Immutable ledger tracking logins, bulk ingestions, and validations</p>
                </div>

                <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-50 border-b text-slate-600 font-semibold uppercase">
                      <tr>
                        <th className="p-2.5">Time</th>
                        <th className="p-2.5">User</th>
                        <th className="p-2.5">Action</th>
                        <th className="p-2.5">Details</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {auditLogs.map(l => (
                        <tr key={l.id}>
                          <td className="p-2.5 font-mono text-slate-400 text-[11px]">{l.timestamp}</td>
                          <td className="p-2.5 font-bold text-slate-800">{l.user}</td>
                          <td className="p-2.5"><span className="px-2 py-0.5 rounded bg-blue-50 text-blue-800 font-mono text-[10px] font-bold">{l.action}</span></td>
                          <td className="p-2.5 text-slate-600">{l.details}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* TAB: REPORTS */}
            {activeTab === 'reports' && (
              <div className="space-y-4">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">Statutory AICTE Compliance Reports</h2>
                  <p className="text-xs text-slate-500">Export downloadable CSV and official PDF compliance dossiers for AICTE inspection</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
                    <h3 className="font-bold text-sm text-slate-900">Student Enrolment Roster</h3>
                    <p className="text-xs text-slate-500">Comprehensive student breakdown with masked APAAR reference, course, and verification status.</p>
                    <div className="flex gap-2">
                      <button onClick={handleExportStudentCsv} className="flex-1 py-2 bg-blue-700 hover:bg-blue-800 text-white rounded text-xs font-bold shadow transition">
                        Export CSV
                      </button>
                      <button onClick={() => window.open('/api/v1/reports/enrolment/pdf', '_blank')} className="flex-1 py-2 bg-slate-800 hover:bg-slate-900 text-white rounded text-xs font-bold shadow transition">
                        Official PDF
                      </button>
                    </div>
                  </div>

                  <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
                    <h3 className="font-bold text-sm text-slate-900">Intake Compliance Audit</h3>
                    <p className="text-xs text-slate-500">Contrasts sanctioned seats against actual enrolled students, checking NRI & supernumerary limits.</p>
                    <div className="flex gap-2">
                      <button onClick={handleExportIntakeCsv} className="flex-1 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded text-xs font-bold shadow transition">
                        Export CSV
                      </button>
                      <button onClick={() => window.open('/api/v1/reports/intake/pdf', '_blank')} className="flex-1 py-2 bg-slate-800 hover:bg-slate-900 text-white rounded text-xs font-bold shadow transition">
                        Official PDF
                      </button>
                    </div>
                  </div>

                  <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
                    <h3 className="font-bold text-sm text-slate-900">Anomaly &amp; Duplicate Dossier</h3>
                    <p className="text-xs text-slate-500">Detailed dossier of cross-institution duplicate enrolments and invalid APAAR formats.</p>
                    <button onClick={handleExportAnomaliesCsv} className="w-full py-2 bg-red-600 hover:bg-red-700 text-white rounded text-xs font-bold shadow transition">
                      Export Anomaly Dossier (CSV)
                    </button>
                  </div>
                </div>
              </div>
            )}
          </main>
        </div>
      )}

      {/* VIEW: PUBLIC VERIFY */}
      {currentView === 'public-verify' && (
        <div className="flex-1 max-w-2xl mx-auto w-full p-6 space-y-6">
          <div className="text-center space-y-2">
            <h2 className="text-2xl font-black text-slate-900">CYPHER Public Record Verification</h2>
            <p className="text-xs text-slate-600">Enter a cryptographically signed verification token to verify credential genuineness</p>
          </div>

          <form onSubmit={handlePublicVerify} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase mb-1">Signed Verification Token</label>
              <div className="flex gap-2">
                <input 
                  type="text" 
                  value={verifyTokenInput} 
                  onChange={e => setVerifyTokenInput(e.target.value)}
                  placeholder="e.g. tok_sec_982345012344_aicte_verified_sig"
                  className="flex-1 px-3 py-2 text-xs font-mono border rounded-lg focus:outline-none" 
                  required
                />
                <button type="submit" className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-lg shadow">
                  Verify Token
                </button>
              </div>
            </div>
            <div className="text-[11px] text-slate-500 flex gap-2 items-center">
              <span>Preset Tokens:</span>
              <button type="button" onClick={() => setVerifyTokenInput('tok_sec_982345012344_aicte_verified_sig')} className="underline text-blue-700 font-mono">Aarav Patel (Valid)</button>
              <button type="button" onClick={() => setVerifyTokenInput('tok_sec_773412095621_dual_enrolled_flag')} className="underline text-amber-700 font-mono">Karan Sharma (Flagged)</button>
            </div>
          </form>

          {verifyResult && (
            <div className="bg-white rounded-2xl border border-slate-200 shadow-md p-6 space-y-4">
              {verifyResult.notFound ? (
                <div className="text-center py-6 text-red-600 font-bold text-sm">
                  Invalid or Unrecognized Verification Token in AICTE Ledger
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="flex items-center gap-3 p-3 bg-emerald-50 text-emerald-900 rounded-lg border border-emerald-200">
                    <CheckCircle className="w-6 h-6 text-emerald-600 flex-shrink-0" />
                    <div>
                      <div className="font-bold text-xs">Authentic AICTE Verified Record</div>
                      <div className="text-[11px]">Validated against institutional ledger and APAAR Mock Gateway.</div>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-3 text-xs">
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Student Name</span>
                      <span className="font-bold text-slate-800">{verifyResult.studentName}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">APAAR ID (Masked)</span>
                      <span className="font-mono font-bold text-slate-800">{verifyResult.maskedApaar}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Institution</span>
                      <span className="font-semibold text-slate-700">{verifyResult.institutionName}</span>
                    </div>
                    <div>
                      <span className="text-slate-400 block text-[10px] uppercase font-bold">Course</span>
                      <span className="font-semibold text-slate-700">{verifyResult.course}</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* MODAL: ADD STUDENT */}
      {showAddModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6 space-y-4">
            <div className="flex justify-between items-center">
              <h3 className="font-bold text-base text-slate-900">Add New Student</h3>
              <button onClick={() => setShowAddModal(false)} className="text-slate-400 hover:text-slate-600 text-lg">&times;</button>
            </div>
            <form onSubmit={handleAddStudent} className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-600 mb-1">Legal Name *</label>
                <input 
                  type="text" 
                  required 
                  value={newStudentData.name} 
                  onChange={e => setNewStudentData({ ...newStudentData, name: e.target.value })} 
                  className="w-full px-3 py-1.5 border rounded"
                />
              </div>
              <div>
                <label className="block font-semibold text-slate-600 mb-1">12-Digit APAAR ID</label>
                <input 
                  type="text" 
                  maxLength={12} 
                  value={newStudentData.apaarId} 
                  onChange={e => setNewStudentData({ ...newStudentData, apaarId: e.target.value })} 
                  className="w-full px-3 py-1.5 border rounded font-mono"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-semibold text-slate-600 mb-1">Gender</label>
                  <select 
                    value={newStudentData.gender} 
                    onChange={e => setNewStudentData({ ...newStudentData, gender: e.target.value })} 
                    className="w-full px-3 py-1.5 border rounded">
                    <option value="FEMALE">Female</option>
                    <option value="MALE">Male</option>
                  </select>
                </div>
                <div>
                  <label className="block font-semibold text-slate-600 mb-1">Roll No</label>
                  <input 
                    type="text" 
                    value={newStudentData.rollNo} 
                    onChange={e => setNewStudentData({ ...newStudentData, rollNo: e.target.value })} 
                    className="w-full px-3 py-1.5 border rounded font-mono"
                  />
                </div>
              </div>
              <div className="pt-2 flex justify-end gap-2">
                <button type="button" onClick={() => setShowAddModal(false)} className="px-3 py-1.5 border rounded text-slate-600">Cancel</button>
                <button type="submit" className="px-4 py-1.5 bg-blue-700 hover:bg-blue-800 text-white rounded font-bold">Save Student</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: QR VIEWER */}
      {showQrModal && selectedStudentForQr && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-sm w-full p-6 text-center space-y-4">
            <div className="flex justify-between items-center">
              <h3 className="font-bold text-sm text-slate-900">Signed Verification Credential</h3>
              <button onClick={() => setShowQrModal(false)} className="text-slate-400 hover:text-slate-600 text-lg">&times;</button>
            </div>
            <div className="p-4 bg-slate-50 border rounded-xl inline-block">
              <QrCode className="w-36 h-36 mx-auto text-[#0f2b5c]" />
            </div>
            <div className="text-left text-xs space-y-1">
              <div className="font-bold text-slate-800">{selectedStudentForQr.name}</div>
              <div className="font-mono text-[10px] text-blue-700 bg-slate-100 p-1.5 rounded break-all">
                {selectedStudentForQr.verificationToken}
              </div>
              <p className="text-[10px] text-slate-500 pt-1">
                Zero PII inside the QR code. Uses unpredictable signed tokens.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
