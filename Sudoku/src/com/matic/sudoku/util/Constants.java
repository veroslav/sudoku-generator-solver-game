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

package com.matic.sudoku.util;

import java.util.Random;

/**
 * Various constants used that do not belong to any specific class 
 * @author vedran
 *
 */
public class Constants {
	
	//The name of this application
	public static final String APPLICATION_NAME = "SuDonkey";
	
	//The version of this application
	public static final String APPLICATION_VERSION = "1.0.0";
	
	//When used as an array index, represents a board's X coordinate
	public static final int X = 0;
	
	//When used as an array index, represents a board's Y coordinate
	public static final int Y = 1;
	
	//Represents a zero (as dot) found one some Sudoku forums 
	public static final char ZERO_DOT_FORMAT = '.';
	
	//A single random instance to use for randomness generation needs of the whole app
	public static final Random RANDOM_INSTANCE = new Random(System.nanoTime());

	//Prevent instantiation of this class
	private Constants() {}

}