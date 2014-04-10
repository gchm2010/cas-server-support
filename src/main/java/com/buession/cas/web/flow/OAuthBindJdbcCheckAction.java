/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 
 * (the "License"); you may not use this file except in compliance with the License. You may obtain 
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * =================================================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Apache Software Foundation. For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * +------------------------------------------------------------------------------------------------+
 * | License: http://cas-server-support.buession.com.cn/LICENSE 									|
 * | Author: Yong.Teng <webmaster@buession.com> 													|
 * | Copyright @ 2013-2014 Buession.com Inc.														|
 * +------------------------------------------------------------------------------------------------+
 */
package com.buession.cas.web.flow;

import javax.validation.constraints.NotNull;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Yong.Teng <webmaster@buession.com>
 */
public abstract class OAuthBindJdbcCheckAction extends OAuthBindCheckAction {

	/**
	 * JdbcTemplate
	 */
	@NotNull
	protected JdbcTemplate jdbcTemplate;

	/**
	 * 检测用户是否进行绑定的 SQL 语句
	 */
	@NotNull
	protected String sql;

	/**
	 * 返回 JdbcTemplate
	 * 
	 * @return
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * 设置 JdbcTemplate
	 * 
	 * @param jdbcTemplate
	 *        JdbcTemplate
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 返回检测用户是否进行绑定的 SQL 语句
	 * 
	 * @return 检测用户是否进行绑定的 SQL 语句
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * 设置检测用户是否进行绑定的 SQL 语句
	 * 
	 * @param sql
	 *        检测用户是否进行绑定的 SQL 语句
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

}