package jp.co.seino.sis.prototype.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

	/**
	 * AES�Í���
	 * @param val
	 * @param key
	 * @return
	 */
	public static String encryptAES(String val, String key) {
		if (val == null || val.equals("") ) return val;
		try {
		    // ��
		    SecretKeySpec sks = new SecretKeySpec(key.getBytes(),"AES");
		    // �Í���
		    Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    c.init(Cipher.ENCRYPT_MODE, sks);
		    byte input[] = val.getBytes();
		    byte encrypted[] = c.doFinal(input);
		    // �o�C�g�z��𕶎���ɂ���
		    StringBuffer buf = new StringBuffer();
		    for (int i=0; i<encrypted.length; i++) {
		    	//char ch = (char)encrypted[i];
		    	String str = Integer.toHexString(encrypted[i]).toUpperCase();
		    	str = str.replaceAll("FFFFFF","");//FFFFFF����菜��
		    	str = (str.length() == 1) ? "0"+str : str; //��0����
		    	buf.append(str);
		    }
	        return buf.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
