package co.soma.app.view.item.details.share;

import android.os.Bundle;
import butterknife.OnClick;
import co.soma.app.R;
import co.soma.app.view.common.flipboard.BaseFlipBoardBottomSheetFragment;

public class ShareItemFragment extends BaseFlipBoardBottomSheetFragment<ShareItemPresenter> implements ShareItemView {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_share_item;
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion

	//region Share

	@OnClick(R.id.fbShare)
	void onFacebookShareClicked() {
		getPresenter().shareItemOnFacebook();
	}

	@OnClick(R.id.twitterShare)
	void onTwitterShareClicked() {
		getPresenter().shareItemOnTwitter();
	}

	@OnClick(R.id.googlePlusShare)
	void onGooglePlusShareClicked() {
		getPresenter().shareItemOnGooglePlus();
	}

	@OnClick(R.id.gmailShare)
	void onGmailShareClicked() {
		getPresenter().shareItemOnGmail();
	}

	//endregion
}
