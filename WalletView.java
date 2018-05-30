package co.soma.app.view.wallet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import pl.itcraft.core.executor.Actions.ParameterAction;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.fragment.CoreFragmentView;

interface WalletView extends CoreFragmentView, ProgressIndicator {

	//region Wallet

	void setWalletAddress(String walletAddress);

	void setEthBalance(@Nullable String balance);

	void setSctBalance(@Nullable String balance);

	//endregion

	//region Dialogs

	ProgressIndicator getEnterPinDialogProgressIndicator();

	ProgressIndicator getRestoreWalletDialogProgressIndicator();

	void showEnterPinDialog(@NonNull Bundle bundle, @NonNull ParameterAction<String> action);

	//endregion

	//region Placeholders

	void showRestorePlaceholder();

	void hidePlaceholder();

	void showNoWalletPlaceholder(String path, String filename);

	//endregion

	//region Errors

	void showIncorrectPinError();

	//endregion
}
