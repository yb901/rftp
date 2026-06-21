/**
 * 后端统一响应。
 */
export interface ApiResult<T> {
  code: string;
  message: string;
  data: T;
}
