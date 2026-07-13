import {
  HomeOutlined,
  BankOutlined,
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  EnvironmentOutlined,
  EyeOutlined,
  LoginOutlined,
  LogoutOutlined,
  PlusOutlined,
  ReloadOutlined,
  RetweetOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
  UploadOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Breadcrumb, Button, Card, DatePicker, Descriptions, Dropdown, Form, Input, Layout, Menu, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, Typography, Upload, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { MenuProps } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import companyLogo from './assets/company-logo.svg';
import { buildMenuItems, canAccessModule, canAccessPage, getFirstAccessiblePage, getPageKeyByPath, getPageMeta, navigateToPage, type PageKey } from './shared/navigation';
import {
  AdminSaveParam,
  AdminUser,
  BatchRecord,
  EmployeePerformanceImportUpload,
  EmployeePerformanceRecord,
  LoginUser,
  PerformanceTask,
  SocialSecurityEnterprise,
  SocialSecurityRegionSite,
  TaskRecord,
  adjustPerformanceRecord,
  createBatch,
  createPerformanceTask,
  deleteSocialSecurityEnterprise,
  deleteSocialSecurityRegionSite,
  deletePerformanceTask,
  deletePerformanceRecord,
  disablePerformanceTask,
  deleteAdmin,
  disableAdminTotp,
  enablePerformanceTask,
  exportPerformanceRecords,
  fetchBatches,
  fetchAdmins,
  fetchPerformanceImportUploads,
  fetchPerformanceRecords,
  fetchPerformanceTasks,
  fetchSocialSecurityEnterprises,
  fetchSocialSecurityRegionSites,
  fetchTasks,
  generateAdminTotp,
  getRequestErrorMessage,
  handlePerformanceFeedbackUnchanged,
  importPerformanceRecordsFile,
  login,
  logout,
  performanceImportFailureDownloadUrl,
  performanceImportOriginalDownloadUrl,
  retryTask,
  saveAdmin,
  saveSocialSecurityEnterprise,
  saveSocialSecurityRegionSite,
  updateAdmin,
} from './api';

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

const statusColor: Record<string, string> = {
  SUBMITTED: 'processing',
  RUNNING: 'processing',
  PROCESSING: 'processing',
  MATCHED: 'success',
  MISMATCHED: 'error',
  SKIPPED: 'default',
  SUCCESS: 'success',
  ISSUED: 'processing',
  DOWNLOADED: 'success',
  PARTIAL_SUCCESS: 'warning',
  FAILED: 'error',
  PENDING: 'default',
  CANCELED: 'default',
  PENDING_CONFIRM: 'processing',
  CONFIRMED: 'success',
  AUTO_CONFIRMED: 'warning',
  FEEDBACK_SUBMITTED: 'warning',
  PENDING_SECOND_CONFIRM: 'processing',
  SECOND_CONFIRMED: 'success',
  SECOND_AUTO_CONFIRMED: 'warning',
  NONE: 'default',
  HANDLED_ADJUSTED: 'success',
  HANDLED_UNCHANGED: 'success',
  OPEN: 'success',
  CLOSED: 'default',
  DRAFT: 'default',
  CONFIRMING: 'success',
};

const confirmStatusText: Record<string, string> = {
  PENDING_CONFIRM: '待确认',
  CONFIRMED: '已确认',
  AUTO_CONFIRMED: '超时自动确认',
  FEEDBACK_SUBMITTED: '已反馈',
  PENDING_SECOND_CONFIRM: '待二次确认',
  SECOND_CONFIRMED: '二次已确认',
  SECOND_AUTO_CONFIRMED: '二次超时自动确认',
};

const feedbackStatusText: Record<string, string> = {
  NONE: '无反馈',
  PENDING: '待处理',
  HANDLED_ADJUSTED: '已调整',
  HANDLED_UNCHANGED: '已处理未调整',
};

const performanceExportItems: MenuProps['items'] = [
  { key: 'ALL', label: '下载全部' },
  { key: 'CONFIRMED', label: '下载已确认（含二次确认）' },
  { key: 'UNCONFIRMED', label: '下载未确认（含二次确认）' },
];

const socialTaskStatusText: Record<string, string> = {
  PENDING: '待机器人领取',
  PROCESSING: '执行中',
  SUCCESS: '已成功',
  FAILED: '已失败',
  CANCELED: '已取消',
};

const performanceTaskStatusText: Record<string, string> = {
  OPEN: '开启',
  CLOSED: '关闭',
  DRAFT: '关闭',
  CONFIRMING: '开启',
};

const performanceTaskStatusOptions = [
  { value: 'OPEN', label: '开启' },
  { value: 'CLOSED', label: '关闭' },
];

const statusOptions = [
  { value: 'active', label: '启用' },
  { value: 'disabled', label: '停用' },
];

const stageStatusText: Record<string, string> = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  MATCHED: '一致',
  MISMATCHED: '不一致',
  SKIPPED: '跳过',
  SUCCESS: '成功',
  FAILED: '失败',
  ISSUED: '已开具',
  DOWNLOADED: '已下载',
};

const performanceImportTemplateUrl = 'https://static.zcglhr.com/qy-mng/upload-task/%E5%91%98%E5%B7%A5%E7%BB%A9%E6%95%88%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF-20260624.xlsx';

const adminRoleOptions = [
  { value: 1, label: '超级管理员' },
  { value: 2, label: '管理员' },
  { value: 3, label: '社保专员' },
  { value: 4, label: '绩效专员' },
];

