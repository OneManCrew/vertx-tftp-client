/*
 * MIT License
 *
 * Copyright (c) 2020 OneManCrew
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.onemancrew.vertx.tftp.protocol.message.request;

import io.github.onemancrew.vertx.tftp.protocol.enums.Opcode;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownErrorException;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownOpcodeException;
import io.github.onemancrew.vertx.tftp.protocol.message.IBufferProtocol;
import java.nio.ByteBuffer;

/**
 * Project: vertx-tftp-client
 * File: RequestMessage.java
 * Package: io.github.onemancrew.vertx.tftp.message
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public  abstract class RequestMessage  implements IBufferProtocol {
	public static final int ZERO_BYTE = 1;

	protected Opcode m_opcode;
	private String m_filename;
	private String m_mode;

	public RequestMessage(String filename) {
		m_filename = filename;
		m_mode = "octet";
	}
	public RequestMessage(String filename, String mode) {
		m_filename = filename;
		m_mode = mode;
	}

	public Opcode getOpcode() {
		return m_opcode;
	}

	public String getFilename() {
		return m_filename;
	}

	public String getMode() {
		return m_mode;
	}

	@Override
	public void toBuffer(ByteBuffer buffer) {
		buffer.putShort(m_opcode.getValue());
		buffer.put(m_filename.getBytes());
		buffer.put((byte)0);
		buffer.put(m_mode.getBytes());
		buffer.put((byte)0);

	}

	@Override
	public void fromBuffer(ByteBuffer buffer) throws UnknownOpcodeException, UnknownErrorException {
		m_opcode = Opcode.ValueOf(buffer.get());
		//TBD.......
	}

	@Override
	public int getBufferSize() {
		return Opcode.getSizeInBytes()+ m_filename.length() + ZERO_BYTE+m_mode.length()+ ZERO_BYTE;
	}

}
