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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.buession.cas.authentication.principal.RememberMeUsernamePasswordCaptchaCredentials;
import com.buession.cas.web.utils.CaptchaValidate;
import com.google.code.kaptcha.util.Config;

/**
 * Action to authenticate credentials with and retrieve a TicketGrantingTicket for
 * those credentials. If there is a request for renew, then it also generates
 * the Service Ticket required.
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public class AuthenticationCaptchaViaFormAction extends AbstractAction {

	/**
	 * 凭证绑定器
	 */
	@SuppressWarnings("deprecation")
	private CredentialsBinder credentialsBinder;

	/**
	 * 认证服务
	 */
	@NotNull
	private CentralAuthenticationService centralAuthenticationService;

	/**
	 * Cookie 生成器
	 */
	@NotNull
	private CookieGenerator warnCookieGenerator;

	/**
	 * 验证码配置
	 */
	@NotNull
	private Config captchaConfig;

	/**
	 * 返回凭证绑定器
	 * 
	 * @return 凭证绑定器
	 */
	@SuppressWarnings("deprecation")
	public CredentialsBinder getCredentialsBinder() {
		return credentialsBinder;
	}

	/**
	 * 设置凭证绑定器
	 * 
	 * @param credentialsBinder
	 *        凭证绑定器
	 */
	@SuppressWarnings("deprecation")
	public void setCredentialsBinder(CredentialsBinder credentialsBinder) {
		this.credentialsBinder = credentialsBinder;
	}

	/**
	 * 返回认证服务
	 * 
	 * @return 认证服务
	 */
	public CentralAuthenticationService getCentralAuthenticationService() {
		return centralAuthenticationService;
	}

	/**
	 * 设置认证服务
	 * 
	 * @param centralAuthenticationService
	 *        认证服务
	 */
	public void setCentralAuthenticationService(
			CentralAuthenticationService centralAuthenticationService) {
		this.centralAuthenticationService = centralAuthenticationService;
	}

	/**
	 * 返回 Cookie 生成器
	 * 
	 * @return Cookie 生成器
	 */
	public CookieGenerator getWarnCookieGenerator() {
		return warnCookieGenerator;
	}

	/**
	 * 设置 Cookie 生成器
	 * 
	 * @param warnCookieGenerator
	 *        Cookie 生成器
	 */
	public void setWarnCookieGenerator(CookieGenerator warnCookieGenerator) {
		this.warnCookieGenerator = warnCookieGenerator;
	}

	/**
	 * 返回验证码配置
	 * 
	 * @return 验证码配置
	 */
	public Config getCaptchaConfig() {
		return captchaConfig;
	}

	/**
	 * 设置验证码配置
	 * 
	 * @param captchaConfig
	 */
	public void setCaptchaConfig(Config captchaConfig) {
		this.captchaConfig = captchaConfig;
	}

	/**
	 * 用户凭证绑定
	 * 
	 * @param context
	 *        请求上下文
	 * @param credentials
	 *        用户凭证
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public final void doBind(final RequestContext context, final Credentials credentials)
			throws Exception {
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

		if (credentialsBinder != null && credentialsBinder.supports(credentials.getClass())) {
			credentialsBinder.bind(request, credentials);
		}
	}

	/**
	 * @param context
	 *        请求上下文
	 * @param credentials
	 *        用户凭证
	 * @param messageContext
	 *        消息上下文
	 * @return Event
	 * @throws Exception
	 */
	public final String submit(final RequestContext context, final Credentials credentials,
			final MessageContext messageContext) throws Exception {
		HttpServletRequest request = WebUtils.getHttpServletRequest(context);

		// Validate login ticket
		final String authoritativeLoginTicket = WebUtils.getLoginTicketFromFlowScope(context);
		final String providedLoginTicket = WebUtils.getLoginTicketFromRequest(context);
		if (!authoritativeLoginTicket.equals(providedLoginTicket)) {
			logger.warn("Invalid login ticket " + providedLoginTicket);
			final String code = "INVALID_TICKET";
			messageContext.addMessage(new MessageBuilder().error().code(code)
					.arg(providedLoginTicket).defaultText(code).build());
			return error().toString();
		}

		if (captchaValidate(request, credentials, messageContext) == false) {
			return error().toString();
		}

		final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
		final Service service = WebUtils.getService(context);

		if (StringUtils.hasText(context.getRequestParameters().get("renew"))
				&& ticketGrantingTicketId != null && service != null) {
			try {
				final String serviceTicketId = centralAuthenticationService.grantServiceTicket(
						ticketGrantingTicketId, service, credentials);
				WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
				putWarnCookieIfRequestParameterPresent(context);

				return "warn";
			} catch (final TicketException e) {
				if (isCauseAuthenticationException(e)) {
					populateErrorsInstance(e, messageContext);
					return getAuthenticationExceptionEventId(e);
				}

				centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);

				logger.debug(
						"Attempted to generate a ServiceTicket using renew=true with different credentials",
						e);
			}
		}

		try {
			WebUtils.putTicketGrantingTicketInRequestScope(context,
					centralAuthenticationService.createTicketGrantingTicket(credentials));
			putWarnCookieIfRequestParameterPresent(context);

			return success().toString();
		} catch (final TicketException e) {
			populateErrorsInstance(e, messageContext);

			return isCauseAuthenticationException(e) ? getAuthenticationExceptionEventId(e)
					: error().toString();
		}
	}

	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		return null;
	}

	/**
	 * 验证码验证
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param credentials
	 *        用户凭证
	 * @param messageContext
	 *        消息上下文
	 * @return 验证码是否正确
	 */
	private boolean captchaValidate(final HttpServletRequest request,
			final Credentials credentials, final MessageContext messageContext) {
		final RememberMeUsernamePasswordCaptchaCredentials _credentials = (RememberMeUsernamePasswordCaptchaCredentials) credentials;
		String validateCode = _credentials.getValidateCode();
		boolean result = CaptchaValidate.validate(request, captchaConfig, validateCode);

		if (result == false) {
			logger.warn("Invalid captcha " + validateCode);
			final String code = "INVALID_CAPTCHA";
			messageContext.addMessage(new MessageBuilder().error().code(code).arg(validateCode)
					.defaultText(code).build());
		}

		HttpSession session = request.getSession();
		session.removeAttribute(captchaConfig.getSessionKey());

		return result;
	}

	/**
	 * @param e
	 *        TicketException
	 * @param messageContext
	 *        消息上下文
	 */
	private void populateErrorsInstance(final TicketException e, final MessageContext messageContext) {
		try {
			messageContext.addMessage(new MessageBuilder().error().code(e.getCode())
					.defaultText(e.getCode()).build());
		} catch (final Exception fe) {
			logger.error(fe.getMessage(), fe);
		}
	}

	/**
	 * @param context
	 *        请求上下文
	 */
	private void putWarnCookieIfRequestParameterPresent(final RequestContext context) {
		final HttpServletResponse response = WebUtils.getHttpServletResponse(context);

		if (StringUtils.hasText(context.getExternalContext().getRequestParameterMap().get("warn"))) {
			warnCookieGenerator.addCookie(response, "true");
		} else {
			warnCookieGenerator.removeCookie(response);
		}
	}

	/**
	 * @param e
	 *        TicketException
	 * @return
	 */
	private AuthenticationException getAuthenticationExceptionAsCause(final TicketException e) {
		return (AuthenticationException) e.getCause();
	}

	/**
	 * @param e
	 *        TicketException
	 * @return
	 */
	private String getAuthenticationExceptionEventId(final TicketException e) {
		final AuthenticationException authException = getAuthenticationExceptionAsCause(e);

		logger.debug("An authentication error has occurred. Returning the event id "
				+ authException.getType());

		return authException.getType();
	}

	/**
	 * @param e
	 *        TicketException
	 * @return
	 */
	private boolean isCauseAuthenticationException(final TicketException e) {
		return e.getCause() != null
				&& AuthenticationException.class.isAssignableFrom(e.getCause().getClass());
	}

}