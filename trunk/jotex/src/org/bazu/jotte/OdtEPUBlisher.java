/*******************************************************************************
* Copyright (c) 2011, Luca Conte
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions are met:
*
* ·        Redistributions of source code must retain the above copyright 
*          notice, this list of conditions and the following disclaimer. 
*
* ·        Redistributions in binary form must reproduce the above copyright 
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution. 
*
* ·        Neither the name of Adobe Systems Incorporated nor the names of its 
*      contributors may be used to endorse or promote products derived from
*      this software without specific prior written permission. 
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR 
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
* OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
* THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
/**
 * TODO:
 *  -returns from footnote
 *  -tables border
 *  -List styles dotted or numbered wirh variants
 *  -image types support
 *  -remote images support (?) a lot of problems related
 * 
 */
package org.bazu.jotte;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.bazu.jotte.images.ByteArrayImageDataSource;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.OdfStylableElement;
import org.odftoolkit.odfdom.dom.element.draw.DrawTextBoxElement;
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
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextHeading;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextList;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;

import org.w3c.dom.Node;

import com.adobe.dp.css.CSSLength;
import com.adobe.dp.css.CSSName;
import com.adobe.dp.css.Selector;
import com.adobe.dp.css.SelectorRule;
import com.adobe.dp.epub.io.DataSource;
import com.adobe.dp.epub.io.OCFContainerWriter;
import com.adobe.dp.epub.ncx.TOCEntry;
import com.adobe.dp.epub.opf.BitmapImageResource;
import com.adobe.dp.epub.opf.NCXResource;
import com.adobe.dp.epub.opf.OPSResource;
import com.adobe.dp.epub.opf.Publication;
import com.adobe.dp.epub.opf.StyleResource;
import com.adobe.dp.epub.ops.Element;
import com.adobe.dp.epub.ops.HyperlinkElement;
import com.adobe.dp.epub.ops.ImageElement;
import com.adobe.dp.epub.style.Stylesheet;
import com.adobe.dp.epub.util.TOCLevel;

public class OdtEPUBlisher {
	// config fields
	private String epubTitle;
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
	private XPath xpath;
	
	private Map<String, Element> bookmarks;
	private Set<HyperlinkElement> internalLink;
	
	private Set<String> classesForDebug=new HashSet<String>();

	private static NamespaceContext XPATH_ODT_NS_CTX=new NamespaceContext() {
		
		@Override
		public Iterator getPrefixes(String namespaceURI) {
			// fo=urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0
			return null;
		}
		
		@Override
		public String getPrefix(String namespaceURI) {
		  if(namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:drawing:1.0")){
		     return "draw";
		  }else if(namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:style:1.0")){
         return "style";
      }else if(namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:text:1.0")){
         return "text";
      }else if(namespaceURI.equals("urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0")){
         return "fo";
      }
		  
			return null;
		}
		
		@Override
		public String getNamespaceURI(String prefix) {
      if(prefix.equals("draw")){
        return "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";
     }else if(prefix.equals("style")){
        return "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
     }else if(prefix.equals("text")){
        return "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
     }else if(prefix.equals("fo")){
        return "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0";
     }
			return "";
		}
	};


	

	private int breaksCount = 0;
	
	private Stack<TOCLevel> tocEntriesBuffer;

	
	public static void main(String[] args) {
	  if(args.length==0){
	    System.out.println("Usage: java -jar jotte <FILE_NAME>.odt");
	    System.exit(1);
	  }
	  System.out.println("Exporting process STARTED at "+new Date());
		OdtEPUBlisher op=new OdtEPUBlisher();
		op.setEpubLanguage("it");
		op.setEpubTitle("jotte");
		op.setOdtFilename(args[0]);
		op.setEpubFilename(args[0].split("\\.")[0]+".epub");
		try {
			op.startRippingSession();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			 System.out.println("Exporting process FAILED at "+new Date());
		}
		System.out.println("Exporting process FINISHED at "+new Date());
	}
	
	public OdtEPUBlisher() {

	}

