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
package net.dxs.xmpp.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.dom4j.io.XMPPPacketReader;
import org.jivesoftware.openfire.net.MXParser;
import org.jivesoftware.openfire.nio.XMLLightweightParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import net.dxs.xmpp.XmppServer;

/**
 * 创建一个新的会话，并销毁之前的。收到XML节点StanzaHandler
 * 
 * @author lijian
 * @date 2016-12-4 上午12:29:22
 */
public class XmppIoHandler implements IoHandler {

    private static final Log log = LogFactory.getLog(XmppIoHandler.class);

	/** XML解析器 */
	public static final String XML_PARSER = "XML_PARSER";

	/** 连接 */
	private static final String CONNECTION = "CONNECTION";

	/** 节点处理 */
	private static final String STANZA_HANDLER = "STANZA_HANDLER";

	/** 服务名 */
	private String serverName;

	/** 解析器集合 */
	private static Map<Integer, XMPPPacketReader> parsers = new ConcurrentHashMap<Integer, XMPPPacketReader>();

	/** XMLPull解析工厂 */
	private static XmlPullParserFactory factory = null;

    static {
        try {
            factory = XmlPullParserFactory.newInstance(
                    MXParser.class.getName(), null);
            factory.setNamespaceAware(true);
        } catch (XmlPullParserException e) {
			log.error("创建解析器工厂时出错", e);
        }
    }

	/**
	 * 创建一个新的会话，并销毁之前的。收到XML节点StanzaHandler
	 */
	public XmppIoHandler() {
		serverName = XmppServer.getInstance().getServerName();
	}

	/**
	 * 当一个新的连接被创建时，将执行一个I/O处理器线程
	 */
	public void sessionCreated(IoSession session) throws Exception {
		log.debug("sessionCreated()...");
	}

	/**
	 * 当一个连接被打开时，将被调用
	 */
	public void sessionOpened(IoSession session) throws Exception {
		log.debug("sessionOpened()...");
		log.debug("remoteAddress=" + session.getRemoteAddress());
		// 创建一个新的XML解析器
		XMLLightweightParser parser = new XMLLightweightParser("UTF-8");
		session.setAttribute(XML_PARSER, parser);
		// 创建一个新的连接
		Connection connection = new Connection(session);
		session.setAttribute(CONNECTION, connection);
		session.setAttribute(STANZA_HANDLER, new StanzaHandler(serverName,
				connection));
	}

	/**
	 * 当一个连接被关闭时执行
	 */
	public void sessionClosed(IoSession session) throws Exception {
		log.debug("sessionClosed()...");
		Connection connection = (Connection) session.getAttribute(CONNECTION);
		connection.close();
	}

	/**
	 * 当连接空闲时，将执行相关的IdleStatus
	 */
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		log.debug("sessionIdle()...");
		Connection connection = (Connection) session.getAttribute(CONNECTION);
		if (log.isDebugEnabled()) {
			log.debug("关闭闲置连接: " + connection);
		}
		connection.close();
	}

	/**
	 * 当出现异常时被调用
	 */
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		log.debug("exceptionCaught()...");
		log.error(cause);
	}

	/**
	 * 当接收到消息时调用
	 */
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		log.debug("messageReceived()...");
		log.debug("RCVD: " + message);

		// 获得一个节点处理器
		StanzaHandler handler = (StanzaHandler) session
				.getAttribute(STANZA_HANDLER);

		// 获得一个XML包解析器
		int hashCode = Thread.currentThread().hashCode();
		XMPPPacketReader parser = parsers.get(hashCode);
		if (parser == null) {
			parser = new XMPPPacketReader();
			parser.setXPPFactory(factory);
			parsers.put(hashCode, parser);
		}

		// 使用节点处理器处理消息
		try {
			handler.process((String) message, parser);
		} catch (Exception e) {
			log.error("处理消息时发生错误关闭连接: " + message, e);
			Connection connection = (Connection) session
					.getAttribute(CONNECTION);
			connection.close();
		}
	}

	/**
	 * 当一个消息使用IoSession.write(Object)发送出去，将被调用
	 */
	public void messageSent(IoSession session, Object message) throws Exception {
		log.debug("messageSent()...");
	}

}