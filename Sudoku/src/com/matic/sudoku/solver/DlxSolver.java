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

/**
 * Sudoku solver based on the exact cover problem solution. It is an implementation
 * of "Dancing Links" algorithm discovered by Donald Knuth
 * @author vedran
 *
 */
public class DlxSolver implements BruteForceSolver {
	
	private final int[] puzzleSolution;
	private final Node[][] matrixNodes;
	private final Column[] columns;	
		
	private final int maxSolutions;	
	private final int gridSize;
	private final int unitSize;
	private final int boxSize;
	
	private int solutionCount;
	
	private Column headColumn;
	
	/**
	 * Representation of an entry in the exact cover matrix	 
	 */
	private class Node {
		Node left, right, down, up;
		Column column;
		int row;
	}
	
	/**
	 * Representation of a column in the exact cover matrix
	 */
	private class Column extends Node {
		Column rightColumn, leftColumn;
		boolean covered;
		int size;		
	}

	/**
	 * Initialize the solver for a specific puzzle dimension
	 * @param puzzleDimension The size/dimension of the puzzle
	 * @param maxSolutions How many solutions are acceptable
	 */
	public DlxSolver(final int puzzleDimension, final int maxSolutions) {		
		this.maxSolutions = maxSolutions;
		
		boxSize = puzzleDimension;
		unitSize = boxSize * boxSize;
		gridSize = unitSize * unitSize;
		
		puzzleSolution = new int[gridSize];
		
		//Initialize the columns
		final int columnCount = gridSize * 4 + 1; //Add one header column
		columns = new Column[columnCount];
		
		for(int colIndex = 0; colIndex < columns.length; ++colIndex) {
			columns[colIndex] = new Column();
		}
		
		//Initialize the column dependencies			
		for(int colIndex = 0; colIndex < columns.length; ++colIndex) {			
			columns[colIndex].leftColumn = columns[(colIndex + columnCount - 1) % columnCount];
	        columns[colIndex].rightColumn = columns[(colIndex + 1) % columnCount];
	        columns[colIndex].up = columns[colIndex];
	        columns[colIndex].down = columns[colIndex];
	        columns[colIndex].size = 0;
	        columns[colIndex].covered = false;
		}
		
		headColumn = columns[gridSize * 4];
		
		//Initialize the matrix nodes
		final int rowCount = gridSize * unitSize;
		matrixNodes = new Node[rowCount][4];
				
		//Initialize the matrix nodes
		for(int rowIndex = 0; rowIndex < matrixNodes.length; ++rowIndex) {
			for(int colIndex = 0; colIndex < matrixNodes[rowIndex].length; ++colIndex) {
				matrixNodes[rowIndex][colIndex] = new Node();
			}
		}
		
		//Initialize the matrix nodes dependencies
		for(int rowIndex = 0; rowIndex < matrixNodes.length; ++rowIndex) {
			for(int colIndex = 0; colIndex < matrixNodes[rowIndex].length; ++colIndex) {
				matrixNodes[rowIndex][colIndex].right = matrixNodes[rowIndex][(colIndex + 1) % 4];
	            matrixNodes[rowIndex][colIndex].left = matrixNodes[rowIndex][(colIndex + 3) % 4];
	            matrixNodes[rowIndex][colIndex].row = rowIndex;
	            
	            int actualColumnIndex = gridSize * colIndex;
	            
	            switch(colIndex) {
	            case 0:	// row-col
	            	actualColumnIndex += rowIndex / gridSize * unitSize + rowIndex % gridSize / unitSize;
	            	break;
	            case 1: // row-value
	            	actualColumnIndex += rowIndex / gridSize * unitSize + rowIndex % unitSize;
	            	break;
	            case 2: // col-value
	            	actualColumnIndex += rowIndex % gridSize / unitSize * unitSize + rowIndex % unitSize;
	            	break;
	            case 3: // box-value
	            	actualColumnIndex += (rowIndex / gridSize / boxSize * boxSize + rowIndex % gridSize / 
	            			unitSize / boxSize)* unitSize + rowIndex % unitSize;
	            	break;
	            }
	            
	            matrixNodes[rowIndex][colIndex].down = matrixNodes[rowIndex][colIndex].column = columns[actualColumnIndex];
	            matrixNodes[rowIndex][colIndex].up = columns[actualColumnIndex].up;
	            columns[actualColumnIndex].up.down = columns[actualColumnIndex].up = matrixNodes[rowIndex][colIndex];
	            columns[actualColumnIndex].size++;
			}
		}
	}
	
