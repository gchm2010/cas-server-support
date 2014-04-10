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
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthConfiguration;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.provider.BaseOAuth10Provider;
import org.scribe.up.provider.BaseOAuthProvider;
import org.scribe.up.provider.OAuthProvider;
import org.scribe.up.session.HttpUserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.buession.cas.web.utils.Constants;

/**
 * This class represents an action in the webflow to retrieve OAuth information on the callback url
 * which is the webflow url (/login). The
 * {@link org.jasig.cas.support.oauth.OAuthConstants.OAUTH_PROVIDER} and the other OAuth parameters
 * are expected after OAuth authentication.
 * Providers are defined by configuration. The
 * {@link org.jasig.cas.support.oauth.OAuthConstants.SERVICE},
 * {@link org.jasig.cas.support.oauth.OAuthConstants.THEME},
 * {@link org.jasig.cas.support.oauth.OAuthConstants.LOCALE} and
 * {@link org.jasig.cas.support.oauth.OAuthConstants.METHOD} parameters are saved and restored from
 * web session after OAuth authentication.
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public class OAuthAction extends AbstractAction {

	/**
	 * OAuth 配置
	 */
	@NotNull
	private OAuthConfiguration configuration;

	/**
	 * CAS viewed as a set of services to generate and validate Tickets.
	 */
	@NotNull
	private CentralAuthenticationService centralAuthenticationService;

	private final String oauth10loginUrl = "/" + OAuthConstants.OAUTH10_LOGIN_URL;

	private final static Logger logger = LoggerFactory.getLogger(OAuthAction.class);

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

		for (final OAuthProvider provider : configuration.getProviders()) {
			final BaseOAuthProvider baseProvider = (BaseOAuthProvider) provider;
			baseProvider.setCallbackUrl(OAuthUtils.addParameter(configuration.getLoginUrl(),
					OAuthConstants.OAUTH_PROVIDER, provider.getType()));
		}
	}

	/**
	 * 返回 CAS viewed as a set of services to generate and validate Tickets.
	 * 
	 * @return CAS viewed as a set of services to generate and validate Tickets
	 */
	public CentralAuthenticationService getCentralAuthenticationService() {
		return centralAuthenticationService;
	}

	/**
	 * 设置 CAS viewed as a set of services to generate and validate Tickets
	 * 
	 * @param centralAuthenticationService
	 *        CAS viewed as a set of services to generate and validate Tickets
	 */
	public void setCentralAuthenticationService(
			CentralAuthenticationService centralAuthenticationService) {
		this.centralAuthenticationService = centralAuthenticationService;
	}

	/**
	 * @param context
	 *        请求上下文
	 */
	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
		final HttpSession session = request.getSession();

		// get provider type
		final String providerType = request.getParameter(OAuthConstants.OAUTH_PROVIDER);
		logger.debug("providerType : {}", providerType);

		// it's an authentication
		if (StringUtils.isNotBlank(providerType)) {
			MutableAttributeMap flowScope = context.getFlowScope();

			// get provider
			final OAuthProvider provider = OAuthUtils.getProviderByType(
					this.configuration.getProviders(), providerType);
			logger.debug("provider : {}", provider);

			// get credential
			final OAuthCredential credential = provider.getCredential(new HttpUserSession(request),
					request.getParameterMap());
			logger.debug("credential : {}", credential);

			// retrieve parameters from web session
			final Service service = (Service) session.getAttribute(OAuthConstants.SERVICE);
			context.getFlowScope().put(OAuthConstants.SERVICE, service);
			restoreRequestAttribute(request, session, OAuthConstants.THEME);
			restoreRequestAttribute(request, session, OAuthConstants.LOCALE);
			restoreRequestAttribute(request, session, OAuthConstants.METHOD);

			// create credentials
			final Credentials credentials = new OAuthCredentials(credential);

			try {
				WebUtils.putTicketGrantingTicketInRequestScope(context,
						centralAuthenticationService.createTicketGrantingTicket(credentials));
				flowScope.put(Constants.OAUTH_CREDENTIALS, credentials);

				return success();
			} catch (final TicketException e) {
			}
		} else {
			// no authentication : go to login page

			// save parameters in web session
			final Service service = (Service) context.getFlowScope().get(OAuthConstants.SERVICE);
			if (service != null) {
				session.setAttribute(OAuthConstants.SERVICE, service);
			}
			saveRequestParameter(request, session, OAuthConstants.THEME);
			saveRequestParameter(request, session, OAuthConstants.LOCALE);
			saveRequestParameter(request, session, OAuthConstants.METHOD);

			// for all providers, generate authorization urls
			for (final OAuthProvider provider : configuration.getProviders()) {
				final String key = provider.getType() + "Url";
				String authorizationUrl = null;
				// for OAuth 1.0 protocol, delay request_token request by pointing to an
				// intermediate url
				if (provider instanceof BaseOAuth10Provider) {
					authorizationUrl = OAuthUtils.addParameter(request.getContextPath()
							+ oauth10loginUrl, OAuthConstants.OAUTH_PROVIDER, provider.getType());
				} else {
					authorizationUrl = provider.getAuthorizationUrl(new HttpUserSession(session));
				}

				logger.debug("{} -> {}", key, authorizationUrl);
				context.getFlowScope().put(key, authorizationUrl);
			}
		}

		return error();
	}

	/**
	 * Restore an attribute in web session as an attribute in request.
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param session
	 *        HttpSession
	 * @param name
	 *        Session 名称
	 */
	private void restoreRequestAttribute(final HttpServletRequest request,
			final HttpSession session, final String name) {
		final String value = (String) session.getAttribute(name);
		request.setAttribute(name, value);
	}

	/**
	 * Save a request parameter in the web session.
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param session
	 *        HttpSession
	 * @param name
	 *        参数名称
	 */
	private void saveRequestParameter(final HttpServletRequest request, final HttpSession session,
			final String name) {
		final String value = request.getParameter(name);

		if (value != null) {
			session.setAttribute(name, value);
		}
	}

}