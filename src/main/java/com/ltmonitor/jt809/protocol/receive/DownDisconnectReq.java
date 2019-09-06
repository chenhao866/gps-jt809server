package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.jt809.app.GlobalConfig;
import org.apache.log4j.Logger;

import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
/**
 * 
 * 从链路注销请求
 */
public class DownDisconnectReq implements IReceiveProtocol {
	private static Logger logger = Logger.getLogger(DownDisconnectReq.class);

	public String handle(JT809Message message) {
		String messageBody = message.getMessageBody();

		try {
			//int verifyCode = Integer.parseInt(messageBody.substring(0, 8));
			MessageParser mp = new MessageParser(messageBody);
			int verifyCode = mp.getInt(4);
			message.setMsgDescr("" + verifyCode);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			logger.error(ex.getStackTrace());
		}

		T809Manager.DownDisconnectRsp();

		T809Manager.setSubLinkState(false, "上级平台主动注销");

		GlobalConfig.normalDisconnect = true;
		return null;
	}
}
