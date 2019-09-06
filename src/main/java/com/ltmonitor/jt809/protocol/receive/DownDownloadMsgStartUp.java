package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.entity.TerminalCommand;
import com.ltmonitor.entity.VehicleData;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.service.JT808Constants;
import com.ltmonitor.util.DateUtil;
import com.ltmonitor.video.entity.VideoFileItem;
import com.ltmonitor.video.entity.VideoServerConfig;
import com.ltmonitor.vo.GnssData;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * 远程录像下载请求消息
 * 链路：从链路
 * msgType = 0x9B00 subType = 0x9B01
 */
public class DownDownloadMsgStartUp implements IReceiveProtocol {
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
		int fileSize = mp.getInt(4);

		String code = mp.getString(64);
		GnssData gd = getGnssData(mp);

		TerminalCommand tc = new TerminalCommand();
		tc.setCmdType(JT808Constants.CMD_DOWNLOAD_VIDEO);

		String ftpServerIp = this.getVideoServerConfig().getFtpServerIp();
		int ftpServerPort = this.getVideoServerConfig().getFtpPort();
		String ftpUserName = this.getVideoServerConfig().getFtpUserName();
		String password = this.getVideoServerConfig().getFtpPassword();

		VehicleData vd = ServiceLauncher.getVehicleDataService().getVehicleByPlateNo(message.getPlateNo());
		String simNo = "13300000003";
		if(vd != null) {
			simNo = vd.getSimNo();
			tc.setSimNo(simNo);
			tc.setVehicleId(vd.getEntityId());
		}
		String subDirectory = simNo + "_" + channelId + "_" + videoDataType + "_" + DateUtil.toStringByFormat(new Date(),"yyMMddHHmmss");
		String ftpFilePath = subDirectory;

		int condition = 7;//下载条件
		StringBuilder sb = new StringBuilder();
		sb.append(ftpServerIp).append(';').append(ftpServerPort).append(';').append(ftpUserName).append(';').append(password).append(';')
				.append(ftpFilePath).append(';')
				.append(channelId).append(';')
				.append(DateUtil.datetimeToString(startDate)).append(';')
				.append(DateUtil.datetimeToString(endDate)).append(';')
				.append(alarmType).append(';').append(videoDataType).append(';')
				.append(streamType).append(';').append(storeType).append(';').append(condition);
		tc.setCmdData(sb.toString());
		tc.setPlateNo(message.getPlateNo());
		tc.setOwner(TerminalCommand.FROM_GOV);
		try {
			ServiceLauncher.getTerminalCommandService().save(tc);

			VideoFileItem vi = new VideoFileItem();
			vi.setAlarmStatus(alarmType);
			vi.setChannelId((byte) channelId);
			vi.setDataType((byte) videoDataType);
			vi.setStartDate(startDate);
			vi.setEndDate(endDate);
			vi.setStoreType((byte) storeType);
			vi.setStreamType((byte) streamType);
			vi.setCommandId(tc.getEntityId());
			vi.setVehicleId(tc.getVehicleId());
			vi.setPlateNo(tc.getPlateNo());
			vi.setFileSource(VideoFileItem.FROM_FTP);
			vi.setFilePath(ftpFilePath);
			ServiceLauncher.getVideoFileItemService().save(vi);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

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
