package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.vo.AdasAlarmProcessResult;
import org.apache.log4j.Logger;

/**
 * 苏标协议 上报报警处理结果信息 1403
 * @author DELL
 *
 */
public class UpWarnMsgOperationInfo_Subiao implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpWarnMsgOperationInfo_Subiao.class);
	private String plateNo;
	private int plateColor;
	private long infoId;
	private int result;

	private int msgType = 0x1400;
	private int subType = 0x1403;
	private AdasAlarmProcessResult adasAlarmProcessResult;
	public UpWarnMsgOperationInfo_Subiao(AdasAlarmProcessResult r) {
		this.plateNo = plateNo;
		this.plateColor = plateColor;
		adasAlarmProcessResult = r;
	}
	

	public JT809Message wrapper() {

		StringBuilder sb = new StringBuilder();
		sb.append(Tools.ToHexString(adasAlarmProcessResult.getInfoId(), 32))
				.append(Tools.ToHexString(adasAlarmProcessResult.getResult(), 1))
				.append(Tools.ToHexString(adasAlarmProcessResult.getProcessMethod(), 1))
				.append(Tools.ToHexStringWithLengthHex(adasAlarmProcessResult.getOperator()))
				.append(Tools.ToHexStringWithLengthHex(adasAlarmProcessResult.getOperatorCompany()));

		StringBuilder sbPacket = new StringBuilder();
		int dataLength = sb.toString().length() / 2;
		sbPacket.append(Tools.ToHexString(plateNo, 21))
				.append(Tools.ToHexString(plateColor, 1))
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4)).append(sb);

		StringBuilder descr = new StringBuilder();
		descr.append(adasAlarmProcessResult.getInfoId()).append(",")
				.append(adasAlarmProcessResult.getResult()).append(",")
				.append(adasAlarmProcessResult.getProcessMethod()).append(",")
				.append(adasAlarmProcessResult.getOperator()).append(",")
				.append(adasAlarmProcessResult.getOperatorCompany());

		String body = sbPacket.toString();
		JT809Message m = new JT809Message(msgType, subType, body);
		m.setMsgDescr(descr.toString());
		return m;
	}

	
}
