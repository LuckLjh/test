/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.archivetree.service.impl</p>
 * <p>文件名:FondsArchiveTreeServiceImpl.java</p>
 * <p>创建时间:2019年5月13日 下午2:38:48</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.archivetree.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeDTO;
import com.cescloud.saas.archive.api.modular.archivetree.entity.FondsArchiveTree;
import com.cescloud.saas.archive.service.modular.archivetree.mapper.FondsArchiveTreeMapper;
import com.cescloud.saas.archive.service.modular.archivetree.service.FondsArchiveTreeService;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全宗与档案树绑定
 * 
 * @author qiucs
 * @version 1.0.0 2019年5月13日
 */
@Service
@Slf4j
public class FondsArchiveTreeServiceImpl extends ServiceImpl<FondsArchiveTreeMapper, FondsArchiveTree> implements FondsArchiveTreeService {

    @Override
	@Transactional(rollbackFor = Exception.class)
    public boolean save(FondsArchiveTree entity) {
        // 1. 删除已绑定的数据
        remove(Wrappers.<FondsArchiveTree>lambdaQuery().eq(FondsArchiveTree::getFondsCode, entity.getFondsCode()));
        // 2. 保存当前绑定
        if (null == entity.getArchiveTreeId()) {
            if (log.isDebugEnabled()) {
                log.debug("全宗号[{}]未绑定档案树，跳过", entity.getFondsCode());
            }
            return true;
        }
        return super.save(entity);
    }

    /** 
     * 
     * @see com.cescloud.saas.archive.service.modular.archivetree.service.FondsArchiveTreeService#save(com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeDTO) 
     */  
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FondsArchiveTree> save(FondsArchiveTreeDTO entity) {
        String[] fondsCodes = entity.getFondsCodes();
        if (null == fondsCodes || 0 == fondsCodes.length) {
            throw new ArchiveRuntimeException("请指定要绑定的全宗号");
        }
        
        if (null == entity.getArchiveTreeId()) {
            throw new ArchiveRuntimeException("请指定要绑定的档案树");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("档案树[{}]绑定全宗号[{}]", entity.getArchiveTreeId(), entity.getFondsCodes());
        }
        
        List<FondsArchiveTree> list = new ArrayList<FondsArchiveTree>();
        
        Arrays.stream(fondsCodes).forEach(code -> {
            FondsArchiveTree fondsArchiveTree = new FondsArchiveTree();
            fondsArchiveTree.setArchiveTreeId(entity.getArchiveTreeId());
            fondsArchiveTree.setFondsCode(code);
            save(fondsArchiveTree);
            list.add(fondsArchiveTree);
        });
        
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<FondsArchiveTree> saveBatch(List<FondsArchiveTree> entityList) {
        entityList.forEach(entity -> {
            save(entity);
        });
        return entityList;
    }

	@Override
	public List<FondsArchiveTree> getFondsArchiveTreeByFondsCode(String code) {
		List<FondsArchiveTree> selectFondsArchiveTreeList = this.list(Wrappers.<FondsArchiveTree>lambdaQuery().eq(FondsArchiveTree::getFondsCode, code));
		return selectFondsArchiveTreeList;
	}
}
