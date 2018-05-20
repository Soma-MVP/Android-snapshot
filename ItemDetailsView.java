package co.soma.app.view.item.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import co.soma.app.data.models.item.ItemModel;
import co.soma.app.view.common.flipboard.BaseFlipBoardBottomSheetFragment;
import co.soma.app.view.item.details.items.DetailsGalleryItem;
import java.util.List;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.fragment.CoreFragmentView;

interface ItemDetailsView extends CoreFragmentView, ProgressIndicator {

	//region Form

	void showItemImage(List<DetailsGalleryItem> photoItems);

	void showItemTitle(ItemModel model);

	void showItemPrice(ItemModel model);

	void showItemAbout(ItemModel model);

	void showItemLikes(ItemModel model);

	void showItemViews(ItemModel model);

	void showUserAvatar(ItemModel model);

	void showUserName(ItemModel model);

	void showUserRating(ItemModel model);

	void showItemLocation(ItemModel model);

	void showItemLocationName(ItemModel model);

	void showItemButtons(ItemModel model, Long myId);

	void clearPhotoList();

	void setLikedButtonEnabled(boolean isEnabled);

	void changeColorOfHeartIcon(boolean isLiked);

	void collapseHeadedView();

	//endregion

	//region Navigation

	void navigateToUserProfile(@NonNull Long ownerId, @NonNull Long myId, @NonNull String ownerUsername);

	void navigateToChatView(
		@NonNull Long buyerId, @NonNull Long sellerId, @NonNull Long itemId, @NonNull boolean immediateOffer);

	void navigateToChatView(@NonNull Long buyerId, @NonNull Long sellerId, @NonNull Long itemId);

	//endregion

	//region Bottom Sheet

	void showInBottomSheet(Class<? extends BaseFlipBoardBottomSheetFragment> fragmentClass, Bundle bundle);

	void dismissBottomSheet();

	//endregion

	//region Progress Indicator

	void showBasicsPlaceholder(boolean flag);

	void showUserLocationPlaceholder(boolean flag);

	void showItemPreviewPlaceholder(boolean flag);

	ProgressIndicator showHeartProgressIndicator();

	//endregion

	//region Buttons

	void setActionButtonsEnabled(boolean flag);

	void showResellButton(boolean flag);

	//endregion

	//region Errors

	void showItemLikeNotFoundMessage();

	//endregion
}
