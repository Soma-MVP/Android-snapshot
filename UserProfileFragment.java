package co.soma.app.view.profile.user;

import static co.soma.app.view.profile.user.UserProfilePresenter.ARG_USERNAME;
import static co.soma.app.view.profile.user.UserProfilePresenter.ARG_USER_ID;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import co.soma.app.R;
import co.soma.app.view.conversation.chat.contextless.ContextlessChatFragment;
import co.soma.app.view.profile.base.BaseProfileFragment;
import co.soma.app.view.single.SingleFragmentActivity;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.view.activity.HeadedView;
import pl.itcraft.core.view.dialogfragment.CoreInputDialogFragment;

public class UserProfileFragment extends BaseProfileFragment<UserProfilePresenter>
	implements UserProfileView, HeadedView {

	//region UI

	@Override
	public CharSequence getTitle() {
		return getPresenter().getProfileUsernameTitle();
	}

	//endregion

	//region Bundle

	public static Bundle prepareBundleForUserProfile(@NonNull Long userId, @NonNull String username) {
		Bundle bundle = new Bundle();
		bundle.putLong(ARG_USER_ID, userId);
		bundle.putString(ARG_USERNAME, username);
		return bundle;
	}

	//endregion

	//region Lifecycle

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
	}

	//endregion

	//region Options

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_more, menu);
		final MenuItem addToFriendsItem = menu.findItem(R.id.action_add_friend);
		final MenuItem removeFromFriendsItem = menu.findItem(R.id.action_remove_friend);
		final boolean isFriend = getPresenter().isFriend();
		addToFriendsItem.setVisible(!isFriend);
		removeFromFriendsItem.setVisible(isFriend);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_friend:
				getPresenter().addToFriends();
				break;
			case R.id.action_remove_friend:
				getPresenter().removeFromFriends();
				break;
			case R.id.action_report:
				getPresenter().onReportUserClicked();
				break;
		}
		return true;
	}

	//endregion

	//region Navigation

	@Override
	public void navigateToChatView(Long senderId, Long recipientId) {
		super.navigateToChatView(senderId, recipientId);

		CoreApp.getNavigation().openActivity(
			SingleFragmentActivity.class,
			0,
			SingleFragmentActivity.generateBundle(
				ContextlessChatFragment.class,
				ContextlessChatFragment.prepareForContextless(senderId, recipientId)
			)
		);
	}

	//endregion

	//region Dialogs

	@Override
	public void showReportUserDialog() {
		CoreApp.getNavigation()
			   .prepareInputDialog(CoreInputDialogFragment.class)
			   .withTitle(R.string.profile_report_user)
			   .withPositiveResultButton(R.string.profile_report_option,
										 reason -> {
											 if (!reason.trim().isEmpty()) {
												 getPresenter().reportUser(reason);
											 }
										 })
			   .withHint(R.string.profile_enter_reason)
			   .withFloatingHintEnabled(true)
			   .setCancelableOnTouchOutside(true)
			   .withCancelButton()
			   .showAllowingStateLoss();
	}

	//endregion

	//region Snacks

	@Override
	public void showReportUserSuccessMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.profile_report_user_success, Mode.SUCCESS)
			   .show();
	}

	@Override
	public void showCreateFollowingSuccessMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.create_following_success, Mode.SUCCESS)
			   .show();
	}

	@Override
	public void showUnFollowingSuccessMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.delete_following_success, Mode.SUCCESS)
			   .show();
	}

	@Override
	public void showInvitationToFriendsSuccessMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.profile_add_to_friends_success, Mode.SUCCESS)
			   .show();
	}

	@Override
	public void showRemoveFromFriendsSuccessMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.profile_remove_from_friends_success, Mode.SUCCESS)
			   .show();
	}

	//endregion

	//region Errors

	@Override
	public void showUserIsAlreadyFollowingMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.user_is_already_following, Mode.ERROR)
			   .show();
	}

	@Override
	public void showUserIsNotFollowingMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.user_is_not_following, Mode.ERROR)
			   .show();
	}

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
}
