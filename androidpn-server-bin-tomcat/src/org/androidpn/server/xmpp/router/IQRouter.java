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
package org.androidpn.server.xmpp.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.androidpn.server.xmpp.handler.IQAuthHandler;
import org.androidpn.server.xmpp.handler.IQDeliverConfirmHandler;
import org.androidpn.server.xmpp.handler.IQHandler;
import org.androidpn.server.xmpp.handler.IQRegisterHandler;
import org.androidpn.server.xmpp.handler.IQRosterHandler;
import org.androidpn.server.xmpp.handler.IQSetAliasHandler;
import org.androidpn.server.xmpp.handler.IQSetTagsHandler;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

/**
 * IQ路由
 * 
 * @author lijian
 * @date 2016-12-4 上午12:34:12
 */
public class IQRouter {

    private final Log log = LogFactory.getLog(getClass());

	/** 连接到服务器的会话管理器 */
	private SessionManager sessionManager;

	private List<IQHandler> iqHandlers = new ArrayList<IQHandler>();

	private Map<String, IQHandler> namespace2Handlers = new ConcurrentHashMap<String, IQHandler>();

	/**
	 * IQ路由
	 */
	public IQRouter() {
		sessionManager = SessionManager.getInstance();
		iqHandlers.add(new IQAuthHandler());
		iqHandlers.add(new IQRegisterHandler());
		iqHandlers.add(new IQRosterHandler());
		iqHandlers.add(new IQDeliverConfirmHandler());
		iqHandlers.add(new IQSetAliasHandler());
		iqHandlers.add(new IQSetTagsHandler());
	}

	/**
	 * 路由基于命名空间的IQ数据包。
	 * 
	 * @param packet
	 */
	public void route(IQ packet) {
		if (packet == null) {
			throw new NullPointerException();
		}
		JID sender = packet.getFrom();
		ClientSession session = sessionManager.getSession(sender);

		if (session == null
				|| session.getStatus() == Session.STATUS_AUTHENTICATED
				|| ("jabber:iq:auth".equals(packet.getChildElement()
						.getNamespaceURI())
						|| "jabber:iq:register".equals(packet.getChildElement()
								.getNamespaceURI()) || "urn:ietf:params:xml:ns:xmpp-bind"
							.equals(packet.getChildElement().getNamespaceURI()))) {
			handle(packet);
		} else {
			IQ reply = IQ.createResultIQ(packet);
			reply.setChildElement(packet.getChildElement().createCopy());
			reply.setError(PacketError.Condition.not_authorized);
			session.process(reply);
		}
	}

	/**
	 * 处理IQ
	 * 
	 * @param packet
	 */
	private void handle(IQ packet) {
		try {
			Element childElement = packet.getChildElement();
			String namespace = null;
			if (childElement != null) {
				namespace = childElement.getNamespaceURI();
			}
			if (namespace == null) {
				if (packet.getType() != IQ.Type.result
						&& packet.getType() != IQ.Type.error) {
					log.warn("未知的包  " + packet);
				}
			} else {
				IQHandler handler = getHandler(namespace);
				if (handler == null) {
					sendErrorPacket(packet,
							PacketError.Condition.service_unavailable);
				} else {
					handler.process(packet);
				}
			}

		} catch (Exception e) {
			log.error("不能路由数据包", e);
			Session session = sessionManager.getSession(packet.getFrom());
			if (session != null) {
				IQ reply = IQ.createResultIQ(packet);
				reply.setError(PacketError.Condition.internal_server_error);
				session.process(reply);
			}
		}
	}

	/**
	 * 将错误数据包发送给原始发件人
	 * 
	 * @param originalPacket
	 * @param condition
	 */
	private void sendErrorPacket(IQ originalPacket,
			PacketError.Condition condition) {
		if (IQ.Type.error == originalPacket.getType()) {
			log.error("不能回答一个IQ错误到另一个IQ错误: " + originalPacket);
			return;
		}
		IQ reply = IQ.createResultIQ(originalPacket);
		reply.setChildElement(originalPacket.getChildElement().createCopy());
		reply.setError(condition);
		try {
			PacketDeliverer.deliver(reply);
		} catch (Exception e) {
			// Ignore
		}
	}

	/**
	 * 增加一个新的IQHandler来注册处理程序列表。
	 * 
	 * @param handler
	 */
	public void addHandler(IQHandler handler) {
		if (iqHandlers.contains(handler)) {
			throw new IllegalArgumentException("IQHandler已由server提供");
		}
		namespace2Handlers.put(handler.getNamespace(), handler);
	}

	/**
	 * 从注册处理程序列表移除一个IQHandler
	 * 
	 * @param handler
	 */
	public void removeHandler(IQHandler handler) {
		if (iqHandlers.contains(handler)) {
			throw new IllegalArgumentException("无法一个移除由Server提供的IQHandler");
		}
		namespace2Handlers.remove(handler.getNamespace());
	}

	/**
	 * 根据提供的namespace返回一个IQHandler
	 */
	private IQHandler getHandler(String namespace) {
		IQHandler handler = namespace2Handlers.get(namespace);
		if (handler == null) {
			for (IQHandler handlerCandidate : iqHandlers) {
				if (namespace.equalsIgnoreCase(handlerCandidate.getNamespace())) {
					handler = handlerCandidate;
					namespace2Handlers.put(namespace, handler);
					break;
				}
			}
		}
		return handler;
	}

}
