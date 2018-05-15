package pl.itcraft.soma.core.search;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import pl.itcraft.soma.core.Constants;
import pl.itcraft.soma.core.QueueUtils;
import pl.itcraft.soma.core.QueueUtils.ItemAction;
import pl.itcraft.soma.core.QueueUtils.UserAction;
import pl.itcraft.soma.core.model.entities.Following;
import pl.itcraft.soma.core.model.entities.Item;
import pl.itcraft.soma.core.model.entities.User;
import pl.itcraft.soma.core.model.enums.Category;
import pl.itcraft.soma.core.objectify.OfyUtils;

public class SearchDocumentService {

	private static final Logger logger = Logger.getLogger(SearchDocumentService.class.getName());

	public CollectionResponse<SearchEntityDocument> searchEntities(String keyword, Category category,
			Double latitude, Double longitude, Integer minRange, Integer maxRange, User user, Boolean followersItems, Boolean friendsItems, String nextPageToken, Integer limit) {

		if(limit == null || limit > Constants.ITEM_SEARCH_MAX_LIMIT) {
			limit = Constants.ITEM_SEARCH_MAX_LIMIT;
		}

		//Search by keyword
		StringBuilder queryBuilder = new StringBuilder();
		if (keyword != null && !keyword.isEmpty()) {
			queryBuilder = queryBuilder.append("searchableTextValue = ").append(keyword);
		}

		//Search by min and max distance
		StringBuilder distanceExpr = new StringBuilder();
		if (latitude != null && longitude != null && (maxRange != null || minRange != null)) {
			distanceExpr
				.append("distance(").append("location").append(", ")
				.append("  geopoint(").append(latitude).append(", ").append(longitude).append(")")
				.append(")");

			if(maxRange != null){
				queryBuilder = queryBuilder.append(queryBuilder.length() > 0 ? " AND " : "").append(distanceExpr).append(" < ").append(maxRange);
			}
			if(minRange != null){
				queryBuilder = queryBuilder.append(queryBuilder.length() > 0 ? " AND " : "").append(distanceExpr).append(" > ").append(minRange);
			}
		}

		//Search by category
		if(category != null) {
			queryBuilder = queryBuilder.append(queryBuilder.length() > 0 ? " AND " : "").append("category = ").append(category.name());
		}

		String itemsIndex = DocIndexFactory.ITEMS_GLOBAL_INDEX;
		if(user != null && followersItems != null && followersItems) {
			itemsIndex = DocIndexFactory.ITEMS_USER_INDEX + user.getId();
		}
		if(user != null && friendsItems != null && friendsItems) {
			queryBuilder = queryBuilder.append(queryBuilder.length() > 0 ? " AND " : "").append("isFriend = 1");
			itemsIndex = DocIndexFactory.ITEMS_USER_INDEX + user.getId();
		}



		QueryOptions.Builder qob = QueryOptions.newBuilder()
				.setCursor(nextPageToken != null && !nextPageToken.isEmpty() ?
						Cursor.newBuilder().build(nextPageToken) : Cursor.newBuilder().build())
				.setLimit(limit);

		logger.info("Executing query: " + queryBuilder.toString());

		Query query = Query.newBuilder().setOptions(qob.build()).build(queryBuilder.toString());

		Index index = DocIndexFactory.getIndex(itemsIndex);
		Results<ScoredDocument> results = index.search(query);

		logger.info("Found number of " + results.getNumberReturned() + " returned results.");

		CollectionResponse.Builder<SearchEntityDocument> builder = CollectionResponse.<SearchEntityDocument>builder()
				.setItems(SearchEntityDocument.fromResults(results))
				.setNextPageToken(results != null && results.getCursor() != null ? results.getCursor().toWebSafeString() : null);

		return builder.build();
	}

