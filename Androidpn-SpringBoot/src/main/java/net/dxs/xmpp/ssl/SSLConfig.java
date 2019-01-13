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
package net.dxs.xmpp.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.dxs.utils.Config;

/**
 * SSL配置类
 * 
 * @author lijian
 * @date 2016-12-4 上午12:42:11
 */
public class SSLConfig {

    private static final Log log = LogFactory.getLog(SSLConfig.class);

	/** SSL上下文 */
	private static SSLContext sslContext;

	/** 存储类型 */
	private static String storeType;

	/** 存储key */
	private static KeyStore keyStore;

	/** 存储key位置 */
	private static String keyStoreLocation;

	/** key密码 */
	private static String keyPass;

	/** 信任存储区 */
	private static KeyStore trustStore;

	/** 信任存储区位置 */
	private static String trustStoreLocation;

	/** 信任存储区密钥 */
	private static String trustPass;
    
    private static URL classPath;

    private SSLConfig() {
    }

    static {
		// 加载配置文件信息（见：config.properties）
        storeType = Config.getString("xmpp.ssl.storeType", "JKS");
        keyStoreLocation = Config.getString("xmpp.ssl.keystore", "conf"
                + File.separator + "security" + File.separator + "keystore");
        keyStoreLocation = classPath.getPath() + File.separator
                + keyStoreLocation;
        keyPass = Config.getString("xmpp.ssl.keypass", "changeit");
        trustStoreLocation = Config.getString("xmpp.ssl.truststore", "conf"
                + File.separator + "security" + File.separator + "truststore");
        trustStoreLocation = classPath.getPath()
                + File.separator + trustStoreLocation;
        trustPass = Config.getString("xmpp.ssl.trustpass", "changeit");
        
        classPath = SSLConfig.class.getResource("/");

        log.debug("keyStoreLocation=" + keyStoreLocation);
        log.debug("trustStoreLocation=" + trustStoreLocation);

        // Load keystore
        try {
            keyStore = KeyStore.getInstance(storeType);
            keyStore.load(new FileInputStream(keyStoreLocation), keyPass
                    .toCharArray());
        } catch (Exception e) {
			log.error("SSLConfig 启动发生错误.\n" + "  storeType: [" + storeType
					+ "]\n" + "  keyStoreLocation: [" + keyStoreLocation
					+ "]\n" + "  keyPass: [" + keyPass + "]", e);
            keyStore = null;
        }

		// 加载信任库 truststore
        try {
            trustStore = KeyStore.getInstance(storeType);
            trustStore.load(new FileInputStream(trustStoreLocation), trustPass
                    .toCharArray());

        } catch (Exception e) {
            try {
                trustStore = KeyStore.getInstance(storeType);
                trustStore.load(null, trustPass.toCharArray());
            } catch (Exception ex) {
				log.error("SSLConfig 启动发生错误.\n" + "  storeType: [" + storeType
						+ "]\n" + "  trustStoreLocation: ["
						+ trustStoreLocation + "]\n" + "  trustPass: ["
						+ trustPass + "]", e);
                trustStore = null;
            }
        }

		// 初始化工厂      
        try {
            sslContext = SSLContext.getInstance("TLS");

            KeyManagerFactory keyFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, SSLConfig.getKeyPassword().toCharArray());

            TrustManagerFactory c2sTrustFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            c2sTrustFactory.init(trustStore);

            sslContext.init(keyFactory.getKeyManagers(), c2sTrustFactory
                    .getTrustManagers(), new java.security.SecureRandom());

        } catch (Exception e) {
			log.error("SSLConfig 工厂启动发生错误." + "  storeType: [" + storeType
					+ "]\n" + "  keyStoreLocation: [" + keyStoreLocation
					+ "]\n" + "  keyPass: [" + keyPass + "]\n"
					+ "  trustStoreLocation: [" + trustStoreLocation + "]\n"
					+ "  trustPass: [" + trustPass + "]", e);
            keyStore = null;
            trustStore = null;
        }
    }

	/**
	 * 获得SSLContext.
	 * 
	 * @return SSL上下文
	 */
    public static SSLContext getc2sSSLContext() {
        return sslContext;
    }

	/**
	 * 获取密钥存储位置
	 * 
	 * @return 密钥库的位置
	 */
    public static String getKeystoreLocation() {
        return keyStoreLocation;
    }

	/**
	 * 获得信任的存储位置
	 * 
	 * @return 信任存储位置
	 */
    public static String getc2sTruststoreLocation() {
        return trustStoreLocation;
    }

	/**
	 * 获取存储类型
	 * 
	 * @return
	 */
    public static String getStoreType() {
        return storeType;
    }

	/**
	 * 获取密钥存储区
	 * 
	 * @return
	 */
    public static KeyStore getKeyStore() throws IOException {
        if (keyStore == null) {
            throw new IOException();
        }
        return keyStore;
    }

	/**
	 * 获取密钥存储密码
	 * 
	 * @return
	 */
    public static String getKeyPassword() {
        return keyPass;
    }

	/**
	 * 获得信任存储
	 * 
	 * @return
	 */
    public static KeyStore getc2sTrustStore() throws IOException {
        if (trustStore == null) {
            throw new IOException();
        }
        return trustStore;
    }

	/**
	 * 返回信任存储密码
	 * 
	 * @return
	 */
    public static String getc2sTrustPassword() {
        return trustPass;
    }

}
