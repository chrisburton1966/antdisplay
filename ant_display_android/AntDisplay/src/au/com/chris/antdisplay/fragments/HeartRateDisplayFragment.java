package au.com.chris.antdisplay.fragments;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.Observable;
import java.util.Observer;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import au.com.chris.antdisplay.R;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class HeartRateDisplayFragment extends BasicMetricDisplayFragment {

	private AntPlusHeartRatePcc hrPcc = null;
    private PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;
    
    TextView hrDisplay = null;
    TextView infoDisplay = null;
    private static final int HISTORY_SIZE = 100;
    private XYPlot hrChart;
   
    private SimpleXYSeries hrHistorySeries;
    
	public HeartRateDisplayFragment() {
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		ImageView icon = (ImageView)rootView.findViewById(R.id.metric_icon);
		icon.setImageResource(R.drawable.hr_icon);
		
		hrDisplay = (TextView)rootView.findViewById(R.id.metric_display);
		infoDisplay = (TextView)rootView.findViewById(R.id.metric_info);
	
		hrChart = (XYPlot)rootView.findViewById(R.id.metric_chart);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		/*
		 * Try to connect to a heart rate monitor
		 */
        handleReset();
        
        hrChart.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        
        hrHistorySeries = new SimpleXYSeries("HR");
        hrHistorySeries.useImplicitXVals();
        final PlotStatistics hrHistory = new PlotStatistics(1000, false);
        
        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 0, 0), null, null, null);
		formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
		formatter1.getLinePaint().setStrokeWidth(10);
		
		hrChart.addSeries(hrHistorySeries, formatter1);
		
		// thin out domain tick labels so they dont overlap each other:
		hrChart.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		hrChart.setDomainStepValue(5);
 
		hrChart.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
		hrChart.setRangeStepValue(10);
 
		hrChart.setRangeValueFormat(new DecimalFormat("###.#"));
 
		hrChart.setRangeBoundaries(40, 200, BoundaryMode.FIXED);
		hrChart.addListener(hrHistory);
	}
	
	private void handleReset() {
        //Release the old access if it exists
        if(releaseHandle != null) {
            releaseHandle.close();
        }

        requestAccessToPcc();
    }
    private void requestAccessToPcc() {
    	// starts the plugins UI search
        releaseHandle = AntPlusHeartRatePcc.requestAccess(getActivity(), getActivity().getApplicationContext(),
        		base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
    }
    
    protected IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver =
        new IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {

    	//Handle the result, connecting to events on success or reporting failure to user.
        @Override
        public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
            DeviceState initialDeviceState) {
            //showDataDisplay("Connecting...");
            switch(resultCode)
            {
                case SUCCESS:
                    hrPcc = result;
                 //   tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                    infoDisplay.setVisibility(View.GONE);
                    hrDisplay.setVisibility(View.VISIBLE);
                    subscribeToHrEvents();
                    break;
                case CHANNEL_NOT_AVAILABLE:
//                    Toast.makeText(Activity_HeartRateDisplayBase.this, "Channel Not Available", Toast.LENGTH_SHORT).show();
//                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case ADAPTER_NOT_DETECTED:
//                    Toast.makeText(Activity_HeartRateDisplayBase.this, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
//                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case BAD_PARAMS:
                    //Note: Since we compose all the params ourself, we should never see this result
//                    Toast.makeText(Activity_HeartRateDisplayBase.this, "Bad request parameters.", Toast.LENGTH_SHORT).show();
//                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case OTHER_FAILURE:
                	Log.i("HeartRateDispaly", "something here");
//                    Toast.makeText(Activity_HeartRateDisplayBase.this, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
//                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                case DEPENDENCY_NOT_INSTALLED:
//                    tv_status.setText("Error. Do Menu->Reset.");
//                    AlertDialog.Builder adlgBldr = new AlertDialog.Builder(Activity_HeartRateDisplayBase.this);
//                    adlgBldr.setTitle("Missing Dependency");
//                    adlgBldr.setMessage("The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
//                    adlgBldr.setCancelable(true);
//                    adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            Intent startStore = null;
//                            startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
//                            startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                            Activity_HeartRateDisplayBase.this.startActivity(startStore);
//                        }
//                    });
//                    adlgBldr.setNegativeButton("Cancel", new OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    final AlertDialog waitDialog = adlgBldr.create();
//                    waitDialog.show();
                    break;
                case USER_CANCELLED:
                	Log.i("HeartRateDisplay", "User Cancelled?");
//                    tv_status.setText("Cancelled. Do Menu->Reset.");
                    break;
                case UNRECOGNIZED:
//                    Toast.makeText(Activity_HeartRateDisplayBase.this,
//                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
//                        Toast.LENGTH_SHORT).show();
//                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
                default:
//                    Toast.makeText(Activity_HeartRateDisplayBase.this, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
//                    tv_status.setText("Error. Do Menu->Reset.");
                    break;
            }
        }
    };

    //Receives state changes and shows it on the status display line
    protected  IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver =
        new IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                        tv_status.setText(hrPcc.getDeviceName() + ": " + newDeviceState);
                }
            });


        }
    };
        
    /**
     * Switches the active view to the data display and subscribes to all the data events
     */
    public void subscribeToHrEvents() {
    	 hrPcc.subscribeHeartRateDataEvent(new IHeartRateDataReceiver() {

			@Override
			public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
	                final int computedHeartRate, final long heartBeatCount,
	                final BigDecimal heartBeatEventTime, final DataState dataState) {
				
				//currentHeartRate = computedHeartRate;
				// Mark heart rate with asterisk if zero detected
				final String textHeartRate = String.valueOf(computedHeartRate)
                    + ((DataState.ZERO_DETECTED.equals(dataState)) ? "*" : "");

                // Mark heart beat count and heart beat event time with asterisk if initial value
                final String textHeartBeatCount = String.valueOf(heartBeatCount)
                    + ((DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");
                final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                    + ((DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	hrDisplay.setText(textHeartRate);
                    }
                });
                
                // get rid the oldest sample in history:
                if (hrHistorySeries.size() > HISTORY_SIZE) {
                	hrHistorySeries.removeFirst();
                }
         
                // add the latest history sample:
                hrHistorySeries.addLast(null, computedHeartRate);
         
                // redraw the Plots:
                hrChart.redraw();
			}
    	 });
    }
}
