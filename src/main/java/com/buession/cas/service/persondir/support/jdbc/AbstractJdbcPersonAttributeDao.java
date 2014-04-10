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
package com.buession.cas.service.persondir.support.jdbc;

import java.util.List;
import java.util.regex.Matcher;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;
import org.jasig.services.persondir.support.QueryType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Provides common logic for executing a JDBC based query including building the WHERE clause SQL
 * string.
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public abstract class AbstractJdbcPersonAttributeDao<R> extends
		AbstractQueryPersonAttributeDao<PartialWhereClause> {

	/**
	 * JdbcTemplate
	 */
	@NotNull
	protected JdbcTemplate jdbcTemplate;

	/**
	 * 查询模板
	 */
	@NotNull
	protected String queryTemplate;

	/**
	 * 查询方式（AND 或 OR）
	 */
	protected QueryType queryType = QueryType.AND;

	public AbstractJdbcPersonAttributeDao() {

	}

	/**
	 * @param jdbcTemplate
	 *        JdbcTemplate
	 */
	public AbstractJdbcPersonAttributeDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param jdbcTemplate
	 *        JdbcTemplate
	 * @param queryTemplate
	 *        查询模板
	 */
	public AbstractJdbcPersonAttributeDao(JdbcTemplate jdbcTemplate, String queryTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.queryTemplate = queryTemplate;
	}

	/**
	 * 返回 JdbcTemplate
	 * 
	 * @return JdbcTemplate
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
	 * 返回查询模板
	 * 
	 * @return 查询模板
	 */
	public String getQueryTemplate() {
		return queryTemplate;
	}

	/**
	 * 设置查询模板
	 * 
	 * @param queryTemplate
	 *        查询模板
	 */
	public void setQueryTemplate(String queryTemplate) {
		this.queryTemplate = queryTemplate;
	}

	/**
	 * 返回查询方式（AND 或 OR）
	 * 
	 * @return 查询方式
	 */
	public QueryType getQueryType() {
		return queryType;
	}

	/**
	 * 设置查询方式（AND 或 OR）
	 * 
	 * @param queryType
	 *        查询方式
	 */
	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	/**
	 * @param queryResults
	 *        查询结果集
	 * @param username
	 *        查询用户名
	 * @return
	 */
	protected abstract List<IPersonAttributes> parseAttributeMapFromResults(
			final List<R> queryResults, String username);

	/**
	 * @return
	 */
	protected abstract ParameterizedRowMapper<R> getRowMapper();

	@Override
	protected List<IPersonAttributes> getPeopleForQuery(PartialWhereClause queryBuilder,
			String queryUserName) {
		final ParameterizedRowMapper<R> rowMapper = getRowMapper();
		List<R> results = null;

		if (queryBuilder != null) {
			final String sql = queryTemplate.replaceAll("\\{0\\}", queryBuilder.sql.toString());

			results = jdbcTemplate.query(sql, rowMapper, queryBuilder.arguments.toArray());
			logger.debug("Executed '" + sql + "' with arguments " + queryBuilder.arguments
					+ " and got results " + results);
		} else {
			results = jdbcTemplate.query(queryTemplate, rowMapper);
			logger.debug("Executed '" + queryTemplate + "' and got results " + results);
		}

		return parseAttributeMapFromResults(results, queryUserName);
	}

	@Override
	protected PartialWhereClause appendAttributeToQuery(PartialWhereClause queryBuilder,
			String dataAttribute, List<Object> queryValues) {
		for (final Object queryValue : queryValues) {
			final String queryString = queryValue != null ? queryValue.toString() : null;

			if (StringUtils.isNotBlank(queryString)) {
				if (queryBuilder == null) {
					queryBuilder = new PartialWhereClause();
				} else if (queryBuilder.sql.length() > 0) {
					queryBuilder.sql.append(" ").append(queryType.toString()).append(" ");
				}

				// Convert to SQL wildcard
				final Matcher queryValueMatcher = IPersonAttributeDao.WILDCARD_PATTERN
						.matcher(queryString);
				final String formattedQueryValue = queryValueMatcher.replaceAll("%");

				queryBuilder.arguments.add(formattedQueryValue);

				if (dataAttribute != null) {
					queryBuilder.sql.append(dataAttribute);
					if (formattedQueryValue.equals(queryString)) {
						queryBuilder.sql.append(" = ");
					} else {
						queryBuilder.sql.append(" LIKE ");
					}
				}

				queryBuilder.sql.append("?");
			}
		}

		return queryBuilder;
	}

}