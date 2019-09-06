package com.ltmonitor.jt809.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.ltmonitor.jt809.app.GlobalConfig;
import com.ltmonitor.jt809.app.ServiceLauncher;
import com.ltmonitor.jt809.app.T809Manager;
import com.ltmonitor.jt809.model.JT809Message;
import com.ltmonitor.jt809.model.ParameterModel;
import com.ltmonitor.jt809.tool.Tools;
import com.ltmonitor.util.T809Constants;

public class MainFrame extends JFrame {
	// public static JTextArea dataArea;
	//public static TextAreaMenu dataArea;
	private JMenuItem databaseMenu;
	private JMenuItem exitSystemMenu;
	private JMenu jMenu1;
	private JMenu jMenu2;
	private JMenu jMenu3;
	private JMenu jMenu4;
	private JMenu jMenu5;
	private JMenuBar jMenuBar1;
	private JMenuItem jMenuItem3;
	private JScrollPane jScrollPane1;
	private JMenuItem parameterMenu;
	private JMenuItem queryAckMenu;
	private JPanel panel;
	private JButton btnConnect;
	private JTextField textField_Port;
	private JLabel lblNewLabel;
	private JCheckBox checkBox;
	private JButton btnStopServer;
	private JLabel lblip;

	/**
	 * 主链路状态显示
	 */
	private JLabel lbMainlinkState;
	/**
	 * 从链路状态显示
	 */
	private JLabel lbSublinkState;

	private JTextField textField_ServerIP;
	private JScrollPane scrollPane;
	private JTable table;
	private JButton btnCloseMainLink;
	private JButton btnConnectMainLink;
	private JButton buttonClearLog;
	/**
	 * 是否显示心跳日志
	 */
	private JCheckBox checkBox_HideHeartLog;

	/**
	 * 断线自动重连
	 */
	private JCheckBox checkBox_autoReconnect;
	/**
	 * 自动重发
	 */
	private JCheckBox checkBox_autoResend;

	private JTextField textFilterByPlateNo;

	private JScrollPane scrollPane_1;

	private static Logger logger = Logger.getLogger(MainFrame.class);

	public MainFrame() {
		setTitle("JT/T 809平台数据交换服务器");
		setBackground(Color.LIGHT_GRAY);
		initComponents();
	}

