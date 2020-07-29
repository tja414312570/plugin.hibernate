package com.yanan.frame.jdb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 姝ゆ敞瑙ｅ彧鑳界敤浜巉ield 姝ゆ敞瑙ｇ敤浜庝繚瀛樼被鐨勪俊鎭�(浠ヤ笅鎵�鏈夌殑鈥滅┖鈥濆潎涓� 绌哄瓧绗︿覆锛岄潪null) value 璇ュ�煎彲浠ュ～鎵�鏈夌殑绫诲瀷 Type 鏁版嵁绫诲瀷
 * 
 * @author Administrator
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * 是否忽略此域，忽略之后将不映射与数据表的关系
	 * @return boolean
	 */
	boolean ignore() default false;
	/**
	 * 列名
	 * @return cloumn
	 */
	String name() default "";
	
	String value() default "";
	/**
	 * 列的类型
	 * @return 类型
	 */
	String type() default "";
	/**
	 * 列的内容的长度
	 * @return 长度
	 */
	int length() default 255;
	/**
	 * 数字等类型的长度
	 * @return 长度
	 */
	int size() default 11;

	String format() default "";
	/**
	 * 是否非空
	 * @return 是否非空
	 */
	boolean Not_Null() default false;

	boolean point() default false;
	/**
	 * 是否将列定义为自增
	 * @return 是否定义自增
	 */
	boolean Auto_Increment() default false;
	/**
	 * 是否将列定义为主键
	 * @return 主键
	 */
	boolean Primary_Key() default false;
	/**
	 * 是否将列添加到unique约束
	 * @return 约束
	 */
	boolean unique() default false;

	boolean Not_Sign() default true;

	boolean Auto_Fill() default false;
	/**
	 * 列的注释
	 * @return 注释
	 */
	String Annotations() default "";

	String Default() default "";

	boolean encrypt() default false;
    /**
     * 列的字符集
     * @return 字符集
     */
	String collate() default "";
	/**
	 * 列的编码
	 * @return 编码
	 */
	String charset() default "";
	
}