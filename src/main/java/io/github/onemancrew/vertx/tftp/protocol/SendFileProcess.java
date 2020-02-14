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

package io.github.onemancrew.vertx.tftp.protocol;

import io.github.onemancrew.vertx.tftp.Progress;
import io.github.onemancrew.vertx.tftp.protocol.enums.ErrorCode;
import io.github.onemancrew.vertx.tftp.protocol.enums.Opcode;
import io.github.onemancrew.vertx.tftp.protocol.exception.TftpError;
import io.github.onemancrew.vertx.tftp.protocol.exception.TimeoutException;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownErrorException;
import io.github.onemancrew.vertx.tftp.protocol.exception.UnknownOpcodeException;
import io.github.onemancrew.vertx.tftp.protocol.message.AckMessage;
import io.github.onemancrew.vertx.tftp.protocol.message.DataMessage;
import io.github.onemancrew.vertx.tftp.protocol.message.ErrorMessage;
import io.github.onemancrew.vertx.tftp.protocol.message.IBufferProtocol;
import io.github.onemancrew.vertx.tftp.protocol.message.request.WriteRequestMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Project: vertx-tftp-client
 * File: SendFileProcess.java
 * Package: io.github.onemancrew.vertx.tftp.protocol
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public class SendFileProcess {
	public static final int TIMEOUT_MS = 3000;
	private static final int MAX_RETRIES =3 ;
	private static Logger log = LoggerFactory.getLogger(SendFileProcess.class);
	private final String m_host;
	private int m_port;
	private final String m_fileName;
	private final ByteBuffer m_buffer;
	private final Handler<Progress> m_progressHandler;
	private final Handler<AsyncResult<Void>> m_resultHandler;
	private final int m_totalBlocks;
	private Vertx m_vertx;
	private ByteOrder m_byteOrder= ByteOrder.nativeOrder();
	private DatagramSocket m_socket;
	private SendStatus m_sendStatus=SendStatus.INIT;
	private int m_currBlock=0;
	private IBufferProtocol m_lastMsg;
	private long m_lastSendTimeMs;
	private int m_retries=0;
	private long m_timerID;


	public SendFileProcess(Vertx vertx, ByteOrder byteOrder, String hostDst, int portDst, String fileName,
						   Buffer buffer, Handler<Progress> progress, Handler<AsyncResult<Void>> handler) {
		m_host = hostDst;
		m_port = portDst;
		m_vertx = vertx;
		m_byteOrder=byteOrder;
		m_fileName=fileName;
		m_buffer=ByteBuffer.wrap(buffer.getBytes());
		m_buffer.order(m_byteOrder);
		m_progressHandler=progress;
		m_resultHandler=handler;
		m_totalBlocks=(buffer.length()/DataMessage.BLOCK_SIZE)+1;

	}
	public void send(int port){
		m_socket = m_vertx.createDatagramSocket(new DatagramSocketOptions().setReuseAddress(true));
		m_timerID = m_vertx.setPeriodic(500, new Handler<Long>() {

			@Override
			public void handle(Long aLong) {
				checkLastMsg();
			}
		});
		m_socket.listen(port, "0.0.0.0", asyncResult -> {
			if (asyncResult.succeeded()) {
				startSendFile();
				m_socket.handler(packet -> {
					handleMsg(packet);
				});
			} else {
				handleError(asyncResult.cause());
			}
		});
	}
	private void updateLastMsg(IBufferProtocol msg) {
		m_lastMsg = msg;
		m_lastSendTimeMs=System.currentTimeMillis();
		m_retries=0;
	}

	private void checkLastMsg() {
		if(m_lastMsg == null)
			return;
		long currTime = System.currentTimeMillis();
		if(currTime-m_lastSendTimeMs> TIMEOUT_MS )
		{
			if (m_retries<MAX_RETRIES){
				IBufferProtocol msg = m_lastMsg;
				ByteBuffer buf=ByteBuffer.allocate(msg.getBufferSize());
				m_socket.send(Buffer.buffer(buf.array()),m_port,m_host,datagramSocketAsyncResult -> {
					if(datagramSocketAsyncResult.failed())
						handleError(datagramSocketAsyncResult.cause());
					else {
						m_lastSendTimeMs=currTime;
						m_retries++;
						log.info("Resend last msg of file "+m_fileName+" to "+m_host);
					}

				});
			}
			else {
				handleError(new TimeoutException());
			}

		}
	}

	private void startSendFile() {
		log.info("start Send File "+m_fileName+" to "+m_host);
		WriteRequestMessage msg=new WriteRequestMessage(m_fileName);
		ByteBuffer buf=ByteBuffer.allocate(msg.getBufferSize());
		buf.order(m_byteOrder);
		msg.toBuffer(buf);

		m_socket.send(Buffer.buffer(buf.array()),m_port,m_host,datagramSocketAsyncResult -> {
			if(datagramSocketAsyncResult.failed())
				handleError(datagramSocketAsyncResult.cause());
			else {
				updateLastMsg(msg);
				m_sendStatus = SendStatus.SEND_REQUEST;
				log.info("Send Write request(WRQ) of file "+m_fileName+" to "+m_host);
			}

		});

	}

	private void handleError(Throwable cause) {
		log.error("handleError: "+cause.getMessage());
		close(Future.failedFuture(cause));
	}

	private void handleMsg(DatagramPacket packet) {
		m_port=packet.sender().port();
		byte[] bytes = packet.data().getBytes();
		try {
			Opcode opcode = Opcode.ValueOf(bytes[1]);
			log.info("handle incoming OpCode:"+opcode);
			switch (opcode) {
				case ACK:
					handleACK(bytes);
					break;
				case DATA:
				case ERROR:
				case RRQ:
				case WRQ:
					handleERRMsg(opcode,bytes);
					break;


			}
		} catch (UnknownOpcodeException e) {
			handleError(e);
		}

	}

	private void handleERRMsg(Opcode opcode, byte[] bytes) {
		if (opcode == Opcode.ERROR) {
			ByteBuffer buf=ByteBuffer.wrap(bytes);
			buf.order(m_byteOrder);
			ErrorMessage msg=new ErrorMessage(ErrorCode.NO_ERROR);
			try {
				msg.fromBuffer(buf);
				handleError(new TftpError(msg));
			} catch (UnknownOpcodeException e) {
				handleError(e);
			} catch (UnknownErrorException e) {
				handleError(e);
			}


		}
	}

	private void handleACK(byte[] bytes) throws UnknownOpcodeException {
		ByteBuffer buf=ByteBuffer.wrap(bytes);
		buf.order(m_byteOrder);
		AckMessage msg=new AckMessage();
		msg.fromBuffer(buf);
		if(m_sendStatus==SendStatus.SEND_REQUEST && msg.getBlockId()==0){
			m_sendStatus=SendStatus.SENDING_FILE;
			log.info("receive(file: "+m_fileName+" host:"+m_host+") ACK "+msg.getBlockId()+"/"+m_totalBlocks);

			sendNextBlock();
		}
		else if(m_sendStatus==SendStatus.SENDING_FILE && msg.getBlockId()==m_currBlock){
			m_progressHandler.handle(new Progress(this,m_currBlock/m_totalBlocks));
			if(m_currBlock==m_totalBlocks){
				m_sendStatus=SendStatus.END;
				log.info("m_sendStatus=SendStatus.END");
				close(Future.succeededFuture());
//				m_resultHandler.handle(Future.succeededFuture());
			}
			else
				sendNextBlock();
		}



	}

	private void sendNextBlock() {
		int blockId=m_currBlock+1;
		byte[] data;
		if(m_buffer.remaining()>DataMessage.BLOCK_SIZE)
			data=new byte[DataMessage.BLOCK_SIZE];
		else
			data=new byte[m_buffer.remaining()];
		m_buffer.get(data);
		DataMessage msg=new DataMessage(blockId,data);
		ByteBuffer buf=ByteBuffer.allocate(msg.getBufferSize());
		buf.order(m_byteOrder);
		msg.toBuffer(buf);

		m_socket.send(Buffer.buffer(buf.array()),m_port,m_host,datagramSocketAsyncResult -> {
			if(datagramSocketAsyncResult.failed())
				handleError(datagramSocketAsyncResult.cause());
			else {
				updateLastMsg(msg);
				m_currBlock = blockId;
				log.info("send (file: "+m_fileName+" host:"+m_host+") Block "+m_currBlock+"/"+m_totalBlocks);
			}
		});

	}
	private void close(AsyncResult<Void> result){
		m_vertx.cancelTimer(m_timerID);

		m_socket.close((r) -> {
			m_resultHandler.handle(result);
		});

	}

	private enum SendStatus {
		INIT,
		SEND_REQUEST,
		SENDING_FILE,
		END;
	}


}
