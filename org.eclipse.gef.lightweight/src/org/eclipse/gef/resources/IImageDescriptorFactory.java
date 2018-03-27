/*******************************************************************************
 * Copyright (c) 2014 Metatis Sas and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Enrico Persiani - initial API
 *******************************************************************************/
package org.eclipse.gef.resources;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Abstract {@link org.eclipse.jface.resource.ImageDescriptor} factory for
 * commonly used icons. 
 * 
 * @author Enrico Persiani
 */
public interface IImageDescriptorFactory {
	ImageDescriptor createFolder();
	ImageDescriptor createDelete();
	ImageDescriptor createDeleteDisabled();
}
