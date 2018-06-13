package co.soma.app.view.filter;

import static co.soma.app.data.defs.Category.ART;
import static co.soma.app.data.defs.Category.JEWELRY;
import static co.soma.app.data.defs.Category.MOTO;
import static co.soma.app.data.defs.Category.OTHER;
import static co.soma.app.data.defs.Category.TOYS;
import static co.soma.app.view.filter.FiltersPresenter.DEFAULT_MAX_VALUE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import co.soma.app.R;
import co.soma.app.util.animation.FragmentAnimation;
import co.soma.app.view.home.HomeFragment;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.view.dialogfragment.CoreDialogFragmentView;
import pl.itcraft.core.view.fragment.CoreFragment;

public class FiltersFragment extends CoreFragment<FiltersPresenter>
	implements FiltersView, SingleSelectToggleGroup.OnCheckedChangeListener, OnRangeSeekbarChangeListener {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_filters;
	}

	@Override
	public CharSequence getTitle() {
		return getText(R.string.filter_title);
	}

	@Override
	public void setLocationOnCard(String location) {
		locationField.setText(location);
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Lifecycle

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		FragmentAnimation.enterFromTop(this);
		toggleGroup.setOnCheckedChangeListener(this);
		locationRangeSeekBar.setOnRangeSeekbarChangeListener(this);
	}

	//endregion

	//region Options

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_clear, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_clear:
				showClearFiltersDialog();
				break;
		}
		return true;
	}

	//endregion

	//region Navigation

	@Override
	public void navigateToHomeView(
		@Nullable String categoryType, Double latitude, Double longitude, Integer minRange, Integer maxRange) {
		CoreApp.getNavigation()
			.openFragmentInNearestFragmentActivity(
				HomeFragment.class,
				HomeFragment.prepareBundleForFilters(categoryType, latitude, longitude, minRange, maxRange),
				false);
	}

	//endregion

	//region Dialog

	@Override
	public void showClearFiltersDialog() {
		CoreApp.getNavigation().prepareUniversalDialog(
			R.string.empty,
			R.string.filter_ask_clear_filters,
			R.string.commonYes,
			dialog -> getPresenter().onClearFiltersClicked())
			.withNegativeButton(R.string.commonNo, CoreDialogFragmentView::dismissDialog)
			.showAllowingStateLoss();
	}

	@Override
	public void showResetFiltersDialog() {
		CoreApp.getNavigation().prepareUniversalDialog(
			R.string.empty,
			R.string.filter_ask_reset_filters,
			R.string.commonYes,
			dialog -> getPresenter().onResetFiltersClicked())
			.withNegativeButton(R.string.commonNo, CoreDialogFragmentView::dismissDialog)
			.showAllowingStateLoss();
	}

	//endregion

	//region Errors

	@Override
	public void showUnknownErrorMessage() {
		CoreApp.getSnackManager()
			.prepareSnack(R.string.unknown_error, Mode.ERROR)
			.show();
	}

	//endregion

	//region Filters

	@Override
	public void applyFilters() {
		getPresenter().navigateToHomeView();
	}

	@Override
	public void resetSeekBarValues() {
		locationRangeSeekBar.apply();
	}

	@Override
	public void resetCategories() {
		artCategoryRadio.setChecked(false);
		motorcycleCategoryRadio.setChecked(false);
		jeweleryCategoryRadio.setChecked(false);
		toysCategoryRadio.setChecked(false);
		otherCategoryRadio.setChecked(false);
	}

	private String categoryType;

	@Override
	public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.artCategoryRadio:
				categoryType = ART;
				break;
			case R.id.motorcycleCategoryRadio:
				categoryType = MOTO;
				break;
			case R.id.jeweleryCategoryRadio:
				categoryType = JEWELRY;
				break;
			case R.id.toysCategoryRadio:
				categoryType = TOYS;
				break;
			case R.id.otherCategoryRadio:
				categoryType = OTHER;
				break;
			default:
				categoryType = null;
				break;
		}
	}

	@Override
	public String retrieveCategory() {
		return categoryType;
	}

	@Override
	public Integer retrieveMinValue() {
		return minValue;
	}

	@Override
	public Integer retrieveMaxValue() {
		return maxValue;
	}

	//endregion

	//region Location

	@Override
	public void showLocation(String location) {
		locationField.setText(location);
	}

	@NonNull
	@Override
	public String retrieveLocation() {
		return defaultString(locationField.getText().toString(), EMPTY);
	}

	//endregion

	//region SeekBar

	private Integer minValue = 0;
	private Integer maxValue = 100;

	@Override
	public void valueChanged(Number minValue, Number maxValue) {
		setMinAndMaxLocationValues(String.valueOf(minValue), String.valueOf(maxValue));

		this.minValue = minValue.intValue();
		this.maxValue = maxValue.intValue();
	}

	@Override
	public void setMinAndMaxLocationValues(String minValue, String maxValue) {
		locationRangeFromValue.setText(String.format(getString(R.string.filter_km), minValue));
		locationRangeToValue.setText(
			String.format(
				getString(R.string.filter_km),
				String.valueOf(maxValue).equals(DEFAULT_MAX_VALUE)
				? getString(R.string.filter_km_over_one_hundred)
				: maxValue));
	}

	//endregion

	//region Bindings

	@BindView(R.id.locationRangeSeekBar)
	CrystalRangeSeekbar locationRangeSeekBar;

	@BindView(R.id.locationField)
	TextView locationField;

	@BindView(R.id.locationRangeFrom)
	EditText locationRangeFromValue;

	@BindView(R.id.locationRangeTo)
	EditText locationRangeToValue;

	@BindView(R.id.toggleGroup)
	SingleSelectToggleGroup toggleGroup;

	@BindView(R.id.artCategoryRadio)
	RadioButton artCategoryRadio;

	@BindView(R.id.motorcycleCategoryRadio)
	RadioButton motorcycleCategoryRadio;

	@BindView(R.id.jeweleryCategoryRadio)
	RadioButton jeweleryCategoryRadio;

	@BindView(R.id.toysCategoryRadio)
	RadioButton toysCategoryRadio;

	@BindView(R.id.otherCategoryRadio)
	RadioButton otherCategoryRadio;

	@OnClick(R.id.locationForward)
	void onLocationForwardClicked() {
		getPresenter().onLocationForwardClicked();
	}

	@OnClick(R.id.applyFilters)
	void onApplyFiltersClicked() {
		getPresenter().onApplyFiltersClicked();
	}

	@OnClick(R.id.resetFilters)
	void onResetFiltersClicked() {
		showResetFiltersDialog();
	}

	//endregion
}
