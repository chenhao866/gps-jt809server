package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
/**
 * 主链路断开通知消息 1007
 * 当主链路中断后，下级平台可通过从链路发送此消息，告知上级平台主链路断开了
 */
public class UpDisconnectInform implements ISendProtocol {
	private int msgType = 0x1007;
	
	public UpDisconnectInform()
	{
		
	}

	public JT809Message wrapper() {
		String dataBody = null;

		String error_code = "00"; //错误代码 0： 主链路断开 1：其他原因
		String mess = "";
		dataBody = error_code;

		return new JT809Message(msgType, dataBody);
	}
}
