import { ReloadOutlined, RetweetOutlined, RocketOutlined } from '@ant-design/icons';
import { Button, Form, Input, Layout, Modal, Select, Space, Table, Tabs, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { BatchRecord, TaskRecord, createBatch, fetchBatches, fetchTasks, retryTask } from './api';

const { Header, Content, Sider } = Layout;

const statusColor: Record<string, string> = {
  SUBMITTED: 'processing',
  RUNNING: 'processing',
  SUCCESS: 'success',
  PARTIAL_SUCCESS: 'warning',
  FAILED: 'error',
  PENDING: 'default',
  CANCELED: 'default',
};

function App() {
  const [batchList, setBatchList] = useState<BatchRecord[]>([]);
  const [taskList, setTaskList] = useState<TaskRecord[]>([]);
  const [batchLoading, setBatchLoading] = useState(false);
  const [taskLoading, setTaskLoading] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [form] = Form.useForm();

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

  useEffect(() => {
    void loadBatches();
    void loadTasks();
  }, []);

  const batchColumns: ColumnsType<BatchRecord> = useMemo(() => [
    { title: '批次', dataIndex: 'id', width: 90 },
    { title: '地区', dataIndex: 'regionCode', width: 120 },
    { title: '月份', dataIndex: 'periodMonth', width: 120 },
    { title: '状态', dataIndex: 'status', width: 130, render: (value) => <Tag color={statusColor[value] || 'default'}>{value}</Tag> },
    { title: '任务', width: 170, render: (_, row) => `${row.successCount || 0}/${row.totalCount || 0} 成功，${row.failedCount || 0} 失败` },
    { title: '创建时间', dataIndex: 'createdAt', width: 190 },
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
            await retryTask(row.id, 'admin');
            message.success('已发起重试');
            await loadTasks();
          }}
        >重试</Button>
      ),
    },
  ], []);

  const submitCreate = async () => {
    const values = await form.validateFields();
    await createBatch({
      regionCode: values.regionCode,
      siteType: 'default',
      periodMonth: values.periodMonth,
      taxNoList: values.taxNoText.split(/\s|,|，/).map((item: string) => item.trim()).filter(Boolean),
      operator: values.operator || 'admin',
    });
    message.success('批次已提交');
    setCreateOpen(false);
    form.resetFields();
    await Promise.all([loadBatches(), loadTasks()]);
  };

  return (
    <Layout className="app-shell">
      <Sider width={220} className="app-sider">
        <div className="brand">rfpt-mng</div>
        <div className="nav-item active">社保缴费</div>
        <div className="nav-item">企业维护</div>
        <div className="nav-item">地区配置</div>
      </Sider>
      <Layout>
        <Header className="app-header">
          <div>
            <div className="title">社保缴费管理</div>
            <div className="subtitle">按地区、月份发起任务，跟踪电子税务局执行结果</div>
          </div>
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => Promise.all([loadBatches(), loadTasks()])}>刷新</Button>
            <Button type="primary" icon={<RocketOutlined />} onClick={() => setCreateOpen(true)}>发起批次</Button>
          </Space>
        </Header>
        <Content className="app-content">
          <Tabs
            items={[
              { key: 'batches', label: '缴费批次', children: <Table rowKey="id" loading={batchLoading} columns={batchColumns} dataSource={batchList} pagination={false} size="middle" /> },
              { key: 'tasks', label: '任务明细', children: <Table rowKey="id" loading={taskLoading} columns={taskColumns} dataSource={taskList} pagination={false} size="middle" scroll={{ x: 1300 }} /> },
            ]}
          />
        </Content>
      </Layout>
      <Modal title="发起社保缴费批次" open={createOpen} onCancel={() => setCreateOpen(false)} onOk={submitCreate} okText="提交执行">
        <Form layout="vertical" form={form} initialValues={{ regionCode: 'liaoning', operator: 'admin' }}>
          <Form.Item name="regionCode" label="地区" rules={[{ required: true }]}>
            <Select options={[{ label: '辽宁', value: 'liaoning' }, { label: '宁波', value: 'ningbo' }, { label: '深圳', value: 'shenzhen' }]} />
          </Form.Item>
          <Form.Item name="periodMonth" label="费款所属月份" rules={[{ required: true, message: '请输入 yyyy-MM' }]}>
            <Input placeholder="2026-06" />
          </Form.Item>
          <Form.Item name="taxNoText" label="税号" rules={[{ required: true, message: '请输入至少一个税号' }]}>
            <Input.TextArea rows={5} placeholder="多个税号可用换行、空格或逗号分隔" />
          </Form.Item>
          <Form.Item name="operator" label="操作人">
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
}

export default App;
