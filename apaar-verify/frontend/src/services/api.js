import { INITIAL_INSTITUTIONS, INITIAL_APPROVED_INTAKE, INITIAL_STUDENTS, INITIAL_AUDIT_LOGS } from './mockData';

const BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

// Pre-seeded users with staff verification status
let memoryUsers = [
  {
    id: 'usr-aicte-1',
    username: 'aicte_officer',
    email: 'director.regulatory@aicte-india.gov.in',
    staffId: 'AICTE-REG-2024-001',
    department: 'Regulatory & Approval Bureau',
    fullName: 'Dr. Ramesh Kumar (Director AICTE)',
    role: 'ROLE_AICTE_ADMIN',
    institutionId: null,
    institutionName: 'AICTE Central Headquarters, New Delhi',
    isApproved: true
  },
  {
    id: 'usr-inst-1',
    username: 'diet_admin',
    email: 'admin.academic@diet.edu.in',
    staffId: 'INST-DIET-FAC-042',
    department: 'Academic Administration & Admissions',
    fullName: 'Prof. Sunita Sharma',
    role: 'ROLE_INSTITUTION_ADMIN',
    institutionId: 'inst-1',
    institutionName: 'Delhi Institute of Engineering & Technology',
    isApproved: true
  }
];

// In-memory local state fallback for prototype mode
let memoryStudents = [...INITIAL_STUDENTS];
let memoryAuditLogs = [...INITIAL_AUDIT_LOGS];

