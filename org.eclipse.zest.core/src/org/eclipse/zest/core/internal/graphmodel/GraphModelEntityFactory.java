/*******************************************************************************
 * Copyright 2005, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Chisel Group, University of Victoria
 *******************************************************************************/
package org.eclipse.mylar.zest.core.internal.graphmodel;

import java.util.Iterator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.mylar.zest.core.DebugPrint;
import org.eclipse.mylar.zest.core.internal.viewers.Graph;
import org.eclipse.mylar.zest.core.viewers.IGraphEntityContentProvider;


/**
 * @author irbull
 */
public class GraphModelEntityFactory implements IGraphModelFactory {

	private StructuredViewer viewer = null;
	private boolean highlightAdjacentNodes = false;
	
	public GraphModelEntityFactory(StructuredViewer viewer, boolean highlightAdjacentNodes) {
		this.viewer = viewer;
		this.highlightAdjacentNodes = highlightAdjacentNodes;
	}
	
	/**
	 * Creates a new graph model
	 * @return
	 */
	public GraphModel createModel() {
		return new GraphModel((Graph)viewer.getControl());
	}
	
	private IGraphEntityContentProvider getContentProvider() {
		return (IGraphEntityContentProvider)viewer.getContentProvider();
	}
	
	private ILabelProvider getLabelProvider() {
		return (ILabelProvider)viewer.getLabelProvider();
	}
	
	
	public GraphModel createModelFromContentProvider( Object inputElement ) {
		GraphModel model = createModel();
		Object entities[] = getContentProvider().getElements( inputElement );
		for ( int i = 0; i < entities.length; i++ ) {
			Object data = entities[ i ];
			GraphModelNode node = new GraphModelNode(model, getLabelProvider().getText(data), getLabelProvider().getImage(data), data);
			node.setHighlightAdjacentNodes(highlightAdjacentNodes);
			model.addNode( data, node );
		}
		
		for ( int i=0; i < entities.length; i++ ) {
			Object data = entities[ i ];
			Object[] related = getContentProvider().getConnectedTo( data );
			if ( related != null )
				for ( int j = 0; j < related.length; j++ ) {
					createRelationship(model, null, data, related[j]);
				}
		}	
		return model;
		
		
	}

	
	public GraphModelNode createNode(GraphModel model, Object data) {
		// TODO Auto-generated method stub
		GraphModelNode node = new GraphModelNode(model, getLabelProvider().getText( data ), getLabelProvider().getImage(data), data);
		node.setHighlightAdjacentNodes(highlightAdjacentNodes);
		Object[] related = getContentProvider().getConnectedTo( data );
		for ( int i = 0; i < related.length; i++ ) {
			createRelationship(model, null, data, related);
		}
		return node;
	}

	public GraphModelConnection createRelationship(GraphModel model, Object data, Object source, Object dest) {
		GraphModelNode sourceNode = getNode(model, source );
		GraphModelNode destNode = getNode( model, dest );

		if ( sourceNode == null || destNode == null ) return null;
		
		// Check if connection already exists
		GraphModelConnection connection;
		for (Iterator iterator =  sourceNode.getTargetConnections().iterator(); iterator.hasNext(); ) {
			//TODO: get connections won't work for directed graphs!
			connection = (GraphModelConnection) iterator.next();
			if ((dest != null) && dest.equals(connection.getSource().getExternalNode())) {
				// We already have a node that goes from source to dest!
				DebugPrint.println("Connection already exists: " + connection);
				return null;
			}
		}
		DebugPrint.println("Connecting: " + sourceNode + " : " + destNode);
		// Create the connection
		double weight = getContentProvider().getWeight( source, dest );
		connection = new GraphModelConnection(model, data, sourceNode, destNode, false, weight);
		model.addConnection(connection.getExternalConnection(), connection);
		return connection;
	}

	public GraphModelConnection createRelationship(GraphModel model, Object data) {
		throw new UnsupportedOperationException("Use createRelationship(model, object, object, object)");
	}
	
	private GraphModelNode getNode( GraphModel model, Object data ) {
		GraphModelNode node = model.getInternalNode( data );
		return node;
	}
		
}