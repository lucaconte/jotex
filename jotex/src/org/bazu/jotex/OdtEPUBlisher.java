/*
 * OdtEPUBlisher.java
 * 
 * Copyright (c) 2011, Luca Conte. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
/**
 * TODO:
 *  -returns from footnote
 *  -tables border
 *  -List styles dotted or numbered wirh variants
 *  -image types support
 *  -remote images support (?) a lot of problems related
 * 
 */
package org.bazu.jotex;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.dom.TextImpl;
import org.bazu.jotex.fonts.SinglePathFontLocator;
import org.bazu.jotex.images.ByteArrayImageDataSource;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.OdfStylableElement;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.draw.DrawTextBoxElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeBodyElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeDocumentContentElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeScriptsElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.dom.element.style.StyleParagraphPropertiesElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextAElement;
import org.odftoolkit.odfdom.dom.element.text.TextBookmarkElement;
import org.odftoolkit.odfdom.dom.element.text.TextListItemElement;
import org.odftoolkit.odfdom.dom.element.text.TextNoteBodyElement;
import org.odftoolkit.odfdom.dom.element.text.TextNoteCitationElement;
import org.odftoolkit.odfdom.dom.element.text.TextNoteElement;
import org.odftoolkit.odfdom.dom.element.text.TextSElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawFrame;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawImage;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextHeading;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextList;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.w3c.dom.Node;

import com.adobe.dp.css.CSSLength;
import com.adobe.dp.css.CSSName;
import com.adobe.dp.css.CSSQuotedString;
import com.adobe.dp.css.CSSValue;
import com.adobe.dp.css.Selector;
import com.adobe.dp.css.SelectorRule;
import com.adobe.dp.epub.io.DataSource;
import com.adobe.dp.epub.io.OCFContainerWriter;
import com.adobe.dp.epub.ncx.TOCEntry;
import com.adobe.dp.epub.opf.BitmapImageResource;
import com.adobe.dp.epub.opf.NCXResource;
import com.adobe.dp.epub.opf.OPSResource;
import com.adobe.dp.epub.opf.Publication;
import com.adobe.dp.epub.opf.Resource;
import com.adobe.dp.epub.opf.StyleResource;
import com.adobe.dp.epub.ops.Element;
import com.adobe.dp.epub.ops.HTMLElement;
import com.adobe.dp.epub.ops.HyperlinkElement;
import com.adobe.dp.epub.ops.ImageElement;
import com.adobe.dp.epub.style.Stylesheet;
import com.adobe.dp.epub.util.TOCLevel;
import com.adobe.dp.otf.FontLocator;
import com.adobe.dp.xml.util.StringUtil;

public class OdtEPUBlisher {
    // config fields
    private boolean debugMode = false;
    private String epubLanguage;
    private String epubFilename;
    private String odtFilename;
    // END config fields

    private Publication ePub;
    private OdfTextDocument odt;
    private Stylesheet _stylesheet;
    private StyleResource _styleResource;
    private OPSResource currentResource;
    private OPSResource footnotesResource;
    private OPSResource coverResource;
    private XPath xpath;

    private String fontsPath;
    private String coverPath;
    private boolean _hasFootnotes=false;
    private boolean exposeBookmarks;




    private int maxFilesSize = 0;

    private Map<String, Element> bookmarks;
    private Set<HyperlinkElement> internalLink;

    private Set<String> classesForDebug = new HashSet<String>();

    public static NamespaceContext XPATH_ODT_NS_CTX = new NamespaceContext() {

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            // fo=urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0
            return null;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:drawing:1.0")) {
                return "draw";
            } else if (namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:style:1.0")) {
                return "style";
            } else if (namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:text:1.0")) {
                return "text";
            } else if (namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0")) {
                return "fo";
            } else if (namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:meta:1.0")) {
                return "meta";
            }

