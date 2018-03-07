package co.soma.app.app;

import android.content.Context;
import co.soma.app.R;
import pl.itcraft.core.app.DefaultAppConfiguration;

class SomaConfiguration extends DefaultAppConfiguration {

	//region Constructor

	public SomaConfiguration(Context context) {
		super(context);
	}

	//endregion

	//region Animations

	@Override
	public int getShowNewActivityCurrentExitAnimation() {
		return R.anim.exit_ios_alike_to_left;
	}

	@Override
	public int getShowNewActivityNewEnterAnimation() {
		return R.anim.enter_ios_alike_from_right;
	}

	@Override
	public int getFinishCurrentActivityCurrentExitAnimation() {
		return R.anim.exit_ios_alike_to_right;
	}

	@Override
	public int getFinishCurrentActivityPreviousEnterAnimation() {
		return R.anim.enter_ios_alike_from_left;
	}

	@Override
	public long splashScreenDelay() {
		return 3000;
	}

	//endregion

	//region Fonts

	static final String DEFAULT_FONT_PATH = "fonts/HelveticaNeue-Regular.ttf";

	//endregion
}