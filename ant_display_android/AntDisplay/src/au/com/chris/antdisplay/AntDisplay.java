package au.com.chris.antdisplay;
import android.app.Application;
import android.content.Context;


public class AntDisplay extends Application {

	private static AntDisplay mApp = null;
	
    @Override
    public void onCreate()
    {
        super.onCreate();
        mApp = this;
    }
    public static Context context()
    {
        return mApp.getApplicationContext();
    }
}
