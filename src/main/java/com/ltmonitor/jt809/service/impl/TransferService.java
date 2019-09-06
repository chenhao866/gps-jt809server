package com.ltmonitor.jt809.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.send.*;
import com.ltmonitor.video.entity.VideoFileItem;
import com.ltmonitor.vo.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ltmonitor.entity.Department;
import com.ltmonitor.entity.GPSRealData;
import com.ltmonitor.entity.UserInfo;
import com.ltmonitor.entity.VehicleData;
import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.entity.DriverModel;
import com.ltmonitor.jt809.entity.VehicleRegisterInfo;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.service.IGPSRealDataService;
import com.ltmonitor.service.ITransferService;
import com.ltmonitor.service.IUserInfoService;
import com.ltmonitor.service.IVehicleDataService;
import com.ltmonitor.service.IVehicleRegisterInfoService;
import com.ltmonitor.util.DateUtil;

/**
 * RMI服务类，接收808转发过来的数据，并以809格式转发给上级平台
 */
@Service("transferService")
public class TransferService implements ITransferService {

	private static Logger logger = Logger.getLogger(TransferService.class);

	/**
	 * 从数据库中读取实时数据进行转发
	 */
	@Value("#{config.transferFromDb}")
	private boolean transferFromDb;

	/**
	 * 根据部门Id
	 */
	private boolean transferByPlatformId = false;
	/**
	 * 转发实时数据，根据用户帐户分配的车辆进行过滤
	 */
	@Value("#{config.transferByUserId}")
	private boolean transferByUserId = true;

	@Value("#{config.transferInterval}")
	private int transferInterval = 1000 * 10;
	@Value("#{config.userId}")
	private String userId;

	/**
	 *实时数据数据处理线程
	 */
	private Thread processRealDataThread;

	@Autowired
	private IVehicleDataService vehicleDataService;

	@Autowired
	private IGPSRealDataService gpsRealDataService;

	@Autowired
	private IUserInfoService userInfoService;
	@Autowired
	private IVehicleRegisterInfoService vehicleRegisterInfoService;
	private boolean continueTransfer = true;
	//
	private ConcurrentLinkedQueue<GnssData> dataQueue = new ConcurrentLinkedQueue();

	public long gnssDataQueueNum = 0;

	/**
	 * 实时定位信息，如果主从链路都断掉的情况下，需要压入队列，等链路恢复正常后，通过补发协议进行补发
	 */
	private ConcurrentLinkedQueue<GnssData> gnssDataQueueWaitForSend = new ConcurrentLinkedQueue<GnssData>();
	/**
	 * 如果是压测的情况下，将压测数据单独压入一个独立的队列，降低此队列的补报优先级
	 */
	private ConcurrentLinkedQueue<GnssData> largeDataQueueWaitForSend = new ConcurrentLinkedQueue<GnssData>();
	/**
	 * 注册信息
	 */
	private ConcurrentLinkedQueue<VehicleRegisterInfo> vehicleRegisterQueue = new ConcurrentLinkedQueue<VehicleRegisterInfo>();

	Map<Long, Long> authorizedDepIdMap = new HashMap<Long, Long>();
	Hashtable sendRegisterMap = new Hashtable();

	Hashtable sendMap = new Hashtable();

	public void startTransfer() {
		processRealDataThread = new Thread(new Runnable() {
			public void run() {
				ProcessRealDataThreadFunc();
			}
		});
		processRealDataThread.start();

	}

