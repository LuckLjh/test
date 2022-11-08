
package com.cescloud.saas.archive.service.modular.fonds.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * 全宗
 *
 * @author zhangpeng
 * @date 2019-03-21 12:04:54
 */
public interface FondsMapper extends BaseMapper<Fonds> {

	Fonds selectById(Integer fondsId);

	List<Fonds> selectAllFondsByNameOrCode(@Param("fondsCode")String fondsCode,@Param("fondsName")String fondsName);

	List<Fonds> listByRootTreeCode(@Param("rootTreeCodeList") List<String> rootTreeCodeList);

	Integer countFondsCode();
}
