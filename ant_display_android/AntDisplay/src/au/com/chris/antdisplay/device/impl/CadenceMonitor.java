package au.com.chris.antdisplay.device.impl;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import android.graphics.drawable.Drawable;
import au.com.chris.antdisplay.AntDisplay;
import au.com.chris.antdisplay.R;
import au.com.chris.antdisplay.device.AntDevice;

public class CadenceMonitor implements AntDevice {

	public static final DeviceType DEVICE_TYPE = DeviceType.BIKE_CADENCE;
	
	public CadenceMonitor(){}

	@Override
	public Drawable getDeviceIconDrawable() {
		return AntDisplay.context().getResources().getDrawable(R.drawable.hr_icon);
	}

	@Override
	public String getDeviceName() {
		return "Cadence Monitor";
	}	
}
