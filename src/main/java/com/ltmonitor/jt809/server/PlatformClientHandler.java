 package com.ltmonitor.jt809.server;
 
 import com.ltmonitor.util.DateUtil;
 import com.ltmonitor.util.StringUtil;
 import com.ltmonitor.util.T809Constants;
 import com.ltmonitor.vo.AlarmMessage;
 import org.apache.log4j.Logger;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;

import com.ltmonitor.entity.JT809Command;
import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.service.IJT809CommandParseService;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.jms.core.MessageCreator;

 import javax.annotation.Resource;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.Session;


 public class PlatformClientHandler extends IoHandlerAdapter
 {
   private Logger logger = Logger.getLogger(PlatformClientHandler.class);

	private IJT809CommandParseService jt809CommandService;


     @Resource(name = "jmsTemplate")
     private JmsTemplate jmsTemplate;

     // 队列名gzframe.demo
     @Resource(name = "alarmQueueDestination")
     private Destination alarmQueueDestination;

   public void exceptionCaught(IoSession session, Throwable e)
     throws Exception
   {
	     this.logger.error("通讯时发生异常：" + e.getMessage(), e);
   }
 
   public void messageReceived(IoSession session, Object message)
     throws Exception
   {
     com.ltmonitor.jt809.server.PlatformClient.session = session;
     JT809Message mm = (JT809Message)message;
     MessageAction action = new MessageAction();
     try {
			action.ResolveHandler(session, mm);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
     if (mm.getMsgType() != 0x9005 && mm.getMsgType() != 0x1006) {
			JT809Command jc = new JT809Command();
			jc.setCmd(mm.getMsgType());
			jc.setSubCmd(mm.getSubType());
			jc.setPlateColor((byte) mm.getPlateColor());
			jc.setPlateNo(mm.getPlateNo());
			jc.setCmdData(mm.getDescr());
			jc.setSource(JT809Command.DOWN_NOTIFY);
			jc.setStatus(JT809Command.STATUS_RECEIVED);
			ParameterModel p = GlobalConfig.parModel;
			jc.setUserId((int)p.getPlatformCenterId());

			getJt809CommandService().save(jc);
		}
   }


     private void sendAlarmMessage(final JT809Command alarm) {
         logger.error("向队列发送了消息------------");
         String cmdData = (String) alarm.getCmdData();
         if (StringUtil.isNullOrEmpty(cmdData)) {
             String plateNo = "" + alarm.getPlateNo();
             if (StringUtil.isNullOrEmpty(plateNo) == false) {
                 cmdData = plateNo;
             } else
                 cmdData = "";
         }
         Integer cmdType = (Integer) alarm.getCmd();
         Integer subType = (Integer)alarm.getSubCmd();
         subType = subType == 0 ? cmdType : subType;
         String subDescr = T809Constants.getMsgDescr(subType);
         String strCmd = "0x" + Integer.toHexString(subType);
         final AlarmMessage am = new AlarmMessage();
         am.setDepId(0);
         am.setAlarmId(alarm.getEntityId());
         am.setAlarmType(strCmd);
         am.setPlateNo(subDescr);
         am.setAlarmDescr(cmdData);
         am.setPopupEnabled(true);
         // am.setAlarmSource(alarmSource);
         am.setAlarmTime(DateUtil.toStringByFormat(alarm.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
         am.setMessageType(AlarmMessage.JT809_MESSAGE);

         jmsTemplate.send(this.alarmQueueDestination, new MessageCreator() {
             public Message createMessage(Session session) throws JMSException {
                 return session.createObjectMessage(am);
             }
         });

     }


     public void messageSent(IoSession session, Object message)
     throws Exception
   {
   }
 
   public void sessionClosed(IoSession session)
     throws Exception
   {
	   T809Manager.mainLinkConnected = false;
     session.close(true);
     this.logger.info("与本地服务器断开连接：" + session.getId());
   }
 
   public void sessionCreated(IoSession session)
     throws Exception
   {
    // this.logger.info(session.getId() + "????");
   }
 
   public void sessionIdle(IoSession session, IdleStatus idle)
     throws Exception
   {
     //this.logger.info(" Idle");
   }
 
   public void sessionOpened(IoSession session)
     throws Exception
   {
     //this.logger.info("connection province server" + session.getId() + "??");
   }

public IJT809CommandParseService getJt809CommandService() {
	return jt809CommandService;
}

public void setJt809CommandService(IJT809CommandParseService jt809CommandService) {
	this.jt809CommandService = jt809CommandService;
}
 }

