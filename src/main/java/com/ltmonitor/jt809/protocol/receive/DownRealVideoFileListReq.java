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
 * msgType = 0x9900 subType = 0x9902
 */
public class DownRealVideoFileListReq implements IReceiveProtocol {
	protected Logger logger = Logger.getLogger(getClass());
	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));

		int channelId = mp.getInt(1);

		Date startDate = mp.getUtcDate();
		Date endDate = mp.getUtcDate();
		long alarmType = mp.getLong();
		int videoDataType = mp.getInt(1);
		int streamType = mp.getInt(1);
		int storeType = mp.getInt(1);

		String code = mp.getString(64);
		GnssData gd = getGnssData(mp);

		TerminalCommand tc = new TerminalCommand();
		tc.setCmdType(JT808Constants.CMD_SEARCH_VIDEO_RESOURCE);

		StringBuilder sb = new StringBuilder();
		sb.append(channelId).append(';')
				.append(DateUtil.datetimeToString(startDate)).append(';')
				.append(DateUtil.datetimeToString(endDate)).append(';')
				.append(alarmType).append(';').append(videoDataType).append(';')
				.append(streamType).append(';').append(storeType);
		tc.setCmdData(sb.toString());
		tc.setPlateNo(message.getPlateNo());
		tc.setOwner(TerminalCommand.FROM_GOV);
		try {
			ServiceLauncher.getTerminalCommandService().save(tc);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		message.setMsgDescr(tc.getCmdData());
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