	public CollectionResponse<SearchEntityDocument> searchEntitiesForAutocomplete(String keyword, String nextPageToken, Integer limit) {

		if(limit == null || limit > Constants.ITEM_SEARCH_MAX_LIMIT) {
			limit = Constants.ITEM_SEARCH_MAX_LIMIT;
		}

		//Search by keyword
		StringBuilder queryBuilder = new StringBuilder();
		if (keyword != null && !keyword.isEmpty()) {
			queryBuilder = queryBuilder.append("searchableTextValue = ").append(keyword);
		}

		QueryOptions.Builder qob = QueryOptions.newBuilder()
				.setCursor(nextPageToken != null && !nextPageToken.isEmpty() ?
						Cursor.newBuilder().build(nextPageToken) : Cursor.newBuilder().build())
				.setLimit(limit);

		logger.info("Executing query: " + queryBuilder.toString());

		Query query = Query.newBuilder().setOptions(qob.build()).build(queryBuilder.toString());

		Index index = DocIndexFactory.getIndex(DocIndexFactory.ITEMS_GLOBAL_INDEX);
		Results<ScoredDocument> results = index.search(query);

		logger.info("Found number of " + results.getNumberReturned() + " returned results.");

		CollectionResponse.Builder<SearchEntityDocument> builder = CollectionResponse.<SearchEntityDocument>builder()
				.setItems(SearchEntityDocument.fromResults(results))
				.setNextPageToken(results != null && results.getCursor() != null ? results.getCursor().toWebSafeString() : null);

		return builder.build();
	}
	
	public void updateSearchEntityDocumentGlobal(Item item) {
		Index index = DocIndexFactory.getGlobalIndex();
		if (index.get(item.getId().toString()) != null) {
			index.delete(item.getId().toString());
		}

		SearchEntityDocument document = new SearchEntityDocument(item, null);
		index.put(document.toDocument());
	}

	public void deleteSearchEntityDocumentGlobal(Item item) {
		Index index = DocIndexFactory.getGlobalIndex();
		index.delete(item.getId().toString());
	}

	public void updateSearchEntityDocumentUser(Item item, Long userId, Boolean isFriend) {
		Index index = DocIndexFactory.getUserIndex(userId);
		if (index.get(item.getId().toString()) != null) {
			index.delete(item.getId().toString());
		}

		SearchEntityDocument document = new SearchEntityDocument(item, isFriend);
		index.put(document.toDocument());
	}

	public void deleteSearchEntityDocumentUser(Item item, Long userId) {
		Index index = DocIndexFactory.getUserIndex(userId);
		index.delete(item.getId().toString());
	}

	public void enqueueItemAction(Item item, ItemAction itemAction) {
		Queue queue = QueueFactory.getQueue(QueueUtils.ITEM_ACTION_QUEUE_NAME);
		queue.add(TaskOptions.Builder.withUrl(QueueUtils.ITEM_ACTION_QUEUE_URL)
				.param(QueueUtils.ITEM_ID_PARAMETER_NAME, item.getId().toString())
				.param(QueueUtils.ITEM_ACTION_PARAMETER_NAME, itemAction.toString())
				);
	}

	public void enqueueUserAction(Long followerId, Long followedId, UserAction userAction) {
		Queue queue = QueueFactory.getQueue(QueueUtils.USER_ACTION_QUEUE_NAME);
		queue.add(TaskOptions.Builder.withUrl(QueueUtils.USER_ACTION_QUEUE_URL)
				.param(QueueUtils.FOLLOWER_ID_PARAMETER_NAME, followerId.toString())
				.param(QueueUtils.FOLLOWED_ID_PARAMETER_NAME, followedId.toString())
				.param(QueueUtils.USER_ACTION_PARAMETER_NAME, userAction.toString())
				);
	}


