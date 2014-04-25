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
package com.buession.cas.service;

import javax.validation.constraints.NotNull;

import net.spy.memcached.MemcachedClient;

import com.google.code.kaptcha.util.Config;

/**
 * @author Yong.Teng <webmaster@buession.com>
 */
public class MemcachedCaptchaService extends CaptchaService {

	/**
	 * Memcached Client
	 */
	@NotNull
	private MemcachedClient memcachedClient;

	/**
	 * 验证码缓存时长
	 */
	private int lifetime = 3;

	/**
	 * @param config
	 *        验证码配置
	 */
	public MemcachedCaptchaService(final Config config) {
		super(config);
	}

	/**
	 * 获取 Memcached Client
	 * 
	 * @return Memcached Client
	 */
	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	/**
	 * 设置 Memcached Client
	 * 
	 * @param memcachedClient
	 *        Memcached Client
	 */
	public void setMemcachedClient(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}

	/**
	 * 获取验证码缓存时长
	 * 
	 * @return Memcached Client
	 */
	public int getLifetime() {
		return lifetime;
	}

	/**
	 * 设置验证码缓存时长
	 * 
	 * @param lifetime
	 *        验证码缓存时长
	 */
	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	@Override
	protected void add(String key, String value) {
		memcachedClient.set(key, lifetime, value);
	}

	@Override
	protected boolean validate(String key, String value) {
		Object cacheValue = memcachedClient.get(key);
		System.out.println(value + ", " + cacheValue);
		return cacheValue != null && value != null && value.equalsIgnoreCase(cacheValue.toString());
	}

	@Override
	protected void delete(String key) {
		memcachedClient.delete(key);
	}

}