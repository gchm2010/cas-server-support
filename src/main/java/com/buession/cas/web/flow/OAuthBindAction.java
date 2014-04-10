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

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.oauth.OAuthConfiguration;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.jasig.cas.web.support.WebUtils;
import org.scribe.up.provider.OAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * OAuth 用户绑定到站内用户
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public abstract class OAuthBindAction extends AbstractAction {

	/**
	 * OAuth 配置
	 */
	@NotNull
	protected OAuthConfiguration configuration;

	/**
	 * JdbcTemplate
	 */
	@NotNull
	protected JdbcTemplate jdbcTemplate;

	protected final static Logger logger = LoggerFactory.getLogger(OAuthAction.class);

	/**
	 * 返回 OAuth 配置
	 * 
	 * @return OAuth 配置
	 */
	public OAuthConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * 设置 OAuth 配置
	 * 
	 * @param configuration
	 *        OAuth 配置
	 */
	public void setConfiguration(OAuthConfiguration configuration) {
		this.configuration = configuration;
	}

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
	 * @param context
	 *        请求上下文
	 */
	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		MutableAttributeMap flowScope = context.getFlowScope();

		// get provider type
		final String providerType = request.getParameter(OAuthConstants.OAUTH_PROVIDER);
		logger.debug("providerType : {}", providerType);

		// it's an authentication
		if (StringUtils.isNotBlank(providerType)) {
			// get provider
			final OAuthProvider provider = OAuthUtils.getProviderByType(
					configuration.getProviders(), providerType);
			logger.debug("provider : {}", provider);

			OAuthCredentials credentials = (OAuthCredentials) flowScope.get("oauth_credentials");
			logger.debug("credentials : {}", credentials);

			if (bind(request, provider, credentials) == true) {
				return success();
			}
		}

		return error();
	}

	/**
	 * 绑定 OAuth 用户到站内应用
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param provider
	 *        OAuthProvider
	 * @param credentials
	 *        OAuthCredentials
	 * @return
	 */
	protected abstract boolean bind(final HttpServletRequest request, final OAuthProvider provider,
			final OAuthCredentials credentials);

}