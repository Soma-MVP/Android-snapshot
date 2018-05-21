package co.soma.app.view.registration.form;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import co.soma.app.R;
import co.soma.app.util.view.ViewUtil;
import co.soma.app.view.login.LoginActivity;
import co.soma.app.view.registration.success.RegistrationSuccessFragment;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.managers.snack.SnackBuilder.Mode;
import pl.itcraft.core.utils.Cancelable;
import pl.itcraft.core.utils.NavigationUtil;
import pl.itcraft.core.view.fragment.CoreFragment;
import pl.itcraft.invalidate.exceptions.ValidationError;
import pl.itcraft.invalidate.exceptions.ValidationErrorDetails;

public class RegistrationFormFragment extends CoreFragment<RegistrationFormPresenter> implements RegistrationFormView {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_registration_form;
	}

	@Override
	public CharSequence getTitle() {
		return getText(R.string.registration_form_title);
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Navigation

	@Override
	public void navigateToSingIn() {
		CoreApp.getNavigation().openActivity(
			LoginActivity.class,
			NavigationUtil.OPEN_AS_SINGLE
		);
	}

	@Override
	public void navigateToRegistrationSuccessView() {
		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			RegistrationSuccessFragment.class,
			RegistrationSuccessFragment.generateBundle(),
			false
		);
	}

	//endregion

	//region Form bindings

	@BindView(R.id.emailField)    EditText emailField;
	@BindView(R.id.fullNameField) EditText fullNameField;
	@BindView(R.id.passwordLabel) TextView passwordLabel;
	@BindView(R.id.passwordField) EditText passwordField;
	@BindView(R.id.locationField) EditText locationField;

	@OnClick(R.id.nextButton)
	void onNextClicked() {
		getPresenter().onNextButtonClicked();
	}

	@OnClick(R.id.signInLink)
	void onSignInLinkClicked() {
		getPresenter().onSignInButtonClicked();
	}

	@OnClick(R.id.locationField)
	void onLocationClicked() {
		getPresenter().onLocationFieldClicked();
	}

	@OnFocusChange(R.id.locationField)
	public void reactionOnFocusLocation(boolean focus) {
		if (focus) {
			getPresenter().onLocationFieldClicked();
		}
	}

	//endregion

	//region Form

	@Override
	public void hidePasswordField() {
		passwordField.setVisibility(View.GONE);
		passwordLabel.setVisibility(View.GONE);
	}

	@Override
	public void showLocation(String location) {
		locationField.setText(location == null
							  ? getString(R.string.edit_profile_select_location)
							  : location);
		ViewUtil.moveCursorToEnd(locationField);
	}

	@Override
	public void showEmailValidationMessage(ValidationErrorDetails validationErrorDetails) {
		if (validationErrorDetails != null) {
			prepareWrongEmailValidationError(validationErrorDetails.getValidationError());
		} else {
			emailField.setError(getString(R.string.email_invalid));
		}
	}

	@Override
	public void showPasswordValidationMessage(ValidationErrorDetails validationErrorDetails) {
		if (validationErrorDetails != null) {
			prepareWrongPasswordValidationError(validationErrorDetails.getValidationError());
		} else {
			passwordField.setError(getString(R.string.password_invalid));
		}
	}

	@Override
	public void showUsernameValidationMessage(ValidationErrorDetails validationErrorDetails) {
		if (validationErrorDetails != null) {
			prepareWrongUsernameValidationError(validationErrorDetails.getValidationError());
		} else {
			fullNameField.setText(getString(R.string.username_required));
		}
	}

	@Override
	public void showLocationNameValidationMessage(ValidationErrorDetails validationErrorDetails) {
		if (validationErrorDetails != null) {
			prepareWrongLocationNameValidationError(validationErrorDetails.getValidationError());
		} else {
			locationField.setText(getString(R.string.location_required));
		}
	}

	@NonNull
	@Override
	public String retrieveEmail() {
		return defaultString(emailField.getText().toString(), EMPTY);
	}

	@NonNull
	@Override
	public String retrievePassword() {
		return defaultString(passwordField.getText().toString(), EMPTY);
	}

	@NonNull
	@Override
	public String retrieveUsername() {
		return defaultString(fullNameField.getText().toString(), EMPTY);
	}

	@NonNull
	@Override
	public String retrieveLocationName() {
		return defaultString(locationField.getText().toString(), EMPTY);
	}

	@Override
	public void setEmail(String email) {
		emailField.setEnabled(false);
		emailField.setText(email);
	}

	@Override
	public void setUsername(String username) {
		fullNameField.setText(username);
	}

	//endregion

	//region Snacks

	@Override
	public void showProblemWithLocationMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.problem_reading_coordinates, Mode.ERROR)
			   .show();
	}

	@Override
	public void showEmailConflictMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.email_conflict, Mode.ERROR)
			   .show();
	}

	@Override
	public void showUsernameConflictMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.username_conflict, Mode.ERROR)
			   .show();
	}

	@Override
	public void showUnknownErrorMessage() {
		CoreApp.getSnackManager()
			   .prepareSnack(R.string.unknown_error, Mode.ERROR)
			   .show();
	}

	//endregion

	//region Validation

	private void prepareWrongEmailValidationError(ValidationError validationError) {
		switch (validationError) {
			case REQUIRED:
				emailField.setError(getString(R.string.email_required));
				break;
			case INVALID_EMAIL:
				emailField.setError(getString(R.string.email_invalid));
				break;
			default:
				emailField.setError(getString(R.string.email_invalid));
				break;
		}
	}

	private void prepareWrongPasswordValidationError(ValidationError validationError) {
		switch (validationError) {
			case REQUIRED:
				passwordField.setError(getString(R.string.password_required));
				break;
			default:
				passwordField.setError(getString(R.string.password_invalid));
				break;
		}
	}

	private void prepareWrongUsernameValidationError(ValidationError validationError) {
		switch (validationError) {
			case REQUIRED:
				fullNameField.setError(getString(R.string.username_required));
				break;
			case INVALID_USERNAME:
				fullNameField.setError(getString(R.string.username_invalid));
				break;
			default:
				fullNameField.setError(getString(R.string.username_required));
				break;
		}
	}

	private void prepareWrongLocationNameValidationError(ValidationError validationError) {
		switch (validationError) {
			case REQUIRED:
				locationField.setError(getString(R.string.location_required));
				break;
			default:
				locationField.setError(getString(R.string.location_required));
				break;
		}
	}

	//endregion

	//region Progress indicator

	private Cancelable progressDialog = null;

	@Override
	public void showProgressIndicator() {
		progressDialog = CoreApp.getNavigation()
								.showProgressDialog(R.string.registration_progress_information, false, null);
	}

	@Override
	public void hideProgressIndicator() {
		if (progressDialog != null) {
			progressDialog.cancel();
		}
	}

	//endregion
}
