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

import java.util.HashSet;
import java.util.Set;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.red5.server.Red5;
import org.red5.server.event.IEventDispatcher;
//import org.red5.server.api.scheduling.ISchedulingService;
//import org.red5.server.api.stream.IClientStream;
import org.red5.server.net.ProtocolState;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.event.BytesRead;
import org.red5.server.net.rtmp.event.ChunkSize;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Invoke;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.Ping;
import org.red5.server.net.rtmp.event.Unknown;
import org.red5.server.net.rtmp.message.Constants;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.net.rtmp.message.Packet;
import org.red5.server.net.rtmp.message.StreamAction;
import org.red5.server.net.rtmp.status.StatusCodes;
import org.red5.server.service.IPendingServiceCall;
import org.red5.server.service.IPendingServiceCallback;
import org.red5.server.service.IServiceCall;
import org.red5.server.service.PendingCall;
import org.red5.server.so.SharedObjectMessage;
import org.red5.server.stream.IClientStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;

/**
 * Base class for all RTMP handlers. 处理所有rtmp
 * 
 * @author The Red5 Project (red5@osflash.org)
 */
public abstract class BaseRTMPHandler implements IRTMPHandler, Constants,
		StatusCodes {// rtmp事件处理

	public static int Invoke_Connect = 0x0001;// 服务器连接信息
	public static int Invoke_ServerShotout = 0x0002;// 服务器踢出

	/**
	 * Logger
	 */
	private static Logger mLog = LoggerFactory.getLogger(BaseRTMPHandler.class);

	/**
	 * Application context
	 */
	// private ApplicationContext appCtx;

	// XXX: HACK HACK HACK to support stream ids
	private static ThreadLocal<Integer> mStreamLocal = new ThreadLocal<Integer>(); // 记录本地流id

	/**
	 * Getter for stream ID.
	 * 
	 * @return Stream ID
	 */
	// XXX: HACK HACK HACK to support stream ids
	public static int getStreamId() { // 流id
		return mStreamLocal.get().intValue();
	}

	/**
	 * Setter for stream Id.
	 * 
	 * @param id
	 *            Stream id
	 */
	private static void setStreamId(int id) { // 设置流id
		mStreamLocal.set(id);
	}

	/** {@inheritDoc} */
	// public void setApplicationContext(ApplicationContext appCtx) throws
	// BeansException {
	// this.appCtx = appCtx;
	// }
	/*
	 * Commented :: CatturaVideo To Support Exception Handling
	 */
	public void caughtException(RTMPConnection conn, Throwable cause) { // 异常处理
		caughtExceptions(conn, cause);
	}

	/** {@inheritDoc} */
	public void connectionOpened(RTMPConnection conn, RTMP state) { // 已连接
		mLog.info("connectionOpened - conn: {} state: {}", conn, state);
		// if (state.getMode() == RTMP.MODE_SERVER && appCtx != null) {
		// ISchedulingService service = (ISchedulingService)
		// appCtx.getBean(ISchedulingService.BEAN_NAME);
		// conn.startWaitForHandshake(service);
		// }
	}

	/** {@inheritDoc} */
	public void connectionClosed(RTMPConnection conn, RTMP state) { // 连接关闭
		state.setState(RTMP.STATE_DISCONNECTED);
		conn.close();
	}

	/** {@inheritDoc} */
	public void messageReceived(Object in, IoSession session) throws Exception { // 服务器信息返回处理
		// mLog.error("messageReceived,need handle this!!");
		RTMPConnection conn = (RTMPConnection) session
				.getAttribute(RTMPConnection.RTMP_CONNECTION_KEY);
		IRTMPEvent message = null;
		try {
			final Packet packet = (Packet) in;
			message = packet.getMessage();
			final Header header = packet.getHeader();// ChannelId: 3, Timer: 0,
														// TimerBase: 0,
														// TimerDelta: 0, Size:
														// 41, DataType: 20,
														// Garbage: false,
														// StreamId: 0
			final Channel channel = conn.getChannel(header.getChannelId());
			final IClientStream stream = conn.getStreamById(header
					.getStreamId());
			// mLog.debug("Message received, header: {}", header);
			// Thread local performance ? Should we benchmark
			Red5.setConnectionLocal(conn);
			// XXX: HACK HACK HACK to support stream ids
			BaseRTMPHandler.setStreamId(header.getStreamId());
			// increase number of received messages
			conn.messageReceived();
			// set the source of the message
			message.setSource(conn);
			// process based on data type
			switch (header.getDataType()) {
			case TYPE_CHUNK_SIZE:
				onChunkSize(conn, channel, header, (ChunkSize) message);
				break;
			case TYPE_INVOKE:
			case TYPE_FLEX_MESSAGE:
				// 调用
				onInvoke(conn, channel, header, (Invoke) message,
						(RTMP) session.getAttribute(ProtocolState.SESSION_KEY));

				IPendingServiceCall call = ((Invoke) message).getCall();
				if (message.getHeader().getStreamId() != 0
						&& call.getServiceName() == null
						&& StreamAction.PUBLISH.equals(call
								.getServiceMethodName())) {
					if (stream != null) {
						// Only dispatch if stream really was created
						((IEventDispatcher) stream).dispatchEvent(message);
					}
				}
				break;
			case TYPE_NOTIFY: // 只是调用，不做返回
				if (((Notify) message).getData() != null && stream != null) {
					// Stream metadata
					((IEventDispatcher) stream).dispatchEvent(message);
				} else {
					onInvoke(conn, channel, header, (Notify) message,
							(RTMP) session
									.getAttribute(ProtocolState.SESSION_KEY));
				}
				break;
			case TYPE_FLEX_STREAM_SEND:
				if (stream != null) {
					((IEventDispatcher) stream).dispatchEvent(message);
				}
				break;
			case TYPE_PING:
				onPing(conn, channel, header, (Ping) message);
				break;
			case TYPE_BYTES_READ:
				onStreamBytesRead(conn, channel, header, (BytesRead) message);
				break;
			case TYPE_AGGREGATE:
				mLog.debug("Aggregate type data - header timer: {} size: {}",
						header.getTimer(), header.getSize());
			case TYPE_AUDIO_DATA: // 音频
			case TYPE_VIDEO_DATA:// 视频
				// mark the event as from a live source
				// mLog.trace("Marking message as originating from a Live source");
				message.setSourceType(Constants.SOURCE_TYPE_LIVE);
				// NOTE: If we respond to "publish" with
				// "NetStream.Publish.BadName",
				// the client sends a few stream packets before stopping. We
				// need to ignore them.
				if (stream != null) {
					((IEventDispatcher) stream).dispatchEvent(message);
				}
				break;
			case TYPE_FLEX_SHARED_OBJECT: // flex共享
			case TYPE_SHARED_OBJECT: // 共享对象
				onSharedObject(conn, channel, header,
						(SharedObjectMessage) message);
				break;
			case Constants.TYPE_CLIENT_BANDWIDTH: // onBWDone
				mLog.debug("Client bandwidth: {}", message);
				break;
			case Constants.TYPE_SERVER_BANDWIDTH:
				mLog.debug("Server bandwidth: {}", message);
				break;
			default:
				mLog.debug("Unknown type: {}", header.getDataType());
			}
			if (message instanceof Unknown) {
				mLog.info("Message type unknown: {}", message);
			}
		} catch (RuntimeException e) {
			mLog.error("Exception", e);
		}

		if (message != null) {// 释放
			message.release();
		}

	}

	/** {@inheritDoc} */
	public void messageSent(RTMPConnection conn, Object message) { // 消息发送
		// mLog.debug("Message sent");
		if (message instanceof IoBuffer) {
			return;
		}
		// Increase number of sent messages
		conn.messageSent((Packet) message);
	}

	/**
	 * Return hostname for URL.
	 * 
	 * @param url
	 *            URL
	 * @return Hostname from that URL
	 */
	protected String getHostname(String url) {// 主机名
		mLog.debug("url: {}", url);
		String[] parts = url.split("/");
		if (parts.length == 2) {
			return ""; // 默认为空
		} else {
			return parts[2];
		}
	}

	/**
	 * Handler for pending call result. Dispatches results to all pending call
	 * handlers.
	 * 
	 * @param conn
	 *            Connection
	 * @param invoke
	 *            Pending call result event context
	 */
	protected void handlePendingCallResult(RTMPConnection conn, Notify invoke) {// 回调结果（并移除该事件）
		final IServiceCall call = invoke.getCall();
		final IPendingServiceCall pendingCall = conn.retrievePendingCall(invoke
				.getInvokeId());
		if (pendingCall != null) {
			Object[] args = call.getArguments();// 添加参数
			if ((args != null) && (args.length > 0)) {
				pendingCall.setResult(args[0]);
			}
			Set<IPendingServiceCallback> callbacks = pendingCall.getCallbacks();
			if (!callbacks.isEmpty()) {
				HashSet<IPendingServiceCallback> tmp = new HashSet<IPendingServiceCallback>();
				tmp.addAll(callbacks);
				for (IPendingServiceCallback callback : tmp) {
					try {
						callback.resultReceived(pendingCall);
					} catch (Exception e) {
						mLog.error("Error while executing callback {} {}",
								callback, e);
					}
				}
			}
		}
	}

	/**
	 * Called when bandwidth has been configured.
	 */
	protected abstract void onBWDone(); // 服务器调用

	/*
	 * Commented :: CatturaVideo To Support Exception Handling
	 */
	protected abstract void caughtExceptions(RTMPConnection conn,
			Throwable cause); // 捕捉异常

	/**
	 * Chunk size change event handler. Abstract, to be implemented in
	 * subclasses.
	 * 
	 * @param conn
	 *            Connection
	 * @param channel
	 *            Channel
	 * @param source
	 *            Header
	 * @param chunkSize
	 *            New chunk size
	 */
	protected abstract void onChunkSize(RTMPConnection conn, Channel channel,
			Header source, ChunkSize chunkSize);// 块的大小

	/**
	 * Invocation event handler.
	 * 
	 * @param conn
	 *            Connection
	 * @param channel
	 *            Channel
	 * @param source
	 *            Header
	 * @param invoke
	 *            Invocation event context
	 * @param rtmp
	 *            RTMP connection state
	 */
	protected abstract boolean onInvoke(RTMPConnection conn, Channel channel,
			Header source, Notify invoke, RTMP rtmp); // 调用事件

	/**
	 * Ping event handler.
	 * 
	 * @param conn
	 *            Connection
	 * @param channel
	 *            Channel
	 * @param source
	 *            Header
	 * @param ping
	 *            Ping event context
	 */
	protected abstract void onPing(RTMPConnection conn, Channel channel,
			Header source, Ping ping); // ping事件

	/**
	 * Stream bytes read event handler.
	 * 
	 * @param conn
	 *            Connection
	 * @param channel
	 *            Channel
	 * @param source
	 *            Header
	 * @param streamBytesRead
	 *            Bytes read event context
	 */
	protected void onStreamBytesRead(RTMPConnection conn, Channel channel,
			Header source, BytesRead streamBytesRead) { // 流字节
		conn.receivedBytesRead(streamBytesRead.getBytesRead());
	}

	/**
	 * Shared object event handler.
	 * 
	 * @param conn
	 *            Connection
	 * @param channel
	 *            Channel
	 * @param source
	 *            Header
	 * @param object
	 *            Shared object event context
	 */
	protected abstract void onSharedObject(RTMPConnection conn,
			Channel channel, Header source, SharedObjectMessage object); // 共享对象事件
}
