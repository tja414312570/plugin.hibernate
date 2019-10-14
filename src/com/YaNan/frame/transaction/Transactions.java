package com.YaNan.frame.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;

/**
 * 事物注解
 * @author yanan
 *
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactions {
	/**
	 * 回滚异常
	 * @return
	 */
	Class<?>[] value() default {SQLException.class};
	
	/**
	 * 事物传播行为
	 */
	TransactionPropagion propagion() default TransactionPropagion.PROPAGATION_REQUIRED;
	
	/**
	 * 事物隔离级别
	 */
	TransactionIsolocation isolocation() default TransactionIsolocation.TRANSACTION_DEFAULT;
}
