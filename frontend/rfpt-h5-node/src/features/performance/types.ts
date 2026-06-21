/**
 * 员工绩效摘要。
 */
export interface EmployeePerformance {
  id: number;
  performanceDescription: string;
  periodText: string;
  performance: string;
  confirmStatus: string;
  confirmStatusText: string;
  feedbackStatus: string;
  confirmDeadlineTime: string;
}

/**
 * 登录结果。
 */
export interface PerformanceLoginResult {
  mobile: string;
}

/**
 * 图形验证码配置。
 */
export interface PerformanceCaptchaConfig {
  enabled?: boolean;
  region?: string;
  prefix?: string;
  sceneId?: string;
  language?: string;
  jsUrl?: string;
}