	private void initComponents() {
		this.jMenu3 = new JMenu();
		this.jScrollPane1 = new JScrollPane();
		// dataArea = new JTextArea();
		//dataArea = new TextAreaMenu();
		this.jMenuBar1 = new JMenuBar();
		this.jMenu1 = new JMenu();
		this.exitSystemMenu = new JMenuItem();
		this.jMenu2 = new JMenu();
		this.jMenu4 = new JMenu();
		this.queryAckMenu = new JMenuItem();
		this.jMenuItem3 = new JMenuItem();
		this.jMenu5 = new JMenu();

		this.jMenu3.setText("jMenu3");

		setDefaultCloseOperation(3);

		//dataArea.setColumns(20);
		//dataArea.setRows(5);
		//this.jScrollPane1.setViewportView(dataArea);

		this.jMenu1.setText("\u8BBE\u7F6E");


		this.exitSystemMenu.setText("退出");
		this.exitSystemMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MainFrame.this.exitSystemMenuActionPerformed(evt);
			}
		});
		this.parameterMenu = new JMenuItem();
		jMenu1.add(parameterMenu);

		this.parameterMenu.setText("系统参数设置");
		this.parameterMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MainFrame.this.parameterMenuActionPerformed(evt);
			}
		});
		
		this.jMenu1.add(this.exitSystemMenu);

		this.jMenuBar1.add(this.jMenu1);


		this.jMenu4.setText("\u547D\u4EE4\u4E0B\u53D1");

		this.jMenuItem3.setText("发送809命令");
		this.jMenuItem3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MainFrame.this.jMenuItem3ActionPerformed(evt);
			}
		});
		this.jMenu4.add(this.jMenuItem3);

		this.jMenuBar1.add(this.jMenu4);

		this.jMenu5.setText("\u5E2E\u52A9");

		this.jMenuBar1.add(this.jMenu5);

		setJMenuBar(this.jMenuBar1);

		panel = new JPanel();
		//jScrollPane1.setColumnHeaderView(panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		panel.setSize(100, 100);

		lblip = new JLabel("\u8FD0\u7BA1\u670D\u52A1\u5668IP\uFF1A");
		panel.add(lblip);

		textField_ServerIP = new JTextField();
		textField_ServerIP.setColumns(10);
		panel.add(textField_ServerIP);

		lblNewLabel = new JLabel("\u7AEF\u53E3\uFF1A");
		panel.add(lblNewLabel);

		textField_Port = new JTextField();
		panel.add(textField_Port);
		textField_Port.setColumns(5);

		btnConnect = new JButton("启动");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StartServer();
			}
		});
		panel.add(btnConnect);

		btnStopServer = new JButton("\u505C\u6B62");
		btnStopServer.setEnabled(false);
		btnStopServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StopServer();
			}
		});
		/**
		getContentPane().setLayout(
				new MigLayout("", "[100px,grow]", "[29px][grow]"));
				*/

		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		panel.add(btnStopServer);

		checkBox = new JCheckBox("加密");
		checkBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				boolean isEncrypt = checkBox.isSelected();
				T809Manager.encrypt = isEncrypt; 
			}
		});
		panel.add(checkBox);

		btnCloseMainLink = new JButton("主链路注销请求");
		btnCloseMainLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeMainLinkButtonActionPerformed(arg0);
			}
		});

		btnConnectMainLink = new JButton("连接主链路");
		btnConnectMainLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connectMainLink();
			}
		});

		buttonClearLog = new JButton("清空日志");
		buttonClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.close();
				//System.setOut(new PrintStream(new GUIPrintStream(System.out,
						//MainFrame.dataArea)));
				//MainFrame.this.dataArea.setText("");
				DefaultTableModel tb = (DefaultTableModel) table.getModel();
				tb.setRowCount(0);
			}
		});

		checkBox_HideHeartLog = new JCheckBox(
				"显示日志");
		checkBox_HideHeartLog.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				boolean hideHeartBeatLog = checkBox_HideHeartLog.isSelected();
				GlobalConfig.displayMsg = hideHeartBeatLog;
			}
		});

		checkBox_autoReconnect = new JCheckBox(
				"重连");
		checkBox_autoReconnect.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				boolean autoReconnect = checkBox_autoReconnect.isSelected();
				GlobalConfig.autoReconnect = autoReconnect;
			}
		});
		checkBox_autoResend = new JCheckBox(
				"失败重发");
		checkBox_autoResend.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				boolean autoResend = checkBox_autoResend.isSelected();
				GlobalConfig.autoResend = autoResend;
			}
		});


		textFilterByPlateNo = new JTextField();
		textFilterByPlateNo.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						warn();
					}

					public void removeUpdate(DocumentEvent e) {
						warn();
					}

					public void insertUpdate(DocumentEvent e) {
						warn();
					}

					public void warn() {
						GlobalConfig.filterPlateNo = textFilterByPlateNo.getText();
					}
				});

		textFilterByPlateNo.setText("");
		textFilterByPlateNo.setColumns(15);
		
		panel.add(checkBox_HideHeartLog);
		panel.add(checkBox_autoReconnect);
		panel.add(checkBox_autoResend);
		panel.add(textFilterByPlateNo);
		panel.add(buttonClearLog);
		panel.add(btnConnectMainLink);
		panel.add(btnCloseMainLink);

		lbMainlinkState = new JLabel("主链路状态");
		panel.add(lbMainlinkState);

		lbSublinkState = new JLabel("从链路状态");
		panel.add(lbSublinkState);


		//getContentPane().add(panel);

		scrollPane = new JScrollPane();
		//scrollPane.add(panel);
		getContentPane().add(scrollPane);
		
		scrollPane.setColumnHeaderView(panel);
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportView(scrollPane_1);
		table = new JTable();
		table.setFont(new Font("宋体", Font.PLAIN, 14));
		table.setModel(new DefaultTableModel(new Object[][] {
				{ null, null, null, null, null, null, null },
				{ null, null, null, null, null, null, null },
				{ null, null, null, null, null, null, null },
				{ null, null, null, null, null, null, null },
				{ null, null, null, null, null, null, null }, }, new String[] {
				"\u6D88\u606F\u7C7B\u578B", "\u5B50\u6807\u8BC6",
				"\u6D88\u606F\u5185\u5BB9", "\u53D1\u9001\u65F6\u95F4",
				"\u8F66\u724C\u53F7", "\u989C\u8272",
				"\u539F\u59CB\u6570\u636E\u62A5\u6587" }));
		table.getColumnModel().getColumn(0).setPreferredWidth(65);
		table.getColumnModel().getColumn(0).setMaxWidth(80);
		table.getColumnModel().getColumn(1).setPreferredWidth(260);
		table.getColumnModel().getColumn(1).setMaxWidth(1000);
		table.getColumnModel().getColumn(2).setPreferredWidth(275);
		table.getColumnModel().getColumn(2).setMaxWidth(1050);
		table.getColumnModel().getColumn(3).setPreferredWidth(155);
		table.getColumnModel().getColumn(3).setMaxWidth(400);
		table.getColumnModel().getColumn(4).setPreferredWidth(95);
		table.getColumnModel().getColumn(4).setMaxWidth(450);
		table.getColumnModel().getColumn(5).setPreferredWidth(63);
		table.getColumnModel().getColumn(5).setMaxWidth(150);
		table.getColumnModel().getColumn(6).setPreferredWidth(187);
		scrollPane_1.setViewportView(table);
		//scrollPane.add(table);

		pack();

		GlobalConfig.initSystem();
		
		this.setTitle(GlobalConfig.parModel.getTitle());

		ParameterModel pm = GlobalConfig.parModel;
		this.textField_ServerIP.setText(pm.getPlatformIP());
		this.textField_Port.setText("" + pm.getPlatformPort());

		final Component c = this;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int option = JOptionPane.showConfirmDialog(c, "确定要退出?", "提示",
						JOptionPane.OK_CANCEL_OPTION);
				if (JOptionPane.OK_OPTION == option) {
					try {
						T809Manager.StopServer();
						ServiceLauncher.getJT809CommandParserService().stop();
						if(timer != null)
							timer.stop();
					} catch (Exception ex) {

						logger.error(ex.getMessage(),ex);
						
					}
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				} else {

					MainFrame.this
							.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				}
			}
		});

	}

	Timer timer = null;
	private void connectMainLink() {
		boolean res = T809Manager.UpConnectReq();
		if (res) {
			this.btnConnect.setEnabled(false);
			;
			this.btnStopServer.setEnabled(true);
			this.btnCloseMainLink.setEnabled(true);

		}
	}

	private void StartServer() {

		ParameterModel pm = GlobalConfig.parModel;
		pm.setPlatformIP(textField_ServerIP.getText());
		pm.setPlatformPort(Integer.parseInt(this.textField_Port.getText()));
		try {
			Boolean res = T809Manager.StartServer();
			if (res == false) {
				JOptionPane.showMessageDialog(null, "无法连接上级平台");
				return;
			}

			this.btnConnect.setEnabled(false);
			;
			this.btnStopServer.setEnabled(true);
			if (timer == null) {
				ActionListener taskPerformer = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {

						JT809Message tm = GlobalConfig.pollMsg();
						while (tm != null)
						{
							MainFrame.this.showMsg(tm);
							tm = GlobalConfig.pollMsg();
						}
						String strState = T809Manager.mainLinkConnected ? "主链路连接成功" : "主链路断开";
						lbMainlinkState.setText(strState);
						strState = T809Manager.subLinkConnected ? "从链路连接成功" : "从链路断开";
						strState += ",发送定位:" + GlobalConfig.totalLocationPacketNum + ",补发:" + GlobalConfig.totalHisLocationPacketNum;
						if(GlobalConfig.waitForSend > 0)
						{
							strState += ",待发:" + GlobalConfig.waitForSend;
						}
						lbSublinkState.setText(strState);
					}
				};

				timer = new Timer(100, taskPerformer);
				timer.setRepeats(true);
				timer.start();
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(),ex);
		}

	}

	private void StopServer() {
		T809Manager.StopServer();

		GlobalConfig.normalDisconnect = true;
		this.btnStopServer.setEnabled(false);
		this.btnCloseMainLink.setEnabled(false);
		this.btnConnect.setEnabled(true);

	}

	/**
	 * 关闭主链路按钮点击事件
	 * @param evt
	 */
	private void closeMainLinkButtonActionPerformed(ActionEvent evt) {
		boolean res = T809Manager.UpDisconnectReq();
		if (res) {
			this.btnStopServer.setEnabled(false);
			this.btnCloseMainLink.setEnabled(false);
			this.btnConnect.setEnabled(true);
		}
	}

	private void exitSystemMenuActionPerformed(ActionEvent evt) {
		System.exit(0);
	}



	private void parameterMenuActionPerformed(ActionEvent evt) {
		ParameterFrame pf = new ParameterFrame(null, true);
		pf.setLocationRelativeTo(null);
		pf.setVisible(true);
	}


	private void jMenuItem3ActionPerformed(ActionEvent evt) {
		ControlFrame cf = new ControlFrame();
		cf.setLocationRelativeTo(null);
		cf.setVisible(true);
	}

	private void showMsg(JT809Message tm) {
		/**
		if (this.hideHeartBeatLog) {
			if (tm.getMsgType() == 0x1005 || tm.getMsgType() == 0x1006
					|| tm.getMsgType() == 0x1200
							|| tm.getMsgType() == 0x1400 
					|| tm.getMsgType() == 0x9005 || tm.getMsgType() == 0x9006)
				return;
		}*/
		Date now = new Date();
		Integer subType = tm.getSubType() == 0 ? tm.getMsgType() : tm
				.getSubType();
		String subDescr = T809Constants.getMsgDescr(subType);

		subDescr = "[" + "0x" + Tools.ToHexString(subType, 2) + "]" + subDescr;

		DefaultTableModel tb = (DefaultTableModel) this.table.getModel();
		tb.insertRow(
				0,
				new Object[] { "0x" + Tools.ToHexString(tm.getMsgType(), 2),
						subDescr, tm.getDescr(), now.toLocaleString(),
						tm.getPlateNo(), tm.getPlateColor(),
						tm.getPacketDescr() });
		

		if(table.getRowCount() > 1000)
			tb.setRowCount(10);
		table.setRowHeight(0, 35);
	}

	public static void main(String[] args) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager
					.getInstalledLookAndFeels())
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}
}
