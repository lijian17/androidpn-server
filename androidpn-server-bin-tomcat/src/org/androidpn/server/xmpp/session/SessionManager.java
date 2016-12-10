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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.androidpn.server.xmpp.XmppServer;
import org.androidpn.server.xmpp.net.Connection;
import org.androidpn.server.xmpp.net.ConnectionCloseListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.util.ConcurrentHashSet;
import org.xmpp.packet.JID;

/**
 * 连接到服务器的会话管理器
 * 
 * @author lijian
 * @date 2016-12-4 上午12:41:12
 */
public class SessionManager {

	private static final Log log = LogFactory.getLog(SessionManager.class);

	/** 资源名，注意与Android客户端设置的要一致 */
	private static final String RESOURCE_NAME = "AndroidpnClient";

	private static SessionManager instance;

	private String serverName;

	/** 待认证会话集 */
	private Map<String, ClientSession> preAuthSessions = new ConcurrentHashMap<String, ClientSession>();

	/** 客户端会话集 */
	private Map<String, ClientSession> clientSessions = new ConcurrentHashMap<String, ClientSession>();

	/** 用户别名 */
	private Map<String, String> aliasUsernameMap = new ConcurrentHashMap<String, String>();

	/** 标签用户名集合 */
	private Map<String, ConcurrentHashSet<String>> tagUsernamesMap = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();

	/** 用户会话计数器 */
	private final AtomicInteger connectionsCounter = new AtomicInteger(0);

	private ClientSessionListener clientSessionListener = new ClientSessionListener();

	private SessionManager() {
		serverName = XmppServer.getInstance().getServerName();
	}

	/**
	 * 获得会话管理器单例
	 * 
	 * @return
	 */
	public static SessionManager getInstance() {
		if (instance == null) {
			synchronized (SessionManager.class) {
				instance = new SessionManager();
			}
		}
		return instance;
	}

	/**
	 * 根据连接创建一个新的客户端会话并返回
	 * 
	 * @param conn
	 * @return
	 */
	public ClientSession createClientSession(Connection conn) {
		if (serverName == null) {
			throw new IllegalStateException("服务未初始化");
		}

		Random random = new Random();
		String streamId = Integer.toHexString(random.nextInt());

		ClientSession session = new ClientSession(serverName, conn, streamId);
		conn.init(session);
		conn.registerCloseListener(clientSessionListener);

		// 添加到预认证会话集中
		preAuthSessions.put(session.getAddress().getResource(), session);

		// 用户会话计数器执行自增
		connectionsCounter.incrementAndGet();

		log.debug("一个客户端会话创建完成.");
		return session;
	}

	/**
	 * 添加一个已被认证的新会话
	 * 
	 * @param session
	 */
	public void addSession(ClientSession session) {
		preAuthSessions.remove(session.getStreamID().toString());
		clientSessions.put(session.getAddress().toString(), session);
	}

	/**
	 * 根据用户名返回该用户的会话
	 * 
	 * @param username
	 *            客户端地址的用户名
	 * @return
	 */
	public ClientSession getSession(String username) {
		// return getSession(new JID(username, serverName, null, true));
		return getSession(new JID(username, serverName, RESOURCE_NAME, true));
	}

	/**
	 * 根据JID返回与之关联的会话
	 * 
	 * @param from
	 * @return
	 */
	public ClientSession getSession(JID from) {
		if (from == null || serverName == null
				|| !serverName.equals(from.getDomain())) {
			return null;
		}
		// 检查预认证会话
		if (from.getResource() != null) {
			ClientSession session = preAuthSessions.get(from.getResource());
			if (session != null) {
				return session;
			}
		}
		if (from.getResource() == null || from.getNode() == null) {
			return null;
		}
		return clientSessions.get(from.toString());
	}

	/**
	 * 获得一个包含所有已认证的客户端会话列表
	 * 
	 * @return
	 */
	public Collection<ClientSession> getSessions() {
		return clientSessions.values();
	}

	/**
	 * 移除一个客户端会话
	 * 
	 * @param session
	 * @return true：会话成功删除；false：删除失败
	 */
	public boolean removeSession(ClientSession session) {
		if (session == null || serverName == null) {
			return false;
		}
		JID fullJID = session.getAddress();

		// 从列表中删除会话
		boolean clientRemoved = clientSessions.remove(fullJID.toString()) != null;
		boolean preAuthRemoved = (preAuthSessions.remove(fullJID.getResource()) != null);

		// 用户会话的计数器执行自减
		if (clientRemoved || preAuthRemoved) {
			connectionsCounter.decrementAndGet();
			return true;
		}
		return false;
	}

	/**
	 * 关闭所有会话
	 */
	public void closeAllSessions() {
		try {
			// 发送关闭流到所有连接
			Set<ClientSession> sessions = new HashSet<ClientSession>();
			sessions.addAll(preAuthSessions.values());
			sessions.addAll(clientSessions.values());

			for (ClientSession session : sessions) {
				try {
					session.getConnection().systemShutdown();
				} catch (Throwable t) {
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 会话关闭监听器
	 * 
	 * @author lijian
	 * @date 2016-7-30 下午4:42:59
	 */
	private class ClientSessionListener implements ConnectionCloseListener {

		public void onConnectionClose(Object handback) {
			try {
				ClientSession session = (ClientSession) handback;
				removeSession(session);
			} catch (Exception e) {
				log.error("无法关闭socket", e);
			}
		}
	}

	/**
	 * 设置别名
	 * 
	 * @param username
	 *            用户名
	 * @param alias
	 *            别名
	 */
	public void setUserAlias(String username, String alias) {
		aliasUsernameMap.put(alias, username);
	}

	/**
	 * 根据别名获取用户名
	 * 
	 * @param alias
	 *            别名
	 * @return 用户名
	 */
	public String getUsernameByAlias(String alias) {
		String username = aliasUsernameMap.get(alias);
		return username;
	}

	/**
	 * 设置用户标签
	 * 
	 * @param username
	 *            用户名
	 * @param tag
	 *            该用户所关注的标签
	 */
	public void setUserTag(String username, String tag) {
		ConcurrentHashSet<String> hashSet = tagUsernamesMap.get(tag);
		if (hashSet == null) {
			ConcurrentHashSet<String> set = new ConcurrentHashSet<String>();
			set.add(username);
			tagUsernamesMap.put(tag, set);
		} else {
			// set集合有自动去重属性，不用担心用户名重名问题
			hashSet.add(username);
		}
	}

	/**
	 * 根据标签获取用户集合
	 * 
	 * @param tag
	 *            标签
	 * @return 用户集合
	 */
	public Set<String> getUsernameByTag(String tag) {
		return tagUsernamesMap.get(tag);
	}
}
