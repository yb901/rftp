package com.rf.mng.provider.application.manager.socialsecurity.config.impl;

import com.rf.mng.provider.application.command.socialsecurity.config.SocialSecurityEnterpriseSaveCommand;
import com.rf.mng.provider.application.command.socialsecurity.config.SocialSecurityRegionSiteSaveCommand;
import com.rf.mng.provider.application.manager.socialsecurity.config.SocialSecurityConfigManager;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.SocialSecurityConfigPersistencePort;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.data.SocialSecurityEnterpriseData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.data.SocialSecurityRegionSiteData;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.record.SocialSecurityEnterpriseRecord;
import com.rf.mng.provider.application.port.persistence.socialsecurity.config.record.SocialSecurityRegionSiteRecord;
import com.rf.mng.provider.application.query.socialsecurity.config.SocialSecurityEnterpriseQuery;
import com.rf.mng.provider.application.query.socialsecurity.config.SocialSecurityRegionSiteQuery;
import com.rf.mng.provider.application.result.socialsecurity.config.SocialSecurityEnterpriseResult;
import com.rf.mng.provider.application.result.socialsecurity.config.SocialSecurityRegionSiteResult;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 社保缴费配置管理器实现。
 */
@Service
public class SocialSecurityConfigManagerImpl implements SocialSecurityConfigManager {

    /** 社保缴费配置持久化端口。 */
    @Resource
    private SocialSecurityConfigPersistencePort configPersistencePort;

    @Override
    public PageResp<SocialSecurityEnterpriseResult> pageEnterprise(SocialSecurityEnterpriseQuery query) {
        long total = configPersistencePort.countEnterprise(query);
        return PageResp.of(configPersistencePort.pageEnterprise(query).stream().map(this::toEnterpriseResult).toList(),
                total, query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "robotTransactionManager")
    public SocialSecurityEnterpriseResult saveEnterprise(SocialSecurityEnterpriseSaveCommand command) {
        validateEnterprise(command);
        SocialSecurityEnterpriseRecord duplicate = configPersistencePort.findEnterpriseByTaxNo(command.getTaxNo().trim());
        if (duplicate != null && !duplicate.getId().equals(command.getId())) {
            throw new BusinessException(ErrorCode.E999001, "税号已存在");
        }
        SocialSecurityEnterpriseData data = new SocialSecurityEnterpriseData();
        data.setId(command.getId());
        data.setTaxNo(command.getTaxNo().trim());
        data.setEnterpriseName(command.getEnterpriseName().trim());
        data.setRegionCode(command.getRegionCode().trim());
        data.setSecurityAccountName(StringUtils.trimToNull(command.getSecurityAccountName()));
        data.setStatus(StringUtils.defaultIfBlank(command.getStatus(), "active"));
        data.setRemark(StringUtils.trimToNull(command.getRemark()));
        if (data.getId() == null) {
            configPersistencePort.insertEnterprise(data);
        } else {
            configPersistencePort.updateEnterprise(data);
        }
        return toEnterpriseResult(configPersistencePort.findEnterpriseById(data.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "robotTransactionManager")
    public int importEnterprises(List<SocialSecurityEnterpriseSaveCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return 0;
        }
        for (SocialSecurityEnterpriseSaveCommand command : commands) {
            validateEnterprise(command);
            SocialSecurityEnterpriseRecord existed = configPersistencePort.findEnterpriseByTaxNo(command.getTaxNo().trim());
            SocialSecurityEnterpriseData data = new SocialSecurityEnterpriseData();
            data.setId(existed == null ? null : existed.getId());
            data.setTaxNo(command.getTaxNo().trim());
            data.setEnterpriseName(command.getEnterpriseName().trim());
            data.setRegionCode(command.getRegionCode().trim());
            data.setSecurityAccountName(StringUtils.trimToNull(command.getSecurityAccountName()));
            data.setStatus(StringUtils.defaultIfBlank(command.getStatus(), "active"));
            data.setRemark(StringUtils.trimToNull(command.getRemark()));
            if (data.getId() == null) {
                configPersistencePort.insertEnterprise(data);
            } else {
                configPersistencePort.updateEnterprise(data);
            }
        }
        return commands.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "robotTransactionManager")
    public void deleteEnterprise(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.E999001, "企业编号不能为空");
        }
        configPersistencePort.deleteEnterpriseById(id);
    }

