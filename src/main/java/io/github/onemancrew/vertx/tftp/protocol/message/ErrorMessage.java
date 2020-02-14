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

package io.github.onemancrew.vertx.tftp.protocol.message;

import io.github.onemancrew.vertx.tftp.protocol.enums.ErrorCode;
import io.github.onemancrew.vertx.tftp.protocol.enums.Opcode;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownErrorException;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownOpcodeException;
import java.nio.ByteBuffer;

/**
 * Project: vertx-tftp-client
 * File: ErrorMessage.java
 * Package: io.github.onemancrew.vertx.tftp.message
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public class ErrorMessage  implements IBufferProtocol{
	public static final int ERROR_CODE_SIZE = 2;
	public static final int ZERO_BYTE = 1;

	private Opcode m_opcode;
	private ErrorCode m_errorCode;
	private String m_errorMessage;

	public ErrorMessage(ErrorCode errorCode) {
		m_opcode=Opcode.ERROR;
		m_errorCode = errorCode;
		m_errorMessage=m_errorCode.getDescription();
	}

	public Opcode getOpcode() {
		return m_opcode;
	}

	public ErrorCode getErrorCode() {
		return m_errorCode;
	}

	public String getErrorMessage() {
		return m_errorMessage;
	}
	@Override
	public void toBuffer(ByteBuffer buffer) {
		buffer.putShort(m_opcode.getValue());
		buffer.putShort(m_errorCode.getError());
		buffer.put(m_errorMessage.getBytes());
		buffer.put((byte)0);
	}

	@Override
	public void fromBuffer(ByteBuffer buffer) throws UnknownOpcodeException, UnknownErrorException {
		m_opcode = Opcode.ValueOf(buffer.getShort());
		m_errorCode = ErrorCode.ValueOf(buffer.getShort());
		byte[] tmp=new byte[buffer.remaining()-1];
		buffer.get(tmp);
		m_errorMessage=new String(tmp);
		buffer.get();
	}

	@Override
	public int getBufferSize() {
		return Opcode.getSizeInBytes()+ ERROR_CODE_SIZE +m_errorMessage.length()+ ZERO_BYTE;
	}
}
