package co.soma.app.view.item.form;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import co.soma.app.data.exceptions.ValidationErrorDetails;
import co.soma.app.data.models.item.CategoryModel;
import co.soma.app.data.models.item.CurrencyModel;
import java.util.List;
import pl.itcraft.core.executor.Actions.ParameterAction;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.fragment.CoreFragmentView;

interface AddItemFormView extends CoreFragmentView {

	//region Navigation

	void navigateToProfileView();

	//endregion

	//region Form

	@NonNull
	String retrieveTitle();

	@NonNull
	Long retrievePrice();

	@NonNull
	String retrieveDescription();

	@NonNull
	String retrieveLocation();

	boolean isResellAllowed();

	//endregion

	//region Photo Commons

	void askForPhotoPermissions(ParameterAction<Boolean> onDeclared);

	void askForGalleryPermissions(ParameterAction<Boolean> onDeclared);

	//endregion

	//region Photo Placeholder

	void showAddPhotoPlaceholder(boolean show);

	void revealAddPhotoPlaceholderSheet();

	void showMultiplePhotosImagePicker();

	void showMultiplePhotos(@NonNull List<Uri> uris);

	//endregion

	//region Single Photo

	void showAddedPhotosContainer(boolean show);

	void revealAddPhotoSheet();

	void removeDeleteMenuItem(boolean remove);

	void showSinglePhotoImagePicker();

	void showPhoto1(@Nullable Uri uri);

	boolean isPhoto1Selected();

	void showPhoto2(@Nullable Uri uri);

	boolean isPhoto2Selected();

	void showPhoto3(@Nullable Uri uri);

	boolean isPhoto3Selected();

	//endregion

	//region Price Units

	void showPriceUnits(List<CurrencyModel> units);

	String retrievePriceUnit();

	//endregion

	//region Categories

	void showCategories(List<CategoryModel> categories);

	String retrieveCategory();

	//endregion

	//region Location

	void showLocation(String location);

	void askForLocationPermission(ParameterAction<Boolean> onResult);

	//endregion

	//region Progress indicators

	ProgressIndicator getAddItemProgressIndicator();

	ProgressIndicator getSaveDraftProgressIndicator();

	//endregion

	//region Snacks

	void showUnknownErrorMessage();

	void showProblemWithLocationMessage();

	//endregion

	//region Errors

	void showTitleValidationMessage(ValidationErrorDetails details);

	void showDescriptionValidationMessage(ValidationErrorDetails details);

	void showPriceValidationMessage(ValidationErrorDetails details);

	void showLocationValidationMessage(ValidationErrorDetails details);

	void showOfflineMessage();

	//endregion
}
