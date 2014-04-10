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
package com.buession.cas.web.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.code.kaptcha.util.Config;

/**
 * 验证码验证工具
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public final class CaptchaValidate {

	public final static String REQUEST_CAPTCHA_PARAM_NAME = "validateCode";

	private CaptchaValidate() {
	}

	/**
	 * 验证码验证
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param requestParamName
	 *        传输验证码的请求参数名称
	 * @param config
	 *        验证码配置
	 * @return 验证码是否正确
	 */
	public static boolean validate(final HttpServletRequest request, final String requestParamName,
			final Config config) {
		String value = request.getParameter(requestParamName == null ? REQUEST_CAPTCHA_PARAM_NAME
				: requestParamName);
		return validate(request, config, value);
	}

	/**
	 * 验证码验证
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param captchaProducer
	 *        验证码生成提供者
	 * @param validateCode
	 *        需要验证的验证码
	 * @return 验证码是否正确
	 */
	public static boolean validate(final HttpServletRequest request, final Config config,
			final String validateCode) {
		if (validateCode == null || validateCode.length() == 0) {
			return false;
		}

		HttpSession session = request.getSession();
		String validateCodeSessionValue = (String) session.getAttribute(config.getSessionKey());

		return validateCode != null && validateCodeSessionValue != null
				&& validateCodeSessionValue.equalsIgnoreCase(validateCode);
	}

}