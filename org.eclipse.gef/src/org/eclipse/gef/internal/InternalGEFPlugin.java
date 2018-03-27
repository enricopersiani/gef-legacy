/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.gef.internal;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.gef.resources.GEFResources;
import org.eclipse.gef.resources.IImageDescriptorFactory;

import org.osgi.framework.BundleContext;

public class InternalGEFPlugin extends AbstractUIPlugin {

	private static BundleContext context;
	private static AbstractUIPlugin singleton;

	public InternalGEFPlugin() {
		singleton = this;
	}

	public void start(BundleContext bc) throws Exception {
		super.start(bc);
		context = bc;

		// skip GEF resources initialization in headless mode
		if (Display.getCurrent() == null)
			return;

		GEFResources.getInstance().setImageRegistry(getImageRegistry());
		IImageDescriptorFactory factory = new IImageDescriptorFactory() {
			public ImageDescriptor createFolder() {
				ISharedImages sharedImages = PlatformUI.getWorkbench()
						.getSharedImages();
				return sharedImages
						.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
			}

			public ImageDescriptor createDeleteDisabled() {
				ISharedImages sharedImages = PlatformUI.getWorkbench()
						.getSharedImages();
				return sharedImages
						.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
			}

			public ImageDescriptor createDelete() {
				ISharedImages sharedImages = PlatformUI.getWorkbench()
						.getSharedImages();
				return sharedImages.getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE_DISABLED);
			}
		};
		GEFResources.getInstance().setImageDescriptorFactory(factory);
	}

	public static BundleContext getContext() {
		return context;
	}

	public static AbstractUIPlugin getDefault() {
		return singleton;
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		savePluginPreferences();
		super.stop(context);
	}

}
