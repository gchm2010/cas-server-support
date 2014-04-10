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

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;

/**
 * 验证码控制器
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
@Controller("/captcha")
public class CaptchaController extends AbstractController {

	/**
	 * 验证码配置
	 */
	@NotNull
	@Resource
	private Config config;

	/**
	 * 验证码生成提供者
	 */
	@NotNull
	@Resource
	private Producer producer;

	public CaptchaController() {
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
	public Producer getProducer() {
		return producer;
	}

	/**
	 * 设置验证码生成提供者
	 * 
	 * @param captchaProducer
	 */
	public void setProducer(Producer producer) {
		this.producer = producer;
	}

	/**
	 * @param request
	 *        HttpServletRequest
	 * @param response
	 *        HttpServletResponse
	 * @return
	 */
	@RequestMapping(value = "/captcha.jpg")
	public String index(HttpServletRequest request, HttpServletResponse response) {
		try {
			handleRequestInternal(request, response);
		} catch (IOException e) {
		}

		return null;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType("image/jpg");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		response.setDateHeader("Expires", 0);

		String text = producer.createText();

		HttpSession session = request.getSession();
		session.setAttribute(config.getSessionKey(), text);

		BufferedImage im = producer.createImage(text);
		ServletOutputStream out = response.getOutputStream();
		ImageIO.write(im, "jpg", out);

		try {
			out.flush();
		} finally {
			out.close();
		}

		return null;
	}
}