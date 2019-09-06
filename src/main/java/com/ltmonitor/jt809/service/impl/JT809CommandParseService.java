package com.ltmonitor.jt809.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ltmonitor.entity.Terminal;
import com.ltmonitor.jt809.entity.SafetyTerminalInfo;
import com.ltmonitor.service.ITerminalService;
import com.ltmonitor.vo.AdasAlarmProcessResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ltmonitor.entity.JT809Command;
import com.ltmonitor.entity.VehicleData;
import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.entity.CheckRecord;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.service.ICommandHandler;
import com.ltmonitor.jt809.service.IJT809CommandParseService;
import com.ltmonitor.service.IJT809CommandService;
import com.ltmonitor.service.IVehicleDataService;
import com.ltmonitor.util.DateUtil;
import com.ltmonitor.vo.GnssData;

/**
 * 从数据库中读取 用户界面上下发的809指令，转换成809协议格式，发送给上级平台
 * @author admin
 *
 */

@Service("jt809CommandParseService")
public class JT809CommandParseService implements IJT809CommandParseService {
	private static Logger logger = Logger.getLogger(JT809CommandParseService.class);
	/**
	 * 平台过检编号
	 */
	@Value("#{config.platformNo}")
	private String platformNo;
	/**
	 * 是否是苏标809标准
	 */
	@Value("#{config.subiao809}")
	private boolean subiao809;
	
	@Autowired
	private IJT809CommandService jt809CommandService;
	@Autowired
	private IVehicleDataService vehicleDataService;

	@Autowired
	private ITerminalService  terminalService;

	private ICommandHandler commandHandler;

	public final ICommandHandler getOnRecvCommand() {
		return commandHandler;
	}

	public final void setOnRecvCommand(ICommandHandler value) {
		commandHandler = value;
	}

	private Thread parseThread;
	private boolean IsContinue = true;
	// 访问数据库时间间隔
	private int interval;

	public final int getInterval() {
		return interval;
	}

	public final void setInterval(int value) {
		interval = value;
	}

	public JT809CommandParseService() {
		setInterval(1000); // 默认1s
	}

	// 启动命令解析线程，自动解析命令，并发送给终端
	public final void start() {
		IsContinue = true;
		logger.info("启动监听客户端命令线程");
		parseThread = new Thread(new Runnable() {
			public void run() {
				ParseCommandThreadFunc();
			}
		});
		parseThread.start();

	}

