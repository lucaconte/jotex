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
package org.bazu.jotex;

import java.util.HashMap;
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
import com.adobe.dp.xml.util.SMap;
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
