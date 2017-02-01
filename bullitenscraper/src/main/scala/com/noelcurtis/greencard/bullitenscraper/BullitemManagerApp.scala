package com.noelcurtis.greencard.bullitenscraper

object BullitemManagerApp extends App {

  implicit val jsoupManager: JsoupManager = new WrappingJsoupManager()
  val latestBulliten = new BullitenManager().fetchLatestBulliten()

}
