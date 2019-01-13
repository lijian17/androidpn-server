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
package net.dxs.service.impl;

import java.util.List;

import javax.management.Notification;

import net.dxs.service.NotificationService;

/**
 * NotificationService的实现类
 * 
 * @author lijian
 * @date 2016-12-5 下午11:05:49
 */
public class NotificationServiceImpl implements NotificationService {

	private NotificationDao notificationDao;
	
	public NotificationDao getNotificationDao() {
		return notificationDao;
	}

	public void setNotificationDao(NotificationDao notificationDao) {
		this.notificationDao = notificationDao;
	}

	public void saveNotification(Notification notification) {
		notificationDao.saveNotification(notification);
	}

	public List<Notification> findNotificationsByUsername(String username) {
		return notificationDao.findNotificationsByUsername(username);
	}

	public void deleteNotification(Notification notification) {
		notificationDao.deleteNotification(notification);
	}

	public void deleteNotificationByUUID(String uuid) {
		notificationDao.deleteNotificationByUUID(uuid);
	}


}
