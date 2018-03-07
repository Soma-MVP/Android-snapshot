package co.soma.app.view.splash;

import co.soma.app.R;
import co.soma.app.view.main.MainActivity;
import co.soma.app.view.start.StartActivity;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.utils.NavigationUtil;
import pl.itcraft.core.view.activity.CoreActivity;

public class SplashActivity extends CoreActivity<SplashPresenter> implements SplashView {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_splash;
	}

	@Override
	protected boolean shouldShowToolbar() {
		return false;
	}

	//endregion

	//region Navigate

	@Override
	public void navigateToStartView() {
		CoreApp.getNavigation().openActivity(
			StartActivity.class,
			NavigationUtil.OPEN_AS_SINGLE,
			StartActivity.generateBundle()
		);
	}

	@Override
	public void navigateToMainView() {
		CoreApp.getNavigation().openActivity(
			MainActivity.class,
			MainActivity.generateBundle()
		);
	}

	//endregion
}
