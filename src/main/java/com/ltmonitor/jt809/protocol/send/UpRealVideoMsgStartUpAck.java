package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.model.ProtocolModel;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.ClassUtils;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

/**
 * 实时音视频请求应答
 */
public class UpRealVideoMsgStartUpAck implements ISendProtocol
{
    private static Logger logger = Logger.getLogger(UpCtrlMsg.class);

    private int msgType = 0x1800;
    private int subType = 0x1801;
    private String plateNo;
    private int plateColor;
    private byte result;

    private String serverIp;

    private int serverPort;

    public UpRealVideoMsgStartUpAck(String plateNo, int plateColor, byte result, String _ip, int _port) {
        this.plateNo = plateNo;
        this.plateColor = plateColor;
        this.result = result;
        this.serverIp = _ip;
        this.serverPort = _port;
    }

    public JT809Message wrapper() {
        int dataLength = 1+32 + 2;
        StringBuilder sb = new StringBuilder();
        sb.append(Tools.ToHexString(plateNo, 21))
                .append(Tools.ToHexString(plateColor,1))
                .append(Tools.ToHexString(subType, 2))
                .append(Tools.ToHexString(dataLength, 4))
                .append(Tools.ToHexString(result, 1))
                .append(Tools.ToHexString(serverIp, 32))
                .append(Tools.ToHexString(serverPort, 2));

        String body = sb.toString();

        JT809Message mm = new JT809Message(msgType, subType,body);
        mm.setPlateColor(plateColor);
        mm.setPlateNo(plateNo);
        return mm;
    }
}
