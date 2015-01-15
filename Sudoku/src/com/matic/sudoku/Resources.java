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

import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Various resources (such as translations and images) used by the application 
 * @author vedran
 *
 */
public class Resources {
	
	//Resource bundle holding language translations for the active language
	private static final ResourceBundle LANGUAGE_RESOURCE_BUNDLE = 
			ResourceBundle.getBundle("resources.lang");
	
	//When used as an array index, represents a board's X coordinate
	public static final int X = 0;
	
	//When used as an array index, represents a board's Y coordinate
	public static final int Y = 1;
	
	//Represents a zero (as dot) found one some Sudoku forums 
	public static final char ZERO_DOT_FORMAT = '.';
	
	//A single random instance to use for randomness generation needs of the whole app
	public static final Random RANDOM_INSTANCE = new Random(System.nanoTime());

	//Prevent instantiation of this class
	private Resources() {}
	
	/**
	 * Get string translation for the active language
	 * 
	 * @param string String to translate
	 * @return String translated to the currently active language
	 */
	public static String getTranslation(final String string) {
		return LANGUAGE_RESOURCE_BUNDLE.getString(string);
	}
}