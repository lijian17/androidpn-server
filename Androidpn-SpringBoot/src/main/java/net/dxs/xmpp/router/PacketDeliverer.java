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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

import net.dxs.xmpp.PacketException;
import net.dxs.xmpp.session.ClientSession;
import net.dxs.xmpp.session.SessionManager;

/**
 * 将数据包投递给连接的会话
 * 
 * @author lijian
 * @date 2016-12-4 上午12:36:13
 */
public class PacketDeliverer {

    private static final Log log = LogFactory.getLog(PacketDeliverer.class);

	/**
	 * 将数据包传递给数据包收件人
	 * 
	 * @param packet
	 *            要被投递的数据包
	 * @throws PacketException
	 *             如果数据包为空，或收件人未找到
	 */
	public static void deliver(Packet packet) throws PacketException {
		if (packet == null) {
			throw new PacketException("数据包为null");
		}

		try {
			JID recipient = packet.getTo();
			if (recipient != null) {
				ClientSession clientSession = SessionManager.getInstance()
						.getSession(recipient);
				if (clientSession != null) {
					clientSession.deliver(packet);
				}
			}
		} catch (Exception e) {
			log.error("不能提供数据包: " + packet.toString(), e);
		}
	}
}
