package au.com.chris.antdisplay.device.impl;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import android.graphics.drawable.Drawable;
import au.com.chris.antdisplay.AntDisplay;
import au.com.chris.antdisplay.R;
import au.com.chris.antdisplay.device.AntDevice;

public class HeartRateMonitor implements AntDevice {

	public static final DeviceType DEVICE_TYPE = DeviceType.HEARTRATE;
	
	public HeartRateMonitor(){}

	@Override
	public Drawable getDeviceIconDrawable() {
		return AntDisplay.context().getResources().getDrawable(R.drawable.hr_icon);
	}

	@Override
	public String getDeviceName() {
		return "Heart Rate Monitor";
	}

	@Override
	public DeviceType getAntDeviceType() {
		return DEVICE_TYPE;
	}
}
