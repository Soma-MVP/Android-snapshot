package pl.itcraft.soma.core.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Document.Builder;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;

import pl.itcraft.soma.core.Constants;
import pl.itcraft.soma.core.model.entities.Item;
import pl.itcraft.soma.core.model.enums.Category;

public class SearchEntityDocument {

	private static final Logger logger = Logger.getLogger(SearchEntityDocument.class.getCanonicalName());

	private String id;
	private String searchableTextValue;
	private Category category;
	private GeoPoint location;
	private Integer isFriend;
	private Set<String> tmpPrefixes;

	private Date creationDate;


	public SearchEntityDocument(Item item, Boolean isFriend) {

		this.id = item.getId().toString();
		this.location = new GeoPoint(item.getLatitude(), item.getLongitude());
		this.creationDate = item.getCreationDate();
		this.isFriend = isFriend == null ? 0 : isFriend ? 1 : 0;
		this.category = item.getCategory();

		appendPrefixes(item.getTitle());

		buildSearchableTextValue();
	}

	public SearchEntityDocument(Document document) {

		this.id = document.getId();
		this.creationDate = document.getOnlyField("creationDate").getDate();
		this.isFriend = document.getOnlyField("isFriend").getText().equals("1") ? 1 : 0;
		this.category = Category.valueOf(document.getOnlyField("category").getText());

		if (document.getFieldNames().contains("searchableTextValue")) {
			this.searchableTextValue = document.getOnlyField("searchableTextValue").getText() ;
		}

		if (document.getFieldNames().contains("location")) {
			this.location = document.getOnlyField("location").getGeoPoint();
		}
	}

	private void appendPrefixes(String value) {
		if (value != null && !value.isEmpty()) {
			if (tmpPrefixes == null) {
				tmpPrefixes = new HashSet<String>();
			}
			tmpPrefixes.addAll(generatePrefixes(value, Constants.SEARCH_ENGINE_TEXT_PREFIX_MIN_SIZE));
		}
	}

	private void buildSearchableTextValue() {
		if (tmpPrefixes != null) {
			StringBuilder sb = new StringBuilder();
			for (String prefix : tmpPrefixes) {
				sb.append(prefix).append(" ");
			}
			searchableTextValue = sb.toString();
		}
	}

	protected Set<String> generatePrefixes(String text, int minPrefixLength) {
		Set<String> prefixes = new HashSet<String>();
		String[] tokens = text.toLowerCase().split("\\s+");
		for (String token : tokens) {
			if (token.length() <= minPrefixLength) {
				prefixes.add(token);
			} else {
				for (int j = minPrefixLength; j <= token.length(); j++) {
					prefixes.add(token.substring(0, j));
				}
			}
		}
		return prefixes;
	}

	public Document toDocument() {
//		logger.info("Creating document with id = " + getId());

		Builder builder = Document.newBuilder()
				.setId(id)
				.setRank((int) (creationDate.getTime()/1000));

		if (searchableTextValue != null) {
			builder = builder
					.addField(Field.newBuilder().setName("searchableTextValue").setText(searchableTextValue)); //prefixes? TODO?
		}
		if (creationDate != null) {
			builder = builder
					.addField(Field.newBuilder().setName("creationDate").setDate(creationDate));
		}
		if (isFriend != null) {
			builder = builder
					.addField(Field.newBuilder().setName("isFriend").setText(isFriend.toString()));
		}
		if (location != null) {
			builder = builder.addField(Field.newBuilder().setName("location").setGeoPoint(getLocation()));
		}
		if (category != null) {
			builder = builder.addField(Field.newBuilder().setName("category").setText(category.toString()));
		}

		return builder.build();
	}

	public static List<SearchEntityDocument> fromResults(Results<ScoredDocument> results) {
		List<SearchEntityDocument> items = new ArrayList<SearchEntityDocument>(results.getNumberReturned());
		for (ScoredDocument doc : results.getResults()) {
			items.add(new SearchEntityDocument(doc));
		}
		return items;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public String getSearchableTextValue() {
		return searchableTextValue;
	}

	public void setSearchableTextValue(String searchableTextValue) {
		this.searchableTextValue = searchableTextValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getTmpPrefixes() {
		return tmpPrefixes;
	}

	public void setTmpPrefixes(Set<String> tmpPrefixes) {
		this.tmpPrefixes = tmpPrefixes;
	}


	public Integer getIsFriend() {
		return isFriend;
	}


	public void setIsFriend(Integer isFriend) {
		this.isFriend = isFriend;
	}


	public Date getCreationDate() {
		return creationDate;
	}


	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

}
