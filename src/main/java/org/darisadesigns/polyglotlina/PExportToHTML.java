package org.darisadesigns.polyglotlina;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PExportToHTML {

    public static void exportHTML(String txtSavePath, String text2, String nakedText, boolean selected, boolean selected2,
            boolean selected3, String text3, String text4, boolean selected4, boolean selected5, boolean selected6,
            boolean selected7, boolean selected8, String chapterOrder, DictCore core) throws IOException {
        // TODO Auto-generated method stub

         Path path = Paths.get(txtSavePath);
        
        // try-with-resources sulkee writerin automaattisesti, vaikka tulisi virhe
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            
            // Open the HTML File
            writer.write("<!DOCTYPE html>\n<html lang=\"fi\">\n<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <title>Conlang Dictionary</title>\n");
            writer.write("    <style>body{font-family:sans-serif;} .sana{font-weight:bold; color:#2c3e50;} .luokka{font-style:italic; color:#7f8c8d;}</style>\n");
            writer.write("</head>\n<body>\n");
            writer.write("    <h1>Conlang Dictionary Test File</h1>\n");

            // Add data
            writer.write("    <ul>\n");
            writer.write("    <li>Test1</li>\n");
            writer.write("    <li>Test2</li>\n");

            // Close the HTML file
            writer.write("    </ul>\n</body>\n</html>");

        } catch (IOException e) {
            System.err.println("Error while exporting dictionary: " + e.getMessage());
        }
    }

    // Helper to escape special characters (<, >, &) in HTML
    private static String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;");
    }

    
}
