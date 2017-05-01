package com.github.cuzfrog.utils

/**
  * Created by cuz on 12/7/16.
  */
private[cuzfrog] object Hasher {
  private val md5er = java.security.MessageDigest.getInstance("MD5")
  def hash(pw: String): String = {
    val salt = new String(md5er.digest(pw.take(2).getBytes("utf8")), "utf8")
    new String(md5er.digest(pw.getBytes("utf8")), "utf8") + salt
  }

  def newHash(pw: String): String = {
    val hashedWithSalt = md5(pw.take(2)) + md5(pw)
    hashedWithSalt.take(8) + hashedWithSalt.takeRight(8)
  }

  private def md5(s: String): String =
    md5er.digest(s.getBytes).map(0xFF & _).map("%02x".format(_)).reduce(_ + _)

}
