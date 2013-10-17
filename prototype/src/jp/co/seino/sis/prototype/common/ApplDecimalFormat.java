/*
 * -----------------------------------------------------------------------
 * @(#)ApplDecimalFormat.java
 *
 * Copyright 2003 Seino Information Service Co.,Ltd. All rights reserved.
 *
 * -----------------------------------------------------------------------
 * �V�X�e�����@�@�F�r�k�h�l�r�|�v�g
 * �T�u�V�X�e�����F����
 * �v���O�������@�F�X�����X�����ҏW�N���X
 * -----------------------------------------------------------------------
 * �N���X���F ApplDecimalFormat
 * �쐬�����F2003.12.12 T03-129 Y.Kanamori �V�K�쐬
 * �C�������F2008.04.30 T07-425 E.Yokoi parseNumber���\�b�h��-0.X�ϊ��Ή�
 * �C�������F2003.99.99 XXX-XXX ���O       �C�����e
 * �C�������F 
 * -----------------------------------------------------------------------
 */
package jp.co.seino.sis.prototype.common;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * �X�����X�����ҏW�N���X.
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
	 * ������������J���}�t��������ɕҏW����.
	 * <div>�����G���[���łȂ��o�[�W����</div>
	 * @param num ����������
	 * @param pattern �p�^�[��(DecimalFormat�Ɉˑ�����)
	 * @return �J���}��������
	 */
	public static String formatNumberIgnore(String num, String pattern) {
		try {
			return formatNumber(num, pattern);
		} catch(NumberFormatException e) {
			return num;
		}
	}

	/**
	 * ������������J���}�t��������ɕҏW����.
	 * @param num ����������
	 * @param pattern �p�^�[��(DecimalFormat�Ɉˑ�����)
	 * @return �J���}��������
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
	 * �������J���}�t��������ɕҏW����.
	 * int��MAX�l�ɍ��킹��"#,###,###,###"��formatNumber(num,pattern)��
	 * �Ăяo���B
	 * @param num ���l
	 * @return �J���}��������
	 */
	public static String formatNumber(int num) {
		return formatNumber(num, "#,###,###,###");
	}

	/**
	 * �������J���}�t��������ɕҏW����.
	 * @param num ���l
	 * @param pattern �p�^�[��(DecimalFormat�Ɉˑ�����)
	 * @return �J���}��������
	 */
	public static String formatNumber(int num, String pattern) {
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(num);
	}

	/**
	 * �������J���}�t��������ɕҏW����.
	 * @param num ���l
	 * @return �J���}��������
	 */
	public static String formatNumber(long num) {
		return formatNumber(num, "#,###,###,###,###,###,###");
	}

	/**
	 * �������J���}�t��������ɕҏW����.
	 * @param num ���l
	 * @param pattern �p�^�[��(DecimalFormat�Ɉˑ�����)
	 * @return �J���}��������
	 */
	public static String formatNumber(long num, String pattern) {
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(num);
	}

	/**
	 * �������J���}�t��������ɕҏW����.
	 * @param num ���l
	 * @param pattern �p�^�[��(DecimalFormat�Ɉˑ�����)
	 * @return �J���}��������
	 */
	public static String formatNumber(double num, String pattern) {
		DecimalFormat inst = (DecimalFormat)DecimalFormat.getInstance();
		if(pattern != null && !pattern.equals("")) {
			inst.applyPattern(pattern);
		}
		return inst.format(num);
	}

	/**
	 * �J���}�t��������𐔎��ɕϊ�����.
	 * <div>�����G���[���łȂ��o�[�W����</div>
	 * @param num �J���}�t��������
	 * @param pattern (DecimalFormat�Ɉˑ�����)
	 * @return ����������
	 */
	public static String parseNumberIgnore(String num, String pattern) {
		try {
			return parseNumber(num, pattern);
		} catch(ParseException e) {
			return num;
		}
	}

	/**
	 * �J���}�t��������𐔎��ɕϊ�����.
	 * 
	 * ��pattern�ɏ����_�ȉ��̃t�H�[�}�b�g���w�肷��ꍇ��
	 * �@�����_�ȉ��̃t�H�[�}�b�g���w�肷��ƁA�w�肵�������ւ̊ۂ߂���������.
	 * �@�ۂ߂͎l�̌ܓ��ł͂Ȃ��A�܎̘Z���ɂȂ�.(DecimalFormat�N���X�̕W���d�l)
	 * �@��1)pattern=#,###.## num=1,000.005 �� 1000
	 * �@��2)pattern=#,###.## num=1,000.006 �� 1000.01
	 * 
	 * ��pattern�ɏ����_�ȉ��̃t�H�[�}�b�g���w�肵�Ȃ��ꍇ��
	 * �@�������������̃t�H�[�}�b�g���w�肵�Ă��A�����_�����̊ۂ߂͔������Ȃ�.
	 *   �������_�ȉ��̌������ӎ��������Ȃ��ꍇ�A������̌`���𗘗p���Ă��������B
	 * �@��1)pattern=#,### num=1,000.005 �� 1000.005
	 * �@��2)pattern=#,### num=1,000.006 �� 1000.006
	 * 
	 * @param num �J���}�t��������
	 * @param pattern (DecimalFormat�Ɉˑ�����)
	 * @return ����������
	 * @throws ParseException �����G���[
	 */
	public static String parseNumber(String num, String pattern) throws ParseException {
		if(num == null || num.equals("")) {
			return "0";// Long.parseLong()�Ȃǂŗ�O���������Ȃ����߂�0��Ԃ��悤�ɏC�� 2006.7.5 T.Ishii
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
		
        //2008.04.30 T07-425 STR:ADD -0.X�ϊ��̑Ή�
		//�}�C�i�X���ǂ����𔻒肵�Ă���.
		//�ϊ���t�H�[�}�b�g�������݂̂̏ꍇ�ADecimalFormat��parse�͐��������݂̂�Ώۂ�
		//�ϊ����s������-0.X�̂悤�Ȓl���ƁA�ϊ����ʂ�"0"�ɂȂ�
		//"-"����������Ă��܂�����
		boolean isMinus = false;
		if (num.startsWith("-")){
			isMinus = true;
		}
        //2008.04.30 T07-425 STR:END
		
		ParsePosition pos = new ParsePosition(0);
		Number number = inst.parse(num, pos);
		StringBuffer buf = new StringBuffer();
		if(number != null) {
			// Long�Ɖ��߂ł��Ȃ��̂�Double�ɂȂ�d�l���l��
			if(number instanceof Double) {
				instTo.format(number.doubleValue(), buf, new FieldPosition(0));
			} else {
				instTo.format(number.longValue(), buf, new FieldPosition(0));
			}
		}
		//�ϊ��ΏۊO�ɂȂ����������ǉ�����
		//�i�����̂݃t�H�[�}�b�g�ŏ����_����̒l��ϊ������ۂ́A
		//�@�����_�����͕ϊ��ΏۊO�Ȃ��߁A�����Ō��������j
		buf.append(num.substring(pos.getIndex()));

        //2008.04.30 T07-425 STR:ADD -0.X�ϊ��̑Ή�
		//�ϊ����ʂ�0�������_�t���̏ꍇ�́A���X���}�C�i�X��������}�C�i�X������.
		if (buf.toString().startsWith("0.") && isMinus){
			//-0.01��###�Ȃǂ̐������݂̂̃p�^�[���ŏ��������ꍇ�A
			//DecimalFormat��parse�ł́A�������݂̂̌��ʂ��Ԃ�̂ŁA
			//[-0]�́A[0]�ƂȂ�A�������Ƃ�܂��B�����l�^�ɃZ�b�g���Ă��邽�߁B
			//���������������镔���ł́A��͂���(-0)���Ƃ̕����񕔕�.001����������̂ŁA
			//�����ł��A�������l������Ă��܂���B
			//����āA�����ł́A�}�C�i�X�̒l�̏ꍇ�ɁA�}�C�i�X��������������悤�ɂ��Ă��܂��B
			//�A���A�P����-0�̏ꍇ�ɂ́A�}�C�i�X�������������Ȃ��̂ŁA
			//�����_�ȉ�������ꍇ�̂݌������܂��B
			buf.insert(0, "-");
		}
        //2008.04.30 T07-425 STR:END
		return buf.toString();
	}
}
