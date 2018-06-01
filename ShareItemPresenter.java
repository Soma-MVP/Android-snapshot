package co.soma.app.view.item.details.share;

import android.content.Context;
import co.soma.app.view.common.flipboard.BaseFlipBoardBottomSheetPresenter;

class ShareItemPresenter extends BaseFlipBoardBottomSheetPresenter<ShareItemView, ShareItemCallback> {

	//region Constructor

	protected ShareItemPresenter(Context context, ShareItemView view) {
		super(context, view);
	}

	//endregion

	//region Share

	void shareItemOnFacebook() {
		if (hasCallback()) {
			getCallback().shareItemOnFacebook();
		}
	}

	void shareItemOnTwitter() {
		if (hasCallback()) {
			getCallback().shareItemOnTwitter();
		}
	}

	void shareItemOnGooglePlus() {
		if (hasCallback()) {
			getCallback().shareItemOnGooglePlus();
		}
	}

	void shareItemOnGmail() {
		if (hasCallback()) {
			getCallback().shareItemOnGmail();
		}
	}

	//endregion
}
