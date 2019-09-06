package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

/**
 * 远程录像下载完成通知 0x1B02
 */
public class UpDownloadMsgEndInform implements ISendProtocol
{
    private static Logger logger = Logger.getLogger(UpCtrlMsg.class);

    private int msgType = 0x1B00;
    private int subType = 0x1B02;
    private String plateNo;
    private int plateColor;
    private byte result;

    private short responseMsgSn;

    private String serverIp;

    private int serverPort;

    private String userName;

    private String password;

    private String filePath;

    public UpDownloadMsgEndInform(String plateNo, int plateColor, byte result, short _responseMsgSn, String _ip, int _port, String _userName, String _password, String _filePath) {
        this.plateNo = plateNo;
        this.plateColor = plateColor;
        this.result = result;
        this.responseMsgSn = _responseMsgSn;
        this.serverIp = _ip;
        this.serverPort = _port;
        this.userName = _userName;
        this.password = _password;
        this.filePath = _filePath;
    }

    public JT809Message wrapper() {
        int dataLength = 1 + 2 + 32 + 2 + 49 + 22 + 200;
        StringBuilder sb = new StringBuilder();
        sb.append(Tools.ToHexString(plateNo, 21))
                .append(Tools.ToHexString(plateColor,1))
                .append(Tools.ToHexString(subType, 2))
                .append(Tools.ToHexString(dataLength, 4))
                .append(Tools.ToHexString(result, 1))
                .append(Tools.ToHexString(this.responseMsgSn, 2))
                .append(Tools.ToHexString(serverIp, 32))
                .append(Tools.ToHexString(serverPort, 2))
                .append(Tools.ToHexString(userName, 49))
                .append(Tools.ToHexString(password, 22))
                .append(Tools.ToHexString(filePath, 200))
                ;

        String body = sb.toString();

        JT809Message mm = new JT809Message(msgType, subType,body);
        mm.setPlateColor(plateColor);
        mm.setPlateNo(plateNo);
        return mm;
    }
}
