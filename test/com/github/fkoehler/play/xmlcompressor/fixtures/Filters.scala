/**
 * Play HTML Compressor
 *
 * LICENSE
 *
 * This source file is subject to the new BSD license that is bundled
 * with this package in the file LICENSE.md.
 * It is also available through the world-wide-web at this URL:
 * https://github.com/fkoehler/play-html-compressor/blob/master/LICENSE.md
 */
package com.github.fkoehler.play.xmlcompressor.fixtures

import akka.stream.Materializer
import com.github.fkoehler.play.xmlcompressor.XMLCompressorFilter
import com.googlecode.htmlcompressor.compressor.XmlCompressor
import javax.inject.Inject
import play.api.Configuration
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.gzip.GzipFilter

import scala.concurrent.ExecutionContext

/**
 * A custom XML compressor filter.
 */
class CustomXMLCompressorFilter @Inject() (val configuration: Configuration, val mat: Materializer) extends XMLCompressorFilter {
  override val compressor = {
    val c = new XmlCompressor()
    c.setRemoveComments(false)
    c
  }

  override implicit val executionContext: ExecutionContext =
    ExecutionContext.global
}

/**
 * Provides the default XML compressor filter.
 */
class DefaultFilter @Inject() (xmlCompressorFilter: XMLCompressorFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(xmlCompressorFilter)
}

/**
 * Provides the default XML compressor filter with a Gzip filter.
 */
class WithGzipFilter @Inject() (xmlCompressorFilter: XMLCompressorFilter, gzipFilter: GzipFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(gzipFilter, xmlCompressorFilter)
}
