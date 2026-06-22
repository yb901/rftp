import {
  DownloadOutlined,
  EditOutlined,
  EyeOutlined,
  LoginOutlined,
  LogoutOutlined,
  PlusOutlined,
  ReloadOutlined,
  RetweetOutlined,
  RocketOutlined,
  UploadOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Button, DatePicker, Form, Input, Layout, Modal, Select, Space, Table, Tabs, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import {
  BatchRecord,
  EmployeePerformanceImportItem,
  EmployeePerformanceRecord,
  LoginUser,
  PerformanceTask,
  TaskRecord,
  adjustPerformanceRecord,
  createBatch,
  createPerformanceTask,
  exportPerformanceRecords,
  fetchBatches,
  fetchPerformanceRecords,
  fetchPerformanceTasks,
  fetchTasks,
  importPerformanceRecords,
  login,
  logout,
  retryTask,
} from './api';

const { Header, Content, Sider } = Layout;
const { Text } = Typography;

const statusColor: Record<string, string> = {
  SUBMITTED: 'processing',
  RUNNING: 'processing',
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

function App() {
  const [loginUser, setLoginUser] = useState<LoginUser | null>(() => readLoginUser());
  const [loginLoading, setLoginLoading] = useState(false);
  const [moduleKey, setModuleKey] = useState<'social' | 'performance'>('social');
  const [batchList, setBatchList] = useState<BatchRecord[]>([]);
  const [taskList, setTaskList] = useState<TaskRecord[]>([]);
  const [performanceTaskList, setPerformanceTaskList] = useState<PerformanceTask[]>([]);
  const [performanceList, setPerformanceList] = useState<EmployeePerformanceRecord[]>([]);
  const [batchLoading, setBatchLoading] = useState(false);
  const [taskLoading, setTaskLoading] = useState(false);
  const [performanceTaskLoading, setPerformanceTaskLoading] = useState(false);
  const [performanceLoading, setPerformanceLoading] = useState(false);
  const [performanceTaskPage, setPerformanceTaskPage] = useState({ page: 1, size: 10, total: 0 });
  const [performanceRecordPage, setPerformanceRecordPage] = useState({ page: 1, size: 50, total: 0 });
  const [createOpen, setCreateOpen] = useState(false);
  const [performanceTaskOpen, setPerformanceTaskOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [feedbackOpen, setFeedbackOpen] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<EmployeePerformanceRecord | null>(null);
  const [form] = Form.useForm();
  const [performanceTaskForm] = Form.useForm();
  const [importForm] = Form.useForm();
  const [adjustForm] = Form.useForm();
  const [loginForm] = Form.useForm();
  const [performanceQueryForm] = Form.useForm();
  const [performanceTaskQueryForm] = Form.useForm();

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

  useEffect(() => {
    const handleUnauthorized = () => setLoginUser(null);
    window.addEventListener('rf_mng_unauthorized', handleUnauthorized);
    return () => window.removeEventListener('rf_mng_unauthorized', handleUnauthorized);
  }, []);

  useEffect(() => {
    if (!loginUser) {
      return;
    }
    void loadBatches();
    void loadTasks();
    void loadPerformanceTasks(1, 10);
    void loadPerformanceRecords();
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
    { title: '状态', dataIndex: 'status', width: 130, render: (value) => <Tag color={statusColor[value] || 'default'}>{value}</Tag> },
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
    { title: '绩效描述', dataIndex: 'performanceDescription', width: 180, ellipsis: true },
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
    { title: '任务', dataIndex: 'id', width: 80 },
    { title: '绩效描述', dataIndex: 'performanceDescription', width: 220, ellipsis: true },
    { title: '评价周期', width: 210, render: (_, row) => `${row.periodStartDate || '-'} 至 ${row.periodEndDate || '-'}` },
    { title: '确认截止', dataIndex: 'confirmDeadlineTime', width: 170 },
    { title: '二次截止', dataIndex: 'secondConfirmDeadlineTime', width: 170 },
    { title: '状态', dataIndex: 'statusCode', width: 110, render: (value) => <Tag color={statusColor[value] || 'default'}>{value || 'DRAFT'}</Tag> },
    { title: '人数', width: 170, render: (_, row) => `${row.confirmedCount || 0}/${row.totalCount || 0} 确认，${row.feedbackCount || 0} 反馈` },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" onClick={() => {
            performanceQueryForm.setFieldValue('taskId', row.id);
            void loadPerformanceRecords(1, performanceRecordPage.size);
          }}>查记录</Button>
          <Button size="small" onClick={() => {
            importForm.setFieldValue('taskId', row.id);
            setImportOpen(true);
          }}>导入</Button>
        </Space>
      ),
    },
  ], [importForm, performanceQueryForm, performanceRecordPage.size]);

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
      confirmDeadlineTime: values.confirmDeadlineTime.format('YYYY-MM-DDTHH:mm:ss'),
      secondConfirmDeadlineTime: values.secondConfirmDeadlineTime?.format('YYYY-MM-DDTHH:mm:ss'),
      createAdminId: loginUser?.id,
      createAdminName: values.createAdminName || currentAdminName(loginUser),
    });
    message.success(`绩效任务已创建：${task.id}`);
    setPerformanceTaskOpen(false);
    performanceTaskForm.resetFields();
    performanceQueryForm.setFieldValue('taskId', task.id);
    await Promise.all([loadPerformanceTasks(1, performanceTaskPage.size), loadPerformanceRecords(1, performanceRecordPage.size)]);
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
    if (moduleKey === 'performance') {
      return Promise.all([
        loadPerformanceTasks(performanceTaskPage.page, performanceTaskPage.size),
        loadPerformanceRecords(performanceRecordPage.page, performanceRecordPage.size),
      ]);
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

  if (!loginUser) {
    return (
      <div className="login-shell">
        <div className="login-panel">
          <div className="login-title">rf-mng</div>
          <div className="login-subtitle">管理后台登录</div>
          <Form layout="vertical" form={loginForm} initialValues={{ username: 'admin' }} onFinish={submitLogin}>
            <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
              <Input prefix={<UserOutlined />} autoComplete="username" />
            </Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password autoComplete="current-password" />
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
        <button className={`nav-item ${moduleKey === 'social' ? 'active' : ''}`} onClick={() => setModuleKey('social')}>社保缴费</button>
        <button className={`nav-item ${moduleKey === 'performance' ? 'active' : ''}`} onClick={() => setModuleKey('performance')}>员工绩效</button>
        <button className="nav-item">企业维护</button>
        <button className="nav-item">地区配置</button>
      </Sider>
      <Layout>
        <Header className="app-header">
          <div>
            <div className="title">{moduleKey === 'performance' ? '员工绩效管理' : '社保缴费管理'}</div>
            <div className="subtitle">{moduleKey === 'performance' ? '创建评价周期，导入员工绩效，跟踪确认反馈和调整闭环' : '按地区、月份发起任务，跟踪电子税务局执行结果'}</div>
          </div>
          <Space>
            <Text type="secondary">{currentAdminName(loginUser)}</Text>
            <Button icon={<LogoutOutlined />} onClick={submitLogout}>退出</Button>
            <Button icon={<ReloadOutlined />} onClick={refreshCurrentModule}>刷新</Button>
            {moduleKey === 'performance' ? (
              <>
                <Button icon={<DownloadOutlined />} onClick={downloadPerformanceExport}>导出</Button>
                <Button icon={<DownloadOutlined />} onClick={downloadImportTemplate}>模板</Button>
                <Button icon={<UploadOutlined />} onClick={() => setImportOpen(true)}>导入绩效</Button>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => {
                  performanceTaskForm.setFieldsValue({ createAdminName: currentAdminName(loginUser) });
                  setPerformanceTaskOpen(true);
                }}>创建绩效</Button>
              </>
            ) : (
              <Button type="primary" icon={<RocketOutlined />} onClick={() => {
                form.setFieldsValue({ createAdminName: currentAdminName(loginUser) });
                setCreateOpen(true);
              }}>发起批次</Button>
            )}
          </Space>
        </Header>
        <Content className="app-content">
          {moduleKey === 'performance' ? (
            <Space direction="vertical" size={12} className="full-width">
              <Tabs
                items={[
                  {
                    key: 'performanceTasks',
                    label: '绩效任务',
                    children: (
                      <Space direction="vertical" size={12} className="full-width">
                        <Form layout="inline" form={performanceTaskQueryForm} className="query-bar">
                          <Form.Item name="performanceDescription" label="绩效描述"><Input placeholder="绩效描述" allowClear /></Form.Item>
                          <Form.Item name="status" label="状态"><Input placeholder="状态编码" allowClear /></Form.Item>
                          <Button type="primary" onClick={() => loadPerformanceTasks(1, performanceTaskPage.size)}>查询</Button>
                        </Form>
                        <Table
                          rowKey="id"
                          loading={performanceTaskLoading}
                          columns={performanceTaskColumns}
                          dataSource={performanceTaskList}
                          size="middle"
                          scroll={{ x: 1200 }}
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
                        <Form layout="inline" form={performanceQueryForm} className="query-bar">
                          <Form.Item name="taskId" label="任务ID"><Input placeholder="任务ID" allowClear /></Form.Item>
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
                          size="middle"
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
            </Space>
          ) : (
            <Tabs
              items={[
                { key: 'batches', label: '缴费批次', children: <Table rowKey="id" loading={batchLoading} columns={batchColumns} dataSource={batchList} pagination={false} size="middle" /> },
                { key: 'tasks', label: '任务明细', children: <Table rowKey="id" loading={taskLoading} columns={taskColumns} dataSource={taskList} pagination={false} size="middle" scroll={{ x: 1300 }} /> },
              ]}
            />
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
        <Form layout="vertical" form={performanceTaskForm} initialValues={{ createAdminName: currentAdminName(loginUser) }}>
          <Form.Item name="performanceDescription" label="绩效描述" rules={[{ required: true }]}>
            <Input placeholder="如：2026年6月绩效" />
          </Form.Item>
          <Form.Item name="periodRange" label="评价周期" rules={[{ required: true }]}>
            <DatePicker.RangePicker className="full-width" />
          </Form.Item>
          <Form.Item name="confirmDeadlineTime" label="确认截止时间" rules={[{ required: true }]}>
            <DatePicker showTime className="full-width" />
          </Form.Item>
          <Form.Item name="secondConfirmDeadlineTime" label="二次确认截止时间">
            <DatePicker showTime className="full-width" />
          </Form.Item>
          <Form.Item name="createAdminName" label="创建人">
            <Input />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="导入员工绩效" open={importOpen} onCancel={() => setImportOpen(false)} onOk={submitImport} okText="导入" width={720}>
        <Form layout="vertical" form={importForm}>
          <Form.Item name="taskId" label="绩效任务ID" rules={[{ required: true }]}>
            <Input placeholder="先创建绩效任务后填写任务ID" />
          </Form.Item>
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
    </Layout>
  );
}

function trimObject(values: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(values).filter(([, value]) => value !== undefined && value !== null && String(value).trim() !== ''),
  );
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
