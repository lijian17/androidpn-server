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
package org.androidpn.server.xmpp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 使用Spring配置启动服务作为独立应用程序。
 * 
 * @author lijian
 * @date 2016-7-30 下午1:52:47
 */
public class XmppServer {

	private static final Log log = LogFactory.getLog(XmppServer.class);

	private static XmppServer instance;

	/** Spring上下文对象 */
	private ApplicationContext context;

	/** Androidpn Server 版本号 */
	private String version = "0.5.0";

	private String serverName;

	private String serverHomeDir;

	private boolean shuttingDown;

	/**
	 * 获得XmppServer实例（单例模式）
	 * 
	 * @return
	 */
	public static XmppServer getInstance() {
		if (instance == null) {
			synchronized (XmppServer.class) {
				instance = new XmppServer();
			}
		}
		return instance;
	}

	/**
	 * 构造函数：创建一个服务并启动他
	 */
	public XmppServer() {
		if (instance != null) {
			throw new IllegalStateException("服务已经是运行的");
		}
		instance = this;
		start();
	}

	/**
	 * 使用Spring配置启动服务
	 */
	public void start() {
		try {
			if (isStandAlone()) {
				Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
			}

//			locateServer();
			serverName = Config.getString("xmpp.domain", "127.0.0.1")
					.toLowerCase();
			context = new ClassPathXmlApplicationContext("spring-config.xml");
			log.info("加载Spring配置.");

//			AdminConsole adminConsole = new AdminConsole(serverHomeDir);
//			adminConsole.startup();
//			if (adminConsole.isHttpStarted()) {
//				log.info("Admin console listening at http://"
//						+ adminConsole.getAdminHost() + ":"
//						+ adminConsole.getAdminPort());
//			}
			log.info("XmppServer started: " + serverName);
			log.info("Androidpn Server v" + version);

		} catch (Exception e) {
			e.printStackTrace();
			shutdownServer();
		}
	}

	/**
	 * 停止服务
	 */
	public void stop() {
		shutdownServer();
		Thread shutdownThread = new ShutdownThread();
		shutdownThread.setDaemon(true);
		shutdownThread.start();
	}

	/**
	 * 根据beanName获取一个Spring 的bean
	 * 
	 * @param beanName
	 * @return a Srping bean
	 */
	public Object getBean(String beanName) {
		return context.getBean(beanName);
	}

	/**
	 * 获得server name.
	 * 
	 * @return
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * 服务器当前状态
	 * 
	 * @return true：服务当前正在关闭，false：otherwise
	 */
	public boolean isShuttingDown() {
		return shuttingDown;
	}

	/**
	 * 服务是否是单例模式运行
	 * 
	 * @return true：单例模式；false：非单例模式
	 */
	public boolean isStandAlone() {
		boolean standalone;
		try {
			standalone = Class
					.forName("org.androidpn.server.starter.ServerStarter") != null;
		} catch (ClassNotFoundException e) {
			standalone = false;
		}
		return standalone;
	}

	/**
	 * 定位服务
	 * 
	 * @throws FileNotFoundException
	 */
	private void locateServer() throws FileNotFoundException {
		String baseDir = System.getProperty("base.dir", "..");
		log.debug("base.dir=" + baseDir);

		if (serverHomeDir == null) {
			try {
				File confDir = new File(baseDir, "conf");
				if (confDir.exists()) {
					serverHomeDir = confDir.getParentFile().getCanonicalPath();
				}
			} catch (FileNotFoundException fe) {
				// Ignore
			} catch (IOException ie) {
				// Ignore
			}
		}

		if (serverHomeDir == null) {
			System.err.println("Could not locate home.");
			throw new FileNotFoundException();
		} else {
			Config.setProperty("server.home.dir", serverHomeDir);
			log.debug("server.home.dir=" + serverHomeDir);
		}
	}

	/**
	 * 关闭服务器
	 */
	private void shutdownServer() {
		shuttingDown = true;
		// 关闭所有连接
		SessionManager.getInstance().closeAllSessions();
		log.info("XmppServer stopped");
	}

	private class ShutdownHookThread extends Thread {
		public void run() {
			shutdownServer();
			log.info("Server 停止");
			System.err.println("Server 停止");
		}
	}

	/**
	 * 关闭线程
	 * 
	 * @author lijian
	 * @date 2016-7-30 下午3:27:56
	 */
	private class ShutdownThread extends Thread {
		public void run() {
			try {
				Thread.sleep(5000);
				System.exit(0);// 0正常关闭当前运行的Java虚拟机
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

}
