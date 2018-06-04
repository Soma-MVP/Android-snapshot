package co.soma.app.notifications;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import android.os.Bundle;
import android.support.annotation.NonNull;
import co.soma.app.data.notifications.NotificationKey;
import co.soma.app.data.notifications.NotificationType;
import co.soma.app.util.log.SomaLogger;
import co.soma.app.view.conversation.ConversationsFragment;
import co.soma.app.view.conversation.ConversationsTabs;
import co.soma.app.view.conversation.chat.ChatStartMode;
import co.soma.app.view.conversation.chat.contextual.ContextualChatFragment;
import co.soma.app.view.main.MainActivity;
import co.soma.app.view.notifications.NotificationsFragment;
import co.soma.app.view.profile.ProfileFragment;
import co.soma.app.view.profile.ProfileTabs;
import co.soma.app.view.splash.SplashActivity;
import pl.itcraft.core.app.CoreApp;
import pl.itcraft.core.utils.NavigationUtil;
import pl.itcraft.core.view.activity.ICoreActivity;

public class PushNotificationsBundleManager {

	//region Pushes

	public void handlePushNotificationUsingBundle(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		final String type = bundle.getString(NotificationKey.MESSAGE_TYPE, EMPTY);

		switch (type) {
			case NotificationType.MESSAGE:
			case NotificationType.MESSAGE_IMAGE:
				handleMessage(activity, bundle);
				break;
			case NotificationType.REQUEST_TO_BUY_ITEM:
				handleRequestToBuyItem(activity, bundle);
				break;
			case NotificationType.REQUEST_ACCEPTED:
			case NotificationType.REQUEST_DECLINED:
				handleRequestDecision(activity, bundle);
				break;
			case NotificationType.OFFER_CANCELLED:
				handleOfferCancelled(activity, bundle);
				break;
			case NotificationType.DEAL_CANCELLED:
				handleDealCancelled(activity, bundle);
				break;
			case NotificationType.POST_OFFER_ACCEPT:
				handlePostOfferAccept(activity, bundle);
				break;
			case NotificationType.RESELLING_REQUEST:
			case NotificationType.RESELLING_ACCEPTED:
			case NotificationType.RESELLING_DECLINED:
			case NotificationType.RESELLING_CANCELLED:
				handleReselling(activity, bundle);
				break;
			case NotificationType.PAYMENT_SUCCESSFUL:
			case NotificationType.PAYMENT_FAILED:
				handlePaymentResult(activity, bundle);
				break;
			case NotificationType.PAYMENT_CONFIRM:
				handlePaymentConfirm(activity, bundle);
				break;
			case NotificationType.PAYMENT_RECEIVED:
				handlePaymentReceived(activity, bundle);
				break;
			case NotificationType.RATE_USER:
				handleRateUser(activity, bundle);
				break;
			case NotificationType.CONNECTION_REQUEST:
				handleConnectionRequest(activity, bundle);
				break;
			case NotificationType.USER_FOLLOWING:
				handleUserFollowing(activity, bundle);
				break;
			default:
				// Cannot recognize notification type - proceed to main view.
				SomaLogger.e("Undefined notification type:" + type);
				openMainActivity();
				break;
		}
	}

