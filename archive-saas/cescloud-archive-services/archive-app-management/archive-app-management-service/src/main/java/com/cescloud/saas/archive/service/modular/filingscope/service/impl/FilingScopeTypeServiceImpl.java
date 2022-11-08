package com.cescloud.saas.archive.service.modular.filingscope.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeOrderDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeTypePostDTO;
import com.cescloud.saas.archive.api.modular.filingscope.dto.FilingScopeTypePutDTO;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScope;
import com.cescloud.saas.archive.api.modular.filingscope.entity.FilingScopeType;
import com.cescloud.saas.archive.api.modular.tenant.entity.TenantTemplate;
import com.cescloud.saas.archive.api.modular.tenant.feign.RemoteTenantTemplateService;
import com.cescloud.saas.archive.common.constants.DisposalMethodREnum;
import com.cescloud.saas.archive.common.constants.TemplateFieldConstants;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import com.cescloud.saas.archive.service.modular.filingscope.mapper.FilingScopeTypeMapper;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeService;
import com.cescloud.saas.archive.service.modular.filingscope.service.FilingScopeTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @ClassName FilingScopeTypeServiceImpl
 * @Author zhangxuehu
 * @Date 2020/6/29 13:19
 **/
@Service
@Slf4j
public class FilingScopeTypeServiceImpl extends ServiceImpl<FilingScopeTypeMapper, FilingScopeType> implements FilingScopeTypeService {
    @Autowired
    private FilingScopeService filingScopeService;
    @Autowired
    private RemoteTenantTemplateService remoteTenantTemplateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveFilingScopeType(FilingScopeTypePostDTO filingScopeTypePostDTO) throws ArchiveBusinessException {
        FilingScopeType filingScopeType = new FilingScopeType();
        BeanUtil.copyProperties(filingScopeTypePostDTO, filingScopeType);
        boolean result = save(filingScopeType);
        if (!result) {
            throw new ArchiveBusinessException("新建归档范围信息失败！");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFilingScopeType(FilingScopeTypePutDTO filingScopeTypePutDTO) throws ArchiveBusinessException {
        FilingScopeType filingScopeType = new FilingScopeType();
        BeanUtil.copyProperties(filingScopeTypePutDTO, filingScopeType);
        boolean result = updateById(filingScopeType);
        if (!result) {
            throw new ArchiveBusinessException("修改归档范围信息失败！");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) throws ArchiveBusinessException {
        Boolean result = this.removeById(id);
        if (!result) {
            throw new ArchiveBusinessException("删除归档范围信息失败！");
        }
        return result;
    }

    @Override
    public IPage<List<FilingScopeType>> findFilingScopeTypeByParentId(Page page, String id, String keyWord) {
        LambdaQueryWrapper<FilingScopeType> queryWrapper = Wrappers.<FilingScopeType>lambdaQuery();
        queryWrapper.eq(FilingScopeType::getParentId, id);
        if (StrUtil.isNotBlank(keyWord)) {
            queryWrapper.like(FilingScopeType::getFilingScope, keyWord);
        }
	    queryWrapper.orderByAsc(FilingScopeType::getCatalogueNo);
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeFilingScopeTypeHandle(Long templateId, Long tenantId) throws ArchiveBusinessException {
        ExcelReader excel = null;
        try {
            InputStream inputStream = getDefaultTemplateStream(templateId);
            if (ObjectUtil.isNull(inputStream)) {
                return;
            }
            excel = new ExcelReader(inputStream, TemplateFieldConstants.SHEET_NAMES.FILING_SCOPE_TYPE_NAME);
            List<List<Object>> read = excel.read();
            List<FilingScope> filingScopes = filingScopeService.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getTenantId, tenantId));
            Map<String, Long> filingScopeMap = filingScopes.stream().collect(Collectors.toMap(FilingScope::getClassName, FilingScope::getId));
            List<FilingScopeType> filingScopeTypes = CollectionUtil.newArrayList();
            for (int i = 1, length = read.size(); i < length; i++) {
                //所属分类
                String classification = StrUtil.toString(read.get(i).get(0));
                // 序号
                String catalogueNo = StrUtil.toString(read.get(i).get(1));
                // 归档范围
                String filingScope = StrUtil.toString(read.get(i).get(2));
                // 保管期限
                String retentionPeriod = StrUtil.toString(read.get(i).get(3));
                // 处置方式
                String disposalMethod = StrUtil.toString(read.get(i).get(4));
                String disposal = "";
                if (StrUtil.isNotBlank(disposalMethod)) {
                    disposal = DisposalMethodREnum.getEnumByName(disposalMethod).getValue();
                }
                FilingScopeType filingScopeType = FilingScopeType.builder().parentId(filingScopeMap.get(classification)).catalogueNo(catalogueNo)
                        .filingScope(filingScope).disposalMethod(disposal)
                        .retentionPeriod(retentionPeriod).tenantId(tenantId).build();
                filingScopeTypes.add(filingScopeType);
            }
            if (CollectionUtil.isNotEmpty(filingScopeTypes)) {
                this.saveBatch(filingScopeTypes);
            }
        } finally {
            IoUtil.close(excel);
        }
    }

    @Override
    public List<ArrayList<String>> getFilingScopeTypeInfo(Long tenantId) {
        List<FilingScopeType> filingScopeTypes = this.list(Wrappers.<FilingScopeType>lambdaQuery().eq(FilingScopeType::getTenantId, tenantId));
        List<FilingScope> filingScopes = filingScopeService.list(Wrappers.<FilingScope>lambdaQuery().eq(FilingScope::getTenantId, tenantId));
        Map<Long, String> filingScopeMap = filingScopes.stream().collect(Collectors.toMap(FilingScope::getId, FilingScope::getClassName));
        List<ArrayList<String>> lists = new ArrayList<>();
        filingScopeTypes.stream().forEach(filingScopeType -> {
            //所属分类	序号	归档范围	保管期限	处置方式
            String disposalMethod = filingScopeType.getDisposalMethod();
            String disposal = DisposalMethodREnum.getEnum(disposalMethod).getName();
            lists.add(CollectionUtil.newArrayList(filingScopeMap.get(filingScopeType.getParentId()),filingScopeType.getCatalogueNo(),filingScopeType.getFilingScope(),filingScopeType.getRetentionPeriod(),disposal));
        });
        return lists;
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean filingScopeTypeOrder(FilingScopeOrderDTO filingScopeOrderDTO) {
		final List<Long> ids = filingScopeOrderDTO.getIds();
		List<FilingScopeType> filingScopeTypes = this.list(Wrappers.<FilingScopeType>lambdaQuery().in(FilingScopeType::getId, ids));
		final List<FilingScopeType> list = IntStream.rangeClosed(1, ids.size()).mapToObj(i -> {
			final FilingScopeType filingScopeType = filingScopeTypes.parallelStream().filter(e -> e.getId().equals(ids.get(i - 1))).findAny().get();
			filingScopeType.setCatalogueNo(String.valueOf(i));
			return filingScopeType;
		}).collect(Collectors.toList());
		return this.updateBatchById(list);
	}

	/**
     * 获取 初始化模板文件流
     *
     * @param templateId 模板id
     * @return
     */
    private InputStream getDefaultTemplateStream(Long templateId) {
        TenantTemplate tenantTemplate = remoteTenantTemplateService.selectTemplateById(templateId).getData();
        byte[] bytes = (byte[]) tenantTemplate.getTemplateContent();
        InputStream inputStream = new ByteArrayInputStream(bytes);
        return inputStream;
    }
}
