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
    private HashMap<String, Float> csvMap;

    public FileChoosing()
    {
        // JFileChooser-Objekt erstellen
        JFileChooser chooser = new JFileChooser();
        // Dialog zum Oeffnen von Dateien anzeigen
        int rueckgabeWert = chooser.showDialog(null, "Choose the corresponding csv-file.");
        /* Abfrage, ob auf "Öffnen" geklickt wurde */
        if(rueckgabeWert == JFileChooser.APPROVE_OPTION)
        {
            // Ausgabe der ausgewaehlten Datei
            String fileName = chooser.getSelectedFile().getName();
            this.chosenFile = chooser.getSelectedFile();
        }
    }

    public HashMap<String, Float> makeMap() {
        HashMap<String, Float> csvMap = new HashMap<>();
        if (chosenFile == null) {
            System.out.println("NO FILE");
            return csvMap;
        }
        String line = "";
        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(chosenFile));
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                String[] values = line.split("\t", 0); // don't truncate empty fields
                if (!Objects.equals(values[0], "reaction_id")) {
                    String key = "";
                    String[] splitValues = values[0].split("_",0);
                    if (splitValues.length == 4) {
                        key = splitValues[0].concat(splitValues[2]);
                    } else {
                        key = splitValues[0].concat(splitValues[1]);
                    }
                    csvMap.put(key, Float.parseFloat(values[1]));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return csvMap;
    }
}
