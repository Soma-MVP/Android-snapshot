package co.soma.app.view.item.details.share;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_STREAM;
import static android.content.Intent.EXTRA_TEXT;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import co.soma.app.R;
import co.soma.app.util.currency.CurrencyUtils;
import pl.itcraft.core.app.CoreApp;

public class Shareable {

	//region Fields

	private final Context context;
	private final int     socialChannel;
	private final String  url;
	private final Uri     image;
	private final String  message;
	private final String  title;

	//endregion

	//region Constructor

	private Shareable(Builder builder) {
		this.context = builder.context;
		this.socialChannel = builder.socialChannel;
		this.message = builder.message;
		this.url = builder.url;
		this.image = builder.image;
		this.title = builder.title;
	}

	//endregion

	//region Share

	public void share(@NonNull String packageName) {
		Intent socialIntent = new Intent();
		socialIntent.setAction(ACTION_SEND);
		if (socialChannel != Builder.ANY) {
			socialIntent.setType("text/*");
			socialIntent.setPackage(packageName);
		}
		socialIntent.putExtra(Intent.EXTRA_SUBJECT, title);
		if (image != null) {
			socialIntent.putExtra(EXTRA_STREAM, image);
			socialIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			socialIntent.putExtra(EXTRA_TEXT, message + "\n" + url);
			socialIntent.setType("*/*");
		} else {
			socialIntent.setType("text/plain");
			socialIntent.putExtra(EXTRA_TEXT, message + "\n" + url);
		}
		socialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		CoreApp.getNavigation().openActivity(
			Intent.createChooser(socialIntent, context.getString(R.string.item_details_share_item_title)));
	}

	//endregion

	//region Packages & Market

	public static final String FACEBOOK_LITE_PACKAGE             = "com.facebook.lite";
	public static final String FACEBOOK_PACKAGE                  = "com.facebook.katana";
	public static final String TWITTER_PACKAGE                   = "com.twitter.android";
	public static final String GOOGLE_PLUS_PACKAGE               = "com.google.android.apps.plus";
	public static final String GMAIL_PACKAGE                     = "com.google.android.gm";
	public static final String MARKET_URI_WHEN_APP_INSTALLED     = "market://details?id=";
	public static final String MARKET_URI_WHEN_APP_NOT_INSTALLED = "https://play.google.com/store/apps/details?id=";

	//endregion

	//region Check Installation

	public static boolean isAppInstalled(Context context, String packageName) {
		try {
			context.getPackageManager().getApplicationInfo(packageName, 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

	//endregion

	//region Social Share

	public static String messageToShare(Context context, @NonNull String title, @NonNull String priceUnit,
		@NonNull Long priceValue, @NonNull Integer decimalFraction, @NonNull String description) {
		final String formattedPrice = CurrencyUtils.humanReadable(priceValue, priceUnit, decimalFraction);

		return context.getString(R.string.item_details_share_title)
			+ " "
			+ title
			+ "\n"
			+ context.getString(R.string.item_details_share_description)
			+ " "
			+ description
			+ "\n"
			+ context.getString(R.string.item_details_share_price)
			+ " "
			+ formattedPrice;
	}

	//endregion

	//region Builder

	public static class Builder {

		public static final int FACEBOOK_CHANNEL    = 1;
		public static final int TWITTER_CHANNEL     = 2;
		public static final int GOOGLE_PLUS_CHANNEL = 3;
		public static final int GMAIL_CHANNEL       = 4;
		public static final int ANY                 = 0;
		private Context context;
		private int    socialChannel = 0;
		private String url           = null;
		private Uri    image         = null;
		private String message       = null;
		private String title;

		public Builder(Context context) {
			this.context = context;
		}

		public Shareable.Builder socialChannel(int channel) {
			this.socialChannel = channel;
			return this;
		}

		public Shareable.Builder url(String url) {
			this.url = url;
			return this;
		}

		public Shareable.Builder image(Uri img) {
			this.image = img;
			return this;
		}

		public Shareable.Builder message(String message) {
			this.message = message;
			return this;
		}

		public Shareable.Builder title(String title) {
			this.title = title;
			return this;
		}

		public Shareable build() {
			return new Shareable(this);
		}
	}

	//endregion
}
