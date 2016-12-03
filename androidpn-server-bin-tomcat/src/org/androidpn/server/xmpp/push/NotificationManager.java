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
package org.androidpn.server.xmpp.push;

import java.util.Random;

import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;

/**
 * 推送通知管理器
 * 
 * @author lijian
 * @date 2016-12-4 上午12:32:24
 */
public class NotificationManager {
	private final Log log = LogFactory.getLog(getClass());

	/** 通知的名称空间 */
	private static final String NOTIFICATION_NAMESPACE = "androidpn:iq:notification";

	private SessionManager sessionManager;

	/**
	 * 推送通知管理器
	 */
	public NotificationManager() {
		sessionManager = SessionManager.getInstance();
	}

	/**
	 * 向所有已连接的用户广播一个新创建的通知消息
	 * 
	 * @param apiKey
	 *            密钥
	 * @param title
	 *            消息标题
	 * @param message
	 *            消息详情
	 * @param uri
	 *            消息URI
	 */
	public void sendBroadcast(String apiKey, String title, String message,
			String uri) {
		log.debug("sendBroadcast()...");
		IQ notificationIQ = createNotificationIQ(apiKey, title, message, uri);
		for (ClientSession session : sessionManager.getSessions()) {
			if (session.getPresence().isAvailable()) {
				notificationIQ.setTo(session.getAddress());
				session.deliver(notificationIQ);
			}
		}
	}

	/**
	 * 向特定用户发送新创建的通知消息
	 * 
	 * @param apiKey
	 *            密钥
	 * @param username
	 *            用户名
	 * @param title
	 *            消息标题
	 * @param message
	 *            消息详情
	 * @param uri
	 *            消息URI
	 */
	public void sendNotifcationToUser(String apiKey, String username,
			String title, String message, String uri) {
		log.debug("sendNotifcationToUser()...");
		IQ notificationIQ = createNotificationIQ(apiKey, title, message, uri);
		ClientSession session = sessionManager.getSession(username);
		if (session != null) {
			if (session.getPresence().isAvailable()) {
				notificationIQ.setTo(session.getAddress());
				session.deliver(notificationIQ);
			}
		}
	}

	/**
	 * 创建一个新的通知IQ，并返回它。
	 */
	private IQ createNotificationIQ(String apiKey, String title,
			String message, String uri) {
		Random random = new Random();
		String id = Integer.toHexString(random.nextInt());
		// String id = String.valueOf(System.currentTimeMillis());

		Element notification = DocumentHelper.createElement(QName.get(
				"notification", NOTIFICATION_NAMESPACE));
		notification.addElement("id").setText(id);
		notification.addElement("apiKey").setText(apiKey);
		notification.addElement("title").setText(title);
		notification.addElement("message").setText(message);
		notification.addElement("uri").setText(uri);

		IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setChildElement(notification);

		return iq;
	}
}