function App() {
  const [loginUser, setLoginUser] = useState<LoginUser | null>(() => readLoginUser());
  const [loginLoading, setLoginLoading] = useState(false);
  const [pageKey, setPageKey] = useState<PageKey>(() => getPageKeyByPath(window.location.pathname));
  const [batchList, setBatchList] = useState<BatchRecord[]>([]);
  const [taskList, setTaskList] = useState<TaskRecord[]>([]);
  const [enterpriseList, setEnterpriseList] = useState<SocialSecurityEnterprise[]>([]);
  const [regionSiteList, setRegionSiteList] = useState<SocialSecurityRegionSite[]>([]);
  const [performanceTaskList, setPerformanceTaskList] = useState<PerformanceTask[]>([]);
  const [performanceList, setPerformanceList] = useState<EmployeePerformanceRecord[]>([]);
  const [performanceImportUploads, setPerformanceImportUploads] = useState<EmployeePerformanceImportUpload[]>([]);
  const [adminList, setAdminList] = useState<AdminUser[]>([]);
  const [batchLoading, setBatchLoading] = useState(false);
  const [taskLoading, setTaskLoading] = useState(false);
  const [enterpriseLoading, setEnterpriseLoading] = useState(false);
  const [regionSiteLoading, setRegionSiteLoading] = useState(false);
  const [performanceTaskLoading, setPerformanceTaskLoading] = useState(false);
  const [performanceLoading, setPerformanceLoading] = useState(false);
  const [importUploadLoading, setImportUploadLoading] = useState(false);
  const [adminLoading, setAdminLoading] = useState(false);
  const [performanceTaskPage, setPerformanceTaskPage] = useState({ page: 1, size: 10, total: 0 });
  const [performanceRecordPage, setPerformanceRecordPage] = useState({ page: 1, size: 50, total: 0 });
  const [adminPage, setAdminPage] = useState({ page: 1, size: 10, total: 0 });
  const [enterprisePage, setEnterprisePage] = useState({ page: 1, size: 10, total: 0 });
  const [regionSitePage, setRegionSitePage] = useState({ page: 1, size: 10, total: 0 });
  const [createOpen, setCreateOpen] = useState(false);
  const [enterpriseOpen, setEnterpriseOpen] = useState(false);
  const [regionSiteOpen, setRegionSiteOpen] = useState(false);
  const [performanceTaskOpen, setPerformanceTaskOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  const [importUploading, setImportUploading] = useState(false);
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [feedbackOpen, setFeedbackOpen] = useState(false);
  const [socialTaskDetailOpen, setSocialTaskDetailOpen] = useState(false);
  const [performanceTabKey, setPerformanceTabKey] = useState('performanceTasks');
  const [adminOpen, setAdminOpen] = useState(false);
  const [adminTotpOpen, setAdminTotpOpen] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<EmployeePerformanceRecord | null>(null);
  const [currentSocialTask, setCurrentSocialTask] = useState<TaskRecord | null>(null);
  const [editingAdmin, setEditingAdmin] = useState<AdminUser | null>(null);
  const [editingEnterprise, setEditingEnterprise] = useState<SocialSecurityEnterprise | null>(null);
  const [editingRegionSite, setEditingRegionSite] = useState<SocialSecurityRegionSite | null>(null);
  const [adminTotp, setAdminTotp] = useState<{ secret: string; qrCodeUri: string; username: string } | null>(null);
  const [form] = Form.useForm();
  const [enterpriseForm] = Form.useForm();
  const [regionSiteForm] = Form.useForm();
  const [performanceTaskForm] = Form.useForm();
  const [importForm] = Form.useForm();
  const [adjustForm] = Form.useForm();
  const [adminForm] = Form.useForm<AdminSaveParam>();
  const [loginForm] = Form.useForm();
  const [performanceQueryForm] = Form.useForm();
  const [performanceTaskQueryForm] = Form.useForm();
  const [adminQueryForm] = Form.useForm();
  const [enterpriseQueryForm] = Form.useForm();
  const [regionSiteQueryForm] = Form.useForm();
  const confirmDeadlineTime = Form.useWatch('confirmDeadlineTime', performanceTaskForm);
  const importTaskId = Form.useWatch('taskId', importForm);
  const secondConfirmDeadlineText = useMemo(() => {
    if (!confirmDeadlineTime) {
      return '确认截止后 3 天';
    }
    return dayjs(confirmDeadlineTime).endOf('day').add(3, 'day').format('YYYY-MM-DD HH:mm:ss');
  }, [confirmDeadlineTime]);
  const importTaskText = useMemo(() => {
    if (!importTaskId) {
      return '未选择绩效任务';
    }
    const task = performanceTaskList.find((item) => item.id === Number(importTaskId));
    if (!task) {
      return String(importTaskId);
    }
    return `${task.performanceDescription || '未命名任务'}（${formatPeriodRange(task.periodStartDate, task.periodEndDate)}）`;
  }, [importTaskId, performanceTaskList]);

  const loadBatches = async () => {
    setBatchLoading(true);
    try {
      const data = await fetchBatches({ page: 1, size: 20 });
      setBatchList(data.list || []);
    } finally {
      setBatchLoading(false);
    }
  };

  const loadTasks = async () => {
    setTaskLoading(true);
    try {
      const data = await fetchTasks({ page: 1, size: 50 });
      setTaskList(data.list || []);
    } finally {
      setTaskLoading(false);
    }
  };

  const loadEnterprises = async (page = enterprisePage.page, size = enterprisePage.size) => {
    setEnterpriseLoading(true);
    try {
      const values = enterpriseQueryForm.getFieldsValue();
      const data = await fetchSocialSecurityEnterprises({ page, size, ...trimObject(values) });
      setEnterpriseList(data.list || []);
      setEnterprisePage({
        page: data.pagination?.page || page,
        size: data.pagination?.size || size,
        total: data.pagination?.total || 0,
      });
    } finally {
      setEnterpriseLoading(false);
    }
  };

  const loadRegionSites = async (page = regionSitePage.page, size = regionSitePage.size) => {
    setRegionSiteLoading(true);
    try {
      const values = regionSiteQueryForm.getFieldsValue();
      const data = await fetchSocialSecurityRegionSites({ page, size, ...trimObject(values) });
      setRegionSiteList(data.list || []);
      setRegionSitePage({
        page: data.pagination?.page || page,
        size: data.pagination?.size || size,
        total: data.pagination?.total || 0,
      });
    } finally {
      setRegionSiteLoading(false);
    }
  };

  const loadPerformanceTasks = async (page = performanceTaskPage.page, size = performanceTaskPage.size) => {
    setPerformanceTaskLoading(true);
    try {
      const values = performanceTaskQueryForm.getFieldsValue();
      const data = await fetchPerformanceTasks({ page, size, ...trimObject(values) });
      setPerformanceTaskList(data.list || []);
      setPerformanceTaskPage({
        page: data.pagination?.page || page,
        size: data.pagination?.size || size,
        total: data.pagination?.total || 0,
      });
    } finally {
      setPerformanceTaskLoading(false);
    }
  };

  const loadPerformanceRecords = async (page = performanceRecordPage.page, size = performanceRecordPage.size, overrideValues: Record<string, unknown> = {}) => {
    setPerformanceLoading(true);
    try {
      const values = performanceQueryForm.getFieldsValue();
      const data = await fetchPerformanceRecords({ page, size, ...trimObject(values), ...trimObject(overrideValues) });
      setPerformanceList(data.list || []);
      setPerformanceRecordPage({
        page: data.pagination?.page || page,
        size: data.pagination?.size || size,
        total: data.pagination?.total || 0,
      });
    } finally {
      setPerformanceLoading(false);
    }
  };

  const loadPerformanceImportUploads = async (taskId?: number) => {
    setImportUploadLoading(true);
    try {
      const data = await fetchPerformanceImportUploads({ taskId, limit: 50 });
      setPerformanceImportUploads(data || []);
    } finally {
      setImportUploadLoading(false);
    }
  };

  const loadAdmins = async (page = adminPage.page, size = adminPage.size) => {
    setAdminLoading(true);
    try {
      const values = adminQueryForm.getFieldsValue();
      const data = await fetchAdmins({ page, size, ...trimObject(values) });
      setAdminList(data.list || []);
      setAdminPage({
        page: data.pagination?.page || page,
        size: data.pagination?.size || size,
        total: data.pagination?.total || 0,
      });
    } finally {
      setAdminLoading(false);
    }
  };

  useEffect(() => {
    const handleUnauthorized = () => setLoginUser(null);
    window.addEventListener('rf_mng_unauthorized', handleUnauthorized);
    return () => window.removeEventListener('rf_mng_unauthorized', handleUnauthorized);
  }, []);

  useEffect(() => {
    const handlePopState = () => {
      setPageKey(getPageKeyByPath(window.location.pathname));
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  useEffect(() => {
    if (!loginUser) {
      return;
    }
    if (canAccessModule(loginUser, 'social')) {
      void loadBatches();
      void loadTasks();
      void loadEnterprises(1, 10);
      void loadRegionSites(1, 10);
    }
    if (canAccessModule(loginUser, 'performance')) {
      void loadPerformanceTasks(1, 10);
      void loadPerformanceRecords();
    }
    if (canAccessModule(loginUser, 'admin')) {
      void loadAdmins(1, 10);
    }
  }, [loginUser]);

  useEffect(() => {
    if (!loginUser || canAccessPage(loginUser, pageKey)) {
      return;
    }
    navigateToPage(getFirstAccessiblePage(loginUser), pageKey, setPageKey);
  }, [loginUser, pageKey]);

  useEffect(() => {
    if (!loginUser || !canAccessModule(loginUser, 'performance')) {
      return;
    }
    if (!importOpen) {
      return;
    }
    void loadPerformanceImportUploads(importTaskId ? Number(importTaskId) : undefined);
  }, [importOpen, importTaskId]);

  const batchColumns: ColumnsType<BatchRecord> = useMemo(() => [
    { title: '批次', dataIndex: 'id', width: 90 },
    { title: '地区', dataIndex: 'regionCode', width: 120 },
    { title: '月份', dataIndex: 'periodMonth', width: 120 },
    { title: '状态', dataIndex: 'status', width: 130, render: (value) => <Tag color={statusColor[value] || 'default'}>{value}</Tag> },
    { title: '任务', width: 170, render: (_, row) => `${row.successCount || 0}/${row.totalCount || 0} 成功，${row.failedCount || 0} 失败` },
    { title: '创建时间', dataIndex: 'gmtCreate', width: 190, render: (value) => formatDateTime(value) },
  ], []);

  const taskColumns: ColumnsType<TaskRecord> = useMemo(() => [
    { title: '任务', dataIndex: 'id', width: 90 },
    { title: '税号', dataIndex: 'taxNo', width: 190 },
    { title: '企业', dataIndex: 'enterpriseName', width: 240, ellipsis: true },
    { title: '社保账号', dataIndex: 'securityAccountName', width: 160 },
    { title: '地区', dataIndex: 'regionCode', width: 110 },
    { title: '月份', dataIndex: 'periodMonth', width: 110 },
    { title: '状态', dataIndex: 'status', width: 140, render: (value) => <Tag color={statusColor[value] || 'default'}>{socialTaskStatusText[value] || value}</Tag> },
    { title: '比对', dataIndex: 'compareStatus', width: 100, render: renderStageTag },
    { title: '缴费', dataIndex: 'paymentStatus', width: 100, render: renderStageTag },
    { title: '凭证', dataIndex: 'certificateStatus', width: 100, render: renderStageTag },
    { title: 'BMS', dataIndex: 'bmsFeedbackStatus', width: 100, render: renderStageTag },
    { title: '机器人', dataIndex: 'workerId', width: 120, render: (value) => value || '-' },
    { title: '金额', width: 160, render: (_, row) => `${row.wpmTotalAmount ?? '-'} / ${row.payableAmount ?? '-'}` },
    { title: '失败原因', dataIndex: 'errorMessage', ellipsis: true },
    {
      title: '操作', width: 170, fixed: 'right', render: (_, row) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => openSocialTaskDetail(row)}>详情</Button>
          <Button
            size="small"
            icon={<RetweetOutlined />}
            disabled={!row.retryable}
            onClick={async () => {
              await retryTask(row.id);
              message.success('已发起重试');
              await loadTasks();
            }}
          >重试</Button>
        </Space>
      ),
    },
  ], []);

  const enterpriseColumns: ColumnsType<SocialSecurityEnterprise> = useMemo(() => [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '税号', dataIndex: 'taxNo', width: 190 },
    { title: '企业名称', dataIndex: 'enterpriseName', width: 240, ellipsis: true },
    { title: '地区', dataIndex: 'regionCode', width: 110 },
    { title: '社保账号', dataIndex: 'securityAccountName', width: 200, ellipsis: true, render: (value) => value || '-' },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => <Tag color={value === 'active' ? 'success' : 'default'}>{value === 'active' ? '启用' : '停用'}</Tag> },
    { title: '备注', dataIndex: 'remark', ellipsis: true, render: (value) => value || '-' },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEnterpriseEdit(row)}>编辑</Button>
          <Popconfirm title="确定删除该企业？" onConfirm={() => removeEnterprise(row)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ], [enterprisePage.page, enterprisePage.size]);

  const regionSiteColumns: ColumnsType<SocialSecurityRegionSite> = useMemo(() => [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '地区', dataIndex: 'regionCode', width: 110 },
    { title: '站点类型', dataIndex: 'siteType', width: 120 },
    { title: '入口地址', dataIndex: 'etaxEntryUrl', width: 260, ellipsis: true },
    { title: '登录按钮', dataIndex: 'loginButtonText', width: 110, render: (value) => value || '登录' },
    { title: '状态', dataIndex: 'status', width: 100, render: (value) => <Tag color={value === 'active' ? 'success' : 'default'}>{value === 'active' ? '启用' : '停用'}</Tag> },
    { title: '备注', dataIndex: 'remark', ellipsis: true, render: (value) => value || '-' },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openRegionSiteEdit(row)}>编辑</Button>
          <Popconfirm title="确定删除该地区站点？" onConfirm={() => removeRegionSite(row)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ], [regionSitePage.page, regionSitePage.size]);

  const performanceColumns: ColumnsType<EmployeePerformanceRecord> = useMemo(() => [
    { title: '绩效任务', dataIndex: 'performanceDescription', width: 160, ellipsis: true },
    { title: '姓名', dataIndex: 'employeeName', width: 100 },
    { title: '手机号', dataIndex: 'mobile', width: 130 },
    { title: '工号', dataIndex: 'employeeNo', width: 110 },
    { title: '项目/部门', dataIndex: 'projectDepartment', width: 150, ellipsis: true },
    { title: '岗位', dataIndex: 'positionName', width: 130, ellipsis: true },
    { title: '绩效', dataIndex: 'performance', width: 120 },
    { title: '绩效说明', dataIndex: 'performanceExplanation', width: 180, ellipsis: true, render: (value) => value || '-' },
    {
      title: '确认',
      dataIndex: 'confirmStatus',
      width: 150,
      render: (value) => <Tag color={statusColor[value] || 'default'}>{confirmStatusText[value] || value}</Tag>,
    },
    {
      title: '反馈',
      dataIndex: 'feedbackStatus',
      width: 120,
      render: (value) => <Tag color={statusColor[value] || 'default'}>{feedbackStatusText[value] || value}</Tag>,
    },
    {
      title: '操作',
      width: 320,
      fixed: 'right',
      render: (_, row) => (
        <Space className="table-action-group" wrap>
          <Button size="small" icon={<EyeOutlined />} disabled={!row.feedbackContent} onClick={() => openFeedback(row)}>反馈</Button>
          <Button size="small" icon={<EditOutlined />} disabled={!canAdjustPerformance(row)} onClick={() => openAdjust(row)}>调整</Button>
          <Button size="small" disabled={!canAdjustPerformance(row)} onClick={() => closeFeedbackUnchanged(row)}>无需调整</Button>
          <Popconfirm
            title="确定删除该员工绩效记录？"
            description="删除后该员工绩效记录及相关反馈、确认留痕将被清除。"
            onConfirm={() => removePerformanceRecord(row)}
          >
            <Button size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ], [performanceRecordPage.page, performanceRecordPage.size, performanceTaskPage.page, performanceTaskPage.size]);

  const performanceTaskColumns: ColumnsType<PerformanceTask> = useMemo(() => [
    { title: '绩效描述', dataIndex: 'performanceDescription', width: 220, ellipsis: true },
    { title: '绩效周期', width: 230, render: (_, row) => formatPeriodRange(row.periodStartDate, row.periodEndDate) },
    { title: '确认截止', dataIndex: 'confirmDeadlineTime', width: 180, render: (value) => formatDateTime(value) },
    {
      title: '状态',
      dataIndex: 'statusCode',
      width: 110,
      render: (value) => <Tag color={statusColor[value] || 'default'}>{performanceTaskStatusText[value || 'CLOSED'] || value || '关闭'}</Tag>,
    },
    { title: '人数', width: 170, render: (_, row) => `${row.confirmedCount || 0}/${row.totalCount || 0} 确认，${row.feedbackCount || 0} 反馈` },
    {
      title: '操作',
      width: 330,
      fixed: 'right',
      render: (_, row) => {
        const deleteBlockedReason = getPerformanceTaskDeleteBlockedReason(row);
        return (
          <Space className="table-action-group" wrap>
            <Popconfirm
              title={isPerformanceTaskOpen(row.statusCode) ? '确定停用该绩效任务？' : '确定启用该绩效任务？'}
              description={isPerformanceTaskOpen(row.statusCode) ? '停用后员工端将不可查看该任务。' : '启用后员工端可查看并确认该任务。'}
              onConfirm={() => void togglePerformanceTaskEnabled(row)}
            >
              <Button size="small" type={isPerformanceTaskOpen(row.statusCode) ? 'default' : 'primary'}>
                {isPerformanceTaskOpen(row.statusCode) ? '停用' : '启用'}
              </Button>
            </Popconfirm>
            <Button
              size="small"
              type="primary"
              ghost
              icon={<EyeOutlined />}
              onClick={() => {
                performanceQueryForm.setFieldValue('taskId', row.id);
                setPerformanceTabKey('performanceRecords');
                void loadPerformanceRecords(1, performanceRecordPage.size, { taskId: row.id });
              }}
            >
              员工记录
            </Button>
            <Button
              size="small"
              icon={<UploadOutlined />}
              onClick={() => {
                importForm.setFieldValue('taskId', row.id);
                setImportOpen(true);
              }}
            >
              导入
            </Button>
            <Button
              size="small"
              danger
              icon={<DeleteOutlined />}
              title={deleteBlockedReason || '删除绩效任务'}
              onClick={() => requestDeletePerformanceTask(row, deleteBlockedReason)}
            >
              删除
            </Button>
          </Space>
        );
      },
    },
  ], [importForm, performanceQueryForm, performanceRecordPage.size, performanceTaskPage.page, performanceTaskPage.size]);

  const performanceImportColumns: ColumnsType<EmployeePerformanceImportUpload> = useMemo(() => [
    { title: '上传时间', dataIndex: 'gmtCreate', width: 170, render: (value) => formatDateTime(value) },
    { title: '原始文件', dataIndex: 'fileName', width: 220, ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      render: (value) => <Tag color={statusColor[value] || 'default'}>{value === 'SUCCESS' ? '成功' : value === 'FAILED' ? '失败' : '部分成功'}</Tag>,
    },
    { title: '总数', dataIndex: 'totalCount', width: 80 },
    { title: '成功', dataIndex: 'successCount', width: 90 },
    { title: '失败', dataIndex: 'failCount', width: 90 },
    { title: '上传人', dataIndex: 'createAdminName', width: 120, render: (value) => value || '-' },
    { title: '失败原因', dataIndex: 'errorMessage', width: 180, ellipsis: true, render: (value) => value || '-' },
    {
      title: '文件',
      width: 210,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" disabled={!row.hasOriginalFile} onClick={() => downloadUrl(performanceImportOriginalDownloadUrl(row.id), row.fileName)}>原始文件</Button>
          <Button size="small" disabled={!row.hasFailureFile} onClick={() => downloadUrl(performanceImportFailureDownloadUrl(row.id), row.failureFileName || `${row.fileName}-失败明细.xls`)}>
            失败Excel
          </Button>
        </Space>
      ),
    },
  ], []);

  const adminColumns: ColumnsType<AdminUser> = useMemo(() => [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '用户名', dataIndex: 'username', width: 150 },
    { title: '姓名', dataIndex: 'realName', width: 140 },
    {
      title: '角色',
      dataIndex: 'role',
      width: 130,
      render: (value: number) => adminRoleOptions.find((item) => item.value === value)?.label || value,
    },
    {
      title: '启用',
      dataIndex: 'enabled',
      width: 100,
      render: (value: number) => <Tag color={value === 1 ? 'success' : 'default'}>{value === 1 ? '是' : '否'}</Tag>,
    },
    {
      title: 'TOTP',
      dataIndex: 'totpEnabled',
      width: 110,
      render: (value: boolean) => <Tag color={value ? 'success' : 'default'}>{value ? '已启用' : '未启用'}</Tag>,
    },
    { title: '创建时间', dataIndex: 'gmtCreate', width: 190, render: (value) => formatDateTime(value) },
    {
      title: '操作',
      width: 300,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" onClick={() => openAdminEdit(row)}>编辑</Button>
          <Button size="small" type={row.enabled === 1 ? 'default' : 'primary'} danger={row.enabled === 1} onClick={() => toggleAdminEnabled(row)}>
            {row.enabled === 1 ? '禁用' : '启用'}
          </Button>
          {row.totpEnabled ? (
            <Button size="small" danger onClick={() => disableTotp(row)}>禁用TOTP</Button>
          ) : (
            <Button size="small" onClick={() => generateTotp(row)}>启用TOTP</Button>
          )}
          <Popconfirm title="确定删除该管理员？" onConfirm={() => removeAdmin(row)}>
            <Button size="small" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ], [adminPage.page, adminPage.size]);

  const submitCreate = async () => {
    const values = await form.validateFields();
    await createBatch({
      regionCode: values.regionCode,
      siteType: 'default',
      periodMonth: values.periodMonth,
      taxNoList: (values.taxNoText || '').split(/\s|,|，/).map((item: string) => item.trim()).filter(Boolean),
      createAdminId: loginUser?.id,
      createAdminName: values.createAdminName || currentAdminName(loginUser),
    });
    message.success('批次已提交');
    setCreateOpen(false);
    form.resetFields();
    await Promise.all([loadBatches(), loadTasks()]);
  };

  const submitPerformanceTask = async () => {
    const values = await performanceTaskForm.validateFields();
    const task = await createPerformanceTask({
      performanceDescription: values.performanceDescription,
      periodStartDate: values.periodRange[0].format('YYYY-MM-DD'),
      periodEndDate: values.periodRange[1].format('YYYY-MM-DD'),
      confirmDeadlineTime: values.confirmDeadlineTime.endOf('day').format('YYYY-MM-DDTHH:mm:ss'),
      createAdminId: loginUser?.id,
      createAdminName: currentAdminName(loginUser),
    });
    message.success(`绩效任务已创建：${task.id}`);
    setPerformanceTaskOpen(false);
    performanceTaskForm.resetFields();
    performanceQueryForm.setFieldValue('taskId', task.id);
    await Promise.all([loadPerformanceTasks(1, performanceTaskPage.size), loadPerformanceRecords(1, performanceRecordPage.size)]);
  };

  const removePerformanceTask = async (task: PerformanceTask) => {
    try {
      await deletePerformanceTask(task.id);
      message.success('绩效任务已删除');
      if (performanceQueryForm.getFieldValue('taskId') === task.id) {
        performanceQueryForm.setFieldValue('taskId', undefined);
      }
      await Promise.all([loadPerformanceTasks(1, performanceTaskPage.size), loadPerformanceRecords(1, performanceRecordPage.size)]);
    } catch (error) {
      message.error(getRequestErrorMessage(error, '绩效任务删除失败'));
    }
  };

  const requestDeletePerformanceTask = (task: PerformanceTask, blockedReason = getPerformanceTaskDeleteBlockedReason(task)) => {
    if (blockedReason) {
      message.warning(blockedReason);
      return;
    }
    Modal.confirm({
      title: '确定删除该绩效任务？',
      content: '删除后任务列表将不再展示该绩效任务。',
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: () => removePerformanceTask(task),
    });
  };

  const togglePerformanceTaskEnabled = async (task: PerformanceTask) => {
    if (isPerformanceTaskOpen(task.statusCode)) {
      await disablePerformanceTask(task.id);
      message.success('绩效任务已停用');
    } else {
      await enablePerformanceTask(task.id);
      message.success('绩效任务已启用');
    }
    await loadPerformanceTasks(performanceTaskPage.page, performanceTaskPage.size);
  };

  const uploadPerformanceImportFile = async (file: File) => {
    const values = await importForm.validateFields(['taskId']);
    const taskId = Number(values.taskId);
    const task = performanceTaskList.find((item) => item.id === taskId);
    const taskName = task ? `${task.performanceDescription || '未命名任务'}（${formatPeriodRange(task.periodStartDate, task.periodEndDate)}）` : String(taskId);
    setImportUploading(true);
    try {
      const result = await importPerformanceRecordsFile(taskId, file, taskName);
      const failCount = result.failCount || 0;
      const successCount = result.successCount || 0;
      if (failCount > 0) {
        message.warning(`导入完成，成功 ${successCount} 条，失败 ${failCount} 条`);
      } else {
        message.success(`导入成功 ${successCount} 条`);
      }
      performanceQueryForm.setFieldValue('taskId', taskId);
      await Promise.all([
        loadPerformanceTasks(performanceTaskPage.page, performanceTaskPage.size),
        loadPerformanceRecords(1, performanceRecordPage.size),
        loadPerformanceImportUploads(taskId),
      ]);
    } catch (error) {
      message.error(getRequestErrorMessage(error, '导入失败'));
      await loadPerformanceImportUploads(taskId);
    } finally {
      setImportUploading(false);
    }
  };

  const openFeedback = (record: EmployeePerformanceRecord) => {
    setCurrentRecord(record);
    setFeedbackOpen(true);
  };

  const openSocialTaskDetail = (task: TaskRecord) => {
    setCurrentSocialTask(task);
    setSocialTaskDetailOpen(true);
  };

  const openAdjust = (record: EmployeePerformanceRecord) => {
    setCurrentRecord(record);
    adjustForm.setFieldsValue({ afterPerformance: record.performance });
    setAdjustOpen(true);
  };

  const removePerformanceRecord = async (record: EmployeePerformanceRecord) => {
    try {
      await deletePerformanceRecord(record.id);
      message.success('员工绩效记录已删除');
      await Promise.all([
        loadPerformanceRecords(performanceRecordPage.page, performanceRecordPage.size),
        loadPerformanceTasks(performanceTaskPage.page, performanceTaskPage.size),
      ]);
    } catch (error) {
      message.error(getRequestErrorMessage(error, '员工绩效记录删除失败'));
    }
  };

  const submitAdjust = async () => {
    if (!currentRecord) {
      return;
    }
    const values = await adjustForm.validateFields();
    await adjustPerformanceRecord(currentRecord.id, values);
    message.success('绩效已调整，员工需二次确认');
    setAdjustOpen(false);
    adjustForm.resetFields();
    setCurrentRecord(null);
    await loadPerformanceRecords(performanceRecordPage.page, performanceRecordPage.size);
  };

  const closeFeedbackUnchanged = (record: EmployeePerformanceRecord) => {
    let handleOpinion = '';
    Modal.confirm({
      title: '确认无需调整？',
      content: (
        <Input.TextArea
          rows={4}
          placeholder="请输入处理意见"
          onChange={(event) => {
            handleOpinion = event.target.value;
          }}
        />
      ),
      okText: '确认处理',
      cancelText: '取消',
      onOk: async () => {
        if (!handleOpinion.trim()) {
          message.error('请输入处理意见');
          throw new Error('请输入处理意见');
        }
        await handlePerformanceFeedbackUnchanged(record.id, {
          handleOpinion,
          operatorAdminId: loginUser?.id,
          operatorAdminName: currentAdminName(loginUser),
        });
        message.success('反馈已处理');
        await loadPerformanceRecords(performanceRecordPage.page, performanceRecordPage.size);
      },
    });
  };

  const openEnterpriseCreate = () => {
    setEditingEnterprise(null);
    enterpriseForm.resetFields();
    enterpriseForm.setFieldsValue({ regionCode: 'liaoning', status: 'active' });
    setEnterpriseOpen(true);
  };

  const openEnterpriseEdit = (enterprise: SocialSecurityEnterprise) => {
    setEditingEnterprise(enterprise);
    enterpriseForm.setFieldsValue(enterprise);
    setEnterpriseOpen(true);
  };

  const submitEnterprise = async () => {
    const values = await enterpriseForm.validateFields();
    await saveSocialSecurityEnterprise({ ...values, id: editingEnterprise?.id });
    message.success(editingEnterprise ? '企业已更新' : '企业已新增');
    setEnterpriseOpen(false);
    enterpriseForm.resetFields();
    await loadEnterprises(enterprisePage.page, enterprisePage.size);
  };

  const removeEnterprise = async (enterprise: SocialSecurityEnterprise) => {
    await deleteSocialSecurityEnterprise(enterprise.id);
    message.success('企业已删除');
    await loadEnterprises(enterprisePage.page, enterprisePage.size);
  };

  const openRegionSiteCreate = () => {
    setEditingRegionSite(null);
    regionSiteForm.resetFields();
    regionSiteForm.setFieldsValue({ regionCode: 'liaoning', siteType: 'default', loginButtonText: '登录', status: 'active' });
    setRegionSiteOpen(true);
  };

  const openRegionSiteEdit = (regionSite: SocialSecurityRegionSite) => {
    setEditingRegionSite(regionSite);
    regionSiteForm.setFieldsValue(regionSite);
    setRegionSiteOpen(true);
  };

  const submitRegionSite = async () => {
    const values = await regionSiteForm.validateFields();
    await saveSocialSecurityRegionSite({ ...values, id: editingRegionSite?.id });
    message.success(editingRegionSite ? '地区站点已更新' : '地区站点已新增');
    setRegionSiteOpen(false);
    regionSiteForm.resetFields();
    await loadRegionSites(regionSitePage.page, regionSitePage.size);
  };

  const removeRegionSite = async (regionSite: SocialSecurityRegionSite) => {
    await deleteSocialSecurityRegionSite(regionSite.id);
    message.success('地区站点已删除');
    await loadRegionSites(regionSitePage.page, regionSitePage.size);
  };

  const openAdminCreate = () => {
    setEditingAdmin(null);
    adminForm.resetFields();
    adminForm.setFieldsValue({ role: 2 });
    setAdminOpen(true);
  };

  const openAdminEdit = (admin: AdminUser) => {
    setEditingAdmin(admin);
    adminForm.setFieldsValue({ username: admin.username, realName: admin.realName, role: admin.role, password: '' });
    setAdminOpen(true);
  };

  const submitAdmin = async () => {
    const values = await adminForm.validateFields();
    if (editingAdmin) {
      await updateAdmin({ ...values, id: editingAdmin.id, username: editingAdmin.username, enabled: editingAdmin.enabled });
      message.success('管理员已更新');
    } else {
      await saveAdmin({ ...values, enabled: 1 });
      message.success('管理员已新增');
    }
    setAdminOpen(false);
    adminForm.resetFields();
    await loadAdmins(adminPage.page, adminPage.size);
  };

  const toggleAdminEnabled = async (admin: AdminUser) => {
    await updateAdmin({ ...admin, enabled: admin.enabled === 1 ? 0 : 1 });
    message.success(admin.enabled === 1 ? '管理员已禁用' : '管理员已启用');
    await loadAdmins(adminPage.page, adminPage.size);
  };

  const removeAdmin = async (admin: AdminUser) => {
    await deleteAdmin(admin.id);
    message.success('管理员已删除');
    await loadAdmins(adminPage.page, adminPage.size);
  };

  const generateTotp = async (admin: AdminUser) => {
    const result = await generateAdminTotp(admin.id);
    setAdminTotp(result);
    setAdminTotpOpen(true);
    await loadAdmins(adminPage.page, adminPage.size);
  };

  const disableTotp = async (admin: AdminUser) => {
    await disableAdminTotp(admin.id);
    message.success('TOTP 已禁用');
    await loadAdmins(adminPage.page, adminPage.size);
  };

  const downloadPerformanceExport = async (exportConfirmScope = 'ALL') => {
    const values = performanceQueryForm.getFieldsValue();
    const exportParams = trimObject(values);
    delete exportParams.confirmStatus;
    const response = await exportPerformanceRecords({ ...exportParams, exportConfirmScope });
    const url = window.URL.createObjectURL(response.data);
    const link = document.createElement('a');
    link.href = url;
    link.download = `员工绩效记录-${dayjs().format('YYYYMMDDHHmmss')}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
  };

  const refreshCurrentModule = () => {
    if (pageKey === 'performance') {
      return Promise.all([
        loadPerformanceTasks(performanceTaskPage.page, performanceTaskPage.size),
        loadPerformanceRecords(performanceRecordPage.page, performanceRecordPage.size),
      ]);
    }
    if (pageKey === 'admin') {
      return loadAdmins(adminPage.page, adminPage.size);
    }
    if (pageKey === 'enterprise' || pageKey === 'region') {
      return pageKey === 'enterprise'
        ? loadEnterprises(enterprisePage.page, enterprisePage.size)
        : loadRegionSites(regionSitePage.page, regionSitePage.size);
    }
    return Promise.all([loadBatches(), loadTasks()]);
  };

  const submitLogin = async () => {
    const values = await loginForm.validateFields();
    setLoginLoading(true);
    try {
      const result = await login(values);
      setLoginUser(result.user);
      window.localStorage.setItem('rf_mng_login_user', JSON.stringify(result.user));
      const landingPageKey = canAccessPage(result.user, pageKey) ? pageKey : getFirstAccessiblePage(result.user);
      navigateToPage(landingPageKey, pageKey, setPageKey);
      message.success('登录成功');
    } catch (error) {
      message.error(getRequestErrorMessage(error, '登录失败，请检查账号、密码或动态验证码'));
    } finally {
      setLoginLoading(false);
    }
  };

  const submitLogout = async () => {
    await logout();
    window.localStorage.removeItem('rf_mng_login_user');
    setLoginUser(null);
    message.success('已退出登录');
  };

  const pageMeta = getPageMeta(pageKey);
  const menuItems = buildMenuItems(loginUser);

  if (!loginUser) {
    return (
      <div className="login-shell">
        <div className="login-panel">
          <div className="login-brand">
            <img src={companyLogo} alt="中工经联" />
            <div>
              <div className="login-title">中工经联</div>
              <div className="login-subtitle">管理后台登录</div>
            </div>
          </div>
          <Form layout="vertical" form={loginForm} onFinish={submitLogin}>
            <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input prefix={<UserOutlined />} autoComplete="username" />
            </Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password autoComplete="current-password" />
            </Form.Item>
            <Form.Item name="otpCode" label="动态验证码" rules={[{ required: true, message: '请输入动态验证码' }]}>
              <Input prefix={<SafetyCertificateOutlined />} autoComplete="one-time-code" inputMode="numeric" maxLength={6} />
            </Form.Item>
            <Button type="primary" htmlType="submit" icon={<LoginOutlined />} loading={loginLoading} block>
              登录
            </Button>
          </Form>
        </div>
      </div>
    );
  }

  return (
    <Layout className="app-shell">
      <Sider width={220} className="app-sider">
        <div className="brand">
          <img src={companyLogo} alt="中工经联" />
          <span>中工经联</span>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          items={menuItems}
          selectedKeys={[pageKey]}
          defaultOpenKeys={canAccessModule(loginUser, 'social') ? ['social'] : []}
          onClick={({ key }) => navigateToPage(key as PageKey, pageKey, setPageKey)}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Button type="text" className="header-trigger" icon={<BankOutlined />} />
          <Space size={12}>
            <Avatar size={34} icon={<UserOutlined />} />
            <Text strong>{currentAdminName(loginUser)}</Text>
            <Button icon={<LogoutOutlined />} onClick={submitLogout}>退出登录</Button>
          </Space>
        </Header>
        <Content className="app-content">
          <Breadcrumb
            items={[
              { title: <><HomeOutlined /> 首页</> },
              { title: pageMeta.title },
            ]}
            className="page-breadcrumb"
          />
          {pageKey === 'performance' ? (
            <div className="page-panel performance-panel">
              <Card size="small">
                <Tabs
                  activeKey={performanceTabKey}
                  onChange={setPerformanceTabKey}
                  items={[
                    {
                      key: 'performanceTasks',
                      label: '绩效任务',
                      children: (
                        <Space direction="vertical" size={12} className="full-width">
                          <div className="toolbar-row">
                            <Text strong>任务列表</Text>
                            <Space wrap>
                              <Button icon={<ReloadOutlined />} onClick={() => loadPerformanceTasks(1, performanceTaskPage.size)}>刷新</Button>
                              <Button type="primary" icon={<PlusOutlined />} onClick={() => setPerformanceTaskOpen(true)}>创建绩效</Button>
                            </Space>
                          </div>
                          <Form layout="inline" form={performanceTaskQueryForm} className="query-bar">
                            <Form.Item name="performanceDescription" label="绩效描述"><Input placeholder="绩效描述" allowClear /></Form.Item>
                            <Form.Item name="status" label="状态">
                              <Select
                                allowClear
                                placeholder="全部状态"
                                style={{ width: 140 }}
                                options={performanceTaskStatusOptions}
                              />
                            </Form.Item>
                            <Button type="primary" onClick={() => loadPerformanceTasks(1, performanceTaskPage.size)}>查询</Button>
                          </Form>
                          <Table
                            rowKey="id"
                            className="performance-task-table"
                            loading={performanceTaskLoading}
                            columns={performanceTaskColumns}
                            dataSource={performanceTaskList}
                            size="small"
                            scroll={{ x: 1100 }}
                            pagination={{
                              current: performanceTaskPage.page,
                              pageSize: performanceTaskPage.size,
                              total: performanceTaskPage.total,
                              onChange: loadPerformanceTasks,
                            }}
                          />
                        </Space>
                      ),
                    },
                    {
                      key: 'performanceRecords',
                      label: '员工记录',
                      children: (
                        <Space direction="vertical" size={12} className="full-width">
                          <div className="toolbar-row">
                            <Text strong>员工记录</Text>
                            <Space wrap>
                              <Button icon={<ReloadOutlined />} onClick={() => loadPerformanceRecords(1, performanceRecordPage.size)}>刷新</Button>
                              <Dropdown
                                menu={{ items: performanceExportItems, onClick: ({ key }) => downloadPerformanceExport(key) }}
                              >
                                <Button icon={<DownloadOutlined />}>导出</Button>
                              </Dropdown>
                            </Space>
                          </div>
                          <Form layout="inline" form={performanceQueryForm} className="query-bar">
                            <Form.Item name="taskId" label="任务">
                              <Select
                                allowClear
                                showSearch
                                optionFilterProp="label"
                                placeholder="请选择绩效任务"
                                style={{ width: 320 }}
                                options={performanceTaskList.map((task) => ({
                                  value: task.id,
                                  label: `${task.performanceDescription || '未命名任务'}（${formatPeriodRange(task.periodStartDate, task.periodEndDate)}）`,
                                }))}
                              />
                            </Form.Item>
                            <Form.Item name="employeeName" label="姓名"><Input placeholder="员工姓名" allowClear /></Form.Item>
                            <Form.Item name="mobile" label="手机号"><Input placeholder="手机号" allowClear /></Form.Item>
                            <Form.Item name="confirmStatus" label="确认状态">
                              <Select allowClear placeholder="全部" style={{ width: 150 }} options={Object.entries(confirmStatusText).map(([value, label]) => ({ value, label }))} />
                            </Form.Item>
                            <Form.Item name="feedbackStatus" label="反馈状态">
                              <Select allowClear placeholder="全部" style={{ width: 120 }} options={Object.entries(feedbackStatusText).map(([value, label]) => ({ value, label }))} />
                            </Form.Item>
                            <Button type="primary" onClick={() => loadPerformanceRecords(1, performanceRecordPage.size)}>查询</Button>
                          </Form>
                          <Table
                            rowKey="id"
                            loading={performanceLoading}
                            columns={performanceColumns}
                            dataSource={performanceList}
                            size="small"
                            scroll={{ x: 1450 }}
                            pagination={{
                              current: performanceRecordPage.page,
                              pageSize: performanceRecordPage.size,
                              total: performanceRecordPage.total,
                              onChange: loadPerformanceRecords,
                            }}
                          />
                        </Space>
                      ),
                    },
                  ]}
                />
              </Card>
            </div>
          ) : pageKey === 'admin' ? (
            <Card
              size="small"
              title={pageMeta.title}
              extra={(
                <Space>
                  <Button icon={<ReloadOutlined />} onClick={refreshCurrentModule}>刷新</Button>
                  <Button type="primary" icon={<PlusOutlined />} onClick={openAdminCreate}>新增管理员</Button>
                </Space>
              )}
            >
              <Space direction="vertical" size={12} className="full-width">
                <Form layout="inline" form={adminQueryForm} className="query-bar">
                  <Form.Item name="username" label="用户名"><Input placeholder="用户名" allowClear /></Form.Item>
                  <Form.Item name="realName" label="姓名"><Input placeholder="姓名" allowClear /></Form.Item>
                  <Form.Item name="role" label="角色"><Select allowClear placeholder="全部" style={{ width: 140 }} options={adminRoleOptions} /></Form.Item>
                  <Button type="primary" onClick={() => loadAdmins(1, adminPage.size)}>查询</Button>
                </Form>
                <Table
                  rowKey="id"
                  loading={adminLoading}
                  columns={adminColumns}
                  dataSource={adminList}
                  size="small"
                  scroll={{ x: 1200 }}
                  pagination={{
                    current: adminPage.page,
                    pageSize: adminPage.size,
                    total: adminPage.total,
                    onChange: loadAdmins,
                  }}
                />
              </Space>
            </Card>
          ) : pageKey === 'socialBatches' ? (
            <Card
              size="small"
              title={pageMeta.title}
              extra={(
                <Space>
                  <Button icon={<ReloadOutlined />} onClick={refreshCurrentModule}>刷新</Button>
                  <Button type="primary" icon={<RocketOutlined />} onClick={() => {
                    form.setFieldsValue({ createAdminName: currentAdminName(loginUser) });
                    setCreateOpen(true);
                  }}>发起批次</Button>
                </Space>
              )}
            >
              <Table rowKey="id" loading={batchLoading} columns={batchColumns} dataSource={batchList} pagination={false} size="small" />
            </Card>
          ) : pageKey === 'socialTasks' ? (
            <Card
              size="small"
              title={pageMeta.title}
              extra={<Button icon={<ReloadOutlined />} onClick={refreshCurrentModule}>刷新</Button>}
            >
              <Table rowKey="id" loading={taskLoading} columns={taskColumns} dataSource={taskList} pagination={false} size="small" scroll={{ x: 1800 }} />
            </Card>
          ) : pageKey === 'enterprise' ? (
            <Card
              size="small"
              title={pageMeta.title}
              extra={(
                <Space>
                  <Button icon={<ReloadOutlined />} onClick={refreshCurrentModule}>刷新</Button>
                  <Button type="primary" icon={<PlusOutlined />} onClick={openEnterpriseCreate}>新增企业</Button>
                </Space>
              )}
            >
              <Space direction="vertical" size={12} className="full-width">
                <Form layout="inline" form={enterpriseQueryForm} className="query-bar">
                  <Form.Item name="taxNo" label="税号"><Input placeholder="税号" allowClear /></Form.Item>
                  <Form.Item name="enterpriseName" label="企业"><Input placeholder="企业名称" allowClear /></Form.Item>
                  <Form.Item name="regionCode" label="地区"><Input placeholder="地区编码" allowClear /></Form.Item>
                  <Form.Item name="status" label="状态">
                    <Select allowClear placeholder="全部" style={{ width: 120 }} options={statusOptions} />
                  </Form.Item>
                  <Button type="primary" onClick={() => loadEnterprises(1, enterprisePage.size)}>查询</Button>
                </Form>
                <Table
                  rowKey="id"
                  loading={enterpriseLoading}
                  columns={enterpriseColumns}
                  dataSource={enterpriseList}
                  size="small"
                  scroll={{ x: 1200 }}
                  pagination={{
                    current: enterprisePage.page,
                    pageSize: enterprisePage.size,
                    total: enterprisePage.total,
                    onChange: loadEnterprises,
                  }}
                />
              </Space>
            </Card>
          ) : (
            <Card
              size="small"
              title={pageMeta.title}
              extra={(
                <Space>
                  <Button icon={<ReloadOutlined />} onClick={refreshCurrentModule}>刷新</Button>
                  <Button type="primary" icon={<PlusOutlined />} onClick={openRegionSiteCreate}>新增站点</Button>
                </Space>
              )}
            >
              <Space direction="vertical" size={12} className="full-width">
                <Form layout="inline" form={regionSiteQueryForm} className="query-bar">
                  <Form.Item name="regionCode" label="地区"><Input placeholder="地区编码" allowClear /></Form.Item>
                  <Form.Item name="siteType" label="站点"><Input placeholder="default" allowClear /></Form.Item>
                  <Form.Item name="status" label="状态">
                    <Select allowClear placeholder="全部" style={{ width: 120 }} options={statusOptions} />
                  </Form.Item>
                  <Button type="primary" onClick={() => loadRegionSites(1, regionSitePage.size)}>查询</Button>
                </Form>
                <Table
                  rowKey="id"
                  loading={regionSiteLoading}
                  columns={regionSiteColumns}
                  dataSource={regionSiteList}
                  size="small"
                  scroll={{ x: 1200 }}
                  pagination={{
                    current: regionSitePage.page,
                    pageSize: regionSitePage.size,
                    total: regionSitePage.total,
                    onChange: loadRegionSites,
                  }}
                />
              </Space>
            </Card>
          )}
        </Content>
      </Layout>

      <Modal title="发起社保缴费批次" open={createOpen} onCancel={() => setCreateOpen(false)} onOk={submitCreate} okText="提交执行">
        <Form layout="vertical" form={form} initialValues={{ regionCode: 'liaoning', createAdminName: currentAdminName(loginUser) }}>
          <Form.Item name="regionCode" label="地区" rules={[{ required: true }]}>
            <Select options={[{ label: '辽宁', value: 'liaoning' }, { label: '宁波', value: 'ningbo' }, { label: '深圳', value: 'shenzhen' }]} />
          </Form.Item>
          <Form.Item name="periodMonth" label="费款所属月份" rules={[{ required: true, message: '请输入 yyyy-MM' }]}>
            <Input placeholder="2026-06" />
          </Form.Item>
          <Form.Item name="taxNoText" label="税号">
            <Input.TextArea rows={5} placeholder="留空则按地区生成全部启用企业任务；多个税号可用换行、空格或逗号分隔" />
          </Form.Item>
          <Form.Item name="createAdminName" label="创建人">
            <Input />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="社保缴费任务详情"
        open={socialTaskDetailOpen}
        onCancel={() => setSocialTaskDetailOpen(false)}
        footer={<Button onClick={() => setSocialTaskDetailOpen(false)}>关闭</Button>}
        width={860}
      >
        {currentSocialTask && (
          <Space direction="vertical" size={12} className="full-width">
            <Descriptions size="small" column={2} bordered>
              <Descriptions.Item label="任务">{currentSocialTask.id}</Descriptions.Item>
              <Descriptions.Item label="批次">{currentSocialTask.batchId || '-'}</Descriptions.Item>
              <Descriptions.Item label="税号">{currentSocialTask.taxNo}</Descriptions.Item>
              <Descriptions.Item label="企业">{currentSocialTask.enterpriseName || '-'}</Descriptions.Item>
              <Descriptions.Item label="社保账号">{currentSocialTask.securityAccountName || '-'}</Descriptions.Item>
              <Descriptions.Item label="月份">{currentSocialTask.periodMonth || '-'}</Descriptions.Item>
              <Descriptions.Item label="总状态">{renderStageTag(currentSocialTask.status)}</Descriptions.Item>
              <Descriptions.Item label="比对">{renderStageTag(currentSocialTask.compareStatus)}</Descriptions.Item>
              <Descriptions.Item label="缴费">{renderStageTag(currentSocialTask.paymentStatus)}</Descriptions.Item>
              <Descriptions.Item label="凭证">{renderStageTag(currentSocialTask.certificateStatus)}</Descriptions.Item>
              <Descriptions.Item label="BMS">{renderStageTag(currentSocialTask.bmsFeedbackStatus)}</Descriptions.Item>
              <Descriptions.Item label="BMS阶段">{currentSocialTask.bmsFeedbackStage || '-'}</Descriptions.Item>
              <Descriptions.Item label="WPM/税局金额">{currentSocialTask.wpmTotalAmount ?? '-'} / {currentSocialTask.payableAmount ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="机器人">{currentSocialTask.workerId || '-'}</Descriptions.Item>
              <Descriptions.Item label="领取时间">{formatDateTime(currentSocialTask.claimedAt)}</Descriptions.Item>
              <Descriptions.Item label="心跳时间">{formatDateTime(currentSocialTask.heartbeatAt)}</Descriptions.Item>
              <Descriptions.Item label="完成时间">{formatDateTime(currentSocialTask.finishedAt)}</Descriptions.Item>
              <Descriptions.Item label="诊断目录">{currentSocialTask.diagnosticDir || '-'}</Descriptions.Item>
            </Descriptions>
            {(currentSocialTask.errorMessage || currentSocialTask.bmsFeedbackErrorMessage) && (
              <div className="detail-block">
                {currentSocialTask.errorMessage || currentSocialTask.bmsFeedbackErrorMessage}
              </div>
            )}
            {renderJsonBlock('比对结果', currentSocialTask.compareResultPayload)}
            {renderJsonBlock('缴费结果', currentSocialTask.paymentResultPayload)}
            {renderJsonBlock('凭证结果', currentSocialTask.certificateResultPayload)}
            {renderJsonBlock('BMS反馈结果', currentSocialTask.bmsFeedbackResultPayload)}
            {renderJsonBlock('任务结果', currentSocialTask.resultPayload)}
          </Space>
        )}
      </Modal>

      <Modal
        title={editingEnterprise ? '编辑企业' : '新增企业'}
        open={enterpriseOpen}
        onCancel={() => setEnterpriseOpen(false)}
        onOk={submitEnterprise}
        okText="保存"
      >
        <Form layout="vertical" form={enterpriseForm}>
          <Form.Item name="taxNo" label="税号" rules={[{ required: true, message: '请输入税号' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="enterpriseName" label="企业名称" rules={[{ required: true, message: '请输入企业名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="regionCode" label="地区编码" rules={[{ required: true, message: '请输入地区编码' }]}>
            <Input placeholder="liaoning" />
          </Form.Item>
          <Form.Item name="securityAccountName" label="社保账号">
            <Input placeholder="对应 BMS scurityName" />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true }]}>
            <Select options={statusOptions} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editingRegionSite ? '编辑地区站点' : '新增地区站点'}
        open={regionSiteOpen}
        onCancel={() => setRegionSiteOpen(false)}
        onOk={submitRegionSite}
        okText="保存"
        width={760}
      >
        <Form layout="vertical" form={regionSiteForm}>
          <Form.Item name="regionCode" label="地区编码" rules={[{ required: true, message: '请输入地区编码' }]}>
            <Input placeholder="liaoning" />
          </Form.Item>
          <Form.Item name="siteType" label="站点类型" rules={[{ required: true, message: '请输入站点类型' }]}>
            <Input placeholder="default" />
          </Form.Item>
          <Form.Item name="etaxEntryUrl" label="电子税务局入口" rules={[{ required: true, message: '请输入入口地址' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="tpassBaseUrl" label="统一身份认证域名">
            <Input />
          </Form.Item>
          <Form.Item name="loginSuccessUrl" label="登录成功地址特征">
            <Input />
          </Form.Item>
          <Form.Item name="loginButtonText" label="登录按钮文案">
            <Input />
          </Form.Item>
          <Form.Item name="gt4BaseUrl" label="地方特色基础地址">
            <Input />
          </Form.Item>
          <Form.Item name="declarationQueryUrl" label="申报缴费查询地址">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="declarationQueryMenuId" label="申报缴费菜单编号">
            <Input />
          </Form.Item>
          <Form.Item name="socialSecurityPaymentFlowJson" label="社保缴费流程 JSON">
            <Input.TextArea rows={5} />
          </Form.Item>
          <Form.Item name="status" label="状态" rules={[{ required: true }]}>
            <Select options={statusOptions} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="创建绩效" open={performanceTaskOpen} onCancel={() => setPerformanceTaskOpen(false)} onOk={submitPerformanceTask} okText="创建">
        <Form layout="vertical" form={performanceTaskForm}>
          <Form.Item name="performanceDescription" label="绩效描述" rules={[{ required: true }]}>
            <Input placeholder="如：2026年6月绩效" />
          </Form.Item>
          <Form.Item name="periodRange" label="绩效周期" rules={[{ required: true }]}>
            <DatePicker.RangePicker className="full-width" />
          </Form.Item>
          <Form.Item name="confirmDeadlineTime" label="确认截止时间" rules={[{ required: true }]}>
            <DatePicker className="full-width" />
          </Form.Item>
          <div className="deadline-summary">
            <Text type="secondary" className="deadline-summary-label">二次确认截止时间</Text>
            <Text strong className="deadline-summary-value">{secondConfirmDeadlineText}</Text>
          </div>
        </Form>
      </Modal>

      <Modal title="导入员工绩效" open={importOpen} onCancel={() => setImportOpen(false)} footer={<Button onClick={() => setImportOpen(false)}>关闭</Button>} width={980}>
        <Form layout="vertical" form={importForm}>
          <Form.Item name="taskId" hidden rules={[{ required: true, message: '请选择绩效任务' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="绩效任务">
            <div className="readonly-field import-task-readonly full-width">{importTaskText}</div>
          </Form.Item>
          <div className="modal-toolbar">
            <Button type="link" icon={<DownloadOutlined />} onClick={downloadImportTemplate}>
              下载导入模板
            </Button>
            <Upload
              accept=".xlsx,.xls"
              maxCount={1}
              showUploadList={false}
              beforeUpload={(file) => {
                void uploadPerformanceImportFile(file);
                return false;
              }}
            >
              <Button type="primary" loading={importUploading} icon={<UploadOutlined />}>上传绩效</Button>
            </Upload>
          </div>
        </Form>
        <Table
          rowKey="id"
          className="import-record-table"
          columns={performanceImportColumns}
          dataSource={performanceImportUploads}
          loading={importUploadLoading}
          size="small"
          scroll={{ x: 1320 }}
          pagination={{ pageSize: 5 }}
          locale={{ emptyText: '暂无上传记录' }}
        />
      </Modal>

      <Modal title="反馈详情" open={feedbackOpen} onCancel={() => setFeedbackOpen(false)} footer={<Button onClick={() => setFeedbackOpen(false)}>关闭</Button>}>
        <Space direction="vertical" className="full-width">
          <Text type="secondary">员工：{currentRecord?.employeeName} {currentRecord?.mobile}</Text>
          <div className="detail-block">{currentRecord?.feedbackContent || '暂无反馈内容'}</div>
          {currentRecord?.feedbackHandleOpinion && <Text type="secondary">处理意见：{currentRecord.feedbackHandleOpinion}</Text>}
          {currentRecord?.feedbackHandleAdminName && <Text type="secondary">处理人：{currentRecord.feedbackHandleAdminName}</Text>}
        </Space>
      </Modal>

      <Modal title="调整绩效" open={adjustOpen} onCancel={() => setAdjustOpen(false)} onOk={submitAdjust} okText="保存调整">
        <Form layout="vertical" form={adjustForm}>
          <Form.Item label="员工">
            <Input value={`${currentRecord?.employeeName || ''} ${currentRecord?.mobile || ''}`} disabled />
          </Form.Item>
          <Form.Item name="afterPerformance" label="调整后绩效" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="adjustReason" label="调整原因" rules={[{ required: true }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={editingAdmin ? '编辑管理员' : '新增管理员'} open={adminOpen} onCancel={() => setAdminOpen(false)} onOk={submitAdmin} okText="保存">
        <Form layout="vertical" form={adminForm}>
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input disabled={!!editingAdmin} />
          </Form.Item>
          <Form.Item name="realName" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: !editingAdmin, message: '请输入密码' }]}>
            <Input.Password placeholder={editingAdmin ? '不修改请留空' : ''} />
          </Form.Item>
          <Form.Item name="role" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select options={adminRoleOptions} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="TOTP 二维码" open={adminTotpOpen} onCancel={() => setAdminTotpOpen(false)} footer={<Button onClick={() => setAdminTotpOpen(false)}>关闭</Button>}>
        {adminTotp && (
          <Space direction="vertical" align="center" className="full-width">
            <Text type="secondary">{adminTotp.username}</Text>
            <div className="qr-frame">
              <img src={`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(adminTotp.qrCodeUri)}`} alt="TOTP QR Code" />
            </div>
            <Text code>{adminTotp.secret}</Text>
          </Space>
        )}
      </Modal>
    </Layout>
  );
}

function trimObject(values: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== ''),
  );
}

function canAdjustPerformance(record: EmployeePerformanceRecord) {
  return record.confirmStatus === 'FEEDBACK_SUBMITTED' && record.feedbackStatus === 'PENDING';
}

function renderStageTag(value?: string) {
  if (!value) {
    return <Tag>-</Tag>;
  }
  return <Tag color={statusColor[value] || 'default'}>{stageStatusText[value] || socialTaskStatusText[value] || value}</Tag>;
}

function renderJsonBlock(title: string, value?: string) {
  if (!value) {
    return null;
  }
  return (
    <div className="json-block">
      <Text strong>{title}</Text>
      <pre>{formatJsonText(value)}</pre>
    </div>
  );
}

function formatJsonText(value: string) {
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}

type BackendDateValue = string | number | number[] | undefined;

function isPerformanceTaskOpen(status?: string) {
  return status === 'OPEN' || status === 'CONFIRMING';
}

function getPerformanceTaskDeleteBlockedReason(task: PerformanceTask) {
  if (isPerformanceTaskOpen(task.statusCode)) {
    return '请先停用该绩效任务，关闭状态才支持删除';
  }
  if ((task.totalCount || 0) > 0) {
    return '该绩效任务已导入员工记录，不支持删除';
  }
  return '';
}

function formatPeriodRange(startDate?: BackendDateValue, endDate?: BackendDateValue) {
  return `${formatDate(startDate)} ~ ${formatDate(endDate)}`;
}

function formatDate(value?: BackendDateValue) {
  if (!value) {
    return '-';
  }
  if (Array.isArray(value)) {
    const [year = 0, month = 1, day = 1] = value;
    return [year, month, day].map((item) => String(item).padStart(2, '0')).join('-');
  }
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD') : String(value);
}

function formatDateTime(value?: BackendDateValue) {
  if (!value) {
    return '-';
  }
  if (Array.isArray(value)) {
    const [year = 0, month = 1, day = 1, hour = 0, minute = 0, second = 0] = value;
    const date = [year, month, day].map((item) => String(item).padStart(2, '0')).join('-');
    const time = [hour, minute, second].map((item) => String(item).padStart(2, '0')).join(':');
    return `${date} ${time}`;
  }
  const compactParsed = parseCompactDateTime(value);
  if (compactParsed) {
    return compactParsed.format('YYYY-MM-DD HH:mm:ss');
  }
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : String(value);
}

function parseCompactDateTime(value: string | number) {
  const raw = String(value);
  if (!/^\d{12,14}$/.test(raw)) {
    return undefined;
  }
  const year = Number(raw.slice(0, 4));
  const datePart = raw.slice(4, -6);
  const hour = Number(raw.slice(-6, -4));
  const minute = Number(raw.slice(-4, -2));
  const second = Number(raw.slice(-2));
  const candidates = datePart.length === 4
    ? [[Number(datePart.slice(0, 2)), Number(datePart.slice(2))]]
    : datePart.length === 3
      ? [
          [Number(datePart.slice(0, 1)), Number(datePart.slice(1))],
          [Number(datePart.slice(0, 2)), Number(datePart.slice(2))],
        ]
      : [[Number(datePart.slice(0, 1)), Number(datePart.slice(1))]];
  for (const [month, day] of candidates) {
    const parsed = dayjs(`${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`);
    if (parsed.isValid() && parsed.year() === year && parsed.month() + 1 === month && parsed.date() === day) {
      return parsed;
    }
  }
  return undefined;
}

function downloadImportTemplate() {
  const link = document.createElement('a');
  link.href = performanceImportTemplateUrl;
  link.download = '员工绩效导入模板-20260624.xlsx';
  link.target = '_blank';
  link.rel = 'noopener noreferrer';
  link.click();
}

function downloadUrl(url: string, fileName: string) {
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
}

function readLoginUser(): LoginUser | null {
  const raw = window.localStorage.getItem('rf_mng_login_user');
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as LoginUser;
  } catch {
    window.localStorage.removeItem('rf_mng_login_user');
    return null;
  }
}

function currentAdminName(user: LoginUser | null) {
  return user?.realName || user?.username || 'admin';
}

export default App;
