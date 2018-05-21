package co.soma.app.view.registration.form;

import android.support.annotation.NonNull;
import pl.itcraft.core.view.ProgressIndicator;
import pl.itcraft.core.view.fragment.CoreFragmentView;
import pl.itcraft.invalidate.exceptions.ValidationErrorDetails;

interface RegistrationFormView extends CoreFragmentView, ProgressIndicator {

	//region Navigation

	void navigateToSingIn();

	void navigateToRegistrationSuccessView();

	//endregion

	//region Snacks

	void showEmailValidationMessage(ValidationErrorDetails details);

	void showPasswordValidationMessage(ValidationErrorDetails details);

	void showUsernameValidationMessage(ValidationErrorDetails details);

	void showLocationNameValidationMessage(ValidationErrorDetails details);

	void showProblemWithLocationMessage();

	void showEmailConflictMessage();

	void showUsernameConflictMessage();

	void showUnknownErrorMessage();

	//endregion

	//region Form

	void setEmail(String email);

	void setUsername(String username);

	void hidePasswordField();

	@NonNull
	String retrieveEmail();

	@NonNull
	String retrievePassword();

	@NonNull
	String retrieveUsername();

	//endregion

	//region Location

	void showLocation(String location);

	@NonNull
	String retrieveLocationName();

	//endregion
}
