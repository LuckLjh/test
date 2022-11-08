/**
 * <p>Copyright:Copyright(c) 2020</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.stats.service.impl</p>
 * <p>文件名:YearStatsServiceImpl.java</p>
 * <p>创建时间:2020年10月23日 下午2:15:40</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.stats.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cescloud.saas.archive.api.modular.stats.dto.YearStatsDTO;
import com.cescloud.saas.archive.api.modular.stats.entity.YearStats;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.stats.mapper.YearStatsMapper;
import com.cescloud.saas.archive.service.modular.stats.service.YearStatsService;
import com.cescloud.saas.archive.service.modular.stats.service.helper.YearStatsHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author qiucs
 * @version 1.0.0 2020年10月23日
 */
@Component
@Slf4j
public class YearStatsServiceImpl extends ServiceImpl<YearStatsMapper, YearStats> implements YearStatsService {

	private static final String REDIS_STATS_NAME = "global:stats:";

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private YearStatsHelper yearStatsHelper;

	private YearStats toEntity(YearStatsDTO entityDTO) {
        final YearStats entity = BeanUtil.copyProperties(entityDTO, YearStats.class);

        if (null != entityDTO.getLineValueMap() && !entityDTO.getLineValueMap().isEmpty()) {
            entity.setLineValueJson(JSONUtil.toJsonStr(entityDTO.getLineValueMap()));
        }

        return entity;
    }

    /**
     * @see com.cescloud.saas.archive.service.modular.stats.service.YearStatsService#toDTO(com.cescloud.saas.archive.api.modular.stats.entity.YearStats)
     */
    @Override
    public YearStatsDTO toDTO(YearStats entity) {
        if (null == entity) {
            return null;
        }
        final YearStatsDTO entityDTO = BeanUtil.copyProperties(entity, YearStatsDTO.class);

        if (null != entity.getLineValueJson()) {
            entityDTO.setLineValueMap(JSONUtil.toBean(entity.getLineValueJson(), Map.class));
        }

        return entityDTO;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.YearStatsService#save(com.cescloud.saas.archive.api.modular.stats.dto.YearStatsDTO)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(YearStatsDTO entityDTO) {
        // 后台不检验，由前端检验
        //checkLineAmount(entityDTO);
        final YearStatsDTO dbEntity = getByYearCode(entityDTO.getFondsCode(), entityDTO.getYearCode());
        if (null != dbEntity) {
            throw new ArchiveRuntimeException("年度[" + entityDTO.getYearCode() + "]报表已存在，请刷新再操作");
        }
        final YearStats entity = toEntity(entityDTO);
        return super.save(entity);
    }

    private void checkLineAmount(YearStatsDTO entityDTO) {
        final Map<String, Integer> lineValueMap = entityDTO.getLineValueMap();
        if (null == lineValueMap || lineValueMap.isEmpty()) {
            return;
        }
        Integer totalAmount;
        // 专职人员=各类文化程度人数总和，即 2=4+5+6=7+8+9+10+11+12+13
        final Integer amount2 = ObjectUtil.defaultIfNull(lineValueMap.get("2"), 0);
        totalAmount = countRange(lineValueMap, 4, 6);
        if (!amount2.equals(totalAmount)) {
            throw new ArchiveRuntimeException("专职人员数与各类文化程度人数[4~6]总和不相等，请检查");
        }
        totalAmount = countRange(lineValueMap, 7, 13);
        if (!amount2.equals(totalAmount)) {
            throw new ArchiveRuntimeException("专职人员数与各类文化程度人数[7~13]总和不相等，请检查");
        }
        // 室存档案数量=室存档案历史分期数量合计，即28=50+52，29=51+53
        final Integer amount28 = ObjectUtil.defaultIfNull(lineValueMap.get("28"), 0);
        totalAmount = countAmount(lineValueMap, 50, 52);
        if (!amount28.equals(totalAmount)) {
            throw new ArchiveRuntimeException("室存档案数量[28]与[50，52]总和不相等，请检查");
        }
        final Integer amount29 = ObjectUtil.defaultIfNull(lineValueMap.get("29"), 0);
        totalAmount = countAmount(lineValueMap, 51, 53);
        if (!amount29.equals(totalAmount)) {
            throw new ArchiveRuntimeException("室存档案数量[29]与[51，53]总和不相等，请检查");
        }
        //室存档案数量≥室存永久、30年（长期）档案≥永久保管，即 28≥32≥34；
        final Integer amount32 = ObjectUtil.defaultIfNull(lineValueMap.get("32"), 0);
        final Integer amount34 = ObjectUtil.defaultIfNull(lineValueMap.get("34"), 0);
        if (amount28 < amount32 || amount32 < amount34) {
            throw new ArchiveRuntimeException("室存档案数量≥室存永久、30年（长期）档案≥永久保管，即要 28≥32≥34，请检查");
        }
        // 本年利用档案（人次）=工作查考（人次）+其他利用（人次），即98=100+102；
        final Integer amount98 = ObjectUtil.defaultIfNull(lineValueMap.get("98"), 0);
        totalAmount = countAmount(lineValueMap, 100, 102);
        if (!amount98.equals(totalAmount)) {
            throw new ArchiveRuntimeException("本年利用档案（人次）=工作查考（人次）+其他利用（人次），[98]与[100，102]总和不相等，请检查");
        }
        // 本年利用档案（卷件次）=工作查考（卷件次）+其他利用（卷件次），即99=101+103
        final Integer amount99 = ObjectUtil.defaultIfNull(lineValueMap.get("99"), 0);
        totalAmount = countAmount(lineValueMap, 101, 103);
        if (!amount99.equals(totalAmount)) {
            throw new ArchiveRuntimeException("本年利用档案（卷件次）=工作查考（卷件次）+其他利用（卷件次），[99]与[101，103]总和不相等，请检查");
        }

    }

