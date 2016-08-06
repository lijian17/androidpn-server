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

/** 
 * Runtime exceptions produced by failed packet operations.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
/**
 * 运行时异常-由失败的数据包操作
 * 
 * @author lijian
 * @date 2016-8-6 下午1:13:55
 */
public class PacketException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PacketException() {
		super();
	}

	public PacketException(String message) {
		super(message);
	}

	public PacketException(Throwable cause) {
		super(cause);
	}

	public PacketException(String message, Throwable cause) {
		super(message, cause);
	}

}
