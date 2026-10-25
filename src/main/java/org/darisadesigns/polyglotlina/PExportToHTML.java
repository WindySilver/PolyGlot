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

    public static void exportHTML(String txtSavePath, String txtForewordNakedTxt, boolean chkConLocal, boolean chkLocalCon, boolean chkOrtho, String txtSubtitle, String txtTitle, boolean chkGloss, boolean chkGrammar, boolean chkEtymology, boolean chkPrintConjugations, boolean chkPrintPhrases, String chapterOrder, DictCore core) throws IOException {
        Path filePath = Paths.get(txtSavePath);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            
            // Open the HTML File
            writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            // TODO: Proper title based on txtTitle (or lack thereof)
            writer.write("    <title>Conlang Dictionary</title>\n");
            // TODO: Styling
            writer.write("    <style>body{font-family:sans-serif;}</style>\n");
            writer.write("</head>\n<body>\n");
            // TODO: Handle unincluded title, subtitle, foreword
            writer.write("    <h1>" + txtTitle +"</h1>\n");
            writer.write("<h2>"+ txtSubtitle + "</h2>");

            // TODO: Add the real data
            writer.write("    <ul>\n");
            writer.write("    <li>Test1</li>\n");
            writer.write("    <li>Test2</li>\n");
            
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
