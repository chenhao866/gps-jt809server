package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.entity.TerminalCommand;
import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.service.JT808Constants;
import com.ltmonitor.util.GovPlatformVideoAckResult;
import com.ltmonitor.video.entity.VideoServerConfig;
import org.apache.log4j.Logger;

import javax.xml.ws.Service;

/**
 * 实时音视频请求消息
 * 链路：从链路
 * msgType = 0x9800 subType = 0x9801
 */
public class DownRealVideoMsgStartUp implements IReceiveProtocol {
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
		String code = mp.getString(64);

		String authorizeCode = GlobalConfig.authorizeCode;//ServiceLauncher.getPlateformState().getAuthorizeCode1();
		//判断时效口令是否正确
		if(code.equals(authorizeCode) == false)
		{
			logger.error("口令不对,code:" + code + ", authorizeCoe:" + authorizeCode);
			T809Manager.UpRealVideoMsgStartUpAck(message.getPlateNo(), message.getPlateColor(), GovPlatformVideoAckResult.AUTHORIZE_CODE_INCORRECT,
					null,0);
			return "";
		}
		TerminalCommand tc = new TerminalCommand();
		tc.setCmdType(JT808Constants.CMD_REALTIME_VIDEO_REQ);

		String videoServerIp = this.getVideoServerConfig().getVideoServerIp();
		int tcpPort = this.getVideoServerConfig().getVideoServerTcpPort();
		int udpPort = this.getVideoServerConfig().getVideoServerUdpPort();
		int stream = 0;
		int vdeoDataType = videoDataType;
		StringBuilder sb = new StringBuilder();
		sb.append(videoServerIp).append(';').append(tcpPort).append(';')
				.append(udpPort).append(';').append(channelId).append(';')
				.append(vdeoDataType).append(';').append(stream);

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
