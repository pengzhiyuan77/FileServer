package com.yealink.ims.fileshare.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * AES工具类 author:pengzhiyuan Created on:2016/6/4.
 */
public class AESUtil {

	private static final String KEY_ALGORITHM = "AES";
    // 默认AES/ECB/PKCS5Padding
	private static final String CIPHER_ALGORITHM_ECB_ZEROPADDING = "AES/ECB/ZeroBytePadding";
    private static final String CIPHER_ALGORITHM_ECB_NOPADDING = "AES/ECB/NoPadding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

	/**
	 * 初始化 AES 加密密码器
	 * 采用ECB模式 PaddingMode.Zeros填充方式
     * @param sKey --- base64编码的密钥
	 * @return
	 */
	public Cipher initAESEnCipherZeroPadding(String sKey) {
        Cipher enCipher = null;
		try {
			byte[] bytes = Base64.decodeBase64(sKey);
			SecretKey key = new SecretKeySpec(bytes, KEY_ALGORITHM);
			enCipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB_ZEROPADDING, "BC");
			// 初始化
			enCipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(); 
		} catch (NoSuchPaddingException e) {
			e.printStackTrace(); 
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return enCipher;
    }
	
	/**
	 * 初始化 AES 解密密码器
	 * 采用ECB模式 PaddingMode.Zeros填充方式
     * =======目前这种解密方式有点问题========== 文件共享采用nopadding解密后截取
	 * @return
	 */
	public Cipher initAESDeCipherZeroPadding(String sKey) {
        Cipher deCipher = null;
		try {
			byte[] bytes = Base64.decodeBase64(sKey);
			SecretKey key = new SecretKeySpec(bytes, KEY_ALGORITHM);
			deCipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB_ZEROPADDING, "BC");
			// 初始化
			deCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(); 
		} catch (NoSuchPaddingException e) {
			e.printStackTrace(); 
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return deCipher;
    }

    /**
     * 采用nopadding解密
     * 文件共享解密用
     * @param sKey -- 密钥 base64编码
     */
    public Cipher initAESDeCipherNopadding(String sKey) {
        Cipher deCipher = null;
        try {
            byte[] bytes = Base64.decodeBase64(sKey);
            SecretKey key = new SecretKeySpec(bytes, KEY_ALGORITHM);
            deCipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB_NOPADDING);
            // 初始化
            deCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return deCipher;
    }

	/**
	 * 采用AES加密
     * @param enCipher
	 * @param content 待加密内容
	 * @return
	 */
	public byte[] encrypt(Cipher enCipher, byte[] content) {
		byte[] encryptData = null;
		if (enCipher != null) {
			try {
				encryptData = enCipher.doFinal(content);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
		}
		return encryptData;
	}
	
	/**
	 * 采用AES解密
     * @param deCipher
	 * @param content -- 待解密
	 * @return
	 */
	public byte[] decrypt(Cipher deCipher, byte[] content) {
		byte[] origntData = null;
		if (deCipher != null) {
			try {
				origntData = deCipher.doFinal(content);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
		}
		return origntData;
	}

	
	public static void main(String[]ar) {
		AESUtil util = new AESUtil();
		String key = "OgzRHs1Xh65njC99gpE0Zg==";
		Cipher deCipher = util.initAESDeCipherZeroPadding(key);
        Cipher enCipher = util.initAESEnCipherZeroPadding(key);

//		byte[] httpCodeArr = ByteUtil.intToBytes2(123123213);
//		byte[] enda = util.encrypt(httpCodeArr);

		String content="he 号，阿斯蒂芬asdfasdfasfasdfasfdasdfasfd241zd！";
		byte[] enda = util.encrypt(enCipher, content.getBytes());
//		System.out.println(new String(enda));
		byte[] deda = util.decrypt(deCipher, enda);
		System.out.println(new String(deda));
	}
}
