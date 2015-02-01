/*
* This file is part of SuDonkey, an open-source Sudoku puzzle game generator and solver.
* Copyright (C) 2015 Vedran Matic
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

package com.matic.sudoku.solver;

import java.util.List;

/**
 * A hint on how next solution step might be revealed.
 * 
 * @author vedran
 *
 */
public class Hint {
	
	private final List<Pair> affectedCells;
	private final String strategyName;
	private final String description; 

	/**
	 * Init a hint with it's cell locations and a short description
	 * 
	 * @param affectedCells Which cells are affected by the hint
	 * @param description A short description of the hint
	 * @param strategyName Required strategy
	 */
	public Hint(final List<Pair> affectedCells, final String description, 
			final String strategyName) {
		this.affectedCells = affectedCells;
		this.strategyName = strategyName;
		this.description = description;
	}

	public List<Pair> getAffectedCells() {
		return affectedCells;
	}

	public String getDescription() {
		return description;
	}
	
	public String getStrategyName() {
		return strategyName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((affectedCells == null) ? 0 : affectedCells.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((strategyName == null) ? 0 : strategyName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hint other = (Hint) obj;
		if (affectedCells == null) {
			if (other.affectedCells != null)
				return false;
		} else if (!affectedCells.equals(other.affectedCells))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (strategyName == null) {
			if (other.strategyName != null)
				return false;
		} else if (!strategyName.equals(other.strategyName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Hint [affectedCells=" + affectedCells + ", strategyName="
				+ strategyName + ", description=" + description + "]";
	}	
}
