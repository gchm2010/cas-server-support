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
package com.buession.cas.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;

import com.google.code.kaptcha.util.Config;

/**
 * @author Yong.Teng <webmaster@buession.com>
 */
public abstract class CaptchaService {

	public final static String VALIDATE_CODE = "validateCode";

	/**
	 * 验证码配置
	 */
	private final Config config;

	private String cacheName;

	/**
	 * @param config
	 *        验证码配置
	 */
	public CaptchaService(final Config config) {
		Assert.notNull(config, "Captcha config could not be null");

		this.config = config;
		cacheName = config.getSessionKey();
		if (cacheName == null) {
			cacheName = VALIDATE_CODE;
		}
	}

	/**
	 * 缓存验证码
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param value
	 *        需缓存的验证码值
	 */
	public final void add(final HttpServletRequest request, final String value) {
		add(getCacheName(request), value);
	}

	/**
	 * 验证码验证
	 * 
	 * @param request
	 *        HttpServletRequest
	 * @param validateCode
	 *        需要验证的验证码
	 * @return 验证码是否正确
	 */
	public final boolean validate(final HttpServletRequest request, final String validateCode) {
		if (validateCode == null || validateCode.length() == 0) {
			return false;
		}

		return validate(getCacheName(request), validateCode);
	}

	/**
	 * 删除验证码缓存
	 * 
	 * @param request
	 *        HttpServletRequest
	 */
	public void delete(final HttpServletRequest request) {
		delete(getCacheName(request));
	}

	/**
	 * 获取验证码缓存名称
	 * 
	 * @return 验证码缓存名称
	 */
	public final String getCacheName(final HttpServletRequest request) {
		Assert.notNull(request, "HttpServletRequest could not be null");

		return cacheName + "_" + request.getSession().getId();
	}

	/**
	 * 缓存验证码
	 * 
	 * @param key
	 *        验证码 Key
	 * @param value
	 *        需缓存的验证码值
	 */
	protected abstract void add(final String key, final String value);

	/**
	 * 验证码验证
	 * 
	 * @param key
	 *        验证码 Key
	 * @param value
	 *        需要验证的验证码
	 * @return 验证码是否正确
	 */
	protected abstract boolean validate(final String key, final String value);

	/**
	 * 删除验证码缓存
	 * 
	 * @param key
	 *        验证码 Key
	 */
	protected abstract void delete(final String key);

}