	public final void stop() {
		IsContinue = false;
		try {
			if (parseThread != null)
				parseThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void ParseCommandThreadFunc() {
		logger.info("开始监听客户端命令");
		while (IsContinue) {
			try {
				ParseCommand();
			} catch (RuntimeException ex) {
				logger.error(ex.getMessage(),ex);
			}
			try {
				Thread.sleep(getInterval());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 解析数据库的命令，并调用通信服务器，发送给终端
	 */
	public final void ParseCommand() {
		//String hsql = "from JT809Command where CreateDate > ? and Status = ? and UserId > 0";
		//Date startTime = DateUtil.getDate(DateUtil.now(), Calendar.MINUTE, -5);
		List<JT809Command> result = this.jt809CommandService.getLatestCommand();

		for (Object obj : result) {
			JT809Command tc = (JT809Command) obj;
			if (tc.getSubCmd() == 0x1301) {
				ParameterModel p = GlobalConfig.parModel;
				int platformId = (int)p.getPlatformCenterId();
				if(platformId != tc.getUserId())
					continue;
			}
				
			try {
				if (T809Manager.mainLinkConnected == false
						&& T809Manager.subLinkConnected == false && tc.getCmd() != 0x1001) {
					tc.setStatus(JT809Command.STATUS_Disconnected);
				} else {
					// 链路没有连接，无法发送
					boolean rs = Parse(tc);
					tc.setStatus(rs ? "发送成功" : "发送失败");
				}
				// tc.setUpdateDate(new Date());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				logger.error("commpand parse error:", ex);
				tc.setStatus(JT809Command.STATUS_INVALID);
				tc.setRemark(ex.getMessage());
			}
			UpdateCommand(tc);
		}
	}

	public void save(JT809Command jc) {
		this.jt809CommandService.saveOrUpdate(jc);
	}


	/**
	 * 更新命令的执行状态
	 */
	public final void UpdateCommand(JT809Command tc) {
		try {
			tc.setUpdateDate(new Date());
			this.jt809CommandService.saveOrUpdate(tc);
		} catch (RuntimeException ex) {
			logger.error(ex.getMessage(),ex);
		}

	}


	/**
	 * 不对非法命令格式进行解析，在命令录入时确保格式正确
	 * 
	 * @param tc
	 * @return
	 * @throws Exception
	 */
	private final Boolean Parse(JT809Command tc) throws Exception {

		String cmdData = tc.getCmdData();
		String[] strData = cmdData == null ? null : cmdData.split(";");
		if (tc.getSubCmd() == 0x1207) {
			// 申请交换指定车辆定位信息
			Date start = DateUtil.stringToDatetime(strData[0],
					"yyyy-MM-dd HH:mm:ss");
			Date end = DateUtil.stringToDatetime(strData[1],
					"yyyy-MM-dd HH:mm:ss");

			return T809Manager.UpExgMsgApplyForMonitorStartup(tc.getPlateNo(),
					tc.getPlateColor(), start, end);
		} else if (tc.getSubCmd() == 0x1208) {
			// 结束申请交换指定车辆定位信息
			return T809Manager.UpExgMsgApplyForMonitorEnd(tc.getPlateNo(),
					tc.getPlateColor());
		} else if (tc.getCmd() == 0x1001) {
			// 主链路登录申请
			return T809Manager.UpConnectReq();
		} else if (tc.getSubCmd() == 0x1209) {
			// 申请上级平台补发定位数据请求
			Date start = DateUtil.stringToDatetime(strData[0],
					"yyyy-MM-dd HH:mm:ss");
			Date end = DateUtil.stringToDatetime(strData[1],
					"yyyy-MM-dd HH:mm:ss");
			return T809Manager.UpExgMsgApplyHisGnssDataReq(tc.getPlateNo(),
					tc.getPlateColor(), start, end);
		} else if (tc.getSubCmd() == 0x1240) {
			VehicleData vd = vehicleDataService.getVehicleByPlateNo(tc.getPlateNo());
			Terminal t = this.terminalService.load(vd.getTermId());
			if(t == null)
			{
				logger.error("车辆没有安装终端设备");
			}
			SafetyTerminalInfo si = new SafetyTerminalInfo();
			si.setPlateNo(vd.getPlateNo());
			si.setPlateColor(vd.getPlateColor());
			si.setProducer(t.getProducer());
			si.setContacts(t.getContacts());
			si.setInstallCompany(t.getInstallCompany());
			si.setInstallTime(t.getInstallTime());
			si.setTelephone(t.getContactTelephone());
			si.setTerminalId(t.getTermNo());
			si.setTerminalModel(t.getTermType());
			si.setPlateformId(platformNo);
			si.setComplianceRequirements(true);
			return T809Manager.UpExgMsgSafetyTerminal_Subiao(si);
		} else if (tc.getSubCmd() == 0x1203) {
			// 补发历史定位数据
			Date start = DateUtil.stringToDatetime(strData[0],
					"yyyy-MM-dd HH:mm:ss");
			Date end = DateUtil.stringToDatetime(strData[1],
					"yyyy-MM-dd HH:mm:ss");

			List<GnssData> gnssDatas = this.getHistoryGnssData(tc.getPlateNo(),
					start, end);
			int count = gnssDatas.size();
			if (count > 5) {
				int index = 0;
				while (index < gnssDatas.size()) {
					int endIndex = (index + 5);
					endIndex = endIndex > count ? count : endIndex;
					List<GnssData> gs = gnssDatas.subList(index, endIndex);
					T809Manager.UpExgMsgHistoryLocations(tc.getPlateNo(),
							tc.getPlateColor(), gs);
					index += 5;
				}
				return true;
			} else
				return false;

		} else if (tc.getCmd() == 0x1003) {
			// 主链路注销请求消息
			return T809Manager.UpDisconnectReq();

		} else if (tc.getCmd() == 0x1007) {
			// 主链路注销请求消息
			return T809Manager.UpDisconnectMainLinkInform();

		} else if (tc.getCmd() == 0x1008) {
			// 下级平台主动关闭主从链路的通知  
			return T809Manager.UpCloseLinkInform();

			 //T809Manager.StopServer();
			 //return true;

		} else if (tc.getSubCmd() == 0x1403) {
			if(subiao809)
			{
				AdasAlarmProcessResult r = new AdasAlarmProcessResult();
				int m = 0;
				r.setInfoId(strData[m++]);
				r.setResult(Integer.parseInt(strData[m++]));
				r.setProcessMethod(Integer.parseInt(strData[m++]));
				r.setOperator(strData[m++]);
				r.setOperatorCompany(strData[m++]);
				r.setPlateColor(tc.getPlateColor());
				r.setPlateNo(tc.getPlateNo());
				return T809Manager.UpWarnMsgOperationInfo(r);
			}else {
				// 主动上报报警处理结果
				int alarmId = Integer.parseInt(strData[0]);
				int result = Integer.parseInt(strData[1]);
				return T809Manager.UpWarnMsgAdptToDoInfo(tc.getPlateNo(),
						tc.getPlateColor(), alarmId, result);
			}
		} else if (tc.getSubCmd() == 0x1301) {
			// 查岗应答
			CheckRecord pm = new CheckRecord();
			int objType = Integer.parseInt(strData[0]);
			pm.setObjType(objType);
			pm.setObjId(strData[1]);
			int infoId = Integer.parseInt(strData[2]);
			pm.setInfoId(infoId);
			pm.setMessage(strData[3]);
			return T809Manager.UpPlatFormMsgPostQueryAck(pm);
		} else if (tc.getSubCmd() == 0x1302) {
			// 平台间报文应答
			int infoId = Integer.parseInt(strData[0]);
			return T809Manager.UpPlatFormMsgInfoAck(infoId);
		}else if (tc.getSubCmd() == 0x1401) {
			// 平台间督办应答
			int superviseId = Integer.parseInt(strData[0]);
			
			byte result = Byte.parseByte(strData[1]);
			return T809Manager.UpWarnMsgUrgeToDoAck(tc.getPlateNo(), tc.getPlateColor(),
					superviseId, result);
		}else if (tc.getCmd() == 0x1701) {
			return T809Manager.UpAuthorizeMsgStartUp();
		}

		return false;
		// return ts;
	}

	private List<GnssData> getHistoryGnssData(String plateNo, Date start,
			Date end) {
		List<GnssData> result = new ArrayList<GnssData>();
		String hsql = "from VehicleData where plateNo = ?";
		VehicleData vd = (VehicleData)this.vehicleDataService.find(hsql, plateNo);
		if(vd == null)
			return result;
		
		for (int m = 0; m < 25; m++) {
			GnssData d = new GnssData();
			d.setPlateNo(plateNo);
			d.setPlateColor(vd.getPlateColor());
			d.setGpsSpeed(31+m);
			d.setRecSpeed(0);
			d.setAlarmState(0);
			d.setAltitude(0);
			d.setDirection(m*4);
			d.setLatitude(31324566);
			d.setLongitude(121243666);
			d.setPosEncrypt(0);
			Date sendTime = DateUtil.getDate(start, Calendar.SECOND,m);
			d.setPosTime(sendTime);
			d.setTotalMileage(321+m);
			d.setVehicleState(0);
			result.add(d);
		}
		return result;
		/**
		String hql = "from GpsInfo where sendTime >= ? and sendTime <= ? and plateNo = ?";

		List gpsInfoList = this.baseDao.query(hql, new Object[] { start, end,
				plateNo });
		for (Object obj : gpsInfoList) {
			GpsInfo g = (GpsInfo) obj;
			GnssData d = new GnssData();
			d.setPlateNo(plateNo);
			d.setGpsSpeed((int) (g.getVelocity() * 10));
			d.setRecSpeed((int) (g.getRecordVelocity() * 10));
			d.setAlarmState(g.getAlarmState());
			d.setAltitude((int) (g.getAltitude() * 10));
			d.setDirection(g.getDirection());
			d.setLatitude((int) (g.getLatitude() * 1000000));
			d.setLongitude((int) (g.getLongitude() * 1000000));
			d.setPosEncrypt(0);
			d.setPosTime(g.getSendTime());
			d.setTotalMileage((int) (g.getMileage() * 10));
			d.setVehicleState(g.getStatus());
			result.add(d);
		}
		return result;
		*/
	}
}