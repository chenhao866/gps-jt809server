package com.ltmonitor.jt809.protocol.receive;

import com.ltmonitor.adas.entity.AdasAttachment;
import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;
import com.ltmonitor.util.StringUtil;
import com.ltmonitor.vo.AdasAlarmAttachmentInfo;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 5.2.2.3 主动安全报警附件目录请求消息
 链路类型：从链路
 消息方向：上级平台向下级平台
 业务类型标识：DOWN_WARN_MSG_FILELIST_REQ。（0x9404）
 描述: 上级平台向下级平台发送主动安全报警附件目录请求业务，其数据体定义见表 39。
 * 
 * @author DELL
 * 
 */
public class DownWarnMsgFileListReq_Subiao implements IReceiveProtocol {
	private Logger logger = Logger.getLogger(DownWarnMsgFileListReq_Subiao.class);

	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();

		MessageParser mp = new MessageParser(dataBody);
		message.setPlateNo(mp.getString(21));

		message.setPlateColor(mp.getInt(1));

		message.setSubType(mp.getInt(2));
		int dataLength = mp.getInt(4);

		String infoId = mp.getString(32);

		message.setMsgDescr("报警信息No:"  + infoId);
		try {
			//ServiceLauncher.getWarnMsgUrgeTodoReqService().save(wd);
			List<AdasAttachment> attachmentList = ServiceLauncher.getAdasAttachmentService().getAdasAttachments(infoId);
			List<AdasAlarmAttachmentInfo> attachments = new ArrayList<>();
			ParameterModel p = GlobalConfig.parModel;
			for(AdasAttachment a : attachmentList)
			{
				String filePath = a.getFilePath().replace("\\", "/");
				/**
				 * ftp://用户名：密码@站点地址
				 例如：ftp://test:test@192.168.0.1:21/profile  登录后转到profile文件夹
				 */
				StringBuilder ftpUrl = new StringBuilder();
				ftpUrl.append( "ftp://" )
						.append(p.getAdasAttachmentFtpUser()).append(":").append(p.getAdasAttachmentFtpPassword())
						.append("@").append(p.getAdasAttachmentFtpServerIp()).append(":").append(p.getAdasAttachmentFtpPort());
				if(StringUtil.isNullOrEmpty(p.getAdasAttachmentFtpPath()) == false)
					ftpUrl.append("/").append(p.getAdasAttachmentFtpPath());
				ftpUrl.append("/").append(filePath);

				AdasAlarmAttachmentInfo i = new AdasAlarmAttachmentInfo();
				i.setFileLength(a.getFileLength());
				i.setFileName(a.getFileName());
				i.setFileType(a.getFileType());
				i.setFileUrl(ftpUrl.toString());
				attachments.add(i);
			}
			T809Manager.UpWarnMsgFileListAck(message.getPlateNo(), message.getPlateColor(), infoId,
					p.getAdasAttachmentFtpServerIp(), p.getAdasAttachmentFtpPort(), p.getAdasAttachmentFtpUser(), p.getAdasAttachmentFtpPassword(),
			     attachments);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		
	
		return "";
	}

}
