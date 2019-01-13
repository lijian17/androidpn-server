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
package net.dxs.xmpp.handler;

import java.util.List;

import javax.management.Notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import net.dxs.service.NotificationService;
import net.dxs.service.ServiceLocator;
import net.dxs.xmpp.push.NotificationManager;
import net.dxs.xmpp.router.PacketDeliverer;
import net.dxs.xmpp.session.ClientSession;
import net.dxs.xmpp.session.Session;
import net.dxs.xmpp.session.SessionManager;

/**
 * 这个类处理出席协议
 * 
 * @author lijian
 * @date 2016-12-4 上午12:11:28
 */
public class PresenceUpdateHandler {

	protected final Log log = LogFactory.getLog(getClass());

	/** 连接到服务器的会话管理器 */
	protected SessionManager sessionManager;

	private NotificationService notificationService;

	private NotificationManager notificationManager;

	/**
	 * 这个类处理出席协议.
	 */
	public PresenceUpdateHandler() {
		sessionManager = SessionManager.getInstance();
		notificationService = ServiceLocator.getNotificationService();
		notificationManager = new NotificationManager();
	}

	/**
	 * 处理出席数据包
	 * 
	 * @param packet
	 */
	public void process(Packet packet) {
		ClientSession session = sessionManager.getSession(packet.getFrom());

		try {
			Presence presence = (Presence) packet;
			Presence.Type type = presence.getType();

			if (type == null) { // null == available
				if (session != null
						&& session.getStatus() == Session.STATUS_CLOSED) {
					log.warn("Rejected available presence: " + presence + " - "
							+ session);
					return;
				}

				if (session != null) {
					session.setPresence(presence);
					if (!session.isInitialized()) {
						// initSession(session);
						session.setInitialized(true);
					}
					List<Notification> list = notificationService
							.findNotificationsByUsername(session.getUsername());
					if (list != null && list.size() > 0) {
						for (Notification notification : list) {
							String apiKey = notification.getApiKey();
							String title = notification.getTitle();
							String message = notification.getMessage();
							String uri = notification.getUri();
							String imageUrl = notification.getImageUrl();
							notificationManager.sendNotifcationToUser(apiKey,
									session.getUsername(), title, message, uri,
									imageUrl, false);
							notificationService
									.deleteNotification(notification);
						}
					}
				}

			} else if (Presence.Type.unavailable == type) {// 不可用的

				if (session != null) {
					session.setPresence(presence);
				}

			} else {
				presence = presence.createCopy();
				if (session != null) {
					presence.setFrom(new JID(null, session.getServerName(),
							null, true));
					presence.setTo(session.getAddress());
				} else {
					JID sender = presence.getFrom();
					presence.setFrom(presence.getTo());
					presence.setTo(sender);
				}
				presence.setError(PacketError.Condition.bad_request);
				PacketDeliverer.deliver(presence);
			}

		} catch (Exception e) {
			log.error("内部服务器错误. Triggered by packet: " + packet, e);
		}
	}

}
