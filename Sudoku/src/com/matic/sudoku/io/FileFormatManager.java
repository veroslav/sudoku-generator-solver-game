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

package com.matic.sudoku.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.matic.sudoku.Resources;

/**
 * A class for managing puzzle extraction from Strings/Files and offering methods for
 * storing puzzles to the disk. The following are the supported puzzle formats:
 * - SadMan Sudoku puzzle files (*.sdk)
 * - SimpleSudoku puzzle files (*.ss)
 * - SudoCue files (*.sdk)
 * - Simple format, where whole puzzle is contained on a single row
 * 
 * @author vedran
 *
 */

public class FileFormatManager {
	
	public static final String SDK_SUDOKU_FILTER_NAME = "format.sdk";
	public static final String SADMAN_SUDOKU_FILTER_NAME = "format.sadman";
	public static final String SIMPLE_SUDOKU_FILTER_NAME = "format.simple_sudoku";
	public static final String SUDOCUE_SUDOKU_FILTER_NAME = "format.sudocue";
	public static final String EMPTY_STRING = "";
	
	private static final String SDK_EXTENSION = "sdk";
	private static final String SS_EXTENSION = "ss";
	
	private static final String[] SADMAN_HEADERS = {"A","C","D","B","S","U","L","N","H","T"};
	private static final String[] SUDOCUE_HEADERS = {"A","D","C","B", "S", "L", "U"};
	
	private static final String SADMAN_PENCILMARKS_TAG = "[PencilMarks]";
	private static final String SADMAN_COLOURS_TAG = "[Colours]";
	private static final String SADMAN_PUZZLE_TAG = "[Puzzle]";
	private static final String SADMAN_STATE_TAG = "[State]";	
		
	private static final String SIMPLE_SUDOKU_COLUMN_SEPARATOR = "|";
	private static final String SIMPLE_SUDOKU_ROW_SEPARATOR = "-";		
	
	private static final char PENCILMARK_SEPARATOR = ',';
	private static final char SADMAN_HEADER_TAG = '#';
	private static final char SPACE_CHAR = ' ';
	private static final char ZERO_CHAR = '0';
	private static final char DOT_CHAR = '.';
	
	private static final int CLASSIC_PUZZLE_CELL_COUNT = 81;
	private static final int CLASSIC_PUZZLE_DIMENSION = 3;
	private static final int CLASSIC_PUZZLE_UNIT = 9;
	
	//All supported Sudoku file formats (read and write)
	public enum FormatType {
		SADMAN_SUDOKU, SUDOCUE_SUDOKU, SIMPLE_SUDOKU, SIMPLE_FORMAT
	}
	
	public static FileFilter[] getSupportedFileSaveFilters() {
		final FileFilter[] fileFilters = {
				new FileNameExtensionFilter(Resources.getTranslation("format.sadman"), 
						SDK_EXTENSION),
				new FileNameExtensionFilter(Resources.getTranslation("format.simple_sudoku"), 
						SS_EXTENSION),
				new FileNameExtensionFilter(Resources.getTranslation("format.sudocue"), 
						SDK_EXTENSION)};
		
		return fileFilters;
	}
	
	public static FileFilter[] getSupportedFileOpenFilters() {
		final FileFilter[] fileFilters = {
				new FileNameExtensionFilter(
						Resources.getTranslation("format.sdk"), SDK_EXTENSION),
				new FileNameExtensionFilter(
						Resources.getTranslation("format.simple_sudoku"), SS_EXTENSION)};
		
		return fileFilters;
	}
	
	public static String getFormatTypeExtensionName(final FormatType formatType) {
		switch(formatType) {
		case SADMAN_SUDOKU:
		case SUDOCUE_SUDOKU:
			return SDK_EXTENSION;
		case SIMPLE_SUDOKU:
			return SS_EXTENSION;
		default:
			return EMPTY_STRING;	
		}
	}
	
