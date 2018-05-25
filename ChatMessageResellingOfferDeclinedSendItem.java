package co.soma.app.view.conversation.chat.items.reselling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import co.soma.app.R;
import co.soma.app.data.models.chat.conversation.chat.MessageModel;
import co.soma.app.view.conversation.chat.items.BaseChatItem;
import co.soma.app.view.conversation.chat.items.BaseChatItemCallback;
import co.soma.app.view.conversation.chat.items.BaseChatItemHolder;
import co.soma.app.view.conversation.chat.items.reselling.ChatMessageResellingOfferDeclinedSendItem.ChatMessageResellingDeclinedSendViewHolder;

public class ChatMessageResellingOfferDeclinedSendItem
	extends BaseChatItem<BaseChatItemCallback, ChatMessageResellingDeclinedSendViewHolder> {

	//region Constructor

	public ChatMessageResellingOfferDeclinedSendItem(
		@NonNull MessageModel model,
		@Nullable BaseChatItemCallback callback) {
		super(model, callback);
	}

	//endregion

	//region UI

	@NonNull
	@Override
	public ChatMessageResellingDeclinedSendViewHolder getViewHolder(@NonNull View view) {
		return new ChatMessageResellingDeclinedSendViewHolder(view);
	}

	@Override
	public int getType() {
		return R.id.vh_item_chat_reselling_declined_send;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_chat_message_reselling_offer_declined_send;
	}

	//endregion

	//region View Holder

	protected static class ChatMessageResellingDeclinedSendViewHolder
		extends BaseChatItemHolder<MessageModel, BaseChatItemCallback> {

		//region Constructor

		ChatMessageResellingDeclinedSendViewHolder(View itemView) {
			super(itemView);
		}

		//endregion

		//region Bindings

		@Override
		protected void bindView(MessageModel model) {
			// No-op.
		}

		//endregion
	}

	//endregion
}
