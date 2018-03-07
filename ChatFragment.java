package co.soma.app.view.chat;

import android.os.Bundle;
import co.soma.app.R;
import pl.itcraft.core.view.fragment.CoreFragment;

public class ChatFragment extends CoreFragment<ChatPresenter> implements ChatView {

	//region UI

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_chat;
	}

	@Override
	public CharSequence getTitle() {
		return getText(R.string.chat_title);
	}

	//endregion

	//region Bundle

	public static Bundle generateBundle() {
		return new Bundle();
	}

	//endregion
}
