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
		expectedSudocueHeaders.put("B", "2014-02-10");
		expectedSudocueHeaders.put("S", "SadMan Software Sudoku");
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
		
		assertNotNull(actual.getPencilmarks());		
		
		final int[] colors = actual.getColors();
		assertNotNull(colors);
		assertArrayEquals(expectedColors, colors);
		
		//TODO: Check contents of pencilmarks , not only != null
	}
	
	private boolean assertGivens(final BitSet givens) {
		final int[] givenIndexes = {0,1,2,3,4,7,8,10,14,15,19,23,27,29,38,42,
				51,53,57,61,65,66,70,72,73,76,77,78,79,80};
		
		for(int i = 0; i < givenIndexes.length; ++i) {
			if(!givens.get(givenIndexes[i])) {
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
	
	private static void assertFilesEqual(File expected, File actual) throws IOException {
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
