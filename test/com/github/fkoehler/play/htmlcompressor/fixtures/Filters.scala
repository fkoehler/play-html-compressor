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
package com.github.fkoehler.play.htmlcompressor.fixtures

import javax.inject.Inject

import akka.stream.Materializer
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import com.github.fkoehler.play.htmlcompressor.HTMLCompressorFilter
import play.api.{ Environment, Mode, Configuration }
import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.gzip.GzipFilter

import scala.concurrent.ExecutionContext

/**
 * A custom HTML compressor filter.
 */
class CustomHTMLCompressorFilter @Inject() (val configuration: Configuration, environment: Environment, val mat: Materializer)
  extends HTMLCompressorFilter {

  override val compressor: HtmlCompressor = {
    val c = new HtmlCompressor()
    if (environment.mode == Mode.Dev) {
      c.setPreserveLineBreaks(true)
    }

    c.setRemoveComments(true)
    c.setRemoveIntertagSpaces(true)
    c.setRemoveHttpProtocol(true)
    c.setRemoveHttpsProtocol(true)
    c
  }

  override implicit val executionContext: ExecutionContext =
    ExecutionContext.global
}

/**
 * Provides the default HTML compressor filter.
 */
class DefaultFilter @Inject() (htmlCompressorFilter: HTMLCompressorFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(htmlCompressorFilter)
}

/**
 * Provides the default HTML compressor filter with a Gzip filter.
 */
class WithGzipFilter @Inject() (htmlCompressorFilter: HTMLCompressorFilter, gzipFilter: GzipFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(gzipFilter, htmlCompressorFilter)
}
