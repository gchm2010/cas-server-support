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

import com.buession.cas.service.CaptchaService;

/**
 * 验证码异步验证控制器
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
@Controller("/captcha")
public class ValidateCaptchaController extends AbstractController {

	/**
	 * 验证码 Service
	 */
	@NotNull
	@Resource
	private CaptchaService captchaService;

	/**
	 * 传输验证码的请求参数
	 */
	@NotNull
	private String requestParamName;

	public ValidateCaptchaController() {
		setCacheSeconds(0);
	}

	/**
	 * 返回验证码 Service
	 * 
	 * @return 验证码 Service
	 */
	public CaptchaService getCaptchaService() {
		return captchaService;
	}

	/**
	 * 设置验证码 Service
	 * 
	 * @param captchaService
	 *        验证码 Service
	 */
	public void setCaptchaService(CaptchaService captchaService) {
		this.captchaService = captchaService;
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

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PrintWriter writer = response.getWriter();

		writer.write(captchaService.validate(request, request.getParameter(requestParamName)) == true ? "true"
				: "false");
		writer.close();

		return null;
	}

}