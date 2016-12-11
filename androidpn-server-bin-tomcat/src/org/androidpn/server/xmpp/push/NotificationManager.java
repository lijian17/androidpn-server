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

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.androidpn.server.model.Notification;
import org.androidpn.server.model.User;
import org.androidpn.server.service.NotificationService;
import org.androidpn.server.service.ServiceLocator;
import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.service.UserService;
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

	private NotificationService notificationService;

	private UserService userService;

	/**
	 * 推送通知管理器
	 */
	public NotificationManager() {
		sessionManager = SessionManager.getInstance();
		notificationService = ServiceLocator.getNotificationService();
		userService = ServiceLocator.getUserService();
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
	 * @param imageUrl
	 *            图片地址
	 */
	public void sendBroadcast(String apiKey, String title, String message,
			String uri, String imageUrl) {
		log.debug("sendBroadcast()...");
		List<User> allUser = userService.getUsers();
		for (User user : allUser) {
			Random random = new Random();
			String id = Integer.toHexString(random.nextInt());
			saveNotification(apiKey, user.getUsername(), title, message, uri,
					imageUrl, id);
			IQ notificationIQ = createNotificationIQ(id, apiKey, title,
					message, uri, imageUrl);
			ClientSession session = sessionManager.getSession(user
					.getUsername());
			if (session != null && session.getPresence().isAvailable()) {

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
	 * @param imageUrl
	 *            图片地址
	 * @param shouldSave
	 *            是否要保存该消息
	 */
	public void sendNotifcationToUser(String apiKey, String username,
			String title, String message, String uri, String imageUrl,
			boolean shouldSave) {
		log.debug("sendNotifcationToUser()...");
		Random random = new Random();
		String id = Integer.toHexString(random.nextInt());
		try {
			User user = userService.getUserByUsername(username);
			// 避免user非法存储到数据库（过滤垃圾数据）
			if (user != null && shouldSave) {
				saveNotification(apiKey, username, title, message, uri,
						imageUrl, id);
			}
		} catch (UserNotFoundException e) {
			log.debug("未找到该用户：" + username);
			e.printStackTrace();
		}
		IQ notificationIQ = createNotificationIQ(id, apiKey, title, message,
				uri, imageUrl);
		ClientSession session = sessionManager.getSession(username);
		if (session != null) {
			if (session.getPresence().isAvailable()) {
				notificationIQ.setTo(session.getAddress());
				session.deliver(notificationIQ);
			}
		}
	}

	/**
	 * 根据别名向特定用户发送新创建的通知消息
	 * 
	 * @param apiKey
	 *            密钥
	 * @param alias
	 *            别名
	 * @param title
	 *            消息标题
	 * @param message
	 *            消息详情
	 * @param uri
	 *            消息URI
	 * @param imageUil
	 *            图片地址
	 * @param shouldSave
	 *            是否要保存该消息
	 */
	public void sendNotificationByAlias(String apiKey, String alias,
			String title, String message, String uri, String imageUil,
			boolean shouldSave) {
		String username = sessionManager.getUsernameByAlias(alias);
		if (username != null) {
			sendNotifcationToUser(apiKey, username, title, message, uri,
					imageUil, shouldSave);
		}
	}

	/**
	 * 根据标签向特定用户集合发送新创建的通知消息
	 * 
	 * @param apiKey
	 *            密钥
	 * @param tag
	 *            标签
	 * @param title
	 *            消息标题
	 * @param message
	 *            消息详情
	 * @param uri
	 *            消息URI
	 * @param imageUrl
	 *            图片地址
	 * @param shouldSave
	 *            是否要保存该消息
	 */
	public void sendNotificationByTag(String apiKey, String tag, String title,
			String message, String uri, String imageUrl, boolean shouldSave) {
		Set<String> usernameSet = sessionManager.getUsernameByTag(tag);
		if (usernameSet != null && !usernameSet.isEmpty()) {
			for (String username : usernameSet) {
				sendNotifcationToUser(apiKey, username, title, message, uri,
						imageUrl, shouldSave);
			}
		}
	}

	/**
	 * 保存消息至数据库
	 * 
	 * @param apiKey
	 * @param username
	 * @param title
	 * @param message
	 * @param uri
	 * @param imageUrl
	 * @param uuid
	 */
	private void saveNotification(String apiKey, String username, String title,
			String message, String uri, String imageUrl, String uuid) {
		Notification notification = new Notification();
		notification.setApiKey(apiKey);
		notification.setUsername(username);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setUri(uri);
		notification.setImageUrl(imageUrl);
		notification.setUuid(uuid);
		notificationService.saveNotification(notification);
	}

	/**
	 * 创建一个新的通知IQ，并返回它。
	 * 
	 * @param imageUrl
	 */
	private IQ createNotificationIQ(String id, String apiKey, String title,
			String message, String uri, String imageUrl) {
		// Random random = new Random();
		// String id = Integer.toHexString(random.nextInt());
		// String id = String.valueOf(System.currentTimeMillis());

		Element notification = DocumentHelper.createElement(QName.get(
				"notification", NOTIFICATION_NAMESPACE));
		notification.addElement("id").setText(id);
		notification.addElement("apiKey").setText(apiKey);
		notification.addElement("title").setText(title);
		notification.addElement("message").setText(message);
		notification.addElement("uri").setText(uri);
		notification.addElement("imageUrl").setText(imageUrl);

		IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setChildElement(notification);

		return iq;
	}
}
