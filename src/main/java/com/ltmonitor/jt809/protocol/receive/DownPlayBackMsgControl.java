package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.entity.TerminalCommand;
import com.ltmonitor.jt809.app.GlobalConfig;
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
 * 视频回放控制指令
 * 链路：从链路
 * msgType = 0x9A00 subType = 0x9A02
 */
public class DownPlayBackMsgControl implements IReceiveProtocol {
	protected Logger logger = Logger.getLogger(getClass());
	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);

		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));

		int playbackType = mp.getInt(1);
		int speed = mp.getInt(1);

		int channelId = 0;
		if(GlobalConfig.playBackChannelMap.containsKey(message.getPlateNo()))
		{
			channelId = GlobalConfig.playBackChannelMap.get(message.getPlateNo());
		}

		Date playBackPos = mp.getUtcDate();

		TerminalCommand tc = new TerminalCommand();
		tc.setCmdType(JT808Constants.CMD_VIDEO_PLAY_BACK_CONTROL);

		StringBuilder sb = new StringBuilder();
		sb.append(channelId).append(";").append(playbackType).append(";").append(speed).append(";")
				.append(DateUtil.toStringByFormat(playBackPos, "yyyy-MM-dd HH:mm:ss"));
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
