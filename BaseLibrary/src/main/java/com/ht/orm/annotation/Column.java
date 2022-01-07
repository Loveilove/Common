package com.ht.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {

	String name() default "";
	
	int length() default 0;
	
	String defaultValue() default "";

	String type() default "";
	
}
