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

import org.androidpn.server.xmpp.handler.PresenceUpdateHandler;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

/**
 * Presence路由
 * 
 * @author lijian
 * @date 2016-8-6 下午12:47:59
 */
public class PresenceRouter {

	private final Log log = LogFactory.getLog(getClass());

	/** 连接到服务器的会话管理器 */
	private SessionManager sessionManager;

	/** 处理出席协议 */
	private PresenceUpdateHandler presenceUpdateHandler;

	/**
	 * Presence路由
	 */
	public PresenceRouter() {
		sessionManager = SessionManager.getInstance();
		presenceUpdateHandler = new PresenceUpdateHandler();
	}

	/**
	 * 路由这个Presence包
	 * 
	 * @param packet
	 */
	public void route(Presence packet) {
		if (packet == null) {
			throw new NullPointerException();
		}
		ClientSession session = sessionManager.getSession(packet.getFrom());

		if (session == null || session.getStatus() != Session.STATUS_CONNECTED) {
			handle(packet);
		} else {
			packet.setTo(session.getAddress());
			packet.setFrom((JID) null);
			packet.setError(PacketError.Condition.not_authorized);
			session.process(packet);
		}
	}

	/**
	 * 处理Presence
	 * 
	 * @param packet
	 */
	private void handle(Presence packet) {
		try {
			Presence.Type type = packet.getType();
			// Presence updates (null == 'available')
			if (type == null || Presence.Type.unavailable == type) {
				presenceUpdateHandler.process(packet);
			} else {
				log.warn("未知的presence类型");
			}

		} catch (Exception e) {
			log.error("不能路由数据包", e);
			Session session = sessionManager.getSession(packet.getFrom());
			if (session != null) {
				session.close();
			}
		}
	}

}
