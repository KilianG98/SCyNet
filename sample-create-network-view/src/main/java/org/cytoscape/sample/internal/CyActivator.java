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


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		DataSourceManager dataSourceManager = getService(bc, DataSourceManager.class);

		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);

		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);

		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);

		Properties createNetworkViewTaskFactoryProps = new Properties();
		createNetworkViewTaskFactoryProps.setProperty("preferredMenu","Apps.Samples");

		// This Part has been changed to make the Menu bigger and add the currently loaded Networks
		JFrame myFrame = new JFrame();
		JToggleButton myButton = new JToggleButton("Toggle only \'crossfeeding\' Nodes");

		Set<CyNetwork> allNetworks = cyNetworkManagerServiceRef.getNetworkSet();
		for (CyNetwork currentNetwork : allNetworks) {
			CreateNetworkViewTaskFactory createNetworkViewTaskFactory = new CreateNetworkViewTaskFactory(cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef,cyNetworkManagerServiceRef, cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, dataSourceManager, currentNetwork, myButton);
			myButton = createNetworkViewTaskFactory.getButton();
			String currentName = currentNetwork.getDefaultNetworkTable().getRow(currentNetwork.getSUID()).get("name", String.class);
			createNetworkViewTaskFactoryProps.setProperty("title", currentName);
			registerService(bc,createNetworkViewTaskFactory,TaskFactory.class, createNetworkViewTaskFactoryProps);
		}
		myFrame.add(myButton);
		myFrame.setSize(400,200);
		myFrame.setVisible(true);
	}
}