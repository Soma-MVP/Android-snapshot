package co.soma.app.view.item.form;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.net.Uri.fromFile;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.google.android.gms.location.LocationRequest.create;
import static com.google.android.gms.location.places.ui.PlaceAutocomplete.IntentBuilder;
import static com.google.android.gms.location.places.ui.PlaceAutocomplete.MODE_OVERLAY;
import static io.reactivex.internal.functions.Functions.identity;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import co.soma.app.app.SomaApp;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.api.exceptions.ApiErrorCode;
import co.soma.app.data.api.exceptions.ApiException;
import co.soma.app.data.exceptions.RepositoryError;
import co.soma.app.data.exceptions.RepositoryException;
import co.soma.app.data.exceptions.ValidationErrorDetails;
import co.soma.app.data.managers.item.ItemManager;
import co.soma.app.data.models.item.CategoryModel;
import co.soma.app.data.models.item.CurrencyModel;
import co.soma.app.data.models.photo.UploadPhotoModel;
import co.soma.app.util.LocationUtil;
import co.soma.app.util.log.SomaLogger;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import io.reactivex.Observable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.executor.Actions.ParameterAction;
import pl.itcraft.core.executor.Executor;
import pl.itcraft.core.executor.ExecutorPool;
import pl.itcraft.core.executor.IExecutor;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.utils.MediaUtil;
import pl.itcraft.core.utils.MediaUtil.PhotoModel.OutputType;
import pl.itcraft.core.view.fragment.CoreFragmentPresenter;

class AddItemFormPresenter extends CoreFragmentPresenter<AddItemFormView, Void> {

	//region Constructor

	protected AddItemFormPresenter(Context context, AddItemFormView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onViewCreated() {
		super.onViewCreated();

		showProperLocation();
		loadPriceUnits();
		loadCategories();

		mediaUtil = new MediaUtil(getContext(), this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		handleSinglePhotoSelection(requestCode, resultCode, data);
		handleMultiplePhotoSelection(requestCode, resultCode, data);
	}

	//endregion

	//region Threading

	private ExecutorPool pool = Executor
		.getPoolBuilder(ExecutorPool.WATERFALL_MODE)
		.bindToPresenterIfNotBinded(this)
		.start();

	//endregion

	//region Photo Commons

	private MediaUtil mediaUtil;

	//endregion

	//region Photo Placeholder

	static final int RC_PHOTO_3 = 5913;

	void onAddPhotoPlaceholderClicked() {
		photoClicked = PHOTO_PLACEHOLDER;
		getView().revealAddPhotoPlaceholderSheet();
	}

	@SuppressLint("MissingPermission")
	void onTakeMultiplePhotoMenuItemSelected() {
		getView().askForPhotoPermissions(granted -> {
			if (granted) {
				mediaUtil.takePhotoUri(uri -> {
					if (uri != null) {
						getView().showMultiplePhotos(singletonList(uri));
						uploadItemPhoto(uri);
					}
				});
			}
		});
	}

	void onChooseMultipleFromAlbumMenuItemSelected() {
		getView().askForGalleryPermissions(granted -> {
			if (granted) {
				getView().showMultiplePhotosImagePicker();
			}
		});
	}

	private void handleMultiplePhotoSelection(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == RC_PHOTO_3) {
			final List<Image> images = ImagePicker.getImages(data);
			final List<Uri> uris = Observable.just(images)
				.defaultIfEmpty(emptyList())
				.flatMapIterable(identity())
				.map(it -> fromFile(new File(it.getPath())))
				.toList()
				.blockingGet();
			getView().showMultiplePhotos(uris);

			clearPhotoIds();
			Observable
				.fromIterable(uris)
				.doOnNext(this::uploadItemPhoto)
				.toList()
				.blockingGet();
		}
	}

	//endregion

	//region Single Photo

	static final int RC_PHOTO_1 = 5911;

	private int photoClicked = 0;

	void onPhoto1Clicked() {
		photoClicked = PHOTO_1;
		getView().revealAddPhotoSheet();
	}

	void onPhoto2Clicked() {
		photoClicked = PHOTO_2;
		getView().revealAddPhotoSheet();
	}

	void onPhoto3Clicked() {
		photoClicked = PHOTO_3;
		getView().revealAddPhotoSheet();
	}

	void onExpandAddPhotoSheet() {
		final boolean remove = (photoClicked == PHOTO_1 && getView().isPhoto1Selected())
			|| (photoClicked == PHOTO_2 && getView().isPhoto2Selected())
			|| (photoClicked == PHOTO_3 && getView().isPhoto3Selected());
		getView().removeDeleteMenuItem(!remove);
	}

	@SuppressLint("MissingPermission")
	void onTakeSinglePhotoMenuItemSelected() {
		getView().askForPhotoPermissions(granted -> {
			if (granted) {
				mediaUtil.takePhotoUri(uri -> {
					if (uri != null) {

						if (photoClicked == PHOTO_1) {
							getView().showPhoto1(uri);
						}

						if (photoClicked == PHOTO_2) {
							getView().showPhoto2(uri);
						}

						if (photoClicked == PHOTO_3) {
							getView().showPhoto3(uri);
						}

						uploadItemPhoto(uri);
					}
				});
			}
		});
	}

	void onChooseSingleFromAlbumMenuItemSelected() {
		getView().askForGalleryPermissions(granted -> {
			if (granted) {
				getView().showSinglePhotoImagePicker();
			}
		});
	}

	void onDeleteAPhotoMenuItemSelected() {
		if (photoClicked == PHOTO_1) {
			getView().showPhoto1(null);
		}

		if (photoClicked == PHOTO_2) {
			getView().showPhoto2(null);
		}

		if (photoClicked == PHOTO_3) {
			getView().showPhoto3(null);
		}

		removePhotoId(photoClicked);

		getView().showAddPhotoPlaceholder(!isAnyPhotoAdded());
		getView().showAddedPhotosContainer(isAnyPhotoAdded());
	}

	private void handleSinglePhotoSelection(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == RC_PHOTO_1) {
			final Image image = ImagePicker.getFirstImageOrNull(data);
			final Uri   uri   = fromFile(new File(image.getPath()));

			if (photoClicked == PHOTO_1) {
				getView().showPhoto1(uri);
			}

			if (photoClicked == PHOTO_2) {
				getView().showPhoto2(uri);
			}

			if (photoClicked == PHOTO_3) {
				getView().showPhoto3(uri);
			}

			uploadItemPhoto(uri);
		}
	}

