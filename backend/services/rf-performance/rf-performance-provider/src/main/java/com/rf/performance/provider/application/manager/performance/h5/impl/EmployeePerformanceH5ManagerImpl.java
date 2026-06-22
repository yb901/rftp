package com.rf.performance.provider.application.manager.performance.h5.impl;

import com.rf.performance.provider.application.command.performance.h5.PerformanceH5ConfirmCommand;
import com.rf.performance.provider.application.command.performance.h5.PerformanceH5FeedbackCommand;
import com.rf.performance.provider.application.command.performance.h5.PerformanceH5LoginCommand;
import com.rf.performance.provider.application.command.performance.h5.PerformanceH5SmsSendCommand;
import com.rf.performance.provider.application.manager.performance.h5.EmployeePerformanceH5Manager;
import com.rf.performance.provider.application.port.gateway.auth.captcha.CaptchaGateway;
import com.rf.performance.provider.application.port.gateway.auth.captcha.param.CaptchaVerifyGatewayParam;
import com.rf.performance.provider.application.port.gateway.auth.sms.SmsCodeGateway;
import com.rf.performance.provider.application.port.gateway.auth.sms.param.SmsCodeSendGatewayParam;
import com.rf.performance.provider.application.port.persistence.performance.EmployeePerformanceH5PersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.PerformanceTaskPersistencePort;
import com.rf.performance.provider.application.port.persistence.performance.data.h5.PerformanceConfirmLogData;
import com.rf.performance.provider.application.port.persistence.performance.data.h5.PerformanceFeedbackData;
import com.rf.performance.provider.application.port.persistence.performance.data.h5.PerformanceSmsEvidenceData;
import com.rf.performance.provider.application.port.persistence.performance.record.PerformanceTaskRecord;
import com.rf.performance.provider.application.port.persistence.performance.record.h5.EmployeePerformanceH5Record;
import com.rf.performance.provider.application.result.performance.h5.EmployeePerformanceH5Result;
import com.rf.performance.provider.application.result.performance.h5.PerformanceH5LoginResult;
import com.rf.performance.provider.common.config.PerformanceSmsProperties;
import com.rf.performance.provider.domain.performance.PerformanceConfirmStatus;
import com.rf.performance.provider.domain.performance.PerformanceFeedbackStatus;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 员工绩效 H5 应用编排实现。
 */
@Service
public class EmployeePerformanceH5ManagerImpl implements EmployeePerformanceH5Manager {

    /**
     * 手机号格式。
     */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1\\d{10}$");

    /**
     * 日期时间格式。
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 员工绩效 H5 持久化端口。
     */
    @Resource
    private EmployeePerformanceH5PersistencePort employeePerformanceH5PersistencePort;

    /**
     * 绩效任务持久化端口。
     */
    @Resource
    private PerformanceTaskPersistencePort performanceTaskPersistencePort;

    /**
     * 短信与验证码配置。
     */
    @Resource
    private PerformanceSmsProperties performanceSmsProperties;

    /**
     * 图形验证码校验网关。
     */
    @Resource
    private CaptchaGateway captchaGateway;

    /**
     * 短信验证码发送网关。
     */
    @Resource
    private SmsCodeGateway smsCodeGateway;

    /**
     * 发送短信验证码。
     *
     * @param command 短信发送命令
     * @return 短信验证留痕 ID
     */
    @Override
    public Long sendSmsCode(PerformanceH5SmsSendCommand command) {
        PerformanceH5SmsSendCommand safeCommand = command == null ? new PerformanceH5SmsSendCommand() : command;
        validateMobile(safeCommand.getMobile());
        String scene = StringUtils.defaultIfBlank(safeCommand.getScene(), "LOGIN");
        if (StringUtils.equals(scene, "LOGIN")) {
            verifyCaptcha(safeCommand.getCaptchaTraceId());
        }
        String code = createSmsCode();
        if (!Boolean.TRUE.equals(performanceSmsProperties.getMockEnabled())) {
            sendRealSmsCode(safeCommand.getMobile(), code);
        }
        PerformanceSmsEvidenceData data = new PerformanceSmsEvidenceData();
        data.setMobile(safeCommand.getMobile());
        data.setScene(scene);
        data.setSmsCode(code);
        data.setSmsSendBizId(UUID.randomUUID().toString());
        data.setCaptchaTraceId(safeCommand.getCaptchaTraceId());
        data.setIpAddress(safeCommand.getIpAddress());
        data.setUserAgent(safeCommand.getUserAgent());
        data.setSentAt(LocalDateTime.now());
        return employeePerformanceH5PersistencePort.insertSmsEvidence(data);
    }

