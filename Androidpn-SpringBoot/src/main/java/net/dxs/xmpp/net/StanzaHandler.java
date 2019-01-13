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

import java.io.IOException;
import java.io.StringReader;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.XMPPPacketReader;
import org.jivesoftware.openfire.net.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Roster;
import org.xmpp.packet.StreamError;

import net.dxs.utils.Config;
import net.dxs.xmpp.router.PacketRouter;
import net.dxs.xmpp.session.ClientSession;
import net.dxs.xmpp.session.Session;

/**
 * 这类处理传入的XML节
 * 
 * @author lijian
 * @date 2016-12-4 上午12:23:41
 */
public class StanzaHandler {

    private static final Log log = LogFactory.getLog(StanzaHandler.class);

	/** 代表一个连接到服务器的XMPP连接 */
	protected Connection connection;

	/** 会话 */
	protected Session session;

	/** 服务名 */
	protected String serverName;

	/** 会话创建 */
	private boolean sessionCreated = false;

	/** 启动TLS */
	private boolean startedTLS = false;

	/** 数据包的路由器 */
	private PacketRouter router;

	/**
	 * 这类处理传入的XML节
	 * 
	 * @param serverName
	 *            服务名
	 * @param connection
	 *            连接
	 */
    public StanzaHandler(String serverName, Connection connection) {
        this.serverName = serverName;
        this.connection = connection;
        this.router = new PacketRouter();
    }

	/**
	 * 使用给定的XMPP包阅读器接收节过程
	 * 
	 * @param stanza
	 *            收到的statza
	 * @param reader
	 *            XMPP包阅读器
	 * @throws Exception
	 *             如果XML流无效。
	 */
    public void process(String stanza, XMPPPacketReader reader)
            throws Exception {
        boolean initialStream = stanza.startsWith("<stream:stream");
        if (!sessionCreated || initialStream) {
            if (!initialStream) {
                return; // Ignore <?xml version="1.0"?>
            }
            if (!sessionCreated) {
                sessionCreated = true;
                MXParser parser = reader.getXPPParser();
                parser.setInput(new StringReader(stanza));
                createSession(parser);
            } else if (startedTLS) {
                startedTLS = false;
                tlsNegotiated();
            }
            return;
        }

		// 如果请求结束流
        if (stanza.equals("</stream:stream>")) {
            session.close();
            return;
        }
		// 忽略 <?xml version="1.0"?>
        if (stanza.startsWith("<?xml")) {
            return;
        }
		// 创建DOM对象
        Element doc = reader.read(new StringReader(stanza)).getRootElement();
        if (doc == null) {
            return;
        }

        String tag = doc.getName();
        if ("starttls".equals(tag)) {
            if (negotiateTLS()) { // Negotiate TLS
                startedTLS = true;
            } else {
                connection.close();
                session = null;
            }
        } else if ("message".equals(tag)) {
            processMessage(doc);
        } else if ("presence".equals(tag)) {
            log.debug("presence...");
            processPresence(doc);
        } else if ("iq".equals(tag)) {
            log.debug("iq...");
            processIQ(doc);
        } else {
			log.warn("意外的数据包tag (not message, iq, presence)" + doc.asXML());
            session.close();
        }

    }

	/**
	 * 加工Message
	 * 
	 * @param doc
	 */
    private void processMessage(Element doc) {
        log.debug("processMessage()...");
        Message packet;
        try {
            packet = new Message(doc, false);
        } catch (IllegalArgumentException e) {
			log.debug("拒绝包，JID畸形", e);
            Message reply = new Message();
            reply.setID(doc.attributeValue("id"));
            reply.setTo(session.getAddress());
            reply.getElement().addAttribute("from", doc.attributeValue("to"));
            reply.setError(PacketError.Condition.jid_malformed);
            session.process(reply);
            return;
        }

        packet.setFrom(session.getAddress());
        router.route(packet);
        session.incrementClientPacketCount();
    }

	/**
	 * 加工Presence
	 * 
	 * @param doc
	 */
    private void processPresence(Element doc) {
        log.debug("processPresence()...");
        Presence packet;
        try {
            packet = new Presence(doc, false);
        } catch (IllegalArgumentException e) {
			log.debug("拒绝包。JID畸形", e);
            Presence reply = new Presence();
            reply.setID(doc.attributeValue("id"));
            reply.setTo(session.getAddress());
            reply.getElement().addAttribute("from", doc.attributeValue("to"));
            reply.setError(PacketError.Condition.jid_malformed);
            session.process(reply);
            return;
        }
        if (session.getStatus() == Session.STATUS_CLOSED
                && packet.isAvailable()) {
			log.warn("忽略可用的存在数据包的封闭会话: " + packet);
            return;
        }

        packet.setFrom(session.getAddress());
        router.route(packet);
        session.incrementClientPacketCount();
    }

