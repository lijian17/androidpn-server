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
package org.androidpn.server.service;

import org.androidpn.server.xmpp.XmppServer;

/**
 * 工具类-获取服务对象
 * 
 * @author lijian
 * @date 2016-12-3 下午11:39:19
 */
public class ServiceLocator {

    public static String USER_SERVICE = "userService";

    public static String NOTIFICATION_SERVICE = "notificationService";

	/**
	 * 根据名称获取一个服务对象
	 * 
	 * @param name
	 *            服务的名称
	 * @return
	 */
    public static Object getService(String name) {
        return XmppServer.getInstance().getBean(name);
    }

	/**
	 * 获得用户服务
	 * 
	 * @return
	 */
    public static UserService getUserService() {
		System.out.println("ServiceLocator-getUserService--------------------------");
        return (UserService) XmppServer.getInstance().getBean(USER_SERVICE);
    }
    
    /**
     * 获得消息服务
     * 
     * @return
     */
    public static NotificationService getNotificationService() {
		System.out.println("ServiceLocator-getNotificationService--------------------------");
    	return (NotificationService) XmppServer.getInstance().getBean(NOTIFICATION_SERVICE);
    }

}
