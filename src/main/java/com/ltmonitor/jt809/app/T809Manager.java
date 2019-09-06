package com.ltmonitor.jt809.app;

import java.util.Date;
import java.util.List;

import com.ltmonitor.jt809.entity.SafetyTerminalInfo;
import com.ltmonitor.jt809.protocol.send.*;
import com.ltmonitor.jt809.tool.AuthorizeCodeGenerator;
import com.ltmonitor.video.entity.VideoFileItem;
import com.ltmonitor.vo.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.ltmonitor.entity.PlatformState;
import com.ltmonitor.jt809.entity.CheckRecord;
import com.ltmonitor.jt809.entity.DriverModel;
import com.ltmonitor.jt809.entity.VehicleRegisterInfo;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.VehicleModel;
import com.ltmonitor.jt809.server.LocalServer;
import com.ltmonitor.jt809.server.PlatformClient;
import com.ltmonitor.jt809.tool.Tools;

/**
 * 809服务
 * 
 * @author DELL
 * 
 */
public class T809Manager implements IT809Manager {

	private static Logger logger = Logger.getLogger(T809Manager.class);
	public static boolean mainLinkConnected = false;

	public static boolean subLinkConnected = false;

	public static boolean encrypt = false;

	public static boolean startCacheLocationReport = false;

	/**
	 * 加密键值
	 */

	@Value("#{config.encryptKey}")
	private long encryptKey = 0;
	/**
	 * 809加密算法中所需指定的三个密钥参数M1
	 */
	@Value("#{config.m1}")
	private int m1 = 0;
	/**
	 * 密钥参数A1
	 */
	@Value("#{config.a1}")
	private int a1 = 0;
	/**
	 * 密钥参数C1
	 */
	@Value("#{config.c1}")
	private int c1 = 0;

	/**
	 * 上级运管平台Ip
	 */
	@Value("#{config.govServerIp}")
	private String govServerIp;
	/**
	 * 运管服务器端口
	 */
	@Value("#{config.govServerPort}")
	private int govServerPort;
	/**
	 * 本地用于监听从链路的公网Ip地址
	 */
	@Value("#{config.localServerIp}")
	private String localServerIp;
	/**
	 * 本地服务器监听端口
	 */
	@Value("#{config.localServerPort}")
	private int localServerPort;
	/**
	 * 接入运管平台的用户账号
	 */
	@Value("#{config.platformUserId}")
	private long platformUserId;
	/**
	 * 接入密码
	 */
	@Value("#{config.platformPassword}")
	private String platformPassword;
	/**
	 * 唯一接入码
	 */
	@Value("#{config.platformId}")
	private long platformId;
	/**
	 * 运输编码
	 */
	@Value("#{config.licenseno}")
	private String licenseno;

	/**
	 * 主界面标题
	 */
	@Value("#{config.title}")
	private String title;

	/**
	 * 附件FTP服务器 IP
	 */
	@Value("#{config.adasAttachmentFtpServerIp}")
	private String adasAttachmentFtpServerIp;

	/**
	 * ftp端口
	 */
	@Value("#{config.adasAttachmentFtpPort}")
	private int adasAttachmentFtpPort;
	/**
	 * 附件FTP 服务器 用户名
	 */
	@Value("#{config.adasAttachmentFtpUser}")
	private String adasAttachmentFtpUser;
	/**
	 * 附件FTP服务器 密码
	 */
	@Value("#{config.adasAttachmentFtpPassword}")
	private String adasAttachmentFtpPassword;
	/**
	 * 附件服务器 ftp目录文件夹
	 */
	@Value("#{config.adasAttachmentFtpPath}")
	private String adasAttachmentFtpPath;


