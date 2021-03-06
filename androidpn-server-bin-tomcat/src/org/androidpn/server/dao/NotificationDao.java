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
package org.androidpn.server.dao;

import java.util.List;

import org.androidpn.server.model.Notification;

/**
 * Notification DAO (Data Access Object) interface.
 * 
 * @author lijian
 * @date 2016-12-5 下午10:43:20
 */
public interface NotificationDao {
	
	/** 保存消息 */
	public void saveNotification(Notification notification);

	/** 根据用户名获取消息 */
	public List<Notification> findNotificationsByUsername(String username);
	
	/** 删除消息 */
	public void deleteNotification(Notification notification);

	/** 根据UUID删除消息 */
	public void deleteNotificationByUUID(String uuid);
}
