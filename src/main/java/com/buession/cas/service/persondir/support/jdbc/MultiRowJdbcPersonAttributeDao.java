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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.jasig.services.persondir.support.jdbc.ColumnMapParameterizedRowMapper;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * An {@link org.jasig.services.persondir.IPersonAttributeDao} implementation that maps attribute
 * names and values from name and value column pairs.
 * This class expects 1 to N row results for a query, with each row containing 1 to N name
 * value attribute mappings and the userName of the user the attributes are for. This contrasts
 * {@link org.jasig.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao} which
 * expects a single row result for a user query
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public class MultiRowJdbcPersonAttributeDao extends
		AbstractJdbcPersonAttributeDao<Map<String, Object>> {

	private final static ParameterizedRowMapper<Map<String, Object>> MAPPER = new ColumnMapParameterizedRowMapper();

	private Map<String, Set<String>> nameValueColumnMappings = null;

	/**
	 * Return the nameValueColumnMappings
	 * 
	 * @return the nameValueColumnMappings
	 */
	public Map<String, Set<String>> getNameValueColumnMappings() {
		return nameValueColumnMappings;
	}

	/**
	 * set the nameValueColumnMappings
	 * 
	 * @param nameValueColumnMappings
	 */
	public void setNameValueColumnMappings(Map<String, Set<String>> nameValueColumnMappings) {
		this.nameValueColumnMappings = nameValueColumnMappings;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<IPersonAttributes> parseAttributeMapFromResults(
			List<Map<String, Object>> queryResults, String queryUserName) {
		final Map<String, Map<String, List<Object>>> peopleAttributesBuilder = LazyMap.decorate(
				new LinkedHashMap<String, Map<String, List<Object>>>(),
				new LinkedHashMapFactory<String, List<Object>>());

		final String userNameAttribute = this.getConfiguredUserNameAttribute();

		for (final Map<String, Object> queryResult : queryResults) {
			final String userName;  // Choose a username from the best available option

			if (this.isUserNameAttributeConfigured() && queryResult.containsKey(userNameAttribute)) {
				// Option #1: An attribute is named explicitly in the config,
				// and that attribute is present in the results from LDAP; use it
				final Object userNameValue = queryResult.get(userNameAttribute);
				userName = userNameValue.toString();
			} else if (queryUserName != null) {
				// Option #2: Use the userName attribute provided in the query
				// parameters. (NB: I'm not entirely sure this choice is
				// preferable to Option #3. Keeping it because it most closely
				// matches the legacy behavior there the new option -- Option #1
				// -- doesn't apply. ~drewwills)
				userName = queryUserName;
			} else if (queryResult.containsKey(userNameAttribute)) {
				// Option #3: Create the IPersonAttributes useing the default
				// userName attribute, which we know to be present
				final Object userNameValue = queryResult.get(userNameAttribute);
				userName = userNameValue.toString();
			} else {
				throw new BadSqlGrammarException("No userName column named '" + userNameAttribute
						+ "' exists in result set and no userName provided in query Map",
						this.getQueryTemplate(), null);
			}

			final Map<String, List<Object>> attributes = peopleAttributesBuilder.get(userName);

			// Iterate over each attribute column mapping to get the data from the row
			for (final Map.Entry<String, Set<String>> columnMapping : this.nameValueColumnMappings
					.entrySet()) {
				final String keyColumn = columnMapping.getKey();

				// Get the attribute name for the specified column
				final Object attrNameObj = queryResult.get(keyColumn);
				if (attrNameObj == null && !queryResult.containsKey(keyColumn)) {
					throw new BadSqlGrammarException("No attribute key column named '" + keyColumn
							+ "' exists in result set", this.getQueryTemplate(), null);
				}
				final String attrName = String.valueOf(attrNameObj);

				// Get the columns containing the values and add all values to a List
				final Set<String> valueColumns = columnMapping.getValue();
				final List<Object> attrValues = new ArrayList<Object>(valueColumns.size());
				for (final String valueColumn : valueColumns) {
					final Object attrValue = queryResult.get(valueColumn);
					if (attrValue == null && !queryResult.containsKey(valueColumn)) {
						throw new BadSqlGrammarException("No attribute value column named '"
								+ valueColumn + "' exists in result set", this.getQueryTemplate(),
								null);
					}

					attrValues.add(attrValue);
				}

				// Add the name/values to the attributes Map
				MultivaluedPersonAttributeUtils.addResult(attributes, attrName, attrValues);
			}
		}

		// Convert the builder structure into a List of IPersons
		final List<IPersonAttributes> people = new ArrayList<IPersonAttributes>(
				peopleAttributesBuilder.size());

		for (final Map.Entry<String, Map<String, List<Object>>> mappedAttributesEntry : peopleAttributesBuilder
				.entrySet()) {
			final String userName = mappedAttributesEntry.getKey();
			final Map<String, List<Object>> attributes = mappedAttributesEntry.getValue();
			final IPersonAttributes person = new NamedPersonImpl(userName, attributes);
			people.add(person);
		}

		return people;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.buession.cas.service.persondir.support.jdbc.AbstractJdbcPersonAttributeDao#getRowMapper()
	 */
	@Override
	protected ParameterizedRowMapper<Map<String, Object>> getRowMapper() {
		return MAPPER;
	}

	private static final class LinkedHashMapFactory<K, V> implements Factory {
		@Override
		public Map<K, V> create() {
			return new LinkedHashMap<K, V>();
		}
	}

}