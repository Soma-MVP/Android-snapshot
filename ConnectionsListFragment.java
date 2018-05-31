package co.soma.app.view.profile.tabs.connections;

import static co.soma.app.data.defs.FriendshipActions.ADD;
import static co.soma.app.data.defs.FriendshipActions.REJECT;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.InputType;
import android.widget.Button;
import butterknife.BindView;
import butterknife.OnClick;
import co.soma.app.R;
import co.soma.app.view.common.list.PlaceholderListFragment;
import co.soma.app.view.profile.user.UserProfileFragment;
import co.soma.app.view.single.SingleFragmentActivity;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.view.dialogfragment.CoreDialogFragmentView;
import pl.itcraft.core.view.dialogfragment.CoreInputDialogFragment;

public class ConnectionsListFragment extends PlaceholderListFragment<ConnectionsListPresenter>
	implements ConnectionsListView {

	//region Bundle

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_friends;
	}

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Navigation

	@Override
	public void navigateToOtherUserProfile(@NonNull Long userId, @NonNull String username) {
		CoreApp.getNavigation().openActivity(
			SingleFragmentActivity.class,
			0,
			SingleFragmentActivity.generateBundle(
				UserProfileFragment.class,
				UserProfileFragment.prepareBundleForUserProfile(userId, username)));
	}

	//endregion

	//region Layout Manager

	@Override
	protected LinearLayoutManager provideLayoutManager() {
		return new LinearLayoutManager(getContext());
	}

	@Override
	protected LayoutManager providePlaceholderLayoutManager() {
		return new LinearLayoutManager(getContext());
	}

	//endregion

	//region Shimmer

	@Override
	protected int providePlaceholderLayout() {
		return R.layout.item_person_placeholder;
	}

	//endregion

	//region Snacks

	@Override
	public void showAcceptOrRejectSuccessMessage(String status) {
		switch (status) {
			case ADD:
				CoreApp.getSnackManager()
					   .prepareSnack(R.string.accept_invitation_success, Mode.SUCCESS)
					   .show();
				break;
			case REJECT:
				CoreApp.getSnackManager()
					   .prepareSnack(R.string.reject_invitation_success, Mode.SUCCESS)
					   .show();
				break;
		}
	}

	@Override
	public void showInviteToSomaSuccessMessage(int successEmails, int totalEmails) {
		CoreApp.getSnackManager()
			   .prepareSnack(
				   getString(R.string.profile_invite_friends_to_soma_success, successEmails, totalEmails),
				   Mode.SUCCESS)
			   .show();
	}

	//endregion

	//region Errors

	@Override
	public void showAlreadyFriendsMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.users_are_already_friends, Mode.ERROR)
			   .show();
	}

	@Override
	public void showUnableFriendYourselfMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.unable_to_add_yourself_to_your_friends, Mode.ERROR)
			   .show();
	}

	@Override
	public void showInvitationExistsMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.invitation_already_exists, Mode.ERROR)
			   .show();
	}

	//endregion

	//region Dialogs

	@Override
	public void showRejectInvitationDialog(Long userId, String status) {
		CoreApp.getNavigation().prepareUniversalDialog(
			R.string.empty,
			R.string.reject_invitation_ask,
			R.string.commonYes,
			dialog -> getPresenter().manageFriendInvitation(userId, status))
			   .withNegativeButton(R.string.commonNo, CoreDialogFragmentView::dismissDialog)
			   .showAllowingStateLoss();
	}

	@Override
	public void showInviteInputDialog() {
		CoreApp.getNavigation()
			   .prepareInputDialog(CoreInputDialogFragment.class)
			   .withTitle(R.string.invite_friends_to_soma)
			   .withPositiveResultButton(R.string.common_invite,
										 emailsToSend -> {
											 if (!emailsToSend.trim().isEmpty()) {
												 getPresenter().inviteFriendsToSoma(emailsToSend);
											 }
										 })
			   .withHint(R.string.profile_enter_emails_to_invite_hint)
			   .withInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
			   .withFloatingHintEnabled(true)
			   .setCancelableOnTouchOutside(true)
			   .withCancelButton()
			   .showAllowingStateLoss();
	}

	//endregion

	//region Visibility

	@Override
	public void setInviteToSomaButtonEnabled(boolean isEnable) {
		inviteToSomaButton.setEnabled(isEnable);
	}

	//endregion

	//region Bindings

	@BindView(R.id.inviteToSomaButton)
	Button inviteToSomaButton;

	@OnClick(R.id.inviteToSomaButton)
	void onInviteToSomaButton() {
		getPresenter().onInviteToSomaButtonClicked();
	}

	//endregion
}
