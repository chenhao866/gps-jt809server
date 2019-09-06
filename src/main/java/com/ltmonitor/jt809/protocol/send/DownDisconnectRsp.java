 package com.ltmonitor.jt809.protocol.send;
 
 import org.apache.log4j.Logger;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;

 /**
  * 从链路断开应答
  *
  */
 public class DownDisconnectRsp
   implements ISendProtocol
 {
   private static Logger logger = Logger.getLogger(DownDisconnectRsp.class);
 
   public JT809Message wrapper()
   {
     return new JT809Message(0x9004);
   }
 }

