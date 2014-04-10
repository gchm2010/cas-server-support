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
package com.buession.cas.authentication.handler.support;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * 简单数据库查询
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public class DatabaseQueryAuthenticationHandler extends AbstractJdbcAuthenticationHandler {

	/**
	 * 用户认证查询 SQL
	 */
	@NotNull
	protected String sql;

	private final static Logger logger = LoggerFactory
			.getLogger(DatabaseQueryAuthenticationHandler.class);

	/**
	 * 返回用户认证查询 SQL
	 * 
	 * @return 用户认证查询 SQL
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * 设置用户认证查询 SQL
	 * 
	 * @param sql
	 *        用户认证查询 SQL
	 */
	public void setSql(final String sql) {
		this.sql = sql;
	}

	@Override
	protected boolean authenticateUsernamePasswordInternal(
			final UsernamePasswordCredentials credentials) throws AuthenticationException {
		String username = getPrincipalNameTransformer().transform(credentials.getUsername());
		String password = credentials.getPassword();

		try {
			PasswordEncoder passwordEncoder = getPasswordEncoder();

			logger.debug("Qurey SQL: {}, username: {}", sql, username);
			final String dbPassword = jdbcTemplate.queryForObject(sql, String.class, username);
			return dbPassword != null
					&& dbPassword.equals(passwordEncoder.encode(password == null ? "" : password));
		} catch (final IncorrectResultSizeDataAccessException e) {
			return false;
		}
	}

}