	public void startRippingSession() throws Exception {
		// set up title, author and language
		getEpub().addDCMetadata("title", getEpubTitle());
		getEpub().addDCMetadata("creator",
				"Java OdT To Epub (Jotte) <luca.conte at gmail.com>");
		getEpub().addDCMetadata("language", getEpubLanguage());
		getEpub().addDCMetadata("date", (new Date()).toString());
		//le peschiamo da qui: getOdt().getMetaDom()

		getXpath().setNamespaceContext(XPATH_ODT_NS_CTX);
		
		
		
		
		createNewResource();
		int pi = 0;
	//stylesPropsToCSS(	getOdtDocument().getDocumentStyles().get, className)
		//System.out.println(getOdtDocument().getDocumentStyles());
		extractDefaultStyles(getOdt().getDocumentStyles());
		
		
		
	
		
		traverse(getOdt().getContentDom(),null);
		
		
		// processURLSSession(rootTOCEntry,p);
		processInternalLinksCrossReferences();
		getEpub().addToSpine(getFootnotesResource());
		OCFContainerWriter writer = new OCFContainerWriter(
				new FileOutputStream(getEpubFilename()));
		getEpub().serialize(writer);
		
		//printClassesFound();
	}
	public  void traverse(Node e, Element dstElement) throws Exception {
		classesForDebug.add(e.getClass().toString());
		boolean skipChildren=false;
		Element newElement=null;
		if (e instanceof TextNoteBodyElement) {// Corpo di una nota
			TextNoteBodyElement noteBody = (TextNoteBodyElement) e;
		} else if (e instanceof TextNoteCitationElement) {// Rimando ad una nota
		
			
		}else if (e instanceof TextSElement) {// ?
			TextSElement te=(TextSElement) e;
			
		}else if (e instanceof TextAElement) {// is an hyperlink
			TextAElement ta=(TextAElement) e;
			String ref=ta.getAttribute("xlink:href");
			
			HyperlinkElement a = getCurrentResource().getDocument().createHyperlinkElement("a");
			if(ref.startsWith("#")){//internal Link
				a.setTitle(ta.getAttribute("xlink:href"));
				getInternalLink().add(a);
			}else{
				a.setExternalHRef(ref);
			}
			dstElement.add(a);
		//	a.add("ciao");
			traverse(ta.getFirstChild(), a);
			skipChildren=true;
			 
			 
		}else if (e instanceof TextBookmarkElement) {// is bookmark in epub can be used to implement internal link anchors
			TextBookmarkElement ta=(TextBookmarkElement) e;
			
			 HyperlinkElement a = getCurrentResource().getDocument().createHyperlinkElement("a");
			 a.setId(ta.getAttribute("text:name"));
			 dstElement.add(a);
			 getBookmarks().put("#"+ta.getAttribute("text:name"), a);
		}else if (e instanceof TextNoteElement) {// Is a footnote container 
			addFootnote((TextNoteElement) e,dstElement);
			skipChildren=true;
		}
		else if (e instanceof OdfDrawFrame) {
			OdfDrawFrame dframe = (OdfDrawFrame) e;
		
		} else if (e instanceof DrawTextBoxElement) {
			DrawTextBoxElement didascalia = (DrawTextBoxElement) e;
			addImage(didascalia,dstElement);
			skipChildren=true;
		} else if (e instanceof OdfDrawImage) {
		}else if (e instanceof OdfTextList) {
			OdfTextList otl=(OdfTextList) e;
			dstElement=getCurrentResource().getDocument().getBody();
			/**
			 * ul.a {list-style-type:circle;}
ul.b {list-style-type:square;}
ol.c {list-style-type:upper-roman;}
ol.d {list-style-type:lower-alpha;}
			 */
      Object n=getXpath().evaluate("//text:list-style[@style:name='"+otl.getTextStyleNameAttribute()+"']/text:list-level-style-bullet", getOdt().getContentDom(), XPathConstants.NODE);
			if(n!=null){//is a bullet list
			  newElement=getCurrentResource().getDocument().createElement("ul");
			}else{
			  n=getXpath().evaluate("//text:list-style[@style:name='"+otl.getTextStyleNameAttribute()+"']/text:list-level-style-number", getOdt().getContentDom(), XPathConstants.NODE);
			  if(n!=null){
			    newElement=getCurrentResource().getDocument().createElement("ol");
			  }
			}
			
			
			dstElement.add(newElement);
			newElement.setClassName(otl.getTextStyleNameAttribute());
					//title[@lang='eng']
		}else if (e instanceof TextListItemElement) {
			TextListItemElement li=(TextListItemElement) e;
			newElement=getCurrentResource().getDocument().createElement("li");
			dstElement.add(newElement);
			
		}else if (e instanceof TableTableElement) {
			TableTableElement otl=(TableTableElement) e;
			dstElement=getCurrentResource().getDocument().getBody();
			newElement=getCurrentResource().getDocument().createElement("table");
			dstElement.add(newElement);
			newElement.setClassName(otl.getStyleName());
			Selector selector=getStylesheet().getSimpleSelector(null, otl.getStyleName());
			SelectorRule rule= getStylesheet().getRuleForSelector(
					 selector, true);
			rule.set("width", new CSSName("100%"));
			selector=getStylesheet().getSimpleSelector(null, "table");
			rule= getStylesheet().getRuleForSelector(
					 selector, true);e
			rule.set("border-collapse", new CSSName("collapse"));
			selector=getStylesheet().getSimpleSelector(null, "td");
			rule= getStylesheet().getRuleForSelector(
					 selector, true);
			rule.set("border", new CSSName("1px solid black"));
			

		}else if (e instanceof TableTableRowElement) {
			TableTableRowElement otl=(TableTableRowElement) e;
			newElement=getCurrentResource().getDocument().createElement("tr");
			dstElement.add(newElement);
			newElement.setClassName(otl.getStyleName());
			
		}else if (e instanceof TableTableCellElement) {
			TableTableCellElement otl=(TableTableCellElement) e;
			newElement=getCurrentResource().getDocument().createElement("td");
			dstElement.add(newElement);
			newElement.setClassName(otl.getStyleName());
		} else if (e instanceof OdfTextHeading) {// text:p
			// System.out.println(e.getTextContent());
			OdfTextHeading oth = (OdfTextHeading) e;
			if(hasPageBreak(oth)){
				createNewResource();
			}
			newElement=getCurrentResource().getDocument().createElement("h"+oth.getAttribute("text:outline-level"));
			if(dstElement!=null){
				dstElement.add(newElement);
			}else{
				getCurrentResource().getDocument().getBody().add(newElement);
			}
			newElement.setClassName(oth.getStyleName());
			if(oth.getTextContent()!=null&&oth.getTextContent().trim().length()>0){
				addTocEntry(oth.getTextContent(), Integer.parseInt(oth.getAttribute("text:outline-level")),newElement);
			}
			
			
			
			if (oth.getAutomaticStyle() != null) {// probabile che sia stato
								//	oth.getAutomaticStyles()	// modificato lo stile
			
				
				stylesPropsToCSS(oth.getAutomaticStyle().getStyleProperties(), oth.getStyleName());
				if(newElement!=null){
					newElement.setClassName(oth.getStyleName());
				}
//				Utils.printStyleProps(oth.getAutomaticStyle()
//						.getStyleProperties());
			}

			getOdt().getDocumentStyles().getElementsByTagName(
					oth.getTextStyleNameAttribute());

		} else if (e instanceof OdfTextParagraph) {// text:p
			// System.out.println(e.getTextContent());
			OdfTextParagraph otp = (OdfTextParagraph) e;
			if(hasPageBreak(otp)){
				createNewResource();
			}
				newElement=getCurrentResource().getDocument().createElement("p");
				if(dstElement!=null){
					dstElement.add(newElement);
				}else{
					getCurrentResource().getDocument().getBody().add(newElement);
				}
				newElement.setClassName(otp.getStyleName());
			if (otp.getAutomaticStyle() != null) {// probabile che sia stato
													// modificato lo stile
			
				
				stylesPropsToCSS(otp.getAutomaticStyle().getStyleProperties(), otp.getStyleName());
				if(newElement!=null){
					newElement.setClassName(otp.getStyleName());
				}
//				Utils.printStyleProps(otp.getAutomaticStyle()
//						.getStyleProperties());
			}

		} else if (e instanceof OdfTextSpan) {// text:span
			// System.out.println(e.getTextContent());
			// sembra che se automatic.style � vuoto allora esiste uno stile
			// definito che pu� definire bold e italic
			OdfTextSpan ots = (OdfTextSpan) e;
			
			if (ots.getAutomaticStyle() != null) {// probabile che sia stato
													// modificato lo stile
				newElement=getCurrentResource().getDocument().createElement("span");
				dstElement.add(newElement);
				newElement.setClassName(ots.getStyleName());
				stylesPropsToCSS(ots.getAutomaticStyle().getStyleProperties(), newElement.getClassName());
//				Utils.printStyleProps(ots.getAutomaticStyle()
//						.getStyleProperties());
			}

		} else if (e instanceof TextImpl) {
			dstElement.add(e.getTextContent());
//			System.out.println("Pezzo di testo: " + e.getTextContent());
		}

		// {
		// if (!e.hasChildNodes()) {
		//
		// System.out.println("Nome: " + e.getNodeName() + " Value: "
		// + e.getTextContent() + " Tipo: " + e.getNodeType()
		// + " Class: " + e.getClass() + " Parent: "
		// + e.getParentNode().getNodeName());
		// }
		// }
		for (int i = 0; i < e.getChildNodes().getLength()&&!skipChildren; i++) {

			traverse(e.getChildNodes().item(i),newElement!=null?newElement:dstElement);
		}

	}