    private Integer countRange(Map<String, Integer> lineValueMap, Integer startIndex, Integer endIndex) {
        int total = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            total += ObjectUtil.defaultIfNull(lineValueMap.get(String.valueOf(i)), 0);
        }
        return total;
    }

    private Integer countAmount(Map<String, Integer> lineValueMap, Integer... indexes) {
        int total = 0;
        for (final Integer index : indexes) {
            total += ObjectUtil.defaultIfNull(lineValueMap.get(String.valueOf(index)), 0);
        }
        return total;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.YearStatsService#updateById(com.cescloud.saas.archive.api.modular.stats.dto.YearStatsDTO)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(YearStatsDTO entityDTO) {
        final YearStats entity = toEntity(entityDTO);
        return super.updateById(entity);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.YearStatsService#getByYearCode(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public YearStatsDTO getByYearCode(String fondsCode, String yearCode) {

        Assert.notEmpty(fondsCode, "全宗不能为空，请检查");
        Assert.notEmpty(yearCode, "年度不能为空，请检查");

        final YearStats entity = getBaseMapper()
            .selectOne(Wrappers.<YearStats> lambdaQuery().eq(YearStats::getFondsCode, fondsCode)
                .eq(YearStats::getYearCode, yearCode));

        if (null == entity) {
            return null;
        }

        return toDTO(entity);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.YearStatsService#getYearCodeByFondsCode(java.lang.String)
     */
    @Override
    public List<YearStats> getYearCodeByFondsCode(String fondsCode) {

        Assert.notEmpty(fondsCode, "全宗不能为空，请检查");

        final LambdaQueryWrapper<YearStats> lambdaQuery = Wrappers.<YearStats> lambdaQuery()
            .select(YearStats::getYearCode, YearStats::getId)
            .eq(YearStats::getFondsCode, fondsCode)
            .orderByDesc(YearStats::getYearCode);

        return list(lambdaQuery);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.stats.service.YearStatsService#export(java.lang.Long)
     */
    @Override
    public HSSFWorkbook export(Long id) {
        Assert.notNull(id, "ID不能为空");

        final YearStats entity = this.getById(id);

        Assert.notNull(entity, "年报不存在，或已被删除");

        HSSFWorkbook workbook = null;

        final ClassPathResource resource = new ClassPathResource("templatefile/yearstats.xls");

        try (
            POIFSFileSystem poiFile = new POIFSFileSystem(resource.getInputStream());) {
            workbook = new HSSFWorkbook(poiFile);
            workbook.setSheetName(0, entity.getYearCode());

            final HSSFSheet sheet = workbook.getSheetAt(0);

            writeHeader(sheet, entity);

            writeLineAmonut(sheet, entity);

            writeFooter(sheet, entity);

        } catch (final Exception e) {
            log.error("导出年报出错：{}", e.getMessage());
            throw new ArchiveRuntimeException("导出年报出错");
        }

        return workbook;
    }

    private void writeHeader(HSSFSheet sheet, YearStats entity) {
        int startRow = 3;
        HSSFRow row = sheet.getRow(startRow); //单位名称
        HSSFCell cell = row.getCell(0);
        cell.setCellValue(String.format("单位名称：%s", entity.getUnitName()));

        row = sheet.getRow(startRow++); //单位类别代码
        cell = row.getCell(0);
        cell.setCellValue(String.format("单位类别代码：%s", entity.getUnitCatalog()));

        sheet.getRow(startRow++); //统一社会信用代码
        cell = row.getCell(0);
        cell.setCellValue(
            String.format("统一社会信用代码：%s    %s年", entity.getUnifiedSocialCreditCode(), entity.getYearCode()));
        cell = row.getCell(3);
        cell.setCellValue(String.format("%d年1月", Integer.parseInt(entity.getYearCode()) + 1));
    }

    private void writeFooter(HSSFSheet sheet, YearStats entity) {
        //表尾
        final HSSFRow row = sheet.getRow(177);
        HSSFCell cell = row.getCell(0);
        cell.setCellValue(String.format("单位负责人：%s    填表人：%s", entity.getUnitManager(), entity.getFillingUser()));
        cell = row.getCell(2);
        cell.setCellValue(String.format("%s年", entity.getReportYear()));
        cell = row.getCell(3);
        cell.setCellValue(String.format("%s月%s日", entity.getReportMonth(), entity.getReportDay()));
    }

    private void writeLineAmonut(HSSFSheet sheet, YearStats entity) {
        final String lineValueJson = entity.getLineValueJson();
        if (StrUtil.isEmpty(lineValueJson)) {
            return;
        }

        final Map<String, Integer> lineAmountMap = JSONUtil.toBean(entity.getLineValueJson(), Map.class);

        final Map<String, Integer> lineRowMap = lineRowMap();

        lineAmountMap.forEach((key, val) -> {
            final Integer rowIndex = lineRowMap.get(key);
            final HSSFRow row = sheet.getRow(rowIndex);
            row.getCell(3).setCellValue(String.valueOf(val));
        });
    }

    private Map<String, Integer> lineRowMap() {

        final Map<String, Integer> lineRowMap = new HashMap<String, Integer>();

        Integer lineNum = continuousRange(lineRowMap, 1, 8, 3); // 9~11
        lineNum = continuousRange(lineRowMap, lineNum, 12, 3); // 13~15
        lineNum = continuousRange(lineRowMap, lineNum, 16, 7); // 17~23
        lineNum = continuousRange(lineRowMap, lineNum, 24, 6); // 25~30
        lineNum = continuousRange(lineRowMap, lineNum, 31, 8); // 32~39
        lineNum = continuousRange(lineRowMap, lineNum, 45, 11); // 46~56
        lineNum = continuousRange(lineRowMap, lineNum, 57, 4); // 58~61
        lineNum = continuousRange(lineRowMap, lineNum, 63, 4); // 64~67
        lineNum = continuousRange(lineRowMap, lineNum, 68, 3); // 69~71
        lineNum = continuousRange(lineRowMap, lineNum, 72, 4); // 73~76

        lineNum = continuousRange(lineRowMap, lineNum, 77, 1); // 78
        lineNum = continuousRange(lineRowMap, lineNum, 79, 2); // 80~81
        lineNum = continuousRange(lineRowMap, lineNum, 87, 6); // 88~93
        lineNum = continuousRange(lineRowMap, lineNum, 94, 3); // 95~97
        lineNum = continuousRange(lineRowMap, lineNum, 99, 5); // 100~104

        lineNum = continuousRange(lineRowMap, lineNum, 105, 2); // 106~107
        lineNum = continuousRange(lineRowMap, lineNum, 109, 4); // 110~113
        lineNum = continuousRange(lineRowMap, lineNum, 114, 3); // 115~117
        lineNum = continuousRange(lineRowMap, lineNum, 124, 5); // 125~129
        lineNum = continuousRange(lineRowMap, lineNum, 130, 2); // 131~132
        lineNum = continuousRange(lineRowMap, lineNum, 134, 4); // 135~138

        lineNum = continuousRange(lineRowMap, lineNum, 139, 3); // 140~142
        lineNum = continuousRange(lineRowMap, lineNum, 144, 2); // 145~146
        lineNum = continuousRange(lineRowMap, lineNum, 147, 2); // 148~149
        lineNum = continuousRange(lineRowMap, lineNum, 150, 7); // 151~157
        lineNum = continuousRange(lineRowMap, lineNum, 158, 4); // 159~162

        lineNum = continuousRange(lineRowMap, lineNum, 168, 2); // 169~170
        lineNum = continuousRange(lineRowMap, lineNum, 171, 1); // 172
        lineNum = continuousRange(lineRowMap, lineNum, 173, 3); // 174~176

        return lineRowMap;

    }

    private Integer continuousRange(Map<String, Integer> lineRowMap, Integer beginLine, Integer begin, Integer range) {

        String key;
        Integer val;
        for (int i = 0; i < range; i++) {
            key = String.valueOf(beginLine + i);
            val = begin + i;
            if (log.isDebugEnabled()) {
                log.debug(String.format("excel行号[%s]=[%d]", key, val));
            }
            lineRowMap.put(key, val);
        }

        return beginLine + range;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByTenantId(Long tenantId){
        return getBaseMapper().removeByTenantId(tenantId);
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R yearStatsCount(String fondsCode , Long yearStatsId){
		YearStats yearStats = this.getById(yearStatsId);
		if(ObjectUtil.isNull(yearStats)){
			return new R().fail(null, "未找到该年报信息!请检查条件!");
		}
		if(redisTemplate.hasKey(REDIS_STATS_NAME + yearStatsId)){
			return new R(null, "正在统计中....");
		}

		//开启异步线程
		yearStatsHelper.runAsync(yearStats,fondsCode);
		return new R(null, "正在统计中....");
	}


}
