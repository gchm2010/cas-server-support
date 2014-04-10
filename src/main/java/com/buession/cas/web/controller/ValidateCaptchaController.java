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
package com.buession.cas.web.controller;

import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.buession.cas.web.utils.CaptchaValidate;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;

/**
 * 验证码异步验证控制器
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
@Controller("/captcha")
public class ValidateCaptchaController extends AbstractController {

	/**
	 * 验证配置
	 */
	@NotNull
	@Resource
	private Config config;

	/**
	 * 验证码生成提供者
	 */
	@NotNull
	@Resource
	private Producer captchaProducer;

	/**
	 * 传输验证码的请求参数
	 */
	@NotNull
	private String requestParamName;

	public ValidateCaptchaController() {
		setCacheSeconds(0);
	}

	/**
	 * 返回验证码配置
	 * 
	 * @return 验证码配置
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * 设置验证码配置
	 * 
	 * @param config
	 *        验证码配置
	 */
	public void setConfig(Config config) {
		this.config = config;
	}

	/**
	 * 返回验证码生成提供者
	 * 
	 * @return 验证码生成提供者
	 */
	public Producer getCaptchaProducer() {
		return captchaProducer;
	}

	/**
	 * 设置验证码生成提供者
	 * 
	 * @param captchaProducer
	 */
	public void setCaptchaProducer(Producer captchaProducer) {
		this.captchaProducer = captchaProducer;
	}

	/**
	 * 返回传输验证码的请求参数
	 * 
	 * @return 传输验证码的请求参数
	 */
	public String getRequestParamName() {
		return requestParamName;
	}

	/**
	 * 设置传输验证码的请求参数
	 * 
	 * @param requestParamName
	 *        传输验证码的请求参数
	 */
	public void setRequestParamName(final String requestParamName) {
		Assert.hasText(requestParamName,
				"RequestParamName must be have length; it could not be null or empty");
		this.requestParamName = requestParamName;
	}

	/**
	 * @param request
	 *        HttpServletRequest
	 * @param response
	 *        HttpServletResponse
	 * @return
	 */
	@RequestMapping(value = "/validateCode")
	public String index(HttpServletRequest request, HttpServletResponse response) {
		try {
			handleRequestInternal(request, response);
		} catch (Exception e) {
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet
	 * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PrintWriter writer = response.getWriter();

		writer.write(CaptchaValidate.validate(request, requestParamName, config) == true ? "true"
				: "false");
		writer.close();

		return null;
	}

}