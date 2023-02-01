package org.cytoscape.sample.internal;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Set;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		DataSourceManager dataSourceManager = getService(bc, DataSourceManager.class);

		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc, CyNetworkNaming.class);

		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);

		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		// Add a button to launch the application
		JButton launchButton = new JButton("Launch");
		launchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// Create the properties for the launch button
				Properties launchButtonProperties = new Properties();
				launchButtonProperties.setProperty("preferredMenu", "Apps.SCyNet");
				launchButtonProperties.setProperty("title", "SCyNet");

				// Register the launch button as a TaskFactory service

				LaunchButtonTaskFactory launchButtonTaskFactory = new LaunchButtonTaskFactory(bc, cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef, cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, dataSourceManager);
				registerService(bc, launchButtonTaskFactory, TaskFactory.class, launchButtonProperties);
			}
		});
	}
}



