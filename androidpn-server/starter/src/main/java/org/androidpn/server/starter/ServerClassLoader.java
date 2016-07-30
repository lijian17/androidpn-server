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
package org.androidpn.server.starter;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 这是一个简单的类装载器，装载lib目录下所有的jar
 * 
 * @author lijian
 * @date 2016-7-30 下午2:22:54
 */
public class ServerClassLoader extends URLClassLoader {

	/**
	 * 构造函数
	 * 
	 * @param parent
	 *            父类装载器（可为null）
	 * @param confDir
	 *            加载配置文件目录
	 * @param libDir
	 *            加载jar文件目录
	 * @throws MalformedURLException
	 *             如果libDir路径无效，则抛异常
	 */
	public ServerClassLoader(ClassLoader parent, File confDir, File libDir)
			throws MalformedURLException {
		super(new URL[] { confDir.toURI().toURL(), libDir.toURI().toURL() },
				parent);

		File[] jars = libDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				boolean accept = false;
				String smallName = name.toLowerCase();
				if (smallName.endsWith(".jar")) {
					accept = true;
				} else if (smallName.endsWith(".zip")) {
					accept = true;
				}
				return accept;
			}
		});

		if (jars == null) {
			return;
		}

		for (int i = 0; i < jars.length; i++) {
			if (jars[i].isFile()) {
				addURL(jars[i].toURI().toURL());
			}
		}
	}

}
