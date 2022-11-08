package com.cescloud.saas.archive.service.modular.syssetting.annotation;

import java.lang.annotation.*;

/**
 * 更新租户配置信息注解,一般用于Service层方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysSettingReload {
	String value() default "";
}
