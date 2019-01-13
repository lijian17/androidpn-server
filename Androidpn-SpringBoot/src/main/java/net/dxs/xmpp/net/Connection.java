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

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.ssl.SslFilter;
import org.dom4j.io.OutputFormat;
import org.jivesoftware.util.XMLWriter;
import org.xmpp.packet.Packet;

import net.dxs.utils.Config;
import net.dxs.xmpp.session.Session;
import net.dxs.xmpp.ssl.SSLConfig;
import net.dxs.xmpp.ssl.SSLKeyManagerFactory;
import net.dxs.xmpp.ssl.SSLTrustManagerFactory;

/**
 * 代表一个连接到服务器的XMPP连接
 * 
 * @author lijian
 * @date 2016-12-4 上午12:14:51
 */
public class Connection {

    private static final Log log = LogFactory.getLog(Connection.class);

    private IoSession ioSession;

	/** 这是一个抽象类，用于服务器和客户端之间的会话 */
    private Session session;

	/** 会话连接关闭监听器 */
    private ConnectionCloseListener closeListener;

	/** 主版本号 */
    private int majorVersion = 1;

	/** 小版本号 */
    private int minorVersion = 0;

	/** 语言 */
    private String language = null;

	/**
	 * <pre>
	 * 安全传输层协议（TLS）用于在两个通信应用程序之间提供保密性和数据完整性。 
	 * 该协议由两层组成： TLS 记录协议（TLS Record）和 TLS握手协议（TLS Handshake）。
	 * </per>
	 */
    private TLSPolicy tlsPolicy = TLSPolicy.optional;

	/** 本地线程池编码器 */
    private static ThreadLocal<?> encoder = new ThreadLocalEncoder();

	/** 当前连接的关闭状态 */
    private boolean closed;

	/**
	 * 代表一个连接到服务器的XMPP连接.
	 * 
	 * @param ioSession
	 */
    public Connection(IoSession ioSession) {
        this.ioSession = ioSession;
        this.closed = false;
    }

    //    /**
    //     * 验证该连接是否依然存活着
    //     * 
    //     * @return true if the socket remains valid, false otherwise.
    //     */
    //    public boolean validate() {
    //        if (isClosed()) {
    //            return false;
    //        }
    //        deliverRawText(" ");
    //        return !isClosed();
    //    }

	/**
	 * 关闭session包括相关的socket连接，通知所有 侦听器，该通道正在关闭。
	 */
    public void close() {
        boolean closedSuccessfully = false;
        synchronized (this) {
            if (!isClosed()) {
                try {
                    deliverRawText("</stream:stream>", false);
                } catch (Exception e) {
                    // Ignore
                }
                if (session != null) {
                    session.setStatus(Session.STATUS_CLOSED);
                }
                ioSession.close(false);
                closed = true;
                closedSuccessfully = true;
            }
        }
        if (closedSuccessfully) {
            notifyCloseListeners();
        }
    }

	/**
	 * 发送一个通知消息，指示该服务器正在被关闭
	 */
    public void systemShutdown() {
        deliverRawText("<stream:error><system-shutdown "
                + "xmlns='urn:ietf:params:xml:ns:xmpp-streams'/></stream:error>");
        close();
    }

	/**
	 * 初始化这个拥有会话的连接
	 * 
	 * @param session
	 *            拥有此连接的会话
	 */
    public void init(Session session) {
        this.session = session;
    }

	/**
	 * 当前连接是否关闭
	 * 
	 * @return true：关闭, false：未关闭.
	 */
    public boolean isClosed() {
        if (session == null) {
            return closed;
        }
        return session.getStatus() == Session.STATUS_CLOSED;
    }

    //    /**
    //     * Returns true if this connection is secure.
    //     * 
    //     * @return true if the connection is secure
    //     */
    //    public boolean isSecure() {
    //        return ioSession.getFilterChain().contains("tls");
    //    }

	/**
	 * 注册一个关闭事件通知监听器
	 * 
	 * @param listener
	 *            关闭事件监听器
	 */
    public void registerCloseListener(ConnectionCloseListener listener) {
        if (closeListener != null) {
            throw new IllegalStateException("Close listener already configured");
        }
        if (isClosed()) {
            listener.onConnectionClose(session);
        } else {
            closeListener = listener;
        }
    }

	/**
	 * 移除一个监听器-注册关闭事件
	 * 
	 * @param listener
	 *            关闭事件的监听器
	 */
    public void unregisterCloseListener(ConnectionCloseListener listener) {
        if (closeListener == listener) {
            closeListener = null;
        }
    }

	/**
	 * 通知关闭监听
	 */
    private void notifyCloseListeners() {
        if (closeListener != null) {
            try {
                closeListener.onConnectionClose(session);
            } catch (Exception e) {
                log.error("Error notifying listener: " + closeListener, e);
            }
        }
    }

	/**
	 * 将此数据包传递给此连接（不检查收件人）
	 * 
	 * @param packet
	 *            提供的数据包
	 */
    public void deliver(Packet packet) {
        log.debug("SENT: " + packet.toXML());
        if (!isClosed()) {
            IoBuffer buffer = IoBuffer.allocate(4096);
            buffer.setAutoExpand(true);

            boolean errorDelivering = false;
            try {
                XMLWriter xmlSerializer = new XMLWriter(new IoBufferWriter(
                        buffer, (CharsetEncoder) encoder.get()),
                        new OutputFormat());
                xmlSerializer.write(packet.getElement());
                xmlSerializer.flush();
                buffer.flip();
                ioSession.write(buffer);
            } catch (Exception e) {
				log.debug("连接：传递数据包错误" + "\n" + this.toString(), e);
                errorDelivering = true;
            }
            if (errorDelivering) {
                close();
            } else {
                session.incrementServerPacketCount();
            }
        }
    }

