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
package org.androidpn.server.xmpp.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SSL密钥管理器工厂类
 * 
 * @author lijian
 * @date 2016-12-4 上午12:46:27
 */
public class SSLKeyManagerFactory {

    private static final Log log = LogFactory
            .getLog(SSLKeyManagerFactory.class);

	/**
	 * 获得key管理器集合
	 * 
	 * @param storeType
	 *            存储类型
	 * @param keystore
	 *            key库
	 * @param keypass
	 *            key密钥
	 * @return
	 * @throws NoSuchAlgorithmException
	 *             没有这样的算法异常
	 * @throws KeyStoreException
	 *             密钥库异常
	 * @throws IOException
	 *             IO异常
	 * @throws CertificateException
	 *             证书异常
	 * @throws UnrecoverableKeyException
	 *             不可恢复的Key异常
	 */
    public static KeyManager[] getKeyManagers(String storeType,
            String keystore, String keypass) throws NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateException,
            UnrecoverableKeyException {
        KeyManager[] keyManagers;
        if (keystore == null) {
            keyManagers = null;
        } else {
            if (keypass == null) {
                keypass = "";
            }
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(new FileInputStream(keystore), keypass.toCharArray());

            KeyManagerFactory keyFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, keypass.toCharArray());
            keyManagers = keyFactory.getKeyManagers();
        }
        return keyManagers;
    }

	/**
	 * 获得key管理器集合
	 * 
	 * @param keystore
	 *            key库
	 * @param keypass
	 *            key密钥
	 * @return
	 */
    public static KeyManager[] getKeyManagers(KeyStore keystore, String keypass) {
        KeyManager[] keyManagers;
        try {
            if (keystore == null) {
                keyManagers = null;
            } else {
                KeyManagerFactory keyFactory = KeyManagerFactory
                        .getInstance(KeyManagerFactory.getDefaultAlgorithm());
                if (keypass == null) {
                    keypass = SSLConfig.getKeyPassword();
                }

                keyFactory.init(keystore, keypass.toCharArray());
                keyManagers = keyFactory.getKeyManagers();
            }
        } catch (KeyStoreException e) {
			keyManagers = null;
			log.error("SSLKeyManagerFactory 启动问题.", e);
		} catch (NoSuchAlgorithmException e) {
			keyManagers = null;
			log.error("SSLKeyManagerFactory 启动问题.", e);
		} catch (UnrecoverableKeyException e) {
			keyManagers = null;
			log.error("SSLKeyManagerFactory 启动问题.", e);
		}
        return keyManagers;
    }

}
