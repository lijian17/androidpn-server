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
package org.androidpn.server.container;

import java.io.File;

import org.androidpn.server.util.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * 在配置的端口上启动一个实例，并加载admin控制台Web application
 * 
 * @author lijian
 * @date 2016-7-30 下午2:59:50
 */
public class AdminConsole {

	private static final Log log = LogFactory.getLog(AdminConsole.class);

	/** admin主机 */
	private String adminHost;

	/** admin端口 */
	private int adminPort;

	/** admin服务 */
	private Server adminServer;

	/** 上下文处理控制器 */
	private ContextHandlerCollection contexts;

	private boolean httpStarted = false;

	/**
	 * 创建一个Jetty模块
	 * 
	 * @param homeDir
	 *            application主目录
	 */
	public AdminConsole(String homeDir) {
		contexts = new ContextHandlerCollection();
		Context context = new WebAppContext(contexts, homeDir + File.separator
				+ "console", "/");
		context.setWelcomeFiles(new String[] { "index.jsp" });

		adminHost = Config.getString("admin.console.host", "127.0.0.1");
		adminPort = Config.getInt("admin.console.port", 8080);
		adminServer = new Server();
		adminServer.setSendServerVersion(false);
	}

	/**
	 * 启动Jetty服务器实例
	 */
	public void startup() {
		if (adminPort > 0) {
			Connector httpConnector = new SelectChannelConnector();
			httpConnector.setHost(adminHost);
			httpConnector.setPort(adminPort);
			adminServer.addConnector(httpConnector);
		}

		if (adminServer.getConnectors() == null
				|| adminServer.getConnectors().length == 0) {
			adminServer = null;
			log.warn("由于配置错误，服务启动Admin console.");
			return;
		}

		adminServer
				.setHandlers(new Handler[] { contexts, new DefaultHandler() });

		try {
			adminServer.start();
			httpStarted = true;
			log.debug("Admin console 启动成功.");
		} catch (Exception e) {
			log.error("服务启动admin conosle 服务 ", e);
		}
	}

	/**
	 * 关闭Jetty服务
	 */
	public void shutdown() {
		try {
			if (adminServer != null && adminServer.isRunning()) {
				adminServer.stop();
			}
		} catch (Exception e) {
			log.error("关闭admin console server 的时候发生错误", e);
		}
		adminServer = null;
	}

	/**
	 * 重启Jetty服务
	 */
	public void restart() {
		try {
			adminServer.stop();
			adminServer.start();
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * 获得admin console的Jetty上下文句柄
	 * 
	 * @return Jetty上下文句柄
	 */
	public ContextHandlerCollection getContexts() {
		return contexts;
	}

	/**
	 * 获取admin console的主机名
	 * 
	 * @return
	 */
	public String getAdminHost() {
		return adminHost;
	}

	/**
	 * 获得admin console端口
	 * 
	 * @return
	 */
	public int getAdminPort() {
		return adminPort;
	}

	/**
	 * 获取admin console的启动状态
	 * 
	 * @return true：已启动；false：未启动
	 */
	public boolean isHttpStarted() {
		return httpStarted;
	}

}
