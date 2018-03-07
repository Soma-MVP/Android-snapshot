package co.soma.app.itemmodel;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.soma.app.R;
import co.soma.app.data.models.people.PeopleModel;
import co.soma.app.itemmodel.PeopleItemModel.PeopleViewHolder;
import co.soma.app.util.image.GlideApp;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.fastadapter.FastAdapter.ViewHolder;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.util.List;

public class PeopleItemModel extends AbstractItem<PeopleItemModel, PeopleViewHolder> {

	//region Fields

	private PeopleModel peopleModel;

	//endregion

	//region Constructor

	public PeopleItemModel(PeopleModel peopleModel) {
		withIdentifier(peopleModel.getUserId());
		this.peopleModel = peopleModel;
	}

	//endregion

	//region Methods [public]

	@NonNull
	@Override
	public PeopleViewHolder getViewHolder(@NonNull View v) {
		return new PeopleViewHolder(v);
	}

	@Override
	public int getType() {
		return R.id.vh_item_people;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_people;
	}

	//endregion

	//region Model

	public PeopleModel getPeopleModel() {
		return peopleModel;
	}

	//endregion

	//region inner Classes

	public class PeopleViewHolder extends ViewHolder<PeopleItemModel> {

		//region Bindings

		@BindView(R.id.userPhoto)    ImageView userPhoto;
		@BindView(R.id.userUsername) TextView  userUsername;
		@BindView(R.id.userItems)    TextView  userItems;

		//endregion

		//region Constructor

		public PeopleViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		//endregion

		//region Bindings

		@Override
		public void bindView(@NonNull PeopleItemModel item, @NonNull List<Object> payloads) {

			final PeopleModel peopleModel = item.getPeopleModel();

			setPhoto(peopleModel.getProfilePhotoUrl());
			userUsername.setText(peopleModel.getUsername());
			userItems.setText(
				itemView.getContext().getString(R.string.profile_number_of_items, peopleModel.getNumberOfItems()));
		}

		@Override
		public void unbindView(@NonNull PeopleItemModel item) {
			userUsername.setText(null);
			userItems.setText(null);
		}

		//endregion

		//region Glide

		private void setPhoto(String photoUrl) {
			GlideApp.with(itemView.getContext())
				.load(photoUrl)
				.fitCenter()
				.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
				.apply(RequestOptions.circleCropTransform())
				.into(userPhoto);
		}

		//endregion

	}

	//endregion
}
