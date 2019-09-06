package com.ltmonitor.jt809.protocol.receive;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;


public class DownExgMsgCarInfo implements IReceiveProtocol {
	private static Logger logger = Logger.getLogger(DownExgMsgCarInfo.class);

	public String handle(JT809Message message) {
		String outPut = "";

		String dataBody = message.getMessageBody();

		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		int dataLength = mp.getInt(4);
		String vehicleInfo = mp.getString(dataLength);
		message.setMsgDescr(vehicleInfo);

		return null;
	}
}
