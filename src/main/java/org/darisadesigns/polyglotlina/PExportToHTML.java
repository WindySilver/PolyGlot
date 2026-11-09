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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import org.darisadesigns.polyglotlina.CustomControls.GrammarChapNode;
import org.darisadesigns.polyglotlina.CustomControls.GrammarSectionNode;
import org.darisadesigns.polyglotlina.Desktop.DesktopPropertiesManager;
import org.darisadesigns.polyglotlina.Nodes.ConWord;
import org.darisadesigns.polyglotlina.Nodes.ConjugationNode;
import org.darisadesigns.polyglotlina.Nodes.ConjugationPair;
import org.darisadesigns.polyglotlina.Nodes.EtyExternalParent;
import org.darisadesigns.polyglotlina.Nodes.PhraseNode;
import org.darisadesigns.polyglotlina.Nodes.PronunciationNode;
import org.darisadesigns.polyglotlina.Nodes.TypeNode;
import org.darisadesigns.polyglotlina.Nodes.WordClass;
import org.darisadesigns.polyglotlina.Nodes.ImageNode;
import org.darisadesigns.polyglotlina.Nodes.EtyTreeNode;
import org.darisadesigns.polyglotlina.WebInterface;

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
    // Regex patterns for parsing image tags
    private static final Pattern IMG_PATTERN_GRAMMAR = Pattern.compile("<img\\s+src=\"(\\d+);\">");
    private static final Pattern IMG_PATTERN_OTHERS = Pattern.compile("<img\\s+src=\"(\\d+)\">");

    public static void exportHTML(String txtSavePath, String txtForeword, boolean chkConLocal, boolean chkLocalCon, boolean chkOrtho, String txtSubtitle, String txtTitle, boolean chkGloss, boolean chkGrammar, boolean chkEtymology, boolean chkPrintConjugations, boolean chkPrintPhrases, boolean chkEmbedCon, boolean chkEmbedLoc, String chapterOrder, DictCore core) throws IOException {
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
            
            // Put the fonts into CSS format, including encoding for embedding them if need be (embedding is necessary for custom fonts like the one used by the example language Lodenkur)
            String fonts = encodeFonts(chkEmbedCon, chkEmbedLoc);

            // Open the HTML File
            writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <title>" + _mainTitle +"</title>\n");
            // TODO: Styling
            writer.write("    <style>body{}" +
            "#frontpage{text-align: center;}" +
            "#TOC * {list-style-type: none;}" +
            "table, th, td {border: 1px solid; border-collapse: collapse; text-align: center;}" +
            "th, td {padding: 0.3em;}" +
            fonts +
            ".branch { display: flex; align-items: center; }" +
            ".group  { display: flex; flex-direction: column; }" +
            ".stem   { width: 20px; border-top: 1px solid #888; }" +
            ".node   { padding: 2px 6px; margin: 4px 0; border: 1px solid #888; white-space: nowrap; }" +
            
            "/* each sibling gets a stub to the vertical bar, plus a bar segment */" +
            ".item { position: relative; display: flex; align-items: center; }" +
            ".item::before, .item::after { content: \"\"; position: absolute; border: 0 solid #888; }" +

            ".parents > .item  { padding-right: 20px; }" +
            ".children > .item { padding-left: 20px; }" +
            ".parents > .item { justify-content: flex-end; }" +

            ".parents > .item::before  { right: 0; width: 20px; top: 50%; border-top-width: 1px; }" +
            ".parents > .item::after   { right: 0; top: 0; bottom: 0; border-left-width: 1px; }" +
            ".children > .item::before { left: 0; width: 20px; top: 50%; border-top-width: 1px; }" +
            ".children > .item::after  { left: 0; top: 0; bottom: 0; border-left-width: 1px; }" +

            "/* trim the bar so it only spans first sibling's center to last sibling's center */" +
            ".item.first::after { top: 50%; }" +
            ".item.last::after  { bottom: 50%; }" +
            ".item.only::after  { display: none; }" +
            "</style>\n");
            writer.write("<script>function openAll() {document.body.querySelectorAll('details')" +
                ".forEach((e) => {(e.hasAttribute('open')) ?" +
                "e.removeAttribute('open') : e.setAttribute('open',true);" +
                "})}" +
                "function closeAll() {document.body.querySelectorAll('details')" +
                ".forEach((e) => {(e.hasAttribute('open')) ?" +
                "e.removeAttribute('open') : e.setAttribute('open',false);" +
                "})}" +
                "</script>");
            writer.write("</head>\n<body>\n");
            buildFrontPage();
            
            buildTOC();

            if(_txtForeword.length() > 0) buildForeword();

            if(_chkOrtho) buildOrthography();

            if(_chkGloss) buildGlossKey();

            if(_chkConLocal) buildConToLocal();

            if (_chkLocalCon) buildLocalToCon();

            if (_chkPrintPhrases) buildPhrases();

            if (_chkGrammar) buildGrammar();

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
        // Update: Changing the local font before exporting seems to make .isBold() and .isItalic() work (for confont they seem to work already)
        // Update2: Tried to fix this. Saw so much wackery occurring with fonts' italics and bolding (especially localfont's) that I'm now very disoriented and decided to give up
        /* Update3: Fonts with bolding and italics in their name (e.g. Times New Roman or Open Sans) seem to work all the time at least to some degree (YMMV depending on font),
        probably because the font name points to the right styling, so locallang's style being set plain somewhere doesn't matter. Seems like a logic error in setting the locallang's fontstyle.
        A super low-priority thing to fix since you should be selecting font files directly instead of OS selection but it's been bothering me enough that I keep returning to this issue.
        This is as far as I'll go with this, though. Might add a more detailed explanation of this behavior into OS selection window for future new users. */
        // TODO for someone who cares enough to deep-dive into fonts: Fix the aforementioned logic error with locallang fontstyle

        // Embed fonts if options for that were selected and the fonts have been cached (in other words, changed from default)
        // Very low-priority TODO for someone who wants to embed the Charis font: Embed default font. Location: UNICODE_FONT_LOCATION
        if (conB && _core.getPropertiesManager().getCachedFont() != null){
            ret = "@font-face {\nfont-family: '" + con.getFontName() + "';\n";
            ret = ret + "src: url('data:font/woff2;charset=utf-8;base64," + Base64.getEncoder().encodeToString(_core.getPropertiesManager().getCachedFont()) + "') format('woff2');}\n";
        }
        if (locB && _core.getPropertiesManager().getCachedLocalFont() != null){
            ret = ret + "@font-face {\nfont-family: '" + loc.getFontName() + "';\n";
            ret = ret + "src: url('data:font/woff2;charset=utf-8;base64," + Base64.getEncoder().encodeToString(_core.getPropertiesManager().getCachedLocalFont()) + "') format('woff2');}\n";
        }

        // Build the conlang and locallang styles
        ret = ret + ".conlang {font-family: \"" + con.getFontName() + "\"; font-size: " + con.getSize() + "px;}\n";
        ret = ret + ".locallang {font-family: \"" + loc.getFontName() + "\"; font-size: " + loc.getSize() + "px;}\n";

        return ret;
    }

    private static void buildFrontPage(){
        try{
            String author = _core.getPropertiesManager().getCopyrightAuthorInfo();
            writer.write("<div id=\"frontpage\">");
            writer.write("<h1>" + _mainTitle +"</h1>\n");
            if (_txtSubtitle.length() > 0) writer.write("<h2>"+ _txtSubtitle + "</h2>");
            if (author.length() > 0) writer.write("<p>" + encodeImages(WebInterface.archiveHTML(author, _core), IMG_PATTERN_OTHERS) + "</p>");
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
            if (_chkEtymology){
                writer.write("<button onclick=\"openAll()\">Open all etymology trees</button>");
                writer.write("<button onclick=\"closeAll()\">Close all etymology trees</button>");
            }

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
                        if (_chkGrammar) {
                            writer.write("<li><a href=\"#" + grammarTitle + "\">" + grammarTitle + "</a></li>\n");
                            int subheaders = 1;
                            if(_core.getGrammarManager().getChapters().length > 0){
                                writer.write("<ul>\n");
                                for (GrammarChapNode curNode : _core.getGrammarManager().getChapters()) {
                                    writer.write("<li><a href=\"#GrammarC" + subheaders + "\">" + curNode.getName() + "</a></li>\n");
                                    subheaders++;
                                }
                                writer.write("</ul>\n");
                            }
                        }
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
            _txtForeword = encodeImages(WebInterface.archiveHTML(_txtForeword.replaceAll("\n", "<br>"), _core), IMG_PATTERN_OTHERS);
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

            for (PronunciationNode curNode : _core.getPronunciationMgr().getPronunciations()) {
                // Check for regex usage
                if(curNode.getValue().contains("$") || curNode.getValue().contains("^")) usesRegex = true;
                // Build the next table row
                tableBuilding = tableBuilding + "<tr>\n<td class=\"conlang\">" + isolateRegex(curNode.getValue()) + "</td>\n<td class=\"locallang\">" + curNode.getPronunciation() + "</td>\n</tr>\n";
             }

            if(usesRegex) writer.write("<p>Symbols $ and ^ indicate that elements of orthography must appear at the beginning or the end of a word.</p>\n");
            writer.write(tableBuilding + "\n</table></div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building orthography: " + e.getMessage());
        }
    }

    // Isolates regex characters from the conlang font class
    private static String isolateRegex(String pronunciation){
        String ret = pronunciation;
        if(ret.contains("^")) ret = "<span class=\"locallang\">^</span>" + ret.substring(1);
        if(ret.contains("$")) ret = ret.substring(0, ret.length()-1) + "<span class=\"locallang\">$</span>";
        return ret;
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

    // TODO: Proper formatting
    private static void buildConToLocal() throws Exception {
        try{
            if (posNames == null) fetchPOS();
            writer.write("<div id=\"conLocal\"><h2 id=\""+ conToLocalLink + "\">" + conToLocalTitle + "</h2>\n");

            String activeLetter = "";
            for (ConWord curWord : _core.getWordCollection().getWordNodes()) {

                // Build letter sectioning when active first letter changes
                if (!curWord.getValue().substring(0,1).equals(activeLetter)){
                    activeLetter = curWord.getValue().substring(0,1);
                    writer.write("<h3 id=\"con-" + activeLetter + "\"><u><b class=\"conlang\">" + activeLetter +"</b> words</u></h3>\n\n");
                }

                // Build singular dictionary entry
                writer.write("<h4 id=\"" + curWord.getValue().replaceAll(" ", "") + "\" class=\"conlang\">" + curWord.getValue() + "</h4>\n");

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
                if (stripHTML(curWord.getDefinition(), false).trim().length() > 0) writer.write("<br><p><b>Longer definition</b></p><p>" + encodeImages(WebInterface.archiveHTML(curWord.getDefinition().trim(), _core), IMG_PATTERN_OTHERS) + "</p><br>\n");

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
    
    // TODO: Proper formatting
    private static void buildLocalToCon() {
        try{
            if (posNames == null) fetchPOS();
            writer.write("<div id=\"localCon\"><h2 id=\""+ localToConLink + "\">" + localToConTitle + "</h2>\n");
            
            String activeLetter = "";
            for (ConWord curWord : _core.getWordCollection().getNodesLocalOrder()) {
                // Build letter sectioning when active first letter changes
                if (!curWord.getLocalWord().substring(0,1).equals(activeLetter)){
                    activeLetter = curWord.getLocalWord().substring(0,1);
                    writer.write("<h3 id=\"loc-" + activeLetter + "\"><u><b class=\"conlang\">" + activeLetter +"</b> words</u></h3>\n\n");
                }

                // Build singular dictionary entry
                writer.write("<h4 id=\"" + curWord.getLocalWord().replaceAll(" ", "") + "\" class=\"locallang\">" + curWord.getLocalWord() + "</h4><br>\n");
                writer.write("<p class=\"conlang\">" + curWord.getValue() + "</p>\n");
                
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
                
                // If longer definition exists, write it
                if (stripHTML(curWord.getDefinition(), false).trim().length() > 0) writer.write("<br><p><b>Longer definition</b></p><p>" + encodeImages(WebInterface.archiveHTML(curWord.getDefinition().trim(), _core), IMG_PATTERN_OTHERS) + "</p><br>\n");
                
                // If word has conjugations and they've been selected for exporting, write them
                if (_chkPrintConjugations) buildConJugations(curWord);

                // If word has etymology and it has been selected for exporting, build the etymology tree
                if (_chkEtymology && _core.getEtymologyManager().hasEtymology(curWord)) buildEtymologyTree(curWord);
            }            
            
            writer.write("</div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building locallang to conlang dictionary: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error while building locallang to conlang dictionary: " + e.getMessage());
        }
    }

    private static void buildPhrases() {
        try{
            writer.write("<div id=\"phrasebook\"><h2 id=\""+ phraseTitle + "\">" + phraseTitle + "</h2>\n");

            for (PhraseNode curNode : _core.getPhraseManager().getAllValues()) {

                // Phrase's title
                writer.write("<h3>" + curNode.getGloss() + "</h3>\n");

                // Phrase
                writer.write("<p>" + _core.localLabel() + " Phrase: " + curNode.getLocalPhrase() +"</p>\n");

                // Phrase in conlang
                writer.write("<p>" + _core.conLabel() + " Phrase: <span class=\"conlang\">" + curNode.getConPhrase() +"</span></p>\n");

                // Pronunciation, if any
                if (curNode.getPronunciation().length() > 0) writer.write("<p>Pronunciation: /" + curNode.getPronunciation() +"/</p>\n");

                // Notes, if any
                if (curNode.getNotes().length() > 0) writer.write("<p>Notes: " + curNode.getNotes() +"</p>\n");
            }
            writer.write("</div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building phrasebook: " + e.getMessage());
        }
    }

    private static void buildGrammar(){
        try{
            writer.write("<div id=\"grammar\"><h2 id=\""+ grammarTitle + "\">" + grammarTitle + "</h2>\n");
            int subheaders = 1;
            int subSubHeaders = 1;
            for (GrammarChapNode curNode : _core.getGrammarManager().getChapters()) {
                writer.write("<h3 id=\"GrammarC" + subheaders + "\">" + curNode.getName() + "</h3>\n");
                subheaders++;
                
                for (int i = 0; i < curNode.getChildCount(); i++) {
                    GrammarSectionNode curSec = (GrammarSectionNode) curNode.getChild(i);
                    writer.write("<h4 id=\"GrammarSC" + subSubHeaders + "\">" + curSec.getName() + "</h4>\n");
                    subSubHeaders++;
                    
                    writer.write(formatGrammar(curSec.getSectionText()));
                }
            }
            writer.write("</div>\n<hr>\n\n");
        } catch (IOException e) {
            System.err.println("Error while building grammar: " + e.getMessage());
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
            writer.write("<p>" + curPair.label + ": <span class=\"conlang\">" + declension + "</span></p>\n");
        }
    }

    private static EtyTreeNode collectParents(int wordID) {

        EtyTreeNode node = new EtyTreeNode("<a href=\"#" + _core.getWordCollection().getNodeById(wordID).getValue().replaceAll(" ", "") + "\">" +
        _core.getWordCollection().getNodeById(wordID).getValue() + "</a>");

        // Collect any external parents first since they never have any parents of their own
        for (EtyExternalParent extPar : _core.getEtymologyManager().getWordExternalParents(wordID)) {
            node.getParents().add(new EtyTreeNode(extPar.toString()));
        }

        // In-language parents need to be gone through recursively
        for (Integer parentID : _core.getEtymologyManager().getWordParentsIds(wordID)) {
            node.getParents().add(collectParents(parentID));
        }

        return node;
    }

    private static EtyTreeNode collectChildren(int wordID) {
        EtyTreeNode node = new EtyTreeNode("<a href=\"#" + _core.getWordCollection().getNodeById(wordID).getValue().replaceAll(" ", "") + "\">" +
        _core.getWordCollection().getNodeById(wordID).getValue() + "</a>");

        for (Integer childID : _core.getEtymologyManager().getChildren(wordID)) {
            node.getChildren().add(collectChildren(childID));
        }

        return node;
    }

    /**
     * Handles rendering the entire etymology tree
     * @param root The root word
     * @throws IOException
     */
    private static void renderFullTree(EtyTreeNode root) throws IOException {
        writer.write("<div class=\"branch\">\n");

        if (!root.getParents().isEmpty()) {
            renderBranchGroup(root.getParents(), true);
            writer.write("<div class=\"stem\"></div>\n");
        }

        writer.write("<div class=\"node\"><b>" + root.getLabel() + "</b></div>\n");

        if (!root.getChildren().isEmpty()) {
            writer.write("<div class=\"stem\"></div>\n");
            renderBranchGroup(root.getChildren(), false);
        }

        writer.write("</div>\n");
    }

    /**
     * Renders a column of sibling branches
     * @param nodes The nodes of the branch
     * @param parents Decides whether the branch group is for parents or children
     * @throws IOException
     */
    private static void renderBranchGroup(List<EtyTreeNode> nodes, boolean parents) throws IOException {
        writer.write("<div class=\"group " + (parents ? "parents" : "children") + "\">\n");
        for (int i = 0; i < nodes.size(); i++) {
            writer.write("<div class=\"item " + branchPosition(i, nodes.size()) + "\">\n");
            renderBranch(nodes.get(i), parents);
            writer.write("</div>\n");
        }
        writer.write("</div>\n");
    }

    /**
     * Renders a singular branch
     * @param node The node whose branch is being rendered
     * @param parents Decides whether the branch is for parents or children
     * @throws IOException
     */
    private static void renderBranch(EtyTreeNode node, boolean parents) throws IOException {
        List<EtyTreeNode> next = parents ? node.getParents() : node.getChildren();
        String nodeHtml = "<div class=\"node\">"
                + node.getLabel() + "</div>\n";

        writer.write("<div class=\"branch\">\n");
        if (parents) {
            if (!next.isEmpty()) {
                renderBranchGroup(next, true);
                writer.write("<div class=\"stem\"></div>\n");
            }
            writer.write(nodeHtml);
        } else {
            writer.write(nodeHtml);
            if (!next.isEmpty()) {
                writer.write("<div class=\"stem\"></div>\n");
                renderBranchGroup(next, false);
            }
        }
        writer.write("</div>\n");
    }

    /**
     * Determines the branch's position in the branch group
     * @param i The branch's index in the group
     * @param size The total number of branches in the group
     * @return The position of the branch in the group
     */
    private static String branchPosition(int i, int size) {
        if (size == 1) return "only";
        if (i == 0) return "first";
        if (i == size - 1) return "last";
        return "mid";
    }

    private static void buildEtymologyTree(ConWord curWord) throws IOException{
        writer.write("<br><details>\n<summary>Etymology</summary>\n");

        // Build the data structure for curWord's etymology tree for rendering
        EtyTreeNode root = new EtyTreeNode(curWord.getValue());
        root.getParents().addAll(collectParents(curWord.getId()).getParents());
        root.getChildren().addAll(collectChildren(curWord.getId()).getChildren());
        renderFullTree(root);

        // If word has etymology notes, write them
        if(stripHTML(curWord.getEtymNotes(), true).length() > 0) writer.write("<p>" + curWord.getEtymNotes().trim() + "</p>\n");
        
        writer.write("</details>\n");
    }

    // Formats grammar section text to do linebreaks and replaces the deprecated <font> tag with correct tags
    private static String formatGrammar(String grammar){

        // Linebreaks
        String ret = grammar.replaceAll("\n</font><font", "\n</font><br><font");

        // Styling
        String conFont = _core.getPropertiesManager().getFontConFamily();
        String locFont = _core.getPropertiesManager().getFontLocalFamily();
        ret = ret.replaceAll("face=\"" + conFont + "\"", "class=\"conlang\" ")
                 .replaceAll("face=\"" + locFont + "\"", "class=\"locallang\" ")
                 .replaceAll("size=\"", "style=\"font-size: ")
                 .replaceAll("\"color=\"", "px; color:")
                 .replaceAll("\">", ";\">");

        // Replace opening and closing <font> tags with <p> (for full paragraphs) (and watch out for images that are at the end of a chapter!)
        ret = ret.replaceFirst("<font", "<p");
        if (ret.lastIndexOf("</font>") > -1) ret = ret.substring(0, ret.lastIndexOf("</font>")) + "</p>" + ret.substring(ret.lastIndexOf("</font>")+7);

        // Iterate through the paragraphs using line breaks to get <span> tags (font changes in the middle of paragraph) right
        String[] lines = ret.split("\n");
        String finRet = "";
        String helper = "";
        for (String line : lines){
            helper = line.replaceAll("</font><br><font", "</p><br><p")
                         .replaceFirst("</font><font", "<span")
                         .replaceAll("<font", "<span")
                         .replaceAll("</font>", "</span>");
            if (helper.indexOf("<span") > -1) helper = helper + "</span>";

            // Encode any image on this line
            if(helper.indexOf("<img") != -1) helper = encodeImages(helper, IMG_PATTERN_GRAMMAR);

            // If the text line is empty, replace it with a <br> tag for correct line break
            if (helper.indexOf("<img") == -1 && helper.endsWith(";\">")) finRet = finRet + "\n<br>\n";
            else finRet = finRet + helper;
        }

        // Clean up any wrongly ordered paragraph-closers, remove unnecessary <br> tags and fix <br></p> ordering
        finRet = finRet.replaceAll("</p></span>", "</span></p>")
                       .replaceAll("</p><br><p", "\n</p><p")
                       .replaceAll("<br>\n\n</p>", "</p>\n\n<br>");
        return finRet;
    }

    // Encodes embedded images into Base64 and adds them to the <img> tag
    private static String encodeImages(String line, Pattern pattern){
        Matcher matcher = pattern.matcher(line);
        StringBuilder sb = new StringBuilder();

        // Replace found IDs with encoded images
        while (matcher.find()) {
            int imageId = Integer.parseInt(matcher.group(1));
            String replacement = "<img src=\"data:image/png;base64," + Base64.getEncoder().encodeToString(_core.getImageCollection().getNodeById(imageId).getImageBytes()) + "\">";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        // Append any remaining text after the last match
        matcher.appendTail(sb);

        return sb.toString();
    }

    // Helper to escape special characters (<, >, &) in HTML
    private static String escapeHTML(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;");
    }

    
}