    @Override
    public PageResp<SocialSecurityRegionSiteResult> pageRegionSite(SocialSecurityRegionSiteQuery query) {
        long total = configPersistencePort.countRegionSite(query);
        return PageResp.of(configPersistencePort.pageRegionSite(query).stream().map(this::toRegionSiteResult).toList(),
                total, query.getPage(), query.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "robotTransactionManager")
    public SocialSecurityRegionSiteResult saveRegionSite(SocialSecurityRegionSiteSaveCommand command) {
        validateRegionSite(command);
        String siteType = StringUtils.defaultIfBlank(command.getSiteType(), "default");
        SocialSecurityRegionSiteRecord duplicate = configPersistencePort.findRegionSiteByCodeAndType(command.getRegionCode().trim(), siteType);
        if (duplicate != null && !duplicate.getId().equals(command.getId())) {
            throw new BusinessException(ErrorCode.E999001, "地区站点已存在");
        }
        SocialSecurityRegionSiteData data = new SocialSecurityRegionSiteData();
        data.setId(command.getId());
        data.setRegionCode(command.getRegionCode().trim());
        data.setSiteType(siteType);
        data.setEtaxEntryUrl(command.getEtaxEntryUrl().trim());
        data.setTpassBaseUrl(StringUtils.trimToNull(command.getTpassBaseUrl()));
        data.setLoginSuccessUrl(StringUtils.trimToNull(command.getLoginSuccessUrl()));
        data.setLoginButtonText(StringUtils.defaultIfBlank(command.getLoginButtonText(), "登录"));
        data.setGt4BaseUrl(StringUtils.trimToNull(command.getGt4BaseUrl()));
        data.setDeclarationQueryUrl(StringUtils.trimToNull(command.getDeclarationQueryUrl()));
        data.setDeclarationQueryMenuId(StringUtils.trimToNull(command.getDeclarationQueryMenuId()));
        data.setSocialSecurityPaymentFlowJson(StringUtils.trimToNull(command.getSocialSecurityPaymentFlowJson()));
        data.setStatus(StringUtils.defaultIfBlank(command.getStatus(), "active"));
        data.setRemark(StringUtils.trimToNull(command.getRemark()));
        if (data.getId() == null) {
            configPersistencePort.insertRegionSite(data);
        } else {
            configPersistencePort.updateRegionSite(data);
        }
        return toRegionSiteResult(configPersistencePort.findRegionSiteById(data.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "robotTransactionManager")
    public void deleteRegionSite(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.E999001, "地区站点编号不能为空");
        }
        configPersistencePort.deleteRegionSiteById(id);
    }

    private void validateEnterprise(SocialSecurityEnterpriseSaveCommand command) {
        if (StringUtils.isBlank(command.getTaxNo())) {
            throw new BusinessException(ErrorCode.E999001, "税号不能为空");
        }
        if (StringUtils.isBlank(command.getEnterpriseName())) {
            throw new BusinessException(ErrorCode.E999001, "企业名称不能为空");
        }
        if (StringUtils.isBlank(command.getRegionCode())) {
            throw new BusinessException(ErrorCode.E999001, "地区编码不能为空");
        }
    }

    private void validateRegionSite(SocialSecurityRegionSiteSaveCommand command) {
        if (StringUtils.isBlank(command.getRegionCode())) {
            throw new BusinessException(ErrorCode.E999001, "地区编码不能为空");
        }
        if (StringUtils.isBlank(command.getEtaxEntryUrl())) {
            throw new BusinessException(ErrorCode.E999001, "电子税务局入口不能为空");
        }
    }

    private SocialSecurityEnterpriseResult toEnterpriseResult(SocialSecurityEnterpriseRecord record) {
        SocialSecurityEnterpriseResult result = new SocialSecurityEnterpriseResult();
        result.setId(record.getId());
        result.setTaxNo(record.getTaxNo());
        result.setEnterpriseName(record.getEnterpriseName());
        result.setRegionCode(record.getRegionCode());
        result.setSecurityAccountName(record.getSecurityAccountName());
        result.setStatus(record.getStatus());
        result.setRemark(record.getRemark());
        result.setGmtCreate(record.getGmtCreate());
        result.setGmtModified(record.getGmtModified());
        return result;
    }

    private SocialSecurityRegionSiteResult toRegionSiteResult(SocialSecurityRegionSiteRecord record) {
        SocialSecurityRegionSiteResult result = new SocialSecurityRegionSiteResult();
        result.setId(record.getId());
        result.setRegionCode(record.getRegionCode());
        result.setSiteType(record.getSiteType());
        result.setEtaxEntryUrl(record.getEtaxEntryUrl());
        result.setTpassBaseUrl(record.getTpassBaseUrl());
        result.setLoginSuccessUrl(record.getLoginSuccessUrl());
        result.setLoginButtonText(record.getLoginButtonText());
        result.setGt4BaseUrl(record.getGt4BaseUrl());
        result.setDeclarationQueryUrl(record.getDeclarationQueryUrl());
        result.setDeclarationQueryMenuId(record.getDeclarationQueryMenuId());
        result.setSocialSecurityPaymentFlowJson(record.getSocialSecurityPaymentFlowJson());
        result.setStatus(record.getStatus());
        result.setRemark(record.getRemark());
        result.setGmtCreate(record.getGmtCreate());
        result.setGmtModified(record.getGmtModified());
        return result;
    }
}
