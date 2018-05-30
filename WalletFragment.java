package co.soma.app.view.wallet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import co.soma.app.R;
import co.soma.app.defs.Navigation;
import co.soma.app.view.main.MainUtils;
import co.soma.app.view.pin.PinDialog;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.executor.Actions.ParameterAction;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.dialogfragment.CoreDialogFragmentPresenter.SimpleDialogCallback;
import pl.itcraft.core.view.fragment.CoreFragment;
import pl.itcraft.core.view.widget.placeholder.PlaceholderView;

public class WalletFragment extends CoreFragment<WalletPresenter> implements WalletView {

	//region Fields [binding]

	@BindView(R.id.progress_indicator) View            progressIndicator;
	@BindView(R.id.walletAddress)      TextView        walletAddress;
	@BindView(R.id.ethBalance)         TextView        ethBalance;
	@BindView(R.id.sctBalance)         TextView        sctBalance;
	@BindView(R.id.placeholder)        PlaceholderView placeholder;

	//endregion

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_wallet;
	}

	@Override
	public CharSequence getTitle() {
		return getText(R.string.wallet_title);
	}

	@Override
	public void showProgressIndicator() {
		if (progressIndicator != null) {
			progressIndicator.setVisibility(View.VISIBLE);
			this.sctBalance.setVisibility(View.GONE);
			this.ethBalance.setVisibility(View.GONE);
			this.walletAddress.setVisibility(View.GONE);
		}
	}

	@Override
	public void hideProgressIndicator() {
		if (progressIndicator != null) {
			progressIndicator.setVisibility(View.GONE);
			this.sctBalance.setVisibility(View.VISIBLE);
			this.ethBalance.setVisibility(View.VISIBLE);
			this.walletAddress.setVisibility(View.VISIBLE);
		}
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Lifecycle

	@Override
	public void onStart() {
		super.onStart();
		MainUtils.changeNavigationSelection(Navigation.WALLET);
	}

	//endregion

	//region Wallet

	@Override
	public void setWalletAddress(String walletAddress) {
		this.walletAddress.setText(walletAddress);
	}

	@Override
	public void setEthBalance(@Nullable String balance) {
		this.ethBalance.setText(balance);
	}

	@Override
	public void setSctBalance(@Nullable String balance) {
		this.sctBalance.setText(balance);
	}

	//endregion

	//region Dialogs

	private final ProgressIndicator enterPinDialogProgressIndicator = CoreApp
		.getNavigation()
		.prepareProgressDialog(R.string.wallet_creating_wallet, false, null);

	@Override
	public ProgressIndicator getEnterPinDialogProgressIndicator() {
		return enterPinDialogProgressIndicator;
	}

	private final ProgressIndicator restoreWalletProgressIndicator = CoreApp
		.getNavigation()
		.prepareProgressDialog(
			R.string.wallet_restoring_wallet, false, null);

	@Override
	public ProgressIndicator getRestoreWalletDialogProgressIndicator() {
		return restoreWalletProgressIndicator;
	}

	@Override
	public void showEnterPinDialog(@NonNull Bundle bundle, @NonNull ParameterAction<String> action) {
		CoreApp.getNavigation().prepareAlertDialog(PinDialog.class)
			   .withArguments(bundle)
			   .withResultCalback(
				   new SimpleDialogCallback<String>() {
					   @Override
					   public void onResult(@Nullable String pin) {
						   action.doIt(pin);
					   }
				   })
			   .showAllowingStateLoss();
	}

	//endregion

	//region Placeholders

	@Override
	public void showRestorePlaceholder() {
		placeholder.setVisibility(View.VISIBLE);
		placeholder.setTextLine2(R.string.wallet_restore_placeholder);
		placeholder.showButton(R.string.wallet_restore_wallet, () -> getPresenter().onRestoreWalletButtonClicked());
	}

	@Override
	public void hidePlaceholder() {
		placeholder.setVisibility(View.GONE);
	}

	@Override
	public void showNoWalletPlaceholder(String path, String filename) {
		final String message = getString(R.string.wallet_not_found_placeholder_message, path, filename);
		placeholder.setVisibility(View.VISIBLE);
		placeholder.setTextLine2(message);
		placeholder.showButton(R.string.wallet_create_button, () -> getPresenter().onCreateWalletButtonClicked());
	}

	//endregion

	//region Errors

	@Override
	public void showIncorrectPinError() {
		CoreApp
			.getNavigation()
			.prepareUniversalDialog(
				null,
				getString(R.string.pin_wrong_pin),
				getString(R.string.pin_try_again),
				universalDialogView -> getPresenter().onRestoreWalletButtonClicked())
			.setCancelableOnTouchOutside(false)
			.setCancelable(true)
			.withCancelButton()
			.showAllowingStateLoss();
	}

	//endregion
}
