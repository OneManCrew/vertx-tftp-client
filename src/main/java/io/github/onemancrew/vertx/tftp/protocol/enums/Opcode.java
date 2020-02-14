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

package io.github.onemancrew.vertx.tftp.protocol.enums;

import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownOpcodeException;

/**
 * Project: vertx-tftp-client
 * File: Opcodes.java
 * Package: io.github.onemancrew.vertx.tftp.enums
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public enum Opcode {
	RRQ((short)1),	// Read Request (RRQ)
	WRQ((short)2),	// Write Request (WRQ)
	DATA((short)3),	// Date (DATA)
	ACK((short)4),	// Acknowledgment (ACK)
	ERROR((short)5);	// Error (ERROR)


	private final short value;

	Opcode(short opcode) {
		this.value = opcode;
	}

	public short getValue() {
		return value;
	}
	public static Opcode ValueOf(short value) throws UnknownOpcodeException {
		if (value >5 || value <1)
			throw new UnknownOpcodeException(value);
		switch (value) {
			case 1: return Opcode.RRQ;
			case 2: return Opcode.WRQ;
			case 3: return Opcode.DATA;
			case 4: return Opcode.ACK;
			case 5: return Opcode.ERROR;
		}
		return null;
	}
	public static int getSizeInBytes(){
		return 2;
	}
}
