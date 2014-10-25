package au.com.chris.antdisplay.fragments;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EnumSet;

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

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc.ICalculatedCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.DataState;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc.IHeartRateDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

public class CadenceDisplayFragment extends BasicMetricDisplayFragment {

	AntPlusBikeCadencePcc bcPcc = null;
	PccReleaseHandle<AntPlusBikeCadencePcc> bcReleaseHandle = null;
    
    TextView cadenceDisplay = null;
    TextView infoDisplay = null;
    private static final int HISTORY_SIZE = 100;
    private XYPlot cadenceChart;
   
    private SimpleXYSeries cadenceHistorySeries;
    
	public CadenceDisplayFragment() {
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);

		ImageView icon = (ImageView)rootView.findViewById(R.id.metric_icon);
		icon.setImageResource(R.drawable.hr_icon);
		
		cadenceDisplay = (TextView)rootView.findViewById(R.id.metric_display);
		infoDisplay = (TextView)rootView.findViewById(R.id.metric_info);
	
		cadenceChart = (XYPlot)rootView.findViewById(R.id.metric_chart);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		/*
		 * Try to connect to a heart rate monitor
		 */
        handleReset();
        
        cadenceChart.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        
        cadenceHistorySeries = new SimpleXYSeries("HR");
        cadenceHistorySeries.useImplicitXVals();
        final PlotStatistics hrHistory = new PlotStatistics(1000, false);
        
        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 0, 0), null, null, null);
		formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
		formatter1.getLinePaint().setStrokeWidth(10);
		
		cadenceChart.addSeries(cadenceHistorySeries, formatter1);
		
		// thin out domain tick labels so they dont overlap each other:
		cadenceChart.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		cadenceChart.setDomainStepValue(5);
 
		cadenceChart.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
		cadenceChart.setRangeStepValue(10);
 
		cadenceChart.setRangeValueFormat(new DecimalFormat("###.#"));
 
		cadenceChart.setRangeBoundaries(40, 200, BoundaryMode.FIXED);
		cadenceChart.addListener(hrHistory);
	}
	
	private void handleReset() {
        //Release the old access if it exists
        if(bcReleaseHandle != null) {
            bcReleaseHandle.close();
        }

        requestAccessToPcc();
    }
    private void requestAccessToPcc() {
    	// starts the plugins UI search
        bcReleaseHandle = AntPlusBikeCadencePcc.requestAccess(getActivity(), getActivity().getApplicationContext(),
        		base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
    }
    
    protected IPluginAccessResultReceiver<AntPlusBikeCadencePcc> base_IPluginAccessResultReceiver =
        new IPluginAccessResultReceiver<AntPlusBikeCadencePcc>() {

    	//Handle the result, connecting to events on success or reporting failure to user.
        @Override
        public void onResultReceived(AntPlusBikeCadencePcc result, RequestAccessResult resultCode,
            DeviceState initialDeviceState) {
            //showDataDisplay("Connecting...");
            switch(resultCode)
            {
                case SUCCESS:
                	bcPcc = result;
                 //   tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                    infoDisplay.setVisibility(View.GONE);
                    cadenceDisplay.setVisibility(View.VISIBLE);
                    subscribeToEvents();
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
//                        tv_status.setText(bcPss.getDeviceName() + ": " + newDeviceState);
                }
            });


        }
    };
        
    /**
     * Switches the active view to the data display and subscribes to all the data events
     */
    public void subscribeToEvents() {
    		bcPcc.subscribeCalculatedCadenceEvent(new ICalculatedCadenceReceiver()
            {
                @Override
                public void onNewCalculatedCadence(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags, final BigDecimal calculatedCadence)
                {
                	getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                        	cadenceDisplay.setText(String.valueOf(calculatedCadence));
                        }
                    });

                }
            });
    }
}
