package co.soma.app.view.profile.tabs.connections;

import android.support.annotation.NonNull;
import co.soma.app.view.common.list.PlaceholderListView;

interface ConnectionsListView extends PlaceholderListView {

	//region Navigation

	void navigateToOtherUserProfile(@NonNull Long userId, @NonNull String username);

	//endregion

	//region Snacks

	void showAcceptOrRejectSuccessMessage(String status);

	void showInviteToSomaSuccessMessage(int successEmails, int totalEmails);

	//endregion
	
	//region Errors

	void showAlreadyFriendsMessage();

	void showUnableFriendYourselfMessage();

	void showInvitationExistsMessage();
	
	//endregion

	//region Dialogs

	void showRejectInvitationDialog(Long userId, String status);

	void showInviteInputDialog();

	//endregion

	//region Enable

	void setInviteToSomaButtonEnabled(boolean isEnable);

	//endregion
}
