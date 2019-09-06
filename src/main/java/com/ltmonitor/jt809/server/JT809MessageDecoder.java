package com.ltmonitor.jt809.server;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.tool.Tools;


public class JT809MessageDecoder extends CumulativeProtocolDecoder {
	private Logger logger = Logger.getLogger(JT809MessageDecoder.class);
	private CharsetDecoder decoder;

	public JT809MessageDecoder(Charset charset) {
		this.decoder = charset.newDecoder();
	}

	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		while (in.remaining() > 0) {
			in.mark();
			int position = in.markValue();
			byte tag = in.get();


			if (tag == 91) {
				int i = 0;
				boolean isflag = false;
				while ((i < in.limit()) && (in.remaining() > 0)) {
					i++;
					byte b = in.get();
					if (b == 93) {
						position = in.position();
						isflag = true;
						break;
					}
				}

				if (!isflag) {
					in.reset();
					return false;
				}
				in.reset();
				byte[] tmp = new byte[position - in.markValue()];
				in.get(tmp);
				
				String content = Tools.ToHexString(tmp).toUpperCase();
				String totalMsgPacket = content;

				String headFlag = content.substring(0, 2);
				content = Tools.decoderStringEscape(tmp);
				String strLength = content.substring(2, 10);
				int len = Integer.valueOf(strLength, 16).intValue();
				content = content.substring(10, content.length());
				long msgSn = Long.valueOf(content.substring(0, 8), 16)
						.longValue();
				int msgId = Integer.valueOf(content.substring(8, 12), 16)
						.intValue();

				if(msgId != 0x1200)
				{
					StringBuilder sbLog = new StringBuilder();
					sbLog.append("↓[0x").append(Tools.ToHexString(msgId, 2)).append("],收到原始报文:").append(totalMsgPacket);
					logger.error(sbLog.toString());
				}
				long msgGpsCenterId = Long.valueOf(content.substring(12, 20),
						16).longValue();

				int encryptFlag = Integer
						.valueOf(content.substring(26, 28), 16).intValue();

				long encryptKey = Long
						.parseLong(content.substring(28, 36), 16);
				String msgBody = "";
				if ((len > 16) && (content.length() > 42))
					msgBody = content.substring(36, len * 2 - 16);
				else
					msgBody = "";
				String endFlag = content.substring(content.length() - 2,
						content.length());
				if (endFlag.equalsIgnoreCase("5D")) {
					JT809Message mm = new JT809Message(msgId);
					mm.setHeadFlag(headFlag);
					mm.setMsgLength(len);
					mm.setMsgType(msgId);
					mm.setMsgGNSSCenterID(msgGpsCenterId);
					mm.setMsgSN(msgSn);
					if ((encryptFlag == 1) && (msgBody.length() > 0)) {
						msgBody = Tools.encrypt(encryptKey, msgBody);
						logger.error("key"+encryptFlag);
					}

					mm.setPacketDescr(totalMsgPacket);
					mm.setMessageBody(msgBody);
					mm.setEndFlag(headFlag);
					mm.setEncryptFlag((byte) encryptFlag);
					mm.setEncryptKey(encryptKey);
					out.write(mm);

					return true;
				}

				return false;
			}

		}
		//logger.warn(sb.toString());

		return false;
	}
}
