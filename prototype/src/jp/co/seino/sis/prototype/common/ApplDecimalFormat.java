/*
 * -----------------------------------------------------------------------
 * @(#)ApplDecimalFormat.java
 *
 * Copyright 2003 Seino Information Service Co.,Ltd. All rights reserved.
 *
 * -----------------------------------------------------------------------
 * システム名　　：ＳＬＩＭＳ－ＷＨ
 * サブシステム名：共通
 * プログラム名　：スリムス数字編集クラス
 * -----------------------------------------------------------------------
 * クラス名： ApplDecimalFormat
 * 作成期日：2003.12.12 T03-129 Y.Kanamori 新規作成
 * 修正履歴：2008.04.30 T07-425 E.Yokoi parseNumberメソッドの-0.X変換対応
 * 修正履歴：2003.99.99 XXX-XXX 名前       修正内容
 * 修正履歴： 
 * -----------------------------------------------------------------------
 */
package jp.co.seino.sis.prototype.common;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * スリムス数字編集クラス.
 * @author user
 */
public class ApplDecimalFormat extends DecimalFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for ApplDecimalFormat.
	 */
	public ApplDecimalFormat() {
		super();
	}

	/**
	 * Constructor for ApplDecimalFormat.
	 * @param pattern
	 */
	public ApplDecimalFormat(String pattern) {
		super(pattern);
	}

	/**
	 * Constructor for ApplDecimalFormat.
	 * @param pattern
	 * @param symbols
	 */
	public ApplDecimalFormat(String pattern, DecimalFormatSymbols symbols) {
		super(pattern, symbols);
	}

	/**
	 * 数字文字列をカンマ付き文字列に編集する.
	 * <div>書式エラーがでないバージョン</div>
	 * @param num 数字文字列
	 * @param pattern パターン(DecimalFormatに依存する)
	 * @return カンマつき文字列
	 */
	public static String formatNumberIgnore(String num, String pattern) {
		try {
			return formatNumber(num, pattern);
		} catch(NumberFormatException e) {
			return num;
		}
	}

	/**
	 * 数字文字列をカンマ付き文字列に編集する.
	 * @param num 数字文字列
	 * @param pattern パターン(DecimalFormatに依存する)
	 * @return カンマつき文字列
	 */
	public static String formatNumber(String num, String pattern) {
		if(num == null || num.equals("")) {
			return "";
		}
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(Double.parseDouble(num));
	}

	/**
	 * 数字をカンマ付き文字列に編集する.
	 * intのMAX値に合わせて"#,###,###,###"でformatNumber(num,pattern)を
	 * 呼び出す。
	 * @param num 数値
	 * @return カンマつき文字列
	 */
	public static String formatNumber(int num) {
		return formatNumber(num, "#,###,###,###");
	}

	/**
	 * 数字をカンマ付き文字列に編集する.
	 * @param num 数値
	 * @param pattern パターン(DecimalFormatに依存する)
	 * @return カンマつき文字列
	 */
	public static String formatNumber(int num, String pattern) {
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(num);
	}

	/**
	 * 数字をカンマ付き文字列に編集する.
	 * @param num 数値
	 * @return カンマつき文字列
	 */
	public static String formatNumber(long num) {
		return formatNumber(num, "#,###,###,###,###,###,###");
	}

	/**
	 * 数字をカンマ付き文字列に編集する.
	 * @param num 数値
	 * @param pattern パターン(DecimalFormatに依存する)
	 * @return カンマつき文字列
	 */
	public static String formatNumber(long num, String pattern) {
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(num);
	}

	/**
	 * 数字をカンマ付き文字列に編集する.
	 * @param num 数値
	 * @param pattern パターン(DecimalFormatに依存する)
	 * @return カンマつき文字列
	 */
	public static String formatNumber(double num, String pattern) {
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(num);
	}

	/**
	 * カンマ付き文字列を数字に変換する.
	 * <div>書式エラーがでないバージョン</div>
	 * @param num カンマ付き文字列
	 * @param pattern (DecimalFormatに依存する)
	 * @return 数字文字列
	 */
	public static String parseNumberIgnore(String num, String pattern) {
		try {
			return parseNumber(num, pattern);
		} catch(ParseException e) {
			return num;
		}
	}

	/**
	 * カンマ付き文字列を数字に変換する.
	 * 
	 * ＜patternに小数点以下のフォーマットを指定する場合＞
	 * 　小数点以下のフォーマットを指定すると、指定した桁数への丸めが発生する.
	 * 　丸めは四捨五入ではなく、五捨六入になる.(DecimalFormatクラスの標準仕様)
	 * 　例1)pattern=#,###.## num=1,000.005 → 1000
	 * 　例2)pattern=#,###.## num=1,000.006 → 1000.01
	 * 
	 * ＜patternに小数点以下のフォーマットを指定しない場合＞
	 * 　整数部分だけのフォーマットを指定しても、小数点部分の丸めは発生しない.
	 *   ※小数点以下の桁数を意識したくない場合、こちらの形式を利用してください。
	 * 　例1)pattern=#,### num=1,000.005 → 1000.005
	 * 　例2)pattern=#,### num=1,000.006 → 1000.006
	 * 
	 * @param num カンマ付き文字列
	 * @param pattern (DecimalFormatに依存する)
	 * @return 数字文字列
	 * @throws ParseException 書式エラー
	 */
	public static String parseNumber(String num, String pattern) throws ParseException {
		if(num == null || num.equals("")) {
			return "0";// Long.parseLong()などで例外が発生しないために0を返すように修正 2006.7.5 T.Ishii
			//return "";
		}
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		DecimalFormat instTo = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
			String toPattern = BeanUtil.removeCharInc(pattern, "#0.-");
			instTo.applyPattern(toPattern);
		}
		if(pattern.indexOf('.') == -1) {
			inst.setParseIntegerOnly(true);
		}
		
        //2008.04.30 T07-425 STR:ADD -0.X変換の対応
		//マイナスかどうかを判定しておく.
		//変換先フォーマットが整数のみの場合、DecimalFormatのparseは整数部分のみを対象に
		//変換を行うため-0.Xのような値だと、変換結果は"0"になり
		//"-"が除去されてしまうため
		boolean isMinus = false;
		if (num.startsWith("-")){
			isMinus = true;
		}
        //2008.04.30 T07-425 STR:END
		
		ParsePosition pos = new ParsePosition(0);
		Number number = inst.parse(num, pos);
		StringBuffer buf = new StringBuffer();
		if(number != null) {
			// Longと解釈できないのがDoubleになる仕様を考慮
			if(number instanceof Double) {
				instTo.format(number.doubleValue(), buf, new FieldPosition(0));
			} else {
				instTo.format(number.longValue(), buf, new FieldPosition(0));
			}
		}
		//変換対象外になった文字列を追加する
		//（整数のみフォーマットで小数点ありの値を変換した際は、
		//　小数点部分は変換対象外なため、ここで結合される）
		buf.append(num.substring(pos.getIndex()));

        //2008.04.30 T07-425 STR:ADD -0.X変換の対応
		//変換結果が0かつ小数点付きの場合は、元々がマイナスだったらマイナスをつける.
		if (buf.toString().startsWith("0.") && isMinus){
			//-0.01を###などの整数部のみのパターンで処理した場合、
			//DecimalFormatのparseでは、整数部のみの結果が返るので、
			//[-0]は、[0]となり、符号がとれます。※数値型にセットしているため。
			//小数部を結合する部分では、解析した(-0)あとの文字列部分.001を結合するので、
			//ここでも、符号が考慮されていません。
			//よって、ここでは、マイナスの値の場合に、マイナス符号を結合するようにしています。
			//但し、単純に-0の場合には、マイナス符号をつけたくないので、
			//小数点以下がある場合のみ結合します。
			buf.insert(0, "-");
		}
        //2008.04.30 T07-425 STR:END
		return buf.toString();
	}
}
