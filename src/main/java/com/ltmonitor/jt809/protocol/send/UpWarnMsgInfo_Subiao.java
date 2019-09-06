package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.vo.AdasWarnData;
import com.ltmonitor.vo.WarnData;
import org.apache.log4j.Logger;

/**
 * 上报报警信息 1402
 * @author DELL
 *
 */
public class UpWarnMsgInfo_Subiao implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpWarnMsgInfo_Subiao.class);
	private AdasWarnData wd;

	private int msgType = 0x1400;
	private int subType = 0x1402;
	public UpWarnMsgInfo_Subiao(AdasWarnData wd) {
		this.wd = wd;
	}
	

	public JT809Message wrapper() {

		int longitude = (int)(wd.getLongitude() * 1000000);
		int latitude = (int)(wd.getLatitude() * 1000000);
		StringBuilder sb = new StringBuilder();
		sb.append(Tools.ToHexString(wd.getSrc(), 1))
				.append(Tools.ToHexString(wd.getType(), 2))
				.append(Tools.getUTC(wd.getWarnTime()))
				.append(Tools.ToHexString(wd.getInfoId(), 32))
				.append(Tools.ToHexStringWithLengthHex(wd.getDriverName()))
				.append(Tools.ToHexStringWithLengthHex(wd.getDriverLicenseNo()))
				.append(Tools.ToHexString(wd.getAlarmLevel(), 1))
				.append(Tools.ToHexString(longitude, 4))
				.append(Tools.ToHexString(latitude, 4))
				.append(Tools.ToHexString(wd.getAltitude(), 2))
				.append(Tools.ToHexString(wd.getGpsSpeed(), 2))
				.append(Tools.ToHexString(wd.getRecSpeed(), 2))
				.append(Tools.ToHexString(wd.getAlarmStatus(), 1))
				.append(Tools.ToHexString(wd.getDirection(), 2))
				.append(Tools.ToHexStringWithLengthHex(wd.getContent(), 2));
		int dataLength = sb.toString().length() / 2;

		StringBuilder sbPacket = new StringBuilder();
		sbPacket.append(Tools.ToHexString(wd.getPlateNo(), 21))
				.append(Tools.ToHexString(wd.getPlateColor(), 1))
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4)).append(sb.toString());

		String body = sbPacket.toString();
		JT809Message mm = new JT809Message(msgType,  subType,body);
		mm.setPlateNo(wd.getPlateNo());
		mm.setPlateColor(wd.getPlateColor());
		mm.setMsgDescr(wd.toString());
		return mm;
	}

	
}
