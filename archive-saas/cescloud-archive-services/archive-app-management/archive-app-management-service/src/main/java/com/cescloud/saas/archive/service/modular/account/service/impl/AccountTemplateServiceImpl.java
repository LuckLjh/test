package com.cescloud.saas.archive.service.modular.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.account.dto.AccountTemplateDTO;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplate;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateRole;
import com.cescloud.saas.archive.api.modular.account.entity.AccountTemplateUser;
import com.cescloud.saas.archive.api.modular.role.dto.RoleSyncTreeNode;
import com.cescloud.saas.archive.api.modular.role.entity.SysRole;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteRoleService;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteSysRoleAuthService;
import com.cescloud.saas.archive.api.modular.stats.dto.DeckTotalStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.ArchiveDeckNew;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.AccountModuleEnum;
import com.cescloud.saas.archive.common.constants.ArchiveConstants;
import com.cescloud.saas.archive.common.constants.BoolEnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.common.util.CesBlobUtil;
import com.cescloud.saas.archive.common.util.InitializeUtil;
import com.cescloud.saas.archive.common.util.JsonUtil;
import com.cescloud.saas.archive.common.util.TreeUtil;
import com.cescloud.saas.archive.service.modular.account.mapper.AccountTemplateMapper;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateRoleService;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateService;
import com.cescloud.saas.archive.service.modular.account.service.AccountTemplateUserService;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.SecurityConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.data.authority.filter.SysLogContextHolder;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AccountTemplateServiceImpl
 * @Author zhangxuehu
 * @Date 2021/2/24 上午10:01
 **/
@Service
@Slf4j
@AllArgsConstructor
public class AccountTemplateServiceImpl extends ServiceImpl<AccountTemplateMapper, AccountTemplate> implements AccountTemplateService {

    private final AccountTemplateRoleService accountTemplateRoleService;

    private final RemoteRoleService remoteRoleService;

    private final RemoteTenantTemplateService remoteTenantTemplateService;

    private final RemoteSysRoleAuthService remoteSysRoleAuthService;
	private final AccountTemplateUserService accountTemplateUserService;

	@Autowired
	private RedisTemplate<String,String> redisTemplate;

    @Override
    public IPage getPage(Page page, String moduleName) {
		List<AccountTemplateDTO> data = Lists.newArrayList();
		final IPage<AccountTemplateDTO> pageByName = baseMapper.getPageByName(page, moduleName);
        List<AccountTemplateDTO> records = pageByName.getRecords();
        final List<Long> ids = records.stream().map(accountTemplateDTO -> accountTemplateDTO.getId()).collect(Collectors.toList());
        final Map<Long, List<AccountTemplateRole>> templateRoleMap = accountTemplateRoleService.getTemplateRoleByTemplatIds(ids);
        records = records.stream().peek(accountTemplateDTO -> {
            if (ObjectUtil.isNotNull(accountTemplateDTO.getTemplateDetail())) {
                byte[] bytes = CesBlobUtil.objConvertToByte(accountTemplateDTO.getTemplateDetail());
                List<Map<String, Object>> list = (List) ObjectUtil.deserialize(bytes);
                accountTemplateDTO.setTemplateDetail(list);
            }
            List<AccountTemplateRole> accountTemplateRoles = templateRoleMap.get(accountTemplateDTO.getId());
            if (CollectionUtil.isNotEmpty(accountTemplateRoles)) {
                accountTemplateDTO.setAccountTemplateRoles(accountTemplateRoles);
            }
        }).collect(Collectors.toList());
        //v8 沉淀到最顶部
		AccountTemplateDTO v8 = records.stream().filter(t->StrUtil.equalsAnyIgnoreCase(ArchiveConstants.V8_DECK_SHOW_TILE, t.getTemplateName())).findFirst().orElse(null);
		records.stream().filter(t->!StrUtil.equalsAnyIgnoreCase(ArchiveConstants.V8_DECK_SHOW_TILE, t.getTemplateName()))
				.collect(Collectors.toList());
		if(v8!=null){
			data.add(v8);
		}
		data.addAll(records);
		List<AccountTemplateDTO> list = data.stream().filter(item -> !item.getTemplateName().equals("档案系统驾驶舱可视化平台")).collect(Collectors.toList());
		pageByName.setRecords(list);
		pageByName.setTotal(pageByName.getTotal());
        return pageByName;
    }

