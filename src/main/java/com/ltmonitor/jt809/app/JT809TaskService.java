package com.ltmonitor.jt809.app;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.protocol.send.UpConnectReq;
import com.ltmonitor.jt809.protocol.send.UpExgMsgRealLocation;
import com.ltmonitor.jt809.protocol.send.UpExgMsgRegister;
import com.ltmonitor.jt809.protocol.send.UpLinkTestReq;
import com.ltmonitor.jt809.server.PlatformClient;


/**
 * 定时任务类，定时发送主链路心跳、断线重连和车辆注册
 * @author DELL
 *
 */
public class JT809TaskService {
	private Logger logger = Logger.getLogger(JT809TaskService.class);

	private static JT809TaskService instance = null;
	private int counter = 0;
	String message = "";
	public ScheduledExecutorService scheduleService = null;
	
	public JT809TaskService()
	{
	}

	public static final synchronized JT809TaskService getInstance() {
		if (instance == null) {
			instance = new JT809TaskService();
		}
		return instance;
	}
	
	
	public  final void Stop()
	{
		GlobalConfig.isTimerStart = false;
		try {
			if(scheduleService != null)
			scheduleService.shutdown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public  final boolean start() {

		if (!GlobalConfig.isTimerStart) {

			scheduleService = Executors.newScheduledThreadPool(1);
			run();
			GlobalConfig.isTimerStart = true;
		}
		return GlobalConfig.isTimerStart;
	}

	public void run() {
		scheduleService.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try
				{
					JT809TaskService.this.counter += 1;
					if (JT809TaskService.this.counter % 30 == 0) {
						//如果主链路中断或异常，按照协议要求，需要从链路发送主链路中断通知
						if (!PlatformClient.session.isConnected() )
						{
							if (T809Manager.subLinkConnected) {
								T809Manager.UpDisconnectMainLinkInform();
							}
							// PlatformClient.connect();

							if (GlobalConfig.normalDisconnect == false && GlobalConfig.autoReconnect) {
								T809Manager.StopServer();
								boolean res = T809Manager.StartServer();
								if (res)
									T809Manager.UpConnectReq();
							}

						}
					}
					if ((JT809TaskService.this.counter > 0 && JT809TaskService.this.counter % 30 == 0)
							&& (GlobalConfig.isConnection)) {
						if ((PlatformClient.session.isConnected())
								&& (!GlobalConfig.isOpenPlat)) {
							// 登录连接请求
							T809Manager.UpConnectReq();
						}
					}
					if ((GlobalConfig.isConnection) && (T809Manager.mainLinkConnected))
					{
							if (JT809TaskService.this.counter % 20 == 0) {
								//主链路心跳
								T809Manager.UpLinkTestReq();
							}
					}
			
				}catch(Exception ex){
					logger.error(ex.getMessage(),ex);
					
				}
			}
		}, 2L, 1L, TimeUnit.SECONDS);
	}
}
