package com.cescloud.saas.archive.service.modular.report.util;
/*
@auth xaz
@date 2019/5/8 - 18:38
*/

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.report.entity.Report;
import com.cescloud.saas.archive.api.modular.report.entity.ReportMetadata;
import com.cescloud.saas.archive.api.modular.report.entity.ReportTable;
import com.cescloud.saas.archive.common.constants.ArchiveLayerEnum;
import com.cescloud.saas.archive.common.constants.ReportConstants;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//ireport工具类
public class JasperUtil {

	private JasperUtil() {

	}

    //打印报表文件类型
    public static enum DocType {
        pdf, excel, word
    }

    //根据文件类型分别执行不同的导出方法
    public static void export(String type, String defaultFilename, JasperPrint jasperPrint ,
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            if (DocType.excel.name().equalsIgnoreCase(type)) {
                exportExcel(jasperPrint, defaultFilename, request, response);
            } else if (DocType.pdf.name().equalsIgnoreCase(type)) {
                exportPdf(jasperPrint, defaultFilename, request, response);
            } else if (DocType.word.name().equalsIgnoreCase(type)) {
                exportWord(jasperPrint, defaultFilename, request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //导出excel
    public static void exportExcel(JasperPrint jasperPrint,
                                   String defaultFilename, HttpServletRequest request,
                                   HttpServletResponse response) throws IOException, JRException {
        /*
         * 设置头信息
         */
        response.setContentType("application/vnd.ms-excel");
        String defaultname = null;
        if (defaultFilename.trim() != null && defaultFilename != null) {
            defaultname = defaultFilename + ".xls";
        } else {
            defaultname = "export.xls";
        }
        String fileName = java.net.URLEncoder.encode(defaultname, "UTF-8");;
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName + "\"");

        ServletOutputStream ouputStream = response.getOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ouputStream);
        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
                Boolean.TRUE); // 删除记录最下面的空行
        exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET,
                Boolean.FALSE);// 删除多余的ColumnHeader
        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                Boolean.FALSE);// 显示边框
