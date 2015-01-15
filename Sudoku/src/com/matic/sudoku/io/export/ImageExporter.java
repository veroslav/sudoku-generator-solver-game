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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.matic.sudoku.Resources;
import com.matic.sudoku.gui.board.Board;
import com.matic.sudoku.io.FileSaveFilter;

/**
 * Support for exporting puzzles to images (GIF, JPEG and PNG)
 * @author vedran
 *
 */
public class ImageExporter implements FileSaveFilter {
	
	private static final String JPEG_FILTER_NAME = "format.jpeg";
	private static final String PNG_FILTER_NAME = "format.png";
	private static final String GIF_FILTER_NAME = "format.gif";
	
	private static final String JPEG_SUFFIX = "jpg";
	private static final String PNG_SUFFIX = "png";
	private static final String GIF_SUFFIX = "gif";
	
	@Override
	public FileFilter[] getSupportedFileSaveFilters() {
		final FileFilter[] fileFilters = {
				new FileNameExtensionFilter(
						Resources.getTranslation(PNG_FILTER_NAME), 
						PNG_SUFFIX),
				new FileNameExtensionFilter(
						Resources.getTranslation(JPEG_FILTER_NAME), 
						JPEG_SUFFIX),				
				new FileNameExtensionFilter(
						Resources.getTranslation(GIF_FILTER_NAME), 
						GIF_SUFFIX)};		
		
		return fileFilters;
	}
	
	@Override
	public String getFileSuffix(final String description) {
		switch(description) {
		case JPEG_FILTER_NAME:
			return JPEG_SUFFIX;
		case GIF_FILTER_NAME:
			return GIF_SUFFIX;
		default:
			return PNG_SUFFIX;	
		}
	}

	/**
	 * Write board contents to an image file
	 * 
	 * @param board Board contents to write
	 * @param targetFile Output image file
	 * @param imageType File suffix for output image file
	 * @throws IOException If any file writing error occur
	 */
	public void write(final Board board, final File targetFile, final String imageType) throws IOException {
		board.setSize(Board.PREFERRED_HEIGHT, Board.PREFERRED_HEIGHT);
		board.handleResized();
		
		final BufferedImage bufferedImage = new BufferedImage(
				board.getWidth(), board.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = bufferedImage.createGraphics();
		
		board.draw(g2d, true, false);
		
		ImageIO.write(bufferedImage, imageType, targetFile);
		
		g2d.dispose();
	}
}