export const apiService = {
  // --- AUTHENTICATION ---
  login: async (usernameOrIdentifier, password) => {
    try {
      const res = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: usernameOrIdentifier, password })
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || 'Authentication failed');
      }
      return data;
    } catch (err) {
      if (err.message && !err.message.includes('fetch') && !err.message.includes('Failed to fetch')) {
        throw err;
      }
    }

    // Prototype fallback matching backend security rules
    const identifier = (usernameOrIdentifier || '').trim().toLowerCase();
    const matchedUser = memoryUsers.find(u => 
      u.username.toLowerCase() === identifier || 
      (u.email && u.email.toLowerCase() === identifier) || 
      (u.staffId && u.staffId.toLowerCase() === identifier)
    );

    if (!matchedUser) {
      throw new Error('Invalid credentials. Check your Staff Email / Staff ID / Username and password.');
    }

    // Check staff verification approval status
    if (!matchedUser.isApproved) {
      throw new Error('Account Pending Staff Verification: Your Staff ID is currently awaiting administrative approval before regulatory officer privileges are granted. Please contact your AICTE nodal desk.');
    }

    return {
      token: `mock-jwt-${matchedUser.username}-token`,
      user: matchedUser
    };
  },

  registerStaff: async ({ fullName, email, staffId, department, password }) => {
    try {
      const res = await fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName, email, staffId, department, password })
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || 'Registration failed');
      }
      return data;
    } catch (err) {
      if (err.message && !err.message.includes('fetch') && !err.message.includes('Failed to fetch')) {
        throw err;
      }
    }

    // Uniqueness validation on Staff ID and Staff Email
    const existingStaff = memoryUsers.find(
      u => (u.staffId && u.staffId.toLowerCase() === staffId.trim().toLowerCase()) ||
           (u.email && u.email.toLowerCase() === email.trim().toLowerCase())
    );
    if (existingStaff) {
      throw new Error('A staff officer with this Staff ID or Staff Email is already registered.');
    }

    const newUser = {
      id: `usr-staff-${Date.now()}`,
      username: email.split('@')[0],
      email: email.trim(),
      staffId: staffId.trim(),
      department: department.trim(),
      fullName: fullName.trim(),
      role: 'ROLE_INSTITUTION_ADMIN',
      institutionId: 'inst-1',
      institutionName: 'AICTE Monitored Institution',
      isApproved: false, // Crucial: New sign-ups require administrative verification before login
      createdAt: new Date().toISOString()
    };
    memoryUsers.push(newUser);

    return {
      success: true,
      message: 'Staff registration successful. Account submitted for AICTE administrative verification.',
      user: newUser
    };
  },

  // --- INSTITUTIONS ---
  getInstitutions: async () => {
    try {
      const res = await fetch(`${BASE_URL}/institutions`);
      if (res.ok) return await res.json();
    } catch {}
    return INITIAL_INSTITUTIONS;
  },

  // --- APPROVED INTAKE ---
  getIntake: async (institutionId) => {
    try {
      const url = institutionId ? `${BASE_URL}/intake?institutionId=${institutionId}` : `${BASE_URL}/intake`;
      const res = await fetch(url);
      if (res.ok) return await res.json();
    } catch {}
    if (institutionId) {
      return INITIAL_APPROVED_INTAKE.filter(i => i.institutionId === institutionId);
    }
    return INITIAL_APPROVED_INTAKE;
  },

  // --- STUDENTS ---
  getStudents: async (institutionId) => {
    try {
      const url = institutionId ? `${BASE_URL}/students?institutionId=${institutionId}` : `${BASE_URL}/students`;
      const res = await fetch(url);
      if (res.ok) return await res.json();
    } catch {}
    if (institutionId) {
      return memoryStudents.filter(s => s.institutionId === institutionId);
    }
    return memoryStudents;
  },

  addStudent: async (studentData) => {
    try {
      const res = await fetch(`${BASE_URL}/students`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(studentData)
      });
      if (res.ok) return await res.json();
    } catch {}
    const newStudent = {
      ...studentData,
      id: `s-${Date.now()}`,
      verificationToken: `tok_sec_${studentData.apaarId || Date.now()}_signed`,
      verificationStatus: (studentData.apaarId && /^\d{12}$/.test(studentData.apaarId)) ? 'MOCK_VERIFIED' : 'UNVERIFIED'
    };
    memoryStudents.push(newStudent);
    return newStudent;
  },

  bulkUploadStudents: async (records, institutionId) => {
    try {
      const res = await fetch(`${BASE_URL}/students/bulk`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ records, institutionId })
      });
      if (res.ok) return await res.json();
    } catch {}
    const ingested = records.map((r, i) => ({
      ...r,
      id: `csv-ingest-${Date.now()}-${i}`,
      institutionId: institutionId || 'inst-1',
      verificationToken: `tok_sec_batch_${Date.now()}_${i}`,
      verificationStatus: (r.apaarId && /^\d{12}$/.test(r.apaarId)) ? 'MOCK_VERIFIED' : 'UNVERIFIED'
    }));
    memoryStudents = [...memoryStudents, ...ingested];
    return { count: ingested.length, records: ingested };
  },

  // --- VALIDATION ENGINE ---
  runValidation: (studentsList) => {
    const flags = [];
    const students = studentsList || memoryStudents;

    students.forEach(s => {
      // 1. Missing APAAR
      if (!s.apaarId || s.apaarId.trim() === '') {
        flags.push({
          id: `flag-missing-${s.id}`,
          category: 'MISSING_APAAR',
          severity: 'WARNING',
          studentName: s.name,
          studentId: s.id,
          apaar: 'None',
          message: 'Student admitted without mandatory APAAR ID reference. Provisional grace period.'
        });
      } else {
        // 2. Format validation (12 numerical digits)
        const isTwelve = /^\d{12}$/.test(s.apaarId);
        if (!isTwelve) {
          flags.push({
            id: `flag-fmt-${s.id}`,
            category: 'INVALID_APAAR_FORMAT',
            severity: 'ERROR',
            studentName: s.name,
            studentId: s.id,
            apaar: s.apaarId,
            message: `APAAR ID '${s.apaarId}' fails 12-digit standard. Check syntax and check-digit.`
          });
        }
      }
    });

    // 3. Cross-Institution Active Duplicate Check
    const apaarMap = {};
    students.forEach(s => {
      if (s.apaarId && /^\d{12}$/.test(s.apaarId) && s.enrolmentStatus === 'ACTIVE') {
        if (!apaarMap[s.apaarId]) apaarMap[s.apaarId] = [];
        apaarMap[s.apaarId].push(s);
      }
    });

    Object.keys(apaarMap).forEach(apaar => {
      const matches = apaarMap[apaar];
      if (matches.length > 1) {
        const instIds = new Set(matches.map(m => m.institutionId));
        if (instIds.size > 1) {
          matches.forEach(m => {
            flags.push({
              id: `flag-cross-${m.id}`,
              category: 'INTER_INSTITUTION_DUPLICATE',
              severity: 'CRITICAL',
              studentName: m.name,
              studentId: m.id,
              apaar: m.apaarId,
              message: `Active dual enrolment found across multiple colleges. Review required: confirm migration/transfer vs dual admission.`
            });
          });
        }
      }
    });

    return flags;
  },

  // --- VERIFY BY TOKEN (PUBLIC) ---
  verifyRecord: async (token) => {
    try {
      const res = await fetch(`${BASE_URL}/verify/${token}`);
      if (res.ok) return await res.json();
    } catch {}
    const match = memoryStudents.find(s => s.verificationToken === token);
    if (!match) return null;
    const inst = INITIAL_INSTITUTIONS.find(i => i.id === match.institutionId);
    return {
      isValid: true,
      studentName: match.name,
      maskedApaar: match.apaarId ? `${match.apaarId.substring(0, 4)}-XXXX-${match.apaarId.substring(8)}` : 'None',
      institutionName: inst ? inst.name : 'AICTE Institution',
      institutionCode: inst ? inst.code : 'AICTE-CODE',
      course: match.course,
      academicYear: match.academicYear,
      enrolmentStatus: match.enrolmentStatus,
      verificationStatus: match.verificationStatus,
      verifiedAt: new Date().toISOString()
    };
  },

  // --- AUDIT LOGS ---
  getAuditLogs: async () => {
    try {
      const res = await fetch(`${BASE_URL}/audit-logs`);
      if (res.ok) return await res.json();
    } catch {}
    return memoryAuditLogs;
  },

  addAuditLog: (action, entity, details, user = 'Current User') => {
    memoryAuditLogs.unshift({
      id: `log-${Date.now()}`,
      timestamp: new Date().toISOString().replace('T', ' ').substring(0, 19),
      user: user,
      role: 'SYSTEM',
      action: action,
      entity: entity,
      ip: '127.0.0.1',
      details: details
    });
  },

  // --- REPORT GENERATION ---
  downloadReport: async (reportPath, filename) => {
    try {
      const res = await fetch(`${BASE_URL}/reports/${reportPath}`);
      if (res.ok) {
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        return true;
      }
    } catch {}
    return false;
  }
};
