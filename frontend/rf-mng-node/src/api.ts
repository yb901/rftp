import axios from 'axios';

export interface LoginUser {
  id: number;
  username: string;
  realName: string;
  role: number;
}

export interface LoginResult {
  token?: string;
  user: LoginUser;
}

export interface PageResp<T> {
  list: T[];
  pagination: { page: number; size: number; total: number };
}

export interface BatchRecord {
  id: number;
  regionCode: string;
  regionName?: string;
  periodMonth: string;
  status: string;
  totalCount: number;
  successCount: number;
  failedCount: number;
  gmtCreate: string;
}

export interface TaskRecord {
  id: number;
  batchId?: number;
  taxNo: string;
  enterpriseName?: string;
  securityAccountName?: string;
  regionCode: string;
  periodMonth?: string;
  status: string;
  payableAmount?: number;
  errorCode?: string;
  errorMessage?: string;
  retryable?: boolean;
  workerId?: string;
  claimedAt?: string;
  heartbeatAt?: string;
  finishedAt?: string;
  gmtModified?: string;
}

export interface PerformanceTask {
  id: number;
  performanceDescription: string;
  periodStartDate: string;
  periodEndDate: string;
  confirmDeadlineTime: string;
  secondConfirmDeadlineTime?: string;
  statusCode?: string;
  totalCount?: number;
  confirmedCount?: number;
  feedbackCount?: number;
  autoConfirmedCount?: number;
}

export interface EmployeePerformanceImportItem {
  rowNo?: number;
  employeeName: string;
  mobile: string;
  employeeNo?: string;
  projectDepartment?: string;
  positionName?: string;
  performance: string;
}

export interface EmployeePerformanceImportResult {
  success: boolean;
  successCount: number;
  failCount: number;
  errors?: Array<{ rowNo?: number; mobile?: string; errorMessage?: string }>;
}

export interface EmployeePerformanceRecord {
  id: number;
  taskId: number;
  performanceDescription?: string;
  employeeName: string;
  mobile: string;
  employeeNo?: string;
  projectDepartment?: string;
  positionName?: string;
  performance: string;
  confirmStatus: string;
  feedbackStatus: string;
  feedbackContent?: string;
  feedbackHandleOpinion?: string;
  feedbackHandleAdminName?: string;
}

export interface AdminUser {
  id: number;
  username: string;
  realName?: string;
  enabled: number;
  role: number;
  totpEnabled?: boolean;
  gmtCreate?: string;
  gmtModified?: string;
}

export interface AdminSaveParam {
  id?: number;
  username: string;
  realName?: string;
  password?: string;
  enabled?: number;
  role: number;
}

export interface AdminTotpResult {
  secret: string;
  qrCodeUri: string;
  username: string;
}

const request = axios.create({ baseURL: import.meta.env.API_BASE_URL || '', withCredentials: true });

interface ApiResult<T> {
  code?: number | string;
  message?: string;
  data: T;
}

async function unwrap<T>(promise: Promise<{ data: ApiResult<T> | T }>): Promise<T> {
  const response = await promise;
  const body = response.data as ApiResult<T>;
  if (body && body.code !== undefined && body.code !== 0 && body.code !== '000000' && body.code !== 'E000000') {
    if (body.code === '100005') {
      window.localStorage.removeItem('rf_mng_login_user');
      window.dispatchEvent(new Event('rf_mng_unauthorized'));
    }
    throw new Error(body.message || '请求失败');
  }
  return body && Object.prototype.hasOwnProperty.call(body, 'data') ? body.data : (response.data as T);
}

