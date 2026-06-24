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
  TeamOutlined,
  UploadOutlined,
  UserSwitchOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Breadcrumb, Button, Card, DatePicker, Descriptions, Form, Input, Layout, Menu, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, Typography, Upload, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { MenuProps } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import companyLogo from './assets/company-logo.svg';
import {
  AdminSaveParam,
  AdminUser,
  BatchRecord,
  EmployeePerformanceImportUpload,
  EmployeePerformanceRecord,
  LoginUser,
  PerformanceTask,
  TaskRecord,
  adjustPerformanceRecord,
  createBatch,
  createPerformanceTask,
  deletePerformanceTask,
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
  updateAdmin,
} from './api';

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

const statusColor: Record<string, string> = {
  SUBMITTED: 'processing',
  RUNNING: 'processing',
  PROCESSING: 'processing',
  SUCCESS: 'success',
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

const performanceImportTemplateUrl = 'https://static.zcglhr.com/qy-mng/upload-task/%E5%91%98%E5%B7%A5%E7%BB%A9%E6%95%88%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF-20260624.xlsx';

const adminRoleOptions = [
  { value: 1, label: '超级管理员' },
  { value: 2, label: '管理员' },
  { value: 3, label: '社保专员' },
  { value: 4, label: '绩效专员' },
];

type ModuleKey = 'social' | 'performance' | 'admin';
type PageKey = 'socialBatches' | 'socialTasks' | 'enterprise' | 'region' | 'performance' | 'admin';

const roleModules: Record<number, ModuleKey[]> = {
  1: ['social', 'performance', 'admin'],
  2: ['social', 'performance'],
  3: ['social'],
  4: ['performance'],
};

function App() {
  const [loginUser, setLoginUser] = useState<LoginUser | null>(() => readLoginUser());
  const [loginLoading, setLoginLoading] = useState(false);
  const [pageKey, setPageKey] = useState<PageKey>(() => getPageKeyByPath(window.location.pathname));
  const [batchList, setBatchList] = useState<BatchRecord[]>([]);
  const [taskList, setTaskList] = useState<TaskRecord[]>([]);
  const [performanceTaskList, setPerformanceTaskList] = useState<PerformanceTask[]>([]);
  const [performanceList, setPerformanceList] = useState<EmployeePerformanceRecord[]>([]);
  const [performanceImportUploads, setPerformanceImportUploads] = useState<EmployeePerformanceImportUpload[]>([]);
  const [adminList, setAdminList] = useState<AdminUser[]>([]);
  const [batchLoading, setBatchLoading] = useState(false);
  const [taskLoading, setTaskLoading] = useState(false);
  const [performanceTaskLoading, setPerformanceTaskLoading] = useState(false);
  const [performanceLoading, setPerformanceLoading] = useState(false);
  const [importUploadLoading, setImportUploadLoading] = useState(false);
  const [adminLoading, setAdminLoading] = useState(false);
  const [performanceTaskPage, setPerformanceTaskPage] = useState({ page: 1, size: 10, total: 0 });
  const [performanceRecordPage, setPerformanceRecordPage] = useState({ page: 1, size: 50, total: 0 });
  const [adminPage, setAdminPage] = useState({ page: 1, size: 10, total: 0 });
  const [createOpen, setCreateOpen] = useState(false);
  const [performanceTaskOpen, setPerformanceTaskOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  const [importUploading, setImportUploading] = useState(false);
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [feedbackOpen, setFeedbackOpen] = useState(false);
  const [performanceTabKey, setPerformanceTabKey] = useState('performanceTasks');
  const [adminOpen, setAdminOpen] = useState(false);
  const [adminTotpOpen, setAdminTotpOpen] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<EmployeePerformanceRecord | null>(null);
  const [editingAdmin, setEditingAdmin] = useState<AdminUser | null>(null);
  const [adminTotp, setAdminTotp] = useState<{ secret: string; qrCodeUri: string; username: string } | null>(null);
  const [form] = Form.useForm();
  const [performanceTaskForm] = Form.useForm();
  const [importForm] = Form.useForm();
  const [adjustForm] = Form.useForm();
  const [adminForm] = Form.useForm<AdminSaveParam>();
  const [loginForm] = Form.useForm();
  const [performanceQueryForm] = Form.useForm();
  const [performanceTaskQueryForm] = Form.useForm();
  const [adminQueryForm] = Form.useForm();
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
    { title: '机器人', dataIndex: 'workerId', width: 120, render: (value) => value || '-' },
    { title: '金额', dataIndex: 'payableAmount', width: 120 },
    { title: '失败原因', dataIndex: 'errorMessage', ellipsis: true },
    {
      title: '操作', width: 110, fixed: 'right', render: (_, row) => (
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
      ),
    },
  ], []);

  const performanceColumns: ColumnsType<EmployeePerformanceRecord> = useMemo(() => [
    { title: '绩效描述', dataIndex: 'performanceDescription', width: 160, ellipsis: true },
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
      width: 180,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} disabled={!row.feedbackContent} onClick={() => openFeedback(row)}>反馈</Button>
          <Button size="small" icon={<EditOutlined />} disabled={!canAdjustPerformance(row)} onClick={() => openAdjust(row)}>调整</Button>
          <Button size="small" disabled={!canAdjustPerformance(row)} onClick={() => closeFeedbackUnchanged(row)}>无需调整</Button>
        </Space>
      ),
    },
  ], []);

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
      render: (_, row) => (
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
          <Popconfirm title="确定删除该绩效任务？" description="仅支持删除未导入员工记录的关闭任务。" onConfirm={() => void removePerformanceTask(row)}>
            <Button size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
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
      taxNoList: values.taxNoText.split(/\s|,|，/).map((item: string) => item.trim()).filter(Boolean),
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
    await deletePerformanceTask(task.id);
    message.success('绩效任务已删除');
    if (performanceQueryForm.getFieldValue('taskId') === task.id) {
      performanceQueryForm.setFieldValue('taskId', undefined);
    }
    await Promise.all([loadPerformanceTasks(1, performanceTaskPage.size), loadPerformanceRecords(1, performanceRecordPage.size)]);
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

  const openAdjust = (record: EmployeePerformanceRecord) => {
    setCurrentRecord(record);
    adjustForm.setFieldsValue({ afterPerformance: record.performance, operatorAdminId: loginUser?.id, operatorAdminName: currentAdminName(loginUser) });
    setAdjustOpen(true);
  };

  const submitAdjust = async () => {
    if (!currentRecord) {
      return;
    }
    const values = await adjustForm.validateFields();
    await adjustPerformanceRecord(currentRecord.id, { ...values, operatorAdminId: loginUser?.id, operatorAdminName: values.operatorAdminName || currentAdminName(loginUser) });
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

  const openAdminCreate = () => {
    setEditingAdmin(null);
    adminForm.resetFields();
    adminForm.setFieldsValue({ enabled: 1, role: 2 });
    setAdminOpen(true);
  };

  const openAdminEdit = (admin: AdminUser) => {
    setEditingAdmin(admin);
    adminForm.setFieldsValue({ ...admin, password: '' });
    setAdminOpen(true);
  };

  const submitAdmin = async () => {
    const values = await adminForm.validateFields();
    if (editingAdmin) {
      await updateAdmin({ ...values, id: editingAdmin.id, username: editingAdmin.username });
      message.success('管理员已更新');
    } else {
      await saveAdmin({ ...values, enabled: values.enabled ?? 1 });
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

  const downloadPerformanceExport = async () => {
    const values = performanceQueryForm.getFieldsValue();
    const response = await exportPerformanceRecords({ ...trimObject(values) });
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
      return Promise.resolve();
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
                              <Button icon={<DownloadOutlined />} onClick={downloadPerformanceExport}>导出</Button>
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
                            scroll={{ x: 1330 }}
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
              <Table rowKey="id" loading={taskLoading} columns={taskColumns} dataSource={taskList} pagination={false} size="small" scroll={{ x: 1300 }} />
            </Card>
          ) : pageKey === 'enterprise' ? (
            <ConfigPlaceholder icon={<BankOutlined />} title="企业维护" description="企业维护属于社保缴费模块，后续可在这里接入企业、税号和社保账号维护能力。" />
          ) : (
            <ConfigPlaceholder icon={<EnvironmentOutlined />} title="地区配置" description="地区配置属于社保缴费模块，后续可在这里维护地区编码、站点类型和机器人执行参数。" />
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
          <Form.Item name="taxNoText" label="税号" rules={[{ required: true, message: '请输入至少一个税号' }]}>
            <Input.TextArea rows={5} placeholder="多个税号可用换行、空格或逗号分隔" />
          </Form.Item>
          <Form.Item name="createAdminName" label="创建人">
            <Input />
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
          <Form.Item name="operatorAdminName" label="操作人" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="operatorMobile" label="操作人手机号">
            <Input />
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
          <Form.Item name="enabled" label="是否启用" rules={[{ required: true, message: '请选择是否启用' }]}>
            <Select options={[{ value: 1, label: '是' }, { value: 0, label: '否' }]} />
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

function ConfigPlaceholder({ icon, title, description }: { icon: ReactNode; title: string; description: string }) {
  return (
    <div className="config-placeholder">
      <div className="config-icon">{icon}</div>
      <div className="config-title">{title}</div>
      <div className="config-description">{description}</div>
    </div>
  );
}

function getPageMeta(pageKey: PageKey) {
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

function getPagePath(pageKey: PageKey) {
  const pathMap: Record<PageKey, string> = {
    socialBatches: '/social-security/batches',
    socialTasks: '/social-security/tasks',
    enterprise: '/social-security/enterprise',
    region: '/social-security/region',
    performance: '/performance',
    admin: '/admin',
  };
  return pathMap[pageKey];
}

function getPageKeyByPath(pathname: string): PageKey {
  const normalizedPath = pathname.replace(/\/+$/, '') || '/';
  const pageMap: Array<{ prefix: string; key: PageKey }> = [
    { prefix: '/social-security/batches', key: 'socialBatches' },
    { prefix: '/social-security/tasks', key: 'socialTasks' },
    { prefix: '/social-security/enterprise', key: 'enterprise' },
    { prefix: '/social-security/region', key: 'region' },
    { prefix: '/performance', key: 'performance' },
    { prefix: '/admin', key: 'admin' },
  ];
  const matched = pageMap.find((item) => normalizedPath === item.prefix || normalizedPath.startsWith(`${item.prefix}/`));
  return matched?.key || 'socialBatches';
}

function navigateToPage(nextPageKey: PageKey, currentPageKey: PageKey, setPageKey: (pageKey: PageKey) => void) {
  if (nextPageKey === currentPageKey) {
    return;
  }
  const nextPath = getPagePath(nextPageKey);
  window.history.pushState({}, '', nextPath);
  setPageKey(nextPageKey);
}

function buildMenuItems(user: LoginUser | null): MenuProps['items'] {
  const items: MenuProps['items'] = [];
  if (canAccessModule(user, 'social')) {
    items.push({
      key: 'social',
      icon: <BankOutlined />,
      label: '社保缴费',
      children: [
        { key: 'socialBatches', label: '缴费批次' },
        { key: 'socialTasks', label: '任务明细' },
        { key: 'enterprise', label: '企业维护' },
        { key: 'region', label: '地区配置' },
      ],
    });
  }
  if (canAccessModule(user, 'performance')) {
    items.push({ key: 'performance', icon: <TeamOutlined />, label: '员工绩效' });
  }
  if (canAccessModule(user, 'admin')) {
    items.push({ key: 'admin', icon: <UserSwitchOutlined />, label: '系统管理员' });
  }
  return items;
}

function canAccessPage(user: LoginUser | null, pageKey: PageKey) {
  return canAccessModule(user, getPageModule(pageKey));
}

function canAccessModule(user: LoginUser | null, moduleKey: ModuleKey) {
  if (!user) {
    return false;
  }
  return (roleModules[user.role] || []).includes(moduleKey);
}

function getPageModule(pageKey: PageKey): ModuleKey {
  if (pageKey === 'performance') {
    return 'performance';
  }
  if (pageKey === 'admin') {
    return 'admin';
  }
  return 'social';
}

function getFirstAccessiblePage(user: LoginUser | null): PageKey {
  if (canAccessModule(user, 'social')) {
    return 'socialBatches';
  }
  if (canAccessModule(user, 'performance')) {
    return 'performance';
  }
  if (canAccessModule(user, 'admin')) {
    return 'admin';
  }
  return 'socialBatches';
}

function trimObject(values: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== ''),
  );
}

function canAdjustPerformance(record: EmployeePerformanceRecord) {
  return record.confirmStatus === 'FEEDBACK_SUBMITTED' && record.feedbackStatus === 'PENDING';
}

type BackendDateValue = string | number | number[] | undefined;

function isPerformanceTaskOpen(status?: string) {
  return status === 'OPEN' || status === 'CONFIRMING';
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
