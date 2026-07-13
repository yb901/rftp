package com.rf.mng.provider.application.manager.socialsecurity.config;

import com.rf.mng.provider.application.command.socialsecurity.config.SocialSecurityEnterpriseSaveCommand;
import com.rf.mng.provider.application.command.socialsecurity.config.SocialSecurityRegionSiteSaveCommand;
import com.rf.mng.provider.application.query.socialsecurity.config.SocialSecurityEnterpriseQuery;
import com.rf.mng.provider.application.query.socialsecurity.config.SocialSecurityRegionSiteQuery;
import com.rf.mng.provider.application.result.socialsecurity.config.SocialSecurityEnterpriseResult;
import com.rf.mng.provider.application.result.socialsecurity.config.SocialSecurityRegionSiteResult;

import java.util.List;
import com.zy.common.core.bo.PageResp;

/**
 * 社保缴费配置管理器。
 */
public interface SocialSecurityConfigManager {

    PageResp<SocialSecurityEnterpriseResult> pageEnterprise(SocialSecurityEnterpriseQuery query);

    SocialSecurityEnterpriseResult saveEnterprise(SocialSecurityEnterpriseSaveCommand command);

    /** 批量导入企业配置，税号相同则覆盖更新。 */
    int importEnterprises(List<SocialSecurityEnterpriseSaveCommand> commands);

    void deleteEnterprise(Long id);

    PageResp<SocialSecurityRegionSiteResult> pageRegionSite(SocialSecurityRegionSiteQuery query);

    SocialSecurityRegionSiteResult saveRegionSite(SocialSecurityRegionSiteSaveCommand command);

    void deleteRegionSite(Long id);
}
