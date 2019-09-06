package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.entity.TerminalCommand;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.service.JT808Constants;
import com.ltmonitor.video.entity.VideoServerConfig;
import org.apache.log4j.Logger;

/**
 * 实时音视频停止请求消息
 * 链路：从链路
 * msgType = 0x9800 subType = 0x9802
 */
public class DownRealVideoMsgEnd implements IReceiveProtocol {
	protected Logger logger = Logger.getLogger(getClass());
	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);


		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		message.setContentLength(mp.getInt(4));

		int channelId = mp.getInt(1);
		int videoDataType = mp.getInt(1);
		//String code = mp.getString(64);
		TerminalCommand tc = new TerminalCommand();
		tc.setCmdType(JT808Constants.CMD_REALTIME_VIDEO_STOP);
		int vdeoDataType = videoDataType;
		StringBuilder sb = new StringBuilder();
		byte controlCommand = 0;
		byte streamType = 0;
		sb.append(channelId).append(';').append(controlCommand).append(';').append(videoDataType).append(';').append(streamType);

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


	private VideoServerConfig getVideoServerConfig()
	{
		return ServiceLauncher.getVideoServerConfigService().getVideoServerConfig();
	}

}
