package com.noelcurtis.greencard.bullitenscraper

import java.io.ByteArrayInputStream
import java.util.Base64

import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}
import com.typesafe.config.Config
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.commons.codec.digest.DigestUtils
import play.api.libs.json.Json

trait BulletinPersistenceService {

  def insertBulletin(bulletin: Bulletin): String

}

class S3BulletinPersistenceService(config: Config) extends BulletinPersistenceService with LazyLogging {

  val s3Bucket: String = config.getString("app.outputS3Bucket")
  val s3KeyPrefix: String = config.getString("app.outputS3KeyPrefix")
  val cacheControlHeader: String = config.getString("app.outputS3ObjectCacheControlHeader")
  lazy val amazonS3: AmazonS3 = new AmazonS3Client()

  override def insertBulletin(bulletin: Bulletin): String = {
    val bulletinAsString = Json.toJson(bulletin).toString()
    val asBytes = bulletinAsString.getBytes
    val key = s"$s3KeyPrefix-${bulletin.date}.json"

    logger.info("Inserting bulletin at S3 key [{}]", key)
    amazonS3.putObject(s3Bucket, key, new ByteArrayInputStream(asBytes), getObjectMetadata(asBytes))
    key
  }

  def getObjectMetadata(data: Array[Byte]): ObjectMetadata = {
    val metadata = new ObjectMetadata()
    metadata.setContentLength(data.length)
    metadata.setContentEncoding("UTF-8")
    metadata.setContentType("application/json")
    metadata.setCacheControl(cacheControlHeader)
    try {
      val resultByte = DigestUtils.md5(data)
      val streamMD5 = new String(Base64.getEncoder.encode(resultByte), "UTF-8")
      metadata.setContentMD5(streamMD5)
    } catch {
      case e: Exception => logger.warn("Could not set md5 for S3 metadata", e)
    }
    metadata
  }

}
