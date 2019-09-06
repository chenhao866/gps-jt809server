package com.ltmonitor.jt809.protocol.send;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.server.PlatformClient;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.vo.WarnData;

/**
 * 上报报警处理结果信息 1403
 * @author DELL
 *
 */
public class UpWarnMsgAdptToDoInfo implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpWarnMsgAdptToDoInfo.class);
	private String plateNo;
	private int plateColor;
	private long infoId;
	private int result;

	private int msgType = 0x1400;
	private int subType = 0x1403;
	public UpWarnMsgAdptToDoInfo(String plateNo, int plateColor, long infoId, int result) {
		this.plateNo = plateNo;
		this.plateColor = plateColor;
		this.infoId = infoId;
		this.result = result;
	}
	

	public JT809Message wrapper() {
		int dataLength = 1  +4 ; 

		StringBuilder sb = new StringBuilder();
		sb.append(Tools.ToHexString(plateNo, 21))
				.append(Tools.ToHexString(plateColor, 1))
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4))
				.append(Tools.ToHexString(infoId, 4))
				.append(Tools.ToHexString(result, 1))
				;

		ParameterModel pm = GlobalConfig.parModel;
		String body = sb.toString();
		return new JT809Message(msgType,  subType,body);
	}

	
}
