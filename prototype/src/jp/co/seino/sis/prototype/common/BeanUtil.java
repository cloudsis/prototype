package jp.co.seino.sis.prototype.common;

public class BeanUtil {
	
	public static String rpad(String str, int length) {
		return rpad(str,length," ");
	}
	
	public static String rpad(String str, int length, String ch) {
		str = (str == null) ? "" : str;
		StringBuffer buf = new StringBuffer(str);
		int idx = str.length();
		while (idx < length) {
			buf.append(ch);
			idx++;
		}
		return buf.toString();
	}

	public static String lpad(String str, int length, String ch) {
		str = (str == null) ? "" : str;
		StringBuffer buf = new StringBuffer();
		int idx = str.length();
		while (idx < length) {
			buf.append(ch);
			idx++;
		}
		buf.append(str);
		return buf.toString();
	}

	public static String replace(String org, String from, String to) {
		if(org == null) {
			return org;
		}
		StringBuffer buf = new StringBuffer();
		int idx = org.indexOf(from); 
		if (idx >= 0) {
			buf.append(org.substring(0,idx));
			buf.append(to);
			buf.append(org.substring(idx+from.length()));
			//Ä‹N“I‚Éˆ—
			return replace(buf.toString(),from,to);
		} else {
			buf.append(org);//’uŠ·‚·‚é•¶š—ñ‚ª‚È‚¯‚ê‚Î‚»‚Ì‚Ü‚Ü
		}
		return buf.toString();
	}
	
	public static String removeCharInc(String str, String inc) {
		if(str == null) {
			return str;
		}
		char[] carr = str.toCharArray();
		StringBuffer buf = new StringBuffer();
		for(int idx = 0; idx < carr.length; idx++) {
			if(inc.indexOf(carr[idx]) != -1) {
				buf.append(carr[idx]);
			}
		}
		return buf.toString();
	}
}