	/**
	 * 加工IQ
	 * 
	 * @param doc
	 */
    private void processIQ(Element doc) {
        log.debug("processIQ()...");
        IQ packet;
        try {
            packet = getIQ(doc);
        } catch (IllegalArgumentException e) {
			log.debug("拒绝包。JID畸形", e);
            IQ reply = new IQ();
            if (!doc.elements().isEmpty()) {
                reply.setChildElement(((Element) doc.elements().get(0))
                        .createCopy());
            }
            reply.setID(doc.attributeValue("id"));
            reply.setTo(session.getAddress());
            String to = doc.attributeValue("to");
            if (to != null) {
                reply.getElement().addAttribute("from", to);
            }
            reply.setError(PacketError.Condition.jid_malformed);
            session.process(reply);
            return;
        }

		// if (packet.getID() == null) {
		// // IQ 数据包必须要有一个'id'属性
		// StreamError error = new StreamError(
		// StreamError.Condition.invalid_xml);
		// session.deliverRawText(error.toXML());
		// session.close();
		// return;
		// }

        packet.setFrom(session.getAddress());
        router.route(packet);
        session.incrementClientPacketCount();
    }

	/**
	 * 获取IQ
	 * 
	 * @param doc
	 * @return
	 */
    private IQ getIQ(Element doc) {
        Element query = doc.element("query");
        if (query != null && "jabber:iq:roster".equals(query.getNamespaceURI())) {
            return new Roster(doc);
        } else {
            return new IQ(doc, false);
        }
    }

	/**
	 * 创建Session
	 * 
	 * @param xpp
	 *            xmlpull解析器
	 * @throws XmlPullParserException
	 *             xmlpull解析器异常
	 * @throws IOException
	 *             IO异常
	 */
    private void createSession(XmlPullParser xpp)
            throws XmlPullParserException, IOException {
        for (int eventType = xpp.getEventType(); eventType != XmlPullParser.START_TAG;) {
            eventType = xpp.next();
        }
		// 基于所发送的命名空间创建正确的会话
        String namespace = xpp.getNamespace(null);
        if ("jabber:client".equals(namespace)) {
            session = ClientSession.createSession(serverName, connection, xpp);
            if (session == null) {
                StringBuilder sb = new StringBuilder(250);
                sb.append("<?xml version='1.0' encoding='UTF-8'?>");
                sb.append("<stream:stream from=\"").append(serverName);
                sb.append("\" id=\"").append(randomString(5));
                sb.append("\" xmlns=\"").append(xpp.getNamespace(null));
                sb.append("\" xmlns:stream=\"").append(
                        xpp.getNamespace("stream"));
                sb.append("\" version=\"1.0\">");

				// 响应中的一个糟糕的命名空间前缀
                StreamError error = new StreamError(
                        StreamError.Condition.bad_namespace_prefix);
                sb.append(error.toXML());
                connection.deliverRawText(sb.toString());
                connection.close();
				log.warn("由于bad_namespace_prefix关闭session在数据流的头部: " + namespace);
            }
        }
    }

	/**
	 * 协商TLS协议
	 * 
	 * @return
	 */
    private boolean negotiateTLS() {
        if (connection.getTlsPolicy() == Connection.TLSPolicy.disabled) {
			// 设置not_authorized错误
            StreamError error = new StreamError(
                    StreamError.Condition.not_authorized);
            connection.deliverRawText(error.toXML());
            connection.close();
			log.warn("当TLS从未提供服务时，将请求初始化，关闭连接 : " + connection);
            return false;
        }
		// 客户要求使用TLS安全连接
        try {
            startTLS();
        } catch (Exception e) {
			log.error("在协商TLS时发生错误：", e);
            connection
                    .deliverRawText("<failure xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\">");
            connection.close();
            return false;
        }
        return true;
    }

	/**
	 * 启动TLS
	 * 
	 * @throws Exception
	 */
    private void startTLS() throws Exception {
        Connection.ClientAuth policy;
        try {
			// TODO 配置文件config.properties中无xmpp.client.cert.policy
            policy = Connection.ClientAuth.valueOf(Config.getString(
                    "xmpp.client.cert.policy", "disabled"));
        } catch (IllegalArgumentException e) {
            policy = Connection.ClientAuth.disabled;
        }
        connection.startTLS(policy);
    }

	/**
	 * TLS协议
	 */
    private void tlsNegotiated() {
		// 提供流的特点包括SASL机制
        StringBuilder sb = new StringBuilder(620);
        sb.append("<?xml version='1.0' encoding='UTF-8'?>");
        sb.append("<stream:stream ");
        sb.append("xmlns:stream=\"http://etherx.jabber.org/streams\" ");
        sb.append("xmlns=\"jabber:client\" from=\"");
        sb.append(serverName);
        sb.append("\" id=\"");
        sb.append(session.getStreamID());
        sb.append("\" xml:lang=\"");
        sb.append(connection.getLanguage());
        sb.append("\" version=\"");
        sb.append(Session.MAJOR_VERSION).append(".").append(
                Session.MINOR_VERSION);
        sb.append("\">");
        sb.append("<stream:features>");
		// 包括具体的功能如客户端会话认证和登记
        String specificFeatures = session.getAvailableStreamFeatures();
        if (specificFeatures != null) {
            sb.append(specificFeatures);
        }
        sb.append("</stream:features>");
        connection.deliverRawText(sb.toString());
    }

	/**
	 * 获得一个指定长度的随机字符串
	 * 
	 * @param length
	 * @return
	 */
    private String randomString(int length) {
        if (length < 1) {
            return null;
        }
        char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
                + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
        char[] randBuffer = new char[length];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[new Random().nextInt(71)];
        }
        return new String(randBuffer);
    }

    //  public String getNamespace() {
    //  return "jabber:client";
    //}

}
