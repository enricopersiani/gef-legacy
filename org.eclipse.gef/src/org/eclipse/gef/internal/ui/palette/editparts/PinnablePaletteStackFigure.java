/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.gef.internal.ui.palette.editparts;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.internal.ui.palette.PaletteColorUtil;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;

/**
 * A pinnable palette stack figure.
 * 
 * @author crevells
 * @since 3.4
 */
public class PinnablePaletteStackFigure
    extends Figure {

private static final Color COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_85 = FigureUtilities
    .mixColors(PaletteColorUtil.WIDGET_BACKGROUND,
        PaletteColorUtil.WIDGET_LIST_BACKGROUND, 0.85);

private static final Color COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_40 = FigureUtilities
    .mixColors(PaletteColorUtil.WIDGET_BACKGROUND,
        PaletteColorUtil.WIDGET_LIST_BACKGROUND, 0.40);

private static final Color COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_65 = FigureUtilities
    .mixColors(PaletteColorUtil.WIDGET_BACKGROUND,
        PaletteColorUtil.WIDGET_NORMAL_SHADOW, 0.65);

private static final Color COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_40 = FigureUtilities
    .mixColors(PaletteColorUtil.WIDGET_BACKGROUND,
        PaletteColorUtil.WIDGET_NORMAL_SHADOW, 0.40);

private static final Color COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_80 = FigureUtilities
    .mixColors(PaletteColorUtil.WIDGET_BACKGROUND,
        PaletteColorUtil.WIDGET_NORMAL_SHADOW, 0.80);

private static final Dimension EMPTY_DIMENSION = new Dimension(0, 0);

/**
 * A toggle with a triangle figure.
 */
class RolloverArrow
    extends Toggle {

RolloverArrow() {
    super();
    setRolloverEnabled(true);
    setBorder(null);
    setOpaque(false);
    setPreferredSize(11, -1);
}

/**
 * @return false so that the focus rectangle is not drawn.
 */
public boolean hasFocus() {
    return false;
}

public void paintFigure(Graphics graphics) {
    Rectangle rect = getBounds().getCopy();

    graphics.translate(getLocation());

    // fill the arrow
    int[] points = new int[8];

    if (isSelected() || layoutMode == PaletteViewerPreferences.LAYOUT_ICONS
        || layoutMode == PaletteViewerPreferences.LAYOUT_COLUMNS) {
        // pointing down
        points[0] = 4;
        points[1] = rect.height / 2;
        points[2] = 9;
        points[3] = rect.height / 2;
        points[4] = 6;
        points[5] = rect.height / 2 + 3;
        points[6] = 4;
        points[7] = rect.height / 2;
    } else {
        // pointing to the right
        points[0] = 5;
        points[1] = rect.height / 2 - 2;
        points[2] = 8;
        points[3] = rect.height / 2 + 1;
        points[4] = 5;
        points[5] = rect.height / 2 + 4;
        points[6] = 5;
        points[7] = rect.height / 2 - 2;
    }

    graphics.fillPolygon(points);

    graphics.translate(getLocation().getNegated());
}
}

/**
 * Layout manager for the palette stack header figure that handles the layout of
 * the <code>headerFigure</code> (<code>pinFigure</code>,
 * <code>arrowFigure</code>, and the active tool figure) when in list or
 * details layout mode.
 */
private class HeaderListLayout
    extends StackLayout {

protected boolean isSensitiveVertically(IFigure container) {
    return false;
}

protected Dimension calculatePreferredSize(IFigure parent, int wHint, int hHint) {
    if (isExpanded()) {
        Dimension pinSize = pinFigure.getSize();
        Dimension preferredSize = super.calculatePreferredSize(parent, wHint
            - pinSize.width, hHint);
        preferredSize.width += pinSize.width;
        return preferredSize;
    } else {
        return super.calculatePreferredSize(parent, wHint, hHint);
    }
}

public void layout(IFigure parent) {

    Rectangle r = parent.getClientArea();
    List children = parent.getChildren();
    IFigure child;

    Dimension pinSize = isExpanded() ? pinFigure.getPreferredSize()
        : EMPTY_DIMENSION;

    for (int i = 0; i < children.size(); i++) {
        child = (IFigure) children.get(i);
        if (child == arrowFigure) {
            Rectangle.SINGLETON.setBounds(r);
            Rectangle.SINGLETON.width = 11;
            child.setBounds(Rectangle.SINGLETON);
        } else if (child == pinFigure) {
            Rectangle.SINGLETON.setSize(pinSize);
            Rectangle.SINGLETON.setLocation(r.right() - pinSize.width, r
                .getCenter().y
                - (pinSize.height / 2));
            child.setBounds(Rectangle.SINGLETON);
        } else {
            child.setBounds(r.getResized(-pinSize.width, 0));
        }
    }
}
}

/**
 * Layout manager for the palette stack figure that handles the layout of the
 * <code>headerFigure</code>, <code>expandablePane</code>, and
 * <code>pinFigure</code> when in icons or columns layout mode.
 */
private class PaletteStackIconLayout
    extends AbstractLayout {

protected Dimension calculatePreferredSize(IFigure parent, int wHint, int hHint) {
    return parent.getSize();
}

public void layout(IFigure parent) {
    if (isExpanded()) {
        headerFigure.setBounds(headerBoundsLayoutHint);

        Rectangle paneBounds = parent.getClientArea();
        paneBounds.y += headerBoundsLayoutHint.height;
        paneBounds.height -= headerBoundsLayoutHint.height;
        expandablePane.setBounds(paneBounds);

        Rectangle pinBounds = Rectangle.SINGLETON;
        Dimension pinSize = pinFigure.getPreferredSize();
        pinBounds.setSize(pinSize);
        int pinFigureAreaHeight = expandablePane.getInsets().top;
        pinBounds.setLocation(expandablePane.getClientArea().right()
            - pinSize.width, (paneBounds.y + pinFigureAreaHeight / 2)
            - (pinSize.height / 2));
        pinFigure.setBounds(pinBounds);
    } else {
        headerFigure.setBounds(parent.getClientArea());
    }
}
}

/**
 * listens to selection events on the arrow figure
 */
private ChangeListener clickableArrowListener = new ChangeListener() {

    public void handleStateChanged(ChangeEvent event) {
        if (event.getPropertyName().equals(ButtonModel.SELECTED_PROPERTY)) {

            Animation.markBegin();
            handleExpandStateChanged();
            Animation.run(150);

            // Now collapse other stacks when they are not pinned open or in the
            // case of columns and icons layout mode (where only one stack can
            // be expanded at a time for layout reasons).
            if (isExpanded()) {
                boolean collapseOtherStacks = (layoutMode == PaletteViewerPreferences.LAYOUT_COLUMNS || layoutMode == PaletteViewerPreferences.LAYOUT_ICONS);

                for (Iterator iterator = getParent().getChildren().iterator(); iterator
                    .hasNext();) {
                    Object childFigure = iterator.next();
                    if (childFigure instanceof PinnablePaletteStackFigure
                        && childFigure != PinnablePaletteStackFigure.this) {

                        if (collapseOtherStacks
                            || (((PinnablePaletteStackFigure) childFigure)
                                .isExpanded() && !((PinnablePaletteStackFigure) childFigure)
                                .isPinnedOpen())) {

                            ((PinnablePaletteStackFigure) childFigure)
                                .setExpanded(false);
                        }
                    }
                }

                // The auto-collapsing of drawers is handled in the
                // PaletteAnimator.
                // If a stack is expanded when there is not enough room to fit
                // the
                // expanded stack than other drawers should be collapsed.
                // However,
                // when the animation is run the first time in this method the
                // drawer layout has not yet completed so other drawers are not
                // collapsed. This 'second pass' of the animation will ensure
                // that
                // drawers get collapsed if necessary as a result of the newly
                // expanded stack.
                Animation.markBegin();
                revalidate();
                Animation.run(150);
            }
        }
    }
};

private IFigure headerFigure;

private IFigure activeToolFigure;

private PinFigure pinFigure;

private RolloverArrow arrowFigure;

private IFigure expandablePane;

private int layoutMode = -1;

private Rectangle headerBoundsLayoutHint = new Rectangle();

public PinnablePaletteStackFigure() {
    super();

    arrowFigure = new RolloverArrow();
    arrowFigure.setBackgroundColor(PaletteColorUtil.WIDGET_DARK_SHADOW);
    arrowFigure.addChangeListener(clickableArrowListener);

    headerFigure = new Figure();
    headerFigure.add(arrowFigure);

    pinFigure = new PinFigure();
    pinFigure.setBorder(new MarginBorder(0, 0, 0, 2));

    expandablePane = new Figure();

    add(headerFigure);
}

protected void paintFigure(Graphics g) {
    super.paintFigure(g);

    if (!isExpanded()) {
        return;
    }

    Rectangle headerBounds = headerFigure.getBounds().getCopy();
    Rectangle paneBounds = expandablePane.getClientArea();

    // fill expandable pane background
    g.setBackgroundColor(COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_40);
    g.fillRectangle(paneBounds);

    if (layoutMode == PaletteViewerPreferences.LAYOUT_ICONS
        || layoutMode == PaletteViewerPreferences.LAYOUT_COLUMNS) {

        int pinHeight = expandablePane.getInsets().top;
        Rectangle pinAreaBounds = new Rectangle(paneBounds.x, expandablePane
            .getBounds().y, paneBounds.width, pinHeight);

        // fill background colors
        g.setForegroundColor(COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_40);
        g.setBackgroundColor(COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_85);
        g.fillGradient(headerBounds, true);

        g.setBackgroundColor(COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_85);
        g.fillRectangle(pinAreaBounds);

        // draw white lines
        g.setForegroundColor(PaletteColorUtil.WIDGET_LIST_BACKGROUND);
        g.drawLine(headerBounds.getTopLeft().getTranslated(1, 1), headerBounds
            .getTopRight().getTranslated(-1, 1));
        g.drawLine(headerBounds.getBottomLeft().getTranslated(1, 0),
            headerBounds.getTopLeft().getTranslated(1, 1));
        g.drawLine(headerBounds.getBottomRight().getTranslated(-2, 0),
            headerBounds.getTopRight().getTranslated(-2, 1));

        g.drawLine(pinAreaBounds.getTopLeft().getTranslated(0, 1),
            pinAreaBounds.getTopRight().getTranslated(-1, 1));
        g.drawLine(pinAreaBounds.getBottomLeft().getTranslated(0, -2),
            pinAreaBounds.getBottomRight().getTranslated(-1, -2));

        // draw grey border around the whole palette stack
        PointList points = new PointList();
        points.addPoint(headerBounds.getBottomLeft());
        points.addPoint(headerBounds.getTopLeft().getTranslated(0, 2));
        points.addPoint(headerBounds.getTopLeft().getTranslated(1, 1));
        points.addPoint(headerBounds.getTopLeft().getTranslated(2, 0));
        points.addPoint(headerBounds.getTopRight().getTranslated(-3, 0));
        points.addPoint(headerBounds.getTopRight().getTranslated(-2, 1));
        points.addPoint(headerBounds.getTopRight().getTranslated(-1, 2));
        points.addPoint(headerBounds.getBottomRight().getTranslated(-1, 0));
        points.addPoint(pinAreaBounds.getTopRight().getTranslated(-1, 0));
        points.addPoint(paneBounds.getBottomRight().getTranslated(-1, -1));
        points.addPoint(paneBounds.getBottomLeft().getTranslated(0, -1));
        points.addPoint(pinAreaBounds.getTopLeft().getTranslated(0, 0));
        points.addPoint(headerBounds.getBottomLeft());

        g.setForegroundColor(COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_40);
        g.drawPolygon(points);

        g.setForegroundColor(COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_80);
        Point pt = headerBounds.getTopLeft().getTranslated(0, 1);
        g.drawPoint(pt.x, pt.y);
        pt = headerBounds.getTopLeft().getTranslated(1, 0);
        g.drawPoint(pt.x, pt.y);
        pt = headerBounds.getTopRight().getTranslated(-2, 0);
        g.drawPoint(pt.x, pt.y);
        pt = headerBounds.getTopRight().getTranslated(-1, 1);
        g.drawPoint(pt.x, pt.y);
    } else {

        // fill header background
        g.setBackgroundColor(COLOR_WIDGET_BACKGROUND_LIST_BACKGROUND_85);
        g.fillRectangle(headerBounds);

        // draw top and bottom border lines of header figure
        g.setForegroundColor(COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_65);
        g.drawLine(headerBounds.getTopLeft(), headerBounds.getTopRight());
        g.setForegroundColor(PaletteColorUtil.WIDGET_LIST_BACKGROUND);
        g.drawLine(headerBounds.getBottomLeft().getTranslated(0, -2),
            headerBounds.getBottomRight().getTranslated(0, -2));

        // draw bottom border line of expandable pane
        g.setForegroundColor(COLOR_WIDGET_BACKGROUND_NORMAL_SHADOW_65);
        g.drawLine(paneBounds.getBottomLeft().getTranslated(0, -1), paneBounds
            .getBottomRight().getTranslated(0, -1));
    }

}

/**
 * @return The content pane for this figure, i.e. the Figure to which children
 *         can be added.
 */
public IFigure getContentPane(IFigure figure) {
    if (figure == activeToolFigure) {
        return headerFigure;
    }
    return getContentPane();
}

public IFigure getContentPane() {
    return expandablePane;
}

public IFigure getActiveFigure() {
    return activeToolFigure;
}

/**
 * @return <code>true</code> if the drawer is expanded
 */
public boolean isExpanded() {
    return arrowFigure.getModel().isSelected();
}

/**
 * @return <code>true</code> if the drawer is expanded and is pinned (i.e., it
 *         can't be automatically collapsed)
 */
public boolean isPinnedOpen() {
    return isExpanded() && pinFigure.getModel().isSelected();
}

/**
 * Pins or unpins the stack. The stack can be pinned open only when it is
 * expanded. Attempts to pin it when it is collapsed will do nothing.
 * 
 * @param pinned
 *            <code>true</code> if the stack is to be pinned
 */
public void setPinned(boolean pinned) {
    if (isExpanded()) {
        pinFigure.setSelected(pinned);
    }
}

public void setExpanded(boolean value) {
    arrowFigure.setSelected(value);
    if (!value) {
        pinFigure.setSelected(false);
    }
}

public void setLayoutMode(int newLayoutMode) {
    if (this.layoutMode == newLayoutMode) {
        return;
    }

    this.layoutMode = newLayoutMode;

    // Only one stack can be expanded in icons and layout mode, therefore for
    // consistency let's always collapse stacks when changing the layout modes.
    setExpanded(false);

    if (newLayoutMode == PaletteViewerPreferences.LAYOUT_LIST
        || newLayoutMode == PaletteViewerPreferences.LAYOUT_DETAILS) {

        headerFigure.setLayoutManager(new HeaderListLayout());

        expandablePane.setLayoutManager(new ToolbarLayout());
        expandablePane.setBorder(new MarginBorder(1, 0, 1, 0));
        setLayoutManager(new ToolbarLayout());

    } else {
        headerFigure.setLayoutManager(new BorderLayout());
        if (activeToolFigure != null) {
            headerFigure.setConstraint(activeToolFigure, BorderLayout.CENTER);
        }
        headerFigure.setConstraint(arrowFigure, BorderLayout.RIGHT);

        setLayoutManager(new PaletteStackIconLayout());

        // account for space used by pin figure
        expandablePane.setBorder(new MarginBorder(18, 0, 0, 0));

        if (layoutMode == PaletteViewerPreferences.LAYOUT_COLUMNS) {
            expandablePane.setLayoutManager(new ColumnsLayout());
        } else { // LAYOUT_ICONS
            FlowLayout fl = new FlowLayout();
            fl.setMinorSpacing(0);
            fl.setMajorSpacing(0);
            expandablePane.setLayoutManager(fl);
        }
    }
}

public void activeEntryChanged(IFigure oldFigure, int oldFigureIndex,
        IFigure newFigure) {

    if (oldFigure != null) {
        // if figure is null, its no longer a child.
        expandablePane.add(oldFigure, oldFigureIndex);
    }

    if (newFigure != null) {
        activeToolFigure = newFigure;
        headerFigure.add(activeToolFigure, BorderLayout.CENTER, 0);
    } else {
        activeToolFigure = null;
    }
}

private void handleExpandStateChanged() {
    if (isExpanded()) {
        if (expandablePane.getParent() != this) {
            add(expandablePane);
            
            if (layoutMode == PaletteViewerPreferences.LAYOUT_LIST
                || layoutMode == PaletteViewerPreferences.LAYOUT_DETAILS) {
                headerFigure.add(pinFigure);
            } else {
                add(pinFigure);
            }
        }
    } else {
        if (expandablePane.getParent() == this) {
            remove(expandablePane);
            pinFigure.getParent().remove(pinFigure);
        }
    }
}

/**
 * Gets the preferred size of the expandable pane figure. Used by
 * <code>PaletteContainerFlowLayout</code> when the layout is icons or columns
 * mode.
 * 
 * @param wHint
 *            width hint
 * @param hHint
 *            height hint
 * @return the preferred size of the expandable pane figure or (0,0) if the pane
 *         is collapsed
 */
public Dimension getExpandedContainerPreferredSize(int wHint, int hHint) {
    if (isExpanded()) {
        return expandablePane.getPreferredSize(wHint, hHint);
    } else {
        return EMPTY_DIMENSION;
    }
}

/**
 * Sets the header bounds layout hint. Set by
 * <code>PaletteContainerFlowLayout</code> when the layout is icons or columns
 * mode and used by <code>PaletteStackIconLayout</code>.
 * 
 * @param rect
 *            the new value
 */
public void setHeaderBoundsLayoutHint(Rectangle rect) {
    headerBoundsLayoutHint.setBounds(rect);
}

/**
 * Gets the preferred size of the header figure. Used by
 * <code>PaletteContainerFlowLayout</code> and <code>ColumnsLayout</code>
 * when the layout is icons or columns mode.
 * 
 * @param wHint
 *            width hint
 * @param hHint
 *            height hint
 * @return the preferred size of the header figure
 */
public Dimension getHeaderPreferredSize(int wHint, int hHint) {
    return headerFigure.getPreferredSize(wHint, hHint);
}

public boolean containsPoint(int x, int y) {
    return headerFigure.containsPoint(x, y)
        || (isExpanded() && expandablePane.containsPoint(x, y));
}

}