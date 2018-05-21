package co.soma.app.view.registration.form;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import co.soma.app.R;
import co.soma.app.app.SomaApp;
import co.soma.app.data.RepositoryManager;
import co.soma.app.data.exceptions.RepositoryError;
import co.soma.app.util.LocationUtil;
import co.soma.app.util.log.SomaLogger;
import co.soma.app.view.registration.callback.FacebookProfileDataCallback;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import java.util.List;
import java.util.Map;
import pl.itcraft.core.executor.Actions.ParameterAction;
import pl.itcraft.core.executor.Executor;
import pl.itcraft.core.view.fragment.CoreFragmentPresenter;
import pl.itcraft.invalidate.exceptions.ValidationErrorDetails;

class RegistrationFormPresenter extends CoreFragmentPresenter<RegistrationFormView, FacebookProfileDataCallback> {

	//region Constructor

	protected RegistrationFormPresenter(Context context, RegistrationFormView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onViewCreated() {
		super.onViewCreated();
		showProperLocation();
		prepareFacebookData();
	}

	//endregion

	//region Buttons

	void onNextButtonClicked() {
		final String email               = getView().retrieveEmail();
		final String password            = getView().retrievePassword();
		final String username            = getView().retrieveUsername();
		final String locationName        = getView().retrieveLocationName();
		final String facebookAccessToken = getCallback().getFacebookAccessToken();

		final Double lat = getLatitude();
		final Double lon = getLongitude();

		if (lat == null || lon == null) {
			getView().showProblemWithLocationMessage();
			return;
		}

		// Register via Facebook as requested.
		if (shouldRegisterViaFacebook()) {
			registerAccountViaFacebook(facebookAccessToken, username, lat, lon, locationName);
		}

		// Register via Credentials as requested.
		else {
			registerAccountViaCredentials(email, password, username, lat, lon, locationName);
		}
	}

	void onSignInButtonClicked() {
		getView().navigateToSingIn();
	}

	void onLocationFieldClicked() {
		showPlaceAutoComplete();
	}

	//endregion

	//region Credentials

	private void registerAccountViaCredentials(
		@NonNull String email, @NonNull String password, @NonNull String username, @NonNull Double latitude,
		@NonNull Double longitude, @NonNull String locationName) {
		Executor.getBuilder(RepositoryManager.get()
			                    .getUserManager()
			                    .register(email, password, username, latitude, longitude, locationName))
			.withSimpleResultListener((newResult, thisExecutor) -> getView().navigateToRegistrationSuccessView())
			.withErrorResultListener(repositoryError -> {
				if (!handleValidationError(repositoryError)) {
					if (repositoryError.getApiException() != null) {
						switch (repositoryError.getApiException().getApiErrorCode()) {
							case USERNAME_CONFLICT:
								getView().showUsernameConflictMessage();
								return true;
							case EMAIL_CONFLICT:
								getView().showEmailConflictMessage();
								return true;
							default:
								return true;
						}
					}
				} else {
					return true;
				}
				return false;
			})
			.bindProgressIndicator(this::getView)
			.bindPresenter(this)
			.execute();
	}

	//endregion

	//region Facebook

	private boolean shouldRegisterViaFacebook() {
		final String email    = getCallback().getEmail();
		final String username = getCallback().getUsername();
		return !isAnyBlank(email, username);
	}

	private void prepareFacebookData() {
		if (shouldRegisterViaFacebook()) {
			final String email    = getCallback().getEmail();
			final String username = getCallback().getUsername();
			getView().hidePasswordField();
			getView().setEmail(email);
			getView().setUsername(username);
		}
	}

	private void registerAccountViaFacebook(
		@Nullable String facebookAccessToken, @NonNull String username, @NonNull Double latitude,
		@NonNull Double longitude,
		@NonNull String locationName) {
		Executor.getBuilder(RepositoryManager.get()
			                    .getUserManager()
			                    .registerViaFacebook(facebookAccessToken, username, latitude, longitude, locationName))
			.withSimpleResultListener((newResult, thisExecutor) -> getView().navigateToRegistrationSuccessView())
			.withErrorResultListener(repositoryError -> {
				if (!handleValidationError(repositoryError)) {
					if (repositoryError.getApiException() != null) {
						switch (repositoryError.getApiException().getApiErrorCode()) {
							case USERNAME_CONFLICT:
								getView().showUsernameConflictMessage();
								return true;
							case EMAIL_CONFLICT:
								getView().showEmailConflictMessage();
								return true;
							default:
								return true;
						}
					}
				} else {
					return true;
				}
				return false;
			})
			.bindProgressIndicator(this::getView)
			.bindPresenter(this)
			.execute();
	}

	//endregion

	//region Validation

	private static final String EMAIL_KEY         = "email";
	private static final String PASSWORD_KEY      = "password";
	private static final String USERNAME_KEY      = "username";
	private static final String LOCATION_NAME_KEY = "locationName";

	private boolean handleValidationError(RepositoryError error) {
		if (error.isValidationError()) {
			Map<String, List<ValidationErrorDetails>> fieldsErrorMap = error.getValidationMap();

			validateEmail(fieldsErrorMap);
			validatePassword(fieldsErrorMap);
			validateUsername(fieldsErrorMap);
			validateLocationName(fieldsErrorMap);
		}
		return error.isValidationError();
	}

	private void validateEmail(Map<String, List<ValidationErrorDetails>> fieldsErrorMap) {
		if (!shouldRegisterViaFacebook()) {
			if (fieldsErrorMap.get(EMAIL_KEY) != null) {
				ValidationErrorDetails validationErrorDetails = fieldsErrorMap.get(EMAIL_KEY).get(0);
				getView().showEmailValidationMessage(validationErrorDetails);
			}
		}
	}

	private void validatePassword(Map<String, List<ValidationErrorDetails>> fieldsErrorMap) {
		if (!shouldRegisterViaFacebook()) {
			if (fieldsErrorMap.get(PASSWORD_KEY) != null) {
				ValidationErrorDetails validationErrorDetails = fieldsErrorMap.get(PASSWORD_KEY).get(0);
				getView().showPasswordValidationMessage(validationErrorDetails);
			}
		}
	}

	private void validateUsername(Map<String, List<ValidationErrorDetails>> fieldsErrorMap) {
		if (fieldsErrorMap.get(USERNAME_KEY) != null) {
			ValidationErrorDetails validationErrorDetails = fieldsErrorMap.get(USERNAME_KEY).get(0);
			getView().showUsernameValidationMessage(validationErrorDetails);
		}
	}

	private void validateLocationName(Map<String, List<ValidationErrorDetails>> fieldsErrorMap) {
		if (fieldsErrorMap.get(LOCATION_NAME_KEY) != null) {
			ValidationErrorDetails validationErrorDetails = fieldsErrorMap.get(LOCATION_NAME_KEY).get(0);
			getView().showLocationNameValidationMessage(validationErrorDetails);
		}
	}

	//endregion

	//region Location

	private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2312;

	private Double longitude = null;
	private Double latitude  = null;

	@SuppressLint("MissingPermission")
	private void findLocation(ParameterAction<Location> onFind) {
		LocationUtil.askAboutGPS(success -> {
			if (success) {
				SomaApp.getPermissionUtil()
					.askPermission(
						R.string.permissionDialogTitle, R.string.permission_gps,
						allTrue -> checkLocationPermissions(allTrue, onFind), Manifest.permission.ACCESS_FINE_LOCATION);
			}
		}, getContext(), getView().getParentActivity(), this);
	}

	private LocationRequest getLocationRequest() {
		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(1000);
		return locationRequest;
	}

	@SuppressLint("MissingPermission")
	private void checkLocationPermissions(boolean isAllPermissionsGranted, ParameterAction<Location> onFind) {
		if (isAllPermissionsGranted) {
			FusedLocationProviderClient fusedLocationProviderClient
				= LocationServices.getFusedLocationProviderClient(getContext());
			LocationCallback locationCallback = new LocationCallback() {
				@Override
				public void onLocationResult(LocationResult locationResult) {
					super.onLocationResult(locationResult);
					Location lastLocation = locationResult.getLastLocation();
					latitude = lastLocation.getLatitude();
					longitude = lastLocation.getLongitude();
					onFind.doIt(lastLocation);
					fusedLocationProviderClient.removeLocationUpdates(this);
				}

				@Override
				public void onLocationAvailability(LocationAvailability locationAvailability) {
					super.onLocationAvailability(locationAvailability);
				}
			};
			fusedLocationProviderClient.requestLocationUpdates(
				getLocationRequest(), locationCallback, getHandler().getLooper());
		}
	}

	private void showPlaceAutoComplete() {
		getView().getParentActivity().registerOnSpecificViewResultAction(
			PLACE_AUTOCOMPLETE_REQUEST_CODE,
			(requestCode, resultCode, data) -> checkRequestResultCode(resultCode, data));
		try {
			Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(
				getView().getParentActivity());
			SomaApp.getNavigation().openActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
		} catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
			SomaLogger.e(e.getMessage());
		}
	}

	private void checkRequestResultCode(int resultCode, Intent data) {
		switch (resultCode) {
			case RESULT_OK:
				Place place = PlaceAutocomplete.getPlace(
					getView().getParentActivity(), data);
				getView().showLocation(place.getAddress().toString());
				longitude = place.getLatLng().longitude;
				latitude = place.getLatLng().latitude;
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
		if (latitude == null && longitude == null) {
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
}
