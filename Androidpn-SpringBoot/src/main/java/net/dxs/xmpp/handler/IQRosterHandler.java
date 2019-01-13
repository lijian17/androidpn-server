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
package net.dxs.xmpp.handler;

import org.xmpp.packet.IQ;

import net.dxs.xmpp.UnauthorizedException;

/**
 * 本类处理TYPE_IQ类型为jabber:iq:roster的协议(花名册)
 * 
 * @author lijian
 * @date 2016-12-4 上午12:10:23
 */
public class IQRosterHandler extends IQHandler {
    
    private static final String NAMESPACE = "jabber:iq:roster";

	/**
	 * 本类处理TYPE_IQ类型为jabber:iq:roster的协议(花名册).
	 */
    public IQRosterHandler() {        
    }

    @Override
    public IQ handleIQ(IQ packet) throws UnauthorizedException {
        // TODO
        return null;
    }
    
    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

}
