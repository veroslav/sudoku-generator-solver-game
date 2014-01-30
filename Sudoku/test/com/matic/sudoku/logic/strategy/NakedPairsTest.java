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

package com.matic.sudoku.logic.strategy;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.logic.strategy.NakedSubset;
import com.matic.sudoku.logic.strategy.Subset;

/**
 * Unit tests for Naked Pairs method logic.
 * @author vedran
 *
 */
public class NakedPairsTest {
	
	private final static int DIMENSION = 3;
	
	private Candidates noNakedPairsCandidates = null; 		
	
	@Before
	public void setup() {		
		noNakedPairsCandidates = Candidates.fromStringArray(new String[][] {
				{"46", "", "12", "", "", "", "19", "129", "46"},
				{"", "", "", "", "", "", "", "", ""},
				{"", "", "12", "", "", "", "", "12", ""},
				{"46", "", "", "", "", "56", "", "45", ""},
				{"", "", "", "", "", "", "", "", ""},
				{"", "", "", "", "", "", "", "", ""},
				{"78", "", "56", "", "49", "567", "189", "19", "46"},
				{"78", "", "", "69", "", "67", "89", "", ""},
				{"", "", "56", "69", "49", "", "", "45", ""}
		}, DIMENSION);
	}

	@Test
	public void testNakedPairIterateRowIsFoundAndNotFiltered() {
		final Subset unitUnderTest = new NakedSubset(DIMENSION, NakedSubset.NAKED_PAIRS);
		
		unitUnderTest.setCandidates(noNakedPairsCandidates);
		
		boolean actualValue = unitUnderTest.iterateRows();
		
		assertFalse(actualValue);
	}
}
