/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.facebook.api.impl;

import static org.springframework.social.facebook.api.impl.PagedListUtils.getPagedListParameters;
import static org.springframework.social.facebook.api.impl.PagedListUtils.getPagingParameters;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.social.UncategorizedApiException;
import org.springframework.social.facebook.api.FacebookLink;
import org.springframework.social.facebook.api.FeedOperations;
import org.springframework.social.facebook.api.GraphApi;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.PagingParameters;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.Post.PostType;
import org.springframework.social.facebook.api.PostData;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class FeedTemplate implements FeedOperations {

	private static final PagingParameters FIRST_PAGE = new PagingParameters(25, null, null, null);

	private final GraphApi graphApi;

	private final ObjectMapper objectMapper;

	private final RestTemplate restTemplate;

	public FeedTemplate(final GraphApi graphApi, final RestTemplate restTemplate, final ObjectMapper objectMapper) {
		this.graphApi = graphApi;
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	public PagedList<Post> getFeed() {
		return getFeed("me", FIRST_PAGE);
	}

	public PagedList<Post> getFeed(final PagingParameters pagedListParameters) {
		return getFeed("me", pagedListParameters);
	}

	public PagedList<Post> getFeed(final String ownerId) {
		return getFeed(ownerId, FIRST_PAGE);
	}

	public PagedList<Post> getFeed(final String ownerId, final PagingParameters pagedListParameters) {
		final JsonNode responseNode = fetchConnectionList(graphApi.getBaseGraphApiUrl() + ownerId + "/feed", pagedListParameters);
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<Post> getHomeFeed() {
		return getHomeFeed(FIRST_PAGE);
	}

	public PagedList<Post> getHomeFeed(final PagingParameters pagedListParameters) {
		final JsonNode responseNode = fetchConnectionList(graphApi.getBaseGraphApiUrl() + "me/home", pagedListParameters);
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<Post> getStatuses() {
		return getStatuses("me", FIRST_PAGE);
	}

	public PagedList<Post> getStatuses(final PagingParameters pagedListParameters) {
		return getStatuses("me", pagedListParameters);
	}

	public PagedList<Post> getStatuses(final String userId) {
		return getStatuses(userId, FIRST_PAGE);
	}

	public PagedList<Post> getStatuses(final String userId, final PagingParameters pagedListParameters) {
		final JsonNode responseNode = fetchConnectionList(graphApi.getBaseGraphApiUrl() + userId + "/statuses", pagedListParameters);
		return deserializeList(responseNode, "status", Post.class);
	}

	public PagedList<Post> getLinks() {
		return getLinks("me", FIRST_PAGE);
	}

	public PagedList<Post> getLinks(final PagingParameters pagedListParameters) {
		return getLinks("me", pagedListParameters);
	}

	public PagedList<Post> getLinks(final String ownerId) {
		return getLinks(ownerId, FIRST_PAGE);
	}

	public PagedList<Post> getLinks(final String ownerId, final PagingParameters pagedListParameters) {
		final JsonNode responseNode = fetchConnectionList(graphApi.getBaseGraphApiUrl() + ownerId + "/links", pagedListParameters);
		return deserializeList(responseNode, "link", Post.class);
	}

	public PagedList<Post> getPosts() {
		return getPosts("me", FIRST_PAGE);
	}

	public PagedList<Post> getPosts(final PagingParameters pagedListParameters) {
		return getPosts("me", pagedListParameters);
	}

	public PagedList<Post> getPosts(final String ownerId) {
		return getPosts(ownerId, FIRST_PAGE);
	}

	public PagedList<Post> getPosts(final String ownerId, final PagingParameters pagedListParameters) {
		final JsonNode responseNode = fetchConnectionList(graphApi.getBaseGraphApiUrl() + ownerId + "/posts", pagedListParameters);
		return deserializeList(responseNode, null, Post.class);
	}

	public PagedList<Post> getTagged() {
		return getTagged("me", FIRST_PAGE);
	}

	public PagedList<Post> getTagged(final PagingParameters pagedListParameters) {
		return getTagged("me", pagedListParameters);
	}

	public PagedList<Post> getTagged(final String ownerId) {
		return getTagged(ownerId, FIRST_PAGE);
	}

	public PagedList<Post> getTagged(final String ownerId, final PagingParameters pagedListParameters) {
		final JsonNode responseNode = fetchConnectionList(graphApi.getBaseGraphApiUrl() + ownerId + "/tagged", pagedListParameters);
		return deserializeList(responseNode, null, Post.class);
	}

	public Post getPost(final String entryId) {
		final ObjectNode responseNode = (ObjectNode) restTemplate.getForObject(graphApi.getBaseGraphApiUrl() + entryId, JsonNode.class);
		return deserializePost(null, Post.class, responseNode);
	}

	public String updateStatus(final String message) {
		return post("me", message);
	}

	public String postLink(final String message, final FacebookLink link) {
		return postLink("me", message, link);
	}

	public String postLink(final String ownerId, final String message, final FacebookLink link) {
		final MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.set("link", link.getLink());
		map.set("name", link.getName());
		map.set("caption", link.getCaption());
		map.set("description", link.getDescription());
		map.set("message", message);
		return graphApi.publish(ownerId, "feed", map);
	}

	public String post(final PostData post) {
		return graphApi.publish(post.getTargetFeedId(), "feed", post.toRequestParameters());
	}

	public String post(final String ownerId, final String message) {
		final MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.set("message", message);
		return graphApi.publish(ownerId, "feed", map);
	}

	public void deletePost(final String id) {
		graphApi.delete(id);
	}

	public PagedList<Post> getCheckins() {
		return getCheckins(new PagingParameters(25, 0, null, null));
	}

	public PagedList<Post> getCheckins(final PagingParameters pagedListParameters) {
		final MultiValueMap<String, String> params = getPagingParameters(pagedListParameters);
		params.set("with", "location");
		return graphApi.fetchConnections("me", "posts", Post.class, params);
	}

	public Post getCheckin(final String checkinId) {
		return graphApi.fetchObject(checkinId, Post.class);
	}

	// private helpers

	private JsonNode fetchConnectionList(final String baseUri, final PagingParameters pagedListParameters) {
		URIBuilder uriBuilder = URIBuilder.fromUri(baseUri);
		uriBuilder = appendPagedListParameters(pagedListParameters, uriBuilder);
		uriBuilder.queryParam("fields", StringUtils.arrayToCommaDelimitedString(ALL_POST_FIELDS));
		final URI uri = uriBuilder.build();
		final JsonNode responseNode = restTemplate.getForObject(uri, JsonNode.class);
		return responseNode;
	}

	private <T> PagedList<T> deserializeList(final JsonNode jsonNode, final String postType, final Class<T> type) {
		final JsonNode dataNode = jsonNode.get("data");
		final List<T> posts = new ArrayList<T>();
		for (final JsonNode jsonNode2 : dataNode) {
			posts.add(deserializePost(postType, type, (ObjectNode) jsonNode2));
		}
		if (jsonNode.has("paging")) {
			final JsonNode pagingNode = jsonNode.get("paging");
			final PagingParameters previousPage = getPagedListParameters(pagingNode, "previous");
			final PagingParameters nextPage = getPagedListParameters(pagingNode, "next");
			return new PagedList<T>(posts, previousPage, nextPage);
		}

		return new PagedList<T>(posts, null, null);
	}

	private <T> T deserializePost(String postType, final Class<T> type, final ObjectNode node) {
		try {
			if (postType == null) {
				postType = determinePostType(node);
			}

			// Must have separate postType field for polymorphic deserialization. If we key off of the "type" field, then it will
			// be null when trying to deserialize the type property.
			node.put("postType", postType); // used for polymorphic deserialization
			node.put("type", postType); // used to set Post's type property
			return objectMapper.reader(type).readValue(node.toString()); // TODO: EXTREMELY HACKY--TEMPORARY UNTIL I FIGURE OUT HOW JACKSON 2 DOES THIS
		} catch (final IOException shouldntHappen) {
			throw new UncategorizedApiException("facebook", "Error deserializing " + postType + " post", shouldntHappen);
		}
	}

	private String determinePostType(final ObjectNode node) {
		if (node.has("type")) {
			try {
				final String type = node.get("type").textValue();
				PostType.valueOf(type.toUpperCase());
				return type;
			} catch (final IllegalArgumentException e) {
				return "post";
			}
		}
		return "post";
	}

	private URIBuilder appendPagedListParameters(final PagingParameters pagedListParameters,
			URIBuilder uriBuilder) {
		if (pagedListParameters.getLimit() != null) {
			uriBuilder = uriBuilder.queryParam("limit", String.valueOf(pagedListParameters.getLimit()));
		}
		if (pagedListParameters.getSince() != null) {
			uriBuilder = uriBuilder.queryParam("since", String.valueOf(pagedListParameters.getSince()));
		}
		if (pagedListParameters.getUntil() != null) {
			uriBuilder = uriBuilder.queryParam("until", String.valueOf(pagedListParameters.getUntil()));
		}
		if (pagedListParameters.getAfter() != null) {
			uriBuilder = uriBuilder.queryParam("after", String.valueOf(pagedListParameters.getAfter()));
		}
		if (pagedListParameters.getBefore() != null) {
			uriBuilder = uriBuilder.queryParam("before", String.valueOf(pagedListParameters.getBefore()));
		}
		if (pagedListParameters.getPagingToken() != null) {
			uriBuilder = uriBuilder.queryParam("__paging_token", String.valueOf(pagedListParameters.getPagingToken()));
		}
		return uriBuilder;
	}


	private static final String[] ALL_POST_FIELDS = {
		"id", "actions", "admin_creator", "application", "caption", "created_time", "description", "from", "icon",
		"is_hidden", "is_published", "link", "message", "message_tags", "name", "object_id", "picture", "place",
		"privacy", "properties", "source",
		"status_type", "story", "to", "type", "updated_time", "with_tags", "shares", "full_picture"
	};

}
