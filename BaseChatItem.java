package co.soma.app.view.conversation.chat.items;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import co.soma.app.data.models.chat.conversation.chat.MessageModel;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.items.ModelAbstractItem;
import com.mikepenz.fastadapter_extensions.swipe.ISwipeable;
import java.util.List;

public abstract class BaseChatItem<Callback, VH extends BaseChatItemHolder<MessageModel, Callback>>
	extends ModelAbstractItem<MessageModel, BaseChatItem<Callback, VH>, VH>
	implements ISwipeable<BaseChatItem, IItem> {

	//region Constructor

	public BaseChatItem(@NonNull MessageModel model) {
		this(model, null);
	}

	public BaseChatItem(@NonNull MessageModel model, @Nullable Callback callback) {
		super(model);
		this.callback = callback;
	}

	//endregion

	//region Callback

	private Callback callback;

	@Nullable
	public Callback getCallback() {
		return callback;
	}

	//endregion

	//region Swipeable

	protected boolean swipeable       = false;
	protected int     swipedDirection = 0;

	@Override
	public BaseChatItem withIsSwipeable(boolean swipeable) {
		this.swipeable = swipeable;
		return this;
	}

	@Override
	public boolean isSwipeable() {
		return swipeable;
	}

	public void setSwipedDirection(int swipedDirection) {
		this.swipedDirection = swipedDirection;
	}

	//endregion

	//region Identifier

	@Override
	public long getIdentifier() {
		return getModel().getLocalId();
	}

	//endregion

	//region Bindings

	@Override
	public void bindView(@NonNull VH holder, @NonNull List<Object> payloads) {
		super.bindView(holder, payloads);
		holder.bindChatItem(getModel(), getCallback());
	}

	@Override
	public void unbindView(@NonNull VH holder) {
		super.unbindView(holder);
		holder.unbindView();
	}

	//endregion
}
