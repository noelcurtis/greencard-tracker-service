package com.noelcurtis.greencard.bullitenscraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

trait JsoupManager {
  def getDocument(url: String): Document
}

class WrappingJsoupManager extends JsoupManager {

  override def getDocument(url: String): Document = {
    Jsoup.connect(url).get()
  }

}
