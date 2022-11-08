
package com.cescloud.saas.archive.service.modular.account.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.common.constants.SwaggerConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.log.annotation.SysLog;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateUserService;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 驾驶舱个人台账模板表
 *
 * @author bob
 * @date 2021-05-28 23:02:59
 */
@Api(value = "AccountTemplateUser", description = "驾驶舱个人台账模板表")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/account-template-user")
public class AccountTemplateUserController {

  private final  AccountTemplateUserService accountTemplateUserService;


	@ApiOperation(value = "设置用户默认模板", httpMethod = SwaggerConstants.PUT)
	@PutMapping("/default")
	@SysLog("设置用户默认模板")
	public R settingDefaultTemplate(@RequestBody @ApiParam(name = "accountTemplate",value = "修改实体") AccountTemplateUser accountTemplate){
		try {
			SysLogContextHolder.setLogTitle(String.format("设置用户默认模板-操作的模板名称【%s】",accountTemplate.getTemplateName()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
		return new R<>(accountTemplateUserService.settingDefaultTemplate(accountTemplate));
	}

	/**
   * 分页查询
   * @param page 分页对象
   * @param accountTemplateUser 驾驶舱个人台账模板表
   * @return
   */
  @ApiOperation(value = "分页查询")
  @GetMapping("/page")
  public R getAccountTemplateUserPage(Page page, AccountTemplateUser accountTemplateUser) {
    return  new R<>(accountTemplateUserService.page(page,Wrappers.query(accountTemplateUser)));
  }


  /**
   * 通过id查询驾驶舱个人台账模板表
   * @param id id
   * @return R
   */
  @ApiOperation(value = "通过id查询驾驶舱个人台账模板表")
  @GetMapping("/{id}")
  public R getById(@PathVariable("id") Long id){
    return new R<>(accountTemplateUserService.getById(id));
  }

  /**
   * 新增驾驶舱个人台账模板表
   * @param accountTemplateUser 驾驶舱个人台账模板表
   * @return R
   */
  @ApiOperation(value = "新增驾驶舱个人台账模板表")
  @SysLog("新增驾驶舱个人台账模板表")
  @PostMapping
  public R save(@RequestBody AccountTemplateUser accountTemplateUser){
	  try {
		  SysLogContextHolder.setLogTitle(String.format("新增驾驶舱个人台账模板表-操作的模板名称【%s】",accountTemplateUser.getTemplateName()));
	  } catch (Exception e) {
		  log.error("记录日志详情失败：", e);
	  }
    return new R<>(accountTemplateUserService.save(accountTemplateUser));
  }

  /**
   * 修改驾驶舱个人台账模板表
   * @param accountTemplateUser 驾驶舱个人台账模板表
   * @return R
   */
  @ApiOperation(value = "修改驾驶舱个人台账模板表")
  @SysLog("修改驾驶舱个人台账模板表")
  @PutMapping
  public R updateById(@RequestBody AccountTemplateUser accountTemplateUser){
	  try {
		  SysLogContextHolder.setLogTitle(String.format("修改驾驶舱个人台账模板表-操作的模板名称【%s】",accountTemplateUser.getTemplateName()));
	  } catch (Exception e) {
		  log.error("记录日志详情失败：", e);
	  }
    return new R<>(accountTemplateUserService.updateById(accountTemplateUser));
  }

  /**
   * 通过id删除驾驶舱个人台账模板表
   * @param id id
   * @return R
   */
  @ApiOperation(value = "通过id删除驾驶舱个人台账模板表")
  @SysLog("删除驾驶舱个人台账模板表")
  @DeleteMapping("/{id}")
  public R removeById(@PathVariable Long id){
	  try {
		  AccountTemplateUser byId = accountTemplateUserService.getById(id);
		  SysLogContextHolder.setLogTitle(String.format("删除驾驶舱个人台账模板表-操作的模板名称【%s】",byId.getTemplateName()));
	  } catch (Exception e) {
		  log.error("记录日志详情失败：", e);
	  }
	  return new R<>(accountTemplateUserService.removeById(id));
  }

}
