 package com.ltmonitor.jt809.protocol.send;
 
 import org.apache.log4j.Logger;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;

 
 public class UpConnectReq
   implements ISendProtocol
 {
   private static Logger logger = Logger.getLogger(UpConnectReq.class);
 
   public JT809Message wrapper()
   {
     String outPut = "";
 
     String message = "";
 
     String dataBody = null;
 
     ParameterModel parModel = GlobalConfig.parModel;
 
//     long userId = parModel.getPlatformUserId();
//     String passWord = parModel.getPlatformPassword();
     long userId = 3456;
     String passWord = "zzas123";
     try
     { 
       String endUserName = Tools.ToHexString(userId+"", 4);
        String endPassWord = Tools.ToHexString(passWord, 8);
 
//       String endTurnPlatFormIp = Tools.ToHexString(parModel.getLocalIp(), 32);
//       String endPlatFormPort = Tools.ToHexString(parModel.getLocalPort(), 2);
       String endTurnPlatFormIp = Tools.ToHexString("127.0.0.1", 32);
       String endPlatFormPort = Tools.ToHexString("8899", 2);
 
       dataBody = endUserName + endPassWord + endTurnPlatFormIp + endPlatFormPort;
 
       return new JT809Message(0x1001, dataBody);
     }
     catch (Exception e) {
    	 logger.error(e.getMessage(),e);
     }
     return null;
   }
 }

