package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.jt809.app.GlobalConfig;
import org.apache.log4j.Logger;

import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
/**
 * 从链路连接请求
 * @author DELL
 *
 */
public class DownConnectReq implements IReceiveProtocol {
	private Logger logger = Logger.getLogger(DownConnectReq.class);

	public String handle(JT809Message message) {
		String strResult = message.getMessageBody();
		MessageParser mp = new MessageParser(strResult);
		try {
			int result = mp.getInt(4);
			// int result = Integer.parseInt(strResult.substring(0, 8));
			message.setMsgDescr("从链路连接成功,校验码:" + result);

			T809Manager.UpAuthorizeMsgStartUp();
			this.logger.warn("从链路连接成功,校验码:" + result);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			logger.error(ex.getStackTrace());
		}

		// 从链路连接应答消息
		T809Manager.DownConnectRsp();

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		T809Manager.setSubLinkState(true, "从链路连接成功");
		GlobalConfig.normalDisconnect = false;
		return null;
	}
}
