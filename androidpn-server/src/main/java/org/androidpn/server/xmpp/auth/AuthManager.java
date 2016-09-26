/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.androidpn.server.xmpp.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.androidpn.server.service.ServiceLocator;
import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.xmpp.UnauthenticatedException;
import org.androidpn.server.xmpp.XmppServer;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 管理器-用户认证
 * 
 * @author lijian
 * @date 2016-7-31 下午10:00:09
 */
public class AuthManager {

	private static final Log log = LogFactory.getLog(AuthManager.class);

	/** 摘要锁 */
	private static final Object DIGEST_LOCK = new Object();

	// 这一消息摘要类提供了应用程序的消息摘要算法的功能，如MD5和SHA。
	// 消息摘要是安全的单向哈希函数接受任意大小的数据，输出一个固定长度的哈希值。
	/** 消息摘要 */
	private static MessageDigest digest;

	static {
		try {
			digest = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			log.error("内部服务器错误-没有这样的摘要算法", e);
		}
	}

	/**
	 * 获得用户的密码
	 * 
	 * @param username
	 *            用户名
	 * @return
	 * @throws UserNotFoundException
	 */
	public static String getPassword(String username)
			throws UserNotFoundException {
		return ServiceLocator.getUserService().getUserByUsername(username)
				.getPassword();
	}

	/**
	 * 根据用户名、密码验证用户名真实性，并返回一个令牌
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            用户密码
	 * @return 一个令牌
	 * @throws UnauthenticatedException
	 *             如果用户名和密码不匹配
	 */
	public static AuthToken authenticate(String username, String password)
			throws UnauthenticatedException {
		if (username == null || password == null) {
			throw new UnauthenticatedException();
		}
		// 用户名是否是@domain形式
		username = username.trim().toLowerCase();
		if (username.contains("@")) {
			int index = username.indexOf("@");
			String domain = username.substring(index + 1);
			// 验证domain
			if (domain.equals(XmppServer.getInstance().getServerName())) {
				// 获得真正的username
				username = username.substring(0, index);
			} else {
				throw new UnauthenticatedException();
			}
		}
		try {
			if (!password.equals(getPassword(username))) {
				throw new UnauthenticatedException();
			}
		} catch (UserNotFoundException unfe) {
			throw new UnauthenticatedException();
		}
		return new AuthToken(username);
	}

	/**
	 * 根据用户名、token、摘要认证用户真实性，并返回一个令牌
	 * 
	 * @param username
	 *            用户名
	 * @param token
	 *            the token
	 * @param digest
	 *            摘要
	 * @return 一个令牌
	 * @throws UnauthenticatedException
	 *             如果用户名和密码不匹配
	 */
	public static AuthToken authenticate(String username, String token,
			String digest) throws UnauthenticatedException {
		if (username == null || token == null || digest == null) {
			throw new UnauthenticatedException();
		}
		username = username.trim().toLowerCase();
		if (username.contains("@")) {
			int index = username.indexOf("@");
			String domain = username.substring(index + 1);
			if (domain.equals(XmppServer.getInstance().getServerName())) {
				username = username.substring(0, index);
			} else {
				throw new UnauthenticatedException();
			}
		}
		try {
			String password = getPassword(username);
			String anticipatedDigest = createDigest(token, password);
			if (!digest.equalsIgnoreCase(anticipatedDigest)) {
				throw new UnauthenticatedException();
			}
		} catch (UserNotFoundException unfe) {
			throw new UnauthenticatedException();
		}
		return new AuthToken(username);
	}

	/**
	 * 如果支持使用JEP-0078的纯文本密码的身份认证，则返回true
	 * 
	 * @return true：支持纯文本密码的身份认证
	 */
	public static boolean isPlainSupported() {
		return true;
	}

	/**
	 * 如果支持使用JEP-0078的摘要认证，则返回true
	 * 
	 * @return true：支持摘要认证
	 */
	public static boolean isDigestSupported() {
		return true;
	}

	/**
	 * 根据token和密码，创建一个摘要
	 * 
	 * @param token
	 * @param password
	 * @return
	 */
	private static String createDigest(String token, String password) {
		synchronized (DIGEST_LOCK) {
			// 使用指定的字节数组来更新摘要。
			digest.update(token.getBytes());
			return Hex.encodeHexString(digest.digest(password.getBytes()));
		}
	}

}