	/**
	 * Solve a given puzzle (updates the puzzle with the solution)
	 * @param puzzle Puzzle to be solved
	 * @return Number of possible solutions for the puzzle or INVALID_PUZZLE if puzzle has
	 * wrong dimension
	 */
	@Override
	public int solve(final int[] puzzle) {
		if(puzzle.length != gridSize) {
			return BruteForceSolver.INVALID_PUZZLE;
		}

		int k = 0;		
		boolean noSolution = false;
		solutionCount = 0;

		for(int i = 0; i < gridSize; ++i) {
			if (puzzle[i] > 0) {
				final int row = i * unitSize + puzzle[i] - 1;
				if (matrixNodes[row][0].column.covered
						|| matrixNodes[row][1].column.covered
						|| matrixNodes[row][2].column.covered
						|| matrixNodes[row][3].column.covered) {
					noSolution = true;
					break;
				}				
				for (int j = 0; j < matrixNodes[row].length; ++j)
					cover(matrixNodes[row][j].column);
				puzzleSolution[k++] = row;
			}
		}
		if(!noSolution) {
			search(k);
		}
		if(solutionCount > 0) {
			for (int i = 0; i < gridSize; ++i) {
				int pos = puzzleSolution[i] / unitSize;
				puzzle[pos] = puzzleSolution[i] % unitSize + 1;
			}
		}
		for(int i = k - 1; i >= 0; --i) {
			for(int j = 3; j >= 0; --j) {
				uncover(matrixNodes[puzzleSolution[i]][j].column);
			}
		}
		return solutionCount;
	}
	
	/**
	 * Uncover a given column
	 * @param column Column to uncover
	 */
	private void uncover(Column column) {
		column.covered = false;
		for(Node i = column.up; i != column; i = i.up) {
			for(Node j = i.left; j != i; j = j.left) {
				j.column.size++;
				j.up.down = j;
				j.down.up = j;
			}
		}
		column.leftColumn.rightColumn = column;
		column.rightColumn.leftColumn = column;
	}
	
	/**
	 * DLX algorithm seach(k)-function implementation
	 * @param k
	 */
	private void search(final int k) {
		if(headColumn.rightColumn == headColumn) {
			++solutionCount;
			return;
		}		
		Column min = headColumn.rightColumn;
		for(Column column = headColumn.rightColumn; column != headColumn; column = column.rightColumn) {
			if(column.size < min.size) {
				min = column;
			}
			if(min.size == 0) {
				return;
			}
		}
		cover(min);
		for(Node r = min.down; r != min && solutionCount < maxSolutions; r = r.down) {
			if (solutionCount == 0) {
				puzzleSolution[k] = r.row;
			}
			for(Node j = r.right; j != r; j = j.right) {
				cover(j.column);
			}
			search(k + 1);
			for(Node j = r.left; j != r; j = j.left) {
				uncover(j.column);
			}
		}
		uncover(min);
	}
	
	/**
	 * Cover a given column
	 * @param column Column to cover
	 */
	private void cover(final Column column) {
		column.covered = true;
		
		column.rightColumn.leftColumn = column.leftColumn;
		column.leftColumn.rightColumn = column.rightColumn;
		for(Node i = column.down; i != column; i = i.down) {
			for (Node j = i.right; j != i; j = j.right) {
				j.down.up = j.up;
				j.up.down = j.down;
				j.column.size--;
			}
		}
	}
}

