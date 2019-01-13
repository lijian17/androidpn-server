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
package net.dxs.xmpp.router;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

/**
 * 处理传入的数据包并将其路由到相应的处理程序
 * 
 * @author lijian
 * @date 2016-12-4 上午12:36:42
 */
public class PacketRouter {

	/** Message路由 */
	private MessageRouter messageRouter;

	/** Presence路由 */
	private PresenceRouter presenceRouter;

	/** IQ路由 */
	private IQRouter iqRouter;

	/**
	 * 处理传入的数据包并将其路由到相应的处理程序.
	 */
	public PacketRouter() {
		messageRouter = new MessageRouter();
		presenceRouter = new PresenceRouter();
		iqRouter = new IQRouter();
	}

	/**
	 * 路由基于ITS类型的数据包
	 * 
	 * @param packet
	 */
	public void route(Packet packet) {
		if (packet instanceof Message) {
			route((Message) packet);
		} else if (packet instanceof Presence) {
			route((Presence) packet);
		} else if (packet instanceof IQ) {
			route((IQ) packet);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * 路由IQ包
	 * 
	 * @param packet
	 */
	public void route(IQ packet) {
		iqRouter.route(packet);
	}

	/**
	 * 路由Message包
	 * 
	 * @param packet
	 */
	public void route(Message packet) {
		messageRouter.route(packet);
	}

	/**
	 * 路由Presence包
	 * 
	 * @param packet
	 */
	public void route(Presence packet) {
		presenceRouter.route(packet);
	}

}
