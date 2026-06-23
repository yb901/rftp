package com.rf.mng.provider.interfaces.admin;

import cn.hutool.core.bean.BeanUtil;
import com.rf.mng.provider.application.command.admin.AdminIdCommand;
import com.rf.mng.provider.application.command.admin.AdminSaveCommand;
import com.rf.mng.provider.application.manager.admin.AdminManager;
import com.rf.mng.provider.application.query.admin.AdminPageQuery;
import com.rf.mng.provider.application.result.admin.AdminResult;
import com.rf.mng.provider.common.auth.MngModule;
import com.rf.mng.provider.common.auth.MngPermission;
import com.rf.mng.provider.interfaces.admin.param.AdminIdCtrlParam;
import com.rf.mng.provider.interfaces.admin.param.AdminPageCtrlParam;
import com.rf.mng.provider.interfaces.admin.param.AdminSaveCtrlParam;
import com.rf.mng.provider.interfaces.admin.vo.AdminTotpVo;
import com.rf.mng.provider.interfaces.admin.vo.AdminVo;
import com.zy.common.core.bo.PageResp;
import com.zy.common.core.bo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * 系统管理员接口。
 */
@RestController
@RequestMapping("/mng/admin")
@MngPermission(MngModule.ADMIN)
public class AdminController {

    /** 管理员应用管理器。 */
    @Resource
    private AdminManager adminManager;

    /**
     * 分页查询管理员。
     *
     * @param param 查询参数
     * @return 管理员分页
     */
    @GetMapping("/list")
    public Result<PageResp<AdminVo>> list(AdminPageCtrlParam param) {
        AdminPageQuery query = BeanUtil.copyProperties(param, AdminPageQuery.class);
        PageResp<AdminResult> resultPage = adminManager.page(query);
        return Result.success(toVoPage(resultPage));
    }

    /**
     * 新增管理员。
     *
     * @param param 保存参数
     * @return 管理员信息
     */
    @PostMapping("/save")
    public Result<AdminVo> save(@RequestBody AdminSaveCtrlParam param) {
        AdminSaveCommand command = BeanUtil.copyProperties(param, AdminSaveCommand.class);
        AdminResult result = adminManager.save(command);
        return Result.success(BeanUtil.copyProperties(result, AdminVo.class));
    }

    /**
     * 更新管理员。
     *
     * @param param 保存参数
     * @return 管理员信息
     */
    @PostMapping("/update")
    public Result<AdminVo> update(@RequestBody AdminSaveCtrlParam param) {
        AdminSaveCommand command = BeanUtil.copyProperties(param, AdminSaveCommand.class);
        AdminResult result = adminManager.update(command);
        return Result.success(BeanUtil.copyProperties(result, AdminVo.class));
    }

    /**
     * 删除管理员。
     *
     * @param param ID 参数
     * @return 空结果
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody AdminIdCtrlParam param) {
        adminManager.delete(toIdCommand(param));
        return Result.success();
    }

    /**
     * 生成 TOTP 密钥。
     *
     * @param param ID 参数
     * @return TOTP 密钥
     */
    @PostMapping("/generateTotp")
    public Result<AdminTotpVo> generateTotp(@RequestBody AdminIdCtrlParam param) {
        return Result.success(BeanUtil.copyProperties(adminManager.generateTotp(toIdCommand(param)), AdminTotpVo.class));
    }

    /**
     * 禁用 TOTP。
     *
     * @param param ID 参数
     * @return 空结果
     */
    @PostMapping("/disableTotp")
    public Result<Void> disableTotp(@RequestBody AdminIdCtrlParam param) {
        adminManager.disableTotp(toIdCommand(param));
        return Result.success();
    }

    /**
     * 转换 ID 命令。
     *
     * @param param ID 参数
     * @return ID 命令
     */
    private AdminIdCommand toIdCommand(AdminIdCtrlParam param) {
        AdminIdCtrlParam safeParam = param == null ? new AdminIdCtrlParam() : param;
        AdminIdCommand command = new AdminIdCommand();
        command.setId(safeParam.getId() == null ? safeParam.getUserId() : safeParam.getId());
        return command;
    }

    /**
     * 转换分页 VO。
     *
     * @param resultPage 应用层分页
     * @return 前端分页
     */
    private PageResp<AdminVo> toVoPage(PageResp<AdminResult> resultPage) {
        if (resultPage == null) {
            return PageResp.of(new ArrayList<>(), 0L, 1, 10);
        }
        PageResp<AdminVo> voPage = new PageResp<>();
        voPage.setPagination(resultPage.getPagination());
        voPage.setList(BeanUtil.copyToList(resultPage.getList(), AdminVo.class));
        return voPage;
    }
}
