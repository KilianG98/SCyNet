package org.cytoscape.sample.internal;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import java.util.Properties;
import java.util.Set;


public class LaunchButtonTaskFactory extends CyActivator{

    public LaunchButtonTaskFactory(BundleContext bc, CyNetworkNaming cyNetworkNamingServiceRef, CyNetworkFactory cyNetworkFactoryServiceRef, CyNetworkManager cyNetworkManagerServiceRef, CyNetworkViewFactory cyNetworkViewFactoryServiceRef, CyNetworkViewManager cyNetworkViewManagerServiceRef, DataSourceManager dataSourceManager) {

        Properties createNetworkViewTaskFactoryProps = new Properties();
        createNetworkViewTaskFactoryProps.setProperty("preferredMenu", "Apps.SCyNet.SCyNet");

        // This Part has been changed to make the Menu bigger and add the currently loaded Networks
        Set<CyNetwork> allNetworks = cyNetworkManagerServiceRef.getNetworkSet();
        for (CyNetwork currentNetwork : allNetworks) {
            CreateNetworkViewTaskFactory createNetworkViewTaskFactory = new CreateNetworkViewTaskFactory(
                    cyNetworkNamingServiceRef, cyNetworkFactoryServiceRef, cyNetworkManagerServiceRef,
                    cyNetworkViewFactoryServiceRef, cyNetworkViewManagerServiceRef, dataSourceManager, currentNetwork
            );
            String currentName = currentNetwork.getDefaultNetworkTable().getRow(currentNetwork.getSUID()).get("name", String.class);
            createNetworkViewTaskFactoryProps.setProperty("title", currentName);
            registerService(bc, createNetworkViewTaskFactory, TaskFactory.class, createNetworkViewTaskFactoryProps);
        }
    }
}