package com.cesgroup.bpm.expr;

import java.util.List;

/**
 * 表达式处理接口
 * 
 * @author 国栋
 *
 */
public interface ExprProcessor {

    /**
     * 处理表达式
     */
    List<String> process(List<String> left, List<String> right, String operation);

    /**
     * 处理表达式
     */
    List<String> process(String text);
}
