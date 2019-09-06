package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.util.AlarmTypeConstants;
import com.ltmonitor.vo.AdasWarnStaticsItem;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 5.2.3.4 报警统计核查请求应答
 链路类型：主链路。
 消息方向：下级平台往上级平台。
 业务类型标识：UP_WARN_MSG_STATICS_ACK。（0x1406）
 描述：下级平台向上级平台响应上报主动安全报警统计核查请求业务，其数据体定义见
 表 45。
 * @author www.jt808.com
 *
 */
public class UpWarnMsgStaticsAck_Subiao implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpWarnMsgStaticsAck_Subiao.class);
	private String plateNo;
	private int plateColor;

	private int msgType = 0x1400;
	private int subType = 0x1406;

	private  List<AdasWarnStaticsItem>  warnStaticsItemList;

	public UpWarnMsgStaticsAck_Subiao(String _plateNo, int _plateColor, List<AdasWarnStaticsItem> wd) {

		plateNo = _plateNo;
		plateColor = _plateColor;
		this.warnStaticsItemList = wd;
	}
	

	public JT809Message wrapper() {
		StringBuilder sb = new StringBuilder();
		StringBuilder descr = new StringBuilder();
		for(AdasWarnStaticsItem s : warnStaticsItemList)
		{
			int jt809WarnType = AlarmTypeConstants.convertSubiao809AlarmType(s.getAlarmType(), s.getAlarmSource());
			sb.append(Tools.ToHexString(jt809WarnType, 2))
					.append(Tools.ToHexString(s.getCount(), 4));
			if(descr.length() > 0)
				descr.append(";");
			descr.append(s.toString());
		}
		int dataLength = sb.length() / 2;

		StringBuilder sbPacket = new StringBuilder();
		sbPacket.append(Tools.ToHexString(plateNo, 21))
				.append(Tools.ToHexString(plateColor, 1))
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4))
				.append(sb)
				;

		String body = sbPacket.toString();
		JT809Message mm = new JT809Message(msgType,  subType,body);
		mm.setPlateNo(plateNo);
		mm.setPlateColor(plateColor);
		mm.setMsgDescr(descr.toString());
		return mm;
	}

	
}
