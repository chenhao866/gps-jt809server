package com.ltmonitor.jt809.protocol.receive;

import org.apache.log4j.Logger;

import com.ltmonitor.entity.EWayBill;
import com.ltmonitor.entity.VehicleData;
import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;

/**
 * 
 * 电子运单请求
 * @author DELL
 * 
 */
public class DownExgMsgTakeEwayBillReq implements IReceiveProtocol {
	Logger logger = Logger.getLogger(DownExgMsgTakeEwayBillReq.class);

	public String handle(JT809Message message) {
		String receBody = message.getMessageBody();
		EWayBill eBill = GlobalConfig.ewayBillMap.get(message.getPlateNo());
		if (eBill != null) {
			T809Manager.UpExgMsgTakeEWayBillAck(message.getPlateNo(),
					eBill.getPlateColor(), eBill.geteContent());
		}
		return "";
	}

	private String queryBillInfo(String plateNo) {
		try {
			String hql = "from EWayBill where plateNo = ? order by createDate desc";

			EWayBill eBill = (EWayBill) ServiceLauncher.geteWayBillService().find(hql,
					plateNo);

			if (eBill != null)
				return eBill.geteContent();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		String ewayBillInfo = "南钢油库，油品：91#，20吨，发车时间12：00"; // queryBillInfo(message.getPlateNo());

		return ewayBillInfo;
	}
}
