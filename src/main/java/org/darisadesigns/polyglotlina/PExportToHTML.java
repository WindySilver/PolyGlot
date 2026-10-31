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

import java.awt.Font;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClass;

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
    private static Map<Integer, String> posNames = null;

    // Constants for chapter titles and links 
    private final static String forewordTitle = "Author Foreword";
    private final static String forewordLink = "Foreword";
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

    public static void exportHTML(String txtSavePath, String txtForeword, boolean chkConLocal, boolean chkLocalCon, boolean chkOrtho, String txtSubtitle, String txtTitle, boolean chkGloss, boolean chkGrammar, boolean chkEtymology, boolean chkPrintConjugations, boolean chkPrintPhrases, boolean chkEmbedCon, boolean chkEmbedLoc, String chapterOrder, DictCore core) throws IOException {
        // Set up received parameters to private variables to make calling each builder function neater
        _txtForeword = txtForeword;
        // TODO: While building the main sections, check if any of the chk booleans are needed outside this main builder
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
            
            // Put the fonts into CSS format, including encoding for embedding them if need be (embedding is necessary for custom fonts like the one used by the example language Lodenkur)
            String fonts = encodeFonts(chkEmbedCon, chkEmbedLoc);

            // Open the HTML File
            writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <title>" + _mainTitle +"</title>\n");
            // TODO: Styling
            writer.write("    <style>body{font-family:sans-serif;}" +
            "#frontpage{text-align: center;}" +
            "#TOC * {list-style-type: none;}" +
            "table, th, td {border: 1px solid; border-collapse: collapse; text-align: center;}" +
            "th, td {padding: 0.3em;}" +
            fonts +
            "</style>\n");
            writer.write("</head>\n<body>\n");
            buildFrontPage();
            
            // TODO: Controls like "expand/collapse all etymology trees (collapsed by default)" to TOC page or somewhere else that makes sense
            buildTOC();

            if(_txtForeword.length() > 0) buildForeword();

            if(chkOrtho) buildOrthography();

            if(chkGloss) buildGlossKey();

            if(chkConLocal) buildConToLocal();

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
            writer.write("    \n</body>\n</html>");
            writer.close();

        } catch (IOException e) {
            System.err.println("Error while exporting dictionary: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while exporting dictionary: " + e.getMessage());
        }
    }
    
    private static String encodeFonts(boolean conB, boolean locB){

        Font con = ((DesktopPropertiesManager)_core.getPropertiesManager()).getFontCon();
        Font loc = ((DesktopPropertiesManager)_core.getPropertiesManager()).getFontLocal();
        String ret = "";

        // Force font names into English to parse bolding and italics out of them because nothing else is doing the trick in this implementation
        String conName = con.getFontName(java.util.Locale.US).toLowerCase();
        String locName = loc.getFontName(java.util.Locale.US).toLowerCase();

        // Embed fonts if options for that were selected and the fonts have been cached (in other words, changed from default)
        // Very low-priority TODO: Find the default font location for embedding that?
        if(conB && _core.getPropertiesManager().getCachedFont() != null){
            ret = "@font-face {\nfont-family: '" + con.getFamily() + "';\n";
            ret = ret + "src: url('data:font/woff2;charset=utf-8;base64," + Base64.getEncoder().encodeToString(_core.getPropertiesManager().getCachedFont()) + "') format('woff2');}\n";
        }
        if(locB && _core.getPropertiesManager().getCachedLocalFont() != null){
            ret = ret + "@font-face {\nfont-family: '" + loc.getFamily() + "';\n";
            ret = ret + "src: url('data:font/woff2;charset=utf-8;base64," + Base64.getEncoder().encodeToString(_core.getPropertiesManager().getCachedLocalFont()) + "') format('woff2');}\n";
        }

        // Build the conlang and locallang styles
        ret = ret + ".conlang {font-family: " + con.getFamily() + "; font-size: " + con.getSize() + "px;" +  ((conB && _core.getPropertiesManager().getCachedFont() != null) ? "" : "font-weight: " + (conName.contains("bold") ? "bold" : "normal") + "; font-style: " + (conName.contains("italic") ? "italic" : "normal") + ";") + "}\n";
        ret = ret + ".locallang {font-family: " + loc.getFamily() + "; font-size: " + loc.getSize() + "px;" + ((locB && _core.getPropertiesManager().getCachedLocalFont() != null) ? "" : "font-weight: " + (locName.contains("bold") ? "bold" : "normal") + "; font-style: " + (locName.contains("italic") ? "italic" : "normal") + ";") + "}\n";

        return ret;
    }

    private static void buildFrontPage(){
        try{
            String author = _core.getPropertiesManager().getCopyrightAuthorInfo();
            writer.write("<div id=\"frontpage\">");
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

            writer.write("<div id=\"TOC\">\n<h2>Table of Contents</h2>\n<ul>\n");
            if (_txtForeword.length() > 0) writer.write("<li><a href=\"#" + forewordLink + "\">" + forewordTitle + "</a></li>\n");

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

    private static void buildForeword(){
        try{
            writer.write("<div id=\"foreword\"><h2 id=\""+ forewordLink + "\">" + forewordTitle + "</h2>\n");
            _txtForeword = _txtForeword.replaceAll("\n", "<br>");
            writer.write("<p>" + _txtForeword + "</p></div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building foreword: " + e.getMessage());
        }
    }

    private static void buildOrthography(){
        try{
            writer.write("<div id=\"orthography\"><h2 id=\""+ orthoTitle + "\">" + orthoTitle + "</h2>\n");
            // If orthography uses regex, the text about it needs to come before the table,
            // but regex usage is only determined while already building the table.
            // Because of that, the full table gets built into a string to allow for correct ordering of elements
            String tableBuilding = "<table>\n<tr>\n<th><b>Character(s)</b></th>\n<th><b>Pronunciation</b></th>\n</tr>\n";
            boolean usesRegex = false;

            // Low-priority TODO: Isolate regex characters from the conlang font class
            for (PronunciationNode curNode : _core.getPronunciationMgr().getPronunciations()) {
                // Check for regex usage
                if(curNode.getValue().contains("$") || curNode.getValue().contains("^")) usesRegex = true;
                // Build the next table row
                tableBuilding = tableBuilding + "<tr>\n<td class=\"conlang\">" + curNode.getValue() + "</td>\n<td class=\"locallang\">" + curNode.getPronunciation() + "</td>\n</tr>\n";
             }

            if(usesRegex) writer.write("<p>Symbols $ and ^ indicate that elements of orthography must appear at the beginning or the end of a word.</p>\n");
            writer.write(tableBuilding + "\n</table></div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building orthography: " + e.getMessage());
        }
    }

    private static void buildGlossKey(){
        try{
            writer.write("<div id=\"gloss\"><h2 id=\""+ glossLink + "\">" + glossTitle + "</h2>");
            writer.write("<table>\n<tr>\n<th><b>Part of Speech</b></th>\n<th><b>Gloss</b></th>\n</tr>\n");
            
            for (TypeNode curNode : _core.getTypes().getNodes()) {
                // I don't like leaving the Gloss column empty nor leaving it out altogether in case of missing abbreviations, so fill missing gloss with PoS itself
                writer.write("<tr>\n<td>" + curNode.getValue() + "</td>\n<td>" + (curNode.getGloss().length() > 0 ? curNode.getGloss() : curNode.getValue()) + "</td>\n</tr>\n");
             }
            writer.write("\n</table></div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building gloss key: " + e.getMessage());
        }
    }

    private static void buildConToLocal() throws Exception {
        try{
            if (posNames == null) fetchPOS();
            writer.write("<div id=\"conLocal\"><h2 id=\""+ conToLocalLink + "\">" + conToLocalTitle + "</h2>\n");

            String activeLetter = "";
            for (ConWord curWord : _core.getWordCollection().getWordNodes()) {

                // Build letter sectioning when active first letter changes
                if (!curWord.getValue().substring(0,1).equals(activeLetter)){
                    activeLetter = curWord.getValue().substring(0,1);
                    writer.write("<h3><u><b class=\"conlang\">" + activeLetter +"</b> words</u></h3>\n\n");
                }

                // Build singular dictionary entry
                writer.write("<h4 class=\"conlang\">" + curWord.getValue() + "</h4>\n");

                // If word has part of speech and/or pronunciation, write them (for formatting reasons, handle "both" and "just one" cases separately)
                if (posNames.containsKey(curWord.getWordTypeId()) && curWord.getPronunciation().length() > 0) writer.write("<p>" + posNames.get(curWord.getWordTypeId()) + " - /" + curWord.getPronunciation() + "/" + "</p>\n");
                else if (posNames.containsKey(curWord.getWordTypeId()) || curWord.getPronunciation().length() > 0) writer.write("<p>" + (posNames.containsKey(curWord.getWordTypeId()) ? posNames.get(curWord.getWordTypeId()) : "") + (curWord.getPronunciation().length() > 0 ? "/" + curWord.getPronunciation() + "/" : "") + "</p>\n");

                // If word has a romanization, write it romanization
                if (_core.getRomManager().getPronunciation(curWord.getValue()).length() > 0) writer.write("<p>Romanization: " + _core.getRomManager().getPronunciation(curWord.getValue()) + "</p>");

                // If word has lexical classes, write them
                String asso = "";
                String val = "";
                String free = "";
                if(!curWord.getClassValues().isEmpty()){
                    // Because the classes aren't automatically ordered by type, ordering needs to be built with builder strings
                    for (Entry<Integer, Integer> curEntry : curWord.getClassValues()) {
                        WordClass curClass = (WordClass) _core.getWordClassCollection()
                                .getNodeById(curEntry.getKey());
                        // Associative class
                        if (curClass.isAssociative()) asso = asso + "<p>" + curClass + ": " + _core.getWordCollection().getNodeById(curEntry.getValue()).getValue() + "</p>\n";
                        // Value class
                        else val = val + "<p>" + curClass + ": " + curClass.getValueById(curEntry.getValue()).getValue() + "</p>\n";
                    }
                }
                // Free text classes are saved separately from associative and value classes
                if (!curWord.getClassTextValues().isEmpty()) {                
                    for (Entry<Integer, String> curEntry : curWord.getClassTextValues()) {
                        // Only list the class if it has a value
                        if (!curEntry.getValue().trim().isEmpty()) free = free + ("<p>" + _core.getWordClassCollection().getNodeById(curEntry.getKey()) + ": " + curEntry.getValue() + "</p>\n");
                    }
                }
                // Ordering in the Lexical Class UI is Associative, Free Text, Values. Will follow the same logic
                if (asso.length() > 0 || val.length() > 0 || free.length() > 0) writer.write(asso + free + val);
                
                // If locallang translation exists, write it
                if (curWord.getLocalWord().length() > 0) writer.write("<p>Short definition: " + curWord.getLocalWord() + "</p>\n");

                // If longer definition exists, write it
                if (stripHTML(curWord.getDefinition(), false).trim().length() > 0) writer.write("<br><p><b>Longer definition</b></p><p>" + curWord.getDefinition().trim() + "</p><br>\n");

                // If word has conjugations and they've been selected for exporting, write them
                if (_chkPrintConjugations) buildConJugations(curWord);

                // If word has etymology and it has been selected for exporting, build the etymology tree
                if (_chkEtymology && _core.getEtymologyManager().hasEtymology(curWord)) buildEtymologyTree(curWord);
            }



            writer.write("</div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building conlang to local dictionary: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while building conlang to local dictionary: " + e.getMessage());
        }
    }

    // Collect the labels of Parts of Speech together for building a dictionary to minimize looking up their names
    private static void fetchPOS(){
        // A HashMap seems to be the best way to build a POS list since IDs can be larger than the number of POS
        try {
            posNames = Arrays.stream(_core.getTypes().getNodes())
            .collect(Collectors.toMap(TypeNode::getId, t -> t.getGloss().length() > 0 ? t.getGloss() : t.getValue()));
        } catch (IllegalStateException e){
            System.err.println("Error while working with parts of speech: " + e.getMessage());
        }
    }

    // Helper to strip unnecessary HTML formatting
    private static String stripHTML(String input, boolean stripBR){
        if(stripBR) return input.replace("<html>", "").replace("<head>", "").replace("</head>", "").replace("<body>", "").replace("</body>", "").replace("</html>", "").replace("<br>", "").trim();
        return input.replace("<html>", "").replace("<head>", "").replace("</head>", "").replace("<body>", "").replace("</body>", "").replace("</html>", "").trim();
    }


    private static void buildConJugations(ConWord curWord) throws IOException {
        for (ConjugationPair curPair : _core.getConjugationManager().getAllCombinedIds(curWord.getWordTypeId())) {
            ConjugationNode curNode = _core.getConjugationManager().getConjugationByCombinedId(curWord.getId(), curPair.combinedId);

            // if conjugation/declension exists, write it. It it doesn't, try to generate it
            String declension = "";
            if (curNode != null) {
                declension = curNode.getValue();
            } else {
                try {
                    declension = _core.getConjugationManager().declineWord(curWord, curPair.combinedId);
                } catch (Exception e) {
                    System.err.println("Problem generating " + curPair.label + " due to bad regex. Please check regex for word form.");
                }
            }
            writer.write("<p>" + curPair.label + ": " + declension + "</p>\n");
        }
    }

    private static void printParents(int wordID) throws IOException{
        // Write any external parents first since they're the oldest of their own part of branch
        if (_core.getEtymologyManager().getWordExternalParents(wordID).length > 0) {
            for (EtyExternalParent extPar : _core.getEtymologyManager().getWordExternalParents(wordID)){
                writer.write("<p>Ext parent: <span class=\"conlang\">" + extPar.toString() + "</span></p>\n");
            }
        }

        for (Integer parentID : _core.getEtymologyManager().getWordParentsIds(wordID)) {
            // Get to the end of the branch first
            if (_core.getEtymologyManager().getWordExternalParents(parentID).length > 0 || _core.getEtymologyManager().getWordParentsIds(parentID).length > 0){
                writer.write("<b>Start of main branch</b>");
                 printParents(parentID);}            
            // Write in-language parents when at the end of the branch
                writer.write("<p>Parent: <span class=\"conlang\">" + _core.getWordCollection().getNodeById(parentID) + "</span></p>\n");
        }
            writer.write("<b>↓</b>");
    }

    private static void printChildren(int wordID) throws IOException{
        for (Integer childID : _core.getEtymologyManager().getChildren(wordID)) {
            writer.write("<p>Child: <span class=\"conlang\">" + _core.getWordCollection().getNodeById(childID) + "</span></p>\n");
            if (_core.getEtymologyManager().getChildren(childID).length > 0) {
                writer.write("<b>↓</b>");
                printChildren(childID);}
        writer.write("<b>Next branching</b>");
        }
    }

    // TODO: Refine formatting
    private static void buildEtymologyTree(ConWord curWord) throws IOException{
        
        writer.write("<br><details>\n<summary>Etymology</summary>\n");

        // Get all the parents and write them from top to bottom
        printParents(curWord.getId());

        // Current word
        writer.write("<p>Current: <span class=\"conlang\">" + curWord.getValue() + "</span></p>\n");

        // Get all the children and write them from top to bottom
        printChildren(curWord.getId());

        // If word has etymology notes, write them
        if(stripHTML(curWord.getEtymNotes(), true).length() > 0) writer.write("<p>" + curWord.getEtymNotes().trim() + "</p>\n");
        
        writer.write("</details>\n");
    }

    // Helper to escape special characters (<, >, &) in HTML
    private static String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;");
    }

    
}
