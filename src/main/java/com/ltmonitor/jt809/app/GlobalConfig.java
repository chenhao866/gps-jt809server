package com.ltmonitor.jt809.app;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.util.StringUtil;
import com.ltmonitor.util.T809Constants;
import org.apache.log4j.Logger;

import com.ltmonitor.entity.EWayBill;
import com.ltmonitor.jt809.entity.CheckRecord;
import com.ltmonitor.jt809.entity.DriverModel;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.model.VehicleModel;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;

public class GlobalConfig {
	private static Logger logger = Logger.getLogger(GlobalConfig.class);
	public static ParameterModel parModel = new ParameterModel();
	public static HashMap<String, VehicleModel> vehicleMap;

	// 电子运单上报
	public static HashMap<String, EWayBill> ewayBillMap = new HashMap();
	// 驾驶员上报
	public static HashMap<String, DriverModel> driverMap = new HashMap();

	public static String filterPlateNo;

	public static boolean displayMsg;

	public static boolean isConnection;
	public static boolean isTimerStart;
	public static HashMap<Integer, IReceiveProtocol> protocolMap = new HashMap<Integer, IReceiveProtocol>();
	// 主链路是否已经建立
	public static boolean isOpenPlat = false;
	// 静态车辆数据是否已经注册
	public static boolean isRegist = false;

	public static String authorizeCode = "";

	/**
	 * 是否自动重连
	 */
	public static boolean autoReconnect = false;

	/**
	 * 是否自动重发
	 */
	public static boolean autoResend = false;


	/**
	 * 是否是手动关闭的连接,如果是手动关闭的，就不需要不断地尝试自动重连接
	 */
	public static boolean normalDisconnect = false;
	/**
	 * 转发的总定位包数量
	 */
	public static long totalLocationPacketNum = 0;
	/**
	 * 补发的历史定位包总数量
	 */
	public static long totalHisLocationPacketNum = 0;

	/**
	 * 队里中等待转发的数量，用于界面显示跟踪队列拥堵情况
	 */
	public static long waitForSend = 0;
	/**
	 * 存储正在回放车辆的通道号，在下发控制的时候使用
	 */
	public static HashMap<String, Integer> playBackChannelMap = new HashMap();


	// 流水号
	private static long upcounter = 0L;
	public static long downcounter = 0L;
	public static HashMap<String, CheckRecord> chagang = new HashMap();

	private static Queue<JT809Message> msgQueue = new ConcurrentLinkedQueue<JT809Message>();

	public static void putMsg(JT809Message tm) {
		logMessage(tm);
		if (displayMsg == false) {
			if (tm.getMsgType() == 0x1005 || tm.getMsgType() == 0x1006
					|| tm.getMsgType() == 0x1200 || tm.getMsgType() == 0x1400
					|| tm.getMsgType() == 0x9005 || tm.getMsgType() == 0x9006)
				return;
		}
		if (filterPlateNo != null && filterPlateNo.length() > 0) {

			//过滤车牌号
			if (tm.getPlateNo() == null
					|| tm.getPlateNo().indexOf(filterPlateNo) < 0)
				return;
		}
		msgQueue.offer(tm);
	}


	private static void logMessage(JT809Message tm)
	{
		if(tm.getMsgType() != 0x1200)
		{
			Integer subType = tm.getSubType() == 0 ? tm.getMsgType() : tm
					.getSubType();
			String subDescr = T809Constants.getMsgDescr(subType);

			//subDescr = "[" + "0x" + Tools.ToHexString(subType, 2) + "]" + subDescr;
			StringBuilder sbLog = new StringBuilder();
			sbLog.append("[0x").append(Tools.ToHexString(subType, 2)).append("]").append(subDescr);

			if(StringUtil.isNullOrEmpty(tm.getDescr()) == false)
			 sbLog.append(",描述：").append(tm.getDescr());
			sbLog.append("原始报文:").append(tm.getPacketDescr());

			logger.error(sbLog.toString());

		}
	}

	public static JT809Message pollMsg() {
		return msgQueue.poll();
	}

	// 获取流水号，用于包的发送
	public static long getSN() {
		return upcounter++;
	}

