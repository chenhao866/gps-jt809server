package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.entity.SafetyTerminalInfo;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

/**
 * 5.2.1.1 主动上传车辆主动安全智能防控终端安装信息消息
 链路类型：主链路。
 消息方向：下级平台往上级平台。
 业务类型标识：UP_EXG_MSG_SAFETY_TERMINAL。(0x1240)
 描述：主动安全智能防控平台收到车辆安全智能防控终端安装信息，并确认符合要求后，
 向上级平台上传该车辆安全智能防控终端安装信息，其数据体规定见表 36。本条消息服务
 端无需应答。
 */
public class UpExgMsgSafetyTerminal_Subiao implements ISendProtocol {
	private static Logger logger = Logger.getLogger(UpExgMsgSafetyTerminal_Subiao.class);
	// 是否注册车辆表中的所有车辆
	private boolean RegisterAllVehicle = false;
	// 协议命令类型
	private int msgType = 0x1200;
	// 子类型
	private int subType = 0x1240;
	// 车辆数据
	private SafetyTerminalInfo safeTermianlInfo;

	public UpExgMsgSafetyTerminal_Subiao(SafetyTerminalInfo _vm) {
		safeTermianlInfo = _vm;
	}

	public JT809Message wrapper() {
		String vehicleNo = Tools.ToHexString(safeTermianlInfo.getPlateNo(), 21);
		String vehicleColor = Tools
				.ToHexString(safeTermianlInfo.getPlateColor(), 1);

		int dataLength = 11 + 50 + 30 + 20 + 8 + 50 + 20 + 20 + 1; // 后续数据长度
		StringBuilder data = new StringBuilder();
		data.append(vehicleNo).append(vehicleColor)
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4))
				.append(Tools.ToHexString(safeTermianlInfo.getPlateformId(), 11)) // 平台唯一编码
				.append(Tools.ToHexString(safeTermianlInfo.getProducer(), 50)) // 终端厂商
				.append(Tools.ToHexString(safeTermianlInfo.getTerminalModel(), 30)) // 终端型号
				.append(Tools.ToHexString(safeTermianlInfo.getTerminalId(), 20)) // 终端编号
				.append(Tools.ToHexString(safeTermianlInfo.getInstallTime())) // 终端编号
				.append(Tools.ToHexString(safeTermianlInfo.getInstallCompany(), 50)) // 终端编号
				.append(Tools.ToHexString(safeTermianlInfo.getContacts(), 20)) // 联系人
				.append(Tools.ToHexString(safeTermianlInfo.getTelephone(), 20)) // 联系电话
				.append(Tools.ToHexString(safeTermianlInfo.isComplianceRequirements()));

		JT809Message mm = new JT809Message(msgType, subType, data.toString());
		mm.setPlateColor(safeTermianlInfo.getPlateColor());
		mm.setPlateNo(safeTermianlInfo.getPlateNo());
		StringBuilder sb = new StringBuilder();
		sb.append(safeTermianlInfo.getPlateformId()).append(",")
				.append(safeTermianlInfo.getProducer()).append(",")
				.append(safeTermianlInfo.getTerminalModel()).append(",")
				.append(safeTermianlInfo.getTerminalId()).append(",")
				.append(safeTermianlInfo.getInstallTime()).append(",")
				.append(safeTermianlInfo.getInstallTime()).append(",")
				.append(safeTermianlInfo.getInstallCompany()).append(",")
				.append(safeTermianlInfo.getContacts()).append(",")
				.append(safeTermianlInfo.getTelephone()).append(",")
				.append(safeTermianlInfo.isComplianceRequirements()).append(",")
		;
		mm.setMsgDescr(sb.toString());
		return mm;
	}

}
