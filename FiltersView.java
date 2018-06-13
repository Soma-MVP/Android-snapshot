package co.soma.app.view.filter;

import android.support.annotation.Nullable;
import pl.itcraft.core.view.fragment.CoreFragmentView;

interface FiltersView extends CoreFragmentView {

	//region Card

	void setLocationOnCard(String location);

	//endregion

	//region Dialog

	void showResetFiltersDialog();

	void showClearFiltersDialog();

	//endregion

	//region Navigation

	void navigateToHomeView(
		@Nullable String categoryType, @Nullable Double latitude, @Nullable Double longitude,
		@Nullable Integer minRange, @Nullable Integer maxRange);

	//endregion

	//region Filters

	void applyFilters();

	void setMinAndMaxLocationValues(String minValue, String maxValue);

	void resetCategories();

	void resetSeekBarValues();

	String retrieveCategory();

	Integer retrieveMinValue();

	Integer retrieveMaxValue();

	//endregion

	//region Location

	void showLocation(String location);

	String retrieveLocation();

	//endregion

	//region Errors

	void showUnknownErrorMessage();

	//endregion
}
