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
package com.buession.cas.authentication.principal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * UsernamePasswordCredentials respresents the username and password and captcha that a user
 * may provide in order to prove the authenticity of who they say they are.
 * 
 * @author Yong.Teng <webmaster@buession.com>
 */
public class UsernamePasswordCaptchaCredentials extends UsernamePasswordCredentials {

	private static final long serialVersionUID = -2929361901593111856L;

	/**
	 * 验证码
	 */
	@NotNull(message = "required.captcha")
	@Size(min = 1, message = "required.captcha")
	private String validateCode;

	/**
	 * 返回验证码
	 * 
	 * @return 验证码
	 */
	public String getValidateCode() {
		return validateCode;
	}

	/**
	 * 设置验证码
	 * 
	 * @param validateCode
	 *        验证码
	 */
	public void setValidateCode(String validateCode) {
		this.validateCode = validateCode;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (validateCode != null ? validateCode.hashCode() : 0);

		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		UsernamePasswordCaptchaCredentials that = (UsernamePasswordCaptchaCredentials) object;
		String username = getUsername();
		String password = getPassword();
		String that_username = that.getUsername();
		String that_password = that.getPassword();

		if (password != null ? !password.equals(that_password) : that_password != null) {
			return false;
		}

		if (username != null ? !username.equals(that_username) : that_username != null) {
			return false;
		}

		return that.getValidateCode().equals(validateCode);
	}

}