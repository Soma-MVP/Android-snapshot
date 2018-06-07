package co.soma.app.view.profile.user;

import co.soma.app.view.profile.base.BaseProfileView;

interface UserProfileView extends BaseProfileView {

	//region Dialogs

	void showReportUserDialog();

	//endregion

	//region Snacks

	void showReportUserSuccessMessage();

	void showCreateFollowingSuccessMessage();

	void showUnFollowingSuccessMessage();

	void showInvitationToFriendsSuccessMessage();

	void showRemoveFromFriendsSuccessMessage();

	//endregion

	//region Errors

	void showUserIsAlreadyFollowingMessage();

	void showUserIsNotFollowingMessage();

	void showAlreadyFriendsMessage();

	void showUnableFriendYourselfMessage();

	void showInvitationExistsMessage();

	//endregion
}
