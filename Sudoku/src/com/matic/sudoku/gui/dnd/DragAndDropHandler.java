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

package com.matic.sudoku.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.TransferHandler;

import com.matic.sudoku.gui.mainwindow.FileOpenHandler;

/**
 * A Drag and Drop handler for opening puzzle files
 * @author vedran
 *
 */
public class DragAndDropHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	
	private final FileOpenHandler fileOpenHandler;
	
	public DragAndDropHandler(final FileOpenHandler fileOpenHandler) {
		this.fileOpenHandler = fileOpenHandler;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		// We are only interested in file drops
        if(!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }
        return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport support) {
		if(!support.isDrop()) {
            return false;
        }
		
        if(!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {       
            return false;
        }
        
        final Transferable transferable = support.getTransferable();
        List<File> files;
        try {
            files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
            //Check that only one file is being dropped
            if(files.size() > 1) {
            	return false;
            }
            fileOpenHandler.openFile(files.get(0));
        } 
        catch(final Exception e) { 
        	return false; 
        }
        
        return true;
	}

}
