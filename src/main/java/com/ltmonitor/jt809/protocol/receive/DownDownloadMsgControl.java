package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.entity.TerminalCommand;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.service.JT808Constants;
import com.ltmonitor.util.DateUtil;
import com.ltmonitor.video.entity.VideoServerConfig;
import com.ltmonitor.vo.GnssData;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * 查询音视频资源目录请求消息
 * 链路：从链路
 * msgType = 0x9A00 subType = 0x9A02
 */
public class DownDownloadMsgControl implements IReceiveProtocol {
	protected Logger logger = Logger.getLogger(getClass());
	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));

		int sessionId = mp.getInt(2);
		int controlType = mp.getInt(1);

		TerminalCommand tc = new TerminalCommand();
		tc.setCmdType(JT808Constants.CMD_DOWNLOAD_VIDEO_CONTROL);

		StringBuilder sb = new StringBuilder();
		sb.append(sessionId).append(";").append(controlType);
		tc.setCmdData(sb.toString());
		tc.setPlateNo(message.getPlateNo());
		tc.setOwner(TerminalCommand.FROM_GOV);
		try {
			ServiceLauncher.getTerminalCommandService().save(tc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		StringBuilder descr = new StringBuilder();
		descr.append("文件上传消息流水号:").append(sessionId).append(",控制类型:").append(controlType);
		message.setMsgDescr(descr.toString());

		return "";
	}

	private  GnssData getGnssData(MessageParser mp)
	{
		GnssData gd = new GnssData();
		gd.setPosEncrypt(mp.getInt(1));
		String dateSTr = mp.getString(7);
		gd.setLongitude(mp.getInt(4));
		gd.setLatitude(mp.getInt(4));
		gd.setGpsSpeed(mp.getInt(2));
		gd.setRecSpeed(mp.getInt(2));
		gd.setTotalMileage(mp.getInt(4));
		gd.setDirection(mp.getInt(2));
		gd.setAltitude(mp.getInt(2));
		gd.setVehicleState(mp.getInt(4));
		gd.setAlarmState(mp.getInt(4));
		return gd;
	}


	private VideoServerConfig getVideoServerConfig()
	{
		return ServiceLauncher.getVideoServerConfigService().getVideoServerConfig();
	}

}