	/**
	 * 将原始文本传送到这个连接（异步模式）
	 * 
	 * @param text
	 *            XML节点字符串传递
	 */
    public void deliverRawText(String text) {
        deliverRawText(text, true);
    }

	/**
	 * 将原始文本传送到这个连接
	 * 
	 * @param text
	 *            XML节点字符串传递
	 * @param asynchronous
	 *            是否异步（true：异步；false：同步）
	 */
    private void deliverRawText(String text, boolean asynchronous) {
        log.debug("SENT: " + text);
        if (!isClosed()) {
			// 分配一个指定长度的IoBuffer
            IoBuffer buffer = IoBuffer.allocate(text.length());
            buffer.setAutoExpand(true);// 设置自动扩大

            boolean errorDelivering = false;
            try {
                buffer.put(text.getBytes("UTF-8"));
                buffer.flip();
                if (asynchronous) {
                    ioSession.write(buffer);
                } else {
					// 发送节点，并等待ACK（确认）
                    boolean ok = ioSession.write(buffer).awaitUninterruptibly(
                            Config.getInt("connection.ack.timeout", 2000));
                    if (!ok) {
						log.warn("发送的节点，无法确认是否有接收到: " + this.toString());
                    }
                }
            } catch (Exception e) {
				log.debug("连接：错误传递原始文本" + "\n" + this.toString(), e);
                errorDelivering = true;
            }
			// 如果发送文本失败，关闭连接
            if (errorDelivering && asynchronous) {
                close();
            }
        }
    }

	/**
	 * 启动TLS
	 * 
	 * @param authentication
	 *            认证
	 * @throws Exception
	 */
    public void startTLS(ClientAuth authentication) throws Exception {
        log.debug("startTLS()...");
        KeyStore ksKeys = SSLConfig.getKeyStore();
        String keypass = SSLConfig.getKeyPassword();

        KeyStore ksTrust = SSLConfig.getc2sTrustStore();
        String trustpass = SSLConfig.getc2sTrustPassword();

        KeyManager[] km = SSLKeyManagerFactory.getKeyManagers(ksKeys, keypass);
        TrustManager[] tm = SSLTrustManagerFactory.getTrustManagers(ksTrust,
                trustpass);

		// 根据TLS协议获得一个SSL上下文
        SSLContext tlsContext = SSLContext.getInstance("TLS");
        tlsContext.init(km, tm, null);

        SslFilter filter = new SslFilter(tlsContext);
        ioSession.getFilterChain().addFirst("tls", filter);
        //ioSession.getFilterChain().addBefore("executor", "tls", filter);
        ioSession.setAttribute(SslFilter.DISABLE_ENCRYPTION_ONCE, Boolean.TRUE);

        deliverRawText("<proceed xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\"/>");
    }

	/**
	 * 获得这个IP地址
	 * 
	 * @return IP地址
	 * @throws UnknownHostException
	 *             如果主机的IP地址无法确定
	 */
    public String getHostAddress() throws UnknownHostException {
        return ((InetSocketAddress) ioSession.getRemoteAddress()).getAddress()
                .getHostAddress();
    }

	/**
	 * 获取这个IP地址的主机名
	 * 
	 * @return IP地址对应的主机名
	 * @throws UnknownHostException
	 *             如果主机的IP地址无法确定
	 */
	public String getHostName() throws UnknownHostException {
		return ((InetSocketAddress) ioSession.getRemoteAddress()).getAddress()
				.getHostName();
	}

	/**
	 * 返回此连接使用的XMPP的主版本
	 * 
	 * @return XMPP的主版本
	 */
	public int getMajorXMPPVersion() {
		return majorVersion;
	}

	/**
	 * 返回此连接使用的XMPP的次版本
	 * 
	 * @return XMPP的次版本
	 */
	public int getMinorXMPPVersion() {
		return minorVersion;
	}

	/**
	 * 设置XMPP版本信息
	 * 
	 * @param majorVersion
	 *            主版本
	 * @param minorVersion
	 *            次版本
	 */
	public void setXMPPVersion(int majorVersion, int minorVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	/**
	 * 获得应用于此连接的语言码
	 * 
	 * @return 语言码
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * 设置应用于此连接的语言码
	 * 
	 * @param language
	 *            语言码
	 */
	public void setLanaguage(String language) {
		this.language = language;
	}

	/**
	 * 本地线程编码器
	 * 
	 * @author lijian
	 * @date 2016-8-6 上午8:14:04
	 */
	private static class ThreadLocalEncoder extends ThreadLocal<Object> {
		@Override
		protected Object initialValue() {
			return Charset.forName("UTF-8").newEncoder();
		}
	}

	/**
	 * 获得TLS策略
	 * 
	 * @return
	 */
	public TLSPolicy getTlsPolicy() {
		return tlsPolicy;
	}

	/**
	 * 设置TLS策略
	 * 
	 * @param tlsPolicy
	 */
	public void setTlsPolicy(TLSPolicy tlsPolicy) {
		this.tlsPolicy = tlsPolicy;
	}

    /**
     * 枚举：可能的TLS策略需要与服务器进行交互
     * 
     * @author lijian
     * @date 2016-12-4 上午12:21:40
     */
    public enum TLSPolicy {
        required, optional, disabled
    }

    /**
     * 枚举：指定客户端是否应进行身份认证（以及如何认证），在TLS协商
     * 
     * @author lijian
     * @date 2016-12-4 上午12:21:51
     */
    public enum ClientAuth {
        disabled, wanted, needed
    }

}
