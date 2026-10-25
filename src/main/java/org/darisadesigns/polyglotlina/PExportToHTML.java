/*
 * Copyright (c) 2016-2026, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: MIT Licence
 * See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.darisadesigns.polyglotlina;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PExportToHTML {

    private static String _txtForewordNakedTxt = "";
    private static boolean _chkConLocal = false;
    private static boolean _chkLocalCon = false;
    private static boolean _chkOrtho = false;
    private static String _txtSubtitle = "";
    private static String _txtTitle = "";
    private static boolean _chkGloss = false;
    private static boolean _chkGrammar = false;
    private static boolean _chkEtymology = false;
    private static boolean _chkPrintConjugations = false;
    private static boolean _chkPrintPhrases = false;
    private static String _chapterOrder = "";
    private static DictCore _core;
    private static BufferedWriter writer;

    public static void exportHTML(String txtSavePath, String txtForewordNakedTxt, boolean chkConLocal, boolean chkLocalCon, boolean chkOrtho, String txtSubtitle, String txtTitle, boolean chkGloss, boolean chkGrammar, boolean chkEtymology, boolean chkPrintConjugations, boolean chkPrintPhrases, String chapterOrder, DictCore core) throws IOException {
        // Set up received parameters to private variables to make calling each builder function neater
        _txtForewordNakedTxt = txtForewordNakedTxt;
        _chkConLocal = chkConLocal;
        _chkLocalCon = chkLocalCon;
        _chkOrtho = chkOrtho;
        _txtSubtitle = txtSubtitle;
        _txtTitle = txtTitle;
        _chkGloss = chkGloss;
        _chkGrammar = _chkGrammar;
        _chkEtymology = _chkEtymology;
        _chkPrintConjugations = chkPrintConjugations;
        _chkPrintPhrases = chkPrintPhrases;
        _chapterOrder = chapterOrder;
        _core = core;
        
        Path filePath = Paths.get(txtSavePath);

        String mainTitle = core.conLabel();

        try {
            writer = Files.newBufferedWriter(filePath);
            
            // Open the HTML File
            writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <title>" + mainTitle +"</title>\n");
            // TODO: Styling
            writer.write("    <style>body{font-family:sans-serif;}" +
            ".frontpage{text-align: center;}"
            + "</style>\n");
            writer.write("</head>\n<body>\n");
            buildFrontPage(mainTitle);
            
            // Controls like "expand/collapse all etymology trees (collapsed by default)" here or somewhere else that makes sense

            // TODO: Author Foreword
            writer.write("<p>" + txtForewordNakedTxt + "</p>");

            // TODO: Orthography

            // TODO: Gloss Key

            // TODO: Dictionary: Conlang to Local
            // Conlang word
            // Part of Speech - Pronunciation
            // Romanization
            // Lexical classes
            // Local word
            // \n
            // Longer meaning
            // \n
            // Conjugations
            // \n
            // Etymology tree \n Etymology notes, collapsed by default

            // TODO: Dictionary: Local to Conlang
            // Local word
            // \n
            // Conlang word
            // Part of Speech - Pronunciation
            // Romanization
            // Lexical classes
            // \n
            // Longer meaning
            // \n
            // Conjugations
            // \n
            // Etymology tree \n Etymology notes, collapsed by default

            // TODO: Phrasebook

            // TODO: Grammar + Subheaders for each chapter

            // Close the HTML file
            writer.write("    </ul>\n</body>\n</html>");
            writer.close();

        } catch (IOException e) {
            System.err.println("Error while exporting dictionary: " + e.getMessage());
        }
    }

    private static void buildFrontPage(String mainTitle){
        try{
            String author = _core.getPropertiesManager().getCopyrightAuthorInfo();
            writer.write("<div class=\"frontpage\">");
            writer.write("<h1>" + mainTitle +"</h1>\n");
            if (_txtSubtitle.length() > 0) writer.write("<h2>"+ _txtSubtitle + "</h2>");
            if (author.length() > 0) writer.write("<p>" + author + "</p>");
            writer.write("</div>\n<hr>");
        } catch (IOException e) {
            System.err.println("Error while building front page: " + e.getMessage());
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
