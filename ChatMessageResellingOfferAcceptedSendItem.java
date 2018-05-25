package co.soma.app.view.conversation.chat.items.reselling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import co.soma.app.R;
import co.soma.app.data.models.chat.conversation.chat.MessageModel;
import co.soma.app.view.conversation.chat.items.BaseChatItem;
import co.soma.app.view.conversation.chat.items.BaseChatItemCallback;
import co.soma.app.view.conversation.chat.items.BaseChatItemHolder;
import co.soma.app.view.conversation.chat.items.reselling.ChatMessageResellingOfferAcceptedSendItem.ChatMessageResellingAcceptedSendViewHolder;

public class ChatMessageResellingOfferAcceptedSendItem
	extends BaseChatItem<BaseChatItemCallback, ChatMessageResellingAcceptedSendViewHolder> {

	//region Constructor

	public ChatMessageResellingOfferAcceptedSendItem(
		@NonNull MessageModel model,
		@Nullable BaseChatItemCallback callback) {
		super(model, callback);
	}

	//endregion

	//region UI

	@NonNull
	@Override
	public ChatMessageResellingAcceptedSendViewHolder getViewHolder(@NonNull View view) {
		return new ChatMessageResellingAcceptedSendViewHolder(view);
	}

	@Override
	public int getType() {
		return R.id.vh_item_chat_reselling_accepted_send;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_chat_message_reselling_offer_accepted_send;
	}

	//endregion

	//region View Holder

	protected static class ChatMessageResellingAcceptedSendViewHolder
		extends BaseChatItemHolder<MessageModel, BaseChatItemCallback> {

		//region Constructor

		ChatMessageResellingAcceptedSendViewHolder(View itemView) {
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
