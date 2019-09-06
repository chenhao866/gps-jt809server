package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

/**
 * 远程录像回放请求应答 0x1A01
 */
public class UpPlayBackMsgStartUpAck implements ISendProtocol
{
    private static Logger logger = Logger.getLogger(UpCtrlMsg.class);

    private int msgType = 0x1A00;
    private int subType = 0x1A01;
    private String plateNo;
    private int plateColor;
    private byte result;

    private String serverIp;

    private int serverPort;

    public UpPlayBackMsgStartUpAck(String plateNo, int plateColor, byte result, String _ip, int _port) {
        this.plateNo = plateNo;
        this.plateColor = plateColor;
        this.result = result;
        this.serverIp = _ip;
        this.serverPort = _port;
    }

    public JT809Message wrapper() {
        int dataLength = 1+32+2;
        StringBuilder sb = new StringBuilder();
        sb.append(Tools.ToHexString(plateNo, 21))
                .append(Tools.ToHexString(plateColor,1))
                .append(Tools.ToHexString(subType, 2))
                .append(Tools.ToHexString(dataLength, 4))
                .append(Tools.ToHexString(serverIp, 32))
                .append(Tools.ToHexString(serverPort, 2))
                .append(Tools.ToHexString(result, 1));

        String body = sb.toString();

        JT809Message mm = new JT809Message(msgType, subType,body);
        mm.setPlateColor(plateColor);
        mm.setPlateNo(plateNo);
        StringBuilder s = new StringBuilder();
        s.append("应答结果:").append(result).append(",服务器:")
                .append(serverIp).append(",")
                .append(serverPort);
        mm.setMsgDescr(s.toString());
        return mm;
    }
}
