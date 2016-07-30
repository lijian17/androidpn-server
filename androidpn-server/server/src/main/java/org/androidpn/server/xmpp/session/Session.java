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
package org.androidpn.server.xmpp.session;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.androidpn.server.xmpp.net.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

/**
 * 这是一个抽象类，用于服务器和客户端之间的会话。
 * 
 * @author lijian
 * @date 2016-7-30 下午4:52:35
 */
public abstract class Session {
	private static final Log log = LogFactory.getLog(Session.class);

	/** 大版本 */
	public static final int MAJOR_VERSION = 1;
	/** 小版本 */
	public static final int MINOR_VERSION = 0;

	/** 关闭时的会话状态 */
	public static final int STATUS_CLOSED = 0;
	/** 连接时的会话状态 */
	public static final int STATUS_CONNECTED = 1;
	/** 认证时的会话状态 */
	public static final int STATUS_AUTHENTICATED = 2;

	protected Connection connection;

	protected SessionManager sessionManager;

	private String serverName;

	private JID address;

	private String streamID;

	private int status = STATUS_CONNECTED;

	/** 会话创建时间 */
	private long startDate = System.currentTimeMillis();

	/** 会话最后活动时间 */
	private long lastActiveDate;

	/** 从客户端发送到服务器的数据包的数量 */
	private long clientPacketCount = 0;

	/** 从服务器发送到客户端的数据包的数量 */
	private long serverPacketCount = 0;

	/** 会话数据集 */
	private final Map<String, Object> sessionData = new HashMap<String, Object>();

	/**
	 * 根据服务名和流ID，创建一个JID
	 * 
	 * @param serverName
	 *            服务名
	 * @param conn
	 *            连接
	 * @param streamID
	 *            流ID
	 */
	public Session(String serverName, Connection conn, String streamID) {
		this.connection = conn;
		this.sessionManager = SessionManager.getInstance();
		this.serverName = serverName;
		this.streamID = streamID;
		this.address = new JID(null, serverName, streamID, true);
	}

	/**
	 * 获得与此会话相关联的连接
	 * 
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * 获得服务名
	 * 
	 * @return
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * 获得与此会话相关联的流ID
	 * 
	 * @return
	 */
	public String getStreamID() {
		return streamID;
	}

	/**
	 * 获得JID
	 * 
	 * @return
	 */
	public JID getAddress() {
		return address;
	}

	/**
	 * 设置此会话的新JID
	 * 
	 * @param address
	 */
	public void setAddress(JID address) {
		this.address = address;
	}

	/**
	 * 返回会话的当前状态
	 * 
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 设置此会话的新状态
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * 返回此会话的创建时间
	 * 
	 * @return
	 */
	public Date getCreationDate() {
		return new Date(startDate);
	}

	/**
	 * 返回此会话的最后活动时间
	 * 
	 * @return
	 */
	public Date getLastActiveDate() {
		return new Date(lastActiveDate);
	}

	/**
	 * 从客户端发送到服务器的数据包的计数器执行自增
	 */
	public void incrementClientPacketCount() {
		clientPacketCount++;
		lastActiveDate = System.currentTimeMillis();
	}

	/**
	 * 从服务器发送到客户端的数据包的计数器执行自增
	 */
	public void incrementServerPacketCount() {
		serverPacketCount++;
		lastActiveDate = System.currentTimeMillis();
	}

	/**
	 * 返回从客户端发送到服务器的数据包的数量
	 * 
	 * @return
	 */
	public long getNumClientPackets() {
		return clientPacketCount;
	}

	/**
	 * 返回从服务器发送到客户端的数据包的数量
	 * 
	 * @return
	 */
	public long getNumServerPackets() {
		return serverPacketCount;
	}

	/**
	 * 设置会话数据
	 * 
	 * @param key
	 * @param value
	 */
	public void setSessionData(String key, Object value) {
		synchronized (sessionData) {
			sessionData.put(key, value);
		}
	}

	/**
	 * 根据键值获得一条会话数据
	 * 
	 * @param key
	 * @return
	 */
	public Object getSessionData(String key) {
		synchronized (sessionData) {
			return sessionData.get(key);
		}
	}

	/**
	 * 根据键值移除会话中的一条数据
	 * 
	 * @param key
	 */
	public void removeSessionData(String key) {
		synchronized (sessionData) {
			sessionData.remove(key);
		}
	}

	/**
	 * 处理数据包
	 * 
	 * @param packet
	 */
	public void process(Packet packet) {
		try {
			deliver(packet);
		} catch (Exception e) {
			log.error("内部服务器错误", e);
		}
	}

	/**
	 * 将数据包传送到相关联的连接
	 * 
	 * @param packet
	 */
	public void deliver(Packet packet) {
		if (connection != null && !connection.isClosed()) {
			connection.deliver(packet);
		}
	}

	/**
	 * 将原始文本传送到相关联的连接
	 * 
	 * @param text
	 *            XML string
	 */
	public void deliverRawText(String text) {
		if (connection != null) {
			connection.deliverRawText(text);
		}
	}

	/**
	 * 关闭会话包括相关联的socket连接
	 */
	public void close() {
		if (connection != null) {
			connection.close();
		}
	}

	/**
	 * 连接当前的连接状态
	 * 
	 * @return true：关闭；flase：未关闭
	 */
	public boolean isClosed() {
		return connection.isClosed();
	}

	// public boolean isSecure() {
	// return connection.isSecure();
	// }

	// public boolean validate() {
	// return connection.validate();
	// }

	/**
	 * 返回IP地址
	 * 
	 * @return
	 */
	public String getHostAddress() throws UnknownHostException {
		return connection.getHostAddress();
	}

	/**
	 * 获取IP地址的主机名
	 * 
	 * @return
	 */
	public String getHostName() throws UnknownHostException {
		return connection.getHostName();
	}

	/**
	 * 返回一个可用的流功能的文本
	 */
	public abstract String getAvailableStreamFeatures();

	@Override
	public String toString() {
		return super.toString() + " status: " + status + " address: " + address
				+ " id: " + streamID;
	}

}
