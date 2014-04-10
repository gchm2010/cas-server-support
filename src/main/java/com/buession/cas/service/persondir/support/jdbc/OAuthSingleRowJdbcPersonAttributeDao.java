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
 * | License: http://cas-server-support.buession.com.cn/LICENSE 										|
 * | Author: Yong.Teng <webmaster@buession.com> 													|
 * | Copyright @ 2013-2014 Buession.com Inc.														|
 * +------------------------------------------------------------------------------------------------+
 */
package com.buession.cas.service.persondir.support.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.CaseInsensitiveAttributeNamedPersonImpl;
import org.jasig.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.jasig.services.persondir.support.MultivaluedPersonAttributeUtils;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.springframework.dao.support.DataAccessUtils;

/**
 * @author Yong.Teng <webmaster@buession.com>
 */
public abstract class OAuthSingleRowJdbcPersonAttributeDao extends SingleRowJdbcPersonAttributeDao {

	@Override
	public IPersonAttributes getPerson(String uid) {
		Validate.notNull(uid, "uid may not be null.");

		// Generate the ProviderId map for the uid
		final ProviderId providerId = convertAttributesMap(uid);

		// Run the query using the seed
		final Set<IPersonAttributes> people = getPeopleProvider(providerId);

		// Ensure a single result is returned
		IPersonAttributes person = DataAccessUtils.singleResult(people);
		if (person == null) {
			return null;
		}

		// Force set the name of the returned IPersonAttributes if it isn't provided in the return
		// object
		if (person.getName() == null) {
			person = new NamedPersonImpl(uid, person.getAttributes());
		}

		return person;
	}

	public final Set<IPersonAttributes> getPeopleProvider(final ProviderId providerId) {
		Validate.notNull(providerId, "ProviderId may not be null.");
		Validate.notNull(providerId.getProviderName(), "Provider Name may not be null.");
		Validate.notNull(providerId.getId(), "Provider Id may not be null.");

		// Execute the query in the subclass
		final List<IPersonAttributes> unmappedPeople = getPeopleForQuery(providerId);
		if (unmappedPeople == null) {
			return null;
		}

		// Map the attributes of the found people according to resultAttributeMapping if it is set
		final Set<IPersonAttributes> mappedPeople = new LinkedHashSet<IPersonAttributes>();
		for (final IPersonAttributes unmappedPerson : unmappedPeople) {
			final IPersonAttributes mappedPerson = this.mapPersonAttributes(unmappedPerson);
			mappedPeople.add(mappedPerson);
		}

		return Collections.unmodifiableSet(mappedPeople);
	}

	protected List<IPersonAttributes> getPeopleForQuery(final ProviderId providerId) {
		List<Map<String, Object>> results = query(providerId);

		return parseAttributeMapFromResults(results, providerId);
	}

	protected abstract List<Map<String, Object>> query(final ProviderId providerId);

	protected List<IPersonAttributes> parseAttributeMapFromResults(
			List<Map<String, Object>> queryResults, ProviderId providerId) {
		final List<IPersonAttributes> peopleAttributes = new ArrayList<IPersonAttributes>(
				queryResults.size());
		String id = providerId.getId();

		for (final Map<String, Object> queryResult : queryResults) {
			final Map<String, List<Object>> multivaluedQueryResult = MultivaluedPersonAttributeUtils
					.toMultivaluedMap(queryResult);
			final IPersonAttributes person;

			if (id != null) {
				person = new CaseInsensitiveNamedPersonImpl(id, multivaluedQueryResult);
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

	/**
	 * @param uid
	 * @return ProviderId
	 */
	protected ProviderId convertAttributesMap(String uid) {
		String[] temp = uid.split("#");

		if (temp.length >= 2) {
			return new ProviderId(temp[0], temp[1]);
		}

		return null;
	}

	protected class ProviderId {
		private final String providerName;
		private final String id;

		public ProviderId(final String providerName, final String id) {
			this.providerName = providerName;
			this.id = id;
		}

		public String getProviderName() {
			return providerName;
		}

		public String getId() {
			return id;
		}

	}

}