package co.soma.app.view.profile.tabs.connections;

import static co.soma.app.data.defs.FriendshipActions.ADD;
import static co.soma.app.data.defs.FriendshipActions.REJECT;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import co.soma.app.R;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.api.exceptions.ApiErrorCode;
import co.soma.app.data.api.exceptions.ApiException;
import co.soma.app.data.exceptions.RepositoryError;
import co.soma.app.data.exceptions.RepositoryExceptionProcessor;
import co.soma.app.data.models.DataContainer;
import co.soma.app.data.models.base.Model;
import co.soma.app.data.models.people.PageablePersonModel;
import co.soma.app.data.models.people.PersonModel;
import co.soma.app.view.common.items.PersonItem;
import co.soma.app.view.common.items.PersonItem.PersonItemHandler;
import co.soma.app.view.common.list.PlaceholderListPresenter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import org.apache.commons.lang3.Validate;
import pl.itcraft.core.executor.Executable;
import pl.itcraft.core.executor.Executor;

class ConnectionsListPresenter extends PlaceholderListPresenter<ConnectionsListView, Void, PageablePersonModel>
	implements PersonItemHandler {

	//region Constructor

	protected ConnectionsListPresenter(Context context, ConnectionsListView view) {
		super(context, view);
	}

	//endregion

	//region Providers

	@Override
	protected String providePlaceholder() {
		return getString(R.string.placeholder_no_connections);
	}

	@Override
	protected Executable<DataContainer<PageablePersonModel>, RepositoryError, RepositoryExceptionProcessor<DataContainer<PageablePersonModel>>> provideLoadExecutable() {
		return RepositoryManager.get()
								.getFriendsManager()
								.requestFriends(getDataSource(), getNextPageToken());
	}

	@Override
	protected IItem mapFromModel(@NonNull Model model) {
		Validate.isInstanceOf(PersonModel.class, model);
		return new PersonItem((PersonModel) model, false, this);
	}

	//endregion

	//region Clicks

	void onInviteToSomaButtonClicked() {
		getView().showInviteInputDialog();
	}

	//endregion

	//region Manage Friends

	public void manageFriendInvitation(Long userId, String status) {
		Executor.getBuilder(RepositoryManager.get()
											 .getFriendsManager()
											 .manageFriendInvitation(status, userId))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> {
						getView().showAcceptOrRejectSuccessMessage(status);
						refreshItemList();
					})
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.withErrorResultListener(this::handleAddFriendApiError)
				.execute();
	}

	private void manageFriends(Long userId, String status) {
		switch (status) {
			case ADD:
				manageFriendInvitation(userId, status);
				break;
			case REJECT:
				getView().showRejectInvitationDialog(userId, status);
				break;
		}
	}

	//endregion

	//region Action

	@Override
	protected void handlePreAction() {
		super.handlePreAction();
		getView().setInviteToSomaButtonEnabled(false);
	}

	@Override
	protected void handleFinishAction() {
		super.handleFinishAction();
		getView().setInviteToSomaButtonEnabled(true);
	}

	//endregion

	//region Invite Friends

	public void inviteFriendsToSoma(String emails) {
		Executor.getBuilder(RepositoryManager.get()
											 .getFriendsManager()
											 .inviteFriends(emails))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> {
						final int success = newResult.getSuccess();
						final int total = newResult.getTotal();
						getView().showInviteToSomaSuccessMessage(success, total);
					})
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.execute();
	}

	//endregion

	//region Validation

	private boolean handleAddFriendApiError(@NonNull RepositoryError error) {
		final ApiException exception = error.getApiException();
		if (exception == null) {
			return false;
		}

		final ApiErrorCode code = exception.getApiErrorCode();
		switch (code) {
			case ALREADY_FRIENDS:
				getView().showAlreadyFriendsMessage();
				return true;
			case UNABLE_FRIEND_YOURSELF:
				getView().showUnableFriendYourselfMessage();
				return true;
			case INVITATION_EXISTS:
				getView().showInvitationExistsMessage();
				return true;
			default:
				return false;
		}
	}

	//endregion

	//region Click

	@Override
	public boolean onClick(View v, IAdapter<PersonItem> adapter, PersonItem item, int position) {
		getView().navigateToOtherUserProfile(item.getItem().getUserId(), item.getItem().getUsername());
		return false;
	}

	@Override
	public void onAcceptOrRejectClicked(Long userId, String action) {
		manageFriends(userId, action);
	}

	//endregion

}
