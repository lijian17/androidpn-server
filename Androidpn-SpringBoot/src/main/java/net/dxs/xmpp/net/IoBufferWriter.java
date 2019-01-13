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
import java.io.Writer;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * 包装类-包装在 MINA{@link IoBuffer} 继承自Writer
 * 
 * @author lijian
 * @date 2016-12-4 上午12:22:53
 */
public class IoBufferWriter extends Writer {

	/** 字符集编码器 */
	private CharsetEncoder encoder;

	/** IO缓冲区 */
	private IoBuffer ioBuffer;

	/**
	 * 包装类-包装在 MINA{@link IoBuffer} 继承自Writer.
	 * 
	 * @param ioBuffer
	 *            IO缓冲区
	 * @param encoder
	 *            字符集编码器
	 */
	public IoBufferWriter(IoBuffer ioBuffer, CharsetEncoder encoder) {
		this.encoder = encoder;
		this.ioBuffer = ioBuffer;
	}

	/**
	 * 写入一个字符数组的一部分
	 * 
	 * @param cbuf
	 *            字符数组
	 * @param off
	 *            开始位置
	 * @param len
	 *            写入长度
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		ioBuffer.putString(new String(cbuf, off, len), encoder);
	}

	/**
	 * 刷新流
	 */
	@Override
	public void flush() throws IOException {
		// Ignore
	}

	/**
	 * 关闭流，并刷新
	 */
	@Override
	public void close() throws IOException {
		// Ignore
	}

}
