/*******************************************************************************
 * Copyright (c) 2014 Metatis Sas and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Metatis sas - initial API and implementation
 *******************************************************************************/
package org.eclipse.gef.resources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

/**
 * A basic registry for shared resources.
 * 
 * @author Enrico Persiani
 */
public class GEFResources {
	private static GEFResources instance;

	private ImageDescriptor folderImage;	
	private ImageDescriptor deleteImage;
	private ImageDescriptor deleteDisabledImage;
	private IImageDescriptorFactory imageDescriptorFactory;

	private ImageRegistry imageRegistry;	

	/**
	 * Return the default instance of the receiver.
	 * @return GEFResources
	 */
	public static GEFResources getInstance() {
		if (instance == null)
			instance = new GEFResources();
		return instance;
	}

	public  void setImageDescriptorFactory(IImageDescriptorFactory imageDescriptorFactory) {
		this.imageDescriptorFactory = imageDescriptorFactory;
	}
	public ImageDescriptor getFolderImage() {
		if (folderImage == null) {
			checkInitialized(imageDescriptorFactory);
			folderImage = imageDescriptorFactory.createFolder();
		}
		return folderImage;
	}
	public ImageDescriptor getDeleteImage() {
		if (deleteImage == null) {
			checkInitialized(imageDescriptorFactory);
			deleteImage = imageDescriptorFactory.createDelete();
		}
		return deleteImage;
	}
	public ImageDescriptor getDeleteDisabledImage() {
		if (deleteDisabledImage == null) {
			checkInitialized(imageDescriptorFactory);
			deleteDisabledImage = imageDescriptorFactory.createDeleteDisabled();
		}
		return deleteDisabledImage;
	}

	public void setImageRegistry(ImageRegistry imageRegistry) {
		this.imageRegistry = imageRegistry;
	}
	public ImageRegistry getImageRegistry() {
		checkInitialized(imageRegistry);
		return imageRegistry;
	}

	public void checkInitialized(Object object) {
		if (object == null)
			throw new IllegalStateException("GEFResources is not completely initialized"); //$NON-NLS-1$
	}
}
