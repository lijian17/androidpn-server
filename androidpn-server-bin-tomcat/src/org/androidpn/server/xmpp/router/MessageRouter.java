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

import org.xmpp.packet.Message;

/**
 * Message路由
 * 
 * @author lijian
 * @date 2016-12-4 上午12:35:41
 */
public class MessageRouter {

	/**
	 * Message路由
	 */
	public MessageRouter() {
	}

	/**
	 * 路由这个Message包
	 * 
	 * @param packet
	 */
	public void route(Message packet) {
		throw new RuntimeException("请实现这个！");
	}

}
