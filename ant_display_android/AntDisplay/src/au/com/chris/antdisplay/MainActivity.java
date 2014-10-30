package au.com.chris.antdisplay;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import au.com.chris.antdisplay.adapters.MultiItemListRowAdapter;
import au.com.chris.antdisplay.device.AntDevice;
import au.com.chris.antdisplay.device.impl.CadenceMonitor;
import au.com.chris.antdisplay.device.impl.HeartRateMonitor;

public class MainActivity extends Activity implements OnItemClickListener {

	private ArrayList<AntDevice> devicesList = new ArrayList<AntDevice>();
	private AlertDialog selectDeviceAlertDialog = null;
	private SelectDevicesAdapter mSelectDevicesAdapter;
	MultiDeviceSearch mSearch;
	
	GradientDrawable gd = new GradientDrawable();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		devicesList.add(new HeartRateMonitor());
		devicesList.add(new CadenceMonitor());
		
		gd.setColor(0xFF00FF00); // Changes this drawbale to use a single color instead of a gradient
	    gd.setCornerRadius(5);
	    gd.setStroke(1, 0xFF000000);
	    
		// if (savedInstanceState == null) {
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new HeartRateDisplayFragment()).commit();
		//
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new CadenceDisplayFragment()).commit();
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add_device) {
			return showSelectDevicesDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	private void startMultiDeviceSearch() {
		EnumSet<DeviceType> searchables = mSelectDevicesAdapter.getCheckedItems();
		
		Log.i("MainActivity", "All good at this point");
		mSearch = new MultiDeviceSearch(this, searchables, mCallback, null);
	}
	
	private boolean showSelectDevicesDialog() {
		AlertDialog.Builder selectDevicesDialog = new AlertDialog.Builder(
				MainActivity.this);

		final ListView selectDevicesListView = new ListView(MainActivity.this);
		LinearLayout layout = new LinearLayout(MainActivity.this);

		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(selectDevicesListView);
		selectDevicesDialog.setTitle(R.string.select_devices_title);
		selectDevicesDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Find devices that have been selected
				Log.i("OK", "CLICKED");
				startMultiDeviceSearch();
			}
		});
		
		selectDevicesDialog.setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.i("CANCEL", "CLICKED");
			}
		});
		
		selectDevicesDialog.setView(layout);

		mSelectDevicesAdapter = new SelectDevicesAdapter(MainActivity.this,
				devicesList);

		MultiItemListRowAdapter wrapperAdapter = new MultiItemListRowAdapter(
				this, mSelectDevicesAdapter, 2, 10);

		selectDevicesListView.setAdapter(wrapperAdapter);
		mSelectDevicesAdapter.setOnItemClickListener(this);

		
		selectDeviceAlertDialog = selectDevicesDialog.create();
		selectDeviceAlertDialog.show();
		return true;
	}

	public class SelectDevicesAdapter extends BaseAdapter {

		private OnItemClickListener mOnItemClickListener;
		private HashMap<Integer, Boolean> checkedItems = 
				new HashMap<Integer, Boolean>();
		
		Context ctx = null;
		ArrayList<AntDevice> deviceList = null;
		
		private LayoutInflater mInflater = null;

		public SelectDevicesAdapter(Activity activity, ArrayList<AntDevice> list) {
			this.ctx = activity;
			mInflater = activity.getLayoutInflater();
			this.deviceList = list;
			
			for(int i = 0; i < list.size(); i++){
				checkedItems.put(i, false);
		    }
		}

		public void setOnItemClickListener(OnItemClickListener listener) {
			mOnItemClickListener = listener;
		}

		@Override
		public int getCount() {
			if (deviceList != null && deviceList.size() > 0) {
				return deviceList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			if (deviceList != null) {
				return deviceList.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			final ViewHolder holder;
			
			AntDevice device = (AntDevice)getItem(position);
			
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.select_device_row,
						null);

				holder.deviceName = (TextView) convertView
						.findViewById(R.id.select_device_name);
				holder.deviceIcon = (ImageView) convertView
						.findViewById(R.id.select_device_icon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if (device == null) {
				return convertView;
			}

			holder.deviceName.setText(device.getDeviceName());
			holder.deviceIcon.setImageDrawable(device.getDeviceIconDrawable());
			
			final View clickedView = convertView;
			// set the on click listener for each of the items
			
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnItemClickListener != null) {
						mOnItemClickListener.onItemClick(null, clickedView,
								position, position);
					}
				}
			});
			
			if(checkedItems.get(position)) {
				convertView.setBackground(getResources().getDrawable(R.drawable.item_selected_background));
			} else {
				convertView.setBackground(null);
			}
			return convertView;
		}
		
		public void toggle(int position) {
			if(checkedItems.get(position)){
				checkedItems.put(position, false);
		    } else{
		    	checkedItems.put(position, true);
		    }
			   
		    notifyDataSetChanged();
		}
		
		public EnumSet<DeviceType> getCheckedItems() {
			EnumSet<DeviceType> selectedDevices = EnumSet.noneOf(DeviceType.class);
			
			for(Integer idx : checkedItems.keySet()) {
				if(checkedItems.get(idx).booleanValue()) {
					selectedDevices.add(deviceList.get(idx).getAntDeviceType());
				}
			}
			return selectedDevices;
		}
	}

	private static class ViewHolder {
		int id;
		TextView deviceName;
		ImageView deviceIcon;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (mSelectDevicesAdapter != null) {
			mSelectDevicesAdapter.toggle(position);
		}
	}
	
	private MultiDeviceSearch.SearchCallbacks mCallback = new MultiDeviceSearch.SearchCallbacks()
    {
		/**
         * Called when a device is found. Display found devices in connected and
         * found lists
         */
        public void onDeviceFound(final MultiDeviceSearchResult deviceFound)
        {
        	Log.i("MULTIDEVICESEARCH CALLBACK", "Device Found");
        }
        
        /**
         * The search has been stopped unexpectedly
         */
        public void onSearchStopped(RequestAccessResult reason)
        {
            finish();
        }
    };
}
