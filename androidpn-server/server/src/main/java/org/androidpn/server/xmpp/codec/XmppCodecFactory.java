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
package org.androidpn.server.xmpp.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 工厂-XMPP编解码器
 * 
 * @author lijian
 * @date 2016-7-31 下午10:52:37
 */
public class XmppCodecFactory implements ProtocolCodecFactory {

	/** 编码器 */
	private final XmppEncoder encoder;

	/** 解码器 */
	private final XmppDecoder decoder;

	public XmppCodecFactory() {
		encoder = new XmppEncoder();
		decoder = new XmppDecoder();
	}

	/**
	 * 返回一个新的（或可重复使用的）ProtocolEncoder实例
	 */
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}

	/**
	 * 返回一个新的（或可重复使用的）ProtocolDecoder实例
	 */
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return decoder;
	}

}
