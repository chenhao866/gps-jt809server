package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;

/**
 * 主动上传音视频资源请求应答
 * 0x9901
 */
public class DownFileListMsgAck implements IReceiveProtocol {
	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));

		int result = mp.getInt(1);

		int itemNumber = mp.getInt(1);

		return "";
	}
}