	public void init() {
		GlobalConfig.parModel.setMiyaoA(a1);
		GlobalConfig.parModel.setMiyaoM(m1);
		GlobalConfig.parModel.setMiyaoC(c1);
		GlobalConfig.parModel.setEncryptKey(this.encryptKey);
		GlobalConfig.parModel.setPlatformCenterId(platformId);
		GlobalConfig.parModel.setLocalIp(this.localServerIp);
		GlobalConfig.parModel.setLocalPort(this.localServerPort);
		GlobalConfig.parModel.setPlatformIP(this.govServerIp);
		GlobalConfig.parModel.setPlatformPort(govServerPort);
		GlobalConfig.parModel.setPlatformUserId(platformUserId);
		GlobalConfig.parModel.setPlatformPassword(platformPassword);
		GlobalConfig.parModel.setTitle(title);
		GlobalConfig.parModel.setLicenseNo(this.licenseno);


		GlobalConfig.parModel.setAdasAttachmentFtpServerIp(this.adasAttachmentFtpServerIp);
		GlobalConfig.parModel.setAdasAttachmentFtpPassword(this.adasAttachmentFtpPassword);
		GlobalConfig.parModel.setAdasAttachmentFtpUser(this.adasAttachmentFtpUser);
		GlobalConfig.parModel.setAdasAttachmentFtpPort(this.adasAttachmentFtpPort);
		GlobalConfig.parModel.setAdasAttachmentFtpPath(this.adasAttachmentFtpPath);

	}


