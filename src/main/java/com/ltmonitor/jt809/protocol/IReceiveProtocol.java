package com.ltmonitor.jt809.protocol;

import com.ltmonitor.jt809.model.JT809Message;

public abstract interface IReceiveProtocol
{
  public abstract String handle(JT809Message paramMessageModel);
}

