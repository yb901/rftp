import { apiGet } from '../../shared/api/request';
import { apiPost } from '../../shared/api/request';
import type { EmployeePerformance, PerformanceCaptchaConfig, PerformanceLoginResult } from './types';

/**
 * 员工绩效员工端 API。
 */
export const performanceApi = {
  /**
   * 查询当前登录态。
   */
  me() {
    return apiGet<PerformanceLoginResult>('/performance/employee/auth/me', { silentError: true });
  },

  /**
   * 查询图形验证码配置。
   */
  captchaConfig() {
    return apiGet<PerformanceCaptchaConfig>('/performance/employee/auth/captcha/config');
  },

  /**
   * 发送短信验证码。
   */
  sendSmsCode(data: { mobile: string; scene: string; captchaTraceId?: string }) {
    return apiPost<number>('/performance/employee/auth/sms/send', data);
  },

  /**
   * 手机号登录。
   */
  login(data: { mobile: string; smsCode: string }) {
    return apiPost<PerformanceLoginResult>('/performance/employee/auth/login', data);
  },

  /**
   * 查询当前员工绩效列表。
   */
  listMine(includeHistory = false) {
    return apiGet<EmployeePerformance[]>('/performance/employee/records/mine', { params: { includeHistory } });
  },

  /**
   * 确认绩效。
   */
  confirm(recordId: number, data: { smsCode: string }) {
    return apiPost<void>(`/performance/employee/records/${recordId}/confirm`, data);
  },

  /**
   * 提交绩效反馈。
   */
  feedback(recordId: number, data: { feedbackContent: string }) {
    return apiPost<void>(`/performance/employee/records/${recordId}/feedback`, data);
  },
};
