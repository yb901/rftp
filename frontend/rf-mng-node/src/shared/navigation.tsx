import { BankOutlined, TeamOutlined, UserSwitchOutlined } from '@ant-design/icons';
import type { MenuProps } from 'antd';
import type { LoginUser } from '../api';

export type ModuleKey = 'social' | 'performance' | 'admin';
export type PageKey = 'socialBatches' | 'socialTasks' | 'enterprise' | 'region' | 'performance' | 'admin';

const roleModules: Record<number, ModuleKey[]> = {
  1: ['social', 'performance', 'admin'],
  2: ['social', 'performance'],
  3: ['social'],
  4: ['performance'],
};

/** 管理后台页面元信息与浏览器路径映射。 */
export function getPageMeta(pageKey: PageKey) {
  const meta: Record<PageKey, { title: string; subtitle: string }> = {
    socialBatches: { title: '社保缴费管理', subtitle: '按地区、月份发起任务，跟踪电子税务局执行结果' },
    socialTasks: { title: '社保缴费任务', subtitle: '查看税号级缴费任务状态，处理失败任务重试' },
    enterprise: { title: '企业维护', subtitle: '维护社保缴费相关企业基础信息' },
    region: { title: '地区配置', subtitle: '维护社保缴费地区与站点配置' },
    performance: { title: '员工绩效管理', subtitle: '创建绩效周期，导入员工绩效，跟踪确认反馈和调整闭环' },
    admin: { title: '系统管理员', subtitle: '管理后台用户、角色、启用状态和动态验证码' },
  };
  return meta[pageKey];
}

export function getPagePath(pageKey: PageKey) {
  const pathMap: Record<PageKey, string> = {
    socialBatches: '/social-security/batches', socialTasks: '/social-security/tasks',
    enterprise: '/social-security/enterprise', region: '/social-security/region',
    performance: '/performance', admin: '/admin',
  };
  return pathMap[pageKey];
}

export function getPageKeyByPath(pathname: string): PageKey {
  const normalizedPath = pathname.replace(/\/+$/, '') || '/';
  const pageMap: Array<{ prefix: string; key: PageKey }> = [
    { prefix: '/social-security/batches', key: 'socialBatches' }, { prefix: '/social-security/tasks', key: 'socialTasks' },
    { prefix: '/social-security/enterprise', key: 'enterprise' }, { prefix: '/social-security/region', key: 'region' },
    { prefix: '/performance', key: 'performance' }, { prefix: '/admin', key: 'admin' },
  ];
  return pageMap.find((item) => normalizedPath === item.prefix || normalizedPath.startsWith(`${item.prefix}/`))?.key || 'socialBatches';
}

export function navigateToPage(nextPageKey: PageKey, currentPageKey: PageKey, setPageKey: (pageKey: PageKey) => void) {
  if (nextPageKey === currentPageKey) return;
  window.history.pushState({}, '', getPagePath(nextPageKey));
  setPageKey(nextPageKey);
}

export function buildMenuItems(user: LoginUser | null): MenuProps['items'] {
  const items: MenuProps['items'] = [];
  if (canAccessModule(user, 'social')) {
    items.push({ key: 'social', icon: <BankOutlined />, label: '社保缴费', children: [
      { key: 'socialBatches', label: '缴费批次' }, { key: 'socialTasks', label: '任务明细' },
      { key: 'enterprise', label: '企业维护' }, { key: 'region', label: '地区配置' },
    ] });
  }
  if (canAccessModule(user, 'performance')) items.push({ key: 'performance', icon: <TeamOutlined />, label: '员工绩效' });
  if (canAccessModule(user, 'admin')) items.push({ key: 'admin', icon: <UserSwitchOutlined />, label: '系统管理员' });
  return items;
}

export function canAccessPage(user: LoginUser | null, pageKey: PageKey) { return canAccessModule(user, getPageModule(pageKey)); }
export function canAccessModule(user: LoginUser | null, moduleKey: ModuleKey) { return !!user && (roleModules[user.role] || []).includes(moduleKey); }
export function getFirstAccessiblePage(user: LoginUser | null): PageKey {
  if (canAccessModule(user, 'social')) return 'socialBatches';
  if (canAccessModule(user, 'performance')) return 'performance';
  if (canAccessModule(user, 'admin')) return 'admin';
  return 'socialBatches';
}

function getPageModule(pageKey: PageKey): ModuleKey {
  return pageKey === 'performance' ? 'performance' : pageKey === 'admin' ? 'admin' : 'social';
}
