package ntu.celt.eUreka2.internal;

import java.io.*;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

public class Html2Text extends HTMLEditorKit.ParserCallback {
 StringBuffer s;

 public Html2Text() {}

 public void parse(Reader in) throws IOException {
   s = new StringBuffer();
   ParserDelegator delegator = new ParserDelegator();
   // the third parameter is TRUE to ignore charset directive
   delegator.parse(in, this, Boolean.TRUE);
 }

 public void handleText(char[] text, int pos) {
   s.append(text);
 }

 public String getText() {
   return s.toString();
 }

 public static String parseHtml(String htmlStr){
	 StringReader in = new StringReader(htmlStr);
	 Html2Text parser = new Html2Text();
     try {
		parser.parse(in);
		in.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
    return parser.getText();
 }
}

