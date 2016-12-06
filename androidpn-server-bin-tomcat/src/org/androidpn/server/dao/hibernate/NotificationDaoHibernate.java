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
package org.androidpn.server.dao.hibernate;

import java.util.List;

import org.androidpn.server.dao.NotificationDao;
import org.androidpn.server.model.Notification;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 使用Spring的HibernateTemplate实现NotificationDao。
 * 
 * @author lijian
 * @date 2016-12-5 下午10:49:03
 */
public class NotificationDaoHibernate extends HibernateDaoSupport implements
		NotificationDao {

	public void saveNotification(Notification notification) {
		getHibernateTemplate().saveOrUpdate(notification);
		getHibernateTemplate().flush();
	}

	@SuppressWarnings("unchecked")
	public List<Notification> findNotificationsByUsername(String username) {
		List<Notification> list = getHibernateTemplate().find(
				"from Notification where username=?", username);
		if (list != null && list.size() > 0) {
			return list;
		}
		return null;
	}

	public void deleteNotification(Notification notification) {
		getHibernateTemplate().delete(notification);
	}

	@SuppressWarnings("unchecked")
	public void deleteNotificationByUUID(String uuid) {
		List<Notification> list = getHibernateTemplate().find(
				"from Notification where uuid=?", uuid);
		if (list != null && list.size() > 0) {
			Notification notification = list.get(0);
			deleteNotification(notification);
		}		
	}

}
