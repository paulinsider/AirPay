package way.pattern;


import way.view.LockPatternUtils;
import android.app.Application;

import com.air.network.ExitAppliation;

public class App extends Application {
	private static App mInstance;
	private LockPatternUtils mLockPatternUtils;

	public static App getInstance() {
		return mInstance;
	}

	@Override
	public void onCreate() {
        //ExitAppliation.getInstance().addActivity(this);
		super.onCreate();
		mInstance = this;
		mLockPatternUtils = new LockPatternUtils(this);
	}

	public LockPatternUtils getLockPatternUtils() {
		return mLockPatternUtils;
	}
}
