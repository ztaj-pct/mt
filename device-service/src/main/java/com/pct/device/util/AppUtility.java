package com.pct.device.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * This is the utility class for validating different type of objects.
 * 
 * @author Aakash
 *
 */
public class AppUtility {

	private AppUtility() {
	}

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * To validate email
	 * 
	 * @param text {@link String}
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean isEmail(String text) {
		if (isEmpty(text)) {
			return false;
		}
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(text);
		return matcher.find();
	}

	/**
	 * To Validate Collection
	 * 
	 * @param collection {@link Collection}
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return (collection == null || collection.isEmpty());
	}

	/**
	 * To Validate Map
	 * 
	 * @param map {@link Map}
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}

	/**
	 * To Validate Object
	 * 
	 * @param object {@link Object}
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean isEmpty(Object object) {
		return (object == null);
	}

	/**
	 * To Validate Object array
	 * 
	 * @param array {@link Object} array
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean isEmpty(Object[] array) {
		return (array == null || array.length == 0);
	}

	/**
	 * To Validate String
	 * 
	 * @param string {@link String}
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean isEmpty(String string) {
		return (string == null || string.trim().length() == 0);
	}
	
	/**
	 * To check null string key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link JSONObject}
	 */
	public static JSONObject checkJSONObjectNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? null : json.optJSONObject(key);
	}
	
	/**
	 * To check null string key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link JSONArray}
	 */
	public static JSONArray checkJSONArrayNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? null : json.optJSONArray(key);
	}
	
	/**
	 * To check null string key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link String}
	 */
	public static String checkNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? null : json.optString(key);
	}

	/**
	 * To check null boolean key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link Boolean} is invalid or valid
	 */
	public static boolean checkBooleanNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? null : json.getBoolean(key);
	}

	/**
	 * To check null Double key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link Double} value
	 */
	public static Double checkDoubleNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? 0d : json.getDouble(key);
	}

	/**
	 * To check null integer key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link Integer}
	 */
	public static int checkIntNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? 0 : json.getInt(key);
	}

	/**
	 * To check null float key on JSON
	 * 
	 * @param json {@link JSONObject}
	 * @param key  {@link String}
	 * @return {@link Float}
	 */
	public static Float checkFlotNullKey(final JSONObject json, final String key) {
		return json.isNull(key) ? null : json.getFloat(key);
	}
	
	public <T> List<T> jsonArrayToObjectList(String json, Class<T> tClass) throws IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, tClass);
	    List<T> ts = mapper.readValue(json, listType);
	    return ts;
	}
}
