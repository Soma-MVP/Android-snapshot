package co.soma.app.view.item.details;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import android.content.Context;
import android.support.annotation.NonNull;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.api.exceptions.ApiErrorCode;
import co.soma.app.data.api.exceptions.ApiException;
import co.soma.app.data.defs.ItemAction;
import co.soma.app.data.exceptions.RepositoryError;
import co.soma.app.data.executor.DataSource;
import co.soma.app.data.models.item.ItemModel;
import co.soma.app.data.models.item.ItemPhotoModel;
import co.soma.app.view.item.details.items.DetailsGalleryItem;
import co.soma.app.view.item.details.share.ShareItemFragment;
import java.util.ArrayList;
import java.util.List;
import pl.itcraft.core.executor.Executor;
import pl.itcraft.core.view.fragment.CoreFragmentPresenter;

class ItemDetailsPresenter extends CoreFragmentPresenter<ItemDetailsView, Void> {

	//region Arguments

	static final String ARG_ITEM_MODEL = "argItemModel";
	static final String ARG_ITEM_ID    = "argItemId";
	static final String ARG_ITEM_TITLE = "argItemTitle";

	private static final Long DEFAULT_LONG_VALUE = -1L;

	private ItemModel model     = null;
	private Long      itemId    = DEFAULT_LONG_VALUE;
	private String    itemTitle = EMPTY;

	ItemModel getModel() {
		return model;
	}

	public String getItemTitle() {
		return itemTitle;
	}

	//endregion

	//region Constructor

	protected ItemDetailsPresenter(Context context, ItemDetailsView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onAttach() {
		super.onAttach();

		if (getView().getArguments() != null) {
			model = getView().getArguments().getParcelable(ARG_ITEM_MODEL);
			itemId = getView().getArguments().getLong(ARG_ITEM_ID, DEFAULT_LONG_VALUE);
			itemTitle = getView().getArguments().getString(ARG_ITEM_TITLE, EMPTY);
		}

		fetchMyId();
	}

	@Override
	public void onResume() {
		super.onResume();

		getView().clearPhotoList();
		fetchItemDetails(model != null ? model.getId() : itemId);
	}

	//endregion

	//region Map

	void onMapReady() {
		// TODO:
	}

	//endregion

	//region User details

	private Long myId;

	private void fetchMyId() {
		myId = RepositoryManager.get().getUserManager().getUserId();
	}

	//endregion

	//region Item details

	private void fetchItemDetails(Long itemId) {
		Executor.getBuilder(RepositoryManager.get().getItemManager().getItem(DataSource.DATABASE_NEXT_API, itemId))
				.withPreStartAction(() -> getView().setActionButtonsEnabled(false))
				.withSimpleResultListener(((newResult, thisExecutor) -> {
					model = newResult.getData();
					convertPhotosAndDisplay(model.getPhotos());
					getView().showItemTitle(model);
					getView().showItemPrice(model);
					getView().showItemAbout(model);
					getView().showItemLikes(model);
					getView().showItemViews(model);
					getView().showUserAvatar(model);
					getView().showUserName(model);
					getView().showUserRating(model);
					getView().showItemLocation(model);
					getView().showItemLocationName(model);
					getView().showItemButtons(model, myId);
					getView().changeColorOfHeartIcon(model.getLiked());
					getView().showResellButton(model.getAllowReselling());
				}))
				.withOnFinishExecutor(() -> getView().setActionButtonsEnabled(true))
				.bindProgressIndicator(this::getView)
				.bindPresenter(this)
				.execute();
	}

	//endregion

	//region Button handlers

	void onUsernameClicked() {
		getView().navigateToUserProfile(model.getOwnerId(), myId, model.getOwnerUsername());
	}

	void onLikeButtonClicked() {
		itemAction(model.getId(), model.getLiked() ? ItemAction.UNLIKE : ItemAction.LIKE);
	}

	void onShareButtonClicked() {
		getView().collapseHeadedView();
		getView().showInBottomSheet(ShareItemFragment.class, ShareItemFragment.generateBundle());
	}

	void onBuyButtonClicked() {
		final Long sellerId = model.getOwnerId();
		final Long itemId = model.getId();
		getView().navigateToChatView(myId, sellerId, itemId, false);
	}

	void onOfferButtonClicked() {
		final Long sellerId = model.getOwnerId();
		final Long itemId = model.getId();
		getView().navigateToChatView(myId, sellerId, itemId, true);
	}

	void onResellButtonClicked() {
		final Long sellerId = model.getOwnerId();
		final Long itemId = model.getId();
		getView().navigateToChatView(myId, sellerId, itemId);
	}

	//endregion

	//region Convert

	private void convertPhotosAndDisplay(List<ItemPhotoModel> itemPhotos) {
		if (!itemPhotos.isEmpty()) {
			final List<DetailsGalleryItem> toShow = new ArrayList<>();
			for (ItemPhotoModel photo : itemPhotos) {
				toShow.add(new DetailsGalleryItem(photo, null));
			}
			getView().showItemImage(toShow);
		}
	}

	//endregion

	//region Likes

	public void itemAction(Long itemId, @ItemAction String itemAction) {
		Executor.getBuilder(RepositoryManager.get()
											 .getItemManager()
											 .itemAction(itemId, itemAction))
				.withPreStartAction(() -> getView().setLikedButtonEnabled(false))
				.withSimpleResultListener(
					(newResult, thisExecutor) -> {
						model = newResult;
						getView().showItemLikes(model);
						getView().changeColorOfHeartIcon(model.getLiked());
					})
				.withErrorResultListener(this::handleItemLikeErrors)
				.bindPresenter(this)
				.bindProgressIndicator(getView().showHeartProgressIndicator())
				.withOnFinishExecutor(() -> getView().setLikedButtonEnabled(true))
				.execute();
	}

	//endregion

	//region Validation

	private boolean handleItemLikeErrors(@NonNull RepositoryError error) {
		final ApiException exception = error.getApiException();
		if (exception == null) {
			return false;
		}

		final ApiErrorCode code = exception.getApiErrorCode();
		switch (code) {
			case ITEM_LIKE_NOT_FOUND:
				getView().showItemLikeNotFoundMessage();
				return true;
			default:
				return false;
		}
	}

	//endregion
}
