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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.matic.sudoku.io.FileFormatManager.FormatType;

public class FileFormatManagerTest {
	
	private final FileFormatManager unitUnderTest = new FileFormatManager();
	private final Map<String, String> expectedSadmanHeaders = new HashMap<String, String>();
	private final Map<String, String> expectedSudocueHeaders = new HashMap<String, String>();
	
	private final int[] expectedPuzzle = {3,8,5,6,1,0,0,7,2,0,7,0,0,0,2,8,0,0,0,6,0,0,0,3,0,0,0,
								6,0,2,0,0,0,0,0,0,0,0,8,0,0,0,5,0,0,0,0,0,0,0,0,9,0,6,
								0,0,0,9,0,0,0,8,0,0,0,9,1,0,0,0,5,0,1,2,0,0,8,5,6,3,9};
	private final int[] expectedState = {3,8,5,6,1,9,4,7,2,9,7,0,0,0,2,8,6,0,0,6,4,0,7,3,0,0,0,
			6,0,2,0,0,0,3,0,8,4,0,8,0,0,0,5,0,0,0,0,0,0,0,8,9,0,6,
			0,0,6,9,0,0,0,8,0,0,0,9,1,0,0,0,5,4,1,2,0,0,8,5,6,3,9};
	private final int[] expectedColors = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,2,1,1,1,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	private final int[] expectedGivenIndexes = {0,1,2,3,4,7,8,10,14,15,19,23,27,29,38,42,
			51,53,57,61,65,66,70,72,73,76,77,78,79,80};
	
	private final int[] expectedPencilmarkIndexes = {5,6,9,11,12,13,16,17,
			18,20,21,22,24,25,26,28,30,31,32,33,34,35,36,37,39,40,41,43,44,
			45,46,47,48,49,50,52,54,55,56,58,59,60,62,63,64,67,68,69,71,74,75};
	
	private final String[] expectedPencilmarks = {
			"49","4","49","14","45","459","1469","1345",
			"249","14","4578","4579","14","149","145"
			,"13459","34578","34579","14789","1347","14","13478",
			"479","1349","2347","234679","14679","124","1347",
			"457","1345","1347","234578","23457","1478","124",
			"457","345","3467","23467","467","1247","147",
			"478","34","23467","467","247","47",
			"47","47"	
	};

	@Before
	public void setup() {
		expectedSadmanHeaders.put("A", "SadMan Software");
		expectedSadmanHeaders.put("C", "watch for the naked pair");
		expectedSadmanHeaders.put("D", "Straightforward enough");
		expectedSadmanHeaders.put("B", "2014-02-10");
		expectedSadmanHeaders.put("S", "SadMan Software Sudoku");
		expectedSadmanHeaders.put("U", "http://www.sadmansoftware.com/sudoku/");
		expectedSadmanHeaders.put("L", "Mild");
		expectedSadmanHeaders.put("N", "1");
		expectedSadmanHeaders.put("H", "30");
		expectedSadmanHeaders.put("T", "30");
		
		expectedSudocueHeaders.put("A", "SadMan Software");
		expectedSudocueHeaders.put("D", "A random puzzle created by SudoCue");
		expectedSudocueHeaders.put("C", "Just start plugging in the numbers");
		expectedSudocueHeaders.put("B", "2014-02-10");
		expectedSudocueHeaders.put("S", "SadMan Software Sudoku");
		expectedSudocueHeaders.put("L", "Easy");
		expectedSudocueHeaders.put("U", "http://www.sadmansoftware.com/sudoku/");
	}
	
	@Test
	public void testWriteSimpleFormat() throws Exception {
		final PuzzleBean puzzleBean = new PuzzleBean(expectedPuzzle);
		final File actualFile = new File("./target/simple_format_output.txt");
		final File expectedFile = new File("./test/resources/reference_simple_format.txt");
		
		unitUnderTest.write(actualFile, puzzleBean, FormatType.SIMPLE_FORMAT);
		
		assertFilesEqual(expectedFile, actualFile);		
		assertTrue(actualFile.delete());
	}
	
