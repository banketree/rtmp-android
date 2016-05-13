package com.eotu.rtmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.red5.server.event.IEvent;
import org.red5.server.event.IEventDispatcher;
import org.red5.server.messaging.IMessage;
import org.red5.server.net.rtmp.INetStreamEventHandler;
import org.red5.server.net.rtmp.RTMPClient;
import org.red5.server.net.rtmp.codec.RTMP;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.service.IPendingServiceCall;
import org.red5.server.service.IPendingServiceCallback;
import org.red5.server.service.PendingCall;
import org.red5.server.stream.message.RTMPMessage;

import com.eotu.core.CoreEvent;
import com.treecore.TBroadcastByInner;
import com.treecore.filepath.TFilePathManager;
import com.treecore.utils.log.TLog;
import android.content.Context;
import android.util.Log;

public class CoreRtmpClient extends RtmpConfig implements
		INetStreamEventHandler, IPendingServiceCallback, IEventDispatcher {
	private RTMPClient mRtmpClient;
	private static CoreRtmpClient mThis;
	private Context mContext;
	private int mStreamId;
	private List<String> mServiceFlvs;
	private MembersShareObject mMembersShareObject;

	public CoreRtmpClient() {
	}

	public static CoreRtmpClient getInstance() {
		if (mThis == null)
			mThis = new CoreRtmpClient();
		return mThis;
	}

	public void initConfig(Context context) {
		mContext = context;
		initConfig();
	}

	@Override
	public void initConfig() {
		super.initConfig();

		mRtmpClient = new RTMPClient();
		mRtmpClient.setStreamEventDispatcher(this);
		mMembersShareObject = new MembersShareObject();
	}

	@Override
	public void release() {
		super.release();
		stopRtmp();
	}

	@Override
	public void resultReceived(IPendingServiceCall call) {// 返回信息
		String serviceMethodString = call.getServiceMethodName();
		if ("connect".equals(serviceMethodString)) { // 连接状态
			TBroadcastByInner.sentEvent(mContext, CoreEvent.RtmpEvent,
					RtmpEvent.Connect);

			if (isRtmpConnected()) {
				mRtmpClient.createStream(this); // 建立流
				mMembersShareObject.initConfig();
			}
		} else if ("Exception".equals(serviceMethodString)) {// 连接服务器异常
			mMembersShareObject.release();
			TBroadcastByInner.sentPostEvent(mContext, CoreEvent.RtmpEvent,
					RtmpEvent.Exception, 2);
		} else if ("ServerShotout".equals(serviceMethodString)) {// 被服务器踢出
			TBroadcastByInner.sentEvent(mContext, CoreEvent.RtmpEvent,
					RtmpEvent.ServerShotout);
		} else if ("createStream".equals(serviceMethodString)) {
			Object result = call.getResult();
			if (result instanceof Integer) {
				Integer streamIdInt = (Integer) result;
				mStreamId = streamIdInt.intValue();
			} else {
				TBroadcastByInner.sentPostEvent(mContext, CoreEvent.RtmpEvent,
						RtmpEvent.Exception, 2);
				stopRtmp();
			}
		} else if ("SetAvailableFlvs".equals(serviceMethodString)) {
			mServiceFlvs = ((ArrayList<String>) (call.getResult()));
			TBroadcastByInner.sentEvent(mContext, CoreEvent.RtmpEvent,
					RtmpEvent.SetAvailableFlvs);
		} else if ("OnMessage".equals(serviceMethodString)) {
			Object result = call.getResult();
			HashMap<String, Object> reHashMap = (HashMap<String, Object>) call
					.getResult();

			String from = (String) reHashMap.get("sender");
			String messageString = (String) reHashMap.get("message");
			Date timeDate = (Date) reHashMap.get("time");
			String to = (String) reHashMap.get("clientname");
			TBroadcastByInner.sentEvent(mContext, CoreEvent.RtmpEvent,
					RtmpEvent.ChatMessage, result.toString());
		}
	}

	@Override
	public void onStreamEvent(Notify notify) { // 事件流
		Log.i("", "onStreamEvent");
	}

	@Override
	public void dispatchEvent(IEvent event) {
		Log.i("", "dispatchEvent");
	}

	@SuppressWarnings("rawtypes")
	public void onStatus(Object obj) {
		Log.i("", "onStatus");
	}

	public RTMPClient getRtmpClient() {
		return mRtmpClient;
	}

	public void pushMessage(IMessage message) throws IOException {
		// if (mPlayerStatus != RtmpPlayerStatus.Playing
		// && message instanceof RTMPMessage) {
		RTMPMessage rtmpMsg = (RTMPMessage) message;
		mRtmpClient.publishStreamData(mStreamId, rtmpMsg);
		// } else {
		// mFrameBuffer.add(message);
		// }
	}

	public void startRtmp(String... params) {// 连接rtmp
		stopRtmp();

		Map<String, Object> defParams = mRtmpClient
				.makeDefaultConnectionParams(getHost(), getPort(), getAppName());

		TBroadcastByInner.sentPostEvent(mContext, CoreEvent.RtmpEvent,
				RtmpEvent.Connect, 1);
		mRtmpClient.connect(getHost(), getPort(), defParams, this, params);
	}

	public void stopRtmp() { // 停止rtmp
		try {
			mMembersShareObject.release();
			mRtmpClient.clearSharedObject();
			if (isRtmpConnecting() || isRtmpConnected()) {
				mRtmpClient.disconnect();
			}
		} catch (Exception e) {
		}
	}

	public boolean isRtmpConnecting() {// 正在连接
		if (mRtmpClient.getConnection() == null)
			return false;

		return mRtmpClient.getConnection().getState().getState() == RTMP.STATE_HANDSHAKE;
	}

	public boolean isRtmpConnected() { // 已连接
		if (mRtmpClient.getConnection() == null)
			return false;

		return mRtmpClient.getConnection().getState().getState() == RTMP.STATE_CONNECTED;
	}

	public boolean isRtmpDisconnected() { // 已断开
		if (mRtmpClient.getConnection() == null)
			return true;

		return mRtmpClient.getConnection().getState().getState() == RTMP.STATE_DISCONNECTED
				|| mRtmpClient.getConnection().getState().getState() == RTMP.STATE_DISCONNECTING;
	}

	public void onGetAvailableFlvs() {
		try {
			mRtmpClient.invoke("getAvailableFlvs", this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 第一参数内容（公聊）；第二个参数对象名（私聊）。（可传一个参数）
	public void sendMessage(Object... params) { // 发送消息
		TLog.i(this, "sendMessage " + params);
		if (params == null || params.length == 0)
			return;

		// Object[] params = new Object[content.length];
		// for (int i = 0; i < content.length; i++) {
		// params[i] = content[i];
		// }

		PendingCall pendingCall = new PendingCall("chatMessage", params);
		// mRtmpClient.invoke(pendingCall, getChannelForStreamId(mStreamId));
		mRtmpClient.invoke("chatMessage", params, this);
	}

	public List<String> getAvailableFlvs() {
		return mServiceFlvs;
	}

	public String getFileDir() {
		return TFilePathManager.getInstance().getCachePath();
	}

	public MembersShareObject getMembersShareObject() {
		return mMembersShareObject;
	}

	public Context getContext() {
		return mContext;
	}
}
