/*****************************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Common Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ****************************************************************************************/

package org.eclipse.gef.examples.text.edit;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.InlineFlow;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import org.eclipse.gef.examples.text.TextLocation;
import org.eclipse.gef.examples.text.figures.CommentPage;
import org.eclipse.gef.examples.text.model.Container;
import org.eclipse.gef.examples.text.model.Style;

/**
 * @since 3.1
 */
public abstract class CompoundTextualPart extends AbstractTextualPart {

public CompoundTextualPart(Object model) {
	setModel(model);
}

protected void createEditPolicies() {}

/**
 * @see AbstractGraphicalEditPart#createFigure()
 */
protected IFigure createFigure() {
	Figure figure;
	switch (getContainer().getType()) {
		case Container.TYPE_INLINE:
			figure = new InlineFlow();
			break;
		case Container.TYPE_COMMENT:
			figure = new CommentPage();
			break;
		case Container.TYPE_PARAGRAPH:
			figure = new FlowPage();
			break;
		default:
			figure = new Figure();
			figure.setLayoutManager(new ToolbarLayout(ToolbarLayout.VERTICAL));
			figure.setBorder(new LineBorder(ColorConstants.lightGray, 4));
	}
	return figure;
}

/**
 * @see TextualEditPart#getCaretPlacement(int)
 */
public Rectangle getCaretPlacement(int offset, boolean trailing) {
	throw new RuntimeException("This part cannot place the caret");
}

protected Container getContainer() {
	return (Container)getModel();
}

/**
 * @see TextualEditPart#getLength()
 */
public int getLength() {
	return getChildren().size();
}

protected List getModelChildren() {
	return getContainer().getChildren();
}

/**
 * @see TextualEditPart#getNextLocation(int, TextLocation)
 */
public TextLocation getNextLocation(CaretSearch search) {
	switch (search.type) {
		case CaretSearch.LINE_BOUNDARY:
			if (search.isForward)
				return searchLineBegin(search);
			return searchLineEnd(search);

		case CaretSearch.ROW:
			if (search.isForward)
				return searchLineBelow(search);
			return searchLineAbove(search);

		case CaretSearch.WORD_BOUNDARY:
		case CaretSearch.COLUMN:
			if (search.isForward)
				return searchForward(search);
			return searchBackwards(search);

		default:
			break;
	}

	if (getParent() instanceof TextualEditPart)
		return getTextParent().getNextLocation(search);
	return null;
}

public void propertyChange(PropertyChangeEvent evt) {
	if (evt.getPropertyName().equals("children"))
		refreshChildren();
}

protected void refreshVisuals() {
	Style style = getContainer().getStyle();
	FontData basis = getFigure().getParent().getFont().getFontData()[0];
	if (style.getFontHeight() > 0)
		basis.setHeight(style.getFontHeight());
	if (style.getFontFamily() != null)
		basis.setName(style.getFontFamily());
	basis.setStyle((style.isBold() ? SWT.BOLD : 0) | (style.isItalic() ? SWT.ITALIC : 0));
	getFigure().setFont(new Font(null, basis));
}

TextLocation searchBackwards(CaretSearch search) {
	TextLocation current = search.where;
	int childIndex = (current == null) ? getChildren().size() - 1
			: getChildren().indexOf(current.part) - 1;
	TextualEditPart part;
	while (childIndex >= 0) {
		part = (TextualEditPart)getChildren().get(childIndex--);
		return part.getNextLocation(search.recurseSearch());
	}
	if (search.isRecursive)
		return null;
	if (getParent() instanceof TextualEditPart)
		return getTextParent().getNextLocation(search.continueSearch(this, 0));
	return null;
}

TextLocation searchForward(CaretSearch search) {
	TextLocation current = search.where;
	
	int childIndex = (current == null) ? 0
			: getChildren().indexOf(current.part) + 1;
	int childCount = getChildren().size();
	TextualEditPart part;
	CaretSearch recurse = search.recurseSearch();
	while (childIndex < childCount) {
		part = (TextualEditPart)getChildren().get(childIndex++);
		return part.getNextLocation(recurse);
	}
	if (search.isRecursive)
		return null;
	if (this instanceof BlockTextualPart)
		search.isInto = true;
	if (getParent() instanceof TextualEditPart)
		return getTextParent().getNextLocation(
				search.continueSearch(this, getLength()));
	return null;
}

protected TextLocation searchLineBegin(CaretSearch search) {
	int childIndex = 0;
	int childCount = getChildren().size();
	TextualEditPart newPart;
	TextLocation result;
	while (childIndex < childCount) {
		newPart = (TextualEditPart)getChildren().get(childIndex);
		result = newPart.getNextLocation(search.recurseSearch());
		if (result != null)
			return result;
		childIndex++;
	}
	return null;
}

protected TextLocation searchLineEnd(CaretSearch search) {
	int childIndex = getChildren().size() - 1;
	TextualEditPart child;
	TextLocation result;
	while (childIndex >= 0) {
		child = (TextualEditPart)getChildren().get(childIndex);
		result = child.getNextLocation(search.recurseSearch());
		if (result != null)
			return result;
		childIndex--;
	}
	return null;
}

protected TextLocation searchLineBelow(CaretSearch search) {
	//The top of this figure must be below the bottom of the caret
//	if (getFigure().getBounds().y < caret.bottom())
		//return null;

	TextLocation location = search.where;
	
	int childIndex;
	int childCount = getChildren().size();
	TextualEditPart part;
	if (location == null)
		childIndex = 0;
	else {
		childIndex = getChildren().indexOf(location.part);
		if (location.offset == location.part.getLength())
			childIndex++;
	}
	
	TextLocation result = null;
	int dx = Integer.MAX_VALUE;
	Rectangle lineBounds = null;
	search = search.recurseSearch();
	while (childIndex < childCount) {
		part = (TextualEditPart)getChildren().get(childIndex);
		location = part.getNextLocation(search);
		if (location != null) {
			//$TODO need to set advancing on getNextLocation
			Rectangle newPlacement = location.part.getCaretPlacement(location.offset, false);
			if (lineBounds == null)
				lineBounds = new Rectangle(newPlacement);
			else if (lineBounds.y > newPlacement.bottom())
				break;
			else
				lineBounds.union(newPlacement);
			
			int distance = Math.abs(newPlacement.x - search.x);
			if (distance < dx) {
				result = location;
				dx = distance;
			}
		}
		childIndex++;
	}
	return result;
}

protected TextLocation searchLineAbove(CaretSearch search) {
	//The bottom of this figure must be above the top of the caret
	//if (getFigure().getBounds().bottom() > caret.y)
	//	return null;
	
	int childIndex;
	TextualEditPart part;
	TextLocation location = search.where;
	if (location == null)
		childIndex = getChildren().size() - 1;
	else {
		childIndex = getChildren().indexOf(location.part);
		if (location.offset == 0)
			childIndex--;
	}
	
	TextLocation result = null;
	int dx = Integer.MAX_VALUE;
	Rectangle lineBounds = null;
	while (childIndex >= 0) {
		part = (TextualEditPart)getChildren().get(childIndex);
		location = part.getNextLocation(search.recurseSearch());
		if (location != null) {
			Rectangle newPlacement = location.part.getCaretPlacement(location.offset, false);
			if (lineBounds == null)
				lineBounds = new Rectangle(newPlacement);
			else if (lineBounds.y > newPlacement.bottom())
				break;
			else
				lineBounds.union(newPlacement);
			
			int distance = Math.abs(newPlacement.x - search.x);
			if (distance < dx) {
				result = location;
				dx = distance;
			}
		}
		childIndex--;
	}
	return result;
}

/**
 * Selection is not rendered
 */
public void setSelection(int start, int end) {}

}