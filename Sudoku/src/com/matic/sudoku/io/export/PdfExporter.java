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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.matic.sudoku.Resources;
import com.matic.sudoku.Sudoku;
import com.matic.sudoku.generator.Generator;
import com.matic.sudoku.generator.Generator.Symmetry;
import com.matic.sudoku.generator.GeneratorResult;
import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.gui.board.Board.SymbolType;
import com.matic.sudoku.io.FileSaveFilter;
import com.matic.sudoku.io.export.ExporterParameters.ExportMode;
import com.matic.sudoku.io.export.ExporterParameters.Ordering;
import com.matic.sudoku.solver.LogicSolver.Grading;

/**
 * Support for exporting puzzles to PDF documents
 * 
 * @author Vedran Matic
 *
 */
public class PdfExporter implements FileSaveFilter {		
	
	public static final String PDF_FILTER_NAME = Resources.getTranslation("format.pdf");	
	public static final String PDF_SUFFIX = "pdf";
	
	private static final Color LEGEND_COLOR = new Color(50, 50, 50, 70);
	
	private static final double LEGEND_FONT_PERCENTAGE = 0.1;
	private static final int DOCUMENT_MARGIN = 20;
	
	private static final char LEGEND_DOT = '.';
	private static final char SEPARATOR = ' ';
	
	@Override
	public FileFilter[] getSupportedFileSaveFilters() {
		final FileFilter[] fileFilters = {new FileNameExtensionFilter(PDF_FILTER_NAME, PDF_SUFFIX)};
		return fileFilters;
	}

	@Override
	public String getFileSuffix(String description) {
		return PDF_SUFFIX;
	}
	
	/**
	 * Write board contents to PDF
	 * 
	 * @param board The board to write
	 * @param targetFile Target output PDF file to write to
	 * @throws DocumentException If any PDF library error occurs
	 * @throws IOException If any error occurs while writing the PDF file
	 */
	public void write(final Board board, final File targetFile) throws DocumentException, IOException {
		//FontFactory.defaultEmbedding = true;
		final Document document = new Document(PageSize.A4, DOCUMENT_MARGIN, DOCUMENT_MARGIN, DOCUMENT_MARGIN, DOCUMENT_MARGIN);
		final OutputStream outputStream = new FileOutputStream(targetFile);
		final PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
		
		document.open();
		
		final PdfContentByte contentByte = pdfWriter.getDirectContent();
		final Rectangle pageSize = document.getPageSize();
		
		final float pageHeight = pageSize.getHeight();
		final float pageWidth = pageSize.getWidth();		
		
		contentByte.saveState();
		
		final Graphics2D g2d = contentByte.createGraphics(pageWidth, pageHeight);				
		board.setSize((int)pageWidth, (int)pageWidth);
		board.handleResized();
		
		final int puzzleWidth = board.getPuzzleWidth();
		
		//Calculate x and y coordinates for centered game board
		final int originX = (int)(pageWidth / 2 - (puzzleWidth / 2));
		final int originY = (int)((pageHeight / 2) - (puzzleWidth / 2));
		
		board.setDrawingOrigin(originX, originY);
		board.draw(g2d, true, false);
		
		contentByte.restoreState();
		
		g2d.dispose();
		document.close();
	    outputStream.flush();
	    outputStream.close();
	}
	
	/**
	 * Generate and export multiple boards to PDF
	 * 
	 * @param exporterParameters PDF exporter parameters
	 * @param generator Generator used for puzzle generation
	 * @throws IOException If any PDF library error occurs
	 * @throws DocumentException If any error occurs while writing the PDF file
	 */
	public void write(final ExporterParameters exporterParameters, final Generator generator, final int boardDimension) throws IOException, DocumentException {
		//How many PDF-pages are needed to fit all puzzles using the desired page formatting
		final int optimisticPageCount = exporterParameters.getPuzzleCount() / exporterParameters.getPuzzlesPerPage();		 
		final int pageCount = exporterParameters.getPuzzleCount() % exporterParameters.getPuzzlesPerPage() > 0?
				optimisticPageCount + 1 : optimisticPageCount;
		
		final Document document = new Document(PageSize.A4, DOCUMENT_MARGIN, DOCUMENT_MARGIN, DOCUMENT_MARGIN, DOCUMENT_MARGIN);
		final OutputStream outputStream = new FileOutputStream(exporterParameters.getOutputPath());
		final PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);		
		
		final String creator = Sudoku.getNameAndVersion();
		document.addSubject("Puzzles generated by " + creator);
		document.addCreator(creator);
		document.open();
				
		final Rectangle pageSize = document.getPageSize();		
		final int pageHeight = (int)pageSize.getHeight();
		final int pageWidth = (int)pageSize.getWidth();
		
		//Get appropriate number of rows and columns needed to divide a page into
		final int horizontalDimension = exporterParameters.getPuzzlesPerPage() > 2? 2 : 1;
		final int verticalDimension = exporterParameters.getPuzzlesPerPage() > 1? 2 : 1;
		
