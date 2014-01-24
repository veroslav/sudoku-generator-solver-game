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

package com.matic.sudoku.solver;

import com.matic.sudoku.logic.Candidates;
import com.matic.sudoku.logic.strategy.HiddenSingles;
import com.matic.sudoku.logic.strategy.HiddenSubset;
import com.matic.sudoku.logic.strategy.Jellyfish;
import com.matic.sudoku.logic.strategy.LockedCandidates;
import com.matic.sudoku.logic.strategy.LogicStrategy;
import com.matic.sudoku.logic.strategy.NakedSingles;
import com.matic.sudoku.logic.strategy.NakedSubset;
import com.matic.sudoku.logic.strategy.SimpleSingles;
import com.matic.sudoku.logic.strategy.SlicingAndSlotting;
import com.matic.sudoku.logic.strategy.Swordfish;
import com.matic.sudoku.logic.strategy.XWing;

/**
 * A sudoku solver that applies logical steps in order to solve a puzzle
 * @author vedran
 *
 */
public class LogicSolver {

	//TODO: Keep DIMENSION, UNIT and GRID measurements in one place only (static Board.getDimension() for instance)
	//TODO: Avoid unnecessary creations of Collection<Integer>, use int[] instead when possibly for efficiency
	
	// Puzzle validation error constant
	public static final int INVALID_PUZZLE = -1;

	// Puzzle solution constants
	public static final int NO_SOLUTION = 0;
	public static final int UNIQUE_SOLUTION = 1;

	// Puzzle grading constants
	public enum Grading {
		EASY("Easy"), MODERATE("Moderate"), HARD("Hard"), 
		EXPERT("Expert"), DIABOLIC("Diabolic");
		
		private final String description;
		
		Grading(final String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
		public static Grading fromString(final String grading) {
			if(grading != null) {
				for(final Grading g : Grading.values()) {
					if(g.description.equals(grading)) {
						return g;
					}
				}
			}
			throw new IllegalArgumentException("No grading with description " + grading + " found");
		}
	}
	
	//TODO: Determine real grading scores for Expert and Diabolic puzzles
	//Difficulty grading thresholds
	private static final double MODERATE_THRESHOLD = 70;
	private static final double HARD_THRESHOLD = 580;
	private static final double EXPERT_THRESHOLD = 2800;
	private static final double DIABOLIC_THRESHOLD = 20000;

	private final int dimension;
	private final int unit;
	
	//Indicates most advanced strategy level required for solving a puzzle
	private int highestLevelRequired;
	
	//Highest strategy level allowed under current grading
	private int maxGradingLevel;
		
	//After a unique solution is found by solve(), this contains the puzzle difficulty grading
	private Grading grading;
	
	private final LogicStrategy[] solutionStrategies;
	
	private Candidates candidates;
	
	public LogicSolver(final int dimension) {
		this.dimension = dimension;
		unit = dimension * dimension;
		grading = Grading.DIABOLIC;
		
		candidates = null;				
	
		final LogicStrategy[] strategies = {
				new SimpleSingles(dimension),
				new SlicingAndSlotting(dimension),
				new NakedSingles(dimension),
				new HiddenSingles(dimension),
				new LockedCandidates(dimension),
				new NakedSubset(dimension, NakedSubset.NAKED_PAIRS),
				new HiddenSubset(dimension, HiddenSubset.HIDDEN_PAIRS),
				new NakedSubset(dimension, NakedSubset.NAKED_TRIPLES),
				new HiddenSubset(dimension, HiddenSubset.HIDDEN_TRIPLES),
				new NakedSubset(dimension, NakedSubset.NAKED_QUADS),
				new HiddenSubset(dimension, HiddenSubset.HIDDEN_QUADS),
				new XWing(dimension),
				new Swordfish(dimension),
				new Jellyfish(dimension)
		};
		solutionStrategies = strategies;		
		maxGradingLevel = solutionStrategies.length - 1;
	}
	
	/**
	 * Given a target grading, set highest allowed strategy level
	 * @param grading Grading
	 */
	public void setMaxGradingLevel(final Grading grading) {
		switch(grading) {
		case EASY:
			maxGradingLevel = 2;	//Naked Singles
			break;
		case MODERATE:
			maxGradingLevel = 4;	//Locked Candidates
			break;
		case HARD:
			maxGradingLevel = 6;	//Hidden Pairs
			break;
		case EXPERT:
			maxGradingLevel = 10;	//Hidden Quads
			break;
		default:
			maxGradingLevel = solutionStrategies.length - 1; 
		}
	}