export function getRequestErrorMessage(error: unknown, fallback = '请求失败，请稍后重试') {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as Partial<ApiResult<unknown>> | undefined;
    if (data?.message) {
      return data.message;
    }
    if (error.message) {
      return error.message;
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function login(data: { username: string; password: string; otpCode?: string }) {
  return unwrap<LoginResult>(request.post('/mng/auth/login', data));
}

export function logout() {
  return unwrap<void>(request.post('/mng/auth/logout'));
}

export function createBatch(data: { regionCode: string; siteType: string; periodMonth: string; taxNoList: string[]; createAdminId?: number; createAdminName?: string }) {
  return unwrap<number>(request.post('/api/social-security-payments/batches', data));
}

export function fetchBatches(params: Record<string, unknown>) {
  return unwrap<PageResp<BatchRecord>>(request.get('/api/social-security-payments/batches', { params }));
}

export function fetchTasks(params: Record<string, unknown>) {
  return unwrap<PageResp<TaskRecord>>(request.get('/api/social-security-payments/tasks', { params }));
}

export function retryTask(taskId: number) {
  return unwrap<void>(request.post(`/api/social-security-payments/tasks/${taskId}/retry`));
}

export function createPerformanceTask(data: {
  performanceDescription: string;
  periodStartDate: string;
  periodEndDate: string;
  confirmDeadlineTime: string;
  secondConfirmDeadlineTime?: string;
  createAdminId?: number;
  createAdminName?: string;
}) {
  return unwrap<PerformanceTask>(request.post('/api/performance/tasks', data));
}

export function fetchPerformanceTasks(params: Record<string, unknown>) {
  return unwrap<PageResp<PerformanceTask>>(request.get('/api/performance/tasks', { params }));
}

export function deletePerformanceTask(taskId: number) {
  return unwrap<void>(request.post(`/api/performance/tasks/${taskId}/delete`));
}

export function enablePerformanceTask(taskId: number) {
  return unwrap<void>(request.post(`/api/performance/tasks/${taskId}/enable`));
}

export function disablePerformanceTask(taskId: number) {
  return unwrap<void>(request.post(`/api/performance/tasks/${taskId}/disable`));
}

export function importPerformanceRecords(taskId: number, records: EmployeePerformanceImportItem[]) {
  return unwrap<EmployeePerformanceImportResult>(request.post(`/api/performance/tasks/${taskId}/records/import`, { records }));
}

export function fetchPerformanceRecords(params: Record<string, unknown>) {
  return unwrap<PageResp<EmployeePerformanceRecord>>(request.get('/api/performance/records', { params }));
}

export function exportPerformanceRecords(params: Record<string, unknown>) {
  return request.get('/api/performance/records/export', { params, responseType: 'blob' });
}

export function adjustPerformanceRecord(recordId: number, data: {
  afterPerformance: string;
  adjustReason?: string;
  operatorAdminId?: number;
  operatorAdminName?: string;
  operatorMobile?: string;
}) {
  return unwrap<void>(request.post(`/api/performance/records/${recordId}/adjust`, data));
}

export function handlePerformanceFeedbackUnchanged(recordId: number, data: {
  handleOpinion: string;
  operatorAdminId?: number;
  operatorAdminName?: string;
}) {
  return unwrap<void>(request.post(`/api/performance/records/${recordId}/feedback/unchanged`, data));
}

export function fetchAdmins(params: Record<string, unknown>) {
  return unwrap<PageResp<AdminUser>>(request.get('/mng/admin/list', { params }));
}

export function saveAdmin(data: AdminSaveParam) {
  return unwrap<AdminUser>(request.post('/mng/admin/save', data));
}

export function updateAdmin(data: AdminSaveParam) {
  return unwrap<AdminUser>(request.post('/mng/admin/update', data));
}

export function deleteAdmin(id: number) {
  return unwrap<void>(request.post('/mng/admin/delete', { id }));
}

export function generateAdminTotp(userId: number) {
  return unwrap<AdminTotpResult>(request.post('/mng/admin/generateTotp', { userId }));
}

export function disableAdminTotp(userId: number) {
  return unwrap<void>(request.post('/mng/admin/disableTotp', { userId }));
}
