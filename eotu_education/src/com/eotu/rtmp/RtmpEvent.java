package com.eotu.rtmp;

import com.eotu.core.CoreEvent;

public class RtmpEvent {
	private static int RtmpEvent = CoreEvent.RtmpEvent;
	public static int Connect = RtmpEvent + 1; // 连接
	public static int Exception = RtmpEvent + 2; // 异常
	public static int ServerShotout = RtmpEvent + 3; // 服务器踢出
	public static int SetAvailableFlvs = RtmpEvent + 4; // 获取服务器在线列表
	public static int ChatMessage = RtmpEvent + 5; // 聊天
	public static int MembersUpdate = RtmpEvent + 6; // 成员列表更新

}
