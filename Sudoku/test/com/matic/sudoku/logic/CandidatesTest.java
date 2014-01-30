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

package com.matic.sudoku.logic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Candidate class.
 * @author vedran
 *
 */
public class CandidatesTest {
	
	private static final int DIMENSION = 3;
	private static final int UNIT = DIMENSION * DIMENSION;
	
	private int[][] emptyPuzzle;
	private int[][] partialPuzzle;
	private int[][] fullyFilledPuzzle;
	private Candidates expectedPartialCandidates;
	
	@Before
	public void setup() {
		emptyPuzzle = new int[UNIT][UNIT];
		
		partialPuzzle = formatPuzzle(new int[][] {
				{0,0,0,6,1,0,4,0,0},
				{0,0,0,7,0,0,0,9,0},
				{5,0,0,2,0,3,1,0,0},
				{0,1,0,0,0,0,9,0,6},
				{0,2,0,0,0,0,0,3,0},
				{9,0,8,0,0,0,0,1,0},
				{0,0,5,8,0,7,0,0,4},
				{0,8,0,0,0,6,0,0,0},
				{0,0,7,0,2,5,0,0,0}}
		);
		
		fullyFilledPuzzle = formatPuzzle(new int[][] {
				{8,3,2,6,1,9,4,5,7},
				{6,4,1,7,5,8,2,9,3},
				{5,7,9,2,4,3,1,6,8},
				{7,1,3,5,8,2,9,4,6},
				{4,2,6,9,7,1,8,3,5},
				{9,5,8,3,6,4,7,1,2},
				{1,6,5,8,9,7,3,2,4},
				{2,8,4,1,3,6,5,7,9},
				{3,9,7,4,2,5,6,8,1}}
		);
		
		expectedPartialCandidates = Candidates.fromStringArray(new String[][] {
				{"2378", "379", "239", "", "", "89", "", "2578", "23578"},
				{"123468", "346", "12346", "", "458", "48", "23568", "", "2358"},
				{"", "4679", "469", "", "489", "", "", "678", "78"},
				{"347", "", "34", "345", "34578", "248", "", "24578", ""},
				{"467", "", "46", "1459", "456789", "1489", "578", "", "578"},
				{"", "34567", "", "345", "34567", "24", "257", "", "257"},
				{"1236", "369", "", "", "39", "", "236", "26", ""},
				{"1234", "", "12349", "1349", "349", "", "2357", "257", "123579"},
				{"1346", "3469", "", "1349", "", "", "368", "68", "1389"}
		}, DIMENSION);
	}
	
	/**
	 * Test we get no candidates from a cell containing no candidates. 
	 */
	@Test
	public void testGetEmptyCell() {	
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		final int[] cellCandidates = unitUnderTest.getAsArray(5, 5);
		
		assertTrue(cellCandidates.length == 0);
	}
	
	/**
	 * Test correct candidates are returned from a partially filled cell.
	 */
	@Test
	public void testGetPartiallyFilledCell() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final int expectedValue = 3;
		
		final Set<Integer> cellCandidates = unitUnderTest.getAsSet(8, 6);
		
