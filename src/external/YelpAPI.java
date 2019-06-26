package external;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YelpAPI implements ExternalAPI {
	private static final String API_HOST = "https://api.yelp.com";
	private static final String DEFAULT_TERM = "dinner";
	private static final int SEARCH_LIMIT = 20;
	private static final String SEARCH_PATH = "/v3/businesses/search";

	private static final String TOKEN_HOST = "https://api.yelp.com/oauth2/token";
	// update if necessary, for demo only
	private static final String CLIENT_ID = "cC7RtV40EDeUsTxm8_2FIA";
	private static final String CLIENT_SECRET = "OxGIJlNU_q4tezT4Qt_0lpzB-AA6Xur3qUsjEL2ZnBdbgfOS-6Ary2rI84UCyediy0bhJVYQanRtogbc1n1Jd2TXhL3a0GFmXAbdYscp-2fyvARGiJJHUmrlvK-oWnYx";
	private static final String GRANT_TYPE = "client_credentials";
	private static final String TOKEN_TYPE = "Bearer";

	private static String accessToken;

	public YelpAPI() {
	}

	/**
	 * Create and send a request to Yelp Token Host and return the access token
	 * 
	 * This method is no longer needed, Yelp Fusion has changed the method to call the API
	 */
	@SuppressWarnings("unused")
	private String getAccessToken() {
		if (accessToken != null) {
			return accessToken;
		}

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(TOKEN_HOST).openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			String query = String.format("grant_type=%s&client_id=%s&client_secret=%s", GRANT_TYPE, CLIENT_ID, CLIENT_SECRET);
			wr.write(query.getBytes());

			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + TOKEN_HOST);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			accessToken = (new JSONObject(response.toString())).getString("access_token");
			System.out.println("Get access token : " + accessToken);
			return accessToken;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates and sends a request to the Search API by term and location.
	 */
	public List<Item> search(double lat, double lon, String term) {
		String latitude = lat + "";
		String longitude = lon + "";
		if (term == null || term.isEmpty()) {
			term = DEFAULT_TERM;
		}
		term = urlEncodeHelper(term);
		String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s", term, latitude, longitude, SEARCH_LIMIT);
		String url = API_HOST + SEARCH_PATH;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url + "?" + query).openConnection();

			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + CLIENT_SECRET);

			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url + "?" + query);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject jsonObject = new JSONObject(response.toString());
			JSONArray bussinesses = (JSONArray) jsonObject.get("businesses");
			return getItemList(bussinesses);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String urlEncodeHelper(String term) {
		try {
			term = java.net.URLEncoder.encode(term, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return term;
	}
	
	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray array) throws JSONException {
		List<Item> list = new ArrayList<>();

		for (int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			// Parse json object fetched from Yelp API specifically.
			ItemBuilder builder = new ItemBuilder();
			// Builder pattern gives us flexibility to construct an item.
			builder.setItemId(object.getString("id"));
			JSONArray jsonArray = (JSONArray) object.get("categories");
			Set<String> categories = new HashSet<>();
			for (int j = 0; j < jsonArray.length(); j++) {
				JSONObject subObejct = jsonArray.getJSONObject(j);
				categories.add(subObejct.getString("title"));
			}
			builder.setCategories(categories);
			builder.setName(object.getString("name"));
			builder.setImageUrl(object.getString("image_url"));
			builder.setRating(object.getDouble("rating"));
			JSONObject coordinates = (JSONObject) object.get("coordinates");
			builder.setLatitude(coordinates.getDouble("latitude"));
			builder.setLongitude(coordinates.getDouble("longitude"));
			JSONObject location = (JSONObject) object.get("location");
			builder.setCity(location.getString("city"));
			builder.setState(location.getString("state"));
			builder.setZipcode(location.getString("zip_code"));
			JSONArray addresses = (JSONArray) location.get("display_address");
			String fullAddress = addresses.join(",");
			builder.setAddress(fullAddress);
			builder.setImageUrl(object.getString("image_url"));
			builder.setUrl(object.getString("url"));

			Item item = builder.build();
			list.add(item);
		}
		return list;
	}

	/**
	 * Queries the Search API based on the command line arguments and takes the
	 * first result to query the Business API.
	 */
	private void queryAPI(double lat, double lon) {
		List<Item> list = search(lat, lon, null);
		try {
			for (Item item : list) {
				// This is a thin version of the original JSONObject fetched from Yelp.
				JSONObject jsonObject = item.toJSONObject();
				System.out.println(jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Main entry for sample Yelp API requests.
	 */
	public static void main(String[] args) {
		YelpAPI yelpApi = new YelpAPI();
		yelpApi.queryAPI(37.38, -122.08);
	}
}