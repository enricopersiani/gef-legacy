/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef;

import org.eclipse.gef.palette.PaletteListener;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.palette.PaletteViewer;

/**
 * An extension of {@link LightweightEditDomain} that supports the
 * installation of a {@link PaletteViewer}.
 * <P>
 */
public class EditDomain extends LightweightEditDomain {

	private PaletteViewer paletteViewer;
	private PaletteRoot paletteRoot;

	/**
	 * Listens to the PaletteViewer for changes in selection, and sets the
	 * Domain's Tool accordingly.
	 */
	private PaletteListener paletteListener = new PaletteListener() {
		public void activeToolChanged(PaletteViewer viewer, ToolEntry tool) {
			handlePaletteToolChanged();
		}
	};

	/**
	 * Constructs an EditDomain and loads the default tool.
	 */
	public EditDomain() {
		loadDefaultTool();
	}

	/**
	 * Returns the palette viewer currently associated with this domain.
	 * 
	 * @since 1.0
	 * @return The current palette viewer
	 */
	public PaletteViewer getPaletteViewer() {
		return paletteViewer;
	}

	private void handlePaletteToolChanged() {
		PaletteViewer paletteViewer = getPaletteViewer();
		if (paletteViewer != null) {
			ToolEntry entry = paletteViewer.getActiveTool();
			if (entry != null)
				setActiveTool(entry.createTool());
			else
				setActiveTool(getDefaultTool());
		}
	}

	/**
	 * Loads the default Tool. If a palette has been provided and that palette
	 * has a default, then that tool is loaded. If not, the EditDomain's default
	 * tool is loaded. By default, this is the
	 * {@link org.eclipse.gef.tools.SelectionTool}.
	 */
	public void loadDefaultTool() {
		setActiveTool(null);
		PaletteViewer paletteViewer = getPaletteViewer();
		if (paletteRoot != null && paletteViewer != null) {
			if (paletteRoot.getDefaultEntry() != null) {
				paletteViewer.setActiveTool(paletteRoot.getDefaultEntry());
				return;
			} else
				paletteViewer.setActiveTool(null);
		}
		setActiveTool(getDefaultTool());
	}

	/**
	 * Sets the PalatteRoot for this EditDomain. If the EditDomain already knows
	 * about a PaletteViewer, this root will be set into the palette viewer
	 * also. Loads the default Tool after the root has been set.
	 * <p>
	 * It is recommended that the palette root not be set multiple times. Some
	 * components (such as the PaletteCustomizerDialog for the PaletteViewer)
	 * might still hold on to the old root. If the input has changed or needs to
	 * be refreshed, just remove all the children from the root and add the new
	 * ones.
	 * 
	 * @param root
	 *            the palette's root
	 */
	public void setPaletteRoot(PaletteRoot root) {
		if (paletteRoot == root)
			return;
		paletteRoot = root;
		if (getPaletteViewer() != null) {
			getPaletteViewer().setPaletteRoot(paletteRoot);
			loadDefaultTool();
		}
	}

	/**
	 * Sets the <code>PaletteViewer</code> for this EditDomain
	 * 
	 * @param palette
	 *            the PaletteViewer
	 */
	public void setPaletteViewer(PaletteViewer palette) {
		if (palette == paletteViewer)
			return;
		if (paletteViewer != null)
			paletteViewer.removePaletteListener(paletteListener);
		paletteViewer = palette;
		if (paletteViewer != null) {
			palette.addPaletteListener(paletteListener);
			if (paletteRoot != null) {
				paletteViewer.setPaletteRoot(paletteRoot);
				loadDefaultTool();
			}
		}
	}
}
