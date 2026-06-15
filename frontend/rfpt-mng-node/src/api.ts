import axios from 'axios';

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
  createdAt: string;
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
  updatedAt?: string;
}

const request = axios.create({ baseURL: '' });

interface ApiResult<T> {
  code?: number | string;
  message?: string;
  data: T;
}

async function unwrap<T>(promise: Promise<{ data: ApiResult<T> | T }>): Promise<T> {
  const response = await promise;
  const body = response.data as ApiResult<T>;
  if (body && body.code !== undefined && body.code !== 0 && body.code !== '000000') {
    throw new Error(body.message || '请求失败');
  }
  return body && Object.prototype.hasOwnProperty.call(body, 'data') ? body.data : (response.data as T);
}

export function createBatch(data: { regionCode: string; siteType: string; periodMonth: string; taxNoList: string[]; operator: string }) {
  return unwrap<number>(request.post('/api/social-security-payments/batches', data));
}

export function fetchBatches(params: Record<string, unknown>) {
  return unwrap<PageResp<BatchRecord>>(request.get('/api/social-security-payments/batches', { params }));
}

export function fetchTasks(params: Record<string, unknown>) {
  return unwrap<PageResp<TaskRecord>>(request.get('/api/social-security-payments/tasks', { params }));
}

export function retryTask(taskId: number, operator: string) {
  return unwrap<void>(request.post(`/api/social-security-payments/tasks/${taskId}/retry`, { operator }));
}