	public Publication getEpub() {
		if (ePub == null) {
			ePub = new Publication();

		}

		return ePub;
	}

	
	public String getEpubTitle() {
		return epubTitle;
	}

	public void setEpubTitle(String epubTitle) {
		this.epubTitle = epubTitle;
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
			odt =  (OdfTextDocument) OdfDocument.loadDocument(getOdtFilename());
		}

		return odt;
	}
	
	protected void createNewResource(){
		
		currentResource=getEpub().createOPSResource("OPS/content"+
				 + getNewBreakIndex() + ".xhtml");
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
		if(footnotesResource==null){
			footnotesResource=getEpub().createOPSResource("OPS/footnotes.xhtml");
			
			footnotesResource.getDocument().addStyleResource(getStyleResource());
		}
		return footnotesResource;
	}
	public void stylesPropsToCSS(Map<OdfStyleProperty, String> props,String className){
		stylesPropsToCSS(props, null, className);
		
	}
	public void stylesPropsToCSS(Map<OdfStyleProperty, String> props,String elementName,String className ){
		Selector selector=getStylesheet().getSimpleSelector(elementName, className);
		for (Entry<OdfStyleProperty, String> e : props.entrySet()) {
			if(e.getKey().getName().getLocalName().equals("font-style")||
					e.getKey().getName().getLocalName().equals("font-weight")||
					e.getKey().getName().getLocalName().equals("text-align")||
					e.getKey().getName().getLocalName().equals("background-color")||
					e.getKey().getName().getLocalName().equals("font-size")||
					e.getKey().getName().getLocalName().equals("color")||
					e.getKey().getName().getLocalName().equals("line-height")){
				
				SelectorRule rule= getStylesheet().getRuleForSelector(
							 selector, true);
							
				rule.set(e.getKey().getName().getLocalName(), new CSSName(e.getValue()));
			}else if(e.getKey().getName().getLocalName().equals("text-underline-style")){
				SelectorRule rule= getStylesheet().getRuleForSelector(
						 selector, true);
						
				rule.set("text-decoration", new CSSName("underline"));
			}else if(e.getKey().getName().getLocalName().equals("text-underline-style")){
				SelectorRule rule= getStylesheet().getRuleForSelector(
						 selector, true);
						
				rule.set("text-decoration", new CSSName("underline"));
			}else if(e.getKey().getName().getLocalName().equals("font-name")){
				SelectorRule rule= getStylesheet().getRuleForSelector(
						 selector, true);
						
				rule.set("font-family", new CSSName(e.getValue()));
			}else if(e.getKey().getName().getLocalName().equals("text-indent")){
				SelectorRule rule= getStylesheet().getRuleForSelector(
						 selector, true);
						
				rule.set(e.getKey().getName().getLocalName(), new CSSName(e.getValue()));
			}
			
			
			
			
			
			
		}
		
	}
	
	//TODO: vanno gestite e verificate tutti i possibili mimetype delle immagini  
	protected Element addImage(DrawTextBoxElement box, Element dstElem){
		Selector selector=getStylesheet().getSimpleSelector(null, "imgDiv");
		SelectorRule rule= getStylesheet().getRuleForSelector(
				 selector, true);
		rule.set("width", new CSSName("100%"));
		rule.set("text-align", new CSSName("center"));
		
		Element idiv=getFootnotesResource().getDocument().createElement("div");
		idiv.setClassName("imgDiv");
		getCurrentResource().getDocument().getBody().add(idiv);
		
		
		
		try {
			
			OdfDrawImage imgOdf = (OdfDrawImage) getXpath().evaluate(".//draw:image", box,XPathConstants.NODE);
			 DataSource dataSource = new ByteArrayImageDataSource(getOdt().getPackage().getBytes(imgOdf.getImageUri().toString()));
			 BitmapImageResource imageResource = getEpub().createBitmapImageResource(
			         "OPS/images/"+System.currentTimeMillis()+idiv.getId()+".jpg", "image/jpeg", dataSource);
			 ImageElement bitmap = getCurrentResource().getDocument().createImageElement("img");
			 bitmap.setImageResource(imageResource);
			 idiv.add(bitmap);
			 traverse((Node) getXpath().evaluate(".//text:p", box, XPathConstants.NODE), idiv);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		

//     // add a bitmap image
//     Element container = chapter2Doc.createElement("p");
//     container.setClassName("container");
//     body2.add(container);
//     ImageElement bitmap = chapter2Doc.createImageElement("img");
//     bitmap.setClassName("bitmap");
//     bitmap.setImageResource(imageResource);
//     container.add(bitmap);
		

	     return idiv;
	}
	protected Element addFootnote(TextNoteElement e, Element dstElem) throws Exception{
		
		
		
		TextNoteCitationElement noteCit = (TextNoteCitationElement) getXpath().evaluate(".//text:note-citation", e, XPathConstants.NODE);
		
		//Element fn=addFootnoteLink(noteCit.getTextContent(),dstElem);

		
		
		
		
		Selector selector=getStylesheet().getSimpleSelector(null, "fnDiv");
		SelectorRule rule= getStylesheet().getRuleForSelector(
				 selector, true);
		rule.set("page-break-before", new CSSName("always"));
		//rule.set("font-size", new CSSLength(0.8,"em"));
		
		selector=getStylesheet().getSimpleSelector("a", "fnLink");
		 rule= getStylesheet().getRuleForSelector(
				 selector, true);
		rule.set("vertical-align", new CSSName("super"));
		rule.set("font-size", new CSSLength(0.5,"em"));
		
		
		Element fn=getFootnotesResource().getDocument().createElement("div");
		fn.setClassName("fnDiv");
		getFootnotesResource().getDocument().getBody().add(fn);
		
		 HyperlinkElement a = getCurrentResource().getDocument().createHyperlinkElement("a");
		 a.setClassName("fnLink");
	     a.setXRef(fn.getSelfRef());
	     a.add(noteCit.getTextContent());
	     dstElem.add(a);
	     
	     
	     HyperlinkElement ar = getFootnotesResource().getDocument().createHyperlinkElement("a");
		 //ar.setClassName("fnLink");
	     ar.setXRef(a.getSelfRef());
	  
	     ar.add("("+noteCit.getTextContent()+")");
	     fn.add(ar);
		OPSResource temp=getCurrentResource();
		setCurrentResource(getFootnotesResource());
		traverse((Node) getXpath().evaluate(".//text:note-body", e, XPathConstants.NODE), fn);
		setCurrentResource(temp);
	     return fn;
	}
	public boolean hasPageBreak(OdfStylableElement e ) throws XPathExpressionException{
		if(e.getAutomaticStyle()!=null){
			StyleParagraphPropertiesElement sp=(StyleParagraphPropertiesElement) getXpath().evaluate(".//style:paragraph-properties", e.getAutomaticStyle(), XPathConstants.NODE);
			if(sp!=null){
				String pbreak=sp.getAttribute("fo:break-before");
				if(pbreak!=null&&pbreak.trim().length()>0&&pbreak.equals("page")){
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
	
	protected void extractDefaultStyles(OdfOfficeStyles styles){
	
		for (OdfStyle	s : styles.getStylesForFamily(OdfStyleFamily.Paragraph)) {
			
			if(s.getAttribute("style:name").equals("Heading")){
				stylesPropsToCSS(s.getStyleProperties(), "h1",null);
				stylesPropsToCSS(s.getStyleProperties(), "h2",null);
				stylesPropsToCSS(s.getStyleProperties(), "h3",null);
				stylesPropsToCSS(s.getStyleProperties(), "h4",null);
				stylesPropsToCSS(s.getStyleProperties(), "h5",null);
				stylesPropsToCSS(s.getStyleProperties(), "h6",null);
				stylesPropsToCSS(s.getStyleProperties(), "h7",null);
				stylesPropsToCSS(s.getStyleProperties(), "h8",null);
				stylesPropsToCSS(s.getStyleProperties(), "h9",null);
				stylesPropsToCSS(s.getStyleProperties(), "h10",null);
				
			}
			if(s.getAttribute("style:name").startsWith("Heading")){
				String level=s.getAttribute("style:default-outline-level");
				if(level!=null&&level.trim().length()>0){
					stylesPropsToCSS(s.getStyleProperties(), "h"+level,null);
				}
			}
			if(s.getAttribute("style:name").startsWith("Standard")){
				
					stylesPropsToCSS(s.getStyleProperties(), "p",null);
				
			}
			if(s.getAttribute("style:name").startsWith("Footnote")){
					stylesPropsToCSS(s.getStyleProperties(), null,"Footnote");
					stylesPropsToCSS(s.getStyleProperties(), null,"fnDiv");
			}
			
			
//			System.out.println("Nome: "+s.getAttribute("style:name")+" Classe: "+s.getAttribute("style:class")+"Outline Level"+s.getAttribute("style:default-outline-level"));
//			System.out.println("{");
//				Utils.printStyleProps(s.getStyleProperties());
//			System.out.println("}");
		}
//		System.out.println("-----");
//		System.out.println("Stile di testo");
		for (OdfStyle	s : styles.getStylesForFamily(OdfStyleFamily.Text)) {
			
//			System.out.println("Nome: "+s.getAttribute("style:name")+" Classe: "+s.getAttribute("style:class"));
//			System.out.println("{");
//			Utils.printStyleProps(s.getStyleProperties());
//			System.out.println("}");
		}
//		System.out.println("-----");
//		System.out.println("Stili di default");
		for (OdfDefaultStyle ds : styles.getDefaultStyles()) {
			stylesPropsToCSS(ds.getStyleProperties(), "body",null);
//			Utils.printStyleProps(ds.getStyleProperties());	
		}
		
	}

	public XPath getXpath() {
		if (xpath == null) {
			xpath =XPathFactory.newInstance().newXPath();
			
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

	public void processInternalLinksCrossReferences(){
		for (HyperlinkElement h : getInternalLink()) {
			Element target=getBookmarks().get(h.getTitle());
			if(target!=null){
				h.setTitle("");
				h.setXRef(target.getSelfRef());
			}
		}
		
	}
	private void printClassesFound(){
		for (String s : classesForDebug) {
			System.out.println(s);
		}
	}
}