package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;

/**
 * 上级平台关闭主从链路通知消息 9008
 * 上级平台发现主链路异常的时候，通过从链路发送此消息，通知下级平台即将关闭主从链路
 */
public class DownCloselinkInform implements IReceiveProtocol {
	public String handle(JT809Message message) {

		return null;
	}


}
