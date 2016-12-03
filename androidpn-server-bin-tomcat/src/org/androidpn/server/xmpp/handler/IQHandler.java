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
package org.androidpn.server.xmpp.handler;

import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.router.PacketDeliverer;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

/**
 * 处理路由的IQ数据包
 * 
 * @author lijian
 * @date 2016-12-4 上午12:06:54
 */
public abstract class IQHandler {

    protected final Log log = LogFactory.getLog(getClass());

	/** 连接到服务器的会话管理器 */
    protected SessionManager sessionManager;

    /**
     * Constructor.
     */
    public IQHandler() {
        sessionManager = SessionManager.getInstance();
    }

	/**
	 * 处理接收到的IQ数据包
	 * 
	 * @param packet
	 */
    public void process(Packet packet) {
        IQ iq = (IQ) packet;
        try {
            IQ reply = handleIQ(iq);
            if (reply != null) {
                PacketDeliverer.deliver(reply);
            }
        } catch (UnauthorizedException e) {
            if (iq != null) {
                try {
					// 创建一个应答IQ
                    IQ response = IQ.createResultIQ(iq);
                    response.setChildElement(iq.getChildElement().createCopy());
                    response.setError(PacketError.Condition.not_authorized);
                    sessionManager.getSession(iq.getFrom()).process(response);
                } catch (Exception de) {
					log.error("内部服务器错误", de);
					// TODO 这里关闭了会话
                    sessionManager.getSession(iq.getFrom()).close();
                }
            }
        } catch (Exception e) {
			log.error("内部服务器错误", e);
            try {
                IQ response = IQ.createResultIQ(iq);
                response.setChildElement(iq.getChildElement().createCopy());
                response.setError(PacketError.Condition.internal_server_error);
                sessionManager.getSession(iq.getFrom()).process(response);
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

	/**
	 * 处理接收到的IQ数据包
	 * 
	 * @param packet
	 * @return
	 * @throws UnauthorizedException
	 *             如果用户未被授权
	 */
	public abstract IQ handleIQ(IQ packet) throws UnauthorizedException;

	/**
	 * 获得处理的名称空间
	 * 
	 * @return 名称空间
	 */
	public abstract String getNamespace();

}
