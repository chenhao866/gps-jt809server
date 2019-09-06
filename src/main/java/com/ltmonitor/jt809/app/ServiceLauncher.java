package com.ltmonitor.jt809.app;

import com.ltmonitor.service.*;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ltmonitor.entity.PlatformState;
import com.ltmonitor.entity.VehicleData;
import com.ltmonitor.jt809.service.IJT809CommandParseService;

/**
 * Spring加载服务
 * @author admin
 *
 */
public class ServiceLauncher {

	private static Logger logger = Logger.getLogger(ServiceLauncher.class);
	protected static ApplicationContext context = null;

	private static IVehicleDataService vehicleDataService;

	private static IMemberInfoService  memberInfoService;

	private static IPlatformStateService platfromStateService;
	private static IDriverInfoService driverInfoService;
	
	private static IJT809CommandParseService jt809CommandParseService;
	private static ITerminalCommandService terminalCommandService;
	
	private static IEWayBillService eWayBillService;
	
	private static IWarnMsgUrgeTodoReqService warnMsgUrgeTodoReqService;

	private static IVideoServerConfigService videoServerConfigService;

	private static IVideoFileItemService videoFileItemService;

	private static IAdasAttachmentService adasAttachmentService;
	private static IAdasAlarmService adasAlarmService;
	private static IAlarmStatisticService alarmStatisticService;
	
	public static void launch() {

		// PropertyConfigurator.configure("log4j.properties");
		// context = new ClassPathXmlApplicationContext(
		// "classpath:applicationContextService.xml");
		context = new ClassPathXmlApplicationContext(new String[] {
				"spring.xml",
				"spring-hibernate.xml",
				"spring-rmi-service.xml",
				//"applicationContext-quartz.xml",
				"spring-mybatis.xml"
				});
		if (context == null) {
			int x = 0;
		}

		jt809CommandParseService = (IJT809CommandParseService) getBean("jt809CommandParseService");
		vehicleDataService = (IVehicleDataService) getBean("vehicleDataService");
		platfromStateService = (IPlatformStateService) getBean("platformStateService");
		memberInfoService = (IMemberInfoService) getBean("memberInfoService");
		terminalCommandService = (ITerminalCommandService) getBean("terminalCommandService");
		driverInfoService = (IDriverInfoService) getBean("driverInfoService");
		eWayBillService = (IEWayBillService) getBean("eWayBillService");
		warnMsgUrgeTodoReqService = (IWarnMsgUrgeTodoReqService) getBean("warnMsgUrgeTodoReqService");
		videoServerConfigService = (IVideoServerConfigService) getBean("videoServerConfigService");
		videoFileItemService = (IVideoFileItemService) getBean("videoFileItemService");
		adasAttachmentService = (IAdasAttachmentService) getBean("adasAttachmentService");
		adasAlarmService = (IAdasAlarmService) getBean("adasAlarmService");
		alarmStatisticService = (IAlarmStatisticService) getBean("alarmStatisticService");

	}

	public static Object getBean(String beanID) {
		return context.getBean(beanID);
	}

	public static void main(String[] args) {
		try {
			ServiceLauncher.launch();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex.getStackTrace());
		}
	}
	/**
	 * 809平台状态״̬
	 * @return
	 */
	public static PlatformState getPlateformState()
	{
		return platfromStateService.getPlatformState();
	}
	/**
	 * 更新809平台状态
	 * @param state
	 */
	public static void updatePlateformState(PlatformState state){
		platfromStateService.saveOrUpdate(state);
	}



	public static IJT809CommandParseService getJT809CommandParserService() {
		return jt809CommandParseService;
	}

	public static void setCommandService(IJT809CommandParseService commandService) {
		ServiceLauncher.jt809CommandParseService = commandService;
	}

	public static IVehicleDataService getVehicleDataService() {
		return vehicleDataService;
	}

	public static void setVehicleDataService(IVehicleDataService vehicleDataService) {
		ServiceLauncher.vehicleDataService = vehicleDataService;
	}

	public static IMemberInfoService getMemberInfoService() {
		return memberInfoService;
	}

	public static void setMemberInfoService(IMemberInfoService memberInfoService) {
		ServiceLauncher.memberInfoService = memberInfoService;
	}

	public static ITerminalCommandService getTerminalCommandService() {
		return terminalCommandService;
	}

	public static void setTerminalCommandService(ITerminalCommandService terminalCommandService) {
		ServiceLauncher.terminalCommandService = terminalCommandService;
	}

	public static IDriverInfoService getDriverInfoService() {
		return driverInfoService;
	}

	public static void setDriverInfoService(IDriverInfoService driverInfoService) {
		ServiceLauncher.driverInfoService = driverInfoService;
	}

	public static IEWayBillService geteWayBillService() {
		return eWayBillService;
	}

	public static void seteWayBillService(IEWayBillService eWayBillService) {
		ServiceLauncher.eWayBillService = eWayBillService;
	}

	public static IWarnMsgUrgeTodoReqService getWarnMsgUrgeTodoReqService() {
		return warnMsgUrgeTodoReqService;
	}

	public static void setWarnMsgUrgeTodoReqService(
			IWarnMsgUrgeTodoReqService warnMsgUrgeTodoReqService) {
		ServiceLauncher.warnMsgUrgeTodoReqService = warnMsgUrgeTodoReqService;
	}

	public static IVideoServerConfigService getVideoServerConfigService() {
		return videoServerConfigService;
	}

	public static IVideoFileItemService getVideoFileItemService() {
		return videoFileItemService;
	}

	public static void setVideoFileItemService(IVideoFileItemService videoFileItemService) {
		ServiceLauncher.videoFileItemService = videoFileItemService;
	}

	public static IAdasAttachmentService getAdasAttachmentService() {
		return adasAttachmentService;
	}

	public static IAdasAlarmService getAdasAlarmService() {
		return adasAlarmService;
	}

	public static IAlarmStatisticService getAlarmStatisticService() {
		return alarmStatisticService;
	}
}
