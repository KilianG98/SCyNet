package org.cytoscape.sample.internal;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.PanelTaskManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

public class CreateNetworkViewTaskFactory extends AbstractTaskFactory {

	private final CyNetworkFactory cnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkManager networkManager;
	private final CyNetworkNaming cyNetworkNaming;
	private final DataSourceManager dataSourceManager;
	private final CyNetwork currentNetwork;
	private boolean showOnlyCrossfeeding;
	private final JToggleButton myButton;

	public CreateNetworkViewTaskFactory(CyNetworkNaming cyNetworkNaming, CyNetworkFactory cnf, CyNetworkManager networkManager, CyNetworkViewFactory cnvf,
										final CyNetworkViewManager networkViewManager, DataSourceManager dataSourceManager, CyNetwork currentNetwork, JToggleButton myButton){
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.networkViewManager = networkViewManager;
		this.networkManager = networkManager;
		this.cyNetworkNaming = cyNetworkNaming;
		this.dataSourceManager = dataSourceManager;
		this.currentNetwork = currentNetwork;
		this.showOnlyCrossfeeding = false;
		this.myButton = myButton;

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton toggleButton = (JToggleButton) e.getSource();
				if (toggleButton.isSelected()) {
					// Handle the "selected" outcome
					// System.out.println("Button is selected");
					showOnlyCrossfeeding = true;
				} else {
					// Handle the "not selected" outcome
					// System.out.println("Button is not selected");
					showOnlyCrossfeeding = false;
				}
			}
		};
		this.myButton.addActionListener(listener);
	}

	public TaskIterator createTaskIterator(){
		FileChoosing newChooser = new FileChoosing();
		HashMap<String, Double> csvMap = newChooser.makeMap();
		return new TaskIterator(new CreateNetworkViewTask(cyNetworkNaming, cnf,networkManager, cnvf, networkViewManager, dataSourceManager, currentNetwork, csvMap, showOnlyCrossfeeding));
	}

	public JToggleButton getButton() {
		return this.myButton;
	}
}