		//Get available space for each board (with margins) on a page
		final int boardWidth = pageWidth / horizontalDimension;
		final int boardHeight = pageHeight / verticalDimension;
									
		final Board board = new Board(boardDimension, SymbolType.DIGITS);
		board.setSize(boardWidth, boardHeight);
		board.handleResized();
		
		//Get available height/width on a page for a puzzle itself
		final int puzzleWidth = board.getPuzzleWidth();
		int puzzlesPrinted = 0;
		
		final PdfContentByte contentByte = pdfWriter.getDirectContent();
		final Grading[] gradings = getGeneratedPuzzleGradings(exporterParameters.getGradings(), 
				exporterParameters.getOrdering(), exporterParameters.getPuzzleCount());
		
		pageCounter: for(int page = 0; page < pageCount; ++page) {						
			document.newPage();
			final Graphics2D g2d = contentByte.createGraphics(pageWidth, pageHeight);
			for(int y = 0, i = 0; i < verticalDimension; y += boardHeight, ++i) {
				for(int x = 0, j = 0; j < horizontalDimension; x += boardWidth, ++j) {
					//Check whether to generate a new puzzle or print empty boards 
					final ExportMode exportMode = exporterParameters.getExportMode();
					final Grading selectedGrading = exportMode == ExportMode.BLANK? null : 
						gradings[puzzlesPrinted];
					if(exportMode == ExportMode.GENERATE_NEW) {						
						board.setPuzzle(generatePuzzle(generator, getSymmetry(exporterParameters.getSymmetries()), 
								selectedGrading));
						board.recordGivens();
					}
					
					//Calculate puzzle drawing origins
					final int originX = x + (int)(boardWidth / 2 - (puzzleWidth / 2));
					final int originY = y + (int)(boardHeight / 2) - (puzzleWidth / 2);
					
					board.setSymbolType(getSymbolType(exporterParameters.getSymbolType()));
					board.setDrawingOrigin(originX, originY);
					board.draw(g2d, false, false);
					
					drawLegend(g2d, getLegendString(exporterParameters.isShowNumbering(), 
							exporterParameters.isShowGrading(), puzzlesPrinted+1, selectedGrading), originX, originY, puzzleWidth);
					
					if(++puzzlesPrinted == exporterParameters.getPuzzleCount()) {
						//We've printed all puzzles, break
						g2d.dispose();
						break pageCounter;						
					}
				}				
			}			
			g2d.dispose();			
		}
		document.close();
	    outputStream.flush();
	    outputStream.close();
	}
	
	private String getLegendString(final boolean showNumbering, final boolean showGrading, final int puzzleIndex, final Grading grading) {
		if(!(showNumbering || showGrading)) {
			return null;
		}
		final StringBuilder legend = new StringBuilder();
		
		if(showNumbering) {
			legend.append(puzzleIndex);
			legend.append(LEGEND_DOT);
			legend.append(SEPARATOR);
		}
		
		if(showGrading && grading != null) {
			legend.append(grading.toString());
		}
		
		return legend.toString();
	}
	
	private void drawLegend(final Graphics2D g2d, final String legend, final int x, final int y, final int width) {
		if(legend == null) {
			return;
		}
		final Font legendFont = new Font("Arial", Font.BOLD, (int)(LEGEND_FONT_PERCENTAGE * width));
		g2d.setFont(legendFont);
		g2d.setColor(LEGEND_COLOR);
		
		final FontMetrics fontMetrics = g2d.getFontMetrics();
		final Rectangle2D stringBounds = fontMetrics.getStringBounds(legend, g2d);

		final int fontWidth = (int)stringBounds.getWidth();
		final int fontHeight = (int)stringBounds.getHeight();

		g2d.drawString(legend, x + (int)((width - fontWidth) / 2.0 + 0.5),
				y + (int)((width - fontHeight) / 2.0 + 0.5) + fontMetrics.getAscent());
	}
	
	private Grading[] getGeneratedPuzzleGradings(final List<Grading> targetGradings, final Ordering targetOrdering, 
			final int puzzleCount) {		
		final Grading[] gradings = new Grading[puzzleCount];
		for(int i = 0; i < gradings.length; ++i) {
			final int randomGrading = Resources.RANDOM_INSTANCE.nextInt(targetGradings.size());
			gradings[i] = targetGradings.get(randomGrading);
		}
		if(targetOrdering != Ordering.RANDOM) {
			Arrays.sort(gradings);
		}
		return gradings;		
	}
	
	private SymbolType getSymbolType(final SymbolType targetSymbolType) {
		return targetSymbolType == null? SymbolType.getRandom() : targetSymbolType;
	}
	
	private Symmetry getSymmetry(final List<Symmetry> targetSymmetries) {		
		return targetSymmetries.get(Resources.RANDOM_INSTANCE.nextInt(targetSymmetries.size())); 
	}
	
	private int[] generatePuzzle(final Generator generator, final Symmetry symmetry, 
			final Grading grading) {
		GeneratorResult result = null;
		do {
			result = generator.createNew(grading, symmetry);
		} while(result == null);
		
		return result.getGeneratedPuzzle();
	}
}
