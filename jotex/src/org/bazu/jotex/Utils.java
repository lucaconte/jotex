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

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.odftoolkit.odfdom.dom.OdfMetaDom;
import org.odftoolkit.odfdom.dom.element.meta.MetaUserDefinedElement;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;

import com.adobe.dp.epub.opf.OPFResource;
import com.adobe.dp.epub.opf.Publication;
import com.adobe.dp.xml.util.SMapImpl;

public class Utils {
  
  
  public static void processMetadata(OdfMetaDom odtMeta, Publication epub, XPath seeker) throws XPathExpressionException{
    seeker.setNamespaceContext(OdtEPUBlisher.XPATH_ODT_NS_CTX);
    SMapImpl attrs=new SMapImpl();
    //AUTHOR
    MetaUserDefinedElement metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_AUTHOR_KEY+"']", odtMeta, XPathConstants.NODE);
    if(metaInfo==null){
      metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_AUTHOR_KEY+"']", odtMeta, XPathConstants.NODE);
    }
    //opf:role="aut"
  
    if(metaInfo!=null){
    	attrs.put(OPFResource.opfns, "role", "aut");
    	epub.addDCMetadata("creator", metaInfo.getTextContent(),attrs);
    }
    
    //TITLE
     metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_TILTE_KEY+"']", odtMeta, XPathConstants.NODE);
    if(metaInfo==null){
      metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_TILTE_KEY+"']", odtMeta, XPathConstants.NODE);
    }
    if(metaInfo!=null){
    	epub.addDCMetadata("title", metaInfo.getTextContent(),null);
    }
    
  //LANGUAGE
    metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_LANGUAGE_KEY+"']", odtMeta, XPathConstants.NODE);
    if(metaInfo==null){
    	metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_LANGUAGE_KEY+"']", odtMeta, XPathConstants.NODE);
    }
    if(metaInfo!=null){
	   epub.addDCMetadata("language", metaInfo.getTextContent(),null);
    }
   
   //PUBLISHER
   metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_PUBLISHER_KEY+"']", odtMeta, XPathConstants.NODE);
   if(metaInfo==null){
    metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_PUBLISHER_KEY+"']", odtMeta, XPathConstants.NODE);
   }
   if(metaInfo!=null){
	  epub.addDCMetadata("publisher", metaInfo.getTextContent(),null);
   }
  //PUBISHING DATE
  metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_PUBLISHING_DATE_KEY+"']", odtMeta, XPathConstants.NODE);
  if(metaInfo==null){
   metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_PUBLISHING_DATE_KEY+"']", odtMeta, XPathConstants.NODE);
  }
  if(metaInfo!=null){
	 attrs=new SMapImpl();
	 attrs.put(OPFResource.opfns, "event", "publication");
	  epub.addDCMetadata("date", metaInfo.getTextContent(),attrs);
  }
  //DESCRIPTION
  metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_DESCRITPION_KEY+"']", odtMeta, XPathConstants.NODE);
  if(metaInfo==null){
   metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_DESCRITPION_KEY+"']", odtMeta, XPathConstants.NODE);
  }
  if(metaInfo!=null){
	  epub.addDCMetadata("description", "<![CDATA["+metaInfo.getTextContent()+"]]>",null);
  }
  //ISBN
  metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_ISBN_KEY+"']", odtMeta, XPathConstants.NODE);
  if(metaInfo==null){
   metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_ISBN_KEY+"']", odtMeta, XPathConstants.NODE);
  }
  if(metaInfo!=null){
	  attrs=new SMapImpl();
		 attrs.put(OPFResource.opfns, "scheme", "ISBN");
	  epub.addDCMetadata("identifier", metaInfo.getTextContent(),attrs);
  }
  //ORIGINAL TITLE
  metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_ORIGINAL_TITLE_KEY+"']", odtMeta, XPathConstants.NODE);
  if(metaInfo==null){
   metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_ORIGINAL_TITLE_KEY+"']", odtMeta, XPathConstants.NODE);
  }
  if(metaInfo!=null){
	  epub.addDCMetadata("source", metaInfo.getTextContent(),null);
  }
  //TAGS
  metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='"+JotexConstants.META_TAGS_KEY+"']", odtMeta, XPathConstants.NODE);
  if(metaInfo==null){
   metaInfo=(MetaUserDefinedElement) seeker.evaluate("//meta:user-defined[@meta:name='w2e_"+JotexConstants.META_TAGS_KEY+"']", odtMeta, XPathConstants.NODE);
  }
  if(metaInfo!=null){
	  epub.addDCMetadata("subject", metaInfo.getTextContent(),null);
  }
 }
	
	public static void printStyleProps(Map<OdfStyleProperty, String> props){
		for (Entry<OdfStyleProperty, String> e : props.entrySet()) {
			System.out.println(e.getKey().getName().getQName()+"="+e.getValue());
			
		}
		
	}
}
