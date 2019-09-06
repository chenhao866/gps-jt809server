package com.ltmonitor.jt809.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.service.IRealDataService;
import com.ltmonitor.util.T809Constants;

/**
 * 服务器程序命令行入口类 如果是在linux上部署，就不能启动界面，只能在命令行启动整个程序
 * 
 * @author admin
 *
 */
public class MainService {

	private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(MainService.class);

	/**
	 * 不断的检测连接的最新上线时间
	 */
	private static Thread refreshJT809MessageThread;

	public static void main(String[] args) {

		String command = args[0];

		if ("start".equals(command)) {
			// int listenPort = Integer.parseInt(args[1]);

			try {
				System.out.println("服务器开始加载配置并连接数据库...");
				ServiceLauncher.launch();
				boolean res = start();
				if (res == false) {
					//StopServer();
					//return;
				}

				while (true) {
					String str = readDataFromConsole();
					if ("conn".equals(str)) {						
						T809Manager.UpConnectReq();
					} else if ("disconn".equals(str)) {						
						T809Manager.UpDisconnectReq();
					} else if ("quit".equals(str)) {
						StopServer();
						break;
					} else if ("stop".equals(str)) {
						StopServer();
						// break;
					} else if ("start".equals(str)) {
						res = start();
						// break;
					}

					Thread.sleep(1000);
				}

			} catch (Exception ex) {
				logger.error("服务器启动错误:" + ex.getMessage(), ex);
			}
		}

	}

	private static IRealDataService realDataService;

	public static boolean start() {
		boolean res = false;
		try {
			GlobalConfig.initSystem();
			try {
				res = T809Manager.StartServer();
				if (res == false) {
					System.out.println("无法连接上级平台");
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
			System.out.println("连接服务器"
					+ GlobalConfig.parModel.getPlatformPort() + "端口:"
					+ GlobalConfig.parModel.getPlatformPort() + ",启动"
					+ (res ? "成功" : "失败"));
			if (res) {
				res = T809Manager.UpConnectReq();
				if (res) {
					refreshJT809MessageThread = new Thread(new Runnable() {
						public void run() {
							refreshJT809MessageThreadFunc();
						}
					});
					refreshJT809MessageThread.start();
				}

			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return res;
	}

	private static void refreshJT809MessageThreadFunc() {
		long timerCount = 0;
		while (true) {
			JT809Message tm = GlobalConfig.pollMsg();
			while (tm != null) {
				showMsg(tm);
				tm = GlobalConfig.pollMsg();
			}
			timerCount++;
			if (timerCount > (Long.MAX_VALUE - 2))
				timerCount = 0;

			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
			}
		}
	}

	private static void showMsg(JT809Message tm) {
		/**
		 * if (this.hideHeartBeatLog) { if (tm.getMsgType() == 0x1005 ||
		 * tm.getMsgType() == 0x1006 || tm.getMsgType() == 0x1200 ||
		 * tm.getMsgType() == 0x1400 || tm.getMsgType() == 0x9005 ||
		 * tm.getMsgType() == 0x9006) return; }
		 */
		Date now = new Date();
		Integer subType = tm.getSubType() == 0 ? tm.getMsgType() : tm
				.getSubType();
		String subDescr = T809Constants.getMsgDescr(subType);

		subDescr = "[" + "0x" + Tools.ToHexString(subType, 2) + "]" + subDescr;

		String msgDescr = "0x" + Tools.ToHexString(tm.getMsgType(), 2);
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(String
				.format("消息类型：%1$s, 子类型：%2$s, 车牌号：%3$s, 车牌颜色：%4$s,消息内容：%5$s,报文：%6$s,时间：%7$s",
						msgDescr, subDescr, tm.getPlateNo(),
						tm.getPlateColor(), tm.getDescr(), tm.getPacketDescr(),
						now.toLocaleString()));
		logger.error(sBuilder.toString());

	}

	public static void StopServer() {
		T809Manager.StopServer();
		try {
			if (refreshJT809MessageThread != null)
				refreshJT809MessageThread.stop();
		} catch (Exception ex) {

		}
		logger.error("服务器已经终止");
	}

	private static String readDataFromConsole() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String str = null;
		try {
			// System.out.print(prompt);
			str = br.readLine();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

}