	/**
	 * Write a puzzle to a file using a specified file format
	 * 
	 * @param targetFile The file to write the puzzle to
	 * @param puzzleBean A bean containing the puzzle info to write
	 * @throws IOException If any write error occurs
	 * @throws UnsupportedPuzzleFormatException On any invalid or missing puzzle properties
	 */
	public void write(final File targetFile, final PuzzleBean puzzleBean) 
			throws IOException, UnsupportedPuzzleFormatException {
		PrintWriter writer = null;
		
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)));
			switch(puzzleBean.getFormatType()) {
			case SADMAN_SUDOKU:
				writeSadmanFormat(writer, puzzleBean);
				break;
			case SUDOCUE_SUDOKU:
				writeSudocueFormat(writer, puzzleBean.getPuzzle(), puzzleBean.getHeaders());
				break;
			case SIMPLE_SUDOKU:
				writeSimpleSudokuFormat(writer, puzzleBean.getPuzzle());
				break;
			case SIMPLE_FORMAT:
				writeSimpleFormat(writer, puzzleBean.getPuzzle());
				break;
			}
			writer.flush();
		}
		finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Parse the puzzle info from a String (usually pasted from the clipboard)
	 * 
	 * @param input String containing the puzzle to be parsed
	 * @return All puzzle info successfully extracted by the parser
	 * @throws IOException If any read error occurs
	 * @throws UnsupportedPuzzleFormatException If puzzle format is not understood
	 */
	public PuzzleBean fromString(final String input) throws IOException, UnsupportedPuzzleFormatException {
		final BufferedReader reader = new BufferedReader(new StringReader(input));
		PuzzleBean response = null;
		
		try {
			response = parse(reader);
		}
		finally {
			reader.close();
		}
		
		return response;
	}
	
	/**
	 * Parse the puzzle info from a file on the disk
	 * 
	 * @param file A file containing the puzzle to be parsed
	 * @return All puzzle info successfully extracted by the parser
	 * @throws IOException If any read error occurs
	 * @throws UnsupportedPuzzleFormatException If puzzle format is not understood
	 */
	public PuzzleBean fromFile(final File file) throws IOException, UnsupportedPuzzleFormatException {
		BufferedReader reader = null;
		PuzzleBean response = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			response = parse(reader);
		}
		finally {
			if(reader != null) {
				reader.close();
			}
		}
		
		return response;
	}
	
	private void writeSimpleFormat(final PrintWriter writer, final int[] puzzle) 
			throws IOException, UnsupportedPuzzleFormatException {
		if(puzzle == null) {
			throw new UnsupportedPuzzleFormatException("Missing puzzle");
		}
		for(int i = 0; i < puzzle.length; ++i) {
			if(puzzle[i] > 0) {
				writer.print(puzzle[i]);
			}
			else {
				writer.print(DOT_CHAR);
			}
		}
	}
	
	private void writeSimpleSudokuFormat(final PrintWriter writer, final int[] puzzle) 
			throws IOException, UnsupportedPuzzleFormatException {
		if(puzzle == null) {
			throw new UnsupportedPuzzleFormatException("Missing puzzle");
		}
		int puzzleIndex = 0;
		for(int i = 0; i < CLASSIC_PUZZLE_UNIT; ++i) {
			for(int j = 0; j < CLASSIC_PUZZLE_UNIT; ++j) {
				final int currentValue = puzzle[puzzleIndex++];
				if(currentValue > 0) {
					writer.print(currentValue);
				}
				else {
					writer.print(DOT_CHAR);
				}
				if(((j + 1) % CLASSIC_PUZZLE_DIMENSION == 0) && j < CLASSIC_PUZZLE_UNIT - 1) {
					//Write column separator after first and second box
					writer.print(SIMPLE_SUDOKU_COLUMN_SEPARATOR);
				}
			}
			if(i < CLASSIC_PUZZLE_UNIT - 1) {
				writer.println();
			}
			if((i + 1) % CLASSIC_PUZZLE_DIMENSION == 0 && i < CLASSIC_PUZZLE_UNIT - 1) {
				for(int k = 0; k < CLASSIC_PUZZLE_UNIT + CLASSIC_PUZZLE_DIMENSION - 1; ++k) {
					writer.print(SIMPLE_SUDOKU_ROW_SEPARATOR);
				}
				writer.println();
			}
		}
	}
	
	private void writeSadmanFormat(final PrintWriter writer, final PuzzleBean puzzleBean) 
			throws UnsupportedPuzzleFormatException {
		final BitSet givens = puzzleBean.getGivens();
		final int[] puzzle = puzzleBean.getPuzzle();		
		
		if(puzzle == null || givens == null) {
			throw new UnsupportedPuzzleFormatException("Missing puzzle");
		}
		
		final Map<String, String> headers = puzzleBean.getHeaders();
		final BitSet[][] pencilmarks = puzzleBean.getPencilmarks();
		final int[] colors = puzzleBean.getColors();
		
		if(headers != null) {
			writeSdkHeaders(writer, headers, true);
		}
		
		writeSdkPuzzle(writer, puzzle, givens);
		writeSdkState(writer, puzzle, true);
		
		if(pencilmarks != null) {
			writeSdkPencilmarks(writer, pencilmarks, true);
		}
		if(colors != null) {
			writeSdkColours(writer, colors);
		}
	}
	
	private void writeSudocueFormat(final PrintWriter writer, final int[] puzzle, 
			final Map<String, String> headers) 
			throws UnsupportedPuzzleFormatException {
		if(puzzle == null) {
			throw new UnsupportedPuzzleFormatException("Missing puzzle");
		}
		if(headers != null) {
			writeSdkHeaders(writer, headers, false);
		}
		writeSdkState(writer, puzzle, false);
	}
	
	private void writeSdkColours(final PrintWriter writer, final int[] colors) {
		writer.println(SADMAN_COLOURS_TAG);
		
		int colorIndex = 0;
		for(int i = 0; i < CLASSIC_PUZZLE_UNIT; ++i) {
			for(int j = 0; j < CLASSIC_PUZZLE_UNIT; ++j) {
				final int currentValue = colors[colorIndex++];
				if(currentValue > 0) {
					writer.print(currentValue);
				}
				else {
					writer.print(DOT_CHAR);
				}
			}
			if(i < CLASSIC_PUZZLE_UNIT - 1) {
				writer.println();
			}
		}
	}
	
	private void writeSdkPuzzle(final PrintWriter writer, final int[] puzzle, final BitSet givens) {
		writer.println(SADMAN_PUZZLE_TAG);
		
		int puzzleIndex = 0;
		for(int i = 0; i < CLASSIC_PUZZLE_UNIT; ++i) {
			for(int j = 0; j < CLASSIC_PUZZLE_UNIT; ++j) {
				final int currentValue = puzzle[puzzleIndex];
				if(currentValue > 0 && givens.get(puzzleIndex)) {
					writer.print(currentValue);
				}
				else {
					writer.print(DOT_CHAR);
				}
				++puzzleIndex;
			}
			writer.println();
		}
	}
	
	private void writeSdkPencilmarks(final PrintWriter writer, final BitSet[][] pencilmarks, final boolean writePencilmarksHeader) {
		if(writePencilmarksHeader) {
			writer.println(SADMAN_PENCILMARKS_TAG);
		}
		for(int i = 0; i < pencilmarks.length; ++i) {
			for(int j = 0; j < pencilmarks[i].length; ++j) {
				final BitSet rowPencilmarks = pencilmarks[j][i];
				for(int k = 0; k < CLASSIC_PUZZLE_UNIT; ++k) {
					if(rowPencilmarks.get(k)) {
						writer.print(k + 1);
					}
				}
				if(j < pencilmarks[i].length - 1) {
					writer.print(PENCILMARK_SEPARATOR);
				}
			}
			writer.println();
		}
	}
	
	private void writeSdkState(final PrintWriter writer, final int[] puzzle, final boolean isSadmanFormat) {
		if(isSadmanFormat) {
			writer.println(SADMAN_STATE_TAG);
		}
		int puzzleIndex = 0;
		for(int i = 0; i < CLASSIC_PUZZLE_UNIT; ++i) {
			for(int j = 0; j < CLASSIC_PUZZLE_UNIT; ++j) {
				final int currentValue = puzzle[puzzleIndex++];
				if(currentValue > 0) {
					writer.print(currentValue);
				}
				else {
					writer.print(DOT_CHAR);
				}
			}
			if((i < CLASSIC_PUZZLE_UNIT - 1 && !isSadmanFormat) || isSadmanFormat) {
				writer.println();
			}
		}
	}
	
	private void writeSdkHeaders(final PrintWriter writer, final Map<String, String> headers, final boolean sadmanFormat) {
		String[] headerNames = null;
		
		if(sadmanFormat) {
			headerNames = SADMAN_HEADERS;
		}
		else {
			headerNames = SUDOCUE_HEADERS;
		}
		for(final String headerName : headerNames) {
			final String headerValue = headers.get(headerName);
			if(headerValue != null) {
				writer.print(SADMAN_HEADER_TAG);
				writer.print(headerName);
				if(sadmanFormat) {
					writer.print(SPACE_CHAR);
				}
				writer.println(headerValue);
			}
		}
	}
	
	private PuzzleBean parse(final BufferedReader reader) throws IOException, UnsupportedPuzzleFormatException {
		String firstLine = reader.readLine();
		
		if(firstLine == null) {
			throw new UnsupportedPuzzleFormatException("Empty input file");
		}
		
		firstLine = firstLine.trim();
		
		//Check whether a puzzle was pasted from the clipboard
		if(isClipboardFormat(firstLine)) {
			return parseClipboardFormat(firstLine);
		}
		
		//Check whether the puzzle is in Simple Sudoku (*.ss) format
		if(isSimpleSudokuFormat(firstLine)) {
			return parseSimpleSudokuFormat(reader, firstLine);
		}
		
		//The puzzle is in either 'SadMan Sudoku' or 'Sudocue' format (*.sdk) or an unsupported format
		return parseSdkFormat(reader, firstLine);		
	}
	
	private boolean isClipboardFormat(final String line) {
		if(line.length() != CLASSIC_PUZZLE_CELL_COUNT) {
			return false;
		}
		return verifyClipboardFormatRow(line);
	}
	
	private boolean isSimpleSudokuFormat(final String line) {
		return (line.length() == CLASSIC_PUZZLE_UNIT + CLASSIC_PUZZLE_DIMENSION - 1) && 
				line.contains(SIMPLE_SUDOKU_COLUMN_SEPARATOR);
	}
	
	private boolean verifyClipboardFormatRow(final String line) {
		for(int i = 0; i < line.length(); ++i) {
			final char currentChar = line.charAt(i);
			if(!(Character.isDigit(currentChar) || currentChar == DOT_CHAR)) {
				return false;
			}
		}
		return true;
	}
	
	private PuzzleBean parseClipboardFormat(final String input) {
		final String trimmed = input.trim();
		final int[] puzzle = new int[CLASSIC_PUZZLE_CELL_COUNT];
		
		for(int i = 0; i < puzzle.length; ++i) {
			final char currentChar = trimmed.charAt(i);
			if(Character.isDigit(currentChar) && currentChar != ZERO_CHAR) {
				puzzle[i] = Character.getNumericValue(currentChar);
			}
			else {
				puzzle[i] = 0;
			}
		}
		
		final PuzzleBean response = new PuzzleBean(puzzle);
		response.setFormatType(FormatType.SIMPLE_FORMAT);
		
		return response;
	}
	
	private PuzzleBean parseSdkFormat(final BufferedReader reader, final String firstLine) 
			throws IOException, UnsupportedPuzzleFormatException {
		boolean isSadmanPuzzleFormat = false;
		Map<String, String> headers = null;
		BitSet[][] pencilmarks = null;
		BitSet givens = null;
		int[] puzzle = null;		
		int[] colors = null;
		int[] state = null;
		
		String line = firstLine;
		
		do {
			if(isSdkHeaderSection(line)) {
				isSadmanPuzzleFormat = line.charAt(1) == SPACE_CHAR;
				headers = parseSdkHeaderSection(reader, line);
			}	
			else if(isSdkPencilmarksSection(line)) {
				final String headlessLine = eatSdkHeader(reader, "Invalid " + SADMAN_PENCILMARKS_TAG + " section");
				pencilmarks = parseSdkPencilmarksSection(reader, headlessLine);
			}
			else if(isSdkStateSection(line)) {
				final String headlessLine = eatSdkHeader(reader, "Invalid " + SADMAN_STATE_TAG + " section");
				state = parseSdkPuzzleSection(reader, headlessLine);
			}
			else if(isSdkColoursSection(line)) {
				final String headlessLine = eatSdkHeader(reader, "Invalid " + SADMAN_COLOURS_TAG + " section");
				colors = parseSdkColoursSection(reader, headlessLine);
			}
			else if(isSdkPuzzleSection(line)) {
				final String headlessLine = eatSdkHeader(reader, "Invalid " + SADMAN_PUZZLE_TAG + " section");				
				puzzle = parseSdkPuzzleSection(reader, headlessLine);
			}
			else if(isHeadlessPuzzleSection(line)) {
				puzzle = parseSdkPuzzleSection(reader, line);
			}
			else {
				throw new UnsupportedPuzzleFormatException("Unknown or invalid puzzle format");
			}
		} while((line = reader.readLine()) != null);
		
		if(puzzle == null) {
			throw new UnsupportedPuzzleFormatException("No puzzle found");
		}
		
		//Update givens if [State] section is found
		if(state != null) {
			givens = new BitSet();
			for(int i = 0; i < puzzle.length; ++i) {
				//Find and set all givens
				if(puzzle[i] != 0 && state[i] == puzzle[i]) {
					givens.set(i);
				}
			}

			puzzle = state;
			state = null;
		}
		
		//Check whether we have a SadMan or a SudoCue type of puzzle
		if(pencilmarks != null || givens != null || colors != null || headers == null) {
			isSadmanPuzzleFormat = true;
		}
				
		final PuzzleBean response = new PuzzleBean(puzzle);
		response.setFormatType(isSadmanPuzzleFormat? FormatType.SADMAN_SUDOKU : FormatType.SUDOCUE_SUDOKU);
		response.setPencilmarks(pencilmarks);
		response.setHeaders(headers);
		response.setGivens(givens);
		response.setColors(colors);
		
		return response;
	}
	
	private String eatSdkHeader(final BufferedReader reader, final String errorMessage) 
			throws IOException, UnsupportedPuzzleFormatException {
		final String headlessLine = reader.readLine();
		if(headlessLine == null) {
			throw new UnsupportedPuzzleFormatException(errorMessage);
		}
		return headlessLine.trim();
	}
	
	private boolean isSdkPencilmarksSection(final String firstLine) {
		return firstLine.startsWith(SADMAN_PENCILMARKS_TAG);
	}
	
	private boolean isHeadlessPuzzleSection(final String firstLine) {
		return firstLine.length() == CLASSIC_PUZZLE_UNIT && verifyClipboardFormatRow(firstLine);
	}
	
	private boolean isSdkPuzzleSection(final String firstLine) {
		return firstLine.startsWith(SADMAN_PUZZLE_TAG);
	}
	
	private boolean isSdkColoursSection(final String firstLine) {
		return firstLine.startsWith(SADMAN_COLOURS_TAG);
	}
	
	private boolean isSdkStateSection(final String firstLine) {
		return firstLine.startsWith(SADMAN_STATE_TAG);
	}
	
	private boolean isSdkHeaderSection(final String firstLine) {
		return firstLine.charAt(0) == SADMAN_HEADER_TAG;
	}
	
	/* The following are the possible header fields in a SadMan Software Sudoku
		#A - Author
		#C - Comment
		#D - Description
		#B - Date
		#S - Source
		#U - URL
		#L - Level or Grade
		#N - Number of solutions
		#H - Number of clues
		#T - The time taken to reach the current state, in seconds
	 */
	private Map<String, String> parseSdkHeaderSection(final BufferedReader reader, final String firstLine) throws IOException {
		final Map<String, String> headers = new HashMap<String, String>();
		final int readAheadLimit = 5;
		
		String line = firstLine;
		
		do {
			line = line.trim();			
			final String headerName = String.valueOf(line.charAt(1));
			final String headerValue = line.substring(2).trim();
			
			headers.put(headerName, headerValue);
			
			//Peek at the next line to check for more header rows to parse						
			reader.mark(readAheadLimit);
			final char peekedChar = (char)reader.read();
			reader.reset();

			if(peekedChar != SADMAN_HEADER_TAG) {
				break;
			}
		} while((line = reader.readLine()) != null);
		
		return headers;
	}
	
	private int[] parseSdkColoursSection(final BufferedReader reader, final String firstLine) 
			throws IOException, UnsupportedPuzzleFormatException {		
		final int puzzleUnit = firstLine.length();
		final int[] colors = new int[puzzleUnit * puzzleUnit];
		final String errorMessage = "Invalid " + SADMAN_COLOURS_TAG + " section";
		
		String line = firstLine;
		int puzzleIndex = 0;
		
		for(int i = 0; i < puzzleUnit; ++i) {
			for(int j = 0; j < puzzleUnit; ++j) {
				final char currentChar = line.charAt(j);				
				if(Character.isDigit(currentChar)) {
					colors[puzzleIndex] = Character.getNumericValue(currentChar);
				}
				else if(currentChar != DOT_CHAR) {
					throw new UnsupportedPuzzleFormatException(errorMessage);
				}
				++puzzleIndex;
			}
			if(i == puzzleUnit - 1) {
				continue;
			}
			if((line = reader.readLine()) == null || (line = line.trim()).length() != CLASSIC_PUZZLE_UNIT) {
				throw new UnsupportedPuzzleFormatException(errorMessage);
			}
		}
		
		return colors;
	}
	
	private BitSet[][] parseSdkPencilmarksSection(final BufferedReader reader, final String firstLine) 
			throws IOException, UnsupportedPuzzleFormatException {		
		String[] rowPencilmarks = parseSdkPencilmarkRow(firstLine);
		final int puzzleUnit = rowPencilmarks.length;
		final BitSet[][] pencilmarks = new BitSet[puzzleUnit][puzzleUnit];
		
		for(int i = 0; i < pencilmarks.length; ++i) {
			for(int j = 0; j < pencilmarks[i].length; ++j) {
				pencilmarks[j][i] = new BitSet();
			}
		}
		
		String line = firstLine;
		int rowIndex = 0;
		
		do {
			line = line.trim();
			rowPencilmarks = parseSdkPencilmarkRow(line);
			for(int i = 0; i < rowPencilmarks.length; ++i) {
				final String cellPencilmarks = rowPencilmarks[i];
				if(!cellPencilmarks.isEmpty()) {
					//Extract pencilmarks for this cell
					for(int j = 0; j < cellPencilmarks.length(); ++j) {
						final int pencilmarkValue = Character.getNumericValue(cellPencilmarks.charAt(j));
						if(!(pencilmarkValue > 0 && pencilmarkValue <= puzzleUnit)) {
							throw new UnsupportedPuzzleFormatException("Invalid pencilmark value: " + pencilmarkValue);
						}
						pencilmarks[i][rowIndex].set(pencilmarkValue - 1);
					}
				}
			}
		} while(rowIndex++ < puzzleUnit - 1 && (line = reader.readLine()) != null && !line.isEmpty());
		
		return pencilmarks;
	}
	
	private String[] parseSdkPencilmarkRow(final String line) {
		final String[] pencilmarks = new String[CLASSIC_PUZZLE_UNIT];
		final int lineLength = line.length();

		for(int i = 0, j = 0; i < lineLength; ++i, ++j) {
			if(line.charAt(i) == PENCILMARK_SEPARATOR) {
				pencilmarks[j] = EMPTY_STRING;
				if(i == lineLength - 1) {
					pencilmarks[++j] = EMPTY_STRING;
				}
			}
			else {
				final StringBuilder sb = new StringBuilder();
				char currentChar;
				while(i < lineLength && Character.isDigit(currentChar = line.charAt(i))) {
					sb.append(currentChar);
					++i;
				}
				pencilmarks[j] = sb.toString();
				if(i == lineLength - 1 && line.charAt(lineLength - 1) == PENCILMARK_SEPARATOR) {
					pencilmarks[j + 1] = EMPTY_STRING;
				}
			}			
		}

		return pencilmarks;
	}
	
	private int[] parseSdkPuzzleSection(final BufferedReader reader, final String firstLine) 
			throws IOException, UnsupportedPuzzleFormatException {
		final int puzzleUnit = firstLine.length();
		final int[] puzzle = new int[puzzleUnit * puzzleUnit];
		
		String line = firstLine;
		int puzzleIndex = 0;
		int rowIndex = 0;
		
		do {
			for(int i = 0; i < puzzleUnit; ++i) {
				final char currentChar = line.charAt(i);
				if(Character.isDigit(currentChar)) {
					//Filled digit
					puzzle[puzzleIndex++] = Character.getNumericValue(currentChar);
				}
				else if(currentChar == DOT_CHAR){
					//Empty cell (represented by a dot '.')
					puzzle[puzzleIndex++] = 0;
				}
				else {
					throw new UnsupportedPuzzleFormatException("Invalid " + SADMAN_PUZZLE_TAG + " section");
				}
			}			
		} while(++rowIndex < puzzleUnit && (line = reader.readLine().trim()) != null && !line.isEmpty());
		
		return puzzle;
	}
	
	private PuzzleBean parseSimpleSudokuFormat(final BufferedReader reader, final String firstLine) 
			throws IOException, UnsupportedPuzzleFormatException {		
		//Determine puzzle dimension from the first row length
		final int puzzleUnit = firstLine.replace(SIMPLE_SUDOKU_COLUMN_SEPARATOR, EMPTY_STRING).length();
		final int[] puzzle = new int[puzzleUnit * puzzleUnit];
		
		String line = firstLine;
		int puzzleIndex = 0;
		
		do {
			if(line.startsWith(SIMPLE_SUDOKU_ROW_SEPARATOR) || line.trim().equals(EMPTY_STRING)) {
				continue;
			}
			parseSimpleSudokuRow(puzzle, line.trim(), puzzleIndex);
			puzzleIndex += puzzleUnit;
		} while((line = reader.readLine()) != null);
		
		if(puzzleIndex != puzzle.length) {
			throw new UnsupportedPuzzleFormatException("Invalid puzzle length");
		}
		
		final PuzzleBean response = new PuzzleBean(puzzle);
		response.setFormatType(FormatType.SIMPLE_SUDOKU);
		
		return response;
	}		
	
	private void parseSimpleSudokuRow(final int[] puzzle, final String line, final int puzzleIndex) 
			throws UnsupportedPuzzleFormatException {
		final char columnSeparator = SIMPLE_SUDOKU_COLUMN_SEPARATOR.charAt(0);
		for(int i = 0, j = puzzleIndex; i < line.length(); ++i) {
			final char currentChar = line.charAt(i);
			final int currentValue = Character.getNumericValue(currentChar);
			//Check whether we have a valid digit
			if(Character.isDigit(currentChar) && currentValue > 0) {
				puzzle[j++] = currentValue;
			}
			//Or whether we have a dot (indicating a non-filled cell)
			else if(currentChar == DOT_CHAR) {
				puzzle[j++] = 0;
			}
			else if(currentChar != columnSeparator) {
				throw new UnsupportedPuzzleFormatException("Invalid puzzle row format");
			}
		}
	}
}