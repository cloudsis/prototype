/*
 * -----------------------------------------------------------------------
 * @(#)CSVFileReader.java
 *
 * Copyright 2004 Seino Information Service Co.,Ltd. All rights reserved.
 *
 * -----------------------------------------------------------------------
 * �V�X�e�����@�@�FSIS��V�X�e��
 * �T�u�V�X�e�����F����
 * �v���O�������@�F�t�@�C�����[�e�B���e�B
 * -----------------------------------------------------------------------
 * �N���X���F FileUtil
 * �쐬�����F2006.02.22 T05-402 Tetsuji Ishii �V�K�쐬
 * �C�������F2004.99.99 XXX-XXX ���O       �C�����e
 * �C�������F
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
	 * �e�L�X�g�t�@�C�����\�[�g����
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sort(File file) throws FileNotFoundException, IOException{
		sort(file,FILE_ENCODING,null);
	}

	/**
	 * �e�L�X�g�t�@�C�����\�[�g����
	 * @param file
	 * @param enc
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void sort(File file,String enc) throws FileNotFoundException, IOException{
		sort(file,enc,null);
	}

	/**
	 * �e�L�X�g�t�@�C�����\�[�g����
	 * @param file
	 * @param enc �����G���R�[�f�B���O
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
	 * �t�@�C�������X�g�ɓǂݍ���
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<String> read(File file) throws FileNotFoundException, IOException{
		return read(new InputStreamReader(new FileInputStream(file),FILE_ENCODING));
	}

	/**
	 * �t�@�C�������X�g�ɓǂݍ���
	 * @param file
	 * @param enc �����G���R�[�f�B���O
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<String> read(File file,String enc) throws IOException{
		return read(new InputStreamReader(new FileInputStream(file),enc));
	}

	/**
	 * �t�@�C�������X�g�ɓǂݍ���
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
	 * ���X�g���t�@�C���ɏ����o��
	 */
	public static void write(File file, List<String> list) throws IOException{
		write(file,list,FILE_ENCODING);
	}
	/**
	 * ���X�g���t�@�C���ɏ����o��
	 */
	public static void write(File file, List<String> list, String enc) throws IOException{
		write(new OutputStreamWriter(new FileOutputStream(file),enc), list);
	}

	/**
	 * ���X�g���t�@�C���ɏ����o��
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
	 * �t�@�C����ǂݍ��ݏo�͂���
	 */
	public static void readWrite(InputStream in, OutputStream out) throws FileNotFoundException, IOException{
		//�t�@�C���̏o��
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
	 * �t�@�C����ǂݍ��ݏo�͂���
	 * �����R�[�h�̕ϊ�������
	 * @param in �ǂݍ��݃t�@�C��
	 * @param out �o�̓t�@�C��
	 * @param inEnc �ǂݍ��ݕ����R�[�h
	 * @param outEnc �o�͕����R�[�h
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void readWriteEncoding(InputStream in, OutputStream out, String inEnc, String outEnc) throws FileNotFoundException, IOException{
		write(new OutputStreamWriter(out,outEnc), read(new InputStreamReader(in,inEnc)));
	}

    /**
     * gzip�t�@�C�����쐬���܂�
     * @param file �o�͂���gzip�t�@�C��
     * @param data �o�͂���f�[�^������
     * @throws IOException ���o�͗�O�����������ꍇ
     */
    public static void gzip(File file, String data) throws IOException {
        final GZIPOutputStream gzipOutStream =new GZIPOutputStream(
             new BufferedOutputStream(new FileOutputStream(file)));
        try {
            /* �t�@�C���f�[�^�̏������� */
            gzipOutStream.write(data.getBytes());
            gzipOutStream.finish();
        } finally {
            gzipOutStream.close();
        }
    }
}
