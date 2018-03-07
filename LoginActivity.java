package co.soma.app.view.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import co.soma.app.view.login.form.LoginFormFragment;
import pl.itcraft.core.view.activity.CoreFragmentActivity;
import pl.itcraft.core.view.fragment.CoreFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends CoreFragmentActivity<LoginPresenter> implements LoginView {

	//region UI

	@Nullable
	@Override
	protected CoreFragment initWithFragment() {
		return createFragment(LoginFormFragment.class, LoginFormFragment.generateBundle());
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Lifecycle

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

	//endregion
}
