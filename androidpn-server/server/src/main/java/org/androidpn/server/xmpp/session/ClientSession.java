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
package org.androidpn.server.xmpp.session;

import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.xmpp.auth.AuthToken;
import org.androidpn.server.xmpp.net.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

/**
 * 服务端与客户端的一个会话
 * 
 * @author lijian
 * @date 2016-7-30 下午4:15:19
 */
public class ClientSession extends Session {

	private static final Log log = LogFactory.getLog(ClientSession.class);

	private static final String ETHERX_NAMESPACE = "http://etherx.jabber.org/streams";

	private AuthToken authToken;

	private boolean initialized;

	private boolean wasAvailable = false;

	/** 出席对象 */
	private Presence presence = null;

	/**
	 * 根据服务名和流ID，创建一个JID
	 * 
	 * @param serverName
	 *            服务名
	 * @param connection
	 *            连接
	 * @param streamID
	 *            流ID
	 */
	public ClientSession(String serverName, Connection connection,
			String streamID) {
		super(serverName, connection, streamID);
		presence = new Presence();
		presence.setType(Presence.Type.unavailable);// 默认为不可用的
	}

	/**
	 * 在服务器和客户端之间创建一个新的会话，并返回它。
	 * 
	 * @param serverName
	 *            服务名
	 * @param connection
	 *            连接
	 * @param xpp
	 *            XML解析器来处理传入的数据
	 * @return 一个新创建的会话
	 * @throws XmlPullParserException
	 *             如果解析传入的数据时发生错误
	 */
	public static ClientSession createSession(String serverName,
			Connection connection, XmlPullParser xpp)
			throws XmlPullParserException {
		log.debug("createSession()...");

		if (!xpp.getName().equals("stream")) {
			throw new XmlPullParserException("坏的开始标签 (not stream)");
		}

		if (!xpp.getNamespace(xpp.getPrefix()).equals(ETHERX_NAMESPACE)) {
			throw new XmlPullParserException("不正确的名称空间的流");
		}

		String language = "en";
		for (int i = 0; i < xpp.getAttributeCount(); i++) {
			if ("lang".equals(xpp.getAttributeName(i))) {
				language = xpp.getAttributeValue(i);
			}
		}

		// 存储语言和版本信息
		connection.setLanaguage(language);
		connection.setXMPPVersion(MAJOR_VERSION, MINOR_VERSION);

		// 创建一个ClientSession
		ClientSession session = SessionManager.getInstance()
				.createClientSession(connection);

		// 建立开始数据包响应
		StringBuilder sb = new StringBuilder(200);
		sb.append("<?xml version='1.0' encoding='UTF-8'?>");
		sb.append("<stream:stream ");
		sb.append("xmlns:stream=\"http://etherx.jabber.org/streams\" xmlns=\"jabber:client\" from=\"");
		sb.append(serverName);
		sb.append("\" id=\"");
		sb.append(session.getStreamID().toString());
		sb.append("\" xml:lang=\"");
		sb.append(language);
		sb.append("\" version=\"");
		sb.append(MAJOR_VERSION).append(".").append(MINOR_VERSION);
		sb.append("\">");
		connection.deliverRawText(sb.toString());

		// XMPP 1.0 需要 stream features
		sb = new StringBuilder();
		sb.append("<stream:features>");
		if (connection.getTlsPolicy() != Connection.TLSPolicy.disabled) {
			sb.append("<starttls xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\">");
			if (connection.getTlsPolicy() == Connection.TLSPolicy.required) {
				sb.append("<required/>");
			}
			sb.append("</starttls>");
		}

		// 具体功能
		String specificFeatures = session.getAvailableStreamFeatures();
		if (specificFeatures != null) {
			sb.append(specificFeatures);
		}
		sb.append("</stream:features>");

		connection.deliverRawText(sb.toString());
		return session;
	}

	/**
	 * 返回与此会话相关联的用户名
	 * 
	 * @return
	 * @throws UserNotFoundException
	 *             如果用户还没有身份认证
	 */
	public String getUsername() throws UserNotFoundException {
		if (authToken == null) {
			throw new UserNotFoundException();
		}
		return getAddress().getNode();
	}

	/**
	 * 返回与此会话相关联的身份认证令牌
	 * 
	 * @return 认证令牌
	 */
	public AuthToken getAuthToken() {
		return authToken;
	}

	/**
	 * 用认证令牌初始化会话
	 * 
	 * @param authToken
	 *            认证令牌
	 */
	public void setAuthToken(AuthToken authToken) {
		this.authToken = authToken;
	}

	/**
	 * 用认证令牌和资源名称初始化会话
	 * 
	 * @param authToken
	 *            认证令牌
	 * @param resource
	 *            资源名
	 */
	public void setAuthToken(AuthToken authToken, String resource) {
		setAddress(new JID(authToken.getUsername(), getServerName(), resource));
		this.authToken = authToken;
		setStatus(Session.STATUS_AUTHENTICATED);
		// 添加会话到会话管理器
		sessionManager.addSession(this);
	}

	/**
	 * 指示会话是否已初始化
	 * 
	 * @return true：会话已初始化, false：还未初始化
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * 设置会话的初始化状态
	 * 
	 * @param initialized
	 *            true：会话已初始化
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	/**
	 * 指示是否有可用的会话。
	 * 
	 * @return true：有, false：无.
	 */
	public boolean wasAvailable() {
		return wasAvailable;
	}

	/**
	 * 返回此会话的presence
	 * 
	 * @return
	 */
	public Presence getPresence() {
		return presence;
	}

	/**
	 * 设置此会话的presence
	 * 
	 * @param presence
	 */
	public void setPresence(Presence presence) {
		Presence oldPresence = this.presence;
		this.presence = presence;
		if (oldPresence.isAvailable() && !this.presence.isAvailable()) {
			setInitialized(false);
		} else if (!oldPresence.isAvailable() && this.presence.isAvailable()) {
			wasAvailable = true;
		}
	}

	/**
	 * 返回一个available stream features的文本.
	 */
	public String getAvailableStreamFeatures() {
		StringBuilder sb = new StringBuilder();
		if (getAuthToken() == null) {
			// 支持 Non-SASL 认证模式
			sb.append("<auth xmlns=\"http://jabber.org/features/iq-auth\"/>");
			// 支持 In-Band 注册
			sb.append("<register xmlns=\"http://jabber.org/features/iq-register\"/>");
		} else {
			// 如果会话已被认证
			sb.append("<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"/>");
			sb.append("<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/>");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return super.toString() + " presence: " + presence;
	}

}
