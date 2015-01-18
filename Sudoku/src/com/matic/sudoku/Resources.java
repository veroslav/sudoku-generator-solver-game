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
	
	//Application preferences stored between the program sessions 
	private static final Preferences APPLICATION_PREFERENCES = Preferences.userRoot().node(Resources.class.getName());
	
	//Resource bundle holding language translations for the active language
	private static ResourceBundle LANGUAGE_RESOURCE_BUNDLE; 
	
	//Resource key pointing to the player selected language ("en" by default and empty)
	private static final String PLAYER_LANG_KEY = "player.lang";
	
	//Language identifier for the default language (English)
	private static final String DEFAULT_LANG_VALUE = "en";
	
	//When used as an array index, represents a board's X coordinate
	public static final int X = 0;
	
	//When used as an array index, represents a board's Y coordinate
	public static final int Y = 1;
	
	//Represents a zero (as dot) found one some Sudoku forums 
	public static final char ZERO_DOT_FORMAT = '.';
	
	//A single random instance to use for randomness generation needs of the whole app
	public static final Random RANDOM_INSTANCE = new Random(System.nanoTime());
	
	static {
		final List<Locale> availableLocales = Resources.getAvailableResourceLocales();		
		final Locale storedLanguage = new Locale(APPLICATION_PREFERENCES.get(PLAYER_LANG_KEY, DEFAULT_LANG_VALUE));
		
		if(!availableLocales.contains(storedLanguage)) {			
			LANGUAGE_RESOURCE_BUNDLE = ResourceBundle.getBundle("resources.lang");
		}
		else {			
			LANGUAGE_RESOURCE_BUNDLE = ResourceBundle.getBundle("resources.lang", storedLanguage);
		}				
		
		UIManager.put("OptionPane.yesButtonText", LANGUAGE_RESOURCE_BUNDLE.getString("button.yes"));
		UIManager.put("OptionPane.noButtonText", LANGUAGE_RESOURCE_BUNDLE.getString("button.no"));
		UIManager.put("OptionPane.cancelButtonText", LANGUAGE_RESOURCE_BUNDLE.getString("button.cancel"));		
	}

	//Prevent instantiation of this class
	private Resources() {}
	
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
	 * Conveniance method for updating language resource bundle with translations for target language code
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
	 * @param locale Target locale
	 * @return Friendly language name
	 */
	public static String getLanguagePresentationName(final Locale locale) {
		final String langName = locale.getDisplayLanguage(locale);
		final StringBuilder builder = new StringBuilder(langName);
		builder.setCharAt(0, Character.toUpperCase(langName.charAt(0)));
		
		return builder.toString();
	}
	
	/**
	 * Get all locales for which a language resource is available
	 * 
	 * @return List of locales with available translations
	 */
	public static List<Locale> getAvailableResourceLocales() {
		final String[] languages = Locale.getISOLanguages();		
		final List<Locale> locales = new ArrayList<Locale>();
		locales.add(Locale.ENGLISH);
		
		for(final String lang : languages) {
			final URL url = ClassLoader.getSystemResource("resources/lang_"+lang+".properties");
			if(url != null) {
				final String urlAsString = url.toString();
				final int startIndex = urlAsString.indexOf('_');
				final int endIndex = urlAsString.indexOf('.', startIndex);
				final String langCode = urlAsString.substring(startIndex + 1, endIndex);				
				
				final Locale locale = new Locale(langCode);
				locales.add(locale);
			}
		}
		
		return locales;
	}
}