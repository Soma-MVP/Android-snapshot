package co.soma.app.view.profile.user;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import android.content.Context;
import android.support.annotation.NonNull;
import co.soma.app.R;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.api.exceptions.ApiErrorCode;
import co.soma.app.data.api.exceptions.ApiException;
import co.soma.app.data.defs.FriendshipActions;
import co.soma.app.data.exceptions.RepositoryError;
import co.soma.app.data.models.profile.user.UserProfileModel;
import co.soma.app.view.profile.base.BaseProfilePresenter;
import co.soma.app.view.profile.tabs.items.UserItemsListFragment;
import pl.itcraft.core.adapter.CoreFragmentPagerAdapter;
import pl.itcraft.core.executor.Executor;

class UserProfilePresenter extends BaseProfilePresenter<UserProfileView, Void> {

	//region Arguments

	static final String ARG_USER_ID  = "argUserId";
	static final String ARG_USERNAME = "argUsername";

	private Long userId = 0L;

	//endregion

	//region Constructor

	protected UserProfilePresenter(Context context, UserProfileView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onAttach() {
		super.onAttach();

		if (getView().getArguments() != null) {
			userId = getView().getArguments().getLong(ARG_USER_ID, 0L);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (userId != 0L) {

			getView().resetScreenCollapse();

			setShouldShowShimmerPlaceholder(true);
			getView().showPlaceholderLinksVisibility(false, true);

			requestOtherProfile(userId);
		}
	}

	//endregion

	//region Pages

	@Override
	public void addPagesToAdapter(CoreFragmentPagerAdapter adapter) {
		getView().getPagerAdapter()
				 .addFragment(
					 R.string.profile_items_tab_title,
					 UserItemsListFragment.class,
					 UserItemsListFragment.prepareBundleForUserItems(userId));
	}

	//endregion

	//region Providers

	@Override
	protected void provideNavigateToFollowersView() {
		// No-op.
	}

	@Override
	protected void provideNavigateToFollowingsView() {
		// No-op.
	}

	@Override
	protected void provideNavigateToChatView() {
		final Long senderId = RepositoryManager.get().getUserManager().getUserId();
		if (getView().getArguments() != null) {
			final Long recipientId = getView().getArguments().getLong(ARG_USER_ID);
			getView().navigateToChatView(senderId, recipientId);
		}
	}

	@Override
	protected void provideCreateOrDeleteFollowing() {
		if (isFollow) {
			if (getView().getArguments() != null) {
				unfollowUser(getView().getArguments().getLong(ARG_USER_ID));
			}
		} else {
			if (getView().getArguments() != null) {
				followUser(getView().getArguments().getLong(ARG_USER_ID));
			}
		}
	}

	@Override
	protected void provideNavigateToEditProfileView() {
		// No-op.
	}

	//endregion

	//region My Profile

	private UserProfileModel userProfileModel;
	private boolean          isFollow;
	private boolean          isFriend;

	public boolean isFriend() {
		return isFriend;
	}

	private void requestOtherProfile(Long userId) {
		Executor.getBuilder(RepositoryManager.get().getUserManager().requestOtherProfile(userId))
				.withPreStartAction(() -> getView().setLinksEnabled(false))
				.withSimpleResultListener((newResult, thisExecutor) -> {
					userProfileModel = newResult.getData();
					final Boolean isFollower = userProfileModel.getFollower();
					final String username = userProfileModel.getUsername();
					final String about = defaultIfBlank(userProfileModel.getAbout(), EMPTY);
					final String locationName = userProfileModel.getLocationName();
					final String profilePhotoUrl = userProfileModel.getProfilePhotoUrl();
					final int itemsCounter = userProfileModel.getNumberOfItems();
					final int followingsCounter = userProfileModel.getNumberOfFollowings();
					final int followersCounter = userProfileModel.getNumberOfFollowers();
					final float rating = userProfileModel.getRating().floatValue();
					isFollow = userProfileModel.getFollower();
					isFriend = userProfileModel.getFriend();

					getView().showLinksVisibility(false, true);
					getView().updateFollowerLinkText(isFollower);
					getView().setPhoto(profilePhotoUrl);
					getView().updateProfileDataView(username, locationName, about, rating);
					getView().updateCounters(itemsCounter, followingsCounter, followersCounter);

					//refresh options
					getView().getParentActivity().invalidateOptionsMenu();
				})
				.withOnFinishExecutor(() -> getView().setLinksEnabled(true))
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.execute();
	}

	//endregion

	//region Profile Title

	public String getProfileUsernameTitle() {
		return getView().getArguments() != null ? getView().getArguments().getString(ARG_USERNAME) : EMPTY;
	}

	//endregion

	//region Manage Friends

	public void addToFriends() {
		manageFriends(FriendshipActions.ADD);
	}

	public void removeFromFriends() {
		manageFriends(FriendshipActions.REJECT);
	}

	private void manageFriends(String status) {
		Executor.getBuilder(RepositoryManager.get()
											 .getFriendsManager()
											 .manageFriendInvitation(status, userId))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> {
						switch (status) {
							case FriendshipActions.ADD:
								getView().showInvitationToFriendsSuccessMessage();
								isFriend = true;
								break;
							case FriendshipActions.REJECT:
								getView().showRemoveFromFriendsSuccessMessage();
								isFriend = false;
								break;
						}
						getView().getParentActivity().invalidateOptionsMenu();
					})
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.withErrorResultListener(this::handleManageFriendsApiError)
				.execute();
	}

	//endregion

	//region Following Action

	private void followUser(@NonNull Long userId) {
		Executor.getBuilder(RepositoryManager.get().getFollowingManager().createFollowing(userId))
				.withPreStartAction(() -> getView().setLinksEnabled(false))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> {
						getView().showCreateFollowingSuccessMessage();
						getView().updateFollowerLinkText(true);
						isFollow = true;
					})
				.withOnFinishExecutor(() -> getView().setLinksEnabled(true))
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.execute();
	}

	private void unfollowUser(@NonNull Long userId) {
		Executor.getBuilder(RepositoryManager.get().getFollowingManager().deleteFollowing(userId))
				.withPreStartAction(() -> getView().setLinksEnabled(false))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> {
						getView().showUnFollowingSuccessMessage();
						getView().updateFollowerLinkText(false);
						isFollow = false;
					})
				.withOnFinishExecutor(() -> getView().setLinksEnabled(true))
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.withErrorResultListener(this::handleUnfollowApiError)
				.execute();
	}

	//endregion

	//region Report User

	public void onReportUserClicked() {
		getView().showReportUserDialog();
	}

	public void reportUser(@NonNull String reason) {
		Executor.getBuilder(RepositoryManager.get().getUserManager().reportUser(userId, reason))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> getView().showReportUserSuccessMessage())
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.execute();
	}

	//endregion

	//region Validation

	private boolean handleUnfollowApiError(RepositoryError error) {
		final ApiException exception = error.getApiException();
		if (exception == null) {
			return false;
		}

		final ApiErrorCode code = exception.getApiErrorCode();
		switch (code) {
			case USER_NOT_FOLLOWING:
				getView().showUserIsNotFollowingMessage();
				return true;
			case USER_ALREADY_FOLLOWING:
				getView().showUserIsAlreadyFollowingMessage();
				return true;
			default:
				break;
		}

		return false;
	}

	private boolean handleManageFriendsApiError(@NonNull RepositoryError error) {
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
}
