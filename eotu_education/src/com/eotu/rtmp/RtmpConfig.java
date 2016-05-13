package com.eotu.rtmp;

import com.treecore.TIGlobalInterface;

public class RtmpConfig implements TIGlobalInterface {

	public enum RtmpPlayerStatus {
		Stopped, // 停止
		Playing, // 播放中
		StreamCreating // 建立流中
	}

	protected String mHost;
	protected int mPort;
	protected String mAppName;
	protected int mConnectStatus;
	protected String mPublishName;
	protected String mPublishMode;
	protected int mStreamId;

	public RtmpConfig() {
	}

	@Override
	public void initConfig() {
	}

	@Override
	public void release() {
	}

	public void setHost(String host) {
		mHost = host;
	}

	public String getHost() {
		return mHost;
	}

	public int getPort() {
		return mPort;
	}

	public void setPort(int port) {
		mPort = port;
	}

	public void setAppName(String appName) {
		mAppName = appName;
	}

	public String getAppName() {
		return mAppName;
	}

	public void setConnectStatus(int connectStatus) {
		mConnectStatus = connectStatus;
	}

	public int getConnectStatus() {
		return mConnectStatus;
	}

	public void setPublishName(String publishName) {
		mPublishName = publishName;
	}

	public String getPublishName() {
		return mPublishName;
	}

	public void setPublishMode(String publishMode) {
		mPublishMode = publishMode;
	}

	public String getPublishMode() {
		return mPublishMode;
	}

	// rtmp://192.168.1.126/vod/xiaoting.flv
	public String getServerUrl() {
		return "rtmp://" + getHost() + "/" + getAppName();
	}
}