	//endregion

	//region Photo APIs

	private void uploadItemPhoto(@NonNull Uri uri) {
		final ItemManager manager = RepositoryManager.get().getItemManager();
		final Bitmap      bitmap  = mediaUtil.getImageFromUriSync(uri, OutputType.BITMAP).getBitmap();
		pool.addExecutor(
			Executor.getBuilder(manager.uploadItemPhoto(bitmap))
				.withSimpleResultListener(this::handleItemPhotoUploadSuccess)
				.withErrorResultListener(this::handleItemPhotoUploadError)
				.bindPresenter(this)
		);
	}

	private void handleItemPhotoUploadSuccess(UploadPhotoModel newResult, IExecutor executor) {
		final Long id = newResult.getObjectId();

		// Add to first available space.
		if (photoClicked == PHOTO_PLACEHOLDER) {
			setFirstAvailablePhotoId(id);
		}
		// Set id for specific photo.
		else {
			setPhotoId(photoClicked, id);
		}
	}

	private boolean handleItemPhotoUploadError(@NonNull RepositoryError error) {
		final ApiException api = error.getApiException();
		if (api != null) {
			SomaLogger.e(api.getApiErrorMessage());
		}

		final RepositoryException repository = error.getRepositoryException();
		if (repository != null) {
			SomaLogger.e(repository.getLocalizedMessage());
		}

		return true;
	}

	//endregion

	//region Photo IDs

	static final int IMAGES_LIMIT      = 3;
	static final int PHOTO_PLACEHOLDER = -1;
	static final int PHOTO_1           = 0;
	static final int PHOTO_2           = 1;
	static final int PHOTO_3           = 2;

	@SuppressLint("UseSparseArrays")
	private ArrayMap<Integer, Long> photoIds = new ArrayMap<>(IMAGES_LIMIT);

	private void clearPhotoIds() {
		photoIds.clear();
	}

	private void removePhotoId(int position) {
		if (photoIds.containsKey(position)) {
			photoIds.removeAt(position);
		}
	}

	private List<Long> getPhotoIds() {
		return new ArrayList<>(photoIds.values());
	}

