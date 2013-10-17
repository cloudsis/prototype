/*
 * -----------------------------------------------------------------------
 * @(#)ApplDateFormat.java
 *
 * Copyright 2003 Seino Information Service Co.,Ltd. All rights reserved.
 *
 * -----------------------------------------------------------------------
 * �V�X�e�����@�@�F�r�k�h�l�r�|�v�g
 * �T�u�V�X�e�����F����
 * �v���O�������@�F�X�����X�����ҏW
 * -----------------------------------------------------------------------
 * �N���X���F ApplDateFormat
 * �쐬�����F2003.12.12 T03-129 Y.Kanamori �V�K�쐬
 * �C�������F2004.03.29 T03-129 Y.Kanamori parse���\�b�h�ŉ�͂ł��Ȃ������͈͂�
 *                                        �A�y���h���ĕԂ��悤�ɂ���
 * �C�������F2003.99.99 XXX-XXX ���O       �C�����e
 * -----------------------------------------------------------------------
 */
package jp.co.seino.sis.prototype.common;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * �X�����X�����ҏW.
 * @author user
 */
public class ApplDateFormat extends SimpleDateFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** slims.common.db.Table�N���X�p�̃t�H�[�}�b�g */
	public static final String FORMAT_TABLE = "yyyyMMddHHmmss";

	/** DB2�p�̃t�H�[�}�b�g */
	public static final String FORMAT_DB2 = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * Constructor for ApplDateFormat.
	 */
	public ApplDateFormat() {
		super();
	}

	/**
	 * Constructor for ApplDateFormat.
	 * @param pattern
	 */
	public ApplDateFormat(String pattern) {
		super(pattern);
	}

	/**
	 * Constructor for ApplDateFormat.
	 * @param pattern
	 * @param loc
	 */
	public ApplDateFormat(String pattern, Locale loc) {
		super(pattern, loc);
	}

	/**
	 * Constructor for ApplDateFormat.
	 * @param pattern
	 * @param formatData
	 */
	public ApplDateFormat(String pattern, DateFormatSymbols formatData) {
		super(pattern, formatData);
	}

	/**
	 * ���t�̕ϊ�.
	 * �ϊ��O�̓��t������null�ł���΁A�󔒕�����Ƃ��ď�������.
	 * <div>
	 * ��O���o���o�[�W����
	 * </div>
	 * @param src ���t������
	 * @param srcPattern �ϊ��O�̃p�^�[��
	 * @param pattern �ϊ���̃p�^�[��
	 * @return �ϊ���̓��t������
	 * @throws ParseException ���t�̏������Ԉ���Ă���
	 */
	public static String convert(String src, String srcPattern, String pattern)
		throws ParseException {

		if(src == null || src.equals("")) {
			return "";
		}

		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
		inst.setLenient(false);
		if(srcPattern != null && !srcPattern.equals("")) {
			inst.applyPattern(srcPattern);
		}
		ParsePosition pos = new ParsePosition(0);
		Date srcDate = inst.parse(src, pos);
		// setLenient(false)�ł����Ă��AyyMMdd -> yyyyMMdd �ւ̕ϊ�����
		// src��1���̏ꍇ�Ɍ���ē��t�Ƃ��ĔF�������
		// ���ꂪ�X���b�V�������� yy/MMdd�Ȃǂł���Ζ��Ȃ����A1���̏ꍇ��
		// �t�H�[�}�b�g�`�����ɐ����݂̂��܂܂�Ă���ꍇ�́A�����
		// "4" -> "19700104"�ƂȂ�悤��
		// ����āAsetLenient(false)�Ń`�F�b�N������j�Ȃ̂ŁAsrc �� arcPattern��
		// ��������v���Ȃ���Εϊ��͎��s������悤�ɂ���
		if(srcDate == null
		|| pos.getIndex() != src.length()	// ����ł����pos��index�ɍŏI�C���f�b�N�X + 1�AerrorIndex��-1��Ԃ����A
											// �ُ�ł����pos��index��0 ��͂ł����ŏI�C���f�b�N�X + 1��Ԃ�
		|| src.length() != srcPattern.length()) {
			StringBuffer buf = new StringBuffer(src);
			int bufIdx = 0;
			int max = src.length();
			if(srcPattern.length() < max) {
				max = srcPattern.length();
			}
			for(int idx = 0; idx < max; idx++) {
				switch(srcPattern.charAt(idx)) {
					// ����g�p����Ǝv����L���̂ݑΉ�
					case 'y':
					case 'M':
					case 'd':
					case 'H':
					case 'h':
					case 'm':
					case 's':
					case 'S':
					bufIdx += 1;
					break;
					default:
					buf.deleteCharAt(bufIdx);
					break;
				}
			}
			return buf.toString();
		}
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(srcDate);
	}

	/**
	 * ���t�̕ϊ�.
	 * <div>
	 * ��O���o���Ȃ��o�[�W����
	 * </div>
	 * @param src ���t������
	 * @param srcPattern �ϊ��O�̃p�^�[��
	 * @param pattern �ϊ���̃p�^�[��
	 * @return �ϊ���̓��t������
	 */
	public static String convertIgnore(String src, String srcPattern, String pattern) {
		try {
			return convert(src, srcPattern, pattern);
		} catch(ParseException e) {
			StringBuffer buf = new StringBuffer(src);
			int bufIdx = 0;
			int max = src.length();
			if(srcPattern.length() < max) {
				max = srcPattern.length();
			}
			for(int idx = 0; idx < max; idx++) {
				switch(srcPattern.charAt(idx)) {
					// ����g�p����Ǝv����L���̂ݑΉ�
					case 'y':
					case 'M':
					case 'd':
					case 'H':
					case 'h':
					case 'm':
					case 's':
					case 'S':
					bufIdx += 1;
					break;
					default:
					buf.deleteCharAt(bufIdx);
					break;
				}
			}
			return buf.toString();
		}
	}

	/**
	 * �X���b�V�������t����X���b�V���Ȃ����t�ɕϊ�.
	 * <div>"yyyy/MM/dd" -> "yyyyMMdd"</div>
	 * @param src �X���b�V�������t
	 * @return �X���b�V���Ȃ����t
	 */
	public static String convertVw2Md(String src) {
		return convertIgnore(src, "yyyy/MM/dd", "yyyyMMdd");
	}

	/**
	 * �X���b�V���Ȃ����t����X���b�V�������t�֕ϊ�.
	 * <div>"yyyyMMdd" -> "yyyy/MM/dd"</div>
	 * @param src �X���b�V���Ȃ����t
	 * @return �X���b�V�������t
	 */
	public static String convertMd2Vw(String src) {
		return convertIgnore(src, "yyyyMMdd", "yyyy/MM/dd");
	}

	/**
	 * ���ݓ������擾.
	 * <div>
	 * �f�t�H���g�̃��P�[�V�����𔽉f
	 * </div>
	 * @return ���ݓ���������킷������
	 */
