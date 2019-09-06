package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import org.apache.log4j.Logger;

/**
 * 下载完成通知应答消息
 * 0x9B02
 */
public class UpDownloadMsgEndInformAck implements IReceiveProtocol {
	Logger logger = Logger.getLogger(UpDownloadMsgEndInformAck.class);

	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();

		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));
		int subType = message.getSubType();

		int result = mp.getInt(1);
		int responseMsgId = mp.getInt(2);
		return "";
	}
}
