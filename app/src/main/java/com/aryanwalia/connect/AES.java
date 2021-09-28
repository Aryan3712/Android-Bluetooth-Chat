package com.aryanwalia.connect;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    //adding some private variables
    private static final String SECRET_KEY = "valar_morghulis_valar_dohaeris";
    private static final String SALT = "a_man_has_no_face";

    //string encryption method
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String input_str){
        try{
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            //create SecretKeyFactory object
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            //create KeySpec object and assign with constructor
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(),SALT.getBytes(),65536,256);
            SecretKey temp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(temp.getEncoded(),"AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey,ivspec);

            return Base64.getEncoder().encodeToString(cipher.doFinal(input_str.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    // string decryption method

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decrypt(String output_str){
        try{
            // Default byte array
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            //create IVParameterSpec object and assign with constructor

            IvParameterSpec ivspec = new IvParameterSpec(iv);

            //create SecretKeyFactory object
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            //create keySpec object and assign with constructor
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(),SALT.getBytes(),65536,256);
            SecretKey temp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(temp.getEncoded(),"AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE,secretKey,ivspec);

            return new String(cipher.doFinal(Base64.getDecoder().decode(output_str)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

}
