package net.mentalarray.doozie.DBStore

/**
 * Created by bgilcrease on 11/21/14.
 * http://stackoverflow.com/questions/1132567/encrypt-password-in-configuration-files-java
 */

import java.io.{IOException, UnsupportedEncodingException}
import java.security.GeneralSecurityException
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}
import javax.crypto.spec.{PBEKeySpec, PBEParameterSpec}

import sun.misc.{BASE64Decoder, BASE64Encoder}

class EncryptDecrypt(password: String) {

  private val _password = password

  private val SALT: Array[Byte] = Array(0xde.toByte, 0x33.toByte, 0x10.toByte, 0x12.toByte, 0xde.toByte, 0x33.toByte, 0x10.toByte, 0x12.toByte)

  @throws(classOf[GeneralSecurityException])
  @throws(classOf[UnsupportedEncodingException])
  def encrypt(property: String): String = {
    val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
    val key: SecretKey = keyFactory.generateSecret(new PBEKeySpec(_password.toCharArray))
    val pbeCipher: Cipher = Cipher.getInstance("PBEWithMD5AndDES")
    pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20))
    return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")))
  }

  private def base64Encode(bytes: Array[Byte]): String = {
    return new BASE64Encoder().encode(bytes)
  }

  @throws(classOf[GeneralSecurityException])
  @throws(classOf[IOException])
  def decrypt(property: String): String = {
    val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
    val key: SecretKey = keyFactory.generateSecret(new PBEKeySpec(_password.toCharArray))
    val pbeCipher: Cipher = Cipher.getInstance("PBEWithMD5AndDES")
    pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20))
    return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8")
  }

  @throws(classOf[IOException])
  private def base64Decode(property: String): Array[Byte] = {
    return new BASE64Decoder().decodeBuffer(property)
  }
}

object EncryptDecrypt {
  def apply() = {
    new EncryptDecrypt("dataflow")
  }
}
