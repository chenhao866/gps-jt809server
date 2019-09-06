package com.ltmonitor.jt809.protocol.receive;

import java.util.Date;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.util.DateUtil;


/**
 * 接收车辆定位消息数量通知 9101，不需要应答,可以入库
 * 
 * @author Administrator
 * 
 */
public class DownTotalRecvBackMsg implements IReceiveProtocol {
	private Logger logger = Logger.getLogger(DownTotalRecvBackMsg.class);

	public String handle(JT809Message message) {

		this.logger.info("ResolveHandle start");

		String body = message.getMessageBody();

		MessageParser mp = new MessageParser(body);
		int dynamicInfoTotal = mp.getInt(4);

		String desc = "收到定位消息数量:" + dynamicInfoTotal;
		try {
			Date start = mp.getUtcDate();
			Date end = mp.getUtcDate();
			desc += ",时间段:"
					+ DateUtil.toStringByFormat(start, "yyyy-MM-dd HH:mm:ss")
					+ " 至 "
					+ DateUtil.toStringByFormat(end, "yyyy-MM-dd HH:mm:ss");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		message.setMsgDescr(desc);
		return null;
	}
}
