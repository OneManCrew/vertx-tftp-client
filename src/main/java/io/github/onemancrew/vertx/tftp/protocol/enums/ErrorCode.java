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

import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownErrorException;

/**
 * Project: vertx-tftp-client
 * File: ErrorCode.java
 * Package: io.github.onemancrew.vertx.tftp.enums
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public enum ErrorCode {
	NO_ERROR((short)0,"No error."),
	FILE_NOT_FOUND((short)1,"File not found."),
	ACCESS_VIOLATION((short)2,"Access violation."),
	DISK_FULL((short)3,"Disk full or allocation exceeded."),
	ILLEGAL_TFTP_OPERATION((short)4,"Illegal TFTP operation."),
	UNKNOWN_TRANSFER_ID((short)5,"Unknown transfer ID."),
	FILE_ALREADY_EXISTS((short)6,"File already exists."),
	NO_SUCH_USER((short)7,"No such user.");

	private final short error;
	private final String description;

	ErrorCode(short error, String description) {
		this.error = error;
		this.description = description;
	}

	public short getError() {
		return error;
	}

	public String getDescription() {
		return description;
	}
	public static ErrorCode ValueOf(short value) throws UnknownErrorException {
		if (value >7 || value <0)
			throw new UnknownErrorException(value);
		switch (value) {
			case 1: return ErrorCode.FILE_NOT_FOUND;
			case 2: return ErrorCode.ACCESS_VIOLATION;
			case 3: return ErrorCode.DISK_FULL;
			case 4: return ErrorCode.ILLEGAL_TFTP_OPERATION;
			case 5: return ErrorCode.UNKNOWN_TRANSFER_ID;
			case 6: return ErrorCode.FILE_ALREADY_EXISTS;
			case 7: return ErrorCode.NO_SUCH_USER;
		}
		return ErrorCode.NO_ERROR;

	}
}
