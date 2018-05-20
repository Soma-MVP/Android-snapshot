package co.soma.app.view.item.details;

import static co.soma.app.view.item.details.ItemDetailsPresenter.ARG_ITEM_ID;
import static co.soma.app.view.item.details.ItemDetailsPresenter.ARG_ITEM_MODEL;
import static co.soma.app.view.item.details.ItemDetailsPresenter.ARG_ITEM_TITLE;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import co.soma.app.R;
import co.soma.app.data.models.item.ItemModel;
import co.soma.app.util.currency.CurrencyUtils;
import co.soma.app.util.image.GlideApp;
import co.soma.app.util.view.ViewUtil;
import co.soma.app.view.common.flipboard.BaseFlipBoardBottomSheetFragment;
import co.soma.app.view.conversation.chat.ChatFragment;
import co.soma.app.view.interpolator.BounceInterpolator;
import co.soma.app.view.item.details.items.DetailsGalleryItem;
import co.soma.app.view.profile.user.UserProfileFragment;
import co.soma.app.view.single.SingleFragmentActivity;
import co.soma.app.view.widget.SnappyHorizontalRecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import io.supercharge.shimmerlayout.ShimmerLayout;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.view.ButterHolder;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.activity.CoreFragmentActivity;
import pl.itcraft.core.view.activity.HeadedView;
import pl.itcraft.core.view.fragment.CoreFragment;

