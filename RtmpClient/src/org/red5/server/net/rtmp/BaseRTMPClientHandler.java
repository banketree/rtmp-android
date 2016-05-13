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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.red5.io.object.Deserializer;
import org.red5.io.object.Serializer;
import org.red5.io.utils.ObjectMap;
import org.red5.server.IConnection;
import org.red5.server.event.IEvent;
import org.red5.server.event.IEventDispatcher;
import org.red5.server.messaging.IMessage;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.codec.RTMPCodecFactory;
import org.red5.server.net.rtmp.event.ChunkSize;
import org.red5.server.net.rtmp.event.Invoke;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.Ping;
import org.red5.server.net.rtmp.message.Header;
import org.red5.server.service.Call;
import org.red5.server.service.IPendingServiceCall;
import org.red5.server.service.IPendingServiceCallback;
import org.red5.server.service.IServiceCall;
import org.red5.server.service.IServiceInvoker;
import org.red5.server.service.ServiceInvoker;
//import org.red5.server.service.IServiceInvoker;
import org.red5.server.service.MethodNotFoundException;
import org.red5.server.service.PendingCall;
//import org.red5.server.service.ServiceInvoker;
import org.red5.server.so.ClientSharedObject;
import org.red5.server.so.IClientSharedObject;
import org.red5.server.so.SharedObjectMessage;
import org.red5.server.stream.AbstractClientStream;
//import org.red5.server.stream.IClientStream;
import org.red5.server.stream.OutputStream;
import org.red5.server.stream.consumer.ConnectionConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for clients (RTMP and RTMPT) rtmp客户端
 */
public abstract class BaseRTMPClientHandler extends BaseRTMPHandler {

	private static final Logger log = LoggerFactory
			.getLogger(BaseRTMPClientHandler.class);

	/**
	 * Connection scheme / protocol
	 */
	protected String mScheme = "rtmp"; // 协议名

	/**
	 * Connection parameters
	 */
	protected Map<String, Object> mConnectionParams; // 连接参数

	/**
	 * Connect call arguments
	 */
	private Object[] mConnectArguments = null;// 连接参数

	/**
	 * Connection callback
	 */
	/* Commented :: CatturaVideo */
	public IPendingServiceCallback mConnectCallback; // 连接回调

	/**
	 * Service provider
	 */
	private Object mServiceProvider; // 服务

	/**
	 * Service invoker
	 */
	private IServiceInvoker mServiceInvoker = new ServiceInvoker(); // 服务调用

	/**
	 * Shared objects map 共享对象
	 */
	private volatile ConcurrentMap<String, ClientSharedObject> mSharedObjects = new ConcurrentHashMap<String, ClientSharedObject>();

	/**
	 * Net stream handling 网络流数据
	 */
	private volatile ConcurrentMap<Object, NetStreamPrivateData> mStreamDataMap = new ConcurrentHashMap<Object, NetStreamPrivateData>();

	/**
	 * Task to start on connection close 关闭连接任务
	 */
	private Runnable mConnectionClosedHandler;

	/**
	 * Task to start on connection errors 连接错误
	 */
	private ClientExceptionHandler mExceptionHandler;
	private RTMPCodecFactory mCodecFactory;// 编码生成
	private IEventDispatcher mStreamEventDispatcher; // 流事件匹配
	protected volatile RTMPConnection mRTMPConnection; // rtmp连接

	protected BaseRTMPClientHandler() {// rtmp 编码生成
		mCodecFactory = new RTMPCodecFactory();
		mCodecFactory.setDeserializer(new Deserializer());
		mCodecFactory.setSerializer(new Serializer());
		mCodecFactory.init();
	}

	public void setConnectionClosedHandler(Runnable connectionClosedHandler) { // 连接关闭
		this.mConnectionClosedHandler = connectionClosedHandler;
	}

	public void setExceptionHandler(ClientExceptionHandler exceptionHandler) {// 异常
		this.mExceptionHandler = exceptionHandler;
	}