    @Override
    public AccountTemplateDTO getInfoById(Long id) {
        final AccountTemplate accountTemplate = this.getById(id);
        AccountTemplateDTO accountTemplateDTO = new AccountTemplateDTO();
        BeanUtil.copyProperties(accountTemplate, accountTemplateDTO);
        final List<AccountTemplateRole> accountTemplateRoles = accountTemplateRoleService.list(Wrappers.<AccountTemplateRole>lambdaQuery().eq(AccountTemplateRole::getTemplateId, id));
        accountTemplateDTO.setAccountTemplateRoles(accountTemplateRoles);
        return accountTemplateDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveAccountTemplate(AccountTemplateDTO accountTemplateDTO) {
        if (ObjectUtil.isNotNull(accountTemplateDTO.getTemplateDetail())) {
            List<Map<String, Object>> formContent = (List<Map<String, Object>>) accountTemplateDTO.getTemplateDetail();
            byte[] bytes = ObjectUtil.serialize(formContent);
            accountTemplateDTO.setTemplateDetail(bytes);
        }
        if(ObjectUtil.isNotNull(accountTemplateDTO.getTemplateDetail()) && StrUtil.equalsAnyIgnoreCase(ArchiveConstants.V8_DECK_SHOW_TILE,accountTemplateDTO.getTemplateName())){
			Assert.isTrue(false,ArchiveConstants.V8_DECK_SHOW_TILE+"已经默认设定");
        	return false;
		}
        final boolean save = this.save(accountTemplateDTO);
        settingAssociatedRole(accountTemplateDTO);
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeAccountTemplate(String ids) {
        final List<Long> idList = Arrays.asList(ids.split(",")).stream()
                .map(s -> Long.parseLong(s.trim()))
                .collect(Collectors.toList());
		try {
			final StringBuilder info = new StringBuilder();
			List<AccountTemplate> accountTemplates = this.listByIds(idList);
			for (AccountTemplate accountTemplate : accountTemplates) {
				info.append(accountTemplate.getTemplateName()+" ");
			}
			SysLogContextHolder.setLogTitle(String.format("删除台账信息-操作的模板名称【%s】",info.toString()));
		} catch (Exception e) {
			log.error("记录日志详情失败：", e);
		}
        final boolean remove = this.remove(Wrappers.<AccountTemplate>lambdaQuery().in(AccountTemplate::getId, idList));
        accountTemplateRoleService.remove(Wrappers.<AccountTemplateRole>lambdaQuery().in(AccountTemplateRole::getTemplateId, idList));
        return remove;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAccountTemplate(AccountTemplateDTO accountTemplateDTO) {
        if (ObjectUtil.isNotNull(accountTemplateDTO.getTemplateDetail())) {
            List<Map<String, Object>> formContent = (List<Map<String, Object>>) accountTemplateDTO.getTemplateDetail();
            byte[] bytes = ObjectUtil.serialize(formContent);
            accountTemplateDTO.setTemplateDetail(bytes);
        }
        boolean result = this.updateById(accountTemplateDTO);
        settingAssociatedRole(accountTemplateDTO);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
	public Boolean settingAssociatedRole(AccountTemplateDTO accountTemplateDTO) {
		final Long id = accountTemplateDTO.getId();
		final List<AccountTemplateRole> accountTemplateRoles = accountTemplateDTO.getAccountTemplateRoles();
		accountTemplateRoleService.remove(Wrappers.<AccountTemplateRole>lambdaQuery().eq(AccountTemplateRole::getTemplateId, id));
		if (CollectionUtil.isNotEmpty(accountTemplateRoles)) {
			accountTemplateRoles.stream().forEach(accountTemplateRole -> accountTemplateRole.setTemplateId(id));
			accountTemplateRoleService.saveBatch(accountTemplateRoles);
		}
		return Boolean.TRUE;
	}


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean settingDefaultTemplate(AccountTemplate accountTemplate) {
        final Integer isDefault = accountTemplate.getIsDefault();
        this.updateById(accountTemplate);
        if (BoolEnum.YES.getCode().equals(isDefault)) {
            AccountTemplate updateAccountTemplate = AccountTemplate.builder().isDefault(BoolEnum.NO.getCode()).build();
            this.update(updateAccountTemplate, Wrappers.<AccountTemplate>lambdaQuery().ne(AccountTemplate::getId, accountTemplate.getId()));
        }
        return Boolean.TRUE;
    }

    @Override
    public AccountTemplate getAccountModule(String roles) {
    	log.info("参数为: {}",roles);
        final List<Long> roleList = Arrays.asList(roles.split(",")).stream()
                .map(s -> Long.parseLong(s.trim()))
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(roleList)) {
            return new AccountTemplate();
        }
        final List<AccountTemplateRole> list = accountTemplateRoleService.list(Wrappers.<AccountTemplateRole>lambdaQuery().in(AccountTemplateRole::getRoleId, roleList));
        if (CollectionUtil.isEmpty(list)) {
            //角色无绑定模板  取 默认模板
            final AccountTemplate accountTemplate = this.getOne(Wrappers.<AccountTemplate>lambdaQuery().eq(AccountTemplate::getIsDefault, BoolEnum.YES.getCode()));
            return formatConversion(accountTemplate);
        }
        final List<Long> templateIds = list.stream().map(accountTemplateRole -> accountTemplateRole.getTemplateId()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(templateIds)) {
            return new AccountTemplate();
        }
        final List<AccountTemplate> accountTemplates = this.listByIds(templateIds);
        if (CollectionUtil.isEmpty(accountTemplates)) {
            return new AccountTemplate();
        }
        List<Map<String, Object>> allList = CollectionUtil.newArrayList();
        accountTemplates.stream().forEach(accountTemplate -> {
            if (ObjectUtil.isNotNull(accountTemplate.getTemplateDetail())) {
                byte[] bytes = CesBlobUtil.objConvertToByte(accountTemplate.getTemplateDetail());
                List<Map<String, Object>> detail = (List) ObjectUtil.deserialize(bytes);
                accountTemplate.setTemplateDetail(detail);
                detail.stream().forEach(map -> allList.add(map));
            }
        });
        if (accountTemplates.size() == 1) {
            return accountTemplates.get(0);
        }
        //多个模板 重组
        final List<String> moduleCode = allList.stream().map(map -> StrUtil.toString(map.get("moduleCode"))).distinct().collect(Collectors.toList());
        List<Map<String, Object>> result = CollectionUtil.newArrayList();
        moduleCode.stream().forEach(str -> {
            Map<String, Object> map = CollectionUtil.newHashMap();
            AccountModuleEnum anEnum = AccountModuleEnum.getEnum(str);
            map.put("moduleName", anEnum.getModuleName());
            map.put("moduleCode", anEnum.getModuleCode());
            map.put("sortNo", anEnum.getSortNo());
            map.put("width", allList.stream().filter(e -> e.get("moduleCode").equals(str)).findFirst().get().get("width"));
            map.put("key", IdUtil.simpleUUID());
            map.put("height", allList.stream().filter(e -> e.get("moduleCode").equals(str)).findFirst().get().get("height"));
            result.add(map);
        });
        return AccountTemplate.builder().templateDetail(result).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean initializeAccountTemplate(Long templateId, Long tenantId) {
        ExcelReader excel = null;
        try {
            InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return Boolean.FALSE;
            }
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.ACCOUNT_TEMPLATE, true);
            List<List<Object>> read = excel.read();
            List<AccountTemplateRole> accountTemplateRoles = CollectionUtil.newArrayList();
            //获取所有角色信息
            final R<List<SysRole>> allRoleList = remoteRoleService.getAllRoleListByTenantId(tenantId, SecurityConstants.FROM_IN);
            final List<SysRole> sysRoles = allRoleList.getData();
            final Map<String, Long> roleByIdMap = sysRoles.stream().collect(Collectors.toMap(SysRole::getRoleName, SysRole::getRoleId));
            //循环行
            for (int i = 1, length = read.size(); i < length; i++) {
                //模块名称
                String templateName = StrUtil.toString(read.get(i).get(0));
                //表单信息
	            byte[] bytes = ObjectUtil.serialize(JsonUtil.toBean(StrUtil.toString(read.get(i).get(1)), List.class));
                //授予角色
                String roleString = InitializeUtil.checkListVal(read.get(i), 2);

                String isDefaultStr = InitializeUtil.checkListVal(read.get(i), 3);
                Integer isDefault = "是".equals(isDefaultStr) ? BoolEnum.YES.getCode() : BoolEnum.NO.getCode();
                AccountTemplate accountTemplate = AccountTemplate.builder().templateName(templateName).templateDetail(bytes).isDefault(isDefault).tenantId(tenantId).build();
                this.save(accountTemplate);
                final Long accountTemplateId = accountTemplate.getId();
                List<String> roleList = Arrays.stream(roleString.split(",")).collect(Collectors.toList());
                roleList.stream().forEach(str -> {
                    final Long sysRoleId = roleByIdMap.get(str);
                    if (ObjectUtil.isNotNull(sysRoleId)) {
                        AccountTemplateRole accountTemplateRole = AccountTemplateRole.builder().templateId(accountTemplateId).roleName(str).roleId(sysRoleId).tenantId(tenantId).build();
                        accountTemplateRoles.add(accountTemplateRole);
                    }
                });
            }
            boolean batch = Boolean.FALSE;
            if (CollUtil.isNotEmpty(accountTemplateRoles)) {
                batch = accountTemplateRoleService.saveBatch(accountTemplateRoles);
            }
            return batch;
        } finally {
            IoUtil.close(excel);
        }
    }

    @Override
	public List<RoleSyncTreeNode> roleTree(Long templateId, String templateName) throws ArchiveBusinessException {
		List<AccountTemplateRole> list = CollectionUtil.newArrayList();
		if (ObjectUtil.isNull(templateId)) {
			list = accountTemplateRoleService.list();
		} else if (ArchiveConstants.V8_DECK_SHOW_TILE.equals(templateName)){
			list = new ArrayList<>();
		}else {
			list = accountTemplateRoleService.list(Wrappers.<AccountTemplateRole>lambdaQuery().ne(AccountTemplateRole::getTemplateId, templateId));
			list = delV8TemplateRole(templateId, list);
		}
		List<Long> roleIds = list.stream().map(accountTemplateRole -> accountTemplateRole.getRoleId()).collect(Collectors.toList());

		final String collect = roleIds.stream().map(n -> String.valueOf(n)).collect(Collectors.joining(","));
		final R<List<RoleSyncTreeNode>> sysRoleTree = remoteSysRoleAuthService.getSysRoleTreeExcludeRoleIds(collect);
		if (sysRoleTree.getCode() == CommonConstants.FAIL) {
			throw new ArchiveBusinessException("获取角色失败");
		}
		return TreeUtil.build(sysRoleTree.getData(), 0L);
	}

	private List<AccountTemplateRole> delV8TemplateRole(Long templateId, List<AccountTemplateRole> list){
		// 查询驾驶舱对应的角色
		CesCloudUser user = SecurityUtils.getUser();
		List<AccountTemplate> accountTemplates = this.list(Wrappers.<AccountTemplate>lambdaQuery().eq(AccountTemplate::getTenantId,user.getTenantId()));
		if (CollectionUtil.isNotEmpty(accountTemplates)){
			AccountTemplate	template = accountTemplates.stream().filter(item -> ArchiveConstants.V8_DECK_SHOW_TILE.equals(item.getTemplateName())).findFirst().get();
				List<AccountTemplateRole> templateRole = getAccountTemplateRole(template.getId().intValue());
				List<Long> roles = templateRole.stream().map(AccountTemplateRole::getRoleId).collect(Collectors.toList());
				// 剔除驾驶舱角色Id
				roles.stream().forEach(item ->{
					for (int i = 0; i < list.size(); i++) {
						if (item.equals(list.get(i).getRoleId())){
							list.remove(i);break;
						}}});
		}

		return list;
	}

	@Override
	public List<ArrayList<String>> getAccountInfo(Long tenantId) {
		List<AccountTemplate> accountTemplates = this.list(Wrappers.<AccountTemplate>lambdaQuery().eq(AccountTemplate::getTenantId, tenantId));
		List<AccountTemplateRole> templateRoles = accountTemplateRoleService.list(Wrappers.<AccountTemplateRole>lambdaQuery().eq(AccountTemplateRole::getTenantId, tenantId));
		// 模板名称  JSON  授予角色  是否默认
		List<ArrayList<String>> collect = accountTemplates.stream().map(accountTemplate -> CollectionUtil.newArrayList(accountTemplate.getTemplateName(),
				Optional.ofNullable(accountTemplate.getTemplateDetail()).map(this::processingFormInformation).orElse(null),
				getTemplatesRoles(templateRoles,accountTemplate.getId()),
				BoolEnum.YES.getCode().equals(accountTemplate.getIsDefault())?"是":"否"
		)).collect(Collectors.toList());
		return collect;
	}

	private String processingFormInformation(Object formContent) {
		byte[] bytes = CesBlobUtil.objConvertToByte(formContent);
		List<Map<String, Object>> map = (List<Map<String, Object>>) ObjectUtil.deserialize(bytes);
		return JsonUtil.bean2json(map);
	}

	private String getTemplatesRoles(List<AccountTemplateRole> templateRoles, Long id) {
    	if (CollUtil.isEmpty(templateRoles)){
    		return "";
	    }
		return templateRoles.stream().filter(e -> id.equals(e.getTemplateId())).map(AccountTemplateRole::getRoleName).collect(Collectors.joining(StrUtil.COMMA));
	}

	@Override
	public DeckTotalStatsDTO<Boolean, List<AccountTemplateUser>> showDeck(ArchiveDeckNew archiveDeckNew) {
    	Boolean showDeck = false;
		CesCloudUser cesCloudUser = SecurityUtils.getUser();
		log.info("目前用户信息:{}",JSONUtil.toJsonStr(cesCloudUser));
		List<String> roleIdList = cesCloudUser.getAuthorities()
				.stream().map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith(SecurityConstants.ROLE))
				.map(authority -> authority.split("_")[1])
				.collect(Collectors.toList());
		List<AccountTemplateRole> accountTemplateRoleList = this.accountTemplateRoleService.list();
		List<AccountTemplateRole> userTemplateRoleCurrentList = accountTemplateRoleList.stream().filter(tr->roleIdList.contains(tr.getRoleId().toString())).collect(Collectors.toList());
		final List<Long> userTemplateIds = userTemplateRoleCurrentList.stream().map(accountTemplateDTO -> accountTemplateDTO.getTemplateId()).collect(Collectors.toList());
		List<AccountTemplate> allAccountTemplate = this.list();
		for (AccountTemplate item:allAccountTemplate) {
			item.setTemplateDetail(null);
		}
		AccountTemplate accountV8DeckShowTemplate = allAccountTemplate.stream().filter(t->StrUtil.equalsAnyIgnoreCase(t.getTemplateName(),ArchiveConstants.V8_DECK_SHOW_TILE)&&t.getTemplateDetail()==null).findFirst().orElse(null);
		//如果没有建立默认的领导魔板，将会新增，确保其存在，注意 default:2 表示此种 不能修改和删除 ！
		if(accountV8DeckShowTemplate==null){
			AccountTemplateDTO accountTemplateDTO = new AccountTemplateDTO();
			accountTemplateDTO.setTemplateName(ArchiveConstants.V8_DECK_SHOW_TILE);
			accountTemplateDTO.setIsDefault(0);
			log.info("将要保存的信息为:{}",JSONUtil.toJsonStr(accountTemplateDTO));
			this.saveAccountTemplate(accountTemplateDTO);
//			accountV8DeckShowTemplate = accountTemplateDTO;
			allAccountTemplate.add(accountTemplateDTO);
		}
		else{
			log.info("目前魔板的信息为:{}",JSONUtil.toJsonStr(accountV8DeckShowTemplate));
		}
		List<AccountTemplate> userTemplateLists = userTemplateIds.size()==0?Lists.newArrayList(): allAccountTemplate.stream().filter(t->userTemplateIds.contains(t.getId())).collect(Collectors.toList());
		ArrayList<AccountTemplate> list = Lists.newArrayList(userTemplateLists.stream().collect(Collectors.toCollection(() -> new TreeSet<AccountTemplate>(Comparator.comparing(AccountTemplate::getTemplateName)))));
		DeckTotalStatsDTO<Boolean, List<AccountTemplateUser>> resultAll = accountTemplateUserService.settingAccountTemplateUser(list,cesCloudUser.getId());
		return resultAll;
	}

	@Override
	public List<AccountTemplateRole> getAccountTemplateRole(Integer templateId) {
		AccountTemplate template = this.getOne(Wrappers.<AccountTemplate>lambdaQuery().eq(AccountTemplate::getId, templateId));
		if (null != template){
			return this.accountTemplateRoleService.list(Wrappers.<AccountTemplateRole>lambdaQuery().eq(AccountTemplateRole::getTemplateId, template.getId()));
		}
		return null;
	}

	private InputStream getDefaultTemplateStream(Long templateId) {
        TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
        byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }

    private AccountTemplate formatConversion(AccountTemplate accountTemplate) {
        if (ObjectUtil.isEmpty(accountTemplate)) {
            return new AccountTemplate();
        }
		if (ObjectUtil.isNotNull(accountTemplate.getTemplateDetail())) {
			byte[] bytes = CesBlobUtil.objConvertToByte(accountTemplate.getTemplateDetail());
			List<Map<String, Object>> list = (List) ObjectUtil.deserialize(bytes);
			accountTemplate.setTemplateDetail(list);
		}
        return accountTemplate;
    }
}