	/**
	 * 初始化程序配置
	 */
	public static void initSystem() {
		upcounter = 0;
		protocolMap.put(0x1200,
				new com.ltmonitor.jt809.protocol.receive.UpExgMsg());
		protocolMap.put(0x9800,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoMsg());
		protocolMap.put(0x9900,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoMsg());
		protocolMap.put(0x9A00,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoMsg());
		protocolMap.put(0x9B00,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoMsg());

		protocolMap.put(0x9801,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoMsgStartUp());
		protocolMap.put(0x9802,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoMsgEnd());

		protocolMap.put(0x9901,
				new com.ltmonitor.jt809.protocol.receive.DownFileListMsgAck());
		protocolMap.put(0x9902,
				new com.ltmonitor.jt809.protocol.receive.DownRealVideoFileListReq());


		protocolMap.put(0x9A01,
				new com.ltmonitor.jt809.protocol.receive.DownPlayBackMsgStartUp());
		protocolMap.put(0x9A02,
				new com.ltmonitor.jt809.protocol.receive.DownPlayBackMsgControl());


		protocolMap.put(0x9B01,
				new com.ltmonitor.jt809.protocol.receive.DownDownloadMsgStartUp());
		protocolMap.put(0x9B02,
				new com.ltmonitor.jt809.protocol.receive.UpDownloadMsgEndInformAck());
		protocolMap.put(0x9B03,
				new com.ltmonitor.jt809.protocol.receive.DownDownloadMsgControl());


		protocolMap.put(0x1002,
				new com.ltmonitor.jt809.protocol.receive.UpConnectRsp());// 登录应答包
		protocolMap.put(0x1004,
				new com.ltmonitor.jt809.protocol.receive.UpDisconnectRsp());// 主链路注销请求应答包
		protocolMap.put(0x1006,
				new com.ltmonitor.jt809.protocol.receive.UpLinkTestRsp());// 保持连接应答包
		protocolMap.put(0x9005,
				new com.ltmonitor.jt809.protocol.receive.DownLinkTestReq());// 保持连接应答包
		protocolMap.put(0x9007,
				new com.ltmonitor.jt809.protocol.receive.DownDisconnectInform());// 保持连接应答包
		protocolMap.put(0x9008,
				new com.ltmonitor.jt809.protocol.receive.DownCloselinkInform());// 保持连接应答包
		protocolMap.put(0x9001,
				new com.ltmonitor.jt809.protocol.receive.DownConnectReq());// 登录请求包
		protocolMap
				.put(0x9003,
						new com.ltmonitor.jt809.protocol.receive.DownDisconnectReq());// 从链路注销请求包
		protocolMap
				.put(0x9501,
						new com.ltmonitor.jt809.protocol.receive.DownCtrlMsgMonitorVehicleReq());// 车辆单项监控请求
		protocolMap
				.put(0x9302,
						new com.ltmonitor.jt809.protocol.receive.DownPlatformMsgInfoReq());// 报文间平台请求9302
																							// ,应答1302
		protocolMap
				.put(0x9502,
						new com.ltmonitor.jt809.protocol.receive.DownCtrlMsgTakePhotoReq());// 车辆拍照请求与应答
		protocolMap.put(0x9200,
				new com.ltmonitor.jt809.protocol.receive.DownExgMsg());//
		protocolMap
				.put(0x9205,
						new com.ltmonitor.jt809.protocol.receive.DownExgMsgReturnStartUp());//
		protocolMap
				.put(0x9206,
						new com.ltmonitor.jt809.protocol.receive.DownExgMsgReturnEnd());//
		protocolMap
				.put(0x9209,
						new com.ltmonitor.jt809.protocol.receive.DownExgMsgApplyHisGnssDataAck());//
		protocolMap.put(0x9500,
				new com.ltmonitor.jt809.protocol.receive.DownCtrlMsg());//
		protocolMap.put(0x9600,
				new com.ltmonitor.jt809.protocol.receive.DownBaseMsg());//
		protocolMap
				.put(0x9601,
						new com.ltmonitor.jt809.protocol.receive.DownBaseMsgVehicleAdded());//
		protocolMap.put(0x9300,
				new com.ltmonitor.jt809.protocol.receive.DownPlatformMsg());
		protocolMap
				.put(0x9301,
						new com.ltmonitor.jt809.protocol.receive.DownPlatformMsgPostQueryReq());
		protocolMap
				.put(0x9504,
						new com.ltmonitor.jt809.protocol.receive.DownCtrlMsgTakeTravelReq());// 　行车记录请求,应答　
		protocolMap
				.put(0x9505,
						new com.ltmonitor.jt809.protocol.receive.DownCtrlMsgEmergencyMonitoringReq());// 车辆应急接入我监控平台
																										// han
		protocolMap
				.put(0x9402,
						new com.ltmonitor.jt809.protocol.receive.DownWarnMsgInformTips());// 报警预警信息
																							// 下级平台不需要应答
																							// han
		protocolMap
				.put(0x9401,
						new com.ltmonitor.jt809.protocol.receive.DownWarnMsgTodoReq());
		protocolMap.put(0x9400,
				new com.ltmonitor.jt809.protocol.receive.DownWarnMsg());
		protocolMap
				.put(0x9503,
						new com.ltmonitor.jt809.protocol.receive.DownCtrlMsgTextInfo()); // 下发消息到车辆
																							// han
		protocolMap
				.put(0x920A,
						new com.ltmonitor.jt809.protocol.receive.DownExgMsgReportDriverInfo());// 驾驶员身份信息
		protocolMap
				.put(0x920B,
						new com.ltmonitor.jt809.protocol.receive.DownExgMsgTakeEwayBillReq());// 电子运单
																								// han
		protocolMap
				.put(0x9101,
						new com.ltmonitor.jt809.protocol.receive.DownTotalRecvBackMsg());

		// 苏标
		protocolMap
				.put(0x9404,
						new com.ltmonitor.jt809.protocol.receive.DownWarnMsgFileListReq_Subiao());
		protocolMap
				.put(0x9405,
						new com.ltmonitor.jt809.protocol.receive.DownWarnMsgCheckReq_Subiao());
		protocolMap
				.put(0x9406,
						new com.ltmonitor.jt809.protocol.receive.DownWarnMsgStaticsReq_Subiao());
	}

}
