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
import java.util.*;
import java.util.stream.*;

public class PExportToHTML {

    private static String _mainTitle = "";
    private static String _conlangName = "";
    private static String _localLangName = "";
    private static String _txtForeword = "";
    private static boolean _chkConLocal = false;
    private static boolean _chkLocalCon = false;
    private static boolean _chkOrtho = false;
    private static String _txtSubtitle = "";
    private static boolean _chkGloss = false;
    private static boolean _chkGrammar = false;
    private static boolean _chkEtymology = false;
    private static boolean _chkPrintConjugations = false;
    private static boolean _chkPrintPhrases = false;
    private static String _chapterOrder = "";
    private static DictCore _core;
    private static BufferedWriter writer;

    // Constants for chapter titles and links 
    private final static String forewordTitle = "Author Foreword";
    private final static String orthoTitle = "Orthography";
    private final static String glossTitle = "Gloss Key";
    private final static String phraseTitle = "Phrasebook";
    private final static String grammarTitle = "Grammar";
    private final static String glossLink = "Glossary";
    private final static String localToConLink = "LocalToCon";
    private final static String conToLocalLink = "ConToLocal";
    // Due to needing language names from core, these two cannot be constants
    private static String localToConTitle = "LocalToCon";
    private static String conToLocalTitle = "ConToLocal";

    public static void exportHTML(String txtSavePath, String txtForeword, boolean chkConLocal, boolean chkLocalCon, boolean chkOrtho, String txtSubtitle, String txtTitle, boolean chkGloss, boolean chkGrammar, boolean chkEtymology, boolean chkPrintConjugations, boolean chkPrintPhrases, String chapterOrder, DictCore core) throws IOException {
        // Set up received parameters to private variables to make calling each builder function neater
        _txtForeword = txtForeword;
        _chkConLocal = chkConLocal;
        _chkLocalCon = chkLocalCon;
        _chkOrtho = chkOrtho;
        _txtSubtitle = txtSubtitle;
        _chkGloss = chkGloss;
        _chkGrammar = chkGrammar;
        _chkEtymology = chkEtymology;
        _chkPrintConjugations = chkPrintConjugations;
        _chkPrintPhrases = chkPrintPhrases;
        _chapterOrder = chapterOrder;
        _core = core;
        
        Path filePath = Paths.get(txtSavePath);

        // Set up language names and titles that use them
        _conlangName = _core.conLabel();
        _localLangName = _core.localLabel();
        localToConTitle = "Dictionary: " + _localLangName + " to " + _conlangName;
        conToLocalTitle = "Dictionary: " + _conlangName + " to " + _localLangName;
        _mainTitle = txtTitle.length() > 0 ? txtTitle : _conlangName;

        try {
            writer = Files.newBufferedWriter(filePath);
            
            // Open the HTML File
            writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <title>" + _mainTitle +"</title>\n");
            // TODO: Styling
            writer.write("    <style>body{font-family:sans-serif;}" +
            ".frontpage{text-align: center;}" +
            ".TOC * {list-style-type: none;}" +
            "</style>\n");
            writer.write("</head>\n<body>\n");
            buildFrontPage();
            
            // TODO: Controls like "expand/collapse all etymology trees (collapsed by default)" to TOC page or somewhere else that makes sense
            buildTOC();

            // TODO: Author Foreword
            // TODO: Change linebreaks in _txtForeword to HTML line breaks
            writer.write("<p>" + _txtForeword + "</p>");

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

    private static void buildFrontPage(){
        try{
            String author = _core.getPropertiesManager().getCopyrightAuthorInfo();
            writer.write("<div class=\"frontpage\">");
            writer.write("<h1>" + _mainTitle +"</h1>\n");
            if (_txtSubtitle.length() > 0) writer.write("<h2>"+ _txtSubtitle + "</h2>");
            if (author.length() > 0) writer.write("<p>" + author + "</p>");
            writer.write("</div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building front page: " + e.getMessage());
        }
    }

    private static void buildTOC(){
        try{
            // Chapter order is given as a int ID list formatted as String
            int[] order = Arrays.stream(_chapterOrder.split(","))
                .mapToInt(Integer::parseInt)
                .toArray();

            writer.write("<div class=\"TOC\">\n<h2>Table of Contents</h2>\n<ul>\n");
            if (_txtForeword.length() > 0) writer.write("<li><a href=\"#Foreword\">" + forewordTitle + "</a></li>\n");

            for (int i : order){
                switch (i) {
                    case PGTUtil.CHAP_ORTHOGRAPHY:
                        if (_chkOrtho) writer.write("<li><a href=\"#" + orthoTitle + "\">" + orthoTitle + "</a></li>\n");
                        break;
                    case PGTUtil.CHAP_GLOSSKEY:
                        if (_chkGloss) writer.write("<li><a href=\"#" + glossLink + "\">" + glossTitle + "</a></li>\n");
                        break;
                    case PGTUtil.CHAP_LOCALTOCON:
                        if (_chkLocalCon) writer.write("<li><a href=\"#" + localToConLink + "\">" + localToConTitle + "</a></li>\n");
                        break;
                    case PGTUtil.CHAP_CONTOLOCAL:
                        if (_chkConLocal) writer.write("<li><a href=\"#" + conToLocalLink + "\">" + conToLocalTitle + "</a></li>\n");
                        break;
                    case PGTUtil.CHAP_PHRASEBOOK:
                        if (_chkPrintPhrases) writer.write("<li><a href=\"#" + phraseTitle + "\">" + phraseTitle + "</a></li>\n");
                        break;
                    case PGTUtil.CHAP_GRAMMAR:
                        if (_chkGrammar) writer.write("<li><a href=\"#" + grammarTitle + "\">" + grammarTitle + "</a></li>\n");
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized chapter key: " + i);
            }
        }
            writer.write("</ul></div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building table of contents: " + e.getMessage());
        } catch (IllegalArgumentException e){
            System.err.println("Error while building table of contents: " + e.getMessage());
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
