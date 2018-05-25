package co.soma.app.view.conversation.chat.items.reselling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import co.soma.app.R;
import co.soma.app.data.models.chat.conversation.chat.MessageModel;
import co.soma.app.view.conversation.chat.items.BaseChatItem;
import co.soma.app.view.conversation.chat.items.BaseChatItemCallback;
import co.soma.app.view.conversation.chat.items.BaseChatItemHolder;
import co.soma.app.view.conversation.chat.items.reselling.ChatMessageResellingOfferAcceptedReceiveItem.ChatMessageResellingAcceptedReceiveViewHolder;

public class ChatMessageResellingOfferAcceptedReceiveItem
	extends BaseChatItem<BaseChatItemCallback, ChatMessageResellingAcceptedReceiveViewHolder> {

	//region Constructor

	public ChatMessageResellingOfferAcceptedReceiveItem(
		@NonNull MessageModel model,
		@Nullable BaseChatItemCallback callback) {
		super(model, callback);
	}

	//endregion

	//region UI

	@NonNull
	@Override
	public ChatMessageResellingAcceptedReceiveViewHolder getViewHolder(@NonNull View view) {
		return new ChatMessageResellingAcceptedReceiveViewHolder(view);
	}

	@Override
	public int getType() {
		return R.id.vh_item_chat_reselling_accepted_receive;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_chat_message_reselling_offer_accepted_receive;
	}

	//endregion

	//region View Holder

	protected static class ChatMessageResellingAcceptedReceiveViewHolder
		extends BaseChatItemHolder<MessageModel, BaseChatItemCallback> {

		//region Constructor

		ChatMessageResellingAcceptedReceiveViewHolder(View itemView) {
			super(itemView);
		}

		//endregion

		//region Bindings

		@BindView(R.id.message)
		TextView messageView;

		@Override
		protected void bindView(MessageModel model) {
			setMessage(model);
		}

		@Override
		protected void unbindView() {
			super.unbindView();
			messageView.setText(null);
		}

		//endregion

		//region Message

		private void setMessage(@NonNull MessageModel model) {
			final String seller = model.getSellerName();
			final String formatted = itemView.getContext()
											 .getString(R.string.message_reselling_accepted_receive_offer, seller);
			messageView.setText(formatted);
		}

		//endregion
	}

	//endregion
}
