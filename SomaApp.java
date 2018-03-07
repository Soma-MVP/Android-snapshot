package co.soma.app.app;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import co.soma.app.BuildConfig;
import co.soma.app.R;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.configuration.RepositoryConfiguration;
import co.soma.app.util.debug.Bugtracker;
import co.soma.app.util.debug.DebugBridge;
import co.soma.app.util.log.SomaLogger;
import co.soma.app.view.login.LoginActivity;
import co.soma.app.view.start.StartActivity;
import co.soma.app.view.test.TestActivity;
import com.facebook.login.LoginManager;
import pl.itcraft.core.app.AppConfiguration;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.utils.NavigationUtil;
import pl.itcraft.core.view.activity.CoreActivity;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SomaApp extends CoreApp implements RepositoryConfiguration {

	//region Lifecycle


	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initializeFonts();
		initializeLogger();
		initializeBugTracker();
		initializeDebugBridge();
		initializeRepositoryManager();
	}

	//endregion

	//region Configuration

	@Override
	protected AppConfiguration prepareConfiguration() {
		return new SomaConfiguration(this);
	}

	//endregion

	//region Versioning

	@Override
	protected int provideVersionCode() {
		return BuildConfig.VERSION_CODE;
	}

	@Override
	protected String provideVersionName() {
		return BuildConfig.VERSION_NAME;
	}

	@Override
	protected boolean provideDebugMode() {
		return BuildConfig.DEBUG;
	}

	//endregion

	//region Fonts

	private void initializeFonts() {
		CalligraphyConfig.initDefault(
			new CalligraphyConfig.Builder()
				.setDefaultFontPath(SomaConfiguration.DEFAULT_FONT_PATH)
				.setFontAttrId(R.attr.fontPath)
				.build());
	}

	//endregion

	//region Logger

	private void initializeLogger() {
		SomaLogger.init(BuildConfig.DEBUG);
	}

	//endregion

	//region Bug tracker

	private void initializeBugTracker() {
		Bugtracker.init(BuildConfig.ENABLE_BUG_TRACKER, this);
	}

	//endregion

	//region Debug bridge

	private void initializeDebugBridge() {
		DebugBridge.init(BuildConfig.ENABLE_DEBUG_BRIDGE, this);
	}

	//endregion

	//region Repository

	private void initializeRepositoryManager() {
		RepositoryManager.getBuilder(this).build();
	}

	//endregion

	//region Session

	@Override
	public void onUserSessionExpired() {
		LoginManager.getInstance().logOut();
		if (getCurrentVisibleActivity() != null && !(getCurrentVisibleActivity() instanceof LoginActivity)) {
			getNavigation().openActivity(StartActivity.class, NavigationUtil.OPEN_AS_SINGLE);
		}
	}

	//endregion

	//region Test activity

	@Nullable
	@Override
	public Class<? extends CoreActivity> provideTestActivity() {
		return TestActivity.class;
	}

	//endregion
}
