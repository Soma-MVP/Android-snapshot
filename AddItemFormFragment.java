package co.soma.app.view.item.form;

import static co.soma.app.view.item.form.AddItemFormPresenter.IMAGES_LIMIT;
import static co.soma.app.view.item.form.AddItemFormPresenter.RC_PHOTO_1;
import static co.soma.app.view.item.form.AddItemFormPresenter.RC_PHOTO_3;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.math.NumberUtils.createLong;

import android.Manifest;
import android.Manifest.permission;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import co.soma.app.R;
import co.soma.app.app.SomaApp;
import co.soma.app.data.exceptions.ValidationError;
import co.soma.app.data.exceptions.ValidationErrorDetails;
import co.soma.app.data.models.item.CategoryModel;
import co.soma.app.data.models.item.CurrencyModel;
import co.soma.app.util.animation.FragmentAnimation;
import co.soma.app.util.image.GlideApp;
import co.soma.app.util.view.ViewUtil;
import co.soma.app.view.profile.ProfileFragment;
import com.cocosw.bottomsheet.BottomSheet;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import java.util.List;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.executor.Actions.ParameterAction;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.utils.Cancelable;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.fragment.CoreFragment;

public class AddItemFormFragment extends CoreFragment<AddItemFormPresenter> implements AddItemFormView {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_add_item_form;
	}

	@Override
	public CharSequence getTitle() {
		return getString(R.string.add_item_title);
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Lifecycle

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		FragmentAnimation.enterFromTop(this);
	}

	//endregion

	//region Navigation

	@Override
	public void navigateToProfileView() {
		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			ProfileFragment.class,
			ProfileFragment.generateBundle(),
			false
		);
	}

	//endregion

	//region Photo Commons

	@Override
	public void askForPhotoPermissions(ParameterAction<Boolean> onDeclared) {
		CoreApp.getPermissionUtil().askPermission(
			R.string.permissionDialogTitle,
			R.string.permissionChooseImage,
			onDeclared,
			Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE
		);
	}

	@Override
	public void askForGalleryPermissions(ParameterAction<Boolean> onDeclared) {
		CoreApp.getPermissionUtil().askPermission(
			R.string.permissionDialogTitle,
			R.string.permissionChooseImage,
			onDeclared,
			Manifest.permission.READ_EXTERNAL_STORAGE
		);
	}

	//endregion

	//region Photo Placeholder

	@OnClick(R.id.addPhotoPlaceholder)
	void onAddPhotoPlaceholderClicked() {
		getPresenter().onAddPhotoPlaceholderClicked();
	}

	@Override
	public void revealAddPhotoPlaceholderSheet() {
		new BottomSheet.Builder(getParentActivity()).sheet(R.menu.pick_photo).listener(
			(dialogInterface, which) -> {
				switch (which) {
					case R.id.takeAPhoto:
						getPresenter().onTakeMultiplePhotoMenuItemSelected();
						break;
					case R.id.chooseFromAlbum:
						getPresenter().onChooseMultipleFromAlbumMenuItemSelected();
						break;
					case R.id.cancel:
						break;
				}
			}).show();
	}

	@Override
	public void showMultiplePhotosImagePicker() {
		ImagePicker.create(this)
			.returnMode(ReturnMode.NONE)
			.folderMode(true)
			.toolbarFolderTitle(getString(R.string.app_name))
			.toolbarImageTitle(getString(R.string.tap_to_select))
			.limit(IMAGES_LIMIT)
			.enableLog(false)
			.start(RC_PHOTO_3);
	}

	@Override
	public void showMultiplePhotos(@NonNull List<Uri> uris) {

		// Show / reveal photos containers.
		showAddPhotoPlaceholder(uris.isEmpty());
		showAddedPhotosContainer(!uris.isEmpty());

		// Reset current images.
		photo1ImageView.setImageResource(R.drawable.ic_add_item_photo_placeholder);
		photo2ImageView.setImageResource(R.drawable.ic_add_item_photo_placeholder);
		photo3ImageView.setImageResource(R.drawable.ic_add_item_photo_placeholder);

		// Show first image.
		if (uris.size() >= 1) {
			final Uri uri = uris.get(0);
			GlideApp.with(this).load(uri).centerCrop().into(photo1ImageView);
		}

		// Show second image.
		if (uris.size() >= 2) {
			final Uri uri = uris.get(1);
			GlideApp.with(this).load(uri).centerCrop().into(photo2ImageView);
		}

		// Show third image.
		if (uris.size() >= 3) {
			final Uri uri = uris.get(2);
			GlideApp.with(this).load(uri).centerCrop().into(photo3ImageView);
		}
	}

	//endregion

	//region Single Photo

	@BindView(R.id.photo1)
	ImageView photo1ImageView;

	@OnClick(R.id.photo1)
	void onPhoto1Clicked() {
		getPresenter().onPhoto1Clicked();
	}

	@Override
	public boolean isPhoto1Selected() {
		return photo1ImageView.getDrawable() != null && !(photo1ImageView.getDrawable() instanceof VectorDrawable);
	}

	@BindView(R.id.photo2)
	ImageView photo2ImageView;

	@OnClick(R.id.photo2)
	void onPhoto2Clicked() {
		getPresenter().onPhoto2Clicked();
	}

	@Override
	public boolean isPhoto2Selected() {
		return photo2ImageView.getDrawable() != null && !(photo2ImageView.getDrawable() instanceof VectorDrawable);
	}

	@BindView(R.id.photo3)
	ImageView photo3ImageView;

	@OnClick(R.id.photo3)
	void onPhoto3Clicked() {
		getPresenter().onPhoto3Clicked();
	}

	@Override
	public boolean isPhoto3Selected() {
		return photo3ImageView.getDrawable() != null && !(photo3ImageView.getDrawable() instanceof VectorDrawable);
	}

	private BottomSheet singleBottomSheet;

	@Override
	public void revealAddPhotoSheet() {
		singleBottomSheet = new BottomSheet.Builder(getParentActivity()).sheet(R.menu.pick_photo_extended).listener(
			(dialogInterface, which) -> {
				switch (which) {
					case R.id.takeAPhoto:
						getPresenter().onTakeSinglePhotoMenuItemSelected();
						break;
					case R.id.chooseFromAlbum:
						getPresenter().onChooseSingleFromAlbumMenuItemSelected();
						break;
					case R.id.deleteAPhoto:
						getPresenter().onDeleteAPhotoMenuItemSelected();
						break;
					case R.id.cancel:
						break;
				}
			}
		).build();

		// Attach item removal method on show.
		singleBottomSheet.setOnShowListener(it -> getPresenter().onExpandAddPhotoSheet());

		// Display dialog.
		singleBottomSheet.show();
	}

	@Override
	public void removeDeleteMenuItem(boolean remove) {
		if (remove) {
			singleBottomSheet.getMenu().removeGroup(R.id.deleteGroup);
			singleBottomSheet.invalidate();
		}
	}

	@Override
	public void showSinglePhotoImagePicker() {
		ImagePicker.create(this)
			.returnMode(ReturnMode.ALL)
			.folderMode(true)
			.toolbarFolderTitle(getString(R.string.app_name))
			.toolbarImageTitle(getString(R.string.tap_to_select))
			.single()
			.enableLog(false)
			.start(RC_PHOTO_1);
	}

	@Override
	public void showPhoto1(@Nullable Uri uri) {

		// Show default placeholder if uri not delivered.
		if (uri == null) {
			photo1ImageView.setImageResource(R.drawable.ic_add_item_photo_placeholder);
		}

		// Show image if loaded.
		else {
			GlideApp.with(this).load(uri).centerCrop().into(photo1ImageView);
		}
	}

	@Override
	public void showPhoto2(@Nullable Uri uri) {

		// Show default placeholder if uri not delivered.
		if (uri == null) {
			photo2ImageView.setImageResource(R.drawable.ic_add_item_photo_placeholder);
		}

		// Show image if loaded.
		else {
			GlideApp.with(this).load(uri).centerCrop().into(photo2ImageView);
		}
	}

	@Override
	public void showPhoto3(@Nullable Uri uri) {

		// Show default placeholder if uri not delivered.
		if (uri == null) {
			photo3ImageView.setImageResource(R.drawable.ic_add_item_photo_placeholder);
		}

		// Show image if loaded.
		else {
			GlideApp.with(this).load(uri).centerCrop().into(photo3ImageView);
		}
	}

	//endregion

	//region Photo

	@BindView(R.id.addPhotoPlaceholder)
	View addPhotoPlaceholderView;

	@Override
	public void showAddPhotoPlaceholder(boolean show) {
		addPhotoPlaceholderView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@BindView(R.id.addedPhotosContainer)
	View addedPhotosContainerView;

	@Override
	public void showAddedPhotosContainer(boolean show) {
		addedPhotosContainerView.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	//endregion

	//region Title

	@BindView(R.id.addTitleField)
	EditText addTitleField;

	@NonNull
	@Override
	public String retrieveTitle() {
		return defaultString(addTitleField.getText().toString(), EMPTY);
	}

	//endregion

	//region Price

	@BindView(R.id.addPriceField)
	EditText addPriceField;

	@NonNull
	@Override
	public Long retrievePrice() {
		final String cleaned = defaultIfBlank(addPriceField.getText().toString(), "0");
		return createLong(cleaned);
	}

	//endregion

	//region Price Unit

	@BindView(R.id.addPriceUnitField)
	Spinner addPriceUnitField;

	private ArrayAdapter<CurrencyModel> unitsAdapter;

	@Override
	public void showPriceUnits(List<CurrencyModel> units) {
		unitsAdapter = new ArrayAdapter<>(getParentActivity(), android.R.layout.simple_spinner_dropdown_item, units);
		addPriceUnitField.setAdapter(unitsAdapter);
	}

	@Override
	public String retrievePriceUnit() {
		final CurrencyModel model = unitsAdapter.getItem(addPriceUnitField.getSelectedItemPosition());
		return model != null ? model.getKey() : EMPTY;
	}

	//endregion

	//region Category

	@BindView(R.id.categoryField)
	Spinner categoryField;

	private ArrayAdapter<CategoryModel> categoriesAdapter;

	@Override
	public void showCategories(List<CategoryModel> categories) {
		categoriesAdapter = new ArrayAdapter<>(
			getParentActivity(), android.R.layout.simple_spinner_dropdown_item, categories);
		categoryField.setAdapter(categoriesAdapter);
	}

	@Override
	public String retrieveCategory() {
		final CategoryModel model = categoriesAdapter.getItem(categoryField.getSelectedItemPosition());
		return model != null ? model.getKey() : EMPTY;
	}

	//endregion

	//region Description

	@BindView(R.id.addDescriptionField)
	EditText addDescriptionField;

	@NonNull
	@Override
	public String retrieveDescription() {
		return defaultString(addDescriptionField.getText().toString(), EMPTY);
	}

	//endregion

	//region Location

	@BindView(R.id.addLocationField)
	EditText addLocationField;

	@OnClick(R.id.addLocationField)
	void onAddLocationFieldClicked() {
		getPresenter().onLocationFieldFocused();
	}

	@NonNull
	@Override
	public String retrieveLocation() {
		return defaultString(addLocationField.getText().toString(), EMPTY);
	}

	@OnFocusChange(R.id.addLocationField)
	void onAddLocationFieldFocusChanged(boolean focus) {
		if (focus) {
			getPresenter().onLocationFieldFocused();
		}
	}

	@Override
	public void showLocation(String location) {
		addLocationField.setText(location);
		ViewUtil.moveCursorToEnd(addLocationField);
	}

	@Override
	public void askForLocationPermission(ParameterAction<Boolean> onResult) {
		SomaApp.getPermissionUtil().askPermission(
			R.string.permissionDialogTitle, R.string.permission_gps, onResult, permission.ACCESS_FINE_LOCATION);
	}

	//endregion

	//region Promote SCT

	@OnClick(R.id.promoteSctButton)
	void onPromoteSctButtonClicked() {
		getPresenter().onPromoteButtonClicked();
	}

	//endregion

	//region Allow resell

	@BindView(R.id.allowResellSwitch)
	Switch allowResellSwitch;

	@Override
	public boolean isResellAllowed() {
		return allowResellSwitch.isChecked();
	}

	//endregion

	//region Draft & Draft

	@OnClick(R.id.saveDraftButton)
	void onSaveDraftButtonClicked() {
		getPresenter().onSaveDraftButtonClicked();
	}

	@OnClick(R.id.publishButton)
	void onPublishButtonClicked() {
		getPresenter().onPublishButtonClicked();
	}

	//endregion

	//region Add item progress indicator

	private Cancelable addItemProgressDialog = null;

	private ProgressIndicator addItemProgressIndicator = new ProgressIndicator() {
		@Override
		public void showProgressIndicator() {
			addItemProgressDialog = CoreApp.getNavigation()
				.showProgressDialog(R.string.add_item_form_progress_publishing, false, null);
		}

		@Override
		public void hideProgressIndicator() {
			if (allNotNull(addItemProgressDialog)) {
				addItemProgressDialog.cancel();
			}
		}
	};

	@Override
	public ProgressIndicator getAddItemProgressIndicator() {
		return addItemProgressIndicator;
	}

	//endregion

	//region Save draft progress indicator

	private Cancelable saveDraftProgressDialog = null;

	private ProgressIndicator saveDraftProgressIndicator = new ProgressIndicator() {
		@Override
		public void showProgressIndicator() {
			saveDraftProgressDialog = CoreApp.getNavigation()
				.showProgressDialog(R.string.add_item_form_progress_saving_draft, false, null);
		}

		@Override
		public void hideProgressIndicator() {
			if (allNotNull(saveDraftProgressDialog)) {
				saveDraftProgressDialog.cancel();
			}
		}
	};

	@Override
	public ProgressIndicator getSaveDraftProgressIndicator() {
		return saveDraftProgressIndicator;
	}

	//endregion

	//region Snacks

	@Override
	public void showUnknownErrorMessage() {
		CoreApp.getSnackManager()
			.prepareSnack(R.string.unknown_error, Mode.ERROR)
			.show();
	}

	@Override
	public void showProblemWithLocationMessage() {
		CoreApp.getSnackManager()
			.prepareSnack(R.string.problem_reading_coordinates, Mode.ERROR)
			.show();
	}

	//endregion

	//region Errors

	@Override
	public void showOfflineMessage() {
		CoreApp.getSnackManager().
			prepareSnack(R.string.offline, Mode.ERROR).
			show();
	}

	@Override
	public void showTitleValidationMessage(ValidationErrorDetails details) {
		if (details != null) {
			prepareTitleValidationError(details.getValidationError());
		} else {
			addTitleField.setError(getString(R.string.title_required));
		}
	}

	private void prepareTitleValidationError(ValidationError error) {
		switch (error) {
			case TOO_SHORT:
			case TOO_LONG:
				addTitleField.setError(getString(R.string.title_invalid));
				break;
			case REQUIRED:
			default:
				addTitleField.setError(getString(R.string.title_required));
				break;
		}
	}

	@Override
	public void showDescriptionValidationMessage(ValidationErrorDetails details) {
		if (details != null) {
			prepareDescriptionValidationError(details.getValidationError());
		} else {
			addDescriptionField.setError(getString(R.string.description_required));
		}
	}

	private void prepareDescriptionValidationError(ValidationError error) {
		switch (error) {
			case REQUIRED:
			default:
				addDescriptionField.setError(getString(R.string.description_required));
				break;
		}
	}

	@Override
	public void showPriceValidationMessage(ValidationErrorDetails details) {
		if (details != null) {
			prepareWrongPriceValidationError(details.getValidationError());
		} else {
			addPriceField.setError(getString(R.string.price_required));
		}
	}

	private void prepareWrongPriceValidationError(ValidationError error) {
		switch (error) {
			case NUMBER_GT_ZERO:
				addPriceField.setError(getString(R.string.price_invalid));
				break;
			default:
				addPriceField.setError(getString(R.string.price_required));
				break;
		}
	}

	@Override
	public void showLocationValidationMessage(ValidationErrorDetails details) {
		if (details != null) {
			prepareWrongLocationValidationError(details.getValidationError());
		} else {
			addLocationField.setError(getString(R.string.location_required));
		}
	}

	private void prepareWrongLocationValidationError(ValidationError error) {
		switch (error) {
			case TOO_SHORT:
			case TOO_LONG:
				addTitleField.setError(getString(R.string.location_invalid));
				break;
			case REQUIRED:
			default:
				addTitleField.setError(getString(R.string.location_required));
				break;
		}
	}

	//endregion
}
