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
package org.androidpn.server.xmpp.presence;

import org.androidpn.server.model.User;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.SessionManager;
import org.xmpp.packet.Presence;

/**
 * 用户出席信息管理类
 * 
 * @author lijian
 * @date 2016-12-4 上午12:31:36
 */
public class PresenceManager {

    private SessionManager sessionManager;

	/**
	 * 用户出席信息管理类
	 */
	public PresenceManager() {
		sessionManager = SessionManager.getInstance();
	}

	/**
	 * 获取用户是否可用的
	 * 
	 * @param user
	 * @return true：用户是可用的
	 */
	public boolean isAvailable(User user) {
		return sessionManager.getSession(user.getUsername()) != null;
	}

	/**
	 * 获得用户当前出席状态
	 * 
	 * @param user
	 * @return
	 */
	public Presence getPresence(User user) {
		if (user == null) {
			return null;
		}
		Presence presence = null;
		ClientSession session = sessionManager.getSession(user.getUsername());
		if (session != null) {
			presence = session.getPresence();
		}
		return presence;
	}

}
