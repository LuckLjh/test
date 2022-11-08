
package com.cescloud.saas.archive.service.modular.archivedict.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.archivedict.entity.DictBase;
import com.cescloud.saas.archive.api.modular.archivetype.dto.SystemColumn;
import com.cescloud.saas.archive.service.modular.archivedict.mapper.DictBaseMapper;
import com.cescloud.saas.archive.service.modular.archivedict.service.DictBaseService;
import com.cescloud.saas.archive.service.modular.common.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础数据字典
 *
 * @author liudong1
 * @date 2019-04-25 16:10:20
 */
@Service
public class DictBaseServiceImpl extends ServiceImpl<DictBaseMapper, DictBase> implements DictBaseService {

}
