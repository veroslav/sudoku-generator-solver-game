/*
* This file is part of SuDonkey, an open-source Sudoku puzzle game generator and solver.
* Copyright (C) 2014 Vedran Matic
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*
*/

package com.matic.sudoku;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

/**
 * Various resources (such as translations and images) used by the application 
 * @author vedran
 *
 */
public class Resources {
	
	//The name of the Nimbus Look&Feel
	private static final String NIMBUS_LAF_NAME = "Nimbus";
	
	//Application preferences stored between the program sessions 
	private static final Preferences APPLICATION_PREFERENCES = Preferences.userRoot().node(Resources.class.getName());
	
	//Resource bundle holding language translations for the active language
	private static ResourceBundle LANGUAGE_RESOURCE_BUNDLE; 
	
	//Language identifier for the default language (English)
	private static final String DEFAULT_LANG_VALUE = "en";
	
	//Resource key pointing to the player selected language ("en" by default and empty)
	private static final String PLAYER_LANG_KEY = "player.lang";
	
	//Last path selected when opening and storing puzzles
	public static final String CURRENT_PATH = "current.path";
	
	//Represents a zero (as dot) found one some Sudoku forums 
	public static final char ZERO_DOT_FORMAT = '.';
	
	//A single random instance to use for randomness generation needs of the whole app
	public static final Random RANDOM_INSTANCE = new Random(System.nanoTime());
	
	static {
		final List<String> availableLanguageResourceNames = Resources.getAvailableLanguageResourceNames();	
		
		//Get previously stored language preference, if any
		final String storedLanguageName = APPLICATION_PREFERENCES.get(PLAYER_LANG_KEY, null);		
		final String defaultPlayerLanguage = Locale.getDefault().getLanguage();
		
		String targetLanguageName = DEFAULT_LANG_VALUE;
		
		//Check if a preffered language was set previously and has a translation file available
		if(storedLanguageName != null 
				&& availableLanguageResourceNames.contains(storedLanguageName)) {			
			targetLanguageName = storedLanguageName;
		}
		//Check if there is a translation file for current player language
		else if(availableLanguageResourceNames.contains(defaultPlayerLanguage)) {			
			targetLanguageName = defaultPlayerLanguage;
		}
				
		LANGUAGE_RESOURCE_BUNDLE = ResourceBundle.getBundle("resources.lang", new Locale(targetLanguageName));			
		setLanguage(targetLanguageName);
		
		UIManager.put("OptionPane.yesButtonText", LANGUAGE_RESOURCE_BUNDLE.getString("button.yes"));
		UIManager.put("OptionPane.noButtonText", LANGUAGE_RESOURCE_BUNDLE.getString("button.no"));
		UIManager.put("OptionPane.cancelButtonText", LANGUAGE_RESOURCE_BUNDLE.getString("button.cancel"));		
	}

	//Prevent instantiation of this class
	private Resources() {}
	
	/**
	 * Find out whether current look and feel is Nimbus LAF
	 * 
	 * @return Whether the current look and feel is set to Nimbus
	 */
	public static boolean isNimbusLookAndFeel() {
		return NIMBUS_LAF_NAME.equals(UIManager.getLookAndFeel().getName());
	}
	
	/**
	 * Get a value of an application property
	 * 
	 * @param propName Property name
	 * @param defaultValue Default value to set and return if it is a new property
	 * @return
	 */
	public static String getProperty(final String propName, final String defaultValue) {
		return APPLICATION_PREFERENCES.get(propName, defaultValue);
	}
	
	/**
	 * Update existing och create a new application property with the given value
	 * 
	 * @param propName Property name
	 * @param propValue Property value
	 */
	public static void setProperty(final String propName, final String propValue) {
		APPLICATION_PREFERENCES.put(propName, propValue);		
	}
	
	/**
	 * Get language code for currently active language
	 * 
	 * @return Current language code
	 */
	public static String getLanguage() {
		final String language = LANGUAGE_RESOURCE_BUNDLE.getLocale().getLanguage(); 
		return language.isEmpty()? DEFAULT_LANG_VALUE : language;
	}
	
	/**
	 * Convenience method for updating language resource bundle with translations for target language code
	 * 
	 * @param languageCode New language code
	 */
	public static void setLanguage(final String languageCode) {	
		Resources.setProperty(PLAYER_LANG_KEY, languageCode);
	}
	
	/**
	 * Get string translation for the active language
	 * 
	 * @param string String to translate
	 * @return String translated to the currently active language
	 */
	public static String getTranslation(final String string) {
		return LANGUAGE_RESOURCE_BUNDLE.getString(string);
	}
	
	/**
	 * Return a player friendly language name (used in language menu items) 
	 * 
	 * @param locale Target language name code
	 * @return Friendly language name
	 */
	public static String getLanguagePresentationName(final String languageName) {
		final Locale locale = new Locale(languageName);
		final String langName = locale.getDisplayLanguage(locale);
		final StringBuilder builder = new StringBuilder(langName);
		builder.setCharAt(0, Character.toUpperCase(langName.charAt(0)));
		
		return builder.toString();
	}
	
	/**
	 * Get all locales for which a language resource is available
	 * 
	 * @return List of locale codes with available translations
	 */
	public static List<String> getAvailableLanguageResourceNames() {
		final String[] languages = Locale.getISOLanguages();		
		final List<String> localeNames = new ArrayList<>();
		
		for(final String lang : languages) {
			final URL url = ClassLoader.getSystemResource("resources/lang_"+lang+".properties");
			if(url != null) {
				final String urlAsString = url.toString();
				final int startIndex = urlAsString.indexOf('_');
				final int endIndex = urlAsString.indexOf('.', startIndex);
				final String langCode = urlAsString.substring(startIndex + 1, endIndex);				
								
				localeNames.add(langCode);
			}
		}
		
		return localeNames;
	}
}