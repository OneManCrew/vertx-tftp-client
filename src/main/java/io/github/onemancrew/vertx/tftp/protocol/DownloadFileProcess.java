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
import io.github.onemancrew.vertx.tftp.protocol.message.request.ReadRequestMessage;
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
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Project: vertx-tftp-client
 * File: DownloadFileProcess.java
 * Package: io.github.onemancrew.vertx.tftp.protocol
 * create by: Levi
 * create date: 12-02-2020
 * Last update by:
 * Last update date:
 **/
public class DownloadFileProcess {
    public static final int TIMEOUT_MS = 3000;
    private static final int MAX_RETRIES = 3;
    private static Logger log = LoggerFactory.getLogger(SendFileProcess.class);
    private final String m_host;
    private final String m_workFolder;
    private final String m_fileName;
    private final Handler<AsyncResult<Void>> m_resultHandler;
    private final Buffer m_buffer;
    private int m_port;
    private Vertx m_vertx;
    private ByteOrder m_byteOrder = ByteOrder.nativeOrder();
    private DatagramSocket m_socket;
    private DownloadStatus m_downloadStatus = DownloadStatus.INIT;
    private int m_currBlock = 0;
    private IBufferProtocol m_lastMsg;
    private long m_lastSendTimeMs;
    private int m_retries = 0;
    private long m_timerID;


    public DownloadFileProcess(Vertx vertx, ByteOrder byteOrder, String hostDst, int portDst, String workFolder, String fileName, Handler<AsyncResult<Void>> handler) {
        m_host = hostDst;
        m_port = portDst;
        m_vertx = vertx;
        m_byteOrder = byteOrder;
        m_fileName = fileName;
        m_workFolder = workFolder;
        m_resultHandler = handler;
        m_buffer = Buffer.buffer();

    }

    public void download(int port) {
        m_socket = m_vertx.createDatagramSocket(new DatagramSocketOptions().setReuseAddress(true));
        m_timerID = m_vertx.setPeriodic(500, new Handler<Long>() {

            @Override
            public void handle(Long aLong) {
                checkLastMsg();
            }
        });
        m_socket.listen(port, "0.0.0.0", asyncResult -> {
            if (asyncResult.succeeded()) {
                startDownloadFile();
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
        m_lastSendTimeMs = System.currentTimeMillis();
        m_retries = 0;
    }

    private void checkLastMsg() {
        if (m_lastMsg == null)
            return;
        long currTime = System.currentTimeMillis();
        if (currTime - m_lastSendTimeMs > TIMEOUT_MS) {
            if (m_retries < MAX_RETRIES) {
                IBufferProtocol msg = m_lastMsg;
                ByteBuffer buf = ByteBuffer.allocate(msg.getBufferSize());
                m_socket.send(Buffer.buffer(buf.array()), m_port, m_host, datagramSocketAsyncResult -> {
                    if (datagramSocketAsyncResult.failed())
                        handleError(datagramSocketAsyncResult.cause());
                    else {
                        m_lastSendTimeMs = currTime;
                        m_retries++;
                        log.info("Resend last msg of file " + m_fileName + " to " + m_host);
                    }

                });
            } else {
                handleError(new TimeoutException());
            }

        }
    }

    private void startDownloadFile() {
        log.info("start Download File " + m_fileName + " to " + m_host);
        ReadRequestMessage msg = new ReadRequestMessage(m_fileName);
        ByteBuffer buf = ByteBuffer.allocate(msg.getBufferSize());
        buf.order(m_byteOrder);
        msg.toBuffer(buf);

        m_socket.send(Buffer.buffer(buf.array()), m_port, m_host, datagramSocketAsyncResult -> {
            if (datagramSocketAsyncResult.failed())
                handleError(datagramSocketAsyncResult.cause());
            else {
                updateLastMsg(msg);
                m_downloadStatus = DownloadStatus.SEND_REQUEST;
                log.info("Send Read request(RRQ) of file " + m_fileName + " to " + m_host);
            }

        });

    }

    private void handleError(Throwable cause) {
        log.error("handleError: " + cause.getMessage());
        close();
        m_resultHandler.handle(Future.failedFuture(cause));
    }

    private void handleMsg(DatagramPacket packet) {
        m_port = packet.sender().port();
        byte[] bytes = packet.data().getBytes();
        try {
            Opcode opcode = Opcode.ValueOf(bytes[1]);
            log.info("handle incoming OpCode:" + opcode);
            switch (opcode) {
                case DATA:
                    handleData(bytes);
                    break;
                case ACK:
                case ERROR:
                case RRQ:
                case WRQ:
                    handleERRMsg(opcode, bytes);
                    break;


            }
        } catch (UnknownOpcodeException e) {
            handleError(e);
        }

    }

    private void handleERRMsg(Opcode opcode, byte[] bytes) {
        if (opcode == Opcode.ERROR) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(m_byteOrder);
            ErrorMessage msg = new ErrorMessage(ErrorCode.NO_ERROR);
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

    private void handleData(byte[] bytes) throws UnknownOpcodeException {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.order(m_byteOrder);
        DataMessage msg = new DataMessage();
        msg.fromBuffer(buf);
        log.info("receive(file: " + m_fileName + " host:" + m_host + ") Data " + msg.getBlockId());
        if (m_downloadStatus == DownloadStatus.SEND_REQUEST && msg.getBlockId() == 1) {
            m_downloadStatus = DownloadStatus.DOWNLOADING_FILE;
            updateData(msg);
        } else if (m_downloadStatus == DownloadStatus.DOWNLOADING_FILE && msg.getBlockId() == m_currBlock + 1) {
            updateData(msg);
        } else {

        }


    }

    private void updateData(DataMessage msg) {
        m_currBlock = msg.getBlockId();
        m_buffer.appendBytes(msg.getData());
        sendAck(m_currBlock);
        if (msg.isLast())
            finishDownload();


    }

    private void finishDownload() {
        String sep = File.separator;
        String file = m_workFolder + sep + m_fileName;
        m_vertx.fileSystem().writeFile(file, m_buffer, (result) -> {
            if (result.failed()) {
                handleError(result.cause());
            } else {
                log.info("write file (file: " + file + " host:" + m_host + ") ");
                close();
                m_resultHandler.handle(Future.succeededFuture());
            }
        });
    }

    private void sendAck(int currBlock) {
        AckMessage msg = new AckMessage(currBlock);
        ByteBuffer buf = ByteBuffer.allocate(msg.getBufferSize());
        buf.order(m_byteOrder);
        msg.toBuffer(buf);

        m_socket.send(Buffer.buffer(buf.array()), m_port, m_host, datagramSocketAsyncResult -> {
            if (datagramSocketAsyncResult.failed())
                handleError(datagramSocketAsyncResult.cause());
            else {
                updateLastMsg(msg);
                log.info("Send Ack of file: " + m_fileName + " to: " + m_host + " BlockId: " + currBlock);
            }

        });
    }


    private void close() {
        m_socket.close();
        m_vertx.cancelTimer(m_timerID);

    }

    private enum DownloadStatus {
        INIT,
        SEND_REQUEST,
        DOWNLOADING_FILE,
        END;
    }


}