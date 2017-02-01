package com.noelcurtis.greencard.bullitenscraper

import com.amazonaws.services.lambda.runtime.Context

class BulletinManagerApp {
  implicit val jsoupManager: JsoupManager = new WrappingJsoupManager()
  val bulletinManager: BulletinScraper = new JsoupBulletinScraper()
  val bulletinPersistenceService: BulletinPersistenceService = new S3BulletinPersistenceService()

  def handler(name: String, context: Context): String = {
    bulletinPersistenceService.insertBulletin(bulletinManager.fetchLatestBulliten())
  }
}
