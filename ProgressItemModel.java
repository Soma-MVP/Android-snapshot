package co.soma.app.itemmodel;

import android.support.annotation.NonNull;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.soma.app.R;
import co.soma.app.itemmodel.ProgressItemModel.ProgressItemViewHolder;
import com.mikepenz.fastadapter.FastAdapter.ViewHolder;
import com.mikepenz.fastadapter.items.AbstractItem;
import java.util.List;
import pl.itcraft.core.executor.Actions.Action;

public class ProgressItemModel extends AbstractItem<ProgressItemModel, ProgressItemViewHolder> {

	//region Fields

	private final Action actionOnBind;

	//endregion

	//region Constructor

	public ProgressItemModel(Action actionOnBind) {
		this.actionOnBind = actionOnBind;
	}

	//endregion

	//region Getters and Setters

	@NonNull
	@Override
	public ProgressItemViewHolder getViewHolder(@NonNull View v) {
		return new ProgressItemViewHolder(v);
	}

	@Override
	public int getType() {
		return R.id.vh_progress;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.item_progress;
	}

	//endregion

	//region inner Classes

	public class ProgressItemViewHolder extends ViewHolder<ProgressItemModel> {

		//region Fields [binding]

		@BindView(R.id.progressIndicator) View progressIndicator;

		//endregion

		//region Constructor

		public ProgressItemViewHolder(View itemView) {
			super(itemView);
			ButterKnife.bind(this, itemView);
		}

		//endregion

		//region Bindings

		@Override
		public void bindView(@NonNull ProgressItemModel item, @NonNull List<Object> payloads) {
			if (item.actionOnBind != null) {
				item.actionOnBind.doIt();
			}
		}

		@Override
		public void unbindView(@NonNull ProgressItemModel item) {
		}

		//endregion

	}

	//endregion
}
