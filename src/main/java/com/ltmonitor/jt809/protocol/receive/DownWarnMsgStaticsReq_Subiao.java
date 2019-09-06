package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.util.DateUtil;
import com.ltmonitor.vo.AdasWarnStaticsItem;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * 报警统计核查请求消息
 链路类型：从链路
 消息方向：上级平台向下级平台
 业务类型标识： DOWN_WARN_MSG_STATICS_REQ。（0x9406）
 描述: 上级平台向下级平台发送主动安全报警统计核查请求业务，其数据体定义见表 44。
 */
public class DownWarnMsgStaticsReq_Subiao implements IReceiveProtocol {
	protected Logger logger = Logger.getLogger(getClass());
	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));

		Date startDate = mp.getUtcDate();
		Date endDate = mp.getUtcDate();

		StringBuilder sb = new StringBuilder();
		sb.append("统计时间：").append(DateUtil.datetimeToString(startDate)).append("至").append(DateUtil.datetimeToString(endDate));
		message.setMsgDescr(sb.toString());

		List<AdasWarnStaticsItem> res = ServiceLauncher.getAlarmStatisticService().getAdasWarnStaticsItemList(message.getPlateNo(), startDate, endDate);
		T809Manager.UpWarnMsgStaticsAck_Subiao(message.getPlateNo(), message.getPlateColor(), res);
		return "";
	}


}
