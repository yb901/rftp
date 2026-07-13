package com.rf.mng.provider.interfaces.socialsecurity;

import com.rf.mng.provider.application.command.socialsecurity.config.SocialSecurityEnterpriseSaveCommand;
import com.rf.mng.provider.application.command.socialsecurity.config.SocialSecurityRegionSiteSaveCommand;
import com.rf.mng.provider.application.manager.socialsecurity.config.SocialSecurityConfigManager;
import com.rf.mng.provider.application.query.socialsecurity.config.SocialSecurityEnterpriseQuery;
import com.rf.mng.provider.application.query.socialsecurity.config.SocialSecurityRegionSiteQuery;
import com.rf.mng.provider.application.result.socialsecurity.config.SocialSecurityEnterpriseResult;
import com.rf.mng.provider.application.result.socialsecurity.config.SocialSecurityRegionSiteResult;
import com.rf.mng.provider.common.auth.MngModule;
import com.rf.mng.provider.common.auth.MngPermission;
import com.rf.mng.provider.interfaces.socialsecurity.param.config.SocialSecurityEnterpriseSaveCtrlParam;
import com.rf.mng.provider.interfaces.socialsecurity.param.config.SocialSecurityRegionSiteSaveCtrlParam;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.zy.common.core.enums.ErrorCode;
import com.zy.common.core.exception.BusinessException;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.bo.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 社保缴费配置管理接口。
 */
@RestController
@RequestMapping("/api/social-security-config")
@MngPermission(MngModule.SOCIAL_SECURITY)
public class SocialSecurityConfigController {

    /** 社保缴费配置管理器。 */
    @Resource
    private SocialSecurityConfigManager socialSecurityConfigManager;

    /** 分页查询企业配置。 */
    @GetMapping("/enterprises")
    public Result<PageResp<SocialSecurityEnterpriseResult>> pageEnterprise(SocialSecurityEnterpriseQuery query) {
        return Result.success(socialSecurityConfigManager.pageEnterprise(query));
    }

    /** 保存企业配置。 */
    @PostMapping("/enterprises")
    public Result<SocialSecurityEnterpriseResult> saveEnterprise(@RequestBody SocialSecurityEnterpriseSaveCtrlParam param) {
        SocialSecurityEnterpriseSaveCommand command = new SocialSecurityEnterpriseSaveCommand();
        BeanUtils.copyProperties(param, command);
        return Result.success(socialSecurityConfigManager.saveEnterprise(command));
    }

    /**
     * 批量导入社保缴费企业。Excel 首行是表头，列顺序为税号、企业名称、地区编码、社保账户名。
     */
    @PostMapping(value = "/enterprises/import", consumes = "multipart/form-data")
    public Result<Integer> importEnterprise(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "请选择企业导入文件");
        }
        List<SocialSecurityEnterpriseSaveCommand> commands = parseEnterpriseImportFile(file);
        if (commands.isEmpty()) {
            throw new BusinessException(ErrorCode.E999001, "导入文件没有有效企业数据");
        }
        return Result.success(socialSecurityConfigManager.importEnterprises(commands));
    }

    /** 删除企业配置。 */
    @PostMapping("/enterprises/{id}/delete")
    public Result<Void> deleteEnterprise(@PathVariable Long id) {
        socialSecurityConfigManager.deleteEnterprise(id);
        return Result.success();
    }

    private List<SocialSecurityEnterpriseSaveCommand> parseEnterpriseImportFile(MultipartFile file) {
        List<SocialSecurityEnterpriseSaveCommand> commands = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> row, AnalysisContext context) {
                    String taxNo = trim(row.get(0));
                    if (taxNo == null) return;
                    SocialSecurityEnterpriseSaveCommand command = new SocialSecurityEnterpriseSaveCommand();
                    command.setTaxNo(taxNo);
                    command.setEnterpriseName(requiredCell(row.get(1), "企业名称", context.readRowHolder().getRowIndex() + 1));
                    command.setRegionCode(requiredCell(row.get(2), "地区编码", context.readRowHolder().getRowIndex() + 1));
                    command.setSecurityAccountName(requiredCell(row.get(3), "社保账户名", context.readRowHolder().getRowIndex() + 1));
                    command.setStatus("active");
                    commands.add(command);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().headRowNumber(1).doRead();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.E999001, "读取企业导入文件失败");
        }
        return commands;
    }

    private String requiredCell(String value, String columnName, int rowNo) {
        String result = trim(value);
        if (result == null) throw new BusinessException(ErrorCode.E999001, "第" + rowNo + "行缺少" + columnName);
        return result;
    }

    private String trim(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /** 分页查询地区站点配置。 */
    @GetMapping("/region-sites")
    public Result<PageResp<SocialSecurityRegionSiteResult>> pageRegionSite(SocialSecurityRegionSiteQuery query) {
        return Result.success(socialSecurityConfigManager.pageRegionSite(query));
    }

    /** 保存地区站点配置。 */
    @PostMapping("/region-sites")
    public Result<SocialSecurityRegionSiteResult> saveRegionSite(@RequestBody SocialSecurityRegionSiteSaveCtrlParam param) {
        SocialSecurityRegionSiteSaveCommand command = new SocialSecurityRegionSiteSaveCommand();
        BeanUtils.copyProperties(param, command);
        return Result.success(socialSecurityConfigManager.saveRegionSite(command));
    }

    /** 删除地区站点配置。 */
    @PostMapping("/region-sites/{id}/delete")
    public Result<Void> deleteRegionSite(@PathVariable Long id) {
        socialSecurityConfigManager.deleteRegionSite(id);
        return Result.success();
    }
}