	private void setPhotoId(int position, Long id) {
		photoIds.put(position, id);
	}

	private void setFirstAvailablePhotoId(Long id) {
		final Long first    = photoIds.get(PHOTO_1);
		final Long second   = photoIds.get(PHOTO_2);
		final Long third    = photoIds.get(PHOTO_3);
		final int  position = first == null ? PHOTO_1 : second == null ? PHOTO_2 : third == null ? PHOTO_3 : -1;
		setPhotoId(position, id);
	}

	private boolean isAnyPhotoAdded() {
		return getView().isPhoto1Selected() || getView().isPhoto2Selected() || getView().isPhoto3Selected();
	}

	//endregion

	//region Price Unit

	private List<CurrencyModel> priceUnits;

	private void loadPriceUnits() {
		pool.addExecutor(
			Executor.getBuilder(RepositoryManager.get().getItemManager().getCurrencies())
				.withSimpleResultListener(((newResult, thisExecutor) -> {
					priceUnits = newResult;
					getView().showPriceUnits(priceUnits);
				}))
				.withErrorResultListener(error -> true)
		);
	}

	//endregion

	//region Category

	private List<CategoryModel> categories;

	private void loadCategories() {
		pool.addExecutor(
			Executor.getBuilder(RepositoryManager.get().getItemManager().getCategories())
				.withSimpleResultListener(((newResult, thisExecutor) -> {
					categories = newResult;
					getView().showCategories(categories);
				}))
				.withErrorResultListener(error -> true)
		);
	}

	//endregion

	//region Location

	private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2312;

	private Double longitude = null;
	private Double latitude  = null;

	void onLocationFieldFocused() {
		showPlaceAutoComplete();
	}

	private void findLocation(ParameterAction<Location> onFind) {
		LocationUtil.askAboutGPS(success -> {
			if (success) {
				getView().askForLocationPermission(granted -> checkLocationPermissions(granted, onFind));
			}
		}, getContext(), getView().getParentActivity(), this);
	}

	private LocationRequest getLocationRequest() {
		final LocationRequest request = create();
		request.setPriority(PRIORITY_HIGH_ACCURACY);
		request.setInterval(TimeUnit.SECONDS.toMillis(1));
		return request;
	}

