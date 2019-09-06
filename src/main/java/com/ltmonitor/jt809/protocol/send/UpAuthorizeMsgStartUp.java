package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

/**
 * 时效口令就上报消息
 */
public class UpAuthorizeMsgStartUp implements ISendProtocol
{
    private static Logger logger = Logger.getLogger(UpCtrlMsgEmergencyMonitoringAck.class);

    private int msgType = 0x1700;

    private int subType = 0x1701;

    private String platformId;

    /**
     * 地区口令
     */
    private String code1;
    /**
     * 跨域口令
     */
    private String code2;


    public UpAuthorizeMsgStartUp(String _platformId,String code1,String code2) {
        this.platformId = _platformId;
        this.code1 = code1;
        this.code2 = code2;
    }

    public JT809Message wrapper() {
        int dataLength = 1;
        StringBuilder sb = new StringBuilder();
        sb
                .append(Tools.ToHexString(subType, 2))
                .append(Tools.ToHexString(platformId, 11))
                .append(Tools.ToHexString(code1, 64)).append(Tools.ToHexString(code2, 64));

        //ParameterModel pm = GNSSImpl.parModel;
        String body = sb.toString();
        //String mess = Tools.getHeaderAndFlag(GNSSImpl.getSN(), body, msgType,
        //pm.getPlatformCenterId());
        JT809Message mm = new JT809Message(msgType, subType,body);
        return mm;
    }

}