            return null;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals("draw")) {
                return "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";
            } else if (prefix.equals("style")) {
                return "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
            } else if (prefix.equals("text")) {
                return "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
            } else if (prefix.equals("fo")) {
                return "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0";
            } else if (prefix.equals("meta")) {
                return "urn:oasis:names:tc:opendocument:xmlns:meta:1.0";
            }

            return "";
        }
    };

    private int breaksCount = 0;

    private Stack<TOCLevel> tocEntriesBuffer;

    public OdtEPUBlisher() {

    }

    public void startRippingSession() throws Exception {

        Utils.processMetadata(getOdt().getMetaDom(), getEpub(), getXpath());


        // stylesPropsToCSS( getOdtDocument().getDocumentStyles().get,
        // className)
        // System.out.println(getOdtDocument().getDocumentStyles());
        extractDefaultStyles(getOdt().getDocumentStyles());

        if(getCoverPath()!=null&&getCoverPath().trim().length()>0){//a cover will be processed
            
           
            
            File fcover=new File(getCoverPath());
            if(fcover.exists()){
                try {
                    HTMLElement covelem=getCoverResource().getDocument().createElement("div");
                   
                    Resource r=addImage(fcover.getAbsolutePath(), covelem,getCoverResource());
                    if(r!=null){
                    	r.setId("cover-image");
	                    getEpub().addMetadata(null, "cover", "cover-image");
	                    //
	                    covelem.setClassName("cover");
	                    Selector selector = getStylesheet().getSimpleSelector("div", "cover");
	                    SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
	                    rule.set("width", new CSSName("100%"));
	                    rule.set("text-align", new CSSName("center"));
	                    getCoverResource().getDocument().getBody().add(covelem);
	                    //TODO: uncomment the following row for best practies. But doing this some readers put the cover at the epub's end and not at the epub's beginning 
	                    //getCoverResource().putSerializationAttribute(null, "linear", "no");
	                    getEpub().addToSpine(getCoverResource());
                    }
                } catch (Exception e) {
                    // Unable to set the cover
                    e.printStackTrace();
                }
             
            }
            
        }
        createNewResource();
        traverse(getOdt().getContentDom(), null);

        // processURLSSession(rootTOCEntry,p);
        processInternalLinksCrossReferences();
        if(isExposeBookmarks()){
            bookmarksToToc();
        }
        
        if(_hasFootnotes){
            setFootnotesCSSStyles();
            getEpub().addToSpine(getFootnotesResource());
        }
        if (getMaxFilesSize() > 0) {
            getEpub().splitLargeChapters(getMaxFilesSize() * 1024);
        }

        // include fonts
        // embed fonts
        // NB: on non-Windows platforms you need to supply your own
        // FontLocator implementation or place fonts in ~/.epubfonts
        if (getFontsPath() != null && getFontsPath().trim().length() > 0) {
            FontLocator fontLocator = new SinglePathFontLocator(getFontsPath());
            getEpub().cascadeStyles();
            getEpub().addFonts(getStyleResource(), fontLocator);
        }
        OCFContainerWriter writer = new OCFContainerWriter(new FileOutputStream(getEpubFilename()));
        getEpub().serialize(writer);

        
        //TODO: uncomment  for testing stylesheet optimization
        //Utils.optimizeStylesheet(getStyleResource());
        if (isDebugMode()) {
            printClassesFound();
        }
    }

    public void traverse(Node e, Element dstElement) throws Exception {
        classesForDebug.add(e.getClass().toString());
        boolean skipChildren = false;
        Element newElement = null;
        /**
         * Original ODT's element evalutation 
         */
        if (e instanceof TextNoteBodyElement) {// Corpo di una nota
            TextNoteBodyElement noteBody = (TextNoteBodyElement) e;
        } else if (e instanceof TextNoteCitationElement) {// Rimando ad una nota

        } else if (e instanceof OfficeTextElement) {// OFFICE
            OfficeTextElement ote = (OfficeTextElement) e;

        } else if (e instanceof OfficeScriptsElement) {// OFFICE

        } else if (e instanceof OfficeDocumentContentElement) {// OFFICE

        } else if (e instanceof OdfOfficeAutomaticStyles) {// OFFICE

        } else if (e instanceof OfficeBodyElement) {// OFFICE

        } else if (e instanceof TextSElement) {// ?
            TextSElement te = (TextSElement) e;

        } else if (e instanceof TextAElement) {// is an hyperlink
            TextAElement ta = (TextAElement) e;
            String ref = ta.getAttribute("xlink:href");

            HyperlinkElement a = getCurrentResource().getDocument().createHyperlinkElement("a");
            if (ref.startsWith("#")) {// internal Link
                a.setTitle(ta.getAttribute("xlink:href"));
                getInternalLink().add(a);
            } else {
                a.setExternalHRef(ref);
            }
            dstElement.add(a);
            // a.add("ciao");
            traverse(ta.getFirstChild(), a);
            skipChildren = true;

        } else if (e instanceof TextBookmarkElement) {// is bookmark in epub can
                                                      // be used to implement
                                                      // internal link anchors
            TextBookmarkElement ta = (TextBookmarkElement) e;

            HyperlinkElement a = getCurrentResource().getDocument().createHyperlinkElement("a");
            a.setId(ta.getAttribute("text:name"));
            dstElement.add(a);
            getBookmarks().put("#" + ta.getAttribute("text:name"), a);
        } else if (e instanceof TextNoteElement) {// Is a footnote container
            addFootnote((TextNoteElement) e, dstElement);
            skipChildren = true;
        } else if (e instanceof OdfDrawFrame) {
            OdfDrawFrame dframe = (OdfDrawFrame) e;
            // here can be captured resize options

        } else if (e instanceof DrawTextBoxElement) {
            DrawTextBoxElement didascalia = (DrawTextBoxElement) e;
            newElement = addImageBox(didascalia, dstElement);

        } else if (e instanceof OdfDrawImage) {
            addImage((OdfDrawImage) e, dstElement);

        } else if (e instanceof OdfTextList) {
            OdfTextList otl = (OdfTextList) e;
            dstElement = getCurrentResource().getDocument().getBody();
            /**
             * ul.a {list-style-type:circle;} ul.b {list-style-type:square;}
             * ol.c {list-style-type:upper-roman;} ol.d
             * {list-style-type:lower-alpha;}
             */
            Object n = getXpath().evaluate(
                    "//text:list-style[@style:name='" + otl.getTextStyleNameAttribute()
                            + "']/text:list-level-style-bullet", getOdt().getContentDom(), XPathConstants.NODE);
            if (n != null) {// is a bullet list
                newElement = getCurrentResource().getDocument().createElement("ul");
            } else {
                n = getXpath().evaluate(
                        "//text:list-style[@style:name='" + otl.getTextStyleNameAttribute()
                                + "']/text:list-level-style-number", getOdt().getContentDom(), XPathConstants.NODE);
                if (n != null) {
                    newElement = getCurrentResource().getDocument().createElement("ol");
                }
            }

            if (newElement != null) {
                dstElement.add(newElement);
                newElement.setClassName(otl.getTextStyleNameAttribute());
            }
            // title[@lang='eng']
        } else if (e instanceof TextListItemElement) {
            TextListItemElement li = (TextListItemElement) e;
            newElement = getCurrentResource().getDocument().createElement("li");
            dstElement.add(newElement);

        } else if (e instanceof TableTableElement) {
            TableTableElement otl = (TableTableElement) e;
            dstElement = getCurrentResource().getDocument().getBody();
            newElement = getCurrentResource().getDocument().createElement("table");
            dstElement.add(newElement);
            newElement.setClassName(otl.getStyleName());
            Selector selector = getStylesheet().getSimpleSelector(null, otl.getStyleName());
            SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
           
            rule.set("width", new CSSName("100%"));
            selector = getStylesheet().getSimpleSelector("table", null);
            rule = getStylesheet().getRuleForSelector(selector, true);
            rule.set("border-collapse", new CSSName("collapse"));
            rule.set("border", new CSSName("1px solid black"));
            selector = getStylesheet().getSimpleSelector("td",null);
            rule = getStylesheet().getRuleForSelector(selector, true);
            rule.set("border", new CSSName("1px solid black"));
            selector = getStylesheet().getSimpleSelector("tr", null);
            rule = getStylesheet().getRuleForSelector(selector, true);
            rule.set("border", new CSSName("1px solid black"));

        } else if (e instanceof TableTableRowElement) {
            TableTableRowElement otl = (TableTableRowElement) e;
            newElement = getCurrentResource().getDocument().createElement("tr");
            dstElement.add(newElement);
            newElement.setClassName(otl.getStyleName());

        } else if (e instanceof TableTableCellElement) {
            TableTableCellElement otl = (TableTableCellElement) e;
            newElement = getCurrentResource().getDocument().createElement("td");
            dstElement.add(newElement);
            newElement.setClassName(otl.getStyleName());
        } else if (e instanceof OdfTextHeading) {// text:p
            // System.out.println(e.getTextContent());
            OdfTextHeading oth = (OdfTextHeading) e;
            if (hasPageBreak(oth)) {
                createNewResource();
            }
            newElement = getCurrentResource().getDocument().createElement("h" + oth.getAttribute("text:outline-level"));
            if (dstElement != null) {
                dstElement.add(newElement);
            } else {
                getCurrentResource().getDocument().getBody().add(newElement);
            }
            newElement.setClassName(oth.getStyleName());
            if (oth.getTextContent() != null && oth.getTextContent().trim().length() > 0) {
                addTocEntry(oth.getTextContent(), Integer.parseInt(oth.getAttribute("text:outline-level")), newElement);
            }

            if (oth.getAutomaticStyle() != null) {// probabile che sia stato
                // oth.getAutomaticStyles() // modificato lo stile
                List<OdfStyleBase> classeCSS = new ArrayList<OdfStyleBase>();
                OdfStyleBase p = oth.getAutomaticStyle().getParentStyle();
                while (p != null) {
                    classeCSS.add(p);
                    p = p.getParentStyle();

                }
                Collections.reverse(classeCSS);
                for (OdfStyleBase odfStyleBase : classeCSS) {
                    stylesPropsToCSS(odfStyleBase.getStyleProperties(), newElement.getClassName());
                }
                stylesPropsToCSS(oth.getAutomaticStyle().getStyleProperties(), oth.getStyleName());
                if (newElement != null) {
                    newElement.setClassName(oth.getStyleName());
                }
                if (isDebugMode()) {
                    Utils.printStyleProps(oth.getAutomaticStyle().getStyleProperties());
                }
            }

            getOdt().getDocumentStyles().getElementsByTagName(oth.getTextStyleNameAttribute());

        } else if (e instanceof OdfTextParagraph) {// text:p
            // System.out.println(e.getTextContent());

            OdfTextParagraph otp = (OdfTextParagraph) e;
            if (hasPageBreak(otp)) {
                createNewResource();
            }

            newElement = getCurrentResource().getDocument().createElement("p");

            if (dstElement != null) {
                dstElement.add(newElement);
            } else {
                getCurrentResource().getDocument().getBody().add(newElement);
            }

            newElement.setClassName(otp.getStyleName().trim());

            if (otp.getAutomaticStyle() != null) {// probabile che sia stato
                                                  // modificato lo stile
                List<OdfStyleBase> classeCSS = new ArrayList<OdfStyleBase>();
                OdfStyleBase p = otp.getAutomaticStyle().getParentStyle();
                while (p != null) {
                    classeCSS.add(p);
                    p = p.getParentStyle();

                }
                Collections.reverse(classeCSS);
                for (OdfStyleBase odfStyleBase : classeCSS) {
                    stylesPropsToCSS(odfStyleBase.getStyleProperties(), newElement.getClassName());
                }

                // while(p!=null){
                // if(p.getAttribute("style:name")!=null&&p.getAttribute("style:name").trim().length()>0){
                // newElement.setClassName(p.getAttribute("style:name").trim()+" "+newElement.getClassName());
                // stylesPropsToCSS(p.getStyleProperties(),
                // p.getAttribute("style:name"));
                // }
                // p=p.getParentStyle();
                //
                // }
                // Object
                // n=getXpath().evaluate("//style:style[@style:name='Body_20_Text_20_3']",
                // getOdt().getDocumentStyles(), XPathConstants.NODE);
                // n=getXpath().evaluate("//style:style[@style:name='Standard']",
                // getOdt().getDocumentStyles(), XPathConstants.NODE);
                // getXpath().evaluate("//style:style[@style:name='P38']",
                // getOdt().getDocumentStyles(), XPathConstants.NODE)
                stylesPropsToCSS(otp.getAutomaticStyle().getStyleProperties(), otp.getStyleName());

                if (isDebugMode()) {
                    Utils.printStyleProps(otp.getAutomaticStyle().getStyleProperties());
                }
            }

        } else if (e instanceof OdfTextSpan) {// text:span
            // System.out.println(e.getTextContent());
            // sembra che se automatic.style � vuoto allora esiste uno stile
            // definito che pu� definire bold e italic
            OdfTextSpan ots = (OdfTextSpan) e;

            if (ots.getAutomaticStyle() != null) {// probabile che sia stato
                                                  // modificato lo stile
                newElement = getCurrentResource().getDocument().createElement("span");
                dstElement.add(newElement);
                newElement.setClassName(ots.getStyleName());
                stylesPropsToCSS(ots.getAutomaticStyle().getStyleProperties(), newElement.getClassName());
                if (isDebugMode()) {
                    Utils.printStyleProps(ots.getAutomaticStyle().getStyleProperties());
                }
            }

        } else if (e instanceof TextImpl) {
            dstElement.add(e.getTextContent());
            if (isDebugMode()) {
                System.out.println("Pezzo di testo: " + e.getTextContent());
            }
        }
        /**
         * End of Original ODT's element evalutation 
         */
        for (int i = 0; i < e.getChildNodes().getLength() && !skipChildren; i++) {

            traverse(e.getChildNodes().item(i), newElement != null ? newElement : dstElement);
        }

    }

    public Publication getEpub() {
        if (ePub == null) {
            ePub = new Publication();

        }

        return ePub;
    }

    public String getEpubLanguage() {
        return epubLanguage;
    }

    public void setEpubLanguage(String epubLanguage) {
        this.epubLanguage = epubLanguage;
    }

    public String getEpubFilename() {
        return epubFilename;
    }

    public void setEpubFilename(String epubFilename) {
        this.epubFilename = epubFilename;
    }

    public NCXResource getToc() {
        return getEpub().getTOC();
    }

    public Stylesheet getStylesheet() {
        if (_stylesheet == null) {
            _stylesheet = getStyleResource().getStylesheet();
          

        }
        return _stylesheet;
    }

    protected void setStylesheet(Stylesheet stylesheet) {

        this._stylesheet = stylesheet;
    }

    public String getOdtFilename() {
        return odtFilename;
    }

    public void setOdtFilename(String odtFilename) {
        this.odtFilename = odtFilename;
    }

    protected int getNewBreakIndex() {
        breaksCount++;
        return breaksCount;
    }

    public OdfTextDocument getOdt() throws Exception {

        if (odt == null) {
            odt = (OdfTextDocument) OdfDocument.loadDocument(getOdtFilename());
        }

        return odt;
    }

    protected void createNewResource() {

        currentResource = getEpub().createOPSResource("OPS/content" + +getNewBreakIndex() + ".xhtml");
        getEpub().addToSpine(currentResource);
        currentResource.getDocument().addStyleResource(getStyleResource());
    }

    public OPSResource getCurrentResource() {

        if (currentResource == null) {
            createNewResource();

        }

        return currentResource;
    }

    protected void setCurrentResource(OPSResource currentResource) {
        this.currentResource = currentResource;
    }

    protected OPSResource getFootnotesResource() {
        if (footnotesResource == null) {
            footnotesResource = getEpub().createOPSResource("OPS/footnotes.xhtml");

            footnotesResource.getDocument().addStyleResource(getStyleResource());
        }
        return footnotesResource;
    }
    protected OPSResource getCoverResource() {
        if (coverResource == null) {
            coverResource = getEpub().createOPSResource("OPS/cover.xhtml");

            coverResource.getDocument().addStyleResource(getStyleResource());
        }
        return coverResource;
    }
    public void stylesPropsToCSS(Map<OdfStyleProperty, String> props, String className) {
        stylesPropsToCSS(props, null, className);

    }

    public void stylesPropsToCSS(Map<OdfStyleProperty, String> props, String elementName, String className) {
        Selector selector = getStylesheet().getSimpleSelector(elementName, className);
        for (Entry<OdfStyleProperty, String> e : props.entrySet()) {
            if (e.getKey().getName().getLocalName().equals("font-style")
                    || e.getKey().getName().getLocalName().equals("font-weight") ||

                    e.getKey().getName().getLocalName().equals("background-color")
                    || e.getKey().getName().getLocalName().equals("color")
                    || e.getKey().getName().getLocalName().equals("margin-left")
                    || e.getKey().getName().getLocalName().equals("margin-right")
                    || e.getKey().getName().getLocalName().equals("margin-top")
                    || e.getKey().getName().getLocalName().equals("margin-bottom")
                    || e.getKey().getName().getLocalName().equals("line-height")) {

                SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);

                rule.set(e.getKey().getName().getLocalName(), new CSSName(e.getValue()));
            } else if (e.getKey().getName().getLocalName().equals("font-size")) {
                SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
                String s = e.getValue();
                if (s.endsWith("pt")) {
                    s = s.substring(0, s.length() - 2);
                    BigDecimal ref = new BigDecimal(12);
                    BigDecimal val = new BigDecimal(s);

                    rule.set(e.getKey().getName().getLocalName(), new CSSLength(val
                            .divide(ref, 2, RoundingMode.HALF_UP).doubleValue(), "em"));
                } else {
                    rule.set(e.getKey().getName().getLocalName(), new CSSName(e.getValue()));
                }
            } else if (e.getKey().getName().getLocalName().equals("text-underline-style")) {
                SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);

                rule.set("text-decoration", new CSSName("underline"));
            } else if (e.getKey().getName().getLocalName().equals("text-align")) {
                SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
                String val = e.getValue();
                if (val.equals("end")) {
                    val = "right";
                } else if (val.equals("start")) {
                    val = "left";
                }
                rule.set(e.getKey().getName().getLocalName(), new CSSName(val));
            } else if (e.getKey().getName().getLocalName().equals("font-name")) {
                SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);

                rule.set("font-family", new CSSQuotedString(e.getValue()));
            } else if (e.getKey().getName().getLocalName().equals("text-indent")) {
                SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);

                rule.set(e.getKey().getName().getLocalName(), new CSSName(e.getValue()));
            }

        }

    }

    protected Element addImageBox(DrawTextBoxElement box, Element dstElem) {
        Selector selector = getStylesheet().getSimpleSelector(null, "imgDiv");
        SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
        rule.set("width", new CSSName("100%"));
        rule.set("text-align", new CSSName("center"));

        Element idiv = getFootnotesResource().getDocument().createElement("div");
        idiv.setClassName("imgDiv");
        getCurrentResource().getDocument().getBody().add(idiv);

        return idiv;
    }

    protected void addImage(OdfDrawImage imgOdf, Element dstElem) {
            
            addImage(imgOdf.getImageUri().toString(), dstElem);
       

    }
    protected BitmapImageResource addImage(String imgUri, Element dstElem, OPSResource dstResource) {
        try {

            String mimetype = null;
            String ext = null;
            if (imgUri.toString().toUpperCase().endsWith("JPG")
                    || imgUri.toUpperCase().endsWith("JPEG")) {
                mimetype = "image/jpeg";
                ext = "jpg";
            } else if (imgUri.toUpperCase().endsWith("PNG")) {
                mimetype = "image/png";
                ext = "png";
            } else if (imgUri.toUpperCase().endsWith("GIF")) {
                mimetype = "image/gif";
                ext = "gif";
            }
            if (mimetype != null) {
                byte[] content=getOdt().getPackage().getBytes(
                        imgUri);
                if(content==null){
                    File f=new File(imgUri);
                    if(f.exists()){
                        content=Utils.getBytesFromFile(f);
                    }
                }
                if(content!=null){
                    DataSource dataSource = new ByteArrayImageDataSource(content);
    
                    BitmapImageResource imageResource = getEpub().createBitmapImageResource(
                            "OPS/images/" + System.currentTimeMillis() + (Math.random() * 100) + "." + ext, mimetype,
                            dataSource);
                    ImageElement bitmap = dstResource.getDocument().createImageElement("img");
                    bitmap.setImageResource(imageResource);
                    
                    dstElem.add(bitmap);
                    
                    return imageResource;
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        
        return null;

    }
    protected void addImage(String imgUri, Element dstElem) {
        addImage(imgUri, dstElem,  getCurrentResource());

    }
    
    protected Element addFootnote(TextNoteElement e, Element dstElem) throws Exception {

        TextNoteCitationElement noteCit = (TextNoteCitationElement) getXpath().evaluate(".//text:note-citation", e,
                XPathConstants.NODE);

        // Element fn=addFootnoteLink(noteCit.getTextContent(),dstElem);

        Element fn = getFootnotesResource().getDocument().createElement("div");
        fn.setClassName("fnDiv");
        getFootnotesResource().getDocument().getBody().add(fn);

        HyperlinkElement a = getCurrentResource().getDocument().createHyperlinkElement("a");
        a.setClassName("fnLink");
        a.setXRef(fn.getSelfRef());
        a.add(noteCit.getTextContent());
        dstElem.add(a);

        Element noteId = getFootnotesResource().getDocument().createHyperlinkElement("p");
        noteId.setClassName("Footnote");
        noteId.add(noteCit.getTextContent() + ")");
        fn.add(noteId);

        HyperlinkElement ar = getFootnotesResource().getDocument().createHyperlinkElement("a");
        // ar.setClassName("fnLink");
        ar.setXRef(a.getSelfRef());
        ar.add(" \u21B5");

        // fn.add(ar);
        OPSResource temp = getCurrentResource();
        setCurrentResource(getFootnotesResource());
        traverse((Node) getXpath().evaluate(".//text:note-body", e, XPathConstants.NODE), fn);
        HTMLElement dst = (HTMLElement) fn.getLastChild();
        while (dst.getLastChild() != null && dst.getLastChild() instanceof Element
                && ((Element) dst.getLastChild()).getElementName().equals("p")) {
            dst = (HTMLElement) dst.getLastChild();
        }
        dst.add(ar);

        setCurrentResource(temp);
        _hasFootnotes=true;
        return fn;
    }

    /**
     * Used to prevent negative indentations and to set default styles
     */
    public void setFootnotesCSSStyles() {
        Selector selector = getStylesheet().getSimpleSelector(null, "fnDiv");
        SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
        rule.set("page-break-before", new CSSName("always"));
        rule.set("margin-left", new CSSName("0em"));
        rule.set("margin-right", new CSSName("0em"));
        // if(rule.get("text-indent")!=null&&rule.get("text-indent").toCSSString().startsWith("-")){
        // rule.set("text-indent", new CSSLength(0,"em"));
        // }
        selector = getStylesheet().getSimpleSelector(null, "Footnote");
        rule = getStylesheet().getRuleForSelector(selector, true);

        // if(rule.get("text-indent")!=null&&rule.get("text-indent").toCSSString().startsWith("-")){
        // rule.set("text-indent", new CSSLength(0,"em"));
        // }

        selector = getStylesheet().getSimpleSelector("a", "fnLink");
        rule = getStylesheet().getRuleForSelector(selector, true);
        rule.set("vertical-align", new CSSName("super"));
        rule.set("font-size", new CSSLength(0.80, "em"));
    }

    public boolean hasPageBreak(OdfStylableElement e) throws XPathExpressionException {
        if (e.getAutomaticStyle() != null) {
            StyleParagraphPropertiesElement sp = (StyleParagraphPropertiesElement) getXpath().evaluate(
                    ".//style:paragraph-properties", e.getAutomaticStyle(), XPathConstants.NODE);
            if (sp != null) {
                String pbreak = sp.getAttribute("fo:break-before");
                if (pbreak != null && pbreak.trim().length() > 0 && pbreak.equals("page")) {
                    return true;
                }
            }
        }
        return false;

    }

    public void addTocEntry(String title, int headingLevel, Element dstElem) {
        TOCEntry te = getToc().createTOCEntry(title, dstElem.getSelfRef());
        boolean found = false;
        for (int j = getTocEntriesBuffer().size() - 1; j >= 0; j--) {

            TOCLevel target = getTocEntriesBuffer().get(j);
            if (target.getHeadingLevel() < headingLevel) {
                target.getTOCEntry().add(te);
                found = true;
                break;
            }
        }
        if (!found) {
            getToc().getRootTOCEntry().add(te);
        }
        getTocEntriesBuffer().add(new TOCLevel(headingLevel, te));

    }

    public Stack<TOCLevel> getTocEntriesBuffer() {
        if (tocEntriesBuffer == null) {
            tocEntriesBuffer = new Stack<TOCLevel>();

        }

        return tocEntriesBuffer;
    }

    public StyleResource getStyleResource() {
        if (_styleResource == null) {
            _styleResource = getEpub().createStyleResource("OPS/styles.css");

        }

        return _styleResource;
    }

    protected void extractDefaultStyles(OdfOfficeStyles styles) {

        for (OdfStyle s : styles.getStylesForFamily(OdfStyleFamily.Paragraph)) {

            if (s.getAttribute("style:name").equals("Heading")) {
                stylesPropsToCSS(s.getStyleProperties(), "h1", null);
                stylesPropsToCSS(s.getStyleProperties(), "h2", null);
                stylesPropsToCSS(s.getStyleProperties(), "h3", null);
                stylesPropsToCSS(s.getStyleProperties(), "h4", null);
                stylesPropsToCSS(s.getStyleProperties(), "h5", null);
                stylesPropsToCSS(s.getStyleProperties(), "h6", null);
                stylesPropsToCSS(s.getStyleProperties(), "h7", null);
                stylesPropsToCSS(s.getStyleProperties(), "h8", null);
                stylesPropsToCSS(s.getStyleProperties(), "h9", null);
                stylesPropsToCSS(s.getStyleProperties(), "h10", null);

            }
            if (s.getAttribute("style:name").startsWith("Heading")) {
                String level = s.getAttribute("style:default-outline-level");
                if (level != null && level.trim().length() > 0) {
                    stylesPropsToCSS(s.getStyleProperties(), "h" + level, null);
                }
            }
            if (s.getAttribute("style:name").startsWith("Standard")) {

                stylesPropsToCSS(s.getStyleProperties(), "p", null);

            }
            if (s.getAttribute("style:name").startsWith("Footnote")) {
                stylesPropsToCSS(s.getStyleProperties(), null, "Footnote");
                stylesPropsToCSS(s.getStyleProperties(), null, "fnDiv");
            }
            if (isDebugMode()) {
                System.out.println("Nome: " + s.getAttribute("style:name") + " Classe: "
                        + s.getAttribute("style:class") + "Outline Level"
                        + s.getAttribute("style:default-outline-level"));
                System.out.println("{");
                Utils.printStyleProps(s.getStyleProperties());
                System.out.println("}");
            }
        }

        if (isDebugMode()) {
            System.out.println("-----");
            System.out.println("Stile di testo");
            for (OdfStyle s : styles.getStylesForFamily(OdfStyleFamily.Text)) {

                System.out.println("Nome: " + s.getAttribute("style:name") + " Classe: "
                        + s.getAttribute("style:class"));
                System.out.println("{");
                Utils.printStyleProps(s.getStyleProperties());
                System.out.println("}");
            }
            System.out.println("-----");
            System.out.println("Stili di default");
        }
        for (OdfDefaultStyle ds : styles.getDefaultStyles()) {
            stylesPropsToCSS(ds.getStyleProperties(), "body", null);
            if (isDebugMode()) {
                Utils.printStyleProps(ds.getStyleProperties());
            }
        }

        
        Selector selector = getStylesheet().getSimpleSelector("body", null);
        SelectorRule rule = getStylesheet().getRuleForSelector(selector, true);
        CSSValue minh = rule.get("font-size");
        selector = getStylesheet().getSimpleSelector("p", null);
        rule = getStylesheet().getRuleForSelector(selector, true);
        if (rule.get("margin-top") == null) {
            rule.set("margin-top", new CSSLength(0d, "em"));
        }
        if (rule.get("margin-bottom") == null) {
            rule.set("margin-bottom", new CSSLength(0d, "em"));
        }

        if (minh != null) {
            rule.set("min-height", minh);
        }

    }

    public XPath getXpath() {
        if (xpath == null) {
            xpath = XPathFactory.newInstance().newXPath();

        }

        return xpath;
    }

    public Map<String, Element> getBookmarks() {
        if (bookmarks == null) {
            bookmarks = new HashMap<String, Element>();

        }

        return bookmarks;
    }

    public Set<HyperlinkElement> getInternalLink() {
        if (internalLink == null) {
            internalLink = new HashSet<HyperlinkElement>();

        }

        return internalLink;
    }

    public void processInternalLinksCrossReferences() {
        for (HyperlinkElement h : getInternalLink()) {
            Element target = getBookmarks().get(h.getTitle());
            if (target != null) {
                h.setTitle("");
                h.setXRef(target.getSelfRef());
            }
        }

    }
    
    protected void  bookmarksToToc(){
        if(getBookmarks().size()>0){
            List<String> bl=new ArrayList();
            for (String e : getBookmarks().keySet()) {
                bl.add(e);
            }
            Collections.sort(bl);
            
            addTocEntry("Bookmarks", 0, getBookmarks().get(bl.get(0)));
            
            for (String k : bl) {
                Element el=getBookmarks().get(k);
                addTocEntry(el.getId(), 1, el);
            }
           
        }
    }

    private void printClassesFound() {
        for (String s : classesForDebug) {
            System.out.println(s);
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getMaxFilesSize() {
        return maxFilesSize;
    }

    public void setMaxFilesSize(int maxFilesSize) {
        this.maxFilesSize = maxFilesSize;
    }

    public String getFontsPath() {
        return fontsPath;
    }

    public void setFontsPath(String fontsPath) {
        this.fontsPath = fontsPath;
    }
    
    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
    public boolean isExposeBookmarks() {
        return exposeBookmarks;
    }

    public void setExposeBookmarks(boolean exposeBookmarks) {
        this.exposeBookmarks = exposeBookmarks;
    }
}
