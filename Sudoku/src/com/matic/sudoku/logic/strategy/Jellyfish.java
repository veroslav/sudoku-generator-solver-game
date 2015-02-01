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

public class Jellyfish extends Fish {

	private static final String STRATEGY_NAME = "Jellyfish";
	private static final int SCORE = 20000;

	public Jellyfish(int dimension) {
		super(dimension);		
	}
	
	@Override
	public int getScore() {		
		return SCORE;
	}

	@Override
	protected Subset getSubset(int strategy) {
		final Subset subsetStrategy = strategy == Fish.NAKED_STRATEGY?
				new NakedSubset(dimension, NakedSubset.NAKED_QUADS) :
				new HiddenSubset(dimension, HiddenSubset.HIDDEN_QUADS);	
		return subsetStrategy;
	}

	@Override
	public String getName() {
		return STRATEGY_NAME;
	}
	
	@Override
	public String asHint() {
		return null;
	}
}
