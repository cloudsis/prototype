/*
 * -----------------------------------------------------------------------
 * @(#)ApplDateFormat.java
 *
 * Copyright 2003 Seino Information Service Co.,Ltd. All rights reserved.
 *
 * -----------------------------------------------------------------------
 * システム名　　：ＳＬＩＭＳ－ＷＨ
 * サブシステム名：共通
 * プログラム名　：スリムス日時編集
 * -----------------------------------------------------------------------
 * クラス名： ApplDateFormat
 * 作成期日：2003.12.12 T03-129 Y.Kanamori 新規作成
 * 修正履歴：2004.03.29 T03-129 Y.Kanamori parseメソッドで解析できなかった範囲を
 *                                        アペンドして返すようにした
 * 修正履歴：2003.99.99 XXX-XXX 名前       修正内容
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
 * スリムス日時編集.
 * @author user
 */
public class ApplDateFormat extends SimpleDateFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** slims.common.db.Tableクラス用のフォーマット */
	public static final String FORMAT_TABLE = "yyyyMMddHHmmss";

	/** DB2用のフォーマット */
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
	 * 日付の変換.
	 * 変換前の日付文字列がnullであれば、空白文字列として処理する.
	 * <div>
	 * 例外を出すバージョン
	 * </div>
	 * @param src 日付文字列
	 * @param srcPattern 変換前のパターン
	 * @param pattern 変換後のパターン
	 * @return 変換後の日付文字列
	 * @throws ParseException 日付の書式が間違っている
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
		// setLenient(false)であっても、yyMMdd -> yyyyMMdd への変換時に
		// srcが1桁の場合に誤って日付として認識される
		// これがスラッシュがある yy/MMddなどであれば問題ないが、1桁の場合で
		// フォーマット形式中に数字のみが含まれている場合は、誤って
		// "4" -> "19700104"となるようだ
		// よって、setLenient(false)でチェックする方針なので、src と arcPatternの
		// 長さが一致しなければ変換は失敗させるようにする
		if(srcDate == null
		|| pos.getIndex() != src.length()	// 正常であればposはindexに最終インデックス + 1、errorIndexに-1を返すが、
											// 異常であればposはindexに0 解析できた最終インデックス + 1を返す
		|| src.length() != srcPattern.length()) {
			StringBuffer buf = new StringBuffer(src);
			int bufIdx = 0;
			int max = src.length();
			if(srcPattern.length() < max) {
				max = srcPattern.length();
			}
			for(int idx = 0; idx < max; idx++) {
				switch(srcPattern.charAt(idx)) {
					// 今回使用すると思われる記号のみ対応
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
	 * 日付の変換.
	 * <div>
	 * 例外を出さないバージョン
	 * </div>
	 * @param src 日付文字列
	 * @param srcPattern 変換前のパターン
	 * @param pattern 変換後のパターン
	 * @return 変換後の日付文字列
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
					// 今回使用すると思われる記号のみ対応
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
	 * スラッシュつき日付からスラッシュなし日付に変換.
	 * <div>"yyyy/MM/dd" -> "yyyyMMdd"</div>
	 * @param src スラッシュつき日付
	 * @return スラッシュなし日付
	 */
	public static String convertVw2Md(String src) {
		return convertIgnore(src, "yyyy/MM/dd", "yyyyMMdd");
	}

	/**
	 * スラッシュなし日付からスラッシュつき日付へ変換.
	 * <div>"yyyyMMdd" -> "yyyy/MM/dd"</div>
	 * @param src スラッシュなし日付
	 * @return スラッシュつき日付
	 */
	public static String convertMd2Vw(String src) {
		return convertIgnore(src, "yyyyMMdd", "yyyy/MM/dd");
	}

	/**
	 * 現在日時を取得.
	 * <div>
	 * デフォルトのロケーションを反映
	 * </div>
	 * @return 現在日時をあらわす文字列
	 */
//	public static String getCurrentTime() {
//		return getCurrentTime(null);
//	}

	/**
	 * 現在日時を取得.
	 * <div>
	 * 指定されたフォーマットを使用する
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
//	 * 現在日時を取得.
//	 * <div>
//	 * デフォルトのロケーションを反映
//	 * </div>
//	 * @return 現在日時をあらわす文字列
//	 */
//	public static String getCurrentTime(DBConnection dbconn) throws SQLException{
//		return getCurrentTime(dbconn,ApplDateFormat.FORMAT_TABLE);
//	}
////
////	/**
////	 * 現在日時を取得.
////	 * <div>
////	 * 指定されたフォーマットを使用する
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
//	 * DB用書式で日付を出力.
//	 * <div>
//	 * Tableクラスを使用するときに使用する
//	 * </div>
//	 * 現在日時で
//	 * @return "yyyyMMddHHmmss"でフォーマットされた現在日時の文字列
//	 * @deprecated
//	 */
//	public static String formatUpdateDatet() {
//		return ApplDateFormat.formatUpdateDatet(new Timestamp(System.currentTimeMillis()));
//	}
//	/**
//	 * DB用書式で日付を出力.
//	 * <div>
//	 * Tableクラスを使用するときに使用する
//	 * </div>
//	 * DBサーバの現在日時で
//	 * @return "yyyyMMddHHmmss"でフォーマットされた現在日時の文字列
//	 */
//	public static String formatUpdateDatet(DBConnection dbconn) throws SQLException{
//		return ApplDateFormat.formatUpdateDatet(dbconn.getCurrentTimestamp());
//	}
	/**
	 * DB用書式で日付を出力.
	 * <div>
	 * Tableクラスを使用するときに使用する
	 * </div>
	 * @param src
	 * @return
	 */
	public static String formatUpdateDatet(Timestamp src) {
		return formatUpdateDatet(src, ApplDateFormat.FORMAT_TABLE);
	}
	/**
	 * DB用書式で日付を出力.
	 * <div>
	 * SQLのTimestampクラスを使用するときに使用する
	 * </div>
	 * @param src 
	 * @param pattern 書式
	 * @return
	 */
	public static String formatUpdateDatet(Timestamp src, String pattern) {
		if(src == null) {
			return "";
		}
		return ApplDateFormat.formatUpdateDatet(src.getTime(), pattern);
	}
	/**
	 * DB用書式で日付を出力.
	 * <div>
	 * LONG値を使用するときに使用する
	 * </div>
	 * @param src 
	 * @param pattern 書式
	 * @return
	 */
	public static String formatUpdateDatet(long src, String pattern) {
		SimpleDateFormat inst = (SimpleDateFormat)SimpleDateFormat.getInstance();
		inst.applyPattern(pattern);
		return inst.format(new Date(src));
	}

	/**
	 * DB用書式で指定された日付を時間で取得.
	 * <div>
	 * Tableクラスを使用するときに使用する
	 * </div>
	 * 厳密にチェックする
	 * @param src
	 * @return
	 */
	public static Timestamp parseUpdateDatet(String src) throws ParseException {
		return parseUpdateDatet(src, FORMAT_TABLE);
	}

	/**
	 * DB用書式で指定された日付を時間で取得.
	 * <div>
	 * Tableクラスを使用するときに使用する
	 * </div>
	 * 厳密にチェックする
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
	 * 日付比較.
	 * <pre>
	 * 過去==================>未来
	 * 　　　　A　　　　B　　　　　=>正の値
	 * 　　　　B　　　　A　　　　　=>負の値
	 * を返す。
	 * </pre>
	 * @param srcA 日付値Ａ
	 * @param patternA 日付値Ａのパターン(nullが指定されればデフォルトのフォーマット)
	 * @param srcB 日付値Ｂ
	 * @param patternB 日付値Ｂのパターン(nullが指定されればデフォルトのフォーマット)
	 * @return Ｂ－(マイナス)Ａの値 Ａ＜Ｂなら正 Ｂ＜Ａなら負
	 * @throws ParseException 日付値の解析エラー
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
	 * 年を足す.
	 * <code>
	 * slims.common.ApplDateFormat.formatUpdateDatet(
	 * slims.common.ApplDateFormat.addYear(System.currentTimeMillis(), 2),
	 * "yyyy/MM/dd");
	 * のように使用する。
	 * </code>
	 * @param time 足す元となる時間
	 * @param amount 足す量.－(マイナス)なら減る
	 * @return 足された時間
	 */
	public static long addYear(long time, int amount) {
		return ApplDateFormat.add(time, amount, Calendar.YEAR);
	}

	/**
	 * 月を足す.
	 * <code>
	 * slims.common.ApplDateFormat.formatUpdateDatet(
	 * slims.common.ApplDateFormat.addMonth(System.currentTimeMillis(), -1),
	 * "yyyy/MM/dd");
	 * のように使用する。
	 * </code>
	 * @param time 足す元となる時間
	 * @param amount 足す量.－(マイナス)なら減る
	 * @return 足された時間
	 */
	public static long addMonth(long time, int amount) {
		return ApplDateFormat.add(time, amount, Calendar.MONTH);
	}

	/**
	 * 日を足す.
	 * <code>
	 * slims.common.ApplDateFormat.formatUpdateDatet(
	 * slims.common.ApplDateFormat.addDay(System.currentTimeMillis(), -5),
	 * "yyyy/MM/dd");
	 * のように使用する。
	 * </code>
	 * @param time 足す元となる時間
	 * @param amount 足す量.－(マイナス)なら減る
	 * @return 足された時間
	 */
	public static long addDay(long time, int amount) {
		return ApplDateFormat.add(time, amount, Calendar.DATE);
	}
	/**
	 * 日を足す.(書式指定)
	 * <code>
	 * slims.common.ApplDateFormat.addDay("2004/04/01", -5,"yyyy/MM/dd");
	 * のように使用する。戻り値は"2004/03/27"となる。
	 * </code>
	 * @param time 足す元となる時間
	 * @param amount 足す量.－(マイナス)なら減る
	 * @param pattern 日付書式
	 * @return 足された時間
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
	 * 時間を足す.
	 * @param time 足す元となる時間
	 * @param amount 足す量.－(マイナス)なら減る
	 * @param field 足す時間の種類.Calendarクラスの固定値を使用すること.
	 * 分なら Calendar.MINUTEなど
	 * @see Calendar
	 * @return 足された時間
	 */
	public static long add(long time, int amount, int field) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(new Date(time));
		cal.add(field, amount);
		return cal.getTime().getTime();
	}

}
