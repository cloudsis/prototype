package jp.co.seino.sis.prototype.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

	/**
	 * AES暗号化
	 * @param val
	 * @param key
	 * @return
	 */
	public static String encryptAES(String val, String key) {
		if (val == null || val.equals("") ) return val;
		try {
		    // 鍵
		    SecretKeySpec sks = new SecretKeySpec(key.getBytes(),"AES");
		    // 暗号化
		    Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    c.init(Cipher.ENCRYPT_MODE, sks);
		    byte input[] = val.getBytes();
		    byte encrypted[] = c.doFinal(input);
		    // バイト配列を文字列にする
		    StringBuffer buf = new StringBuffer();
		    for (int i=0; i<encrypted.length; i++) {
		    	//char ch = (char)encrypted[i];
		    	String str = Integer.toHexString(encrypted[i]).toUpperCase();
		    	str = str.replaceAll("FFFFFF","");//FFFFFFを取り除く
		    	str = (str.length() == 1) ? "0"+str : str; //頭0埋め
		    	buf.append(str);
		    }
	        return buf.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
