import { Button, Dialog, Empty, Form, Input, List, NavBar, Space, Tag, TextArea, Toast } from 'antd-mobile';
import { useCallback, useEffect, useRef, useState, type MouseEvent } from 'react';
import { performanceApi } from '../features/performance/api';
import type { EmployeePerformance, PerformanceCaptchaConfig } from '../features/performance/types';

interface AliyunCaptchaOptions {
  SceneId?: string;
  mode?: string;
  element?: string;
  button?: string;
  captchaVerifyCallback?: (captchaVerifyParam: string) => Promise<CaptchaVerifyCallbackResult> | CaptchaVerifyCallbackResult;
  onBizResultCallback?: () => void;
  slideStyle?: { width: number; height: number };
  language?: string;
}

interface CaptchaVerifyCallbackResult {
  captchaResult: boolean;
  bizResult: boolean;
}

declare global {
  interface Window {
    AliyunCaptchaConfig?: { region?: string; prefix?: string };
    initAliyunCaptcha?: (options: AliyunCaptchaOptions) => void;
  }
}

const LOGIN_SCENE = 'LOGIN';
const CONFIRM_SCENE = 'CONFIRM';
const CAPTCHA_ELEMENT_ID = 'aliyun-captcha-element';
const CAPTCHA_BUTTON_ID = 'aliyun-captcha-button';
const captchaScriptPromises = new Map<string, Promise<void>>();

/**
 * 员工绩效首页。
 */
