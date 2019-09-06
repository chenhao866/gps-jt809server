package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.vo.AdasAlarmAttachmentInfo;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 5.2.2.4 主动安全报警附件目录请求应答
 链路类型：主链路
 消息方向：下级平台向上级平台
 业务类型标识： UP_WARN_MSG_FILELIST_ACK。（0x1404）
 描述:下级平台向上级平台发送主动安全报警附件目录请求应答业务，上级平台可通过
 报警附件文件 URL 以 FTP 协议自行下载报警附件文件，其数据体定义见表 40。
 */
public class UpWarnMsgFileListAck implements ISendProtocol
{
    private static Logger logger = Logger.getLogger(UpCtrlMsg.class);

    private int msgType = 0x1400;
    private int subType = 0x1404;
    private String plateNo;
    private int plateColor;
    /**
     * 32 Octet String 报警信息 ID
     */
    private String infoId;

    /**
     * 附件服务器 IP 或域名
     */
    private String serverIp;

    /**
     * 附件服务器 FTP 协议端口号
     */
    private int serverPort;

    private String userName;

    private String password;

    private List<AdasAlarmAttachmentInfo> attachmentInfoList = new ArrayList<AdasAlarmAttachmentInfo>();

    public UpWarnMsgFileListAck(String plateNo, int plateColor, String _infoId,
                                String _ip, int _port, String _userName, String _password, List<AdasAlarmAttachmentInfo> attachments) {
        this.plateNo = plateNo;
        this.plateColor = plateColor;
        this.infoId = _infoId;
        this.serverIp = _ip;
        this.serverPort = _port;
        this.userName = _userName;
        this.password = _password;
        attachmentInfoList = attachments;
    }

    public JT809Message wrapper() {
        StringBuilder sb = new StringBuilder();
        sb.append(Tools.ToHexString(infoId, 32))
                .append(Tools.ToHexStringWithLengthHex(serverIp))
                .append(Tools.ToHexString(serverPort, 2))
                .append(Tools.ToHexStringWithLengthHex(userName))
                .append(Tools.ToHexStringWithLengthHex(password))
                .append(Tools.ToHexString(attachmentInfoList.size(), 1))          ;


        StringBuilder descr = new StringBuilder();
        descr.append("报警信息ID:").append(infoId).append(",附件服务器IP")
                .append(serverIp).append(",端口:")
                .append(serverPort).append(",Ftp用户名称:")
                .append(userName).append(",密码:")
                .append(password).append(",附件数:")
                .append(attachmentInfoList.size())          ;

        int m = 1;
        for(AdasAlarmAttachmentInfo a:attachmentInfoList)
        {
            sb.append(Tools.ToHexStringWithLengthHex(a.getFileName()))
                    .append(Tools.ToHexString(a.getFileType(),1))
                    .append(Tools.ToHexString(a.getFileLength(), 4))
                    .append(Tools.ToHexStringWithLengthHex(a.getFileUrl()));

            descr.append(",附件").append(m).append(":").append(a.toString());
        }



        int dataLength = sb.length() / 2;
        StringBuilder sbPacket = new StringBuilder();
        sbPacket.append(Tools.ToHexString(plateNo, 21))
                .append(Tools.ToHexString(plateColor,1))
                .append(Tools.ToHexString(subType, 2))
                .append(Tools.ToHexString(dataLength, 4))
                .append(sb.toString());



        String body = sbPacket.toString();

        JT809Message mm = new JT809Message(msgType, subType,body);
        mm.setPlateColor(plateColor);
        mm.setPlateNo(plateNo);
        mm.setMsgDescr(descr.toString());
        return mm;
    }
}
