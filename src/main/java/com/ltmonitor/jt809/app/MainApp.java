package com.ltmonitor.jt809.app;

import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.protocol.send.UpConnectReq;
import com.ltmonitor.jt809.tool.Tools;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * 809服务程序的入口类
 * @author admin
 *
 */
public class MainApp {

	private static Logger logger = Logger.getLogger(MainApp.class);
	public static void main(String[] args) {
		UpConnectReq upConnectReq = new UpConnectReq();
		JT809Message msg = upConnectReq.wrapper();
		//对登录数据进行组包
		String strMsg = Tools.getHeaderAndFlag(GlobalConfig.getSN(),
				msg.getMessageBody(), msg.getMsgType(),
				msg.getMsgGNSSCenterID(), T809Manager.encrypt);
		//默认strMsg的值
		String system ="5B0000004A00000000100100000000312E300000000000333435367A7A6173313233003132372E302E302E31000000000000000000000000000000000000000000000038383939FEA35D";
		String b = Tools.getStringFromHex(system);
//		5B //头标识
//		00 00 00 4A //数据长度 十六进制
//		00 00 00 00 //报文序列号
//		10 01 //标识 十六进制
//		00 00 00 00 //下级平台接入码
//		31 2E 30 //协议版本号
//		00  //报文加密标识位：b:0 表示报文不加密  1：表示报文加密
//		00 00 00 00 数据加密密钥
//数据体	33 34 35 36 //用户名7A 7A 61 73 31 32 33 00 //密码31 32 37 2E 30 2E 30 2E 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00//ip地址38 38 3939 //端口号
//		90 65 数据校验码
//		5D 尾标识

		//字符串转十六进制，长度不够补0;如果是十进制将转换为十六进制，如果是十六进制将转换为十进制；如果是字符串会另行转换且可解析还原
		//转换
		String c = Tools.ToHexString(65187,2);//CRC
		//解析数据体部分；其他进制转换可直接使用上述进制转换方法进行转换；
		String d  = Tools.getStringFromHex("333435367A7A6173313233003132372E302E302E31000000000000000000000000000000000000000000000038383939");

		//将数据转换成字节数组
		byte[] aa = system.getBytes();
		String str= Arrays.toString(aa);
		System.out.println("原字节数组"+str);
		//对字节数组进行加密
		byte[] bb = encrypt(12451,54784,45794,456,aa);
		String str1= Arrays.toString(bb);
		System.out.println("加密后字节数组"+str1);
		//对字节数组进行解密
		byte[] cc = encrypt(12451,54784,45794,456,bb);
		String str2= Arrays.toString(cc);;
		System.out.println("解密后字节数组"+str2);
		//将解密的字符数组转换为字符串
		String str3=new String(cc);;
		System.out.println("解密后字节数组转字符串"+str3);
		String dd  = Tools.getStringFromHex(str3);
		System.out.println("解析后的字符串"+dd);
		//CRC效验
		String vv ="AA0C01000100000405170501A0860100";
		String zz = Tools.getCRCString(vv);
		System.out.println("解析后的字符串"+zz);




//		try {
//			UIManager.LookAndFeelInfo[] arrayOfLookAndFeelInfo;
//			int j = (arrayOfLookAndFeelInfo = UIManager
//					.getInstalledLookAndFeels()).length;
//			for (int i = 0; i < j; i++) {
//				UIManager.LookAndFeelInfo info = arrayOfLookAndFeelInfo[i];
//				if ("Nimbus".equals(info.getName())) {
//					UIManager.setLookAndFeel(info.getClassName());
//					break;
//				}
//			}
//
//			ServiceLauncher.launch();
//			GlobalConfig.initSystem();
//
//		} catch(Exception ex) {
//			//Logger.getLogger(ParameterFrame.class.getName()).log(Level.SEVERE,
//					//null, ex);
//
//			logger.error(ex.getMessage(), ex);
//		}
//
//
//		MainFrame mainFrame = new MainFrame();
//		mainFrame.setLocationRelativeTo(null);
//
//		mainFrame.setVisible(true);
//		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

	}

	//加密解密都为此方法；且M1，IA1，IC1必须有一个大于五位数，不然结果会与加密数据一致
	public static byte[] encrypt(int M1,int IA1,int IC1,int key,byte[] data) {
		if(data == null) return null;
		byte[] array = data;//使用原对象，返回原对象
		int idx=0;
		if(key==0){
			key=1;
		}
		int mkey = M1;
		if (0 == mkey ) {
			mkey = 1;
		}
		while(idx<array.length){
			key = IA1 * ( key % mkey ) + IC1;
			array[idx]^=((key>>20)&0xFF);
			idx++;
		}
		return array;
	}

	}