	/**
	 * Start network connection to server
	 * 
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 */
	protected abstract void startConnector(String server, int port); // 开始连接

	/**
	 * Connect RTMP client to server's application via given port
	 * 
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 * @param application
	 *            Application at that server
	 */
	public void connect(String server, int port, String application) { // 连接
		log.debug("connect server: {} port {} application {}", new Object[] {
				server, port, application });
		connect(server, port, application, null);
	}

	/**
	 * Connect RTMP client to server's application via given port with given
	 * connection callback
	 * 
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 * @param application
	 *            Application at that server
	 * @param connectCallback
	 *            Connection callback
	 */
	public void connect(String server, int port, String application,
			IPendingServiceCallback connectCallback) {// 带回调的连接
		log.debug(
				"connect server: {} port {} application {} connectCallback {}",
				new Object[] { server, port, application, connectCallback });
		connect(server, port,
				makeDefaultConnectionParams(server, port, application),
				connectCallback);
	}

	/**
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 * @param application
	 *            Application at that server
	 * @return default connection parameters
	 */

	/*
	 * Commented :: CatturaVideo This change has been made to introduce the
	 * authentication by User Name & Password
	 */
	public Map<String, Object> makeDefaultConnectionParams(String server,
			int port, String application) { // 产生参数
		Map<String, Object> params = new ObjectMap<String, Object>();
		params.put("app", application);
		// params.put("objectEncoding", Integer.valueOf(0));
		// params.put("fpad", Boolean.FALSE);
		params.put("flashVer", "WIN 9,0,124,2");
		// params.put("audioCodecs", Integer.valueOf(1639));
		// params.put("videoFunction", Integer.valueOf(1));
		// params.put("pageUrl", null);
		// params.put("path", application);
		// params.put("capabilities", Integer.valueOf(15));
		params.put("swfUrl",
				String.format("rtmp://%s:%s/%s", server, port, application));
		// params.put("videoCodecs", Integer.valueOf(252));
		params.put("type", "nonprivate");
		// params.put("flashVer", "FMLE/3.0 (compatible; FMSC/1.0)");
		if (!params.containsKey("tcUrl"))
			params.put("tcUrl",
					String.format("rtmp://%s:%s/%s", server, port, application));
		return params;
	}

	/**
	 * Connect RTMP client to server via given port and with given connection
	 * parameters
	 * 
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 * @param connectionParams
	 *            Connection parameters
	 */
	public void connect(String server, int port,
			Map<String, Object> connectionParams) { // 连接
		log.debug("connect server: {} port {} connectionParams {}",
				new Object[] { server, port, connectionParams });
		connect(server, port, connectionParams, null);
	}

	/**
	 * Connect RTMP client to server's application via given port
	 * 
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 * @param connectionParams
	 *            Connection parameters
	 * @param connectCallback
	 *            Connection callback
	 */
	public void connect(String server, int port,
			Map<String, Object> connectionParams,
			IPendingServiceCallback connectCallback) { // rtmp连接服务器
		connect(server, port, connectionParams, connectCallback, null);
	}

	/**
	 * Connect RTMP client to server's application via given port
	 * 
	 * @param server
	 *            Server
	 * @param port
	 *            Connection port
	 * @param connectionParams
	 *            Connection parameters
	 * @param connectCallback
	 *            Connection callback
	 * @param connectCallArguments
	 *            Arguments for 'connect' call
	 */
	public void connect(String server, int port,
			Map<String, Object> connectionParams,
			IPendingServiceCallback connectCallback,
			Object[] connectCallArguments) { // 连接
		log.debug(
				"connect server: {} port {} connect - params: {} callback: {} args: {}",
				new Object[] { server, port, connectionParams, connectCallback,
						Arrays.toString(connectCallArguments) });
		log.info("{}://{}:{}/{}", new Object[] { mScheme, server, port,
				connectionParams.get("app") });
		this.mConnectionParams = connectionParams;
		this.mConnectArguments = connectCallArguments;

		if (!connectionParams.containsKey("objectEncoding")) {
			/*
			 * Commented :: CatturaVideo objectEncoding is not part of
			 * Connection Parameters
			 */
			// connectionParams.put("objectEncoding", 0);
		}

		this.mConnectCallback = connectCallback;

		startConnector(server, port);
	}

