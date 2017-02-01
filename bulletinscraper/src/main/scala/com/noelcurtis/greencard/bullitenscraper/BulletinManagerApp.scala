package com.noelcurtis.greencard.bullitenscraper

import com.amazonaws.services.lambda.runtime.Context
import com.typesafe.config.{Config, ConfigFactory}

class BulletinManagerApp {
  implicit val jsoupManager: JsoupManager = new WrappingJsoupManager()

  val config: Config = ConfigFactory.load()
  val bulletinManager: BulletinScraper = new JsoupBulletinScraper(config)
  val bulletinPersistenceService: BulletinPersistenceService = new S3BulletinPersistenceService(config)

  def handler(name: String, context: Context): String = {
    bulletinPersistenceService.insertBulletin(bulletinManager.fetchLatestBulliten())
  }
}
