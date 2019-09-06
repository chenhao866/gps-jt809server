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
 * 上报报警信息 1402
 * @author DELL
 *
 */
public class UpWarnMsgAdptInfo implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpWarnMsgAdptInfo.class);
	private WarnData wd;

	private int msgType = 0x1400;
	private int subType = 0x1402;
	public UpWarnMsgAdptInfo(WarnData wd) {
		this.wd = wd;
	}
	

	public JT809Message wrapper() {
		String content = Tools.ToHexString(wd.getContent());
		int infoLength = content.length() / 2;
		int dataLength = 1 + 2 + 8 + 4 + 4 + infoLength;

		StringBuilder sb = new StringBuilder();
		sb.append(Tools.ToHexString(wd.getPlateNo(), 21))
				.append(Tools.ToHexString(wd.getPlateColor(), 1))
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4))
				.append(Tools.ToHexString(wd.getSrc(), 1))
				.append(Tools.ToHexString(wd.getType(), 2))
				.append(Tools.getUTC(wd.getWarnTime()))
				.append(Tools.ToHexString(wd.getInfoId(), 4))
				.append(Tools.ToHexString(infoLength, 4))
				.append(content)
				;

		ParameterModel pm = GlobalConfig.parModel;
		String body = sb.toString();
		JT809Message mm = new JT809Message(msgType,  subType,body);
		mm.setPlateNo(wd.getPlateNo());
		mm.setPlateColor(wd.getPlateColor());
		return mm;
	}

	
}
