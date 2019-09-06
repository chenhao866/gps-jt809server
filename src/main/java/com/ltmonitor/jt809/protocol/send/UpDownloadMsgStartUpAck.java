package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

/**
 * 远程录像下载请求应答 0x1B01
 */
public class UpDownloadMsgStartUpAck implements ISendProtocol
{
    private static Logger logger = Logger.getLogger(UpCtrlMsg.class);

    private int msgType = 0x1B00;
    private int subType = 0x1B01;
    private String plateNo;
    private int plateColor;
    /**
     * 0:成功，1：失败，2：不支持，3：会话结束
     */
    private byte result;
    /**
     * 应答消息流水号
     */
    private short responseMsgId;


    public UpDownloadMsgStartUpAck(String plateNo, int plateColor, byte result,short responseMsgId) {
        this.plateNo = plateNo;
        this.plateColor = plateColor;
        this.result = result;
        this.responseMsgId = responseMsgId;
    }

    public JT809Message wrapper() {
        int dataLength = 1+2;
        StringBuilder sb = new StringBuilder();
        sb.append(Tools.ToHexString(plateNo, 21))
                .append(Tools.ToHexString(plateColor,1))
                .append(Tools.ToHexString(subType, 2))
                .append(Tools.ToHexString(dataLength, 4))
                .append(Tools.ToHexString(result, 1))
                .append(Tools.ToHexString(responseMsgId, 2));

        String body = sb.toString();

        JT809Message mm = new JT809Message(msgType, subType,body);
        mm.setPlateColor(plateColor);
        mm.setPlateNo(plateNo);
        mm.setMsgDescr("结果:" + result + ",上传消息流水号:" + responseMsgId);
        return mm;
    }
}
