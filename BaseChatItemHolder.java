package co.soma.app.view.conversation.chat.items;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;

public abstract class BaseChatItemHolder<Item, Callback> extends RecyclerView.ViewHolder {

	//region Constructor

	public BaseChatItemHolder(View itemView) {
		super(itemView);
		ButterKnife.bind(this, itemView);
	}

	//endregion

	//region Bindings

	@Deprecated
	public final void bindChatItem(Item item, Callback callback) {
		this.item = item;
		this.callback = callback;
		bindView(item);
	}

	protected abstract void bindView(Item item);

	protected void unbindView() {
		// No-op.
	}

	//endregion

	//region Item

	private Item item;

	public Item getItem() {
		return item;
	}

	//endregion

	//region Callback

	private Callback callback;

	public Callback getCallback() {
		return callback;
	}

	//endregion

	//region View

	protected final View getView() {
		return itemView;
	}

	//endregion

	//region Context

	protected final Context getContext() {
		return itemView.getContext();
	}

	//endregion
}
