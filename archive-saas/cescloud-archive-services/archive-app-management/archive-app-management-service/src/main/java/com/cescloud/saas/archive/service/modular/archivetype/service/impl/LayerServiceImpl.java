/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetype.service.impl</p>
 * <p>文件名:LayerServiceImpl.java</p>
 * <p>创建时间:2020年2月14日 下午12:01:20</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetype.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetype.entity.Layer;
import com.cescloud.saas.archive.api.modular.archivetype.entity.LayerBase;
import com.cescloud.saas.archive.api.modular.archivetype.entity.TemplateTable;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.service.modular.archivetype.mapper.LayerMapper;
import com.cescloud.saas.archive.service.modular.archivetype.service.LayerBaseService;
import com.cescloud.saas.archive.service.modular.archivetype.service.LayerService;
import com.cescloud.saas.archive.service.modular.archivetype.service.TemplateTableService;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.common.security.service.CesCloudUser;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author qiucs
 * @version 1.0.0 2020年2月14日
 */
@Component
public class LayerServiceImpl extends ServiceImpl<LayerMapper, Layer> implements LayerService {

    @Autowired
    private LayerBaseService layerBaseService;

    @Autowired
    private TemplateTableService templateTableService;

	/**
     * @see com.baomidou.mybatisplus.extension.service.impl.ServiceImpl#save(java.lang.Object)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Layer entity) {
        // 第一次保存，初始化基础层级
        if (0 == count()) {
            initLayer();
        }
        // 唯一性检查
        checkUniqueCode(entity);
        checkUniqueName(entity);
        // 设置排序号
        processSortNo(entity);
        // 保存
        return super.save(entity);
    }

    private void checkUniqueCode(Layer entity) {
        final LambdaQueryWrapper<Layer> lambdaQuery = Wrappers.<Layer> lambdaQuery();
        lambdaQuery.eq(Layer::getCode, entity.getCode());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(Layer::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            throw new ArchiveRuntimeException(String.format("层级编码[%s]重复！", entity.getCode()));
        }
    }

    private void checkUniqueName(Layer entity) {
        final LambdaQueryWrapper<Layer> lambdaQuery = Wrappers.<Layer> lambdaQuery();
        lambdaQuery.eq(Layer::getName, entity.getName());
        if (ObjectUtil.isNotNull(entity.getId())) {
            lambdaQuery.ne(Layer::getId, entity.getId());
        }
        if (count(lambdaQuery) > 0) {
            throw new ArchiveRuntimeException(String.format("层级名称[%s]重复！", entity.getName()));
        }
    }

    private void processSortNo(Layer entity) {
        if (null != entity.getSortNo()) {
            return;
        }
        Integer maxSortNo = baseMapper.getMaxSortNo();
        if (null == maxSortNo) {
            maxSortNo = 100;
        }
        entity.setSortNo(++maxSortNo);
    }

    /**
     * @see com.baomidou.mybatisplus.extension.service.impl.ServiceImpl#updateById(java.lang.Object)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Layer entity) {
        // 检查层级是否被使用了，如果被使用了就不能修改编码
        Assert.isTrue(checkCodeUsing(entity), "层级已被使用了，不修改编码！");
        // 唯一性检查
        checkUniqueCode(entity);
        checkUniqueName(entity);
        // 设置排序号
        processSortNo(entity);
        return super.updateById(entity);
    }

    private boolean checkCodeUsing(Layer entity) {
        if (null == entity.getId()) {
            return true;
        }
        final Layer oldEntity = getById(entity.getId());
        if (null == entity.getSortNo() && null != oldEntity.getSortNo()) {
            entity.setSortNo(oldEntity.getSortNo());
        }
        if (oldEntity.getCode().equals(entity.getCode())) {
            return true;
        }
        return checkCodeUsingByCode(oldEntity.getCode());
    }

    private boolean checkCodeUsingByCode(String code) {
        final int count = templateTableService
            .count(Wrappers.<TemplateTable> lambdaQuery().eq(TemplateTable::getLayerCode, code));
        return 0 == count;
    }

    /**
     * @see com.baomidou.mybatisplus.extension.service.impl.ServiceImpl#removeById(java.io.Serializable)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        final Layer entity = getById(id);
        final ArchiveLayerEnum layer = ArchiveLayerEnum.getEnum(entity.getCode());
        Assert.isTrue(null == layer, "预置层级不能删除！");

        // 检查层级是否被使用了，如果被使用了就不能修改编码
        Assert.isTrue(checkCodeUsingByCode(entity.getCode()), "层级已被使用了，不删除！");
        return super.removeById(id);
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.LayerService#getLayerList(String)
     */
    @Override
    public List<Layer> getLayerList(String keyword) {
        final LambdaQueryWrapper<Layer> lambdaQuery = Wrappers.<Layer> lambdaQuery();
        if (StrUtil.isNotEmpty(keyword)) {
            lambdaQuery.or().like(Layer::getName, keyword).or().like(Layer::getCode, keyword.toUpperCase());
        }
        lambdaQuery.orderByAsc(Layer::getSortNo);
        final List<Layer> list = list(lambdaQuery);
        if (StrUtil.isEmpty(keyword) && (null == list || list.isEmpty())) {
            initLayer();
            return list(lambdaQuery);
        }
        return list;
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.LayerService#initLayer()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initLayer() {
        final List<LayerBase> list = layerBaseService.list();
        final CesCloudUser user = SecurityUtils.getUser();
        list.forEach(base -> {
            final Layer entity = new Layer();
            BeanUtil.copyProperties(base, entity);
            entity.setId(null);
            if (null != user) {
                entity.setCreatedBy(user.getId());
            }
            entity.setCreatedTime(LocalDateTime.now());
            // 唯一性检查
            checkUniqueCode(entity);
            checkUniqueName(entity);
            // 保存
            baseMapper.insert(entity);
        });
    }

    /**
     * 根据编码获取层级
     *
     * @param code
     * @return
     */
    @Override
    public Layer getByCode(String code) {
        return this.getOne(Wrappers.<Layer> lambdaQuery().eq(Layer::getCode, code));
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.archivetype.service.LayerService#page(com.baomidou.mybatisplus.core.metadata.IPage,
     *      java.lang.String)
     */
    @Override
    public IPage<Layer> page(IPage<Layer> page, String keyword) {
        final LambdaQueryWrapper<Layer> lambdaQuery = Wrappers.<Layer> lambdaQuery();
        if (StrUtil.isNotEmpty(keyword)) {
            lambdaQuery.or().like(Layer::getName, keyword).or().like(Layer::getCode, keyword.toUpperCase());
        }
        lambdaQuery.orderByAsc(Layer::getSortNo);
        final IPage<Layer> p = page(page, lambdaQuery);
        if (StrUtil.isEmpty(keyword) && 1 == page.getCurrent() && (null == p || 0 == p.getTotal())) {
            initLayer();
            return page(page, keyword);
        }
        return p;
    }
}
