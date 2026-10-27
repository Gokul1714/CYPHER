export const INITIAL_INSTITUTIONS = [
  { id: 'inst-1', code: '1-AICTE-DELHI-001', name: 'Delhi Institute of Engineering & Technology', state: 'Delhi', district: 'New Delhi', type: 'Central University' },
  { id: 'inst-2', code: '1-AICTE-MAHA-002', name: 'Maharashtra National Institute of Technology', state: 'Maharashtra', district: 'Pune', type: 'State Government' },
  { id: 'inst-3', code: '1-AICTE-KARN-003', name: 'Bangalore Institute of Applied Sciences', state: 'Karnataka', district: 'Bengaluru', type: 'Private-Self Financed' }
];

export const INITIAL_APPROVED_INTAKE = [
  { id: 'intake-1', institutionId: 'inst-1', course: 'B.Tech Computer Science & Engineering', department: 'Engineering & Technology', academicYear: '2026-2027', approved: 120, nriQuota: 6, supernumeraryQuota: 12 },
  { id: 'intake-2', institutionId: 'inst-1', course: 'B.Tech Electronics & Communication', department: 'Engineering & Technology', academicYear: '2026-2027', approved: 60, nriQuota: 3, supernumeraryQuota: 6 },
  { id: 'intake-3', institutionId: 'inst-2', course: 'B.Tech Computer Science & Engineering', department: 'Engineering & Technology', academicYear: '2026-2027', approved: 180, nriQuota: 9, supernumeraryQuota: 18 },
  { id: 'intake-4', institutionId: 'inst-2', course: 'B.Tech Mechanical Engineering', department: 'Engineering & Technology', academicYear: '2026-2027', approved: 60, nriQuota: 3, supernumeraryQuota: 6 }
];

export const INITIAL_STUDENTS = [
  {
    id: 's1',
    name: 'Aarav Patel',
    gender: 'MALE',
    dob: '2005-04-12',
    apaarId: '982345012344',
    institutionId: 'inst-1',
    course: 'B.Tech Computer Science & Engineering',
    academicYear: '2026-2027',
    rollNo: '26-CSE-001',
    category: 'GENERAL',
    isNri: false,
    isSupernumerary: false,
    enrolmentStatus: 'ACTIVE',
    verificationStatus: 'MOCK_VERIFIED',
    verificationToken: 'tok_sec_982345012344_aicte_verified_sig'
  },
  {
    id: 's2',
    name: 'Priya Iyer',
    gender: 'FEMALE',
    dob: '2005-08-23',
    apaarId: '814529081239',
    institutionId: 'inst-1',
    course: 'B.Tech Computer Science & Engineering',
    academicYear: '2026-2027',
    rollNo: '26-CSE-002',
    category: 'OBC_NCL',
    isNri: false,
    isSupernumerary: false,
    enrolmentStatus: 'ACTIVE',
    verificationStatus: 'MOCK_VERIFIED',
    verificationToken: 'tok_sec_814529081239_aicte_verified_sig'
  },
  {
    id: 's3',
    name: 'Rohan Verma',
    gender: 'MALE',
    dob: '2004-11-05',
    apaarId: '12345', // Format error (<12 digits)
    institutionId: 'inst-1',
    course: 'B.Tech Computer Science & Engineering',
    academicYear: '2026-2027',
    rollNo: '26-CSE-003',
    category: 'SC',
    isNri: false,
    isSupernumerary: false,
    enrolmentStatus: 'ACTIVE',
    verificationStatus: 'UNVERIFIED',
    verificationToken: 'tok_sec_12345_unverified'
  },
  {
    id: 's4',
    name: 'Ananya Sengupta',
    gender: 'FEMALE',
    dob: '2005-01-30',
    apaarId: '', // Missing APAAR
    institutionId: 'inst-1',
    course: 'B.Tech Electronics & Communication',
    academicYear: '2026-2027',
    rollNo: '26-ECE-001',
    category: 'GENERAL',
    isNri: false,
    isSupernumerary: false,
    enrolmentStatus: 'ACTIVE',
    verificationStatus: 'UNVERIFIED',
    verificationToken: 'tok_sec_ananya_missing'
  },
  {
    id: 's5',
    name: 'Karan Johar Sharma',
    gender: 'MALE',
    dob: '2005-06-19',
    apaarId: '773412095621',
    institutionId: 'inst-1',
    course: 'B.Tech Computer Science & Engineering',
    academicYear: '2026-2027',
    rollNo: '26-CSE-045',
    category: 'GENERAL',
    isNri: false,
    isSupernumerary: false,
    enrolmentStatus: 'ACTIVE',
    verificationStatus: 'MOCK_VERIFIED',
    verificationToken: 'tok_sec_773412095621_dual_enrolled_flag'
  },
  {
    id: 's6',
    name: 'Karan Johar Sharma', // Intentional cross-institution dual active match!
    gender: 'MALE',
    dob: '2005-06-19',
    apaarId: '773412095621',
    institutionId: 'inst-2', // Admitted also in MNIT Pune
    course: 'B.Tech Computer Science & Engineering',
    academicYear: '2026-2027',
    rollNo: '26-MNIT-112',
    category: 'GENERAL',
    isNri: false,
    isSupernumerary: false,
    enrolmentStatus: 'ACTIVE',
    verificationStatus: 'MOCK_VERIFIED',
    verificationToken: 'tok_sec_773412095621_dual_enrolled_flag'
  }
];

export const INITIAL_AUDIT_LOGS = [
  { id: 'log-1', timestamp: '2026-10-27 10:15:30', user: 'Prof. Sunita Sharma', role: 'ROLE_INSTITUTION_ADMIN', action: 'BATCH_VALIDATION', entity: 'ENROLMENTS', ip: '192.168.1.45', details: 'Automated 12-digit format & quota scan triggered' },
  { id: 'log-2', timestamp: '2026-10-27 09:40:12', user: 'Dr. Ramesh Kumar', role: 'ROLE_AICTE_ADMIN', action: 'CROSS_DUPLICATE_AUDIT', entity: 'STUDENTS', ip: '14.139.58.12', details: 'Cross-institution active match detected for APAAR 773412095621' },
  { id: 'log-3', timestamp: '2026-10-27 08:30:00', user: 'Public Verifier', role: 'PUBLIC', action: 'TOKEN_VERIFY', entity: 'VERIFICATION_TOKEN', ip: '103.21.125.88', details: 'Successful token lookup for student Aarav Patel' }
];