	private void handleMessage(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// senderId = ???
		final String sellerId = bundle.getString(NotificationKey.OWNER_ID, EMPTY);
		final String buyerId = bundle.getString(NotificationKey.SENDER_ID, EMPTY);
		final String itemId = bundle.getString(NotificationKey.ITEM_ID, EMPTY);

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ContextualChatFragment.class,
			ContextualChatFragment.prepareForContextual(
				Long.valueOf(sellerId),
				Long.valueOf(buyerId),
				Long.valueOf(itemId),
				ChatStartMode.NORMAL
			)
		);
	}

	private void handleRequestToBuyItem(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ConversationsFragment.class,
			ConversationsFragment.generateBundleWithDefaultTab(ConversationsTabs.SELLING),
			true
		);
	}

	private void handleRequestDecision(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// senderId = buyerId
		final String sellerId = bundle.getString(NotificationKey.OWNER_ID, EMPTY);
		final String buyerId = bundle.getString(NotificationKey.SENDER_ID, EMPTY);
		final String itemId = bundle.getString(NotificationKey.ITEM_ID, EMPTY);

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ContextualChatFragment.class,
			ContextualChatFragment.prepareForContextual(
				Long.valueOf(sellerId),
				Long.valueOf(buyerId),
				Long.valueOf(itemId),
				ChatStartMode.NORMAL
			),
			true
		);
	}

	private void handleOfferCancelled(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// senderId = sellerId
		final String sellerId = bundle.getString(NotificationKey.SENDER_ID, EMPTY);
		final String buyerId = bundle.getString(NotificationKey.OWNER_ID, EMPTY);
		final String itemId = bundle.getString(NotificationKey.ITEM_ID, EMPTY);

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ContextualChatFragment.class,
			ContextualChatFragment.prepareForContextual(
				Long.valueOf(sellerId),
				Long.valueOf(buyerId),
				Long.valueOf(itemId),
				ChatStartMode.NORMAL
			),
			true
		);
	}

	private void handleDealCancelled(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// senderId = ???
		final String sellerId = "-1L";
		final String buyerId = "-1L";
		final String itemId = bundle.getString(NotificationKey.ITEM_ID, EMPTY);

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ContextualChatFragment.class,
			ContextualChatFragment.prepareForContextual(
				Long.valueOf(sellerId),
				Long.valueOf(buyerId),
				Long.valueOf(itemId),
				ChatStartMode.NORMAL
			),
			true
		);
	}

	private void handlePostOfferAccept(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// TODO:
	}

	private void handlePaymentReceived(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// senderId = buyerId
		final String sellerId = bundle.getString(NotificationKey.OWNER_ID, EMPTY);
		final String buyerId = bundle.getString(NotificationKey.SENDER_ID, EMPTY);
		final String itemId = bundle.getString(NotificationKey.ITEM_ID, EMPTY);

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ContextualChatFragment.class,
			ContextualChatFragment.prepareForContextual(
				Long.valueOf(sellerId),
				Long.valueOf(buyerId),
				Long.valueOf(itemId),
				ChatStartMode.NORMAL
			),
			true
		);
	}

	private void handleRateUser(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// TODO:
	}

	private void handleConnectionRequest(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ProfileFragment.class,
			ProfileFragment.generateBundleWithDefaultTab(ProfileTabs.CONNECTIONS),
			true
		);
	}

	private void handleUserFollowing(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			NotificationsFragment.class,
			NotificationsFragment.generateBundle(),
			true
		);
	}

	private void handleReselling(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// senderId = ???
		final String sellerId = bundle.getString(NotificationKey.OWNER_ID, EMPTY);
		final String buyerId = bundle.getString(NotificationKey.SENDER_ID, EMPTY);
		final String itemId = bundle.getString(NotificationKey.ITEM_ID, EMPTY);

		CoreApp.getNavigation().openFragmentInNearestFragmentActivity(
			MainActivity.class,
			ContextualChatFragment.class,
			ContextualChatFragment.prepareForContextual(
				Long.valueOf(sellerId),
				Long.valueOf(buyerId),
				Long.valueOf(itemId),
				ChatStartMode.NORMAL
			)
		);
	}

	private void handlePaymentResult(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// TODO:
	}

	private void handlePaymentConfirm(@NonNull ICoreActivity activity, @NonNull Bundle bundle) {
		if (activity instanceof SplashActivity) {
			openMainActivity();
		}

		// TODO:
	}

	private void openMainActivity() {
		CoreApp.getNavigation().openActivity(
			MainActivity.class,
			NavigationUtil.OPEN_AS_SINGLE,
			MainActivity.generateBundle()
		);
	}

	//endregion
}
