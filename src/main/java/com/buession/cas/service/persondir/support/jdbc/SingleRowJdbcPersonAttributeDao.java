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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.CaseInsensitiveAttributeNamedPersonImpl;
import org.jasig.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.jasig.services.persondir.support.jdbc.ColumnMapParameterizedRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * An {@link org.jasig.services.persondir.IPersonAttributeDao} implementation that maps from
 * column names in the result of a SQL query to attribute names.
 * You must set a Map from column names to attribute names and only column names
 * appearing as keys in that map will be used.
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public class SingleRowJdbcPersonAttributeDao extends
		AbstractJdbcPersonAttributeDao<Map<String, Object>> {

	private final static ParameterizedRowMapper<Map<String, Object>> MAPPER = new ColumnMapParameterizedRowMapper(
			true);

	@Override
	protected List<IPersonAttributes> parseAttributeMapFromResults(
			List<Map<String, Object>> queryResults, String username) {
		final List<IPersonAttributes> peopleAttributes = new ArrayList<IPersonAttributes>(
				queryResults.size());

		for (final Map<String, Object> queryResult : queryResults) {
			final Map<String, List<Object>> multivaluedQueryResult = MultivaluedPersonAttributeUtils
					.toMultivaluedMap(queryResult);
			final IPersonAttributes person;

			if (username != null) {
				person = new CaseInsensitiveNamedPersonImpl(username, multivaluedQueryResult);
			} else {
				// Create the IPersonAttributes doing a best-guess at a userName attribute
				final String userNameAttribute = getConfiguredUserNameAttribute();

				person = new CaseInsensitiveAttributeNamedPersonImpl(userNameAttribute,
						multivaluedQueryResult);
			}

			peopleAttributes.add(person);
		}

		return peopleAttributes;
	}

	@Override
	protected ParameterizedRowMapper<Map<String, Object>> getRowMapper() {
		return MAPPER;
	}

}