export function PerformanceHomePage() {
  const [mobile, setMobile] = useState('');
  const [smsCode, setSmsCode] = useState('');
  const [loginMobile, setLoginMobile] = useState('');
  const [records, setRecords] = useState<EmployeePerformance[]>([]);
  const [loading, setLoading] = useState(false);
  const [smsSending, setSmsSending] = useState(false);
  const [captchaConfig, setCaptchaConfig] = useState<PerformanceCaptchaConfig | null>(null);
  const [captchaReady, setCaptchaReady] = useState(false);
  const [captchaInitError, setCaptchaInitError] = useState('');
  const mobileRef = useRef('');

  useEffect(() => {
    performanceApi.me().then((data) => {
      setLoginMobile(data.mobile);
      setMobile(data.mobile);
      loadRecords();
    }).catch(() => {
      setLoginMobile('');
    });
  }, []);

  useEffect(() => {
    mobileRef.current = mobile;
  }, [mobile]);

  const requestLoginSms = useCallback(async (captchaVerifyParam?: string) => {
    await performanceApi.sendSmsCode({ mobile: mobileRef.current, scene: LOGIN_SCENE, captchaTraceId: captchaVerifyParam });
    Toast.show({ icon: 'success', content: '验证码已发送' });
  }, []);

  const handleCaptchaVerified = useCallback(async (captchaVerifyParam: string) => {
    setSmsSending(true);
    try {
      await requestLoginSms(captchaVerifyParam);
    } finally {
      setSmsSending(false);
    }
  }, [requestLoginSms]);

  useEffect(() => {
    let canceled = false;
    performanceApi
      .captchaConfig()
      .then(async (config) => {
        if (canceled) {
          return;
        }
        setCaptchaConfig(config);
        if (!config?.enabled) {
          setCaptchaReady(true);
          return;
        }
        validateCaptchaConfig(config);
        window.AliyunCaptchaConfig = {
          region: config.region || 'cn',
          prefix: config.prefix,
        };
        await loadCaptchaScript(config.jsUrl || '');
        if (!window.initAliyunCaptcha) {
          throw new Error('验证码组件加载失败');
        }
        if (!canceled) {
          initCaptcha(config, handleCaptchaVerified);
          setCaptchaReady(true);
        }
      })
      .catch((error) => {
        if (canceled) {
          return;
        }
        const message = error instanceof Error ? error.message : '验证码组件加载失败';
        setCaptchaInitError(message);
        setCaptchaReady(false);
      });
    return () => {
      canceled = true;
    };
  }, [handleCaptchaVerified]);

  const loadRecords = async () => {
    const data = await performanceApi.listMine();
    setRecords(data);
  };

  const sendLoginSms = async () => {
    if (!/^1\d{10}$/.test(mobile)) {
      Toast.show({ icon: 'fail', content: '请输入正确的手机号' });
      return;
    }
    if (captchaInitError) {
      Toast.show({ icon: 'fail', content: captchaInitError });
      return;
    }
    if (!captchaReady) {
      Toast.show({ icon: 'fail', content: '验证码初始化中，请稍后重试' });
      return;
    }
    if (captchaConfig?.enabled) {
      return;
    }
    setSmsSending(true);
    try {
      await requestLoginSms();
    } finally {
      setSmsSending(false);
    }
  };

  const handleGetCodeClickCapture = (event: MouseEvent<HTMLButtonElement>) => {
    if (!captchaConfig?.enabled) {
      return;
    }
    if (!/^1\d{10}$/.test(mobile) || !captchaReady || captchaInitError) {
      event.preventDefault();
      event.stopPropagation();
      event.nativeEvent.stopImmediatePropagation();
    }
  };

  const login = async () => {
    setLoading(true);
    try {
      const result = await performanceApi.login({ mobile, smsCode });
      setLoginMobile(result.mobile);
      await loadRecords();
      Toast.show({ icon: 'success', content: '登录成功' });
    } finally {
      setLoading(false);
    }
  };

  const confirmRecord = async (record: EmployeePerformance) => {
    await performanceApi.sendSmsCode({ mobile: loginMobile, scene: CONFIRM_SCENE });
    Toast.show({ icon: 'success', content: '确认验证码已发送' });
    const confirmSmsCode = await promptSmsCode('请输入确认验证码');
    if (!confirmSmsCode) {
      return;
    }
    await performanceApi.confirm(record.id, { smsCode: confirmSmsCode });
    Toast.show({ icon: 'success', content: '已确认' });
    await loadRecords();
  };

  const feedbackRecord = async (record: EmployeePerformance) => {
    const feedbackContent = await promptFeedback();
    if (!feedbackContent) {
      return;
    }
    await performanceApi.feedback(record.id, { feedbackContent });
    Toast.show({ icon: 'success', content: '反馈已提交' });
    await loadRecords();
  };

  if (!loginMobile) {
    return (
      <div className="page-shell">
        <NavBar back={null}>员工绩效确认</NavBar>
        <main className="content">
          <section className="login-panel">
            <h1>手机号登录</h1>
            <p>请输入已导入绩效记录的手机号。</p>
            <Form layout="vertical" footer={
              <Button block color="primary" loading={loading} onClick={login}>
                登录
              </Button>
            }>
              <Form.Item label="手机号">
                <Input value={mobile} onChange={setMobile} placeholder="请输入手机号" type="tel" maxLength={11} />
              </Form.Item>
              <Form.Item label="短信验证码">
                <div className="sms-row">
                  <Input value={smsCode} onChange={setSmsCode} placeholder="请输入验证码" maxLength={6} />
                  <button
                    id={CAPTCHA_BUTTON_ID}
                    className={buttonClassName(!captchaReady && !captchaInitError, smsSending)}
                    type="button"
                    disabled={!captchaReady && !captchaInitError}
                    onClickCapture={handleGetCodeClickCapture}
                    onClick={sendLoginSms}
                  >
                    获取验证码
                  </button>
                </div>
              </Form.Item>
            </Form>
            <div id={CAPTCHA_ELEMENT_ID} />
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <NavBar back={null}>员工绩效确认</NavBar>
      <main className="content">
        <section className="summary-band">
          <div>
            <p className="summary-label">当前待处理</p>
            <h1>{records.length}</h1>
          </div>
          <span>请在截止时间前完成确认或反馈</span>
        </section>

        {records.length === 0 ? (
          <Empty description="暂无绩效记录" />
        ) : (
          <List className="record-list">
            {records.map((record) => (
              <List.Item
                key={record.id}
                description={
                  <div className="record-meta">
                    <span>{record.periodText}</span>
                    <span>截止：{record.confirmDeadlineTime}</span>
                  </div>
                }
                extra={<Tag color="warning">{record.confirmStatusText}</Tag>}
              >
                <div className="record-title">{record.performanceDescription}</div>
                <div className="record-score">绩效：{record.performance}</div>
                <div className="record-actions">
                  <Button size="mini" color="primary" disabled={!canConfirm(record)} onClick={() => confirmRecord(record)}>
                    确认
                  </Button>
                  <Button size="mini" disabled={!canFeedback(record)} onClick={() => feedbackRecord(record)}>
                    反馈
                  </Button>
                </div>
              </List.Item>
            ))}
          </List>
        )}
      </main>
    </div>
  );
}

function canConfirm(record: EmployeePerformance) {
  return record.confirmStatus === 'PENDING_CONFIRM' || record.confirmStatus === 'PENDING_SECOND_CONFIRM';
}

function canFeedback(record: EmployeePerformance) {
  return record.confirmStatus === 'PENDING_CONFIRM' && record.feedbackStatus === 'NONE';
}

async function promptSmsCode(title: string) {
  let value = '';
  const confirmed = await Dialog.confirm({
    title,
    content: <Input placeholder="请输入短信验证码" maxLength={6} onChange={(text) => { value = text; }} />,
    confirmText: '确认',
    cancelText: '取消',
  });
  return confirmed ? value : '';
}

async function promptFeedback() {
  let value = '';
  const confirmed = await Dialog.confirm({
    title: '提交绩效反馈',
    content: (
      <Space direction="vertical" block>
        <TextArea placeholder="请输入反馈内容" rows={4} maxLength={500} onChange={(text) => { value = text; }} />
      </Space>
    ),
    confirmText: '提交',
    cancelText: '取消',
  });
  return confirmed ? value : '';
}

function initCaptcha(config: PerformanceCaptchaConfig, onVerified: (captchaVerifyParam: string) => Promise<void>) {
  validateCaptchaConfig(config);
  if (!window.initAliyunCaptcha) {
    throw new Error('验证码组件加载失败');
  }
  window.initAliyunCaptcha({
    SceneId: config.sceneId,
    mode: 'popup',
    element: `#${CAPTCHA_ELEMENT_ID}`,
    button: `#${CAPTCHA_BUTTON_ID}`,
    captchaVerifyCallback: async (captchaVerifyParam: string) => {
      try {
        await onVerified(captchaVerifyParam);
        return { captchaResult: true, bizResult: true };
      } catch {
        return { captchaResult: true, bizResult: false };
      }
    },
    onBizResultCallback: () => undefined,
    slideStyle: { width: 320, height: 40 },
    language: config.language || 'cn',
  });
}

function validateCaptchaConfig(config: PerformanceCaptchaConfig) {
  if (!config.jsUrl || !config.prefix || !config.sceneId) {
    throw new Error('验证码配置不完整');
  }
}

function buttonClassName(disabled: boolean, loading: boolean) {
  return [
    'adm-button',
    'adm-button-primary',
    'adm-button-fill-none',
    'adm-button-small',
    'adm-button-shape-default',
    disabled ? 'adm-button-disabled' : '',
    loading ? 'adm-button-loading' : '',
  ]
    .filter(Boolean)
    .join(' ');
}

function loadCaptchaScript(src: string) {
  const cached = captchaScriptPromises.get(src);
  if (cached) {
    return cached;
  }
  const promise = new Promise<void>((resolve, reject) => {
    const exists = document.querySelector<HTMLScriptElement>(`script[src="${src}"]`);
    if (exists) {
      if (window.initAliyunCaptcha) {
        resolve();
      } else {
        exists.addEventListener('load', () => resolve(), { once: true });
        exists.addEventListener('error', () => reject(new Error('验证码组件加载失败')), { once: true });
      }
      return;
    }
    const script = document.createElement('script');
    script.src = src;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('验证码组件加载失败'));
    document.head.appendChild(script);
  });
  captchaScriptPromises.set(src, promise);
  return promise;
}
