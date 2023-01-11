package com.eddie.mall_goods.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.eddie.common.validation.ListValue;
import com.eddie.common.validation.SaveGroup;
import com.eddie.common.validation.UpdateGroup;
import com.eddie.common.validation.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import javax.validation.constraints.*;

/**
 * 品牌
 * TODO 后端使用JSR303规范(注解方式)进行数据校验 SpringBoot版本要求不超过2.0
 * 可以使用javax.validation提供的注解 也可以自定义校验规则RegExp（正则表达式）以及错误提示message
 * @author Eddie
 * @email 20001207@iCloud.com
 * @date 2022-12-15 22:11:48
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId//标识主键ID
	@NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class})//update的时候会进行验证 一定要携带
	@Null(message = "新增不能指定id",groups = {SaveGroup.class})//save的时候会进行验证 一定不能携带
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交",groups = {SaveGroup.class,UpdateGroup.class})//update和save的时候会进行验证 一定要携带
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(groups = {SaveGroup.class})//save的时候会进行验证 一定要携带
	@URL(message = "品牌LOGO要求必须是一个规范的URL地址",groups = {SaveGroup.class,UpdateGroup.class})//update和save的时候会进行验证 一定要携带
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {SaveGroup.class, UpdateStatusGroup.class})//UpdateStatus和save的时候会进行验证 一定要携带
	//自定义Validation注解
	@ListValue(vals = {0,1},groups = {SaveGroup.class, UpdateStatusGroup.class})//UpdateStatus和save的时候会进行验证 一定要携带
//	@Min(0)
//	@Max(1)
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {SaveGroup.class})//save的时候会进行验证 一定不能携带
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须是单个的字母",groups={SaveGroup.class,UpdateGroup.class})//update和save的时候会进行验证 一定要携带
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {SaveGroup.class})//save的时候会进行验证 一定不能携带
	@Min(value = 0,message = "排序必须大于等于0",groups={SaveGroup.class,UpdateGroup.class})//update和save的时候会进行验证 一定要携带
	private Integer sort;

}
