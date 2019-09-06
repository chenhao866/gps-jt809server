package com.ltmonitor.jt809.protocol.send;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.entity.CheckRecord;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;

/**
 * 查岗应答
 *
 */
public class UpPlatFormMsgPostQueryAck implements ISendProtocol {
	private static Logger logger = Logger
			.getLogger(UpPlatFormMsgPostQueryAck.class);
	private CheckRecord chagang;
	private int msgType = 0x1300;
	private int subType = 0x1301;

	public UpPlatFormMsgPostQueryAck(CheckRecord pm) {
		this.chagang = pm;

	}

	public JT809Message wrapper() {
		String content = Tools.ToHexString(chagang.getMessage());
		int infoLength = content.length() / 2;
		int dataLength = 1 + 12 + 4 + 4 + infoLength;
		StringBuilder sb = new StringBuilder();
		String objId = chagang.getObjId();
		if(chagang.getObjType() == 3 || chagang.getObjType() == 2)
		{
			objId = GlobalConfig.parModel.getLicenseNo();
		}else
			objId = ""+GlobalConfig.parModel.getPlatformCenterId();
		sb.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4))
				.append(Tools.ToHexString(chagang.getObjType(), 1))
				.append(Tools.ToHexString(objId, 12))
				.append(Tools.ToHexString(chagang.getInfoId(), 4))
				.append(Tools.ToHexString(infoLength, 4)).append(content);
		String body = sb.toString();
		JT809Message msg =  new JT809Message(msgType,  subType,body);
		msg.setMsgDescr(chagang.getMessage());
		return msg;
	}
}
