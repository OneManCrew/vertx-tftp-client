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

package io.github.onemancrew.vertx.tftp;

import io.github.onemancrew.vertx.tftp.protocol.DownloadFileProcess;
import io.github.onemancrew.vertx.tftp.protocol.SendFileProcess;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;
import io.github.onemancrew.vertx.tftp.Progress;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Project: vertx-tftp-client
 * File: TftpClient.java
 * Package: io.github.onemancrew.vertx.tftp
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
/**
 * Main TftpClient class to use when sending commands to a TFTP server.
 *
 * @see <a href="https://www.ietf.org/rfc/rfc1350.txt">TFTP RFC</a>
 */
public class TftpClient {
	private static Logger log = LoggerFactory.getLogger(TftpClient.class);
	private final String m_host;
	private final int m_port;
	private Vertx m_vertx;
	private ByteOrder m_byteOrder=ByteOrder.BIG_ENDIAN;

	/**
	 * Create a ftp client which connects to the specified host and port.
	 * @param vertx the vertx instance to use for creating connections.
	 * @param host the host where the TFTP server is running.
	 * @param port the port on the TFTP server.
	 */
	public TftpClient(Vertx vertx, String host, int port) {
		this.m_vertx = vertx;
		this.m_host = host;
		this.m_port = port;
	}

	/**
	 * Perform upload of the file.
	 * @param filePath the file path.
	 * @param progress a progress handler that is called for each part that is recieved.
	 * @param handler callback handler that is called when the list is completed.
	 */
	public void upload(String filePath, Handler<Progress> progress, Handler<AsyncResult<Void>> handler) {
		m_vertx.fileSystem().readFile(filePath,(result)->{
			if(result.succeeded()){
				String fileName= Paths.get(filePath).getFileName().toString();
				SendFileProcess process = new SendFileProcess(m_vertx, m_byteOrder, m_host, m_port, fileName, result.result(), progress, handler);
				process.send(generateRandomPort());
			}
			else
			{
				log.error(result.cause());
				handler.handle(Future.failedFuture(result.cause()));
			}
		});
	}

	/**
	 * Perform Download of  file.
	 * @param fileName the file name.
	 * @param dstFolder the file  destination write path.
	 * @param handler callback handler that is called when the list is completed.
	 */
	public void download(String fileName, String dstFolder, Handler<AsyncResult<Void>> handler) {
		DownloadFileProcess process = new DownloadFileProcess(m_vertx, m_byteOrder, m_host, m_port,dstFolder, fileName, handler);
		process.download(generateRandomPort());
	}
	public static int generateRandomPort() {
		ServerSocket s = null;
		try {
			// ServerSocket(0) results in availability of a free random port
			s = new ServerSocket(0);
			return s.getLocalPort();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			assert s != null;
			try {
				s.close();
			} catch (IOException e) {
				log.error(e);
			}
		}
	}
}