	/** Get the current state of candidates
		@return current candidates or null if no puzzle solving attempt was made so far
	 */
	public Candidates getCandidates() {
		return candidates;
	}
	
	/**
	 * If a puzzle has a unique solution when solved, this method gives the estimated
	 * puzzle difficulty for that puzzle.
	 * @return Puzzle grading
	 */
	public Grading getGrading() {
		return grading;
	}
	
	/**
	 * Solve and grade a puzzle's difficulty depending on used solving strategies.
	 * @param puzzle Puzzle to grade
	 * @return Whether the puzzle has unique, multiple, or no solutions
	 */
	public int solve(final int[][] puzzle) {		
		/*TODO: Check if empty board/invalid length, before solving (emptyCellsLeft) 
				(Only when "Grading" requested by player)*/		
		int emptyCellsLeft = 0;		
		for(int i = 0; i < unit; ++i) {
			for(int j = 0; j < unit; ++j) {
				//Count all empty cells so that we know when we have solved a puzzle
				//(we have when there are no empty cells left, i.e. emptyCellsLeft == 0)
				if(puzzle[j][i] == 0) {
					++emptyCellsLeft;
				}
			}
		}
			
		candidates = new Candidates(dimension, puzzle);		
		highestLevelRequired = 0;
		
		//[solutionStrategies[i]][times_run, times_successful]
		final int[][] strategyStats = new int[solutionStrategies.length][2];
				
		while(true) {			
			boolean strategySuccess = false;			
			for(int i = 0; i <= maxGradingLevel; ++i) {				
				strategySuccess = solutionStrategies[i].apply(puzzle, candidates);
				
				//Update total times run count for this strategy
				++strategyStats[i][0];
				
				if(solutionStrategies[i].getDidFindSingle()) {
					--emptyCellsLeft;
					solutionStrategies[i].setDidFindSingle(false);
				}
				
				if(strategySuccess) {					
					//Update success count for this strategy
					++strategyStats[i][1];
					
					if(i > highestLevelRequired) {
						highestLevelRequired = i;
					}
					if(emptyCellsLeft == 0) {						
						final int score = getDifficultyScore(solutionStrategies, strategyStats);
						grading = calculateGrading(score);						
						
						return UNIQUE_SOLUTION;												
					}
					break;
				}
				else if(i == maxGradingLevel) {
					return NO_SOLUTION;
				}
			}					
		}	
	}	
	
	/**
	 * Reveal next solution step, either a digit or candidate removal
	 * @param puzzle The puzzle to apply next step on
	 * @param nextDigit Whether next step must reveal a digit
	 * @return The solution strategy required or null if puzzle can't be solved 
	 */
	public LogicStrategy nextStep(final int[][] puzzle, boolean nextDigit) {
		candidates = new Candidates(dimension, puzzle);
		while(true) {
			for(int i = 0; i <= maxGradingLevel; ++i) {
				final boolean strategySuccess = solutionStrategies[i].apply(puzzle, candidates);
				
				if(strategySuccess) {
					if(!(nextDigit && !solutionStrategies[i].getDidFindSingle())) {
						return solutionStrategies[i]; 
					}
					break;
				}
				else if(i == maxGradingLevel) {
					return null;
				}
			}
		}
	}
	
	private Grading calculateGrading(int difficultyScore) {
		if(difficultyScore < MODERATE_THRESHOLD) {
			return Grading.EASY;
		}
		if(difficultyScore < HARD_THRESHOLD) {
			return Grading.MODERATE;
		}
		if(difficultyScore < EXPERT_THRESHOLD) {
			return Grading.HARD;
		}
		if(difficultyScore < DIABOLIC_THRESHOLD) {
			return Grading.EXPERT;
		}
		return Grading.DIABOLIC;
	}
	
	private int getDifficultyScore(final LogicStrategy[] strategies, final int[][] strategyStats) {
		int score = 0;
		for(int i = 0; i <= maxGradingLevel; ++i) {
			score += strategies[i].getScore() * strategyStats[i][1];
		}
		return score;
	}
}