	public void proceedCreateItem(Item item) {
		CollectionResponse<Following> followersList = getRawUserFollowers(item.getOwnerId(), Constants.SEARCH_ENGINE_ITEM_BATCH_SIZE, null);
		while(followersList != null && followersList.getItems() != null && !followersList.getItems().isEmpty() && followersList.getNextPageToken() != null) {
			for(Following following : followersList.getItems()) {
				updateSearchEntityDocumentUser(item, following.getFollowerId(), following.getIsFriend());
			}
			followersList = getRawUserFollowers(item.getOwnerId(),  Constants.SEARCH_ENGINE_ITEM_BATCH_SIZE, followersList.getNextPageToken());
		}
	}

	public void proceedDeleteItem(Item item) {
		CollectionResponse<Following> followersList = getRawUserFollowers(item.getOwnerId(), Constants.SEARCH_ENGINE_ITEM_BATCH_SIZE, null);
		while(followersList != null && followersList.getItems() != null && !followersList.getItems().isEmpty() && followersList.getNextPageToken() != null) {
			for(Following following : followersList.getItems()) {
				deleteSearchEntityDocumentUser(item, following.getFollowerId());
			}
			followersList = getRawUserFollowers(item.getOwnerId(),  Constants.SEARCH_ENGINE_ITEM_BATCH_SIZE, followersList.getNextPageToken());
		}
	}

	public void proceedFollowUser(Long followedId, Long followerId, Boolean isFriend) {
		CollectionResponse<Item> itemsList = getPageableUserItems(followedId, Constants.SEARCH_ENGINE_USER_BATCH_SIZE, null);
		while(itemsList != null && itemsList.getItems() != null && !itemsList.getItems().isEmpty() && itemsList.getNextPageToken() != null) {
			for(Item item : itemsList.getItems()) {
				updateSearchEntityDocumentUser(item, followerId, isFriend);
			}
			itemsList = getPageableUserItems(followedId,  Constants.SEARCH_ENGINE_USER_BATCH_SIZE, itemsList.getNextPageToken());
		}
	}

	public void proceedUnfollowUser(Long followedId, Long followerId) {
		CollectionResponse<Item> itemsList = getPageableUserItems(followedId, Constants.SEARCH_ENGINE_USER_BATCH_SIZE, null);
		while(itemsList != null && itemsList.getItems() != null && !itemsList.getItems().isEmpty() && itemsList.getNextPageToken() != null) {
			for(Item item : itemsList.getItems()) {
				deleteSearchEntityDocumentUser(item, followerId);
			}
			itemsList = getPageableUserItems(followedId,  Constants.SEARCH_ENGINE_USER_BATCH_SIZE, itemsList.getNextPageToken());
		}
	}

	public void proceedUnfriendUser(Long followedId, Long followerId) {
		CollectionResponse<Item> itemsList = getPageableUserItems(followedId, Constants.SEARCH_ENGINE_USER_BATCH_SIZE, null);
		while(itemsList != null && itemsList.getItems() != null && !itemsList.getItems().isEmpty() && itemsList.getNextPageToken() != null) {
			for(Item item : itemsList.getItems()) {
				updateSearchEntityDocumentUser(item, followerId, false);
			}
			itemsList = getPageableUserItems(followedId,  Constants.SEARCH_ENGINE_USER_BATCH_SIZE, itemsList.getNextPageToken());
		}
	}

	public CollectionResponse<Item> getPageableUserItems(Long userId, Integer limit, String nextPageToken) {
		Map<String, Object> queryParams = new HashMap<>();
		if (userId != null) {
			queryParams.put("ownerId", userId);
		}
		return OfyUtils.listPagedEntities(queryParams, null, limit, nextPageToken, Item.class);
	}

	public CollectionResponse<Following> getRawUserFollowers(Long userId, Integer limit, String nextPageToken) {
		Map<String, Object> queryParams = new HashMap<>();
		if (userId != null) {
			queryParams.put("followedId", userId);
		}
		return OfyUtils.listPagedEntities(queryParams, null, limit, nextPageToken, Following.class);
	}

}
