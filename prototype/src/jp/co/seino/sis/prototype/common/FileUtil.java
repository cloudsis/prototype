/*
 * -----------------------------------------------------------------------
 * @(#)CSVFileReader.java
 *
 * Copyright 2004 Seino Information Service Co.,Ltd. All rights reserved.
 *
 * -----------------------------------------------------------------------
 * システム名　　：SIS基幹システム
 * サブシステム名：共通
 * プログラム名　：ファイルユーティリティ
 * -----------------------------------------------------------------------
 * クラス名： FileUtil
 * 作成期日：2006.02.22 T05-402 Tetsuji Ishii 新規作成
 * 修正履歴：2004.99.99 XXX-XXX 名前       修正内容
 * 修正履歴：
 * -----------------------------------------------------------------------
 */
package jp.co.seino.sis.prototype.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * @author Tetsuji Ishii
 */
public class FileUtil {
	
	public static final String FILE_ENCODING = "UTF-8";
	public static final String LINE_SEPARATOR = "\r\n";

	/**
	 * テキストファイルをソートする
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sort(File file) throws FileNotFoundException, IOException{
		sort(file,FILE_ENCODING,null);
	}

	/**
	 * テキストファイルをソートする
	 * @param file
	 * @param enc
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sort(File file,String enc) throws FileNotFoundException, IOException{
		sort(file,enc,null);
	}

	/**
	 * テキストファイルをソートする
	 * @param file
	 * @param enc 文字エンコーディング
	 * @param comparator
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sort(File file, String enc, Comparator<String> comparator) throws FileNotFoundException, IOException{
		List<String> list = read(file,enc);
		if (comparator != null) {
			Collections.sort(list,comparator);
		} else {
			Collections.sort(list);
		}
		write(file,list,enc);
	}

	/**
	 * ファイルをリストに読み込む
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<String> read(File file) throws FileNotFoundException, IOException{
		return read(new InputStreamReader(new FileInputStream(file),FILE_ENCODING));
	}

	/**
	 * ファイルをリストに読み込む
	 * @param file
	 * @param enc 文字エンコーディング
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<String> read(File file,String enc) throws IOException{
		return read(new InputStreamReader(new FileInputStream(file),enc));
	}

	/**
	 * ファイルをリストに読み込む
	 */
	public static List<String> read(Reader in) throws IOException{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(in);
			String line;
			List<String> list = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
			return list;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * リストをファイルに書き出す
	 */
	public static void write(File file, List<String> list) throws IOException{
		write(file,list,FILE_ENCODING);
	}
	/**
	 * リストをファイルに書き出す
	 */
	public static void write(File file, List<String> list, String enc) throws IOException{
		write(new OutputStreamWriter(new FileOutputStream(file),enc), list);
	}

	/**
	 * リストをファイルに書き出す
	 */
	public static void write(Writer out, List<String> list) throws FileNotFoundException, IOException{
		String lineseparator = LINE_SEPARATOR;
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(out);
			for (String line : list) {
				writer.write(line, 0, line.length());
				writer.write(lineseparator);
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * ファイルを読み込み出力する
	 */
	public static void readWrite(InputStream in, OutputStream out) throws FileNotFoundException, IOException{
		//ファイルの出力
		byte[] buf = new byte[4096];
		int bufsize = 0;
		try {
			while ( (bufsize = in.read(buf,0,4096)) != -1) {
				out.write( buf, 0, bufsize);
			}
		} finally {
			if (out != null) out.close();
			if (in != null) in.close();
		}
	}

	/**
	 * ファイルを読み込み出力する
	 * 文字コードの変換をする
	 * @param in 読み込みファイル
	 * @param out 出力ファイル
	 * @param inEnc 読み込み文字コード
	 * @param outEnc 出力文字コード
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void readWriteEncoding(InputStream in, OutputStream out, String inEnc, String outEnc) throws FileNotFoundException, IOException{
		write(new OutputStreamWriter(out,outEnc), read(new InputStreamReader(in,inEnc)));
	}

    /**
     * gzipファイルを作成します
     * @param file 出力するgzipファイル
     * @param data 出力するデータ文字列
     * @throws IOException 入出力例外が発生した場合
     */
    public static void gzip(File file, String data) throws IOException {
        final GZIPOutputStream gzipOutStream =new GZIPOutputStream(
             new BufferedOutputStream(new FileOutputStream(file)));
        try {
            /* ファイルデータの書き込み */
            gzipOutStream.write(data.getBytes());
            gzipOutStream.finish();
        } finally {
            gzipOutStream.close();
        }
    }
}