	@SuppressLint("MissingPermission")
	private void checkLocationPermissions(boolean granted, ParameterAction<Location> onFind) {
		if (granted) {
			final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getContext());
			final LocationCallback callback = new LocationCallback() {
				@Override
				public void onLocationResult(LocationResult locationResult) {
					super.onLocationResult(locationResult);
					final Location location = locationResult.getLastLocation();
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					onFind.doIt(location);
					client.removeLocationUpdates(this);
				}
			};
			client.requestLocationUpdates(getLocationRequest(), callback, getHandler().getLooper());
		}
	}

	private void showPlaceAutoComplete() {
		getView().getParentActivity().registerOnSpecificViewResultAction(
			PLACE_AUTOCOMPLETE_REQUEST_CODE,
			(requestCode, resultCode, data) -> checkRequestResultCode(resultCode, data)
		);
		try {
			final Intent intent = new IntentBuilder(MODE_OVERLAY).build(getView().getParentActivity());
			SomaApp.getNavigation().openActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
		} catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
			SomaLogger.e(e.getLocalizedMessage());
		}
	}

	private void checkRequestResultCode(int resultCode, Intent data) {
		switch (resultCode) {
			case RESULT_OK:
				final Place place = PlaceAutocomplete.getPlace(getView().getParentActivity(), data);
				getView().showLocation(place.getAddress().toString());
				longitude = place.getLatLng().longitude;
				latitude = place.getLatLng().longitude;
				break;
			case PlaceAutocomplete.RESULT_ERROR:
				getView().showUnknownErrorMessage();
				break;
			case RESULT_CANCELED:
				longitude = null;
				latitude = null;
				showProperLocation();
				break;
		}
	}

	private void showProperLocation() {
		if (latitude == null || longitude == null) {
			findLocation(location -> getView().showLocation(getAddress()));
		}
	}

	private Double getLatitude() {
		return latitude;
	}

	private Double getLongitude() {
		return longitude;
	}

	private String getAddress() {
		return LocationUtil.getAddressByCoordinates(getContext(), getLatitude(), getLongitude());
	}

	//endregion

	//region Promote SCT

	void onPromoteButtonClicked() {
		// TODO:
		CoreApp.getSnackManager()
			.prepareSnack("TODO: Handle promote button clicked.", Mode.WARNING)
			.show();
	}

	//endregion

	//region Draft

	void onSaveDraftButtonClicked() {
		final ItemManager manager = RepositoryManager.get().getItemManager();
		pool.addExecutor(
			Executor.getBuilder(
				manager.publishItem(
					getView().retrieveTitle(),
					getView().retrieveDescription(),
					getView().retrieveCategory(),
					getView().isResellAllowed(),
					getView().retrievePrice(),
					getView().retrievePriceUnit(),
					getLatitude(), getLongitude(),
					getView().retrieveLocation(),
					true,
					getPhotoIds()))
				.withSimpleResultListener(this::handleResultForPublishItem)
				.withErrorResultListener(this::handleRepositoryErrorForPublishItem)
				.bindProgressIndicator(getView().getSaveDraftProgressIndicator())
				.bindPresenter(this)
		);
	}

	//endregion

	//region Publish

	void onPublishButtonClicked() {
		final ItemManager manager = RepositoryManager.get().getItemManager();
		pool.addExecutor(
			Executor.getBuilder(
				manager.publishItem(
					getView().retrieveTitle(),
					getView().retrieveDescription(),
					getView().retrieveCategory(),
					getView().isResellAllowed(),
					getView().retrievePrice(),
					getView().retrievePriceUnit(),
					getLatitude(), getLongitude(),
					getView().retrieveLocation(),
					false,
					getPhotoIds()))
				.withSimpleResultListener(this::handleResultForPublishItem)
				.withErrorResultListener(this::handleRepositoryErrorForPublishItem)
				.bindProgressIndicator(getView().getAddItemProgressIndicator())
				.bindPresenter(this)
		);
	}

	private void handleResultForPublishItem(Object result, IExecutor executor) {
		getView().navigateToProfileView();
	}

	private boolean handleRepositoryErrorForPublishItem(RepositoryError error) {
		return handleValidationErrorsForPublishItem(error) || handleApiErrors(error);
	}

	//endregion

	//region Publish validation

	private boolean handleValidationErrorsForPublishItem(@NonNull RepositoryError error) {
		if (error.isValidationError()) {
			final Map<String, List<ValidationErrorDetails>> errors = error.getValidationMap();
			validateTitle(errors);
			validatePrice(errors);
			validateDescription(errors);
			validateLocation(errors);
		}
		return error.isValidationError();
	}

	//endregion

	//region Common validation

	private boolean handleApiErrors(@NonNull RepositoryError error) {
		final ApiException exception = error.getApiException();
		if (exception == null) {
			return false;
		}

		final ApiErrorCode code = exception.getApiErrorCode();
		switch (code) {
			case OFFLINE:
				getView().showOfflineMessage();
				break;
			case UNKNOWN:
				getView().showUnknownErrorMessage();
				break;
			default:
				break;
		}
		return true;
	}

	private static final String TITLE_KEY       = "title";
	private static final String DESCRIPTION_KEY = "description";
	private static final String PRICE_KEY       = "price";
	private static final String LOCATION_KEY    = "locationName";

	private void validateTitle(Map<String, List<ValidationErrorDetails>> errors) {
		if (errors.get(TITLE_KEY) != null) {
			final ValidationErrorDetails details = errors.get(TITLE_KEY).get(0);
			getView().showTitleValidationMessage(details);
		}
	}

	private void validateDescription(Map<String, List<ValidationErrorDetails>> errors) {
		if (errors.get(DESCRIPTION_KEY) != null) {
			final ValidationErrorDetails details = errors.get(DESCRIPTION_KEY).get(0);
			getView().showDescriptionValidationMessage(details);
		}
	}

	private void validatePrice(Map<String, List<ValidationErrorDetails>> errors) {
		if (errors.get(DESCRIPTION_KEY) != null) {
			final ValidationErrorDetails details = errors.get(PRICE_KEY).get(0);
			getView().showPriceValidationMessage(details);
		}
	}

	private void validateLocation(Map<String, List<ValidationErrorDetails>> errors) {
		if (errors.get(LOCATION_KEY) != null) {
			final ValidationErrorDetails details = errors.get(LOCATION_KEY).get(0);
			getView().showLocationValidationMessage(details);
		}
	}

	//endregion
}