public class ItemDetailsFragment extends CoreFragment<ItemDetailsPresenter>
	implements ItemDetailsView, OnMapReadyCallback, HeadedView {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_item_details;
	}

	@Override
	public CharSequence getTitle() {
		return getPresenter().getModel() != null ?
			   getPresenter().getModel().getTitle() :
			   getPresenter().getItemTitle();
	}

	//endregion

	//region Bundle

	public static Bundle prepareForDetails(@NonNull ItemModel model) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(ARG_ITEM_MODEL, model);
		return bundle;
	}

	public static Bundle prepareBundleWithIdForDetails(@NonNull Long itemId, @NonNull String itemTitle) {
		final Bundle bundle = new Bundle();
		bundle.putLong(ARG_ITEM_ID, itemId);
		bundle.putString(ARG_ITEM_TITLE, itemTitle);
		return bundle;
	}

	//endregion

	//region Lifecycle

	@Override
	protected void onPostCreateView(View fragmentView, Bundle savedInstanceState) {
		initializeMapView();
		initRecyclerView();
		super.onPostCreateView(fragmentView, savedInstanceState);
	}

	//endregion

	//region Form

	@Override
	public void showItemImage(List<DetailsGalleryItem> photoItems) {
		galleryAdapter.setNewList(photoItems);
		addDotsToView();
	}

	@Override
	public void showItemTitle(ItemModel model) {
		final String title = model.getTitle();
		itemTitle.setText(title);
	}

	@Override
	public void showItemPrice(ItemModel model) {
		final String unit = model.getPriceUnit();
		final Long price = model.getPrice();
		final Integer decimals = model.getDecimalFraction();
		final String formatted = CurrencyUtils.humanReadable(price, unit, decimals);
		itemPrice.setText(formatted);
	}

	@Override
	public void showItemAbout(ItemModel model) {
		final String about = model.getDescription();
		itemAbout.setText(about);
	}

	@Override
	public void showItemLikes(ItemModel model) {
		final String likes = String.valueOf(model.getNumberOfLikes());
		likesIndicator.setText(likes);
	}

	@Override
	public void showItemViews(ItemModel model) {
		final String views = String.valueOf(model.getNumberOfViews());
		viewsIndicator.setText(views);
	}

	@Override
	public void showUserAvatar(ItemModel model) {
		if (getContext() != null) {
			GlideApp.with(getContext())
					.load(model.getOwnerProfilePhotoUrl())
					.placeholder(R.drawable.avatar)
					.fitCenter()
					.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
					.apply(RequestOptions.circleCropTransform())
					.into(userAvatar);
		}
	}

	@Override
	public void showUserName(ItemModel model) {
		final String username = model.getOwnerUsername();
		userName.setText(username);
	}

	@Override
	public void showUserRating(ItemModel model) {
		userRating.setRating(model.getRating().floatValue());
	}

	@Override
	public void changeColorOfHeartIcon(boolean isLiked) {
		if (getContext() != null) {
			likesIcon.setColorFilter(
				ContextCompat.getColor(getContext(), isLiked ? R.color.somaRed : R.color.somaLightText));
		}
	}

	@Override
	public void showItemLocation(ItemModel model) {
		if (mapReady) {
			if (currentLocation != null) {
				currentLocation.remove();
			}

			final Double lat = model.getLatitude();
			final Double lng = model.getLongitude();

			if (lat == null || lng == null) {
				return;
			}

			final LatLng position = new LatLng(lat, lng);
			final BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin);
			final MarkerOptions options = new MarkerOptions().position(position).icon(bitmap);
			currentLocation = map.addMarker(options);
			final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(position, DEFAULT_MAP_ZOOM);
			map.moveCamera(update);
		}
	}

	@Override
	public void showItemLocationName(ItemModel model) {
		final String location = model.getLocationName();
		itemLocationName.setText(location);
	}

	@Override
	public void showItemButtons(ItemModel model, Long myId) {
		final Long ownerId = model.getOwnerId();
		final boolean shouldShow = NumberUtils.compare(ownerId, myId) != 0;
		ViewUtil.changeVisibility(buyButton, shouldShow);
		ViewUtil.changeVisibility(offerButton, shouldShow);
		ViewUtil.changeVisibility(resellButton, shouldShow && model.getAllowReselling());
	}

	@Override
	public void clearPhotoList() {
		galleryAdapter.clear();
	}

	@Override
	public void setLikedButtonEnabled(boolean isEnabled) {
		likesIcon.setEnabled(isEnabled);
		likesIcon.setClickable(isEnabled);
		likesIcon.setFocusable(isEnabled);
	}

	//endregion

	//region Collapse

	@Override
	public void collapseHeadedView() {
		((SingleFragmentActivity) getParentActivity()).collapseHeader(true);
	}

	//endregion

	//region ProgressIndicator

	@Override
	public ProgressIndicator showHeartProgressIndicator() {
		return new ProgressIndicator() {
			@Override
			public void showProgressIndicator() {
				Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
				BounceInterpolator interpolator = new BounceInterpolator(0.2, 20);
				animation.setInterpolator(interpolator);
				likesIcon.startAnimation(animation);
			}

			@Override
			public void hideProgressIndicator() {
				likesIcon.clearAnimation();
			}
		};
	}

	//endregion

	//region Navigation

	@Override
	public void navigateToUserProfile(@NonNull Long ownerId, @NonNull Long myId, @NonNull String ownerUsername) {
		if (NumberUtils.compare(ownerId, myId) != 0) {
			CoreApp.getNavigation().openActivity(
				SingleFragmentActivity.class,
				0,
				SingleFragmentActivity.generateBundle(
					UserProfileFragment.class,
					UserProfileFragment.prepareBundleForUserProfile(ownerId, ownerUsername)));
		}
	}

	@Override
	public void navigateToChatView(
		@NonNull Long buyerId, @NonNull Long sellerId, @NonNull Long itemId, @NonNull boolean immediateOffer) {
		CoreApp.getNavigation().openActivity(
			SingleFragmentActivity.class,
			0,
			SingleFragmentActivity.generateBundle(
				ChatFragment.class,
				ChatFragment.prepareForContextual(sellerId, buyerId, itemId, immediateOffer)
			)
		);
	}

	@Override
	public void navigateToChatView(@NonNull Long buyerId, @NonNull Long sellerId, @NonNull Long itemId) {
		CoreApp.getNavigation().openActivity(
			SingleFragmentActivity.class,
			0,
			SingleFragmentActivity.generateBundle(
				ChatFragment.class,
				ChatFragment.prepareForReselling(sellerId, buyerId, itemId)
			)
		);
	}

	//endregion

	//region Bottom Sheet

	@BindView(R.id.bottomSheet)
	BottomSheetLayout bottomSheetLayout;

	@Override
	public void showInBottomSheet(Class<? extends BaseFlipBoardBottomSheetFragment> fragmentClass, Bundle bundle) {
		final CoreFragmentActivity activity = ((CoreFragmentActivity) getActivity());
		if (activity != null) {
			final BaseFlipBoardBottomSheetFragment fragment = (BaseFlipBoardBottomSheetFragment) activity
				.createFragment(fragmentClass, bundle);
			fragment.show(getChildFragmentManager(), R.id.bottomSheet);
		}
	}

	@Override
	public boolean onBackPressed(Runnable continueAction) {
		if (bottomSheetLayout.isSheetShowing()) {
			dismissBottomSheet();
			return false;
		} else {
			return super.onBackPressed(continueAction);
		}
	}

	@Override
	public void dismissBottomSheet() {
		bottomSheetLayout.dismissSheet();
	}

	//endregion

	//region Progress Indicator

	@BindView(R.id.basicsContainer)
	View basicsContainer;

	@BindView(R.id.basicsContainerPlaceholder)
	ShimmerLayout basicsContainerPlaceholder;

	@BindView(R.id.userAndLocationContainer)
	View userAndLocationContainer;

	@BindView(R.id.userAndLocationPlaceholder)
	ShimmerLayout userAndLocationPlaceholder;

	@Override
	public void showProgressIndicator() {
		showItemPreviewPlaceholder(true);
		showBasicsPlaceholder(true);
		showUserLocationPlaceholder(true);
	}

	@Override
	public void hideProgressIndicator() {
		showItemPreviewPlaceholder(false);
		showBasicsPlaceholder(false);
		showUserLocationPlaceholder(false);
	}

	@Override
	public void showBasicsPlaceholder(boolean flag) {
		// Start animation.
		if (flag) {
			basicsContainerPlaceholder.startShimmerAnimation();
		} else {
			basicsContainerPlaceholder.stopShimmerAnimation();
		}

		// Handle containers visibility.
		ViewUtil.changeVisibility(basicsContainer, !flag);
		ViewUtil.changeVisibility(basicsContainerPlaceholder, flag);
	}

	@Override
	public void showUserLocationPlaceholder(boolean flag) {
		// Start animation.
		if (flag) {
			userAndLocationPlaceholder.startShimmerAnimation();
		} else {
			userAndLocationPlaceholder.stopShimmerAnimation();
		}

		// Handle containers visibility.
		ViewUtil.changeVisibility(userAndLocationContainer, !flag);
		ViewUtil.changeVisibility(userAndLocationPlaceholder, flag);
	}

	@Override
	public void showItemPreviewPlaceholder(boolean flag) {
		// Start animation.
		if (flag) {
			headerHolder.itemImagePlaceholder.startShimmerAnimation();
		} else {
			headerHolder.itemImagePlaceholder.stopShimmerAnimation();
		}

		// Handle containers visibility.
		ViewUtil.changeVisibility(headerHolder.recyclerView, !flag);
		ViewUtil.changeVisibility(headerHolder.sliderDotsPanel, !flag);
		ViewUtil.changeVisibility(headerHolder.itemImagePlaceholder, flag);
	}

	//endregion

	//region Buttons

	@Override
	public void setActionButtonsEnabled(boolean flag) {
		likesIcon.setEnabled(flag);
		likesIndicator.setEnabled(flag);
		shareButton.setEnabled(flag);
		buyButton.setEnabled(flag);
		offerButton.setEnabled(flag);
		resellButton.setEnabled(flag);
	}

	@Override
	public void showResellButton(boolean flag) {
		ViewUtil.changeVisibility(resellButton, flag);
	}

	//endregion

	//region Snacks

	@Override
	public void showItemLikeNotFoundMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.item_like_not_found, Mode.ERROR)
			   .show();
	}

	//endregion

	//region Map

	private boolean mapReady = false;

	private GoogleMap map;

	private static final String MAP_FRAGMENT_TAG = "mapFragment";

	private Marker currentLocation = null;

	private static final float DEFAULT_MAP_ZOOM = 13.0f;

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		reconfigureMap(map);
		getPresenter().onMapReady();
		mapReady = true;
	}

	private void initializeMapView() {
		if (map == null) {
			mapReady = false;
			final FragmentManager manager = getChildFragmentManager();
			SupportMapFragment fragment = (SupportMapFragment) manager.findFragmentByTag(MAP_FRAGMENT_TAG);
			if (fragment == null) {
				fragment = SupportMapFragment.newInstance();
				final FragmentTransaction transaction = manager.beginTransaction();
				transaction.add(R.id.itemLocationMap, fragment, MAP_FRAGMENT_TAG);
				transaction.commit();
				manager.executePendingTransactions();
			}
			fragment.getMapAsync(this);
		}
	}

	private void reconfigureMap(@NonNull GoogleMap map) {
		final UiSettings settings = map.getUiSettings();
		settings.setAllGesturesEnabled(false);
		settings.setCompassEnabled(false);
		settings.setIndoorLevelPickerEnabled(false);
		settings.setMapToolbarEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		settings.setRotateGesturesEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setZoomControlsEnabled(false);
		settings.setZoomGesturesEnabled(false);
	}

	//endregion

	//region RecyclerView

	private FastItemAdapter<DetailsGalleryItem> galleryAdapter;

	private void initRecyclerView() {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
			getContext(), LinearLayoutManager.HORIZONTAL, false);
		headerHolder.recyclerView.setLayoutManager(linearLayoutManager);
		headerHolder.recyclerView.setHasFixedSize(true);
		galleryAdapter = new FastItemAdapter<>();
		galleryAdapter.setHasStableIds(false);
		headerHolder.recyclerView.setAdapter(galleryAdapter);
		headerHolder.recyclerView.setHasFixedSize(true);
		headerHolder.recyclerView.addOnPageChange(position -> {
			if (dots.length > 1) {
				for (int i = 0; i < dotsCount; i++) {
					if (getContext() != null) {
						dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nonactive_dot));
					}
				}

				if (getContext() != null) {
					dots[position]
						.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot_point));
				}
			}
		});
	}

	//endregion

	//region Dots

	private int         dotsCount;
	private ImageView[] dots;

	private void addDotsToView() {
		if (getContext() != null) {
			try {
				dotsCount = galleryAdapter.getAdapterItemCount();
				dots = new ImageView[dotsCount];

				headerHolder.sliderDotsPanel.removeAllViews();

				if (dots.length > 1) {
					for (int i = 0; i < dotsCount; i++) {
						dots[i] = new ImageView(getContext());
						dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nonactive_dot));

						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

						params.setMargins(8, 0, 8, 0);

						headerHolder.sliderDotsPanel.addView(dots[i], params);
					}
					dots[0].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_dot_point));
				}
			} catch (Exception ignore) {

			}
		}
	}

	//endregion

	//region Headed View

	public HeaderHolder headerHolder;

	@Override
	public View prepareHeaderView(LayoutInflater inflater) {
		if (headerHolder == null || headerHolder.getView() == null) {
			View view = LayoutInflater.from(getContext())
									  .inflate(R.layout.fragment_image_preview, null, false);
			headerHolder = new HeaderHolder(view);
		}
		return headerHolder.getView();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (headerHolder != null) {
			try {
				headerHolder.unbind();
			} catch (IllegalStateException e) {
			}
			headerHolder = null;
		}
	}

	//endregion

	//region Bindings

	@BindView(R.id.itemTitle)
	TextView itemTitle;

	@BindView(R.id.itemPrice)
	TextView itemPrice;

	@BindView(R.id.itemAbout)
	TextView itemAbout;

	@BindView(R.id.likesIcon)
	ImageView likesIcon;

	@OnClick(R.id.likesIcon)
	void onLikeButtonClicked(View view) {
		getPresenter().onLikeButtonClicked();
	}

	@BindView(R.id.likesIndicator)
	TextView likesIndicator;

	@BindView(R.id.viewsIndicator)
	TextView viewsIndicator;

	@BindView(R.id.shareButton)
	Button shareButton;

	@OnClick(R.id.shareButton)
	void onShareButtonClicked(View view) {
		getPresenter().onShareButtonClicked();
	}

	@BindView(R.id.userAvatar)
	ImageView userAvatar;

	@OnClick({R.id.userAvatar, R.id.userName})
	void onUsernameClicked(View view) {
		getPresenter().onUsernameClicked();
	}

	@BindView(R.id.userName)
	TextView userName;

	@BindView(R.id.userRating)
	RatingBar userRating;

	@BindView(R.id.itemLocationMap)
	FrameLayout itemLocationMap;

	@BindView(R.id.itemLocationName)
	TextView itemLocationName;

	@BindView(R.id.buyButton)
	Button buyButton;

	@OnClick(R.id.buyButton)
	void onBuyButtonClicked(View view) {
		getPresenter().onBuyButtonClicked();
	}

	@BindView(R.id.offerButton)
	Button offerButton;

	@OnClick(R.id.offerButton)
	void onOfferButtonClicked(View view) {
		getPresenter().onOfferButtonClicked();
	}

	@BindView(R.id.resellButton)
	Button resellButton;

	@OnClick(R.id.resellButton)
	void onResellButtonClicked() {
		getPresenter().onResellButtonClicked();
	}

	//endregion

	//region Class

	public class HeaderHolder extends ButterHolder {

		//region Bindings

		@BindView(R.id.itemImagePlaceholder)
		ShimmerLayout itemImagePlaceholder;

		@BindView(R.id.recyclerView)
		SnappyHorizontalRecyclerView recyclerView;

		@BindView(R.id.sliderDots)
		LinearLayout sliderDotsPanel;

		//endregion

		//region Constructor

		private HeaderHolder(View view) {
			super(view);
		}

		//endregion
	}

	//endregion
}
