package co.soma.app.view.filter;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static co.soma.app.view.home.HomePresenter.DEFAULT_DOUBLE_VALUE;
import static co.soma.app.view.home.HomePresenter.DEFAULT_INTEGER_VALUE;
import static com.google.android.gms.location.places.ui.PlaceAutocomplete.MODE_OVERLAY;

import android.content.Context;
import android.content.Intent;
import co.soma.app.app.SomaApp;
import co.soma.app.data.RepositoryManager;
import co.soma.app.util.log.SomaLogger;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocomplete.IntentBuilder;
import pl.itcraft.core.view.fragment.CoreFragmentPresenter;

class FiltersPresenter extends CoreFragmentPresenter<FiltersView, Void> {

	//region Defaults

	static final String DEFAULT_MIN_VALUE = "0";
	static final String DEFAULT_MAX_VALUE = "100";

	//endregion

	//region Constructor

	protected FiltersPresenter(Context context, FiltersView view) {
		super(context, view);
	}

	//endregion

	//region Lifecycle

	@Override
	public void onViewCreated() {
		super.onViewCreated();
		setStartLocationOnView();
		setDefaultSeekBarValues();
	}

	//endregion

	//region Navigation

	void navigateToHomeView() {
		getView().navigateToHomeView(
			getView().retrieveCategory(),
			getLatitude() != null
			? getLatitude()
			: RepositoryManager.get().getUserManager().getProfileCoordinates().getLatitude(),
			getLongitude() != null
			? getLongitude()
			: RepositoryManager.get().getUserManager().getProfileCoordinates().getLongitude(),
			getView().retrieveMinValue(),
			getView().retrieveMaxValue());
	}

	//endregion

	//region Filters

	void onApplyFiltersClicked() {
		getView().applyFilters();
	}

	void onResetFiltersClicked() {
		getView().navigateToHomeView(
			null, DEFAULT_DOUBLE_VALUE, DEFAULT_DOUBLE_VALUE, DEFAULT_INTEGER_VALUE, DEFAULT_INTEGER_VALUE);
	}

	void onClearFiltersClicked() {
		getView().resetCategories();
		getView().resetSeekBarValues();
		setDefaultSeekBarValues();
		setStartLocationOnView();
	}

	//endregion

	//region SeekBar

	private void setDefaultSeekBarValues() {
		getView().setMinAndMaxLocationValues(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	//endregion

	//region Location

	private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2312;

	private Double longitude = null;
	private Double latitude  = null;

	private void setStartLocationOnView() {
		getView().setLocationOnCard(RepositoryManager.get().getUserManager().getProfileLocationName());
	}

	void onLocationForwardClicked() {
		showPlaceAutoComplete();
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
				latitude = place.getLatLng().latitude;
				break;
			case PlaceAutocomplete.RESULT_ERROR:
				getView().showUnknownErrorMessage();
				break;
			case RESULT_CANCELED:
				longitude = null;
				latitude = null;
				getView().showLocation(getView().retrieveLocation());
				break;
		}
	}

	private Double getLatitude() {
		return latitude;
	}

	private Double getLongitude() {
		return longitude;
	}

	//endregion
}
