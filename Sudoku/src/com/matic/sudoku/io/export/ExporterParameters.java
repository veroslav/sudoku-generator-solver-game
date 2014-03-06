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

package com.matic.sudoku.io.export;

import com.matic.sudoku.generator.Generator.Symmetry;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.solver.LogicSolver.Grading;

/**
 * Parameters used for exporter configuration prior to exporting (and generating) a PDF document
 * 
 * @author vedran
 *
 */
public class ExporterParameters {
	
	public enum ExportMode {
		GENERATE_NEW, BLANK
	}
	
	public enum Ordering {
		RANDOM, GRADING
	}
			
	private SymbolType symbolType;
	private ExportMode exportMode;
	private Ordering ordering;
	private Symmetry symmetry;
	private Grading grading;
	
	private String outputPath;
	
	private boolean showNumbering;
	private boolean showGrading;	
	
	private int puzzlesPerPage;
	private int puzzleCount;
	
	public Ordering getOrdering() {
		return ordering;
	}
	
	public void setOrdering(final Ordering ordering) {
		this.ordering = ordering;
	}
	
	public SymbolType getSymbolType() {
		return symbolType;
	}
	
	public void setSymbolType(final SymbolType symbolType) {
		this.symbolType = symbolType;
	}
	
	public ExportMode getExportMode() {
		return exportMode;
	}
	
	public void setExportMode(final ExportMode exportMode) {
		this.exportMode = exportMode;
	}
	
	public Symmetry getSymmetry() {
		return symmetry;
	}
	
	public void setSymmetry(final Symmetry symmetry) {
		this.symmetry = symmetry;
	}
	
	public Grading getGrading() {
		return grading;
	}
	
	public void setGrading(final Grading grading) {
		this.grading = grading;
	}
	
	public String getOutputPath() {
		return outputPath;
	}
	
	public void setOutputPath(final String outputPath) {
		this.outputPath = outputPath;
	}
	
	public boolean isShowNumbering() {
		return showNumbering;
	}
	
	public void setShowNumbering(final boolean showNumbering) {
		this.showNumbering = showNumbering;
	}
	
	public boolean isShowGrading() {
		return showGrading;
	}
	
	public void setShowGrading(final boolean showGrading) {
		this.showGrading = showGrading;
	}
	
	public int getPuzzlesPerPage() {
		return puzzlesPerPage;
	}
	
	public void setPuzzlesPerPage(final int puzzlesPerPage) {
		this.puzzlesPerPage = puzzlesPerPage;
	}
	
	public int getPuzzleCount() {
		return puzzleCount;
	}
	
	public void setPuzzleCount(final int puzzleCount) {
		this.puzzleCount = puzzleCount;
	}
}