//	public static String getCurrentTime() {
//		return getCurrentTime(null);
//	}

	/**
	 * ���ݓ������擾.
	 * <div>
	 * �w�肳�ꂽ�t�H�[�}�b�g���g�p����
	 * </div>
	 * @param pattern
	 * @return
	 */
	public static String getCurrentTime(String pattern) {
		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(new Date());
	}

//	/**
//	 * ���ݓ������擾.
//	 * <div>
//	 * �f�t�H���g�̃��P�[�V�����𔽉f
//	 * </div>
//	 * @return ���ݓ���������킷������
//	 */
//	public static String getCurrentTime(DBConnection dbconn) throws SQLException{
//		return getCurrentTime(dbconn,ApplDateFormat.FORMAT_TABLE);
//	}
////
////	/**
////	 * ���ݓ������擾.
////	 * <div>
////	 * �w�肳�ꂽ�t�H�[�}�b�g���g�p����
////	 * </div>
////	 * @param pattern
////	 * @return
////	 */
////	public static String getCurrentTime(DBConnection dbconn,String pattern) throws SQLException {
////		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
////		if(pattern != null && !pattern.equals("")) {
////			inst.applyPattern(pattern);
////		}
////		return inst.format(dbconn.getCurrentTimestamp());
////	}
//
//	/**
//	 * DB�p�����œ��t���o��.
//	 * <div>
//	 * Table�N���X���g�p����Ƃ��Ɏg�p����
//	 * </div>
//	 * ���ݓ�����
//	 * @return "yyyyMMddHHmmss"�Ńt�H�[�}�b�g���ꂽ���ݓ����̕�����
//	 * @deprecated
//	 */
//	public static String formatUpdateDatet() {
//		return ApplDateFormat.formatUpdateDatet(new Timestamp(System.currentTimeMillis()));
//	}
//	/**
//	 * DB�p�����œ��t���o��.
//	 * <div>
//	 * Table�N���X���g�p����Ƃ��Ɏg�p����
//	 * </div>
//	 * DB�T�[�o�̌��ݓ�����
//	 * @return "yyyyMMddHHmmss"�Ńt�H�[�}�b�g���ꂽ���ݓ����̕�����
//	 */
//	public static String formatUpdateDatet(DBConnection dbconn) throws SQLException{
//		return ApplDateFormat.formatUpdateDatet(dbconn.getCurrentTimestamp());
//	}
	/**
	 * DB�p�����œ��t���o��.
	 * <div>
	 * Table�N���X���g�p����Ƃ��Ɏg�p����
	 * </div>
	 * @param src
	 * @return
	 */
	public static String formatUpdateDatet(Timestamp src) {
		return formatUpdateDatet(src, ApplDateFormat.FORMAT_TABLE);
	}
	/**
	 * DB�p�����œ��t���o��.
	 * <div>
	 * SQL��Timestamp�N���X���g�p����Ƃ��Ɏg�p����
	 * </div>
	 * @param src 
	 * @param pattern ����
	 * @return
	 */
	public static String formatUpdateDatet(Timestamp src, String pattern) {
		if(src == null) {
			return "";
		}
		return ApplDateFormat.formatUpdateDatet(src.getTime(), pattern);
	}
	/**
	 * DB�p�����œ��t���o��.
	 * <div>
	 * LONG�l���g�p����Ƃ��Ɏg�p����
	 * </div>
	 * @param src 
	 * @param pattern ����
	 * @return
	 */
	public static String formatUpdateDatet(long src, String pattern) {
		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
		inst.applyPattern(pattern);
		return inst.format(new Date(src));
	}

	/**
	 * DB�p�����Ŏw�肳�ꂽ���t�����ԂŎ擾.
	 * <div>
	 * Table�N���X���g�p����Ƃ��Ɏg�p����
	 * </div>
	 * �����Ƀ`�F�b�N����
	 * @param src
	 * @return
	 */
	public static Timestamp parseUpdateDatet(String src) throws ParseException {
		return parseUpdateDatet(src, FORMAT_TABLE);
	}

	/**
	 * DB�p�����Ŏw�肳�ꂽ���t�����ԂŎ擾.
	 * <div>
	 * Table�N���X���g�p����Ƃ��Ɏg�p����
	 * </div>
	 * �����Ƀ`�F�b�N����
	 * @param src
	 * @return
	 */
	public static Timestamp parseUpdateDatet(String src, String pattern) throws ParseException {
		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		inst.setLenient(false);
		Date date = inst.parse(src);
		return new Timestamp(date.getTime());
	}

	/**
	 * ���t��r.
	 * <pre>
	 * �ߋ�==================>����
	 * �@�@�@�@A�@�@�@�@B�@�@�@�@�@=>���̒l
	 * �@�@�@�@B�@�@�@�@A�@�@�@�@�@=>���̒l
	 * ��Ԃ��B
	 * </pre>
	 * @param srcA ���t�l�`
	 * @param patternA ���t�l�`�̃p�^�[��(null���w�肳���΃f�t�H���g�̃t�H�[�}�b�g)
	 * @param srcB ���t�l�a
	 * @param patternB ���t�l�a�̃p�^�[��(null���w�肳���΃f�t�H���g�̃t�H�[�}�b�g)
	 * @return �a�|(�}�C�i�X)�`�̒l �`���a�Ȃ琳 �a���`�Ȃ畉
	 * @throws ParseException ���t�l�̉�̓G���[
	 */
	public static long compareTime(String srcA, String patternA, String srcB, String patternB)
	throws ParseException {
		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
		inst.setLenient(false);
		if(patternA != null && !patternA.equals("")) {
			inst.applyPattern(patternA);
		}
		long timeA = inst.parse(srcA).getTime();
		if(patternB != null && !patternB.equals("")) {
			inst.applyPattern(patternB);
		}
		long timeB = inst.parse(srcB).getTime();
		return (timeB - timeA);
	}

	/**
	 * �N�𑫂�.
	 * <code>
	 * slims.common.ApplDateFormat.formatUpdateDatet(
	 * slims.common.ApplDateFormat.addYear(System.currentTimeMillis(), 2),
	 * "yyyy/MM/dd");
	 * �̂悤�Ɏg�p����B
	 * </code>
	 * @param time �������ƂȂ鎞��
	 * @param amount ������.�|(�}�C�i�X)�Ȃ猸��
	 * @return �����ꂽ����
	 */
	public static long addYear(long time, int amount) {
		return ApplDateFormat.add(time, amount, Calendar.YEAR);
	}

	/**
	 * ���𑫂�.
	 * <code>
	 * slims.common.ApplDateFormat.formatUpdateDatet(
	 * slims.common.ApplDateFormat.addMonth(System.currentTimeMillis(), -1),
	 * "yyyy/MM/dd");
	 * �̂悤�Ɏg�p����B
	 * </code>
	 * @param time �������ƂȂ鎞��
	 * @param amount ������.�|(�}�C�i�X)�Ȃ猸��
	 * @return �����ꂽ����
	 */
	public static long addMonth(long time, int amount) {
		return ApplDateFormat.add(time, amount, Calendar.MONTH);
	}

	/**
	 * ���𑫂�.
	 * <code>
	 * slims.common.ApplDateFormat.formatUpdateDatet(
	 * slims.common.ApplDateFormat.addDay(System.currentTimeMillis(), -5),
	 * "yyyy/MM/dd");
	 * �̂悤�Ɏg�p����B
	 * </code>
	 * @param time �������ƂȂ鎞��
	 * @param amount ������.�|(�}�C�i�X)�Ȃ猸��
	 * @return �����ꂽ����
	 */
	public static long addDay(long time, int amount) {
		return ApplDateFormat.add(time, amount, Calendar.DATE);
	}
	/**
	 * ���𑫂�.(�����w��)
	 * <code>
	 * slims.common.ApplDateFormat.addDay("2004/04/01", -5,"yyyy/MM/dd");
	 * �̂悤�Ɏg�p����B�߂�l��"2004/03/27"�ƂȂ�B
	 * </code>
	 * @param time �������ƂȂ鎞��
	 * @param amount ������.�|(�}�C�i�X)�Ȃ猸��
	 * @param pattern ���t����
	 * @return �����ꂽ����
	 */
	public static String addDay(String date, int amount, String pattern) throws ParseException {
		if ( date == null ) {
			return "";
		}
		Timestamp wk = ApplDateFormat.parseUpdateDatet(date,pattern);
		long time = ApplDateFormat.addDay(wk.getTime(),amount);
		return ApplDateFormat.formatUpdateDatet(time,pattern);
	}

	/**
	 * ���Ԃ𑫂�.
	 * @param time �������ƂȂ鎞��
	 * @param amount ������.�|(�}�C�i�X)�Ȃ猸��
	 * @param field �������Ԃ̎��.Calendar�N���X�̌Œ�l���g�p���邱��.
	 * ���Ȃ� Calendar.MINUTE�Ȃ�
	 * @see Calendar
	 * @return �����ꂽ����
	 */
	public static long add(long time, int amount, int field) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(new Date(time));
		cal.add(field, amount);
		return cal.getTime().getTime();
	}

}
