package org.hibernate;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties获取值方法工具类
 * 
 * @author Sunshine
 *
 */
class PropertiesUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

	/**
	 * Get string value from properties via propertyName
	 * 
	 * @param properties properties
	 * @param propertyName the key
	 * @return a string value,default ""
	 */
	public static String getString(Properties properties, String propertyName) {
		return getString(properties, propertyName, "");
	}

	/**
	 * Get string value from properties via propertyName,with defaultValue return if failure
	 * 
	 * @param properties properties
	 * @param propertyName the key
	 * @param defaultValue the default value
	 * @return a string value,default defaultValue
	 */
	public static String getString(Properties properties, String propertyName, String defaultValue) {
		String value = properties.getProperty(propertyName);
		if (value == null || value.trim().length() == 0) {
			LOGGER.warn("Property not found : {}, use default value : {}", propertyName, defaultValue);
			return defaultValue;
		}

		return value;
	}

	/**
	 * Get boolean value from properties via propertyName
	 * 
	 * @param properties properties
	 * @param propertyName the key
	 * @return a boolean value,default false
	 */
	public static boolean getBoolean(Properties properties, String propertyName) {
		return getBoolean(properties, propertyName, false);
	}

	/**
	 * Get boolean value from properties via propertyName,with defaultValue return if failure
	 * 
	 * @param properties properties
	 * @param propertyName the key
	 * @param defaultValue the default value
	 * @return a boolean value,default defaultValue
	 */
	public static boolean getBoolean(Properties properties, String propertyName, boolean defaultValue) {
		String value = properties.getProperty(propertyName);
		if (value == null || value.trim().length() == 0) {
			LOGGER.warn("Property not found : " + propertyName + ", use default value : " + defaultValue);
			return defaultValue;
		}

		return Boolean.parseBoolean(value);
	}

	/**
	 * Get int value from properties via propertyName
	 * 
	 * @param properties properties
	 * @param propertyName the key
	 * @return a int value,default 0
	 */
	public static int getInt(Properties properties, String propertyName) {
		return getInt(properties, propertyName, 0);
	}

	/**
	 * Get int value from properties via propertyName,with defaultValue return if failure
	 * 
	 * @param properties properties
	 * @param propertyName the key
	 * @param defaultValue the default value
	 * @return a int value,default defaultValue
	 */
	public static int getInt(Properties properties, String propertyName, int defaultValue) {
		String value = properties.getProperty(propertyName);
		if (value == null || value.trim().length() == 0) {
			LOGGER.warn("Property not found : " + propertyName + ", use default value : " + defaultValue);
			return defaultValue;
		}

		return Integer.parseInt(value);
	}
}