	public static void setMainLinkState(boolean connect, String errorMsg) {
		try {
			mainLinkConnected = connect;
			mainLinkConnected = connect;
			PlatformState ps = ServiceLauncher.getPlateformState();
			if (connect == false) {
				ps.setMainLinkState("连接错误:" + errorMsg);
			} else {
				ps.setMainLinkState("连接成功");
				ps.setMainLinkDate(new Date());
			}
			if (T809Manager.subLinkConnected) {
				ps.setSubLinkState("连接成功");
				ps.setSubLinkDate(new Date());
			}
			ServiceLauncher.updatePlateformState(ps);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void setSubLinkState(boolean connect, String errorMsg) {
		try {
			subLinkConnected = connect;
			PlatformState ps = ServiceLauncher.getPlateformState();
			if (connect == false) {
				ps.setSubLinkState(errorMsg);
			} else {
				ps.setSubLinkState("连接成功");
				ps.setSubLinkDate(new Date());
			}

			if (T809Manager.mainLinkConnected) {
				ps.setMainLinkState("连接成功");
				ps.setMainLinkDate(new Date());
			}
			ServiceLauncher.updatePlateformState(ps);
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
		}

	}

	/**
	 * 申请交换指定车辆定位信息
	 *
	 * @param PlateNo 车牌号
	 * @param color   颜色
	 * @param start   开始时间
	 * @param end     结束时间
	 */
	public static boolean UpExgMsgApplyForMonitorStartup(String PlateNo,
														 byte color, Date start, Date end) {
		UpExgMsgApplyForMonitorStartup up1207 = new UpExgMsgApplyForMonitorStartup(
				PlateNo, color, start, end);
		JT809Message msg = up1207.wrapper();
		return Send(msg);
	}

	/**
	 * 主链路连接请求1001
	 *
	 * @return
	 */
	public static boolean UpConnectReq() {
		// 如果主链路连接失败，则不用发送请求
		if (PlatformClient.session == null
				|| (PlatformClient.session.isConnected() == false && subLinkConnected == false)) {
			boolean res = T809Manager.StartServer();
			if (res == false)
				return false;
		}

		UpConnectReq up1001 = new UpConnectReq();

		JT809Message message = up1001.wrapper();
		return Send(message);
	}

	/**
	 * 主动关闭主从链路通知1008
	 *
	 * @return
	 */
	public static boolean UpCloseLinkInform() {
		UpCloseLinkInform up1008 = new UpCloseLinkInform();
		JT809Message msg = up1008.wrapper();
		return SendFromSubLink(msg);
		// T809Manager.StopServer();
	}

	/**
	 * 主链路断开通知1007 从连路发送
	 *
	 * @return
	 */
	public static boolean UpDisconnectMainLinkInform() {
		UpDisconnectInform up1007 = new UpDisconnectInform();
		JT809Message msg = up1007.wrapper();
		return SendFromSubLink(msg);
	}

	/**
	 * 主链路注销请求
	 *
	 * @return
	 */
	public static boolean UpDisconnectReq() {
		UpDisconnectReq up1003 = new UpDisconnectReq();
		JT809Message msg = up1003.wrapper();
		return Send(msg);
	}

	/**
	 * 主链路心跳测试1005
	 *
	 * @return
	 */
	public static boolean UpLinkTestReq() {
		UpLinkTestReq up1005 = new UpLinkTestReq();
		JT809Message mess = up1005.wrapper();
		return Send(mess);
	}

	/**
	 * 请求主动补发指定车辆定位信息 1209
	 *
	 * @param PlateNo
	 * @param color
	 * @param start   开始时间
	 * @param end     结束时间
	 */
	public static boolean UpExgMsgApplyHisGnssDataReq(String PlateNo,
													  byte color, Date start, Date end) {
		UpExgMsgApplyHisGnssDataReq up1209 = new UpExgMsgApplyHisGnssDataReq(
				PlateNo, color, start, end);
		JT809Message msg = up1209.wrapper();
		return Send(msg);
	}

	/**
	 * 取消申请交换指定车辆请求1208
	 *
	 * @param PlateNo
	 * @param color
	 * @return
	 */
	public static boolean UpExgMsgApplyForMonitorEnd(String PlateNo, byte color) {
		UpExgMsgApplyForMonitorEnd up1208 = new UpExgMsgApplyForMonitorEnd(
				PlateNo, color);
		JT809Message mess = up1208.wrapper();
		return Send(mess);
	}

	/**
	 * 静态车辆注册1201
	 *
	 * @param vm
	 * @return
	 */
	public static boolean UpExgMsgRegister(VehicleRegisterInfo vm) {
		vm.setPlateformId("" + GlobalConfig.parModel.getPlatformCenterId());
		UpExgMsgRegister um1201 = new UpExgMsgRegister(vm);
		JT809Message mess = um1201.wrapper();
		return Send(mess);
	}

	/**
	 * 实时上传车辆定位消息 1202
	 *
	 * @param gnssData
	 * @return
	 */
	public static boolean UpExgMsgRealLocation(GnssData gnssData) {
		// 如果链路断开，则需要压入到队列中，等待补发
		if (mainLinkConnected == false && subLinkConnected == false) {
			return false;
		}
		UpExgMsgRealLocation real1202 = new UpExgMsgRealLocation(gnssData);
		JT809Message mess = real1202.wrapper();
		boolean res = Send(mess);
		if (res) {
			GlobalConfig.totalLocationPacketNum++;
		} else {
			logger.error("实时定位数据发送失败");
		}
		return res;
	}

	/**
	 * 定位信息自动补报1203
	 *
	 * @param gnssDatas
	 * @return
	 */
	public static boolean UpExgMsgHistoryLocations(String plateNo,
												   int plateColor, List<GnssData> gnssDatas) {
		UpExgMsgHistoryLocation real1203 = new UpExgMsgHistoryLocation(plateNo,
				plateColor, gnssDatas);
		JT809Message mess = real1203.wrapper();
		boolean res = Send(mess);
		if (res) {
			GlobalConfig.totalLocationPacketNum += gnssDatas.size();
			GlobalConfig.totalHisLocationPacketNum += gnssDatas.size();
		} else {
			logger.error("历史定位数据发送失败,数量:" + gnssDatas.size());
		}
		return res;
	}

	/**
	 * 上报报警信息 1402
	 *
	 * @param wd
	 * @return
	 */
	public static boolean UpWarnMsgAdptInfo(WarnData wd) {
		UpWarnMsgAdptInfo real1402 = new UpWarnMsgAdptInfo(wd);
		JT809Message mess = real1402.wrapper();
		return Send(mess);
	}

	/**
	 * 上报报警處理結果信息 1403
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param infoId
	 * @param result
	 * @return
	 */
	public static boolean UpWarnMsgAdptToDoInfo(String plateNo, int plateColor,
												long infoId, int result) {
		UpWarnMsgAdptToDoInfo real1403 = new UpWarnMsgAdptToDoInfo(plateNo,
				plateColor, infoId, result);
		JT809Message mess = real1403.wrapper();
		return Send(mess);
	}

	/**
	 * 主动上报电子运单消息 120D
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param eContent
	 * @return
	 */
	public static boolean UpExgMsgReportTakeEWayBill(String plateNo,
													 int plateColor, String eContent) {
		UpExgMsgReportTakeEWayBill up120D = new UpExgMsgReportTakeEWayBill(
				plateNo, plateColor, eContent);
		JT809Message mess = up120D.wrapper();
		return Send(mess);
	}

	/**
	 * 向上级平台发送数据
	 *
	 * @param msg
	 * @return
	 */
	private static boolean Send(String msg) {
		PlatformClient pc = PlatformClient.getInstance();
		// 优先使用主链路发送数据
		boolean res = true;
		if (PlatformClient.Send(msg) == false) {
			res = LocalServer.Send(msg);
			logger.error("主链路发送不成功，从从链路发送:" + (res ? "成功" : "失败"));
		}
		return res;
	}

	/**
	 * 向上级平台发送数据
	 *
	 * @param msg
	 * @return
	 */
	public static boolean Send(JT809Message msg) {
		String strMsg = Tools.getHeaderAndFlag(GlobalConfig.getSN(),
				msg.getMessageBody(), msg.getMsgType(),
				msg.getMsgGNSSCenterID(), T809Manager.encrypt);
		msg.setPacketDescr(strMsg);
		GlobalConfig.putMsg(msg);
		return Send(strMsg);
	}

	/**
	 * 启动主链路连接和从链路监听
	 */
	public static boolean StartServer() {
		// 主链路连接
		PlatformClient pc = PlatformClient.getInstance();

		boolean res = pc.start();
		if (res) {
			LocalServer ls = LocalServer.getInstance();
			// 从链路监听
			res = ls.start();
			// 启动定时器
			JT809TaskService.getInstance().start();

		}
		return res;
	}

	/**
	 * 停止服务
	 */
	public static void StopServer() {
		try {
			// 向上级平台发送主动关闭主从链路通知服务
			UpCloseLinkInform();
			JT809TaskService.getInstance().Stop();

			PlatformClient pc = PlatformClient.getInstance();
			pc.Stop();
			LocalServer ls = LocalServer.getInstance();
			ls.Stop();

			T809Manager.setSubLinkState(false, "从链路主动断开");
			T809Manager.setMainLinkState(false, "主链路主动断开");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	/**
	 * 关闭从链路
	 */
	public static void CloseSubLink() {
		try {
			LocalServer ls = LocalServer.getInstance();
			ls.Stop();

			T809Manager.setSubLinkState(false, "从链路主动断开");
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * 关闭主连接
	 */
	public static void CloseMainLink() {
		try {
			PlatformClient pc = PlatformClient.getInstance();
			pc.Stop();
			GlobalConfig.isOpenPlat = false;
			GlobalConfig.isRegist = false;
			T809Manager.mainLinkConnected = false;
			T809Manager.setMainLinkState(false, "主链路主动断开");
			logger.error("主链路主动断开");

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	// *******************一下为自动应答接口***************************************/

	/**
	 * 对从链路连接请求信息的应答，从连路发送
	 */
	public static boolean DownConnectRsp() {

		DownConnectRsp up9002 = new DownConnectRsp();
		JT809Message mess = up9002.wrapper();
		return SendFromSubLink(mess);
	}

	/**
	 * 从从链路 向上级平台发送数据
	 *
	 * @param msg
	 * @return
	 */
	private static boolean SendFromSubLink(String msg) {
		// 优先使用主链路发送数据
		boolean res = LocalServer.Send(msg);
		if (res == false) {
			PlatformClient pc = PlatformClient.getInstance();
			res = PlatformClient.Send(msg);
		}
		return res;
	}

	/**
	 * 从从链路 向上级平台发送数据
	 *
	 * @param msg
	 * @return
	 */
	public static boolean SendFromSubLink(JT809Message msg) {
		String strMsg = Tools.getHeaderAndFlag(GlobalConfig.getSN(),
				msg.getMessageBody(), msg.getMsgType(),
				msg.getMsgGNSSCenterID(), T809Manager.encrypt);
		msg.setPacketDescr(strMsg);
		GlobalConfig.putMsg(msg);
		return SendFromSubLink(strMsg);
	}


	/**
	 * 对从链路注销9003请求信息的应答
	 */
	public static boolean DownDisconnectRsp() {

		DownDisconnectRsp up9004 = new DownDisconnectRsp();
		JT809Message mess = up9004.wrapper();
		return SendFromSubLink(mess);
	}

	/**
	 * 对从链路保持信息的应答
	 */
	public static boolean DownLinkTestRsp() {

		DownLinkTestRsp up9006 = new DownLinkTestRsp();
		JT809Message mess = up9006.wrapper();
		return SendFromSubLink(mess);
	}

	/**
	 * 对上级平台启动交换车辆定位信息的应答
	 *
	 * @param plateNo
	 * @param plateColor
	 * @return
	 */
	public static boolean UpExgMsgReturnStartUpAck(String plateNo,
												   int plateColor) {

		UpExgMsgReturnStartUpAck up1205 = new UpExgMsgReturnStartUpAck(plateNo,
				plateColor);
		JT809Message mess = up1205.wrapper();
		return Send(mess);
	}

	/**
	 * 对上级平台结束交换车辆定位信息的应答
	 *
	 * @param plateNo
	 * @param plateColor
	 * @return
	 */
	public static boolean UpExgMsgReturnEndAck(String plateNo, int plateColor) {

		UpExgMsgReturnEndAck up1206 = new UpExgMsgReturnEndAck(plateNo,
				plateColor);
		JT809Message mess = up1206.wrapper();
		return Send(mess);
	}

	/**
	 * 对上级平台报文信息的应答
	 *
	 * @param plateNo
	 * @param plateColor
	 * @return
	 */
	public static boolean UpCtrlMsgTextInfoAck(String plateNo, int plateColor,
											   int msgId, byte result) {

		UpCtrlMsgTextInfoAck up1503 = new UpCtrlMsgTextInfoAck(plateNo,
				plateColor, msgId, result);
		JT809Message mess = up1503.wrapper();
		return Send(mess);
	}

	/**
	 * 对上级平台单向监听的应答
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	public static boolean UpCtrlMsgMonitorVehicleAck(String plateNo,
													 int plateColor, byte result) {

		UpCtrlMsgMonitorVehicleAck up1501 = new UpCtrlMsgMonitorVehicleAck(
				plateNo, plateColor, result);
		JT809Message mess = up1501.wrapper();
		return Send(mess);
	}

	/**
	 * 对上级平台车辆应急接入的应答
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	public static boolean UpCtrlMsgEmergencyMonitoringAck(String plateNo,
														  int plateColor, byte result) {

		UpCtrlMsgEmergencyMonitoringAck up1505 = new UpCtrlMsgEmergencyMonitoringAck(
				plateNo, plateColor, result);
		JT809Message mess = up1505.wrapper();
		return Send(mess);
	}

	/**
	 * 对上级平台车辆行车记录仪请求的应答 1504
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param cmdType    行车记录仪命令字
	 * @return
	 */
	public static boolean UpCtrlMsgTakeTravelAck(String plateNo,
												 int plateColor, byte cmdType, byte[] cmdData) {

		UpCtrlMsgTakeTravelAck up1504 = new UpCtrlMsgTakeTravelAck(plateNo,
				plateColor, cmdType, cmdData);
		JT809Message mess = up1504.wrapper();
		return Send(mess);
	}

	/**
	 * 对上级平台请求驾驶员身份的应答
	 *
	 * @param dm
	 * @return
	 */
	public static boolean UpExgMsgReportDriverAck(DriverModel dm) {
		UpExgMsgReportDriverAck up120a = new UpExgMsgReportDriverAck(dm);
		JT809Message mess = up120a.wrapper();
		return Send(mess);
	}

	/**
	 * 主动上报驾驶员身份
	 *
	 * @param dm
	 * @return
	 */
	public static boolean UpExgMsgReportDriverInfo(DriverModel dm) {
		UpExgMsgReportDriverInfo up120c = new UpExgMsgReportDriverInfo(dm);
		JT809Message mess = up120c.wrapper();
		return Send(mess);
	}

	/**
	 * 上报电子运单请求消息应答 120B
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param eContent
	 * @return
	 */
	public static boolean UpExgMsgTakeEWayBillAck(String plateNo,
												  int plateColor, String eContent) {
		UpExgMsgTakeEWayBillAck up120B = new UpExgMsgTakeEWayBillAck(plateNo,
				plateColor, eContent);
		JT809Message mess = up120B.wrapper();
		return Send(mess);
	}

	/**
	 * 补报车辆静信息应答 1601
	 *
	 * @param dm
	 * @return
	 */
	public static boolean UpBaseMsgVehicleAddedAck(VehicleModel dm) {
		UpBaseMsgVehicleAddedAck up1601 = new UpBaseMsgVehicleAddedAck(dm);
		JT809Message mess = up1601.wrapper();
		return Send(mess);
	}

	/**
	 * 平台查岗应答 1301
	 *
	 * @param dm
	 * @return
	 */
	public static boolean UpPlatFormMsgPostQueryAck(CheckRecord dm) {
		UpPlatFormMsgPostQueryAck up1301 = new UpPlatFormMsgPostQueryAck(dm);
		JT809Message mess = up1301.wrapper();
		return Send(mess);
	}

	/**
	 * 平台间报文消息应答1302
	 *
	 * @param infoId
	 * @return
	 */
	public static boolean UpPlatFormMsgInfoAck(int infoId) {
		UpPlatFormMsgInfoAck up1302 = new UpPlatFormMsgInfoAck(infoId);
		JT809Message mess = up1302.wrapper();
		return Send(mess);
	}

	/**
	 * 对拍照命令的应答
	 *
	 * @param _photo
	 * @return
	 */
	public static boolean UpCtrlMsgTakePhotoAck(TakePhotoModel _photo) {
		UpCtrlMsgTakePhotoAck up1502 = new UpCtrlMsgTakePhotoAck(_photo);
		JT809Message mm = up1502.wrapper();
		return Send(mm);
	}

	/**
	 * 报警督办应答
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param superviseId 督办ID
	 * @param result      督办结果
	 * @return
	 */
	public static boolean UpWarnMsgUrgeToDoAck(String plateNo, int plateColor,
											   int superviseId, byte result) {

		UpWarnMsgUrgeToDoAck up1401 = new UpWarnMsgUrgeToDoAck(plateNo,
				plateColor, superviseId, result);
		JT809Message mess = up1401.wrapper();
		return Send(mess);
	}

	/**
	 * 时效口令上报消息  0x1701
	 *
	 * @return
	 */
	public static boolean UpAuthorizeMsgStartUp() {
		String plateformId = "" + GlobalConfig.parModel.getPlatformCenterId();
		String code1 = AuthorizeCodeGenerator.create();
		String code2 = AuthorizeCodeGenerator.create();
		PlatformState ps = ServiceLauncher.getPlateformState();
		ps.setAuthorizeCode1(code1);
		ps.setAuthorizeCode2(code2);

		GlobalConfig.authorizeCode = code1;

		ServiceLauncher.updatePlateformState(ps);

		UpAuthorizeMsgStartUp up1701 = new UpAuthorizeMsgStartUp(plateformId, code1, code2);
		JT809Message mess = up1701.wrapper();
		boolean res = Send(mess);

		if (res) {

		}
		return res;
	}

	/**
	 * 实时视频请求应答  0x1801
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param videoServerIp
	 * @param port
	 * @return
	 */
	public static boolean UpRealVideoMsgStartUpAck(String plateNo, int plateColor,
												   byte result, String videoServerIp, int port) {

		UpRealVideoMsgStartUpAck up1801 = new UpRealVideoMsgStartUpAck(plateNo,
				plateColor, result, videoServerIp, port);
		JT809Message mess = up1801.wrapper();
		return Send(mess);
	}

	/**
	 * 终止实时音视频传输应答 0x1802
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	public static boolean UpRealVideoMsgEndAck(String plateNo, int plateColor,
											   byte result) {

		UpRealVideoMsgEndAck up1802 = new UpRealVideoMsgEndAck(plateNo,
				plateColor, result);
		JT809Message mess = up1802.wrapper();
		return Send(mess);
	}

	/**
	 * 主动上传音视频资源目录 0x1901
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param videoFileItems
	 * @return
	 */
	public static boolean UpFileListMsg(String plateNo, int plateColor,
										List<VideoFileItem> videoFileItems) {

		UpFileListMsg up1901 = new UpFileListMsg(plateNo,
				plateColor, videoFileItems);
		JT809Message mess = up1901.wrapper();
		return Send(mess);
	}

	/**
	 * 上传音视频资源目录请求应答消息 0x1902
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param videoFileItems
	 * @return
	 */
	public static boolean UpRealVideoFileListReqAck(String plateNo, int plateColor, int result,
													List<VideoFileItem> videoFileItems) {

		UpRealVideoFileListReqAck up1902 = new UpRealVideoFileListReqAck(plateNo,
				plateColor, result, videoFileItems);
		JT809Message mess = up1902.wrapper();
		return Send(mess);
	}

	/**
	 * 远程录像回放请求应答 0x1A01
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param videoServerIp
	 * @param port
	 * @return
	 */
	public static boolean UpPlayBackMsgStartUpAck(String plateNo, int plateColor,
												  byte result, String videoServerIp, int port) {

		UpPlayBackMsgStartUpAck up1A01 = new UpPlayBackMsgStartUpAck(plateNo,
				plateColor, result, videoServerIp, port);
		JT809Message mess = up1A01.wrapper();
		return Send(mess);
	}

	/**
	 * 远程录像回放控制应答 0x1A02
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	public static boolean UpPlayBackMsgControlAck(String plateNo, int plateColor,
												  byte result) {

		UpPlayBackMsgControlAck up1A02 = new UpPlayBackMsgControlAck(plateNo,
				plateColor, result);
		JT809Message mess = up1A02.wrapper();
		return Send(mess);
	}

	/**
	 * 远程录像下载请求应答 0x1B01
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @param responseMsgSn
	 * @return
	 */
	public static boolean UpDownloadMsgStartUpAck(String plateNo, int plateColor,
												  byte result, short responseMsgSn) {

		UpDownloadMsgStartUpAck up1B01 = new UpDownloadMsgStartUpAck(plateNo,
				plateColor, result, responseMsgSn);
		JT809Message mess = up1B01.wrapper();
		return Send(mess);
	}

	/**
	 * 远程录像下载完成通知 0x1B02
	 *
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
	public static boolean UpDownloadMsgEndInform(String plateNo, int plateColor, byte result,
												 short responseMsgSn, String _ip, int _port, String _userName,
												 String _password, String _filePath) {

		UpDownloadMsgEndInform up1B02 = new UpDownloadMsgEndInform(plateNo,
				plateColor, result, responseMsgSn, _ip, _port, _userName,
				_password, _filePath);
		JT809Message mess = up1B02.wrapper();
		return Send(mess);
	}

	/**
	 * 录像下载控制应答消息 0x1B03
	 *
	 * @param plateNo
	 * @param plateColor
	 * @param result
	 * @return
	 */
	public static boolean UpDownloadMsgControlAck(String plateNo, int plateColor,
												  byte result) {

		UpDownloadMsgControlAck up1B03 = new UpDownloadMsgControlAck(plateNo,
				plateColor, result);
		JT809Message mess = up1B03.wrapper();
		return Send(mess);
	}

	public static boolean UpExgMsgSafetyTerminal_Subiao(SafetyTerminalInfo sm) {

		UpExgMsgSafetyTerminal_Subiao up1240 = new UpExgMsgSafetyTerminal_Subiao(sm);
		JT809Message mess = up1240.wrapper();
		return Send(mess);
	}


	public static boolean UpWarnMsgInfo_Subiao(AdasWarnData sm) {

		UpWarnMsgInfo_Subiao up1402 = new UpWarnMsgInfo_Subiao(sm);
		JT809Message mess = up1402.wrapper();
		return Send(mess);
	}

	public static boolean UpWarnMsgCheckAck_Subiao(AdasWarnData sm) {

		UpWarnMsgCheckAck_Subiao up1405 = new UpWarnMsgCheckAck_Subiao(sm);
		JT809Message mess = up1405.wrapper();
		return Send(mess);
	}

	public static boolean UpWarnMsgOperationInfo(AdasAlarmProcessResult r) {

		UpWarnMsgOperationInfo_Subiao up1403 = new UpWarnMsgOperationInfo_Subiao(r);
		JT809Message mess = up1403.wrapper();
		return Send(mess);
	}

	public static boolean UpWarnMsgStaticsAck_Subiao(String _plateNo, int _plateColor, List<AdasWarnStaticsItem> wd) {

		UpWarnMsgStaticsAck_Subiao up1406 = new UpWarnMsgStaticsAck_Subiao(_plateNo, _plateColor, wd);
		JT809Message mess = up1406.wrapper();
		return Send(mess);
	}

	public static boolean UpWarnMsgFileListAck(String plateNo, int plateColor, String _infoId,
											   String _ip, int _port, String _userName,
											   String _password, List<AdasAlarmAttachmentInfo> attachments) {


		UpWarnMsgFileListAck up1404 = new UpWarnMsgFileListAck(plateNo,  plateColor, _infoId,
				_ip,  _port, _userName,
				_password,  attachments);
		JT809Message mess = up1404.wrapper();
		return Send(mess);


	}


}
