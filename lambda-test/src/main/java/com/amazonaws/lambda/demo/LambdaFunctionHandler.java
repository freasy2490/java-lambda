package com.amazonaws.lambda.demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.jsoup.parser.Parser;
import org.apache.commons.net.ftp.FTPFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.json.XML;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.jcabi.xml.XMLDocument;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {
    FTPClient ftpClient = new FTPClient();
    JSONArray patentArray = new JSONArray();

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
		for(int i=0;i<180;i++) {
			connect_ftp_old("patent");
		}
		for(int i=0;i<120;i++) {
			connect_ftp_old("invention");
		}
        try {
			ftpClient.logout();
			System.out.print("logout success\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			ftpClient.disconnect();
			System.out.print("disconnect success\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return "Hello from Lambda!";
        
    }
    private void connect_ftp_old(String type) {
        JSONParser parser=new JSONParser();
		try {
			org.json.simple.JSONObject jsonObject=(org.json.simple.JSONObject) parser.parse(new FileReader("src/main/java/com/amazonaws/lambda/demo/json/types.json"));
			if(type=="patent") {
	    		String[] folders=patent_old_num();
	    		org.json.simple.JSONObject patent_dic=(org.json.simple.JSONObject) jsonObject.get("patent");
				String patent_root=(String)((org.json.simple.JSONObject)patent_dic.get("publish")).get("old_root");
				String patent_public=(String)((org.json.simple.JSONObject)patent_dic.get("publish")).get("old");
				String patent_guide=(String)((org.json.simple.JSONObject)patent_dic.get("guide")).get("old");
				ftpClient.connect(patent_root, 21);
				ftpClient.enterLocalPassiveMode();
				ftpClient.login("anonymous", "");
				System.out.print("ftp connect success");
//				client.connect(patent_root, 21);
				int i=0;
				for(String folder:folders) {
					System.out.print(patent_public+folder);
					xml_process(patent_guide+folder);
				}
	    	}
	    	else {
	    		invention_old_num();
	    		String[] folders=invention_old_num();
	    		org.json.simple.JSONObject invention_dic=(org.json.simple.JSONObject) jsonObject.get("invention");
				String invention_root=(String)((org.json.simple.JSONObject)invention_dic.get("publish")).get("old_root");
				String invention_public=(String)((org.json.simple.JSONObject)invention_dic.get("publish")).get("old");
				String invention_guide=(String)((org.json.simple.JSONObject)invention_dic.get("guide")).get("old");
				ftpClient.connect(invention_root, 21);
				ftpClient.enterLocalPassiveMode();
				ftpClient.login("anonymous", "");
				for(String folder:folders) {
					xml_process(invention_root+invention_guide+folder);
				}
	    	}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void xml_process(String mas) {
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = null ;
    	InputStream is=null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	Document doc = null;
    	try {
			is =ftpClient.retrieveFileStream(mas);
			try {
				doc = db.parse(is);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			is.close();
			ftpClient.completePendingCommand();
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
    	DOMSource domSource = new DOMSource(doc);
    	StringWriter writer = new StringWriter();
    	StreamResult result = new StreamResult(writer); 
    	TransformerFactory tf = TransformerFactory.newInstance();
    	Transformer transformer;
    	NodeList nl= doc.getElementsByTagName("tw-patent-grant");
		try {
			transformer = tf.newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    	for(int i=0;i<nl.getLength();i++) {
	    		domSource.setNode(nl.item(i));
		    	transformer.transform(domSource, result);
	    	}
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	JSONObject jo=XML.toJSONObject(writer.toString());
    	JSONArray ja=jo.getJSONArray("tw-patent-grant");
    	System.out.print(ja.toString(2));
    	System.out.print("this file length=>"+ja.length()+"/n");
    	
//    	for(int i=0;i<nl.getLength();i++) {
//    		Node node=nl.item(i);
//    		System.out.println("\n This Element:"+node.getNodeName());
//    		if(node.getNodeType()==Node.ELEMENT_NODE) {
//    			Element e=(Element) node;
//    			System.out.println("  this certificate-number=>"+e.getAttribute("certificate-number"));
//    			try {
//					System.out.println("  this inventors=>"+new XMLDocument(elementOftag(e,"inventors")).toString());
//	    			System.out.println("  this PUBLIC_DATE=>"+new XMLDocument(elementOftag(e,"date")).toString());
////	    			System.out.println("  this APPLY_NO=>"+new XMLDocument(elementOftag(e,"doc-number",false)).toString());
//	    			System.out.println("  this APPLY_DATE=>"+new XMLDocument(elementOftag(e,"doc-number")).toString());
//	    			System.out.println("  this AGENT=>"+new XMLDocument(elementOftag(e,"agents")).toString());
//	    			System.out.println("  this APPLYER=>"+new XMLDocument(elementOftag(e,"applicants")).toString());
//				} catch (NullPointerException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (ParserConfigurationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//    		}
//    	}
    }
   

    private Element elementOftag(Element ele,String tname) throws ParserConfigurationException,NullPointerException {
    	NodeList tmp=ele.getElementsByTagName(tname);
    	if(tmp.getLength()>0){
        	Element res=(Element) tmp.item(tmp.getLength()-1);
        	return res;
    	}
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    	DocumentBuilder db = dbf.newDocumentBuilder();
    	Element res=db.newDocument().createElement("not_provided");
		return res;

    }
    private String[] patent_old_num() {
    	String[] res=new String[180];
    	int idx=0;
    	for(Integer j=0;j<5;j++) {
        	for(Integer i=1;i<=36;i++) {
        		res[idx]="_04"+j.toString()+String.format("%03d", i)+"/index_all.xml";
        		idx+=1;
        	}
    	}
    	return res;
    }
    private String[] invention_old_num() {
    	String[] res=new String[120];
    	int idx=0;
    	for(Integer j=1;j<6;j++) {
        	for(Integer i=1;i<=24;i++) {
        		res[idx]="_01"+j.toString()+String.format("%03d", i)+"/index_all.xml";
        		idx+=1;
        	}
    	}
    	return res;
    }
    
}
