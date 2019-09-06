package com.ltmonitor.jt809.service;

import com.ltmonitor.entity.JT809Command;


public interface IJT809CommandParseService {


	public abstract ICommandHandler getOnRecvCommand();

	public abstract void setOnRecvCommand(ICommandHandler value);

	public abstract int getInterval();

	public abstract void setInterval(int value);

	public abstract void start();

	public abstract void stop();

	public abstract void ParseCommand();


	public abstract void UpdateCommand(JT809Command tc);

	//public abstract JT809Command getCommandBySn(int sn);

	void save(JT809Command jc);


}