	@Test
	public void testParseSimpleFormat() throws Exception {
		final File inputFile = new File("./test/resources/reference_simple_format.txt");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedPuzzle, actual.getPuzzle());
		assertNull(actual.getPencilmarks());
		assertNull(actual.getHeaders());	
		assertNull(actual.getColors());
		assertNull(actual.getGivens());
	}
	
	@Test
	public void testParseSimpleFormatFromString() throws Exception {
		final String input = "38561..72.7...28...6...3...6.2........8..." +
				"5........9.6...9...8...91...5.12..85639";
		
		final PuzzleBean actual = unitUnderTest.fromString(input);
		
		assertArrayEquals(expectedPuzzle, actual.getPuzzle());
		assertNull(actual.getPencilmarks());
		assertNull(actual.getHeaders());	
		assertNull(actual.getColors());
		assertNull(actual.getGivens());
	}
	
	@Test
	public void testWriteSimpleSudoku() throws Exception {
		final PuzzleBean puzzleBean = new PuzzleBean(expectedPuzzle);
		final File actualFile = new File("./target/simple_sudoku_output.ss");
		final File expectedFile = new File("./test/resources/reference_simple_sudoku.ss");
		
		unitUnderTest.write(actualFile, puzzleBean, FormatType.SIMPLE_SUDOKU);
		
		assertFilesEqual(expectedFile, actualFile);		
		assertTrue(actualFile.delete());
	}
	
	@Test
	public void testParseSimpleSudoku() throws Exception {
		final File inputFile = new File("./test/resources/reference_simple_sudoku.ss");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedPuzzle, actual.getPuzzle());
		assertNull(actual.getPencilmarks());
		assertNull(actual.getHeaders());
		assertNull(actual.getColors());
		assertNull(actual.getGivens());
	}
	
	@Test
	public void testWriteSudocueSudoku() throws Exception {
		final PuzzleBean puzzleBean = new PuzzleBean(expectedPuzzle);
		puzzleBean.setHeaders(expectedSudocueHeaders);
		
		final File actualFile = new File("./target/sudocue_sudoku_output.sdk");
		final File expectedFile = new File("./test/resources/reference_sudocue_sudoku.sdk");
		
		unitUnderTest.write(actualFile, puzzleBean, FormatType.SUDOCUE_SUDOKU);
		
		assertFilesEqual(expectedFile, actualFile);		
		assertTrue(actualFile.delete());
	}
	
	@Test
	public void testParseSudocueSudoku() throws Exception {
		final File inputFile = new File("./test/resources/reference_sudocue_sudoku.sdk");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedPuzzle, actual.getPuzzle());
		
		final Map<String, String> headers = actual.getHeaders();
		assertNotNull(headers);
		assertEquals(expectedSudocueHeaders.size(), headers.size());
		assertTrue(assertHeaders(expectedSudocueHeaders, headers));
		
		assertNull(actual.getPencilmarks());
		assertNull(actual.getColors());
		assertNull(actual.getGivens());
	}
	
	@Test
	public void testWriteFullSadmanSudoku() throws Exception {
		final PuzzleBean puzzleBean = new PuzzleBean(expectedState);
		puzzleBean.setHeaders(expectedSadmanHeaders);
		puzzleBean.setPencilmarks(getPencilmarks());
		puzzleBean.setColors(expectedColors);
		puzzleBean.setGivens(getGivens());
		
		final File actualFile = new File("./target/sadman_full_sudoku_output.sdk");
		final File expectedFile = new File("./test/resources/reference_complete_sadman_sudoku.sdk");
		
		unitUnderTest.write(actualFile, puzzleBean, FormatType.SADMAN_SUDOKU);
		
		assertFilesEqual(expectedFile, actualFile);		
		assertTrue(actualFile.delete());
	}
	
	@Test
	public void testParseMinimalSadmanSudoku() throws Exception {
		final File inputFile = new File("./test/resources/reference_minimal_sadman_sudoku.sdk");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedPuzzle, actual.getPuzzle());
		assertNull(actual.getPencilmarks());
		assertNull(actual.getHeaders());
		assertNull(actual.getColors());
		assertNull(actual.getGivens());
	}
	
	@Test
	public void testParseHeadersAndPuzzleSadmanSudoku() throws Exception {
		final File inputFile = new File("./test/resources/reference_headers_and_puzzle_sadman_sudoku.sdk");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedPuzzle, actual.getPuzzle());
		
		final Map<String, String> headers = actual.getHeaders();
		assertNotNull(headers);
		assertEquals(expectedSadmanHeaders.size(), headers.size());
		assertTrue(assertHeaders(expectedSadmanHeaders, headers));
		
		assertNull(actual.getPencilmarks());		
		assertNull(actual.getColors());
		assertNull(actual.getGivens());
	}
	
	@Test
	public void testParseNoPencilmarksAndColorsSadmanSudoku() throws Exception {
		final File inputFile = new File("./test/resources/reference_no_pencilmarks_and_colors_sadman_sudoku.sdk");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedState, actual.getPuzzle());
		
		final Map<String, String> headers = actual.getHeaders();
		assertNotNull(headers);
		assertEquals(expectedSadmanHeaders.size(), headers.size());
		assertTrue(assertHeaders(expectedSadmanHeaders, headers));	
		
		final BitSet givens = actual.getGivens();
		assertNotNull(givens);		
		assertEquals(30, givens.cardinality());
		assertTrue(assertGivens(givens));
		
		assertNull(actual.getPencilmarks());
		assertNull(actual.getColors());
	}
	
	@Test
	public void testParseCompleteSadmanSudoku() throws Exception {
		final File inputFile = new File("./test/resources/reference_complete_sadman_sudoku.sdk");
		
		final PuzzleBean actual = unitUnderTest.fromFile(inputFile);
		
		assertArrayEquals(expectedState, actual.getPuzzle());
		
		final Map<String, String> headers = actual.getHeaders();
		assertNotNull(headers);
		assertEquals(expectedSadmanHeaders.size(), headers.size());
		assertTrue(assertHeaders(expectedSadmanHeaders, headers));
		
		final BitSet givens = actual.getGivens();
		assertNotNull(givens);		
		assertEquals(30, givens.cardinality());
		assertTrue(assertGivens(givens));
		
		final BitSet[][] pencilmarks = actual.getPencilmarks(); 
		assertNotNull(pencilmarks);		
		assertTrue(assertPencilmarks(pencilmarks));
		
		final int[] colors = actual.getColors();
		assertNotNull(colors);
		assertArrayEquals(expectedColors, colors);		
	}
	
	private BitSet[][] getPencilmarks() {
		final BitSet[][] pencilmarks = new BitSet[9][9];
		int pencilmarkIndex = 0;
		int expectedIndex = 0;
		
		for(int i = 0; i < pencilmarks.length; ++i) {
			for(int j = 0; j < pencilmarks[i].length; ++j) {
				pencilmarks[j][i] = new BitSet();
				if(expectedIndex < expectedPencilmarkIndexes.length && 
						expectedPencilmarkIndexes[expectedIndex] == pencilmarkIndex++) {
					final String cellPencilmarks = expectedPencilmarks[expectedIndex];
					for(int k = 0; k < cellPencilmarks.length(); ++k) {
						pencilmarks[j][i].set(Character.getNumericValue(cellPencilmarks.charAt(k)) - 1);
					}
					++expectedIndex;
				}
			}
		}
		
		return pencilmarks;
	}
	
	private BitSet getGivens() {
		final BitSet givens = new BitSet();
		for(final int givenIndex : expectedGivenIndexes) {
			givens.set(givenIndex);
		}
		return givens;
	}
	
	private boolean assertPencilmarks(final BitSet[][] pencilmarks) {
		int expectedIndex = 0;
		int actualCounter = 0;
		for(int i = 0; i < pencilmarks.length; ++i) {
			for(int j = 0; j < pencilmarks[i].length; ++j) {
				final BitSet cellPencilmarks = pencilmarks[j][i];
				if(expectedIndex < expectedPencilmarkIndexes.length &&
						expectedPencilmarkIndexes[expectedIndex] == actualCounter) {					
					final String expectedPencilmark = expectedPencilmarks[expectedIndex];					
					if(cellPencilmarks.cardinality() != expectedPencilmark.length()) {
						return false;
					}
					for(int k = 0; k < expectedPencilmark.length(); ++k) {
						if(!cellPencilmarks.get(Character.getNumericValue(expectedPencilmark.charAt(k)) - 1)) {
							return false;
						}
					}
					++expectedIndex;
				}
				else if(cellPencilmarks.cardinality() != 0) {
					return false;
				}
				++actualCounter;
			}
		}
		return true;
	}
	
	private boolean assertGivens(final BitSet givens) {		
		for(int i = 0; i < expectedGivenIndexes.length; ++i) {
			if(!givens.get(expectedGivenIndexes[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean assertHeaders(final Map<String, String> expectedHeaders, final Map<String, String> actualHeaders) {
		final Set<String> headerNames = expectedHeaders.keySet();
		
		for(final String headerName : headerNames) {
			if(!expectedHeaders.get(headerName).equals(actualHeaders.get(headerName))) {
				return false;
			}
		}
		
		return true;
	}
	
	private void assertFilesEqual(File expected, File actual) throws IOException {
		BufferedReader expectedReader = null;
		BufferedReader actualReader = null;
		
		try {
			expectedReader = new BufferedReader(new FileReader(expected));
			actualReader = new BufferedReader(new FileReader(actual));
			
			String expectedLine = null;
			
			while((expectedLine = expectedReader.readLine()) != null) {
			    final String actualLine = actualReader.readLine();
			    
			    assertNotNull("Expected had more lines then the actual.", actualLine);
			    assertEquals(expectedLine, actualLine);
			  }
			  assertNull("Actual had more lines then the expected.", actualReader.readLine());
		}
		finally {
			if(expectedReader != null) {
				try {
					expectedReader.close();
				}
				catch(IOException e) {}
			}
			if(actualReader != null) {
				try {
					actualReader.close();
				}
				catch(IOException e) {}
			}
		}		  		  
	}
}
