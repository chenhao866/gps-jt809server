package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.adas.entity.AdasAlarm;
import com.ltmonitor.entity.AlarmConfig;
import com.ltmonitor.entity.DriverInfo;
import com.ltmonitor.entity.GPSRealData;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.util.AlarmTypeConstants;
import com.ltmonitor.util.DateUtil;
import com.ltmonitor.vo.AdasWarnData;
import com.ltmonitor.vo.WarnData;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * 5.2.3.1 报警信息核查请求消息
 链路类型：从链路
 消息方向：上级平台向下级平台
 业务类型标识：DOWN_WARN_MSG_CHECK_REQ。（0x9405）
 描述: 上级平台向下级平台发送主动安全报警核查请求业务，其数据体定义见表 42。
 */
public class DownWarnMsgCheckReq_Subiao implements IReceiveProtocol {
	private Logger logger = Logger.getLogger(DownWarnMsgCheckReq_Subiao.class);

	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();

		MessageParser mp = new MessageParser(dataBody);
		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		int dataLength = mp.getInt(4);

		//WarnData wd = new WarnData();
		int jt809AlarmType = (mp.getInt(2));
		Date alarmTime = (mp.getUtcDate());
		String strDate = DateUtil.datetimeToString(alarmTime);
		message.setMsgDescr("报警类型:0x" + Tools.ToHexString(jt809AlarmType,2) + ",报警时间:" + strDate);

		AlarmConfig c = AlarmTypeConstants.converTo808AlarmType(jt809AlarmType);
		if(c != null)
		{
			AdasAlarm a = ServiceLauncher.getAdasAlarmService().getAdasAlarmByAlarmTypeAndTime(alarmTime, c.getAlarmType(), c.getAlarmSource());
			if(a != null)
			{
				AdasWarnData d = this.get809Alarm(message.getPlateNo(), message.getPlateColor(), a);
				d.setContent(c.getName());
				T809Manager.UpWarnMsgCheckAck_Subiao(d);
			}
		}else
		{
			logger.error("没有找到对应的部标报警类型" + jt809AlarmType);
		}
		return "";
	}

	private AdasWarnData get809Alarm(String plateNo, int plateColor, AdasAlarm a)
	{
		AdasWarnData d = new AdasWarnData();
		d.setPlateColor(plateColor);
		d.setPlateNo(plateNo);
		d.setAlarmLevel(a.getLevel());
		d.setAlarmStatus(a.getAlarmStatus());
		d.setAltitude(a.getAltitude());
		d.setAlarmStatus(a.getAlarmStatus());

		d.setInfoId(a.getAlarmNo());
		d.setGpsSpeed(a.getSpeed());
		d.setRecSpeed(a.getSpeed());
		d.setLatitude((int)a.getLatitude());
		d.setSrc(AdasWarnData.FROM_TERMINAL);
		int alarmType = AlarmTypeConstants.convertSubiao809AlarmType(a.getAlarmType(), a.getAlarmSource());
		d.setType(alarmType);
		d.setWarnTime(a.getAlarmTime());
		d.setDirection(0);
		DriverInfo r = ServiceLauncher.getDriverInfoService().getMainDriverInfo(a.getVehicleId());
		d.setDriverLicenseNo(r.getDriverLicence());
		d.setDriverName(r.getDriverName());
		d.setContent(a.toString());
		return d;
	}
}