	/**
	 * Register object that provides methods that can be called by the server.
	 * 
	 * @param serviceProvider
	 *            Service provider
	 */
	public void setServiceProvider(Object serviceProvider) {
		this.mServiceProvider = serviceProvider;
	}

	/**
	 * Connect to client shared object.
	 * 
	 * @param name
	 *            Client shared object name
	 * @param persistent
	 *            SO persistence flag
	 * @return Client shared object instance
	 */
	public synchronized IClientSharedObject getSharedObject(String name,
			boolean persistent) { // 客户共享数据
		log.debug("getSharedObject name: {} persistent {}", new Object[] {
				name, persistent });
		ClientSharedObject result = mSharedObjects.get(name);
		if (result != null) {
			if (result.isPersistentObject() != persistent) {
				throw new RuntimeException(
						"Already connected to a shared object with this name, but with different persistence.");
			}
			return result;
		}

		result = new ClientSharedObject(name, persistent);
		mSharedObjects.put(name, result);
		return result;
	}

	public synchronized void clearSharedObject() {
		mSharedObjects.clear();
	}

	/*
	 * Commented :: CatturaVideo To support the exception handling
	 */
	@Override
	protected void caughtExceptions(RTMPConnection conn, Throwable cause) { // 捕捉异常
		Object type = (Object) new String(cause.toString());
		PendingCall pendingCall = new PendingCall("Exception");
		pendingCall.setException(new Exception(cause));
		this.mConnectCallback.resultReceived(pendingCall);
	}

	/** {@inheritDoc} */
	@Override
	protected void onChunkSize(RTMPConnection conn, Channel channel,
			Header source, ChunkSize chunkSize) { // 块大小
		log.debug("onChunkSize");
		// set read and write chunk sizes
		RTMP state = conn.getState();
		state.setReadChunkSize(chunkSize.getSize());
		state.setWriteChunkSize(chunkSize.getSize());
		log.info("ChunkSize is not implemented yet: {}", chunkSize);
	}