    /**
     * 手机号登录。
     *
     * @param command 登录命令
     * @return 登录结果
     */
    @Override
    public PerformanceH5LoginResult login(PerformanceH5LoginCommand command) {
        PerformanceH5LoginCommand safeCommand = command == null ? new PerformanceH5LoginCommand() : command;
        validateMobile(safeCommand.getMobile());
        PerformanceSmsEvidenceData smsEvidence = verifySmsCode(safeCommand.getMobile(), "LOGIN", safeCommand.getSmsCode());
        smsEvidence.setVerifiedAt(LocalDateTime.now());
        employeePerformanceH5PersistencePort.markSmsVerified(smsEvidence);
        PerformanceH5LoginResult result = new PerformanceH5LoginResult();
        result.setMobile(safeCommand.getMobile());
        return result;
    }

    /**
     * 查询当前员工绩效记录。
     *
     * @param mobile 登录手机号
     * @return 员工绩效记录
     */
    @Override
    public List<EmployeePerformanceH5Result> listMine(String mobile) {
        validateMobile(mobile);
        List<EmployeePerformanceH5Record> records = employeePerformanceH5PersistencePort.listByMobile(mobile);
        List<EmployeePerformanceH5Record> enrichedRecords = enrichTaskInfo(records);
        return enrichedRecords.stream()
                .sorted(Comparator.comparing(EmployeePerformanceH5Record::getPeriodStartDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toResult)
                .toList();
    }

    /**
     * 确认绩效。
     *
     * @param command 确认命令
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void confirm(PerformanceH5ConfirmCommand command) {
        PerformanceH5ConfirmCommand safeCommand = command == null ? new PerformanceH5ConfirmCommand() : command;
        validateMobile(safeCommand.getMobile());
        PerformanceSmsEvidenceData smsEvidence = verifySmsCode(safeCommand.getMobile(), "CONFIRM", safeCommand.getSmsCode());
        EmployeePerformanceH5Record record = requireRecord(safeCommand.getRecordId(), safeCommand.getMobile());
        assertConfirmAllowed(record);
        smsEvidence.setVerifiedAt(LocalDateTime.now());
        employeePerformanceH5PersistencePort.markSmsVerified(smsEvidence);
        insertConfirmLog(safeCommand, record, smsEvidence);
        String nextStatus = nextConfirmStatus(record.getConfirmStatus());
        if (!employeePerformanceH5PersistencePort.markConfirmed(record.getId(), record.getMobile(), nextStatus)) {
            throw new BusinessException(ErrorCode.E999002, "绩效确认失败");
        }
    }

    /**
     * 提交绩效反馈。
     *
     * @param command 反馈命令
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void feedback(PerformanceH5FeedbackCommand command) {
        PerformanceH5FeedbackCommand safeCommand = command == null ? new PerformanceH5FeedbackCommand() : command;
        validateMobile(safeCommand.getMobile());
        if (StringUtils.isBlank(safeCommand.getFeedbackContent())) {
            throw new BusinessException(ErrorCode.E999001, "反馈内容不能为空");
        }
        EmployeePerformanceH5Record record = requireRecord(safeCommand.getRecordId(), safeCommand.getMobile());
        assertFeedbackAllowed(record);
        PerformanceFeedbackData data = new PerformanceFeedbackData();
        data.setTaskId(record.getTaskId());
        data.setRecordId(record.getId());
        data.setMobile(record.getMobile());
        data.setFeedbackContent(safeCommand.getFeedbackContent());
        data.setPerformanceSnapshot(record.getPerformance());
        data.setIpAddress(safeCommand.getIpAddress());
        data.setUserAgent(safeCommand.getUserAgent());
        data.setStatus(PerformanceFeedbackStatus.PENDING.getCode());
        if (!employeePerformanceH5PersistencePort.insertFeedback(data)) {
            throw new BusinessException(ErrorCode.E999002, "绩效反馈提交失败");
        }
        if (!employeePerformanceH5PersistencePort.markFeedbackSubmitted(record.getId(), record.getMobile())) {
            throw new BusinessException(ErrorCode.E999002, "绩效反馈状态更新失败");
        }
    }

    /**
     * 自动确认超期绩效记录。
     *
     * @param limit 单次处理上限
     * @return 处理数量
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int autoConfirmExpiredRecords(int limit) {
        int safeLimit = limit <= 0 ? 500 : limit;
        List<Long> firstTaskIds = performanceTaskPersistencePort.listExpiredFirstConfirmTaskIds(safeLimit);
        List<EmployeePerformanceH5Record> firstRecords = employeePerformanceH5PersistencePort
                .listPendingFirstConfirmRecordsByTaskIds(firstTaskIds, safeLimit);
        int firstCount = autoConfirm(firstRecords, "AUTO_CONFIRM", PerformanceConfirmStatus.AUTO_CONFIRMED.getCode());
        int secondLimit = Math.max(safeLimit - firstCount, 0);
        if (secondLimit <= 0) {
            return firstCount;
        }
        List<Long> secondTaskIds = performanceTaskPersistencePort.listExpiredSecondConfirmTaskIds(secondLimit);
        List<EmployeePerformanceH5Record> secondRecords = employeePerformanceH5PersistencePort
                .listPendingSecondConfirmRecordsByTaskIds(secondTaskIds, secondLimit);
        int secondCount = autoConfirm(secondRecords, "SECOND_AUTO_CONFIRM", PerformanceConfirmStatus.SECOND_AUTO_CONFIRMED.getCode());
        return firstCount + secondCount;
    }

    /**
     * 校验手机号。
     *
     * @param mobile 手机号
     */
    private void validateMobile(String mobile) {
        if (StringUtils.isBlank(mobile) || !MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new BusinessException(ErrorCode.E999001, "请输入正确的手机号");
        }
    }

    /**
     * 自动确认指定记录。
     *
     * @param records 员工绩效记录
     * @param confirmType 确认类型
     * @param confirmStatus 确认状态
     * @return 处理数量
     */
    private int autoConfirm(List<EmployeePerformanceH5Record> records, String confirmType, String confirmStatus) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        List<PerformanceConfirmLogData> logs = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (EmployeePerformanceH5Record record : records) {
            ids.add(record.getId());
            PerformanceConfirmLogData log = new PerformanceConfirmLogData();
            log.setTaskId(record.getTaskId());
            log.setRecordId(record.getId());
            log.setMobile(record.getMobile());
            log.setConfirmType(confirmType);
            log.setPerformanceSnapshot(record.getPerformance());
            logs.add(log);
        }
        employeePerformanceH5PersistencePort.batchInsertConfirmLog(logs);
        return employeePerformanceH5PersistencePort.batchMarkAutoConfirmed(ids, confirmStatus);
    }

