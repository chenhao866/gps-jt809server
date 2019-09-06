package com.ltmonitor.jt809.app;

public enum VehiclePlateColor {

	Blue(1),

	Yellow(2),

	Black(3),

	White(4),

	Other(5);

	private int nCode;

	private VehiclePlateColor(int _nCode) {
		this.nCode = _nCode;
	}

	@Override
	public String toString() {
		return String.valueOf(this.nCode);
	}
}
