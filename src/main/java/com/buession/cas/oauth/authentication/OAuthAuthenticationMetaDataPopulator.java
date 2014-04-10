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
package com.buession.cas.oauth.authentication;

import java.util.Map;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.scribe.up.profile.UserProfile;

/**
 * @author Yong.Teng <webmaster@buession.com>
 */
public final class OAuthAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

	private final static String ID = "id";

	@Override
	public Authentication populateAttributes(Authentication authentication, Credentials credentials) {
		if (credentials instanceof OAuthCredentials) {
			OAuthCredentials oauthCredentials = (OAuthCredentials) credentials;
			Principal principal = authentication.getPrincipal();
			UserProfile userProfile = oauthCredentials.getUserProfile();
			Map<String, Object> attributes = userProfile.getAttributes();

			attributes.put(ID, userProfile.getId());
			attributes.putAll(principal.getAttributes());

			final Principal simplePrincipal = new SimplePrincipal(principal.getId(), attributes);
			final MutableAuthentication mutableAuthentication = new MutableAuthentication(
					simplePrincipal, authentication.getAuthenticatedDate());

			mutableAuthentication.getAttributes().putAll(authentication.getAttributes());
			mutableAuthentication.getAttributes().put(OAuthConstants.PROVIDER_TYPE,
					oauthCredentials.getCredential().getProviderType());

			return mutableAuthentication;
		}

		return authentication;
	}

}