package au.com.chris.antdisplay.device;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import android.graphics.drawable.Drawable;

public interface AntDevice {

	public Drawable getDeviceIconDrawable();
	public String getDeviceName();
	public DeviceType getAntDeviceType();
}
