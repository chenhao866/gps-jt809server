package com.ltmonitor.jt809.protocol.receive;

import org.apache.log4j.Logger;

import com.ltmonitor.entity.MemberInfo;
import com.ltmonitor.entity.VehicleData;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.VehicleModel;
import com.ltmonitor.jt809.protocol.IReceiveProtocol;

/**
 * 上级平台下发的静态车辆信息补报指令
 */
public class DownBaseMsgVehicleAdded implements IReceiveProtocol {
	Logger logger = Logger.getLogger(DownBaseMsgVehicleAdded.class);

	public String handle(JT809Message message) {
		String dataBody = message.getMessageBody();
		MessageParser mp = new MessageParser(dataBody);
		VehicleModel vm = getVehicleModel(message.getPlateNo());
		if (vm != null)
			T809Manager.UpBaseMsgVehicleAddedAck(vm);
		else
			logger.error("找不到该车辆信息" + message.getPlateNo());

		this.logger.info("申请交换指定车辆定位信息应答包入库成功" + dataBody);

		return "";
	}

	private VehicleModel getVehicleModel(String plateNo) {
		try {
			String hql = "from VehicleData where plateNo = ?";
			VehicleData vd = (VehicleData) ServiceLauncher.getVehicleDataService().find(
					hql, plateNo);
			if (vd != null) {
				VehicleModel vm = new VehicleModel();
				vm.setPlateNo(plateNo);
				vm.setPlateColor(vd.getPlateColor());
				vm.setVehicleType(vd.getVehicleType()); 
				vm.setNationallity(vd.getRegion()); 
				vm.setTransType(vd.getIndustry()); 

				try {
					MemberInfo mi = (MemberInfo) ServiceLauncher.getMemberInfoService().load(vd.getMemberId());
					vm.setOwnerName(mi.getName());
					vm.setOwnerTel(mi.getContactPhone());
					vm.setOwnerId("" + mi.getEntityId());
				} catch (Exception ex) {
					vm.setOwnerName("四川宜宾众凯物流有限公司");
					vm.setOwnerTel("15814030918");
					vm.setOwnerId("12345678");
				}

				return vm;
			}
		} catch (Exception ex) {

		}
		return null;
	}

}
