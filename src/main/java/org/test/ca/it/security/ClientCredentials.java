package org.mcgill.ca.it.security;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
//import org.apache.commons.codec.binary.Base64;
import java.util.Base64;

public class ClientCredentials {
    
	
    static String initVector = "RandomInitVector"; // 16 bytes IV
	
	public static String encrypt(String key, String initVector, String value) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));

            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());

            //System.out.println("encrypted string: "
              //      + Base64.encodeBase64String(encrypted));

            //return Base64.encodeBase64String(encrypted);
            String s = new String(Base64.getEncoder().encode(encrypted));
            return s;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        
    	String key = "uwsnjdks!*fWdsdcdcsdcs565"; // 128 bit key
        String initVector = "RandomInitVector"; // 16 bytes IV

       System.out.println(encrypt(key, initVector, "sanjeeva"));
        System.out.println(decrypt(key, initVector, encrypt(key, initVector, "sanjeeva")));
    
    
        
        
    }
}