	public void stopTransfer() {
		if (processRealDataThread != null) {
			transferFromDb = false;
			this.continueTransfer = false;
			try {
				processRealDataThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void ProcessRealDataThreadFunc() {

		if (transferFromDb) {
			if (transferByUserId) {
				loadDepIdMap();
			}
		}
		while (continueTransfer) {
			try {
				if (T809Manager.subLinkConnected
						&& T809Manager.mainLinkConnected) {
					sendHistoryLocation();
				}

				if (dataQueue.size() > 100 * 10000) {
					logger.error("转发队列过大，直接清除");
					// dataQueue.clear();
				}
				GnssData tm = dataQueue.poll();
				int count = 0;
				while (tm != null && count < 100) {
					sendRealDataLocation(tm);
					tm = dataQueue.poll();
					count++;
				}

				VehicleRegisterInfo vm = this.vehicleRegisterQueue.poll();
				count = 0;
				while (vm != null && count < 20) {
					T809Manager.UpExgMsgRegister(vm);
					vm = vehicleRegisterQueue.poll();
					count++;
				}

				transferRealDataFromDb();
				GlobalConfig.waitForSend = largeDataQueueWaitForSend.size();
				gnssDataQueueNum = GlobalConfig.waitForSend;
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				// logger.error(ex.getStackTrace());
			}
			try {
				Thread.sleep(50L);
			} catch (InterruptedException e1) {
			}
		}
	}

	public boolean isMainLinked() {
		return T809Manager.mainLinkConnected;
	}

	public boolean isSubLinked() {
		return T809Manager.subLinkConnected;
	}

	/**
	 * 向上级平台转发实时数据
	 * @param gnssData
	 */
	private void sendRealDataLocation(GnssData gnssData) {
		if (T809Manager.mainLinkConnected == false
				&& T809Manager.subLinkConnected == false) {
			if (gnssData.getPlateNo() != null
					&& gnssData.getPlateNo().indexOf("测B0") >= 0) {
				if (gnssDataQueueNum > 500 * 10000) {
					logger.error("补发队列数量增加:" + gnssDataQueueNum);
					//largeDataQueueWaitForSend.clear();
					int m = 0;
					while (m++ < 5) {
						largeDataQueueWaitForSend.poll();
					}
				}
				largeDataQueueWaitForSend.add(gnssData);
			} else {
				gnssDataQueueWaitForSend.add(gnssData);
				logger.error("收到条数:" + gnssDataQueueWaitForSend.size());
			}

		} else {
			boolean res = T809Manager.UpExgMsgRealLocation(gnssData);
			if (res == false) {
				logger.error("transfer service发送实时定位失败,是否重发:" + GlobalConfig.autoResend);
				if (GlobalConfig.autoResend) {
					if (gnssData.getPlateNo() != null
							&& gnssData.getPlateNo().indexOf("测B0") >= 0) {
						if (gnssDataQueueNum > 200 * 10000) {
							logger.error("补发队列数量增加:" + gnssDataQueueNum);
							// largeDataQueueWaitForSend.clear();
						}
						largeDataQueueWaitForSend.add(gnssData);
					} else {
						gnssDataQueueWaitForSend.add(gnssData);
						logger.error("收到条数:" + gnssDataQueueWaitForSend.size());
					}
				}
			}
		}

	}

	/**
	 * 历史自动补报,从历史队列中取出，进行自动补报
	 */
	private void sendHistoryLocation() {
		if (gnssDataQueueWaitForSend.isEmpty() == false) {
			List<GnssData> gnssDataList = new ArrayList<GnssData>();
			while (gnssDataQueueWaitForSend.isEmpty() == false) {
				GnssData g = gnssDataQueueWaitForSend.poll();
				if (g != null) {
					gnssDataList.add(g);
					if (gnssDataList.size() == 5) {
						T809Manager.UpExgMsgHistoryLocations(g.getPlateNo(),
								g.getPlateColor(), gnssDataList);
						gnssDataList.clear();
					}
				}
			}
			if (gnssDataList.size() > 0) {
				GnssData g = gnssDataList.get(0);
				T809Manager.UpExgMsgHistoryLocations(g.getPlateNo(),
						g.getPlateColor(), gnssDataList);
				gnssDataList.clear();
			}
		}

		if (largeDataQueueWaitForSend.isEmpty())
			return;
		int m = 0;
		List<GnssData> gnssDataList = new ArrayList<GnssData>();
		while (largeDataQueueWaitForSend.isEmpty() == false && m < 20) {
			GnssData g = largeDataQueueWaitForSend.poll();
			if (g != null) {
				gnssDataList.add(g);
				if (gnssDataList.size() == 5) {
					T809Manager.UpExgMsgHistoryLocations(g.getPlateNo(),
							g.getPlateColor(), gnssDataList);
					gnssDataList.clear();
					m++;
				}
			}
		}
		if (gnssDataList.size() > 0) {
			GnssData g = gnssDataList.get(0);
			T809Manager.UpExgMsgHistoryLocations(g.getPlateNo(),
					g.getPlateColor(), gnssDataList);
			gnssDataList.clear();
		}

		gnssDataQueueNum = 0;
	}

	private void transferRealDataFromDb() {

		if (transferFromDb == false)
			return;

		if (T809Manager.mainLinkConnected || T809Manager.subLinkConnected) {
			try {
				Date start = new Date();
				List<GPSRealData> result = null;
				if (this.transferByPlatformId) {
					String hql = "from GPSRealData where sendTime >= ? and online = ? and depId = ? and velocity < 150 and latitude > 0 and longitude > 0";

					Date date = DateUtil.getDate(new Date(), Calendar.SECOND,
							-30);
					ParameterModel p = GlobalConfig.parModel;
					int pId = (int) p.getPlatformCenterId();
					result = gpsRealDataService.query(hql, new Object[] { date,
							true, pId });
				} else {
					String hql = "from GPSRealData where sendTime >= ? and online = ? and velocity < 150 and latitude > 0 and longitude > 0";

					Date date = DateUtil.getDate(new Date(), Calendar.SECOND,
							-120);
					result = gpsRealDataService.query(hql, new Object[] { date,
							true });
				}
				for (GPSRealData rd : result) {
					if (this.transferByUserId
							&& this.authorizedDepIdMap.containsKey(rd
									.getDepId()) == false) {
						continue;
					}

					if (sendMap.containsKey(rd.getPlateNo())) {
						Date sendTime = (Date) sendMap.get(rd.getPlateNo());
						if (sendTime != null
								&& sendTime.compareTo(rd.getSendTime()) >= 0)
							continue;
					}
					sendMap.put(rd.getPlateNo(), rd.getSendTime());

					GnssData gd = new GnssData();
					gd.setAltitude((int) (rd.getAltitude() * 10));
					gd.setDirection(rd.getDirection());
					gd.setGpsSpeed((int) (rd.getVelocity()));
					gd.setLatitude((int) (rd.getLatitude() * 1000000));
					gd.setLongitude((int) (rd.getLongitude() * 1000000));
					gd.setPlateNo(rd.getPlateNo());
					gd.setTotalMileage((int) (rd.getMileage() * 10));
					gd.setDirection(rd.getDirection());
					long alarmState = Long.valueOf(rd.getAlarmState(), 2);
					gd.setAlarmState(alarmState);
					long intStatus = Long.valueOf(rd.getStatus(), 2);
					gd.setVehicleState(intStatus);
					gd.setPosTime(rd.getSendTime());

					// VehicleData vd =
					// this.GetVehicleBySimNo(rd.getSimNo());
					// if (vd != null)
					// gd.setPlateColor(vd.getPlateColor());
					gd.setPlateColor(2);
					try {
						VehicleData vd = vehicleDataService.load(rd
								.getVehicleId());
						if (vd != null)
							gd.setPlateColor(vd.getPlateColor());
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}

					UpExgMsgRealLocation(gd);
				}
				TransferRegiserMessage();
				Date end = new Date();
				double sec = DateUtil.getSeconds(start, end);
				if (sec > 5) {
					logger.error("数据库转发耗时:" + sec + "秒");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e1) {
		}

	}

	void sendAlarmData(GPSRealData rd, int warnType, String alarmDescr) {
		WarnData wd = new WarnData();
		wd.setPlateNo(rd.getPlateNo());
		wd.setInfoId(rd.getID());
		wd.setSrc(WarnData.FROM_TERMINAL);
		wd.setWarnTime(rd.getSendTime());
		wd.setType(warnType);
		wd.setContent(alarmDescr);
		wd.setPlateColor(2);
		try {
			VehicleData vd = vehicleDataService.load(rd.getVehicleId());
			if (vd != null)
				wd.setPlateColor(vd.getPlateColor());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		UpWarnMsgAdptInfo(wd);
	}

	/**
	 * 实时转发定位信息
	 */
	public void UpExgMsgRealLocation(GnssData gd) {
		if (gd.getPlateNo() != null && gd.getPlateNo().indexOf("测B0") >= 0)
			dataQueue.add(gd);
		else {
			if (T809Manager.mainLinkConnected == false
					&& T809Manager.subLinkConnected == false) {
				gnssDataQueueWaitForSend.add(gd);
				logger.error("收到条数:" + gnssDataQueueWaitForSend.size());
			} else {
				T809Manager.UpExgMsgRealLocation(gd);
			}
		}
	}

	/**
	 * 拍照应答
	 * 
	 * @param _photo
	 * @return
	 */
	@Override
	public boolean UpCtrlMsgTakePhotoAck(TakePhotoModel _photo) {

		return T809Manager.UpCtrlMsgTakePhotoAck(_photo);
	}

	/**
	 * 上报电子运单
	 * 
	 * @param plateNo
	 * @param plateColor
	 * @param eContent
	 * @return
	 */
	@Override
	public boolean UpExgMsgReportTakeEWayBill(String plateNo, int plateColor,
			String eContent) {
		return T809Manager.UpExgMsgReportTakeEWayBill(plateNo, plateColor,
				eContent);
	}

	/**
	 * 主动上报驾驶员身份
	 * 
	 * @param dm
	 * @return
	 */
	public boolean UpExgMsgReportDriverInfo(DriverModel dm) {
		return T809Manager.UpExgMsgReportDriverInfo(dm);
	}

	/**
	 * 上报报警信息
	 * 
	 * @param wd
	 * @return
	 */
	public boolean UpWarnMsgAdptInfo(WarnData wd) {
		return T809Manager.UpWarnMsgAdptInfo(wd);

	}

	/**
	 * 行车记录仪应答
	 * 
	 * @param plateNo
	 * @param plateColor
	 * @param cmdType
	 * @param cmdData
	 * @return
	 */
	@Override
	public boolean UpCtrlMsgTakeTravelAck(String plateNo, int plateColor,
			byte cmdType, byte[] cmdData) {
		return T809Manager.UpCtrlMsgTakeTravelAck(plateNo, plateColor, cmdType,
				cmdData);
	}

	@Override
	public boolean UpCtrlMsgTextInfoAck(String plateNo, int plateColor,
			int msgId, byte result) {
		return T809Manager.UpCtrlMsgTextInfoAck(plateNo, plateColor, msgId,
				result);
	}

	@Override
	public boolean UpCtrlMsgEmergencyMonitoringAck(String plateNo,
			int plateColor, byte result) {
		return T809Manager.UpCtrlMsgEmergencyMonitoringAck(plateNo, plateColor,
				result);
	}

	/**
	 * 单向监听
	 * 
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	@Override
	public boolean UpCtrlMsgMonitorVehicleAck(String plateNo, int plateColor,
			byte result) {
		return T809Manager.UpCtrlMsgMonitorVehicleAck(plateNo, plateColor,
				result);
	}

	/**
	 * 静态车辆注册1201
	 * 
	 * @param vm
	 * @return
	 */
	public boolean UpExgMsgRegister(VehicleRegisterInfo vm) {
		vehicleRegisterQueue.add(vm);
		return true;
	}

	/**
	 * 实时视频请求应答  0x1801
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param videoServerIp
	 * @param port
	 * @return
	 */
	@Override
	public  boolean UpRealVideoMsgStartUpAck(String plateNo, int plateColor,
											 byte result, String videoServerIp, int port) {

		return T809Manager.UpRealVideoMsgStartUpAck(plateNo,
				plateColor, result,videoServerIp,port);
	}

	/**
	 * 终止实时音视频传输应答 0x1802
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	@Override
	public  boolean UpRealVideoMsgEndAck(String plateNo, int plateColor,
										 byte result) {

		return T809Manager.UpRealVideoMsgEndAck(plateNo,
				plateColor, result);
	}

	/**
	 * 主动上传音视频资源目录 0x1901
	 * @param plateNo
	 * @param plateColor
	 * @param videoFileItems
	 * @return
	 */
	@Override
	public  boolean UpFileListMsg(String plateNo, int plateColor,
								  List<VideoFileItem> videoFileItems) {

		return T809Manager.UpFileListMsg(plateNo,
				plateColor, videoFileItems);
	}

	/**
	 * 上传音视频资源目录请求应答消息 0x1902
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param videoFileItems
	 * @return
	 */
	@Override
	public  boolean UpRealVideoFileListReqAck(String plateNo, int plateColor, int result,
											  List<VideoFileItem> videoFileItems) {

		return T809Manager.UpRealVideoFileListReqAck(plateNo,
				plateColor,result, videoFileItems);
	}

	/**
	 * 远程录像回放请求应答 0x1A01
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param videoServerIp
	 * @param port
	 * @return
	 */
	@Override
	public  boolean UpPlayBackMsgStartUpAck(String plateNo, int plateColor,
											byte result, String videoServerIp, int port) {

		return T809Manager.UpPlayBackMsgStartUpAck(plateNo,
				plateColor, result,videoServerIp,port);
	}
	/**
	 * 远程录像回放控制应答 0x1A02
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	@Override
	public  boolean UpPlayBackMsgControlAck(String plateNo, int plateColor,
											byte result) {

		return T809Manager.UpPlayBackMsgControlAck(plateNo,
				plateColor, result);
	}

	/**
	 * 远程录像下载请求应答 0x1B01
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param responseMsgSn
	 * @return
	 */
	@Override
	public  boolean UpDownloadMsgStartUpAck(String plateNo, int plateColor,
											byte result, short responseMsgSn) {

		return T809Manager.UpDownloadMsgStartUpAck(plateNo,
				plateColor, result,responseMsgSn);
	}

	/**
	 * 远程录像下载完成通知 0x1B02
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param responseMsgSn
	 * @param _ip
	 * @param _port
	 * @param _userName
	 * @param _password
	 * @param _filePath
	 * @return
	 */
	@Override
	public  boolean UpDownloadMsgEndInform(String plateNo, int plateColor, byte result,
										   short responseMsgSn, String _ip, int _port, String _userName,
										   String _password, String _filePath) {

		return T809Manager.UpDownloadMsgEndInform(plateNo,
				plateColor, result,responseMsgSn,  _ip,  _port,  _userName,
				_password,  _filePath);
	}

	/**
	 * 录像下载控制应答消息 0x1B03
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	@Override
	public  boolean UpDownloadMsgControlAck(String plateNo, int plateColor,
											byte result) {

		return T809Manager.UpDownloadMsgControlAck(plateNo,
				plateColor, result);
	}


	/**
	 * 主动上报报警处理消息
	 * @param sm
	 * @return
	 */
	public boolean UpWarnMsgInfo_Subiao(AdasWarnData sm) {

		return T809Manager.UpWarnMsgInfo_Subiao(sm);
	}

	/**
	 * 主动上报报警处理结果
	 * @param r
	 * @return
	 */
	public boolean UpWarnMsgOperationInfo(AdasAlarmProcessResult r) {

		return T809Manager.UpWarnMsgOperationInfo(r);
	}


	private void loadDepIdMap() {
		try {
			String hsql = "from UserInfo where loginName = ? and deleted = ?";

			UserInfo user = (UserInfo) this.userInfoService.find(hsql,
					new Object[] { userId, false });
			if (user == null) {
				logger.error("没有找到此用户:" + userId);
			}

			Set<Department> depSet = user.getDepartments();

			for (Department dep : depSet) {
				if (dep.getDeleted() == false) {
					authorizedDepIdMap
							.put(dep.getEntityId(), dep.getEntityId());
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			logger.error(ex.getStackTrace());
		}

	}

	private void TransferRegiserMessage() {
		List result = null;
		Date date = DateUtil.getDate(new Date(), Calendar.SECOND, -120);
		if (this.transferByPlatformId == false) {
			String hql = "from VehicleRegisterInfo where updateDate > ? and plateNo is not null";
			result = vehicleRegisterInfoService.query(hql,
					new Object[] { date });
		} else {
			String hql = "from VehicleRegisterInfo where updateDate > ? and depId = ? and plateNo is not null";

			ParameterModel p = GlobalConfig.parModel;
			int pId = (int) p.getPlatformCenterId();
			result = vehicleRegisterInfoService.query(hql, new Object[] { date,
					pId });
		}

		for (Object obj : result) {
			VehicleRegisterInfo vi = (VehicleRegisterInfo) obj;
			if (this.transferByUserId
					&& this.authorizedDepIdMap.containsKey(vi.getDepId()) == false) {
				continue;
			}

			if (sendRegisterMap.containsKey(vi.getPlateNo())) {
				Date sendTime = (Date) sendRegisterMap.get(vi.getPlateNo());
				if (sendTime != null
						&& sendTime.compareTo(vi.getUpdateDate()) >= 0)
					continue;
			}
			sendRegisterMap.put(vi.getPlateNo(), vi.getUpdateDate());

			T809Manager.UpExgMsgRegister(vi);
		}
	}

	/**
	 * 定位信息自动补报1203
	 * 
	 * @param gnssDatas
	 * @return
	 */
	public boolean UpExgMsgHistoryLocations(String plateNo, int plateColor,
			List<GnssData> gnssDatas) {
		return T809Manager.UpExgMsgHistoryLocations(plateNo, plateColor,
				gnssDatas);
	}

	public boolean isTransferFromDb() {
		return transferFromDb;
	}

	public void setTransferFromDb(boolean transferFromDb) {
		this.transferFromDb = transferFromDb;
	}

	public boolean isTransferByPlatformId() {
		return transferByPlatformId;
	}

	public void setTransferByPlatformId(boolean transferByPlatformId) {
		this.transferByPlatformId = transferByPlatformId;
	}

	public boolean isTransferByUserId() {
		return transferByUserId;
	}

	public void setTransferByUserId(boolean transferByUserId) {
		this.transferByUserId = transferByUserId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getTransferInterval() {
		return transferInterval;
	}

	public void setTransferInterval(int transferInterval) {
		this.transferInterval = transferInterval;
	}

}
