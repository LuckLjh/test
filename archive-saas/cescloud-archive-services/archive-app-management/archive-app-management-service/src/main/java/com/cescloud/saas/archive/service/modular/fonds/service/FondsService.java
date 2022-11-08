
package com.cescloud.saas.archive.service.modular.fonds.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cescloud.saas.archive.api.modular.fonds.dto.FondsDTO;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveBusinessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * 全宗
 *
 * @author zhangpeng
 * @date 2019-03-21 12:04:54
 */
public interface FondsService extends IService<Fonds> {

	/**
	 * 全宗分页查询
	 * @param fondsDTO 查询实体参数
	 * @return 分页对象
	 */
	Page<Fonds> getPage(FondsDTO fondsDTO);

	/**
	 * 根据全宗id查询全宗实体
	 * @param fondsId 全宗id
	 * @return 全宗实体
	 */
	Fonds getFondsById(Long fondsId);

	/**
	 * 获取所有的全宗
	 * @return List<Fonds>
	 */
	List<Fonds> getFondsList();


	/**
	 * 根据用户ID获取所有的全宗
	 * @return List<Fonds>
	 */
	List<Fonds> getFondsList(Long userID);

	/**
	 * excel数据导入
	 * @param file excel文件
	 * @return
	 * @throws IOException
	 */
	@Transactional(rollbackFor = Exception.class)
	R insertExcel(MultipartFile file) throws IOException;

	/**
	 * 导出excel
	 * @param response
	 * @param fileName
	 */
	void exportExcel(HttpServletResponse response,String fileName, String keyword);

	/**
	 * 导出全宗模板
	 * @param response
	 * @param fileName
	 */
	void downloadExcelTemplate(HttpServletResponse response,String fileName);

	/**
	 * 更新全宗
	 * @param fonds 更新的全宗
	 * @return R<true or false>
	 */
	R updateFondsById(Fonds fonds);

	/**
	 * 根据id删除全宗
	 * @param fondsId
	 * @return
	 */
	R deleteFondsById(Long fondsId) throws ArchiveBusinessException;

	/**
	 * 保存全宗
	 * @param fonds 全宗实体
	 * @return
	 */
	R saveFonds(Fonds fonds);

	/**
	 * 根据rootTreeCodeList集合，查询档案书绑定的全宗集合
	 * @param rootTreeCodeList 档案树节点编码集合
	 * @return 全宗集合
	 */
	List<Fonds> listByRootTreeCode(List<String> rootTreeCodeList);

	/**
	 * 是否可以新增全宗，即数量是否未达上限，true未达上限，false已达上限
	 * @return
	 */
	R permitAddFonds();

	/**
	 * 根据全宗名查询全宗
	 * @param fondsName
	 * @return
	 */
	Fonds getFondsByName(String fondsName);

	/**
	 * 根据全宗编码查询全宗
	 */
	Fonds getFondsByCode(String fondsCode);

	/**
	 * 根据全宗获取全宗
	 * @param fondIds
	 * @return
	 */
	List<Fonds> getFondsByIds(List<Integer> fondIds);

	Integer allFondsCodeNum();

	Page<Fonds> getCopyFondsAuth(Page<Fonds> page, String keyword, String fondsCode);

	boolean checkFonds(String fondsCode, String fondsName);
}
