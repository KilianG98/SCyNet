package org.cytoscape.sample.internal;

import javax.swing.*;
import java.io.File;

public class FileChoosing {
    private File chosenFile;

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
            if (fileName.substring(fileName.length() - 4, 0).equals(".csv")) {
                this.chosenFile = chooser.getSelectedFile();
            } else {
                System.out.println("This was no csv-file!");
            }
        }
    }
    public File giveFile(){
        return chosenFile;
    }
}
