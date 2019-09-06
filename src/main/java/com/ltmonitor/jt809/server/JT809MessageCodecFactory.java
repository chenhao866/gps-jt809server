 package com.ltmonitor.jt809.server;
 
 import java.nio.charset.Charset;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolDecoder;
 import org.apache.mina.filter.codec.ProtocolEncoder;
 
 public class JT809MessageCodecFactory
   implements ProtocolCodecFactory
 {
   private final JT809MessageDecoder decoder;
   private final JT809MessageEncoder encoder;
 
   public JT809MessageCodecFactory()
   {
     this.decoder = new JT809MessageDecoder(Charset.forName("utf-8"));
     this.encoder = new JT809MessageEncoder(Charset.forName("utf-8"));
   }
 
   public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
     return this.decoder;
   }
 
   public ProtocolEncoder getEncoder(IoSession arg0) throws Exception
   {
     return this.encoder;
   }
 }

