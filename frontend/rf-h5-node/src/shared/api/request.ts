import axios, { type AxiosRequestConfig } from 'axios';
import { Toast } from 'antd-mobile';
import { appConfig } from '../env/config';
import type { ApiResult } from './types';

/**
 * H5 API 请求配置。
 */
interface ApiRequestConfig extends AxiosRequestConfig {
  /**
   * 是否静默处理错误提示。
   */
  silentError?: boolean;
}

/**
 * H5 API 请求客户端。
 */
export const request = axios.create({
  baseURL: appConfig.apiBaseUrl,
  timeout: 20000,
  withCredentials: true,
});

/**
 * 读取响应中的业务数据。
 */
function unwrapResult<T>(result: ApiResult<T>): T {
  if (!result || result.code === 'E000000') {
    return result?.data;
  }
  throw new Error(result.message || '请求失败');
}

/**
 * GET 请求。
 */
export async function apiGet<T>(url: string, config?: ApiRequestConfig) {
  try {
    const response = await request.get<ApiResult<T>>(url, config);
    return unwrapResult(response.data);
  } catch (error) {
    const message = error instanceof Error ? error.message : '网络异常';
    if (!config?.silentError) {
      Toast.show({ icon: 'fail', content: message });
    }
    throw error;
  }
}

/**
 * POST 请求。
 */
export async function apiPost<T>(url: string, data?: unknown, config?: ApiRequestConfig) {
  try {
    const response = await request.post<ApiResult<T>>(url, data, config);
    return unwrapResult(response.data);
  } catch (error) {
    const message = error instanceof Error ? error.message : '网络异常';
    if (!config?.silentError) {
      Toast.show({ icon: 'fail', content: message });
    }
    throw error;
  }
}
