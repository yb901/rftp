import { apiGet } from '../../shared/api/request';
import { apiPost } from '../../shared/api/request';
import type { EmployeePerformance, PerformanceCaptchaConfig, PerformanceLoginResult } from './types';

/**
 * 员工绩效 H5 API。
 */
export const performanceApi = {
  /**
   * 查询当前登录态。
   */
  me() {
    return apiGet<PerformanceLoginResult>('/performance/h5/auth/me');
  },

  /**
   * 查询图形验证码配置。
   */
  captchaConfig() {
    return apiGet<PerformanceCaptchaConfig>('/performance/h5/auth/captcha/config');
  },

  /**
   * 发送短信验证码。
   */
  sendSmsCode(data: { mobile: string; scene: string; captchaTraceId?: string }) {
    return apiPost<number>('/performance/h5/auth/sms/send', data);
  },

  /**
   * 手机号登录。
   */
  login(data: { mobile: string; smsCode: string }) {
    return apiPost<PerformanceLoginResult>('/performance/h5/auth/login', data);
  },

  /**
   * 查询当前员工绩效列表。
   */
  listMine() {
    return apiGet<EmployeePerformance[]>('/performance/h5/records/mine');
  },

  /**
   * 确认绩效。
   */
  confirm(recordId: number, data: { smsCode: string }) {
    return apiPost<void>(`/performance/h5/records/${recordId}/confirm`, data);
  },

  /**
   * 提交绩效反馈。
   */
  feedback(recordId: number, data: { feedbackContent: string }) {
    return apiPost<void>(`/performance/h5/records/${recordId}/feedback`, data);
  },
};
