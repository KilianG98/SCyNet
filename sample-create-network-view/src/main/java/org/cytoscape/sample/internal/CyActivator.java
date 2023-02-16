package org.cytoscape.sample.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Set;



/**
 The CyActivator class is responsible for activating the CyNDEx-2 application in Cytoscape.
 This class extends the AbstractCyActivator class and overrides the start() method to register the necessary
 services, tasks, and factories for the application to work properly.
 */
public class CyActivator extends AbstractCyActivator {

	/**
	 * Default constructor for the CyActivator class.
	 */
	public CyActivator() {
		super();
	}

	/**
	 * Overrides the start() method in the AbstractCyActivator class to register the necessary services, tasks,
	 * and factories for the application to work properly.
	 @param bc The bundle context used to register the services.
	 */
	public void start(BundleContext bc) {
		// Get the necessary services
		DataSourceManager dataSourceManager = getService(bc, DataSourceManager.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);

		// Set properties for creating a network view task factory
		Properties createNetworkViewTaskFactoryProps = new Properties();
		createNetworkViewTaskFactoryProps.setProperty("preferredMenu","Apps.Samples");

		// Create a JFrame and JToggleButton for the new menu item
		JFrame myFrame = new JFrame();
		JToggleButton myButton = new JToggleButton("Toggle only 'crossfeeding' Nodes");

		// Get all the networks and create a network view task factory for each network
		Set<CyNetwork> allNetworks = cyNetworkManagerServiceRef.getNetworkSet();
		for (CyNetwork currentNetwork : allNetworks) {
			CreateNetworkViewTaskFactory createNetworkViewTaskFactory = new CreateNetworkViewTaskFactory(cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef,cyNetworkManagerServiceRef, cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, dataSourceManager, currentNetwork, myButton);
			myButton = createNetworkViewTaskFactory.getButton();
			String currentName = currentNetwork.getDefaultNetworkTable().getRow(currentNetwork.getSUID()).get("name", String.class);
			createNetworkViewTaskFactoryProps.setProperty("title", currentName);
			registerService(bc,createNetworkViewTaskFactory,TaskFactory.class, createNetworkViewTaskFactoryProps);
		}

		// Add the JToggleButton to the JFrame and make it visible
		myFrame.add(myButton);
		myFrame.setSize(400,200);
		myFrame.setVisible(true);
	}
}