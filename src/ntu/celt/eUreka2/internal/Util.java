package ntu.celt.eUreka2.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import ntu.celt.eUreka2.entities.User;


public class Util {
	private static final String LOGIN_HASH_KEY = "CED4958AFE33E2EFECAS903";
	private static final String HASH_ALGORITHM = "MD5";
	
	/**
	 * Generate a Hash value based on internal algorithm and key
	 * 
	 * @param msg - the message to be used to hash
	 * @return the hashed value
	 */
	public static String generateHashValue(String msg) {
		msg = msg + LOGIN_HASH_KEY;
		try {
			MessageDigest md5 = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hash = md5.digest(msg.getBytes());
			return byteArrayToHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Generate a Hash value based on internal algorithm, and the input key
	 * 
	 * @param msg - the message to be used to hash
	 * @param hashKey - the key to be used to hash
	 * @return the hash value
	 */
	public static String generateHashValue(String msg, String hashKey) {
		msg = msg + hashKey;
		try {
			MessageDigest md5 = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hash = md5.digest(msg.getBytes());
			return byteArrayToHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convert byte Array into Hexadecimal String
	 * 
	 * @param b
	 * @return
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * Capitalize very word in the string
	 * 
	 * @param str
	 * @return
	 */
	public static String capitalize(String str) {
		if (str==null || str.length() == 0)
			return str;
		StringBuffer outStr = new StringBuffer(str.length()+1);
		String[] words = str.split(" ");

		for (int i = 0; i < words.length; i++) {
			outStr.append(capitalizeFirstLetter(words[i]) + " ");
		}
		
		return outStr.toString().trim(); 
	}
	/**
	 * Capitalize the first letter of the string
	 * (toUpperCase for the first letter, and toLowerCase for the rest
	 * 
	 * @param s
	 * @return
	 */
	public static String capitalizeFirstLetter(String str) {
		if (str==null || str.length() == 0)
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}
	
	/** 
	 * Deletes all files and subdirectories under dir.
     * 
     * @param dir 
     * @return Returns true if all deletions were successful.
     *      If a deletion fails, the method stops attempting to delete and returns false.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    /**
     * Unzip a zip file into the destination directory
     * 
     * @param zipFile
     * @param homeParentDir
     */
    @SuppressWarnings("unchecked")
	public static void unzipFileIntoDirectory(ZipFile zipFile, File homeParentDir) {
        Enumeration files = zipFile.entries();
        File f = null;
        FileOutputStream fos = null;
        
        while (files.hasMoreElements()) {
          try {
            ZipEntry entry = (ZipEntry) files.nextElement();
            InputStream eis = zipFile.getInputStream(entry);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
      
            f = new File(homeParentDir.getAbsolutePath() + "/" + entry.getName());
            
            if (entry.isDirectory()) {
              f.mkdirs();
              continue;
            } else {
              f.getParentFile().mkdirs();
              f.createNewFile();
            }
            
            fos = new FileOutputStream(f);
      
            while ((bytesRead = eis.read(buffer)) != -1) {
              fos.write(buffer, 0, bytesRead);
            }
          } catch (IOException e) {
            e.printStackTrace();
            continue;
          } finally {
            if (fos != null) {
              try {
                fos.close();
              } catch (IOException e) {
                // ignore
              }
            }
          }
        }
    }
    
    /**
	 * Generate default password
	 * 
	 * @param msg
	 * @return
	 */
	public static String generateDefaultPassword(User user) {
		return generateHashValue(user.getUsername());
	}
	
	public static String stripTags(String htmlString){
		if(htmlString==null) 
			return null;
		String t = Html2Text.parseHtml(htmlString);
		if(t==null)
			return null;
		return t.replace("&nbsp;", " ");
		//return htmlString.replaceAll("\\<.*?>",""); //this is a simple way
	}
	
	public static Date stringToDate(String dateStr, String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
	        return sdf.parse(dateStr);            
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
		return null;

	}
	public static Date longObjToDate(Object value){
		Long longValue = (long) 0;
		if(value instanceof Long)
			longValue = (Long) value;
		else 
			return null;
		if(longValue==0)
			return null;
		return new Date(longValue);
	}
	
	
	public static String formatDateTime(Date date, String pattern){
		if(date==null) 
			return "";
		
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	public static String formatDateTime2(Date date){
		return formatDateTime(date, Config.getString(Config.FORMAT_TIME_PATTERN));
	}
	
	public static String formatDateTime(Date date){
		if(date==null)
			return "";
		Calendar now = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		long diff = now.getTimeInMillis() - cal.getTimeInMillis();
		if(diff<10000) //10*1000
			return "a few seconds ago";
		if(diff<60000) //60*1000
			return diff/(1000) +" seconds ago";
		if(diff<180000) //3*60*1000
			return "a few minutes ago";
		if(diff<3600000) //60*60*1000
			return diff/(60*1000) +" minutes ago";
		if(diff<7200000) //2*60*60*1000
			return "about an hour ago";
		if(diff<24*3600000) //24*60*60*1000
			return diff/(3600000) + " hours ago";
		if(diff<48*3600000) //48*60*60*1000
			if(now.get(Calendar.DAY_OF_YEAR)-cal.get(Calendar.DAY_OF_YEAR)==1 )
				return "yesterday";
	//	if(now.get(Calendar.YEAR)==cal.get(Calendar.YEAR))
	//			return new SimpleDateFormat("d MMM HH:mm a").format(date);
		
		return new SimpleDateFormat("d MMM yyyy HH:mm a").format(date);
		 
	}
	
	
	public static String nvl(String str){
		if(str==null)
			return "";
		return str;
	}
	
	public static Long generateLongID(){
		return RandomUtils.nextLong();
	}
	
	public static String generateUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "");
	}
	
	/**
	 * Calculates the number of days between two calendar days in a manner
	 * which is independent of the Calendar type used.
	 *
	 * @param d1    The first date.
	 * @param d2    The second date.
	 *
	 * @return      The number of days between the two dates.  Zero is
	 *              returned if the dates are the same, one if the dates are
	 *              adjacent, etc.  The order of the dates
	 *              does not matter, the value returned is always >= 0.
	 *              If Calendar types of d1 and d2
	 *              are different, the result may not be accurate.
	 */
	public static int getNumDaysBetween (java.util.Calendar d1, java.util.Calendar d2) {
	    if (d1.after(d2)) {  // swap dates so that d1 is start and d2 is end
	        java.util.Calendar swap = d1;
	        d1 = d2;
	        d2 = swap;
	    }
	    int days = d2.get(java.util.Calendar.DAY_OF_YEAR) -
	               d1.get(java.util.Calendar.DAY_OF_YEAR);
	    int y2 = d2.get(java.util.Calendar.YEAR);
	    if (d1.get(java.util.Calendar.YEAR) != y2) {
	        d1 = (java.util.Calendar) d1.clone();
	        do {
	            days += d1.getActualMaximum(java.util.Calendar.DAY_OF_YEAR);
	            d1.add(java.util.Calendar.YEAR, 1);
	        } while (d1.get(java.util.Calendar.YEAR) != y2);
	    }
	    return days;
	} // getNumDaysBetween()

	public static int dateDiff(Date earlierDate, Date laterDate){
		Date d1 = DateUtils.truncate(earlierDate, Calendar.DATE);
		Date d2 = DateUtils.truncate(laterDate, Calendar.DATE);
		
		return (int) ((d2.getTime() - d1.getTime()) / 86400000 ); //(24*60*60*1000));
	}
	
	public static String formatCurrency(Double num){
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		return nf.format(num);
	}
	
	public static String formatFileSize(long size){
		if(size<=0) return "0";
		final String[] units = new String[]{"B", "KB", "MB", "GB","TB"};
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size/Math.pow(1024,digitGroups)) + " " + units[digitGroups];
	}
	public static String formatDecimal(double num){
		return new DecimalFormat("0.#").format(num);
	}
	public static String formatDecimal2(double num){
		return new DecimalFormat("0.##").format(num);
	}
	public static String formatDecimal(double num, String format){
		return new DecimalFormat(format).format(num);
	}
	/*public static String formatDecimal(double num){
		return new DecimalFormat("0.##").format(num);
	}*/
	
	/**
	 * stringList to IntegerList
	 */
	public static List<Integer> StrList2IntList(List<String> strList){
		List<Integer> l = new ArrayList<Integer>();
		for(String s : strList){
			l.add(Integer.parseInt(s));
		}
		return l;
	}
	
	public static String textarea2html(String inputText){
		if(inputText == null) 
			return null;
		return Util.filterOutRestrictedHtmlTags(inputText.trim()).replace("\n", "<br/>");  //strip out potential html code
	}
	public static String html2textarea(String inputText){
		if(inputText == null) 
			return null;
		return inputText.replace("<br/>","\n").replace("<BR>", "\n");
	}
	public static String filterOutRestrictedHtmlTags(String inputHtml){
		if(inputHtml==null || inputHtml.isEmpty())
			return inputHtml;
		String invalidTags[] = {"script"};
		for(String tag : invalidTags){
	        Pattern script = Pattern.compile("<"+tag+".*?>.*?</"+tag+">", Pattern.CASE_INSENSITIVE);
	        Matcher mscript = script.matcher(inputHtml);
	        while (mscript.find()) 
	        	inputHtml = mscript.replaceAll("");
		}
		
		return inputHtml;
	}
/*	public static String identifyURLText2html(String text){
		Pattern patt = Pattern.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>???“”‘’]))");
	    Matcher matcher = patt.matcher(text);    
	    if(matcher.find()){
	        if (matcher.group(1).startsWith("http://") || matcher.group(1).startsWith("https://")){
	            return matcher.replaceAll("<a href=\"$1\">$1</a>");
	        }else{
	            return matcher.replaceAll("<a href=\"http://$1\">$1</a>");
	        }   
	    }else{
	        return text;
	    }
	}
*/	
	/**
	 * 
	 * @param fullEmail
	 * @return partial of the email, e.g: eureka2@ntu.edu.sg  -->  e******@n**.***.**
	 */
	public static String getPartialEmail(String fullEmail){
		if(fullEmail==null || fullEmail.isEmpty())
			return fullEmail;
		//probably also check for valid email first
		
		StringBuffer rtn = new StringBuffer();
		rtn.append(fullEmail.charAt(0));
		for(int i=1; i<fullEmail.length(); i++){
			if(fullEmail.charAt(i) == '@'){
				rtn.append('@');
				i++;
				rtn.append(fullEmail.charAt(i));
			}
			else if(fullEmail.charAt(i) == '.'){
				rtn.append('.');
			}
			else
				rtn.append('*');
		}
		
		return rtn.toString().toLowerCase();
	}
	
	public static String encode(String str){
		if(str==null) 
			return "";
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}
	public static String decode(String str){
		if(str==null) 
			return "";
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}
	public static String truncateString(String str , int maxlenght){
		return Util.truncateString(str, maxlenght, null);
	}
	public static String truncateString(String str , int maxlenght, String suffix){
		if(str==null || str.isEmpty()) return str;
		String trimmedStr = str;
		if(suffix==null)
			suffix = "...";
		if(str.length()>maxlenght){
			int endIndex = maxlenght - 1 - suffix.length();
			if(endIndex<0)
				endIndex = 0;
			trimmedStr = str.substring(0, endIndex).trim() + suffix;
		}
		
		return trimmedStr;
	}
	
	/**
	 * rename the file if exists, e.g: filename.txt  -->  filename_v2.txt  --> filename_v2.txt
	 * @param file
	 * @return new Object[]{newFile, version number}
	 */
	public static Object[] autoRenameFileIfExist(File file){
		String extension = FilenameUtils.getExtension(file.getName());
		int dotIndex = file.getAbsolutePath().lastIndexOf(".");
		String tempPath;
		if(dotIndex>0)
			tempPath = file.getAbsolutePath().substring(0, dotIndex);
		else
			tempPath = file.getAbsolutePath();
		int i = 1;
		while(file.exists()){
			i++;
			String newPath = tempPath + "_v"+i+"."+ extension;
			file = new File(newPath);
		}
		return new Object[]{file, i};
	}
	
	/**
	 * append fileName in the middle, ignore extension. e.g: "filename.txt" + "_v2" -->  "filename_v2.txt"
	 * @param fileName
	 * @return newFileName
	 */
	public static String appendToFileName(String fileName, String appendText){
		String extension = FilenameUtils.getExtension(fileName);
		int dotIndex = fileName.lastIndexOf(".");
		String tempName;
		if(dotIndex>0)
			tempName = fileName.substring(0, dotIndex) + appendText + "."+extension;
		else
			tempName = fileName + appendText;
		
		return tempName;
	}
	
	public static String appendSequenceNo(String str, String prefix, String sufix){
		if(str==null)
			return null;
		String tempStr = str;
		String numStr = null;
		int subIndex = 0;
		int preIndex = 0;
		if(sufix==null)
			sufix = "";
		if(prefix==null)
			prefix = "";
		
		if(!sufix.isEmpty()){ 
			subIndex = str.lastIndexOf(sufix);
			if(subIndex!=-1 && (str.length()==(subIndex+sufix.length())))
				tempStr = str.substring(0, subIndex);
		}
		if(!prefix.isEmpty()){  
			preIndex = tempStr.lastIndexOf(prefix);
			if(preIndex!=-1)
				numStr = tempStr.substring(preIndex+prefix.length());
		}
		else{
			preIndex = tempStr.length()-1;
			if(preIndex!=-1)
				numStr = tempStr.substring(preIndex);
		}
		
		int num = 1;
		try{
			num = Integer.parseInt(numStr);
			num++;
		}
		catch(NumberFormatException ex){
			//System.out.println(ex.getMessage());
			return tempStr+prefix+num+sufix;
		}
		str = tempStr.substring(0, preIndex+prefix.length())+num+sufix;
		return str;
	}
	
	
	public static String replaceHighlightTextHTML(String longText, Collection<String> words){
		if(longText==null) 
			return longText;
		String cssClass = "srchHL";
		
		String regex = "";
		for(String word : words){
			regex += Pattern.quote(word)+"|";
		}
		regex = Util.removeLastSeparator(regex, "|");
		
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(longText);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			String replacement = "<span class='"+cssClass+"'>"+m.group()+"</span>";
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		
		return Util.filterOutRestrictedHtmlTags(sb.toString());
		
	}
	public static String checkAndAddHTTP(String url){
		if(url!=null){
			String temp = url.trim().toLowerCase();
			if(!temp.startsWith("http://") && !temp.startsWith("https://") 
				&&	!temp.startsWith("ftp://") && !temp.startsWith("/") ){
				url = "http://"+ url;
			}
		}
		return url;
	}
	
	public static boolean isContains(Object[] objs, Object obj){
		for(Object o : objs){
			if(o.equals(obj))
				return true;
		}
		return false;
	}
	
	public static String removeLastSeparator(String str, String separator){
		if(str==null || str.isEmpty() || separator==null)
			return str;
		
		str = str.substring(0, str.length()-separator.length());
		return str;
	}
	
	public static boolean isImageExtension(String fileName){
		return FilenameUtils.isExtension(fileName, 
				new String[]{"jpg","jpeg","gif","png","JPG","JPEG","GIF","PNG"});
	}
	
	public static String extractDisplayNames(Collection<User> users){
		String names = "";
		int i=0;
		for(User u : users){
			if(i==0)
				names = u.getDisplayName();
			else
				names += ", "+ u.getDisplayName();
			i++;
		}
		return names;
	}
	public static String collectionToString(Collection<String> strs, String seperator){
		String to = "";
		for(String str : strs){
			to += str + seperator;
		}
		to = Util.removeLastSeparator(to, seperator);
		
		return to;
	}
	public static String setToOrderedString(Set<Integer> nums, String seperator){
		String to = "";
		ArrayList<Integer> daysToRemindList =  new ArrayList<Integer>(nums);
		Collections.sort(daysToRemindList);
		for(Integer num : nums){
			to += num.toString() + seperator;
		}
		to = Util.removeLastSeparator(to, seperator);
		
		return to;
	}
	
	public static String additionContentTypeCheck(String contentType, String fileName){
		if(fileName==null)
			return contentType;
		if(contentType==null || contentType.equals("content/unknown") || contentType.equals("application/octet-stream")){
			if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"doc","docx"})){
				contentType = "application/msword";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"ppt","ppt"})){
				contentType = "application/powerpoint";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"xls","xlsx"})){
				contentType = "application/excel";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"pdf"})){
				contentType = "application/pdf";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"jpg","jpeg"})){
				contentType = "image/jpeg";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"png"})){
				contentType = "image/png";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"gif"})){
				contentType = "image/gif";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"flv","wmv","avi","mov","mp4"})){
				contentType = "video/mpeg";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"mp3","wma","wav"})){
				contentType = "audio/wav";
			}
			else if(FilenameUtils.isExtension(fileName.toLowerCase(), new String[]{"zip","rar","gz","gzip","7z"})){
				contentType = "application/zip";
			}
		}
		return contentType;
	}
	
	public static Date getTodayWithoutTime(){
		Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    return cal.getTime();
	}
	public static Date getDate0000(Date datetime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetime);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    return cal.getTime();
	}
	
	public static Date getDate2359(Date datetime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetime);
	    cal.set(Calendar.HOUR_OF_DAY, 23);
	    cal.set(Calendar.MINUTE, 59);
	    cal.set(Calendar.SECOND, 59);
	    cal.set(Calendar.MILLISECOND, 59);
	    return cal.getTime();
	}
	public static Date getDatePlus1(Date datetime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetime);
		cal.add(Calendar.DAY_OF_YEAR, +1);
	    return cal.getTime();
	}
	
	public static Date getDateHHmm(Date datetime){
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetime);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    return cal.getTime();
	}
	
	public static boolean isInteger(String strNumber) {
		try{
			int a = Integer.parseInt(strNumber);
		}
		catch (RuntimeException ex){
			return false;
		}
		return true;
		
	}
	public static String arrayToString(Object[] objs){
		String str = "";
		for(Object o : objs){
			str += o.toString()+",";
		}
		if(str.length()>0)
			str = removeLastSeparator(str, ",");
		return str;
	}
	
	public static boolean stringYN2Boolean(String str){
		if(null == str || str.isEmpty() || str.toUpperCase().equals("N"))
			return false;
		return true;
	}
	public static String Boolean2stringYN(boolean bool){
		if(bool)
			return "Y";
		return "N";
	}
	
	public static double getStdDev(List<Double> values){
		Double[] d = new Double[values.size()];
		values.toArray(d);
		MathStatistic m = new MathStatistic(d);
		return m.getStdDev();
		
	}
	
	
	public static void sendHttpGETAsync(String url){
		try{
			CloseableHttpClient httpclient = HttpClients.createDefault();
			try{
				HttpGet httpGet = new HttpGet(url);
				CloseableHttpResponse response1 = httpclient.execute(httpGet);
				// The underlying HTTP connection is still held by the response object
				// to allow the response content to be streamed directly from the network socket.
				// In order to ensure correct deallocation of system resources
				// the user MUST call CloseableHttpResponse#close() from a finally clause.
				// Please note that if response content is not fully consumed the underlying
				// connection cannot be safely re-used and will be shut down and discarded
				// by the connection manager. 
				try {
				    System.out.println("Sent request to " + url + ". response Status:" +response1.getStatusLine());
				    HttpEntity entity1 = response1.getEntity();
				    // do something useful with the response body
				    // and ensure it is fully consumed
//				    EntityUtils.consume(entity1);
				} finally {
				    response1.close();
				}
			}
			finally {
	            httpclient.close();
	        }
		}
		catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		
		}
		
	}
	

}
