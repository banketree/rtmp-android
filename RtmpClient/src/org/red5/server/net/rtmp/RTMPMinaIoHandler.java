package org.red5.server.net.rtmp;

/*
 * RED5 Open Source Flash Server - http://code.google.com/p/red5/
 * 
 * Copyright (c) 2006-2010 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.red5.server.net.ProtocolState;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmpe.RTMPEIoFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all RTMP protocol events fired by the MINA framework.
 */
public class RTMPMinaIoHandler extends IoHandlerAdapter {// 处理所有RTMP协议的框架MINA事件（mina调用）

	private static Logger log = LoggerFactory
			.getLogger(RTMPMinaIoHandler.class);

	/**
	 * RTMP events handler
	 */
	protected IRTMPHandler handler; // 客户端处理接口

	/**
	 * Mode
	 */
	protected boolean mode = RTMP.MODE_SERVER;

	/**
	 * Application context
	 */
	// protected ApplicationContext appCtx;

	/**
	 * RTMP protocol codec factory
	 */
	protected ProtocolCodecFactory codecFactory;

	protected IRTMPConnManager rtmpConnManager;

	/** {@inheritDoc} */
	@Override
	public void sessionCreated(IoSession session) throws Exception {// 建立会话
		log.debug("Session created");
		// moved protocol state from connection object to RTMP object
		RTMP rtmp = new RTMP(mode);
		session.setAttribute(ProtocolState.SESSION_KEY, rtmp);
		// add rtmpe filter
		session.getFilterChain().addFirst("rtmpeFilter", new RTMPEIoFilter());
		// add protocol filter next
		session.getFilterChain().addLast("protocolFilter",
				new ProtocolCodecFilter(codecFactory));
		if (log.isTraceEnabled()) {
			session.getFilterChain().addLast("logger", new LoggingFilter());
		}
		// create a connection
		RTMPMinaConnection conn = createRTMPMinaConnection();
		conn.setIoSession(session);
		conn.setState(rtmp);
		// add the connection
		session.setAttribute(RTMPConnection.RTMP_CONNECTION_KEY, conn);
		// create inbound or outbound handshaker
		if (rtmp.getMode() == RTMP.MODE_CLIENT) {
			// create an outbound handshake
			OutboundHandshake outgoingHandshake = new OutboundHandshake();
			// if handler is rtmpe client set encryption on the protocol state
			// if (handler instanceof RTMPEClient) {
			// rtmp.setEncrypted(true);
			// set the handshake type to encrypted as well
			// outgoingHandshake.setHandshakeType(RTMPConnection.RTMP_ENCRYPTED);
			// }
			// add the handshake
			session.setAttribute(RTMPConnection.RTMP_HANDSHAKE,
					outgoingHandshake);
			// set a reference to the connection on the client
			if (handler instanceof BaseRTMPClientHandler) {
				((BaseRTMPClientHandler) handler)
						.setConnection((RTMPConnection) conn);
			}
		} else {
			// add the handshake
			// session.setAttribute(RTMPConnection.RTMP_HANDSHAKE, new
			// InboundHandshake());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sessionOpened(IoSession session) throws Exception { // 会话打开
		log.debug("Session opened");
		super.sessionOpened(session);
		// get protocol state
		RTMP rtmp = (RTMP) session.getAttribute(ProtocolState.SESSION_KEY);
		if (rtmp.getMode() == RTMP.MODE_CLIENT) {
			log.debug("Handshake - client phase 1");
			// get the handshake from the session
			RTMPHandshake handshake = (RTMPHandshake) session
					.getAttribute(RTMPConnection.RTMP_HANDSHAKE);
			session.write(handshake.doHandshake(null));
		} else {
			handler.connectionOpened((RTMPMinaConnection) session
					.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY), rtmp);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void sessionClosed(IoSession session) throws Exception {// （mina调用）会话关闭
		log.debug("Session closed");
		RTMP rtmp = (RTMP) session.removeAttribute(ProtocolState.SESSION_KEY);
		log.debug("RTMP state: {}", rtmp);
		RTMPMinaConnection conn = (RTMPMinaConnection) session
				.removeAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		conn.sendPendingServiceCallsCloseError();
		// fire-off closed
		handler.connectionClosed(conn, rtmp);// 调用客户端的关闭
		// remove the handshake if not already done
		if (session.containsAttribute(RTMPConnection.RTMP_HANDSHAKE)) {
			session.removeAttribute(RTMPConnection.RTMP_HANDSHAKE);
		}
		// remove ciphers
		if (session.containsAttribute(RTMPConnection.RTMPE_CIPHER_IN)) {
			session.removeAttribute(RTMPConnection.RTMPE_CIPHER_IN);
			session.removeAttribute(RTMPConnection.RTMPE_CIPHER_OUT);
		}
		rtmpConnManager.removeConnection(conn.getId());
	}

	/**
	 * Handle raw buffer receiving event.
	 * 
	 * @param in
	 *            Data buffer
	 * @param session
	 *            I/O session, that is, connection between two endpoints
	 */
	protected void rawBufferRecieved(IoBuffer in, IoSession session) {// 处理原始缓冲区接收事件
		// log.debug("rawBufferRecieved: {}", in);
		final RTMP rtmp = (RTMP) session
				.getAttribute(ProtocolState.SESSION_KEY);
		log.debug("state: {}", rtmp);
		final RTMPMinaConnection conn = (RTMPMinaConnection) session
				.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		RTMPHandshake handshake = (RTMPHandshake) session
				.getAttribute(RTMPConnection.RTMP_HANDSHAKE);
		if (handshake != null) {
			IoBuffer out = null;
			conn.getWriteLock().lock();
			try {
				if (rtmp.getMode() == RTMP.MODE_SERVER) {
					if (rtmp.getState() != RTMP.STATE_HANDSHAKE) {
						log.warn("Raw buffer after handshake, something odd going on");
					}
					log.debug("Handshake - server phase 1 - size: {}",
							in.remaining());
				} else {
					log.debug("Handshake - client phase 2 - size: {}",
							in.remaining());
				}
				out = handshake.doHandshake(in);
			} finally {
				conn.getWriteLock().unlock();
				if (out != null) {
					// log.debug("Output: {}", out);
					session.write(out);
					// if we are connected and doing encryption, add the ciphers
					if (rtmp.getState() == RTMP.STATE_CONNECTED) {
						// remove handshake from session now that we are
						// connected
						// session.removeAttribute(RTMPConnection.RTMP_HANDSHAKE);
						// if we are using encryption then put the ciphers in
						// the session
						if (handshake.getHandshakeType() == RTMPConnection.RTMP_ENCRYPTED) {
							log.debug("Adding ciphers to the session");
							session.setAttribute(
									RTMPConnection.RTMPE_CIPHER_IN,
									handshake.getCipherIn());
							session.setAttribute(
									RTMPConnection.RTMPE_CIPHER_OUT,
									handshake.getCipherOut());
						}
					}
				}
			}
		} else {
			log.warn("Handshake was not found for this connection: {}", conn);
			log.debug("RTMP state: {} Session: {}", rtmp, session);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {// 服务器返回消息
		// log.debug("messageReceived");
		if (message instanceof IoBuffer) {
			rawBufferRecieved((IoBuffer) message, session);
		} else {
			handler.messageReceived(message, session);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void messageSent(IoSession session, Object message) throws Exception { // 消息发送（由mina调用）
		// log.debug("messageSent");
		final RTMPMinaConnection conn = (RTMPMinaConnection) session
				.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		handler.messageSent(conn, message);
		if (mode == RTMP.MODE_CLIENT) {
			if (message instanceof IoBuffer) {
				if (((IoBuffer) message).limit() == Constants.HANDSHAKE_SIZE) {
					RTMP rtmp = (RTMP) session
							.getAttribute(ProtocolState.SESSION_KEY);
					handler.connectionOpened(conn, rtmp);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception { // 异常捕捉处理
		log.warn("Exception caught {}", cause.getMessage());
		log.error("Exception detail {}", cause);
		if (log.isDebugEnabled()) {
			log.error("Exception detail", cause);
		}
		/*
		 * Commented :: CatturaVideo To Support Exception Handling
		 */
		handler.caughtException((RTMPMinaConnection) session
				.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY), cause);// Added
																			// by
																			// Cattura
	}

	/**
	 * Setter for handler.
	 * 
	 * @param handler
	 *            RTMP events handler
	 */
	public void setHandler(IRTMPHandler handler) { // 设置rtmp事件
		this.handler = handler;
	}

	/**
	 * Setter for mode.
	 * 
	 * @param mode
	 *            <code>true</code> if handler should work in server mode,
	 *            <code>false</code> otherwise
	 */
	public void setMode(boolean mode) { // 设置模式
		this.mode = mode;
	}

	/**
	 * Setter for codec factory.
	 * 
	 * @param codecFactory
	 *            RTMP protocol codec factory
	 */
	public void setCodecFactory(ProtocolCodecFactory codecFactory) { // 设置编码
		this.codecFactory = codecFactory;
	}

	public void setRtmpConnManager(IRTMPConnManager rtmpConnManager) { // 设置rtmp连接管理
		this.rtmpConnManager = rtmpConnManager;
	}

	protected IRTMPConnManager getRtmpConnManager() { // 返回rtmp连接管理
		return rtmpConnManager;
	}

	/** {@inheritDoc} */
	// public void setApplicationContext(ApplicationContext appCtx) throws
	// BeansException {
	// log.debug("Setting application context: {} {}", appCtx.getDisplayName(),
	// appCtx);
	// this.appCtx = appCtx;
	// }

	protected RTMPMinaConnection createRTMPMinaConnection() { // 创建一个rtmp mina连接
		return (RTMPMinaConnection) rtmpConnManager
				.createConnection(RTMPMinaConnection.class);
	}
}
