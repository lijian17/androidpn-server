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

import org.androidpn.server.xmpp.net.XmppIoHandler;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.jivesoftware.openfire.nio.XMLLightweightParser;

/**
 * 解码器（继承了累计协议解码器）
 * 
 * <pre>
 * 类，从名字上可以看出累积性的协议解码器，也就是说只要有数据发送过来，这个类就会去读取数据，
 * 然后累积到内部的IoBuffer 缓冲区，但是具体的拆包（把累积到缓冲区的数据解码为JAVA 对象）
 * 交由子类的doDecode()方法完成，实际上CumulativeProtocolDecoder就是在decode()
 * 反复的调用暴漏给子类实现的doDecode()方法。
 * </pre>
 * 
 * @author lijian
 * @date 2016-12-4 上午12:02:01
 */
public class XmppDecoder extends CumulativeProtocolDecoder {

    // private final Log log = LogFactory.getLog(XmppDecoder.class);

    @Override
    public boolean doDecode(IoSession session, IoBuffer in,
            ProtocolDecoderOutput out) throws Exception {
        // log.debug("doDecode(...)...");

        XMLLightweightParser parser = (XMLLightweightParser) session
                .getAttribute(XmppIoHandler.XML_PARSER);
        parser.read(in);

		// 如果有消息
        if (parser.areThereMsgs()) {
            for (String stanza : parser.getMsgs()) {
                out.write(stanza);
            }
        }
        
		// 是否还有剩余
        return !in.hasRemaining();
    }

}
