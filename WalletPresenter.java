package co.soma.app.view.wallet;

import static org.apache.commons.lang3.StringUtils.isBlank;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.exceptions.wallet.NoWalletFoundException;
import co.soma.app.data.exceptions.wallet.WrongPinException;
import co.soma.app.data.managers.wallet.WalletManger;
import co.soma.app.view.pin.PinActivityType;
import co.soma.app.view.pin.PinPresenter;
import java.io.File;
import pl.itcraft.core.executor.Executor;
import pl.itcraft.core.view.fragment.CoreFragmentPresenter;

class WalletPresenter extends CoreFragmentPresenter<WalletView, Void> {

	//region Constructor

	protected WalletPresenter(Context context, WalletView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onStart() {
		super.onStart();
		loadWalletBalance();
	}

	//endregion

	//region Load Wallet Balance

	private void loadWalletBalance() {
		Executor.getBuilder(
			RepositoryManager.get().getWalletManger().getWalletBalance())
				.withSimpleResultListener((newResult, thisExecutor) -> {
					getView().hidePlaceholder();
					getView().setEthBalance(newResult.getReadableEthBalance());
					getView().setSctBalance(newResult.getReadableSctBalance());
					getView().setWalletAddress(newResult.getWalletAddress());
				})
				.withErrorResultListener(this::handleWalletBalanceError)
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.execute();
	}

	private boolean handleWalletBalanceError(@NonNull Throwable throwable) {
		if (throwable instanceof NoWalletFoundException) {
			if (RepositoryManager.get().getWalletManger().checkWalletFileExternalExist() ||
				RepositoryManager.get().getWalletManger().checkDefaultWalletFileExist()) {
				getView().showRestorePlaceholder();
			} else {
				final String username = RepositoryManager.get().getUserManager().getUsername();
				final File file = WalletManger.prepareFileForUserWallet(username, true, null);
				final String filename = (file == null) ? "-" : file.getName();
				getView().showNoWalletPlaceholder(
					WalletManger.getWalletDir(true, null).toString(), filename);
			}
			return true;
		}
		return false;
	}

	//endregion

	//region Create Wallet

	void onCreateWalletButtonClicked() {
		getView().showEnterPinDialog(
			PinPresenter.prepareArguments(PinActivityType.CREATE_PIN),
			pin ->
				Executor.getBuilder(
					RepositoryManager.get().getWalletManger().createWallet(
						RepositoryManager.get().getUserManager().getUsername(), pin))
						.withSimpleResultListener((newResult, thisExecutor) -> {
							if (newResult != null) {
								loadWalletBalance();
							}
						})
						.bindProgressIndicator(() -> getView().getEnterPinDialogProgressIndicator())
						.bindPresenter(this)
						.execute());
	}

	//endregion

	//region Restore Wallet

	void onRestoreWalletButtonClicked() {
		restoreWallet(null);
	}

	private void restoreWallet(@Nullable String pin) {

		// No PIN. Need to ask for.
		if (isBlank(pin)) {
			getView().showEnterPinDialog(
				PinPresenter.prepareArguments(PinActivityType.CHECK_PIN),
				this::restoreWallet
			);
		}

		// Already got PIN. Recover wallet.
		else {
			Executor.getBuilder(
				RepositoryManager.get().getWalletManger().tryRecoverWallet(
					RepositoryManager.get().getUserManager().getUsername(), pin))
					.withErrorResultListener(this::handleRestoreWalletError)
					.withOnCompleteAction(this::loadWalletBalance)
					.bindProgressIndicator(() -> getView().getRestoreWalletDialogProgressIndicator())
					.bindPresenter(this)
					.execute();
		}
	}

	private boolean handleRestoreWalletError(@NonNull Throwable throwable) {
		if (throwable instanceof WrongPinException) {
			getView().showIncorrectPinError();
			return true;
		}
		return false;
	}

	//endregion
}