    /**
     * 校验短信验证码。
     *
     * @param smsCode 短信验证码
     */
    private PerformanceSmsEvidenceData verifySmsCode(String mobile, String scene, String smsCode) {
        PerformanceSmsEvidenceData evidence = employeePerformanceH5PersistencePort.getLatestSmsEvidence(mobile, scene);
        if (evidence == null || evidence.getId() == null) {
            throw new BusinessException(ErrorCode.E999001, "请先获取验证码");
        }
        if (!StringUtils.equals(smsCode, evidence.getSmsCode())) {
            throw new BusinessException(ErrorCode.E999001, "验证码不正确");
        }
        return evidence;
    }

    /**
     * 校验图形验证码。
     *
     * @param captchaVerifyParam 图形验证码参数
     */
    private void verifyCaptcha(String captchaVerifyParam) {
        if (!Boolean.TRUE.equals(performanceSmsProperties.getCaptchaEnabled())) {
            return;
        }
        CaptchaVerifyGatewayParam param = new CaptchaVerifyGatewayParam();
        param.setCaptchaVerifyParam(captchaVerifyParam);
        captchaGateway.verify(param);
    }

    /**
     * 创建短信验证码。
     *
     * @return 短信验证码
     */
    private String createSmsCode() {
        if (Boolean.TRUE.equals(performanceSmsProperties.getMockEnabled())) {
            return performanceSmsProperties.getMockCode();
        }
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    /**
     * 发送真实短信验证码。
     *
     * @param mobile 手机号
     * @param code 验证码
     */
    private void sendRealSmsCode(String mobile, String code) {
        SmsCodeSendGatewayParam param = new SmsCodeSendGatewayParam();
        param.setMobile(mobile);
        param.setCode(code);
        smsCodeGateway.sendCode(param);
    }

    /**
     * 要求员工绩效记录存在。
     *
     * @param recordId 员工绩效记录 ID
     * @param mobile 员工手机号
     * @return 员工绩效记录
     */
    private EmployeePerformanceH5Record requireRecord(Long recordId, String mobile) {
        if (recordId == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效记录ID不能为空");
        }
        EmployeePerformanceH5Record record = employeePerformanceH5PersistencePort.getByIdAndMobile(recordId, mobile);
        if (record == null) {
            throw new BusinessException(ErrorCode.E999001, "绩效记录不存在");
        }
        return enrichTaskInfo(record);
    }

    /**
     * 批量补齐绩效任务信息。
     *
     * @param records 员工绩效记录
     * @return 已补齐任务信息的员工绩效记录
     */
    private List<EmployeePerformanceH5Record> enrichTaskInfo(List<EmployeePerformanceH5Record> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        List<Long> taskIds = records.stream()
                .map(EmployeePerformanceH5Record::getTaskId)
                .distinct()
                .toList();
        List<PerformanceTaskRecord> tasks = performanceTaskPersistencePort.listByIds(taskIds);
        Map<Long, PerformanceTaskRecord> taskMap = new HashMap<>();
        for (PerformanceTaskRecord task : tasks) {
            taskMap.put(task.getId(), task);
        }
        List<EmployeePerformanceH5Record> result = new ArrayList<>();
        for (EmployeePerformanceH5Record record : records) {
            PerformanceTaskRecord task = taskMap.get(record.getTaskId());
            if (task == null) {
                continue;
            }
            fillTaskInfo(record, task);
            result.add(record);
        }
        return result;
    }

    /**
     * 补齐单条绩效任务信息。
     *
     * @param record 员工绩效记录
     * @return 已补齐任务信息的员工绩效记录
     */
    private EmployeePerformanceH5Record enrichTaskInfo(EmployeePerformanceH5Record record) {
        PerformanceTaskRecord task = performanceTaskPersistencePort.getById(record.getTaskId());
        if (task == null || task.getId() == null || Integer.valueOf(1).equals(task.getIsDeleted())) {
            throw new BusinessException(ErrorCode.E999001, "绩效任务不存在");
        }
        fillTaskInfo(record, task);
        return record;
    }

    /**
     * 填充绩效任务信息。
     *
     * @param record 员工绩效记录
     * @param task 绩效任务
     */
    private void fillTaskInfo(EmployeePerformanceH5Record record, PerformanceTaskRecord task) {
        record.setPerformanceDescription(task.getPerformanceDescription());
        record.setPeriodStartDate(task.getPeriodStartDate());
        record.setPeriodEndDate(task.getPeriodEndDate());
        record.setConfirmDeadlineTime(task.getConfirmDeadlineTime());
        record.setSecondConfirmDeadlineTime(task.getSecondConfirmDeadlineTime());
    }

    /**
     * 校验绩效是否允许确认。
     *
     * @param record 员工绩效记录
     */
    private void assertConfirmAllowed(EmployeePerformanceH5Record record) {
        if (PerformanceConfirmStatus.PENDING_CONFIRM.getCode().equals(record.getConfirmStatus())) {
            if (LocalDateTime.now().isAfter(record.getConfirmDeadlineTime())) {
                throw new BusinessException(ErrorCode.E999001, "确认已截止");
            }
            return;
        }
        if (PerformanceConfirmStatus.PENDING_SECOND_CONFIRM.getCode().equals(record.getConfirmStatus())) {
            if (record.getSecondConfirmDeadlineTime() != null
                    && LocalDateTime.now().isAfter(record.getSecondConfirmDeadlineTime())) {
                throw new BusinessException(ErrorCode.E999001, "二次确认已截止");
            }
            return;
        }
        throw new BusinessException(ErrorCode.E999001, "当前状态不允许确认");
    }

    /**
     * 校验绩效是否允许反馈。
     *
     * @param record 员工绩效记录
     */
    private void assertFeedbackAllowed(EmployeePerformanceH5Record record) {
        if (!PerformanceConfirmStatus.PENDING_CONFIRM.getCode().equals(record.getConfirmStatus())
                || !PerformanceFeedbackStatus.NONE.getCode().equals(record.getFeedbackStatus())) {
            throw new BusinessException(ErrorCode.E999001, "当前状态不允许反馈");
        }
        if (LocalDateTime.now().isAfter(record.getConfirmDeadlineTime())) {
            throw new BusinessException(ErrorCode.E999001, "反馈已截止");
        }
    }

    /**
     * 构建已验证短信凭证。
     *
     * @param mobile 手机号
     * @param scene 短信场景
     * @param ipAddress 请求 IP
     * @param userAgent 浏览器 User-Agent
     * @return 短信验证留痕写入数据
     */
    private PerformanceSmsEvidenceData verifiedSmsEvidence(String mobile, String scene, String ipAddress, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        PerformanceSmsEvidenceData data = new PerformanceSmsEvidenceData();
        data.setMobile(mobile);
        data.setScene(scene);
        data.setSmsSendBizId(UUID.randomUUID().toString());
        data.setIpAddress(ipAddress);
        data.setUserAgent(userAgent);
        data.setSentAt(now);
        data.setVerifiedAt(now);
        return data;
    }

    /**
     * 新增确认留痕。
     *
     * @param command 确认命令
     * @param record 员工绩效记录
     * @param smsEvidence 短信验证留痕
     */
    private void insertConfirmLog(PerformanceH5ConfirmCommand command,
                                  EmployeePerformanceH5Record record,
                                  PerformanceSmsEvidenceData smsEvidence) {
        PerformanceConfirmLogData data = new PerformanceConfirmLogData();
        data.setTaskId(record.getTaskId());
        data.setRecordId(record.getId());
        data.setMobile(record.getMobile());
        data.setConfirmType(confirmType(record.getConfirmStatus()));
        data.setPerformanceSnapshot(record.getPerformance());
        data.setSmsEvidenceId(smsEvidence.getId());
        data.setSmsSendBizId(smsEvidence.getSmsSendBizId());
        data.setSmsVerifiedAt(smsEvidence.getVerifiedAt());
        data.setIpAddress(command.getIpAddress());
        data.setUserAgent(command.getUserAgent());
        employeePerformanceH5PersistencePort.insertConfirmLog(data);
    }

    /**
     * 获取确认类型。
     *
     * @param confirmStatus 当前确认状态
     * @return 确认类型
     */
    private String confirmType(String confirmStatus) {
        if (PerformanceConfirmStatus.PENDING_SECOND_CONFIRM.getCode().equals(confirmStatus)) {
            return "SECOND_CONFIRM";
        }
        return "FIRST_CONFIRM";
    }

    /**
     * 获取确认后状态。
     *
     * @param confirmStatus 当前确认状态
     * @return 确认后状态
     */
    private String nextConfirmStatus(String confirmStatus) {
        if (PerformanceConfirmStatus.PENDING_SECOND_CONFIRM.getCode().equals(confirmStatus)) {
            return PerformanceConfirmStatus.SECOND_CONFIRMED.getCode();
        }
        return PerformanceConfirmStatus.CONFIRMED.getCode();
    }

    /**
     * 转换员工绩效 H5 返回对象。
     *
     * @param record 员工绩效记录
     * @return 员工绩效 H5 返回对象
     */
    private EmployeePerformanceH5Result toResult(EmployeePerformanceH5Record record) {
        EmployeePerformanceH5Result result = new EmployeePerformanceH5Result();
        result.setId(record.getId());
        result.setPerformanceDescription(record.getPerformanceDescription());
        result.setPeriodText(record.getPeriodStartDate() + " 至 " + record.getPeriodEndDate());
        result.setPerformance(record.getPerformance());
        result.setConfirmStatus(record.getConfirmStatus());
        result.setConfirmStatusText(confirmStatusText(record.getConfirmStatus()));
        result.setFeedbackStatus(record.getFeedbackStatus());
        result.setConfirmDeadlineTime(formatDateTime(record.getConfirmDeadlineTime()));
        return result;
    }

    /**
     * 转换确认状态文案。
     *
     * @param status 确认状态编码
     * @return 确认状态文案
     */
    private String confirmStatusText(String status) {
        for (PerformanceConfirmStatus item : PerformanceConfirmStatus.values()) {
            if (item.getCode().equals(status)) {
                return item.getName();
            }
        }
        return status;
    }

    /**
     * 格式化日期时间。
     *
     * @param dateTime 日期时间
     * @return 日期时间文本
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return DATE_TIME_FORMATTER.format(dateTime);
    }
}
