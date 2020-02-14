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

import io.github.onemancrew.vertx.tftp.protocol.enums.Opcode;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownOpcodeException;
import java.nio.ByteBuffer;

/**
 * Project: vertx-tftp-client
 * File: DataMessage.java
 * Package: io.github.onemancrew.vertx.tftp.message
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public class DataMessage implements IBufferProtocol {
	public static final int BLOCK_SIZE = 512;
	public static final int BLOCK_ID_SIZE = 2;

	private Opcode m_opcode;
	private int m_blockId;
	private byte[] m_data;

	public DataMessage() {
		m_opcode = Opcode.DATA;
		m_blockId = -1;
		m_data = new byte[0];
	}
	public DataMessage(int blockId, byte[] data) {
		m_opcode = Opcode.DATA;
		m_blockId = blockId;
		m_data = data;
	}

	public Opcode getOpcode() {
		return m_opcode;
	}

	public int getBlockId() {
		return m_blockId;
	}

	public byte[] getData() {
		return m_data;
	}

	@Override
	public void toBuffer(ByteBuffer buffer) {
		buffer.putShort(m_opcode.getValue());
		buffer.putShort((short) m_blockId);
		buffer.put(m_data);
	}


	@Override
	public void fromBuffer(ByteBuffer buffer) throws UnknownOpcodeException {
		m_opcode = Opcode.ValueOf(buffer.getShort());
		m_blockId = Short.toUnsignedInt(buffer.getShort());
		m_data=new byte[buffer.remaining()];
		buffer.get(m_data);
	}

	public boolean isLast() {
		return m_data.length<BLOCK_SIZE;
	}

	@Override
	public int getBufferSize() {
		return Opcode.getSizeInBytes()+ BLOCK_ID_SIZE+m_data.length;
	}
}
