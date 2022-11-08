package com.cescloud.saas.archive.service.modular.fwimp.service.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.cescloud.saas.archive.service.modular.fwimp.service.item.ExcelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 读取excel表头监听类
 * @author hyq
 * @date 2021/4/14 - 15:41
 **/

public class ExcelReadListener extends AnalysisEventListener {


	private List<Object> dataList = new ArrayList<Object>();
	/**
	 * 每解析一行会回调invoke()方法
	 　　　* 通过AbalysisContext可以获取当前sheet,当前行等数据
	 * @param object
	 * @param context
	 */
	@Override
	public void invoke(Object object, AnalysisContext context) {
		dataList.add(object);
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext context) {
		//dosomething  整个excel解析结束后会执行这个方法
	}


	public List<Object> getDataList(){
		return dataList;
	}
}