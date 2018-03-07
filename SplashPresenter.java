package co.soma.app.view.splash;

import android.content.Context;
import co.soma.app.app.SomaApp;
import co.soma.app.data.RepositoryManager;
import io.reactivex.Observable;
import java.util.concurrent.TimeUnit;
import pl.itcraft.core.executor.Executor;
import pl.itcraft.core.view.activity.CoreActivityPresenter;

public class SplashPresenter extends CoreActivityPresenter<SplashView> {

	//region Constructor

	protected SplashPresenter(Context context, SplashView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onStart() {
		super.onStart();
		Executor.getBuilder(this::prepareDelayedExecution)
			.withSimpleResultListener((newResult, executor) -> {
				if (RepositoryManager.get().getUserManager().isSignedIn()) {
					// TODO: Register notification token.
					getView().navigateToMainView();
				} else {
					getView().navigateToStartView();
				}
			})
			.withErrorResultListener(throwable -> true)
			.bindPresenter(this)
			.execute();

	}

	//endregion

	//region Delay

	private Observable<Long> prepareDelayedExecution() {
		return Observable.timer(SomaApp.getAppConfiguration().splashScreenDelay(), TimeUnit.MILLISECONDS);
	}

	//endregion
}