//        exporter.exportReport();
//        exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
//        exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
//        exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);//TRUE背景设置为白色
//        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
        //设置Excel的sheet名称
        String sheetName = "sheet1";
        String[] sheetNames = { sheetName };
        exporter.setParameter(JRXlsExporterParameter.SHEET_NAMES, sheetNames);
        exporter.exportReport();
        ouputStream.flush();
    }

    //导出pdf
    private static void exportPdf(JasperPrint jasperPrint,
                                  String defaultFilename, HttpServletRequest request,
                                  HttpServletResponse response) throws IOException, JRException {
        response.setContentType("application/pdf");
        String defaultname = null;
        if (defaultFilename.trim() != null && defaultFilename != null) {
            defaultname = defaultFilename + ".pdf";
        } else {
            defaultname = "export.pdf";
        }
        String fileName = java.net.URLEncoder.encode(defaultname, "UTF-8");;
        response.setHeader("Content-disposition", "attachment; filename="
                + fileName);
        ServletOutputStream ouputStream = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, ouputStream);
        ouputStream.flush();
    }

    //导出word
    public static void exportWord(JasperPrint jasperPrint,
                                   String defaultFilename, HttpServletRequest request,
                                   HttpServletResponse response) throws JRException, IOException {
        response.setContentType("application/msword;charset=utf-8");
        String defaultname = null;
        if (defaultFilename.trim() != null && defaultFilename != null) {
            defaultname = defaultFilename + ".doc";
        } else {
            defaultname = "export.doc";
        }
        String fileName = java.net.URLEncoder.encode(defaultname, "UTF-8");;
        response.setHeader("Content-disposition", "attachment; filename="
                + fileName);
        JRRtfExporter exporter = new JRRtfExporter();
        OutputStream out = response.getOutputStream();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
        exporter.exportReport();
        out.flush();
    }

    //构建ireport报表模板文件字符串
    public static String getiReportModelStr(String reportTopic, List<ReportTable> reportTableList, List<ReportMetadata> reportMetadataList, Report report) {
        StringBuffer reportModel = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"  ?>" + "\r\n");
        Integer reportPageLines = report.getReportPageLines();
		String[] pageField = new String[0];
		Map<String,String> reportMetadataMap = new HashMap();
		if(ObjectUtil.isNotNull(report.getPageField()) && !"".equals(report.getPageField())) {
			pageField = report.getPageField().split(",");
		}
        reportModel.append("<!-- Created with Jaspersoft Studio version 6.10.0.final using JasperReports Library version 6.10.0 -->" + "\r\n");
        reportModel.append("<!DOCTYPE jasperReport PUBLIC \"//JasperReports//DTD Report Design//EN\" \"http://jasperreports.sourceforge.net/dtds/jasperreport.dtd\">" + "\r\n");
        reportModel.append("<jasperReport                     " + "\r\n");
        reportModel.append("       name=\"" + reportTopic + "\"   " + "\r\n");
        reportModel.append("       printOrder=\"Vertical\"    " + "\r\n");
        reportModel.append("       orientation=\"Portrait\"   " + "\r\n");
        reportModel.append("       pageWidth=\"595\"          " + "\r\n");
        reportModel.append("       pageHeight=\"842\"         " + "\r\n");
        reportModel.append("       columnWidth=\"535\"        " + "\r\n");
        reportModel.append("       columnSpacing=\"0\"        " + "\r\n");
        reportModel.append("       leftMargin=\"30\"          " + "\r\n");
        reportModel.append("       rightMargin=\"30\"         " + "\r\n");
        reportModel.append("       topMargin=\"20\"           " + "\r\n");
        reportModel.append("       bottomMargin=\"20\"        " + "\r\n");
        reportModel.append("       whenNoDataType=\"NoPages\" " + "\r\n");
        reportModel.append("       isTitleNewPage=\"false\"   " + "\r\n");
        reportModel.append("       isSummaryNewPage=\"false\">" + "\r\n");
        reportModel.append("<property name=\"ireport.scriptlethandling\" value=\"2\" /> " + "\r\n");
        reportModel.append("<property name=\"ireport.encoding\" value=\"UTF-8\" />      " + "\r\n");
        reportModel.append("<import value=\"java.util.*\" />                            " + "\r\n");
        reportModel.append("<import value=\"net.sf.jasperreports.engine.*\" />          " + "\r\n");
        reportModel.append("<import value=\"net.sf.jasperreports.engine.data.*\" />     " + "\r\n");
        /** 分页规则参数 **/
        reportModel.append("<parameter name=\""+ ReportConstants.PARAMETER_PAGE_SIZE +"\" class=\"java.lang.Integer\" isForPrompting=\"false\">" + "\r\n");
        reportModel.append("      <parameterDescription><![CDATA[每页显示数，默认为10，请勿修改、删除]]></parameterDescription>" + "\r\n");
        reportModel.append("      <defaultValueExpression><![CDATA[10]]></defaultValueExpression>" + "\r\n");
        reportModel.append("</parameter>" + "\r\n");
        reportModel.append("<parameter name=\""+ ReportConstants.PARAMETER_TOTAL_NUMBER +"\" class=\"java.lang.Integer\" isForPrompting=\"false\">" + "\r\n");
        reportModel.append("      <parameterDescription><![CDATA[总记录数，默认为0，请勿修改、删除]]></parameterDescription>" + "\r\n");
        reportModel.append("      <defaultValueExpression><![CDATA[0]]></defaultValueExpression>" + "\r\n");
        reportModel.append("</parameter>" + "\r\n");
        reportModel.append("<field name=\"" + ReportConstants.FIELD_PAGE_GROUP_NAME + "\" class=\"java.lang.String\">" + "\r\n");
        reportModel.append("      <fieldDescription><![CDATA["+ ReportConstants.FIELD_PAGE_GROUP +"]]></fieldDescription>" + "\r\n");
        reportModel.append("</field>" + "\r\n");

        if (reportMetadataList.size() > 0) {
            reportMetadataList.forEach(reportMetadata -> {
                String layer = reportMetadata.getStorageLocate().substring((reportMetadata.getStorageLocate().lastIndexOf("_"))+1).toUpperCase();
				if(StrUtil.isBlank(layer)){
                    layer = reportMetadata.getStorageLocate().substring(0,1).toUpperCase();
                }
                String layerChinese =  ObjectUtil.isNotNull(ArchiveLayerEnum.getEnum(layer)) ? ArchiveLayerEnum.getEnum(layer).getName():"";
				String columnPrefix = layer.toLowerCase();
				if (CommonConstants.REPORT_TYPE_COMPLEX.equals(report.getReportType())) {
				    if(StrUtil.isBlank(layerChinese)){
                        reportModel.append("<field name=\"" +  reportMetadata.getMetadataChinese() + "\" class=\"java.lang.String\">" + "\r\n");
                    }else {
                        reportModel.append("<field name=\"" + layerChinese + "_" + reportMetadata.getMetadataChinese() + "\" class=\"java.lang.String\">" + "\r\n");
                    }
					if (layer.equals(ArchiveLayerEnum.FOLDER.getValue())) {
						reportMetadataMap.put(reportMetadata.getMetadataEnglish(), layerChinese + "_" + reportMetadata.getMetadataChinese());
					}
					reportModel.append("      <fieldDescription><![CDATA["+ columnPrefix + "_" + reportMetadata.getMetadataEnglish()+"]]></fieldDescription> " + "\r\n");
				}else{
					reportModel.append("<field name=\"" + reportMetadata.getMetadataChinese() + "\" class=\"java.lang.String\">" + "\r\n");
					reportMetadataMap.put(reportMetadata.getMetadataEnglish(),reportMetadata.getMetadataChinese());
					reportModel.append("      <fieldDescription><![CDATA["+ reportMetadata.getMetadataEnglish()+"]]></fieldDescription> " + "\r\n");
				}
                reportModel.append("</field>          " + "\r\n");
			});

			/** 生成字段时，自动生成group，并且忽略大小 **/
            reportModel.append("<group name=\""+ ReportConstants.GROUP_PAGE +"\">" + "\r\n");
            reportModel.append("      <groupExpression><![CDATA[$F{" + ReportConstants.FIELD_PAGE_GROUP_NAME + "}]]></groupExpression>" + "\r\n");
            reportModel.append("      <groupHeader>" + "\r\n" );
            reportModel.append("            <band height=\"50\"/>" + "\r\n" );
            reportModel.append("      </groupHeader>" + "\r\n" );
            reportModel.append("      <groupFooter>" + "\r\n" );
            reportModel.append("            <band height=\"50\">" + "\r\n" );
            reportModel.append("                  <break>" + "\r\n" );
            reportModel.append("                        <reportElement x=\"0\" y=\"0\" width=\"99\" height=\"1\">" + "\r\n");
            reportModel.append("                        </reportElement>" + "\r\n");
            reportModel.append("                  </break>" + "\r\n" );
            reportModel.append("            </band>" + "\r\n" );
            reportModel.append("      </groupFooter>" + "\r\n" );
            reportModel.append("</group>" + "\r\n" );

            reportModel.append("       <background>                                        " + "\r\n");
            reportModel.append("            <band height=\"0\"  isSplitAllowed=\"true\" >  " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </background>                                       " + "\r\n");
            reportModel.append("       <title>                                             " + "\r\n");
            reportModel.append("            <band height=\"50\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </title>                                            " + "\r\n");
            reportModel.append("       <pageHeader>                                        " + "\r\n");
            reportModel.append("            <band height=\"50\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </pageHeader>                                       " + "\r\n");
            reportModel.append("       <columnHeader>                                      " + "\r\n");
            reportModel.append("            <band height=\"30\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </columnHeader>                                     " + "\r\n");
            reportModel.append("       <detail>                                            " + "\r\n");
            reportModel.append("            <band height=\"100\"  isSplitAllowed=\"true\" >" + "\r\n");
			reportModel.append("            	<break>                                    " + "\r\n");
			reportModel.append("            		<reportElement x=\"0\" y=\"0\" width=\"99\" height=\"1\">" + "\r\n");
			reportModel.append("            			<printWhenExpression><![CDATA[new Boolean($V{"+ ReportConstants.GROUP_PAGE +"_COUNT}%$P{"+ ReportConstants.PARAMETER_PAGE_SIZE +"}==0 && $P{"+ ReportConstants.PARAMETER_TOTAL_NUMBER +"} > $V{REPORT_COUNT})]]></printWhenExpression> "+ "\r\n");
			reportModel.append("            		</reportElement>                       " + "\r\n");
			reportModel.append("            	</break>                                   " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </detail>                                           " + "\r\n");
            reportModel.append("       <columnFooter>                                      " + "\r\n");
            reportModel.append("            <band height=\"30\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </columnFooter>                                     " + "\r\n");
            reportModel.append("       <pageFooter>                                        " + "\r\n");
            reportModel.append("            <band height=\"50\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </pageFooter>                                       " + "\r\n");
            reportModel.append("       <lastPageFooter>                                    " + "\r\n");
            reportModel.append("            <band height=\"50\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </lastPageFooter>                                   " + "\r\n");
            reportModel.append("       <summary>                                           " + "\r\n");
            reportModel.append("            <band height=\"50\"  isSplitAllowed=\"true\" > " + "\r\n");
            reportModel.append("            </band>                                        " + "\r\n");
            reportModel.append("       </summary>                                          " + "\r\n");
            reportModel.append("</jasperReport>                                            " + "\r\n");
            return reportModel.toString();
        }
        return null;
    }
}
