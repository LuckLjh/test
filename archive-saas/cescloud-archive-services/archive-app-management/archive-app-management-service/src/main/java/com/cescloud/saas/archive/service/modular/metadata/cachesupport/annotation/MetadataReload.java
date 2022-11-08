package com.cescloud.saas.archive.service.modular.metadata.cachesupport.annotation;

import java.lang.annotation.*;

/**
 * 更新元字段信息注解,一般用于Service层方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MetadataReload {
	String value() default "";
}
