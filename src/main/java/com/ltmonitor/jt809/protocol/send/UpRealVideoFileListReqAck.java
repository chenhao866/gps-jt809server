package com.ltmonitor.jt809.protocol.send;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.ISendProtocol;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.video.entity.VideoFileItem;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 上传音视频资源目录请求应答消息　0x1902
 */
public class UpRealVideoFileListReqAck implements ISendProtocol {
	private static Logger logger = Logger
			.getLogger(UpRealVideoFileListReqAck.class);

	private String plateNo;

	private int plateColor;

	private List<VideoFileItem> videoFileList;

	private int result;

	private int msgType = 0x1900;

	private int subType = 0x1902;


	public UpRealVideoFileListReqAck(String plateNo, int plateColor, int _result, List<VideoFileItem> videoFileItems) {
		this.plateNo = plateNo;
		this.plateColor = plateColor;
		videoFileList = videoFileItems;
		result = _result;
	}

	public JT809Message wrapper() {
		int videoFileItemNum = videoFileList.size();
		int dataLength = 1+ 4 + (1+8+8+8+1+1+1+4)* videoFileItemNum;
		StringBuilder sb = new StringBuilder();
		sb.append(Tools.ToHexString(plateNo, 21))
				.append(Tools.ToHexString(plateColor, 1))
				.append(Tools.ToHexString(subType, 2))
				.append(Tools.ToHexString(dataLength, 4))
				.append(Tools.ToHexString(result, 1))
				.append(Tools.ToHexString(videoFileItemNum, 4));
		for (int m = 0; m < videoFileItemNum; m++) {
			VideoFileItem gd = videoFileList.get(m);
			sb.append(videoFileItemDataToString(gd));
		}
		String body = sb.toString();
		JT809Message mm = new JT809Message(msgType,  subType,body);
		mm.setPlateColor(plateColor);
		mm.setPlateNo(plateNo);
		mm.setMsgDescr("资源条数:"+videoFileItemNum);
		return mm;
	}

	private String videoFileItemDataToString(VideoFileItem gnssData) {

		StringBuilder sb = new StringBuilder();
		sb.append(Tools.ToHexString(gnssData.getChannelId(), 1))
				.append(Tools.getUTC(gnssData.getStartDate())).append(Tools.getUTC(gnssData.getEndDate()))
				.append(Tools.ToHexString(gnssData.getAlarmStatus(), 8))
					.append(Tools.ToHexString(gnssData.getDataType(), 1))
				.append(Tools.ToHexString(gnssData.getStreamType(), 1))
				.append(Tools.ToHexString(gnssData.getStoreType(), 1))
				.append(Tools.ToHexString(gnssData.getFileLength(), 4));

		return sb.toString();
	}

	public String getPlateNo() {
		return plateNo;
	}

	public void setPlateNo(String plateNo) {
		this.plateNo = plateNo;
	}

	public int getPlateColor() {
		return plateColor;
	}

	public void setPlateColor(int plateColor) {
		this.plateColor = plateColor;
	}

}
