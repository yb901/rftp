/**
 * 员工绩效摘要。
 */
export interface EmployeePerformance {
  id: number;
  performanceDescription: string;
  periodText: string;
  performance: string;
  performanceExplanation?: string;
  confirmStatus: string;
  confirmStatusText: string;
  feedbackStatus: string;
  feedbackStatusText?: string;
  confirmDeadlineTime: string;
  secondConfirmDeadlineTime?: string;
  actionDeadlineTime?: string;
  history?: boolean;
  confirmAvailable: boolean;
  feedbackAvailable: boolean;
}

/**
 * 登录结果。
 */
export interface PerformanceLoginResult {
  mobile: string;
}

/**
 * 登录前手机号绩效检查结果。
 */
export interface PerformancePendingCheckResult {
  hasPendingPerformance?: boolean;
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