		assertEquals(expectedValue, cellCandidates.size());
		assertTrue(cellCandidates.contains(3));
		assertTrue(cellCandidates.contains(6));
		assertTrue(cellCandidates.contains(8));
	}
	
	/**
	 * Test we get correct candidates from a cell containing all possible candidates. 
	 */
	@Test
	public void testGetFullyFilledCell() {	
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		final int rowIndex = 6;
		final int colIndex = 6;
		
		for(int i = UNIT; i > 0; --i) {
			unitUnderTest.add(i, rowIndex, colIndex);
		}
		
		final int[] cellCandidates = unitUnderTest.getAsArray(rowIndex, colIndex);
		
		assertEquals(UNIT, cellCandidates.length);
		
		for(int i = 1; i <= UNIT; ++i) {
			assertTrue(unitUnderTest.contains(i, rowIndex, colIndex));
		}
	}
	
	/**
	 * Remove existing candidate from a cell and verify the candidate count afterwards.
	 */
	@Test
	public void testRemoveFromSingleCellMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = true;
		final int expectedCount = 2;
		
		final boolean actualValue = unitUnderTest.remove(2, 3, 5);
		final int actualCount = unitUnderTest.count(3, 5);
		
		assertEquals(expectedValue, actualValue);
		assertEquals(expectedCount, actualCount);
	}
	
	/**
	 * Remove non-existing candidate from a cell and verify the candidate count afterwards.
	 */
	@Test
	public void testRemoveFromSingleCellNoMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = false;
		final int expectedCount = 3;
		
		final boolean actualValue = unitUnderTest.remove(3, 3, 5);
		final int actualCount = unitUnderTest.count(3, 5);
		
		assertEquals(expectedValue, actualValue);
		assertEquals(expectedCount, actualCount);
	}
	
	/**
	 * Test removal of a candidate from an entire row.
	 */
	@Test
	public void testRemoveFromRowMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = true;
		final int row = 3;
		
		final boolean actualValue = unitUnderTest.removeFromRow(2, row);
		
		//Columns 5 and 7 should have candidate 2 removed, no others
		for(int i = 0; i < UNIT; ++i) {
			if(i == 5 || i == 7) {
				assertFalse(unitUnderTest.contains(2, row, i));
				assertEquals(expectedPartialCandidates.count(row, i)-1, unitUnderTest.count(row, i));
			}
			else {
				assertEquals(expectedPartialCandidates.count(row, i), unitUnderTest.count(row, i));
			}
		}
		
		assertEquals(expectedValue, actualValue);		
	}
	
	/**
	 * Test removal of a non-existent candidate from an entire row.
	 */
	@Test
	public void testRemoveFromRowNoMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = false;
		final int row = 7;
		
		//Row 7 doesn't contain candidate 6, try removing it		
		final boolean actualValue = unitUnderTest.removeFromRow(6, row);
		
		for(int i = 0; i < UNIT; ++i) {
			assertEquals(expectedPartialCandidates.count(row, i), unitUnderTest.count(row, i));
		}
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Test removal of a candidate from an entire column.
	 */
	@Test
	public void testRemoveFromColumnMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = true;
		final int candidate = 5;
		final int col = 6;
		
		//Candidate 5 in column 6 -> Match		
		final boolean actualValue = unitUnderTest.removeFromColumn(candidate, col);
		
		//Rows 1,4,5,7 should have candidate 5 removed, no others
		for(int i = 0; i < UNIT; ++i) {
			if(i == 1 || i == 4 || i == 5 || i == 7) {
				assertFalse(unitUnderTest.contains(candidate, i, col));
				assertEquals(expectedPartialCandidates.count(i, col)-1, unitUnderTest.count(i, col));
			}
			else {
				assertEquals(expectedPartialCandidates.count(i, col), unitUnderTest.count(i, col));
			}
		}
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Test removal of a non-existent candidate from an entire column.
	 */
	@Test
	public void testRemoveFromColumnNoMatch() {		
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = false;
		final int candidate = 4;
		final int col = 6;
		
		//Candidate 4 in column 6 -> No match
		
		//Row 7 doesn't contain candidate 6, try removing it		
		final boolean actualValue = unitUnderTest.removeFromColumn(candidate, col);
		
		for(int i = 0; i < UNIT; ++i) {
			assertEquals(expectedPartialCandidates.count(i, col), unitUnderTest.count(i, col));
		}
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Test removal of a candidate from an entire box.
	 */
	@Test
	public void testRemoveFromBoxMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = true;
		final int candidate = 6;
		final int boxX = 6;
		final int boxY = 6;
		
		final boolean actualValue = unitUnderTest.removeFromBox(candidate, boxX, boxY);
		
		for(int i = boxY; i < boxY + DIMENSION; ++i) {
			for(int j = boxX; j < boxX + DIMENSION; ++j) {
				if((i == 6 && j == 6) || (i == 6 && j == 7) ||
						(i == 8 && j == 6) || (i == 8 && j == 7)) {
					assertFalse(unitUnderTest.contains(candidate, i, j));
					assertEquals(expectedPartialCandidates.count(i, j)-1, unitUnderTest.count(i, j));
				}
				else {
					assertEquals(expectedPartialCandidates.count(i, j), unitUnderTest.count(i, j));
				}
			}
		}
		
		//Check nothing is removed from neighboring boxes
		for(int i = 0; i < UNIT; i += DIMENSION) {
			for(int j = 0; j < UNIT; j += DIMENSION) {
				if(i == boxY && j == boxX) {
					continue;
				}
				for(int k = i; k < i + DIMENSION; ++k) {
					for(int m = j; m < j + DIMENSION; ++m) {
						assertEquals(expectedPartialCandidates.count(k, m), unitUnderTest.count(k, m));
					}
				}
			}
		}
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Test removal of a non-existent candidate from an entire box.
	 */
	@Test
	public void testRemoveFromBoxNoMatch() {		
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = false;
		final int candidate = 4;
		final int boxX = 6;
		final int boxY = 6;
		
		final boolean actualValue = unitUnderTest.removeFromBox(candidate, boxX, boxY);
		
		for(int i = 0; i < UNIT; ++i) {
			for(int j = 0; j < UNIT; ++j) {
				assertEquals(expectedPartialCandidates.count(i, j), unitUnderTest.count(i, j));
			}
		}
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Simple test of removing a candidate from all of its regions (rows, cols and boxes).
	 */
	@Test
	public void testRemoveFromAllRegions() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		final boolean expectedValue = true;
		final int candidate = 2;
		final int rowIndex = 7;
		final int colIndex = 7;
		
		final boolean actualValue = unitUnderTest.removeFromAllRegions(candidate, rowIndex, colIndex);
		
		assertEquals(expectedValue, actualValue);
	}
	
	/**
	 * Test getting a correct first candidate value from a cell containing multiple.
	 */
	@Test
	public void testGetFirstMultipleEntries() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		final int expectedValue = 5;
		
		unitUnderTest.add(9, 4, 8);
		unitUnderTest.add(5, 4, 8);
		unitUnderTest.add(6, 4, 8);
		
		assertEquals(expectedValue, unitUnderTest.getFirst(4, 8));
	}
	
	/**
	 * Test retrieving first value from an empty cell.
	 */
	@Test
	public void testGetFirstNoEntries() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		final int expectedValue = -1;
		
		assertEquals(expectedValue, unitUnderTest.getFirst(4, 8));
	}
	
	/**
	 * Test obtaining the first value from a single entry cell
	 */
	@Test
	public void testGetFirstSingleEntry() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		final int expectedValue = 9;
		
		unitUnderTest.add(9, 4, 8);
		
		assertEquals(expectedValue, unitUnderTest.getFirst(4, 8));
	}
	
	/**
	 * Test an exception is raised when using invalid cell indexes.
	 */
	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void testUsingInvalidCellIndexes() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		unitUnderTest.contains(7, UNIT, UNIT);
	}
	
	/**
	 * Test an exception is raised when adding an invalid candidate value.
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testUsingInvalidCandidateValues() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		final int expectedValue = 0;
		
		unitUnderTest.add(-1, 3, 3);
		
		assertEquals(expectedValue, unitUnderTest.count(3, 3));
	}
	
	/**
	 * Test correct candidate count after adding entries to an empty cell
	 */
	@Test
	public void testAddToEmptyCell() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		final int expectedValue = 3;
		
		unitUnderTest.add(6, 1, 6);
		unitUnderTest.add(9, 1, 6);
		unitUnderTest.add(2, 1, 6);
		
		assertEquals(expectedValue, unitUnderTest.count(1, 6));
	}
	
	/**
	 * Test correct candidates count after adding new, unique entries.
	 */
	@Test
	public void testAddToPartiallyFilledCellNotExisting() {		
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		
		final int[] cellCandidates = {1,2,3,5,7,9};
		final int expectedValue = 8;
		
		for(int candidate : cellCandidates) {
			assertTrue(unitUnderTest.contains(candidate, 7, 8));
		}
		
		unitUnderTest.add(4, 7, 8);
		unitUnderTest.add(6, 7, 8);
		
		assertEquals(expectedValue, unitUnderTest.count(7, 8));
	}
	
	/**
	 * Test uniqueness of a cell's candidates (no duplicate entries are allowed)
	 */
	@Test
	public void testAddToPartiallyFilledCellAlreadyExists() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		
		final int[] cellCandidates = {2,3,7,8};
		final int expectedValue = 4;
		
		for(int candidate : cellCandidates) {
			assertTrue(unitUnderTest.contains(candidate, 0, 0));
		}
		
		unitUnderTest.add(3, 0, 0);
		unitUnderTest.add(8, 0, 0);
		
		assertEquals(expectedValue, unitUnderTest.count(0, 0));
	}
	
	/**
	 * Verify that no candidates are populated when creating empty candidates.
	 */
	@Test
	public void testCreateEmptyCandidates() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		final int expectedValue = 0;
		
		for(int i = 0; i < UNIT; ++i) {
			for(int j = 0; j < UNIT; ++j) {
				assertEquals(expectedValue, unitUnderTest.count(i, j));
			}
		}
	}
	
	/**
	 * Check that all possibilities are added to all of the cells for an empty board
	 */
	@Test
	public void testPopulateFromEmptyPuzzle() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, emptyPuzzle);
		
		final int expectedValue = UNIT;
		
		for(int i = 0; i < UNIT; ++i) {
			for(int j = 0; j < UNIT; ++j) {
				assertEquals(expectedValue, unitUnderTest.count(i, j));
			}
		}
	}
	
	/**
	 * Test that possible candidates are correct after filtering from a puzzle
	 */
	@Test
	public void testPopulateFromPartiallyFilledPuzzle() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, partialPuzzle);
		
		for(int i = 0; i < partialPuzzle.length; ++i) {
			for(int j = 0; j < partialPuzzle[i].length; ++j) {
				assertArrayEquals(expectedPartialCandidates.getAsArray(j, i), unitUnderTest.getAsArray(j, i));
			}
		}
	}
	
	/**
	 * Test that no candidates exist after filtering from a fully filled puzzle
	 */
	@Test
	public void testPopulateFromFullyFilledPuzzle() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, fullyFilledPuzzle);
		
		final int expectedCount = 0;
		
		for(int i = 0; i < fullyFilledPuzzle.length; ++i) {
			for(int j = 0; j < fullyFilledPuzzle[i].length; ++j) {
				assertEquals(expectedCount, unitUnderTest.count(i, j));
			}
		}
	}
	
	/**
	 * Populate a cell with all candidates, clear them, and verify they are no longer valid.
	 */
	@Test
	public void testClearFullyPopulatedCell() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, emptyPuzzle);
		
		//Populate a cell with all possible candidates
		for(int i = 1; i <= UNIT; ++i) {
			unitUnderTest.add(i, 8, 2);
		}
		
		//Clear all of the populated candidates
		unitUnderTest.clear(8, 2);
		
		final int expectedCount = 0;
		
		//Check that no candidates exist in the cell anymore
		assertEquals(expectedCount, unitUnderTest.count(8, 2));
		
		//Check for each candidate separately that it no longer exists in the cell
		for(int i = 1; i <= UNIT; ++i) {
			assertFalse(unitUnderTest.contains(i, 8, 2));
		}
	}
	
	/**
	 * Populate a cell with some candidates, clear them, and verify they are no longer valid.
	 */
	@Test
	public void testClearPartiallyPopulatedCell() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, emptyPuzzle);
		
		//Populate a cell with some possible candidates
		for(int i = 1; i <= UNIT; i += 2) {
			unitUnderTest.add(i, 0, 8);
		}
		
		//Clear all of the populated candidates
		unitUnderTest.clear(0, 8);
				
		final int expectedCount = 0;
		
		//Check that no candidates exist in the cell anymore
		assertEquals(expectedCount, unitUnderTest.count(0, 8));
				
		//Check for each candidate separately that it no longer exists in the cell
		for(int i = 1; i <= UNIT; i += 2) {
			assertFalse(unitUnderTest.contains(i, 0, 8));
		}
	}
	
	/**
	 * Clear an already empty cell, check that nothing weird happens as a result.
	 */
	@Test
	public void testClearNotPopulatedCell() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		final int expectedCount = 0;
		
		assertEquals(expectedCount, unitUnderTest.count(2, 2));
		
		unitUnderTest.clear(2, 2);
		
		assertEquals(expectedCount, unitUnderTest.count(2, 2));
	}
	
	/**
	 * Add all possible candidates to a cell and check that it contains every one of them.
	 */
	@Test
	public void testContainsCandidateMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		//Populate a cell with all possible candidates
		for(int i = 1; i <= UNIT; ++i) {
			unitUnderTest.add(i, 4, 3);
		}
		
		//Verify that each candidate is contained by the cell
		for(int i = 1; i <= UNIT; ++i) {
			assertTrue(unitUnderTest.contains(i, 4, 3));
		}
	}
	
	/**
	 * Add some candidates to a cell and verify no other candidates are contained by a cell.
	 */
	@Test
	public void testContainsCandidateNoMatch() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		unitUnderTest.add(1, 7, 0);
		unitUnderTest.add(2, 7, 0);
		unitUnderTest.add(9, 7, 0);
		
		for(int i = 4; i < UNIT; ++i) {
			assertFalse(unitUnderTest.contains(i, 7, 0));
		}
	}
	
	/**
	 * Check that the count for cells containing no candidates always returns zero
	 */
	@Test
	public void testZeroCount() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		final int count1 = unitUnderTest.count(1, 4);
		final int count2 = unitUnderTest.count(8, 7);
		final int count3 = unitUnderTest.count(4, 0);
		
		final int expectedCount = 0;
		
		assertEquals(expectedCount, count1);
		assertEquals(expectedCount, count2);
		assertEquals(expectedCount, count3);
	}
	
	/**
	 * Check counting of multiple candidates contained in a cell is correct.
	 */
	@Test
	public void testMultiCount() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		unitUnderTest.add(3, 1, 4);
		unitUnderTest.add(5, 1, 4);
		unitUnderTest.add(9, 1, 4);
		
		final int count = unitUnderTest.count(1, 4);
		
		final int expectedCount = 3;
		
		assertEquals(expectedCount, count);
	}
	
	/**
	 * Verify that the candidate count for a single candidate in a cell returns one. 
	 */
	@Test
	public void testSingleCount() {
		final Candidates unitUnderTest = new Candidates(DIMENSION, true);
		
		unitUnderTest.add(3, 1, 4);
		unitUnderTest.add(5, 8, 7);
		unitUnderTest.add(9, 4, 0);
		
		final int count1 = unitUnderTest.count(1, 4);
		final int count2 = unitUnderTest.count(8, 7);
		final int count3 = unitUnderTest.count(4, 0);
		
		final int expectedCount = 1;
		
		assertEquals(expectedCount, count1);
		assertEquals(expectedCount, count2);
		assertEquals(expectedCount, count3);		
	}
	
	private int[][] formatPuzzle(final int[][] puzzle) {
		final int[][] formatted = new int[puzzle.length][puzzle.length];
		for(int i = 0; i < puzzle.length; ++i) {
			for(int j = 0; j < puzzle[i].length; ++j) {
				formatted[j][i] = puzzle[i][j];
			}
		}
		return formatted;
	}
}

