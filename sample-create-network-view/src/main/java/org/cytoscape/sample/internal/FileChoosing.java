package org.cytoscape.sample.internal;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class FileChoosing {
    private File chosenFile;

    /**
     * Opens up a window to let the user select a csv file with fluxes.
     */
    public FileChoosing()
    {
        // Here we use the JFileChooser to open a window where the user can select a CSV-file with the fluxes
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose the tab-delimited CSV-file or press CANCEL");

        int fileValue = chooser.showDialog(null, "Choose");
        // If there was no file selected here, later we will return the empty csvMap with makeMap()
        if(fileValue == JFileChooser.APPROVE_OPTION)
        {
            this.chosenFile = chooser.getSelectedFile();
        }
    }

    /**
     * Makes a hashmap which maps the fluxes to the correct reactions in the network.
     * @return the flux map or empty map if the user did not upload a file.
     */
    public HashMap<String, Double> makeMap() {
        HashMap<String, Double> csvMap = new HashMap<>();
        if (chosenFile == null) {
            return csvMap;
        }
        String line = "";
        try {
            // parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(chosenFile));
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split("\t", 0);
                if (!Objects.equals(values[0], "reaction_id")) {
                    String key = "";
                    String[] splitValues = values[0].split("_",0);
                    if (splitValues.length == 4) {
                        key = splitValues[0].concat(splitValues[2]);
                    } else {
                        key = splitValues[0].concat(splitValues[1]);
                    }
                    csvMap.put(key, Double.parseDouble(values[1]));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return csvMap;
    }
}
