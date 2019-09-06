package com.ltmonitor.jt809.protocol.send;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;

/**
 * 
 * 下级平台主动关闭主从链路通知消息 1008
 */
public class UpCloseLinkInform implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpCloseLinkInform.class);
	
	public UpCloseLinkInform()
	{
		
	}

	public JT809Message wrapper() {
		int reasonCode = 0; //关闭原因 0: 网关重启 1：其他原因
		String turnResonCode = Integer.toHexString(reasonCode);
		String endResonCode = Tools.turnDataLength(turnResonCode, 2);

		String dataBody = endResonCode;
		return new JT809Message(0x1008, dataBody);
	}
}
