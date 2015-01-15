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

package com.matic.sudoku.generator;

import com.matic.sudoku.Resources;
import com.matic.sudoku.solver.BruteForceSolver;
import com.matic.sudoku.solver.LogicSolver;
import com.matic.sudoku.solver.LogicSolver.Grading;

/**
 * A generic implementation of a puzzle generator.
 * @author vedran
 *
 */
public abstract class Generator {
	
	public enum Symmetry {
		NONE(Resources.getTranslation("symmetry.none")), 
		ROTATIONAL_180(Resources.getTranslation("symmetry.rotational")), 
		VERTICAL_MIRRORING(Resources.getTranslation("symmetry.vertical_mirroring")),
		HORIZONTAL_MIRRORING(Resources.getTranslation("symmetry.horizontal_mirroring")), 
		DIAGONAL(Resources.getTranslation("symmetry.diagonal")), 
		ANTI_DIAGONAL(Resources.getTranslation("symmetry.anti_diagonal"));
		
		private final String description;
		
		Symmetry(final String type) {
			this.description = type;
		}
		
		public String getDescription() {
			return description;
		}
		
		public static Symmetry fromString(final String symmetry) {
			if (symmetry != null) {
				for (final Symmetry type : Symmetry.values()) {
					if (type.description.equals(symmetry)) {
						return type;
					}
				}
			}
			throw new IllegalArgumentException("No symmetry type with description "
					+ symmetry + " found");
		}
		
		public static Symmetry getRandom() {
			return values()[(int)(Math.random() * values().length)];
		}
	}
	
	public static final int MAX_ITERATIONS = 20;
	
	protected BruteForceSolver bruteForceSolver;
	protected LogicSolver logicSolver;
	
	protected int maxIterations;
	protected int dimension;
	protected int unit;
	protected int grid;
	
	protected Generator(int dimension, int maxIterations) {
		this.dimension = dimension;
		this.maxIterations = maxIterations;
		
		bruteForceSolver = null;
		
		unit = dimension * dimension;
		grid = unit * unit;
	}
	
	public void setBruteForceSolver(final BruteForceSolver bruteForceSolver) {
		this.bruteForceSolver = bruteForceSolver;
	}
	
	public void setLogicSolver(final LogicSolver logicSolver) {
		this.logicSolver = logicSolver;
	}
	
	/**
	 * Create a new puzzle with a given difficulty grading and type of symmetry, if any.
	 * @param grading Target difficulty grading for generated puzzle
	 * @param symmetry Target board symmetry for generated puzzle 
	 * @return A new puzzle and it's solution, or null if no such was possible to generate within generator constraints
	 */
	public abstract GeneratorResult createNew(final Grading grading, final Symmetry symmetry);
}