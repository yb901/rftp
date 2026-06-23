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
import { Avatar, Breadcrumb, Button, Card, DatePicker, Descriptions, Form, Input, Layout, Menu, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { MenuProps } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import {
  AdminSaveParam,
  AdminUser,
  BatchRecord,
  EmployeePerformanceImportItem,
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
  fetchPerformanceRecords,
  fetchPerformanceTasks,
  fetchTasks,
  generateAdminTotp,
  getRequestErrorMessage,
  importPerformanceRecords,
  login,
  logout,
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
  PENDING_SECOND_CONFIRM: 'processing',
  SECOND_CONFIRMED: 'success',
  SECOND_AUTO_CONFIRMED: 'warning',
  NONE: 'default',
  HANDLED_ADJUSTED: 'success',
  OPEN: 'success',
  CLOSED: 'default',
  DRAFT: 'default',
  CONFIRMING: 'success',
};

const confirmStatusText: Record<string, string> = {
  PENDING_CONFIRM: '待确认',
  CONFIRMED: '已确认',
  AUTO_CONFIRMED: '超时自动确认',
  PENDING_SECOND_CONFIRM: '待二次确认',
  SECOND_CONFIRMED: '二次已确认',
  SECOND_AUTO_CONFIRMED: '二次超时自动确认',
};

const feedbackStatusText: Record<string, string> = {
  NONE: '无反馈',
  PENDING: '待处理',
  HANDLED_ADJUSTED: '已调整',
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

const adminRoleOptions = [
  { value: 1, label: '超级管理员' },
  { value: 2, label: '管理员' },
  { value: 3, label: '运营负责人' },
  { value: 4, label: '商务负责人' },
  { value: 5, label: '运营' },
  { value: 6, label: '商务' },
  { value: 7, label: '客服' },
  { value: 8, label: '研发' },
];

type PageKey = 'socialBatches' | 'socialTasks' | 'enterprise' | 'region' | 'performance' | 'admin';

function App() {
  const [loginUser, setLoginUser] = useState<LoginUser | null>(() => readLoginUser());
  const [loginLoading, setLoginLoading] = useState(false);
  const [pageKey, setPageKey] = useState<PageKey>(() => getPageKeyByPath(window.location.pathname));
  const [batchList, setBatchList] = useState<BatchRecord[]>([]);
  const [taskList, setTaskList] = useState<TaskRecord[]>([]);
  const [performanceTaskList, setPerformanceTaskList] = useState<PerformanceTask[]>([]);
  const [performanceList, setPerformanceList] = useState<EmployeePerformanceRecord[]>([]);
  const [adminList, setAdminList] = useState<AdminUser[]>([]);
  const [batchLoading, setBatchLoading] = useState(false);
  const [taskLoading, setTaskLoading] = useState(false);
  const [performanceTaskLoading, setPerformanceTaskLoading] = useState(false);
  const [performanceLoading, setPerformanceLoading] = useState(false);
  const [adminLoading, setAdminLoading] = useState(false);
  const [performanceTaskPage, setPerformanceTaskPage] = useState({ page: 1, size: 10, total: 0 });
  const [performanceRecordPage, setPerformanceRecordPage] = useState({ page: 1, size: 50, total: 0 });
  const [adminPage, setAdminPage] = useState({ page: 1, size: 10, total: 0 });
  const [createOpen, setCreateOpen] = useState(false);
  const [performanceTaskOpen, setPerformanceTaskOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
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
  const secondConfirmDeadlineText = useMemo(() => {
    if (!confirmDeadlineTime) {
      return '确认截止后 3 天';
    }
    return dayjs(confirmDeadlineTime).endOf('day').add(3, 'day').format('YYYY-MM-DD HH:mm:ss');
  }, [confirmDeadlineTime]);

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

  const loadPerformanceRecords = async (page = performanceRecordPage.page, size = performanceRecordPage.size) => {
    setPerformanceLoading(true);
    try {
      const values = performanceQueryForm.getFieldsValue();
      const data = await fetchPerformanceRecords({ page, size, ...trimObject(values) });
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
    void loadBatches();
    void loadTasks();
    void loadPerformanceTasks(1, 10);
    void loadPerformanceRecords();
    void loadAdmins(1, 10);
  }, [loginUser]);

  const batchColumns: ColumnsType<BatchRecord> = useMemo(() => [
    { title: '批次', dataIndex: 'id', width: 90 },
    { title: '地区', dataIndex: 'regionCode', width: 120 },
    { title: '月份', dataIndex: 'periodMonth', width: 120 },
    { title: '状态', dataIndex: 'status', width: 130, render: (value) => <Tag color={statusColor[value] || 'default'}>{value}</Tag> },
    { title: '任务', width: 170, render: (_, row) => `${row.successCount || 0}/${row.totalCount || 0} 成功，${row.failedCount || 0} 失败` },
    { title: '创建时间', dataIndex: 'gmtCreate', width: 190 },
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
    { title: '记录', dataIndex: 'id', width: 80 },
    { title: '任务', dataIndex: 'taskId', width: 90 },
    { title: '绩效描述', dataIndex: 'performanceDescription', width: 160, ellipsis: true },
    { title: '姓名', dataIndex: 'employeeName', width: 100 },
    { title: '手机号', dataIndex: 'mobile', width: 130 },
    { title: '工号', dataIndex: 'employeeNo', width: 110 },
    { title: '项目/部门', dataIndex: 'projectDepartment', width: 150, ellipsis: true },
    { title: '岗位', dataIndex: 'positionName', width: 130, ellipsis: true },
    { title: '绩效', dataIndex: 'performance', width: 120 },
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
          <Button size="small" icon={<EditOutlined />} onClick={() => openAdjust(row)}>调整</Button>
        </Space>
      ),
    },
  ], []);

  const performanceTaskColumns: ColumnsType<PerformanceTask> = useMemo(() => [
    { title: '绩效描述', dataIndex: 'performanceDescription', width: 220, ellipsis: true },
    { title: '评价周期', width: 230, render: (_, row) => formatPeriodRange(row.periodStartDate, row.periodEndDate) },
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
              void loadPerformanceRecords(1, performanceRecordPage.size);
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
    { title: '创建时间', dataIndex: 'gmtCreate', width: 190 },
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

  const submitImport = async () => {
    const values = await importForm.validateFields();
    const records = parseImportRecords(values.recordsText);
    const result = await importPerformanceRecords(Number(values.taskId), records);
    if (result.errors?.length) {
      Modal.error({
        title: '导入失败',
        content: (
          <div className="error-list">
            {result.errors.map((item, index) => (
              <div key={`${item.rowNo || index}-${item.mobile || ''}`}>第 {item.rowNo || '-'} 行 {item.mobile || ''}：{item.errorMessage}</div>
            ))}
          </div>
        ),
      });
      return;
    }
    message.success(`导入成功 ${result.successCount || records.length} 条`);
    setImportOpen(false);
    importForm.resetFields();
    performanceQueryForm.setFieldValue('taskId', Number(values.taskId));
    await Promise.all([loadPerformanceTasks(performanceTaskPage.page, performanceTaskPage.size), loadPerformanceRecords(1, performanceRecordPage.size)]);
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
  const menuItems: MenuProps['items'] = [
    {
      key: 'social',
      icon: <BankOutlined />,
      label: '社保缴费',
      children: [
        { key: 'socialBatches', label: '缴费批次' },
        { key: 'socialTasks', label: '任务明细' },
        { key: 'enterprise', label: '企业维护' },
        { key: 'region', label: '地区配置' },
      ],
    },
    { key: 'performance', icon: <TeamOutlined />, label: '员工绩效' },
    { key: 'admin', icon: <UserSwitchOutlined />, label: '系统管理员' },
  ];

  if (!loginUser) {
    return (
      <div className="login-shell">
        <div className="login-panel">
          <div className="login-title">rf-mng</div>
          <div className="login-subtitle">管理后台登录</div>
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
        <div className="brand">rf-mng</div>
        <Menu
          theme="dark"
          mode="inline"
          items={menuItems}
          selectedKeys={[pageKey]}
          defaultOpenKeys={['social']}
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
                            scroll={{ x: 1500 }}
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
          <Form.Item name="periodRange" label="评价周期" rules={[{ required: true }]}>
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

      <Modal title="导入员工绩效" open={importOpen} onCancel={() => setImportOpen(false)} onOk={submitImport} okText="导入" width={720}>
        <Form layout="vertical" form={importForm}>
          <Form.Item name="taskId" label="绩效任务" rules={[{ required: true, message: '请选择绩效任务' }]}>
            <Select
              showSearch
              optionFilterProp="label"
              placeholder="请选择绩效任务"
              options={performanceTaskList.map((task) => ({
                value: task.id,
                label: `${task.performanceDescription || '未命名任务'}（${formatPeriodRange(task.periodStartDate, task.periodEndDate)}）`,
              }))}
            />
          </Form.Item>
          <div className="modal-toolbar">
            <Button icon={<DownloadOutlined />} onClick={downloadImportTemplate}>下载导入模板</Button>
          </div>
          <Form.Item name="recordsText" label="导入明细" rules={[{ required: true }]}>
            <Input.TextArea
              rows={10}
              placeholder={'支持 JSON 数组，或每行：姓名,手机号,绩效,工号,项目/部门,岗位\n张三,13800138000,A,001,客服一部,客服专员'}
            />
          </Form.Item>
        </Form>
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
    performance: { title: '员工绩效管理', subtitle: '创建评价周期，导入员工绩效，跟踪确认反馈和调整闭环' },
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

function trimObject(values: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== ''),
  );
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
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : String(value);
}

function parseImportRecords(text: string): EmployeePerformanceImportItem[] {
  const source = text.trim();
  if (!source) {
    throw new Error('导入明细不能为空');
  }
  if (source.startsWith('[')) {
    return JSON.parse(source) as EmployeePerformanceImportItem[];
  }
  return source
    .split('\n')
    .map((line, index) => {
      const [employeeName, mobile, performance, employeeNo, projectDepartment, positionName] = line.split(/,|，/).map((item) => item.trim());
      return { rowNo: index + 1, employeeName, mobile, performance, employeeNo, projectDepartment, positionName };
    })
    .filter((item) => item.employeeName || item.mobile || item.performance);
}

function downloadImportTemplate() {
  const content = '\uFEFF姓名,手机号,绩效,工号,项目/部门,岗位\n张三,13800138000,A,001,客服一部,客服专员\n';
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = '员工绩效导入模板.csv';
  link.click();
  window.URL.revokeObjectURL(url);
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
