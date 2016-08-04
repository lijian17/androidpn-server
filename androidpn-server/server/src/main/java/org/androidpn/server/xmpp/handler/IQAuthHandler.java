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
package org.androidpn.server.xmpp.handler;

import gnu.inet.encoding.Stringprep;
import gnu.inet.encoding.StringprepException;

import org.androidpn.server.xmpp.UnauthenticatedException;
import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.auth.AuthManager;
import org.androidpn.server.xmpp.auth.AuthToken;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

/**
 * 本类处理TYPE_IQ类型为jabber:iq:auth的协议(认证)
 * 
 * @author lijian
 * @date 2016-8-3 下午11:07:46
 */
public class IQAuthHandler extends IQHandler {

	private static final String NAMESPACE = "jabber:iq:auth";

	/** 探头应答 */
	private Element probeResponse;

	/**
	 * 本类处理TYPE_IQ类型为jabber:iq:auth的协议.
	 */
	public IQAuthHandler() {
		probeResponse = DocumentHelper.createElement(QName.get("query",
				NAMESPACE));
		probeResponse.addElement("username");
		if (AuthManager.isPlainSupported()) {
			probeResponse.addElement("password");
		}
		if (AuthManager.isDigestSupported()) {
			probeResponse.addElement("digest");
		}
		probeResponse.addElement("resource");
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		IQ reply = null;

		ClientSession session = sessionManager.getSession(packet.getFrom());
		if (session == null) {
			log.error("未找到key的会话 " + packet.getFrom());
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.internal_server_error);
			return reply;
		}

		try {
			Element iq = packet.getElement();
			Element query = iq.element("query");
			Element queryResponse = probeResponse.createCopy();

			if (IQ.Type.get == packet.getType()) { // 得到查询
				String username = query.elementText("username");
				if (username != null) {
					queryResponse.element("username").setText(username);
				}
				reply = IQ.createResultIQ(packet);
				reply.setChildElement(queryResponse);
				if (session.getStatus() != Session.STATUS_AUTHENTICATED) {
					reply.setTo((JID) null);
				}
			} else { // 设置查询
				String resource = query.elementText("resource");
				String username = query.elementText("username");
				String password = query.elementText("password");
				String digest = null;
				if (query.element("digest") != null) {// 摘要
					digest = query.elementText("digest").toLowerCase();
				}

				// 验证这个resource
				if (resource != null) {
					try {
						resource = JID.resourceprep(resource);
					} catch (StringprepException e) {
						throw new UnauthorizedException("无效resource: "
								+ resource, e);
					}
				} else {
					throw new IllegalArgumentException(
							"无效resource (empty or null).");
				}

				// 验证这个username
				if (username == null || username.trim().length() == 0) {
					throw new UnauthorizedException(
							"无效 username (empty or null).");
				}
				try {
					Stringprep.nodeprep(username);
				} catch (StringprepException e) {
					throw new UnauthorizedException("无效 username: " + username,
							e);
				}
				username = username.toLowerCase();

				// 验证username and password是否正确
				AuthToken token = null;
				if (password != null && AuthManager.isPlainSupported()) {
					token = AuthManager.authenticate(username, password);
				} else if (digest != null && AuthManager.isDigestSupported()) {
					token = AuthManager.authenticate(username, session
							.getStreamID().toString(), digest);
				}

				if (token == null) {
					throw new UnauthenticatedException();
				}

				// 设置会话认证成功
				session.setAuthToken(token, resource);
				packet.setFrom(session.getAddress());
				reply = IQ.createResultIQ(packet);
			}
		} catch (Exception ex) {
			log.error(ex);
			reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			if (ex instanceof IllegalArgumentException) {
				reply.setError(PacketError.Condition.not_acceptable);
			} else if (ex instanceof UnauthorizedException) {
				reply.setError(PacketError.Condition.not_authorized);
			} else if (ex instanceof UnauthenticatedException) {
				reply.setError(PacketError.Condition.not_authorized);
			} else {
				reply.setError(PacketError.Condition.internal_server_error);
			}
		}

		// 立即发送应答到会话
		if (reply != null) {
			session.process(reply);
		}
		return null;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

}