	/** {@inheritDoc} */
	@Override
	protected void onPing(RTMPConnection conn, Channel channel, Header source,
			Ping ping) { // ping事件
		// log.debug("onPing");
		switch (ping.getEventType()) {
		case Ping.PING_CLIENT:
		case Ping.STREAM_BEGIN:
		case Ping.RECORDED_STREAM:
		case Ping.STREAM_PLAYBUFFER_CLEAR:
			// The server wants to measure the RTT
			Ping pong = new Ping();
			pong.setEventType(Ping.PONG_SERVER);
			pong.setValue2((int) (System.currentTimeMillis() & 0xffffffff));
			conn.ping(pong);
			break;
		case Ping.STREAM_DRY:
			log.debug("Stream indicates there is no data available");
			break;
		case Ping.CLIENT_BUFFER:
			// set the client buffer
			// IClientStream stream = null;
			// //get the stream id
			// int streamId = ping.getValue2();
			// //get requested buffer size in milliseconds
			// int buffer = ping.getValue3();
			// log.debug("Client sent a buffer size: {} ms for stream id: {}",
			// buffer, streamId);
			// if (streamId != 0) {
			// // The client wants to set the buffer time
			// stream = conn.getStreamById(streamId);
			// if (stream != null) {
			// stream.setClientBufferDuration(buffer);
			// log.info("Setting client buffer on stream: {}", buffer);
			// }
			// }
			// //catch-all to make sure buffer size is set
			// if (stream == null) {
			// // Remember buffer time until stream is created
			// conn.rememberStreamBufferDuration(streamId, buffer);
			// log.info("Remembering client buffer on stream: {}", buffer);
			// }
			break;
		case Ping.PING_SWF_VERIFY:
			log.debug("SWF verification ping");
			Ping swfPong = new Ping();
			swfPong.setEventType(Ping.PONG_SWF_VERIFY);
			swfPong.setValue2((int) (System.currentTimeMillis() & 0xffffffff));
			conn.ping(swfPong);
			break;
		default:
			log.warn("Unhandled ping: {}", ping);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void onSharedObject(RTMPConnection conn, Channel channel,
			Header source, SharedObjectMessage object) {// 共享数据
		log.debug("onSharedObject");
		ClientSharedObject so = mSharedObjects.get(object.getName());
		if (so == null) {
			log.error("Ignoring request for non-existend SO: {}", object);
			return;
		}
		if (so.isPersistentObject() != object.isPersistent()) {
			log.error("Ignoring request for wrong-persistent SO: {}", object);
			return;
		}
		log.debug("Received SO request: {}", object);
		so.dispatchEvent(object);
	}

	/**
	 * Invoke a method on the server.
	 * 
	 * @param method
	 *            Method name
	 * @param callback
	 *            Callback handler
	 */
	public void invoke(String method, IPendingServiceCallback callback) { // 调用方法
		log.debug("invoke method: {} params {} callback {}", new Object[] {
				method, callback });
		// get it from the conn manager
		if (mRTMPConnection != null) {
			mRTMPConnection.invoke(method, callback);
		} else {
			log.info("Connection was null");
			PendingCall result = new PendingCall(method);
			result.setStatus(Call.STATUS_NOT_CONNECTED);
			callback.resultReceived(result);
		}
	}

	/**
	 * Invoke a method on the server and pass parameters.
	 * 
	 * @param method
	 *            Method
	 * @param params
	 *            Method call parameters
	 * @param callback
	 *            Callback object
	 */
	public void invoke(String method, Object[] params,
			IPendingServiceCallback callback) { // 调用方法
		// log.error("invoke method: {} params {} callback {}", new Object[] {
		// method, params, callback });
		if (mRTMPConnection != null) {
			mRTMPConnection.invoke(method, params, callback);
		} else {
			log.info("Connection was null");
			PendingCall result = new PendingCall(method, params);
			result.setStatus(Call.STATUS_NOT_CONNECTED);
			callback.resultReceived(result);
		}
	}

	/**
	 * Disconnect the first connection in the connection map
	 */
	public void disconnect() { // 断开
		log.debug("disconnect");
		if (mRTMPConnection != null) {
			mRTMPConnection.sendPendingServiceCallsCloseError();
			mStreamDataMap.clear();
			mRTMPConnection.close();
		} else {
			log.info("Connection was null");
		}
	}

	public void createStream(IPendingServiceCallback callback) { // 建立流
		log.debug("createStream - callback: {}", callback);
		IPendingServiceCallback wrapper = new CreateStreamCallBack(callback);
		invoke("createStream", null, wrapper);
	}

	public void publish(int streamId, String name, String mode,
			INetStreamEventHandler handler) { // 发布数据
		log.debug("publish - stream id: {}, name: {}, mode: {}", new Object[] {
				streamId, name, mode });
		Object[] params = new Object[2];
		params[0] = name;
		params[1] = mode;
		PendingCall pendingCall = new PendingCall("publish", params);
		mRTMPConnection.invoke(pendingCall, getChannelForStreamId(streamId));
		if (handler != null) {
			NetStreamPrivateData streamData = mStreamDataMap.get(streamId);
			if (streamData != null) {
				log.debug("Setting handler on stream data - handler: {}",
						handler);
				streamData.handler = handler;
			} else {
				log.debug("Stream data not found for stream id: {}", streamId);
			}
		}
	}

	public void unpublish(int streamId) { // 注销publish
		log.debug("unpublish stream {}", streamId);
		PendingCall pendingCall = new PendingCall("publish",
				new Object[] { false });
		mRTMPConnection.invoke(pendingCall, getChannelForStreamId(streamId));
	}

	public void publishStreamData(int streamId, IMessage message) { // 发布流数据
		NetStreamPrivateData streamData = mStreamDataMap.get(streamId);
		// log.debug("publishStreamData - stream data map: {}", streamDataMap);
		if (streamData != null) {
			if (streamData.connConsumer != null) {
				streamData.connConsumer.pushMessage(null, message);
			} else {
				log.warn("Connection consumer was not found for stream id: {}",
						streamId);
			}
		} else {
			log.warn("Stream data not found for stream id: {}", streamId);
		}
	}

	public void play(int streamId, String name, int start, int length) { // 播放
		log.debug("play stream {}, name: {}, start {}, length {}",
				new Object[] { streamId, name, start, length });
		if (mRTMPConnection != null) {
			Object[] params = new Object[3];
			params[0] = name;
			params[1] = start;
			params[2] = length;
			PendingCall pendingCall = new PendingCall("play", params);
			mRTMPConnection
					.invoke(pendingCall, getChannelForStreamId(streamId));
		} else {
			log.info("Connection was null ?");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void connectionOpened(RTMPConnection conn, RTMP state) { // 连接打开
		log.debug("connectionOpened");
		// Send "connect" call to the server
		Invoke invoke = createConnectPendingCall();
		log.debug("Writing 'connect' invoke: {}, invokeId: {}", invoke,
				invoke.getInvokeId());

		Channel channel = conn.getChannel((byte) 3);
		channel.write(invoke);
		super.connectionOpened(conn, state);
	}

	@Override
	public void connectionClosed(RTMPConnection conn, RTMP state) { // 连接关闭
		log.debug("connectionClosed");
		super.connectionClosed(conn, state);
		if (mConnectionClosedHandler != null) {
			mConnectionClosedHandler.run();
		}

		Invoke invoke = createConnectPendingCall();
		handlePendingCallResult(conn, invoke);
	}

	/*
	 * Commented :: CatturaVideo Set the authentication as false (default)
	 */
	boolean isAuth = false;

	/** {@inheritDoc} */
	@Override
	protected boolean onInvoke(RTMPConnection conn, Channel channel,
			Header source, Notify invoke, RTMP rtmp) { // （minaIo调用）调用
		if (invoke.getType() == IEvent.Type.STREAM_DATA) {
			log.debug("Ignoring stream data notify with header: {}", source);
			return false;
		}
		log.debug("onInvoke: {}, invokeId: {}", invoke, invoke.getInvokeId());
		final IServiceCall call = invoke.getCall();
		String methodName = call.getServiceMethodName();

		if ("_result".equals(methodName) || "_error".equals(methodName)) {
			final IPendingServiceCall pendingCall = conn.getPendingCall(invoke
					.getInvokeId());
			log.debug("Received result for pending call {}", pendingCall);
			if (pendingCall != null) {
				if ("connect".equals(pendingCall.getServiceMethodName())) {
					Integer encoding = (Integer) mConnectionParams
							.get("objectEncoding");
					if (encoding != null && encoding.intValue() == 3) {
						log.debug("Setting encoding to AMF3");
						rtmp.setEncoding(IConnection.Encoding.AMF3);
					}
				}

				handlePendingCallResult(conn, invoke);
			}

			return true;
		} else if ("onBWDone".equals(methodName)) { // BW事件完成
			onBWDone();
			return true;
		} else if ("ServerShotout".equals(methodName)) { // 被服务器踢出
			handlePendingCallResult(conn,
					createPendingCall("ServerShotout", Invoke_ServerShotout));
			return true;
		} else if ("SetAvailableFlvs".equals(methodName)) {
			handlePendingCallResult(conn,
					createPendingCall("SetAvailableFlvs", invoke));
			return true;
		} else if ("OnMessage".equals(methodName)) {
			handlePendingCallResult(conn,
					createPendingCall("OnMessage", invoke));
			return true;
		}

		// potentially used twice so get the value once
		boolean onStatus = call.getServiceMethodName().equals("onStatus");
		log.debug("onStatus {}", onStatus);
		if (onStatus) {
			// 系列化存储对象
			ObjectMap<?, ?> objMap = (ObjectMap<?, ?>) call.getArguments()[0];
			// should keep this as an Object to stay compatible with FMS3 etc
			Object clientId = objMap.get("clientid");
			log.debug("Client id at onStatus: {}", clientId);
			if (clientId == null) {
				clientId = source.getStreamId();
			}

			log.debug("Client/stream id: {}", clientId);
			if (clientId != null) {
				// try lookup by client id first
				NetStreamPrivateData streamData = mStreamDataMap.get(clientId);
				// if null try to supply the first one in the map
				if (streamData == null) {
					log.debug("Stream data map: {}", mStreamDataMap);
					streamData = mStreamDataMap.get(1);
				}
				if (streamData == null) {
					log.warn("Stream data was null for client id: {}", clientId);
				}
				if (streamData != null && streamData.handler != null) {
					streamData.handler.onStreamEvent(invoke);
				}
			}
		}

		if (mServiceProvider == null) {
			// Client doesn't support calling methods on him
			call.setStatus(Call.STATUS_METHOD_NOT_FOUND);
			call.setException(new MethodNotFoundException(call
					.getServiceMethodName()));
		} else {
			mServiceInvoker.invoke(call, mServiceProvider);
		}

		if (call instanceof IPendingServiceCall) {
			IPendingServiceCall psc = (IPendingServiceCall) call;
			Object result = psc.getResult();
			log.debug("Pending call result is: {}", result);
			if (result instanceof DeferredResult) {
				DeferredResult dr = (DeferredResult) result;
				dr.setInvokeId(invoke.getInvokeId());
				dr.setServiceCall(psc);
				dr.setChannel(channel);
				conn.registerDeferredResult(dr);
			} else if (!onStatus) {
				Invoke reply = new Invoke();
				reply.setCall(call);
				reply.setInvokeId(invoke.getInvokeId());
				log.debug("Sending empty call reply: {}", reply);
				channel.write(reply);
			}
		}

		return false;
	}

	/**
	 * Setter for codec factory
	 * 
	 * @param factory
	 *            Codec factory to use
	 */
	public void setCodecFactory(RTMPCodecFactory factory) { // 设置编码工厂
		this.mCodecFactory = factory;
	}

	/**
	 * Getter for codec factory
	 * 
	 * @return Codec factory
	 */
	public RTMPCodecFactory getCodecFactory() { // 返回编码工厂
		return this.mCodecFactory;
	}

	public void handleException(Throwable throwable) { // 处理异常
		/*
		 * Commented :: CatturaVideo To Support the exception handling
		 */
		Object type = (Object) new String(throwable.getCause().toString());
		PendingCall pendingCall = new PendingCall("Exception");
		pendingCall.setException(new Exception(throwable.getCause()));
		this.mConnectCallback.resultReceived(pendingCall);
		if (mExceptionHandler != null) {
			mExceptionHandler.handleException(throwable);
		} else {
			log.error("Connection exception", throwable);
			throw new RuntimeException(throwable);
		}
	}

	/**
	 * Returns a channel based on the given stream id.
	 * 
	 * @param streamId
	 * @return the channel for this stream id
	 */
	protected int getChannelForStreamId(int streamId) { // 获取流id
		return (streamId - 1) * 5 + 4;
	}

	/**
	 * Sets a reference to the connection associated with this client handler.
	 * 
	 * @param conn
	 */
	public void setConnection(RTMPConnection conn) { // rtmp连接
		this.mRTMPConnection = conn;
	}

	/**
	 * Returns the connection associated with this client.
	 * 
	 * @return conn
	 */
	public RTMPConnection getConnection() {
		return mRTMPConnection;
	}

	/**
	 * Setter for stream event dispatcher (useful for saving playing stream to
	 * file)
	 * 
	 * @param streamEventDispatcher
	 *            event dispatcher
	 */
	public void setStreamEventDispatcher(IEventDispatcher streamEventDispatcher) { // 事件调度
		mStreamEventDispatcher = streamEventDispatcher;
	}

	public Invoke createConnectPendingCall() {
		PendingCall pendingCall = new PendingCall("connect");
		pendingCall.setArguments(mConnectArguments);
		Invoke invoke = new Invoke(pendingCall);
		invoke.setConnectionParams(mConnectionParams);
		invoke.setInvokeId(Invoke_Connect);

		if (mConnectCallback != null) {
			pendingCall.registerCallback(mConnectCallback);
		}
		mRTMPConnection.registerPendingCall(invoke.getInvokeId(), pendingCall);
		return invoke;
	}

	public Invoke createPendingCall(String method, int invokeId) {
		PendingCall pendingCall = new PendingCall(method);
		Invoke invoke = new Invoke(pendingCall);
		invoke.setInvokeId(invokeId);

		if (mConnectCallback != null) {
			pendingCall.registerCallback(mConnectCallback);
		}
		mRTMPConnection.registerPendingCall(invoke.getInvokeId(), pendingCall);
		return invoke;
	}

	public Invoke createPendingCall(String method, Notify invoke) {
		PendingCall pendingCall = new PendingCall(method);
		if (mConnectCallback != null) {
			pendingCall.registerCallback(mConnectCallback);
		}
		mRTMPConnection.registerPendingCall(invoke.getInvokeId(), pendingCall);
		return (Invoke) invoke;
	}

	private static class NetStream extends AbstractClientStream implements
			IEventDispatcher { // 网络流
		private IEventDispatcher dispatcher;

		public NetStream(IEventDispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}

		public void close() {
			log.debug("NetStream close");
		}

		public void start() {
			log.debug("NetStream start");
		}

		public void stop() {
			log.debug("NetStream stop");
		}

		public void dispatchEvent(IEvent event) {
			// log.debug("NetStream dispatchEvent: {}", event);
			if (dispatcher != null) {
				dispatcher.dispatchEvent(event);
			}
		}
	}

	private class CreateStreamCallBack implements IPendingServiceCallback { // 建立流回调
		private IPendingServiceCallback wrapped;

		public CreateStreamCallBack(IPendingServiceCallback wrapped) {
			log.debug("CreateStreamCallBack {}", wrapped.getClass().getName());
			this.wrapped = wrapped;
		}

		public void resultReceived(IPendingServiceCall call) {
			Integer streamIdInt = (Integer) call.getResult();
			log.debug("Stream id: {}", streamIdInt);
			// RTMPConnection conn =
			// RTMPClientConnManager.getInstance().getConnection(clientId);
			// log.debug("Connection: {}", conn);
			log.debug("CreateStreamCallBack resultReceived - stream id: {}",
					streamIdInt);
			if (mRTMPConnection != null && streamIdInt != null) {
				log.debug("Setting new net stream");
				NetStream stream = new NetStream(mStreamEventDispatcher);
				stream.setConnection(mRTMPConnection);
				stream.setStreamId(streamIdInt);
				mRTMPConnection.addClientStream(stream);
				NetStreamPrivateData streamData = new NetStreamPrivateData();
				streamData.outputStream = mRTMPConnection
						.createOutputStream(streamIdInt);
				streamData.connConsumer = new ConnectionConsumer(
						mRTMPConnection, streamData.outputStream.getVideo()
								.getId(), streamData.outputStream.getAudio()
								.getId(), streamData.outputStream.getData()
								.getId());
				mStreamDataMap.put(streamIdInt, streamData);
				log.debug("streamDataMap: {}", mStreamDataMap);
			}
			wrapped.resultReceived(call);
		}
	}

	private static class NetStreamPrivateData { // 网络流
		public volatile INetStreamEventHandler handler;

		public volatile OutputStream outputStream;

		public volatile ConnectionConsumer connConsumer;
	}
}
