/*
 * Utils.java
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
package org.bazu.jotex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.odftoolkit.odfdom.dom.OdfMetaDom;
import org.odftoolkit.odfdom.dom.element.meta.MetaUserDefinedElement;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;

import com.adobe.dp.css.BaseRule;
import com.adobe.dp.css.Selector;
import com.adobe.dp.css.SelectorRule;
import com.adobe.dp.epub.opf.OPFResource;
import com.adobe.dp.epub.opf.Publication;
import com.adobe.dp.epub.opf.StyleResource;
import com.adobe.dp.xml.util.SMapImpl;

public class Utils {

    public static void processMetadata(OdfMetaDom odtMeta, Publication epub, XPath seeker)
            throws XPathExpressionException {
        /**
         * 
         */
        seeker.setNamespaceContext(OdtEPUBlisher.XPATH_ODT_NS_CTX);
        SMapImpl attrs = new SMapImpl();
        // AUTHOR
        MetaUserDefinedElement metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_AUTHOR_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_AUTHOR_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        // opf:role="aut"

        if (metaInfo != null) {
            attrs.put(OPFResource.opfns, "role", "aut");
            epub.addDCMetadata("creator", metaInfo.getTextContent(), attrs);
        }

        // TITLE
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_TILTE_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_TILTE_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            epub.addDCMetadata("title", metaInfo.getTextContent(), null);
        }

        // LANGUAGE
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_LANGUAGE_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_LANGUAGE_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            epub.addDCMetadata("language", metaInfo.getTextContent(), null);
        }

        // PUBLISHER
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_PUBLISHER_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_PUBLISHER_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            epub.addDCMetadata("publisher", metaInfo.getTextContent(), null);
        }
        // PUBISHING DATE
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_PUBLISHING_DATE_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_PUBLISHING_DATE_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            attrs = new SMapImpl();
            attrs.put(OPFResource.opfns, "event", "publication");
            epub.addDCMetadata("date", metaInfo.getTextContent(), attrs);
        }
        // DESCRIPTION
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_DESCRITPION_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_DESCRITPION_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            epub.addDCMetadata("description", "<![CDATA[" + metaInfo.getTextContent() + "]]>", null);
        }
        // ISBN
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_ISBN_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_ISBN_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            attrs = new SMapImpl();
            attrs.put(OPFResource.opfns, "scheme", "ISBN");
            epub.addDCMetadata("identifier", metaInfo.getTextContent(), attrs);
        }
        // ORIGINAL TITLE
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_ORIGINAL_TITLE_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_ORIGINAL_TITLE_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            epub.addDCMetadata("source", metaInfo.getTextContent(), null);
        }
        // TAGS
        metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"
                + JotexConstants.META_TAGS_KEY + "']", odtMeta, XPathConstants.NODE);
        if (metaInfo == null) {
            metaInfo = (MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"
                    + JotexConstants.META_TAGS_KEY + "']", odtMeta, XPathConstants.NODE);
        }
        if (metaInfo != null) {
            epub.addDCMetadata("subject", metaInfo.getTextContent(), null);
        }
        
     
    }
    
    /**
     *Try to remove unuseful style classes 
     *Not yet completed 
     *TODO: to complete
     */
//    public static void optimizeStylesheet(StyleResource cssResource) {
//              
//            Map<String, List<Selector>> cssCache=new HashMap<String, List<Selector>>();
//            List<SelectorRule> toRemove=new ArrayList<SelectorRule>();
//            Iterator list = cssResource.getStylesheet().getCSS().statements();
//            while (list.hasNext()) {
//                Object stmt = list.next();
//                if (stmt instanceof BaseRule) {
//                    SelectorRule sr=(SelectorRule) stmt;
//                    toRemove.add(sr);
//                    StringWriter sw=new StringWriter();
//                  
//                    
//                    PrintWriter pw=new PrintWriter(sw);
//                    sr.serializeProperties(pw, true);
//                    pw.flush();
//                    
//                    List<Selector> l=cssCache.get(sw.toString());
//                    if(l==null){
//                        l=new ArrayList<Selector>();
//                        cssCache.put(sw.toString(), l);
//                    }
//                   l.addAll(Arrays.asList(sr.getSelectors()) );
//                    
//               
//                }
//            }
//            for (SelectorRule selectorRule : toRemove) {
//                cssResource.getStylesheet().getCSS().removeRule(selectorRule);
//            }
//           for (Entry<String, List<Selector>> s : cssCache.entrySet()) {
//               for (Selector sel : s.getValue()) {
//                System.out.println("----SELETTORI----");
//              //  System.out.println(sel.s);
//               }
//               System.out.println(s);
//           }
//          
//            
//            
//    }

    public static void printStyleProps(Map<OdfStyleProperty, String> props) {
        for (Entry<OdfStyleProperty, String> e : props.entrySet()) {
            System.out.println(e.getKey().getName().getQName() + "=" + e.getValue());

        }

    }
    
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
