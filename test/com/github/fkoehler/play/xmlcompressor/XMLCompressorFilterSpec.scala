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
package com.github.fkoehler.play.xmlcompressor

import akka.util.ByteString
import com.github.fkoehler.play.compressor.Helper
import com.github.fkoehler.play.xmlcompressor.fixtures.{ CustomXMLCompressorFilter, DefaultFilter, RequestHandler, WithGzipFilter }
import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.specification.Scope
import play.api.Environment
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._

/**
 * Test case for the [[XMLCompressorFilter]] class.
 */
class XMLCompressorFilterSpec extends Specification {
  val environment = Environment.simple()

  "The default filter" should {
    "compress an XML document" in new Context {
      new WithApplication(defaultApp) {
        val result = route(gzipApp, FakeRequest(GET, "/action")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        contentAsString(result) must startWith("<?xml version=\"1.0\"?><node><subnode>")
      }
    }

    "compress an async XML document" in new Context {
      new WithApplication(defaultApp) {
        val result = route(gzipApp, FakeRequest(GET, "/asyncAction")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        contentAsString(result) must startWith("<?xml version=\"1.0\"?><node><subnode>")
      }
    }

    "not compress a non XML result" in new Context {
      new WithApplication(defaultApp) {
        val result = route(gzipApp, FakeRequest(GET, "/nonXML")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/plain")
        contentAsString(result) must startWith("  <html/>")
      }
    }

    "not compress chunked XML result" in new Context {
      new WithApplication(defaultApp) {
        val result = route(gzipApp, FakeRequest(GET, "/chunked")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        header(CONTENT_LENGTH, result) must beNone
      }
    }

    "compress static XML assets" in new Context {
      new WithApplication(defaultApp) {
        val file = scala.io.Source.fromInputStream(environment.resourceAsStream("static.xml").get).mkString
        val result = route(gzipApp, FakeRequest(GET, "/static")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        contentAsString(result) must startWith("<?xml version=\"1.0\"?><node><subnode>")
        header(CONTENT_LENGTH, result) must not beSome file.length.toString
      }
    }
  }

  "The custom filter" should {
    "compress an XML document" in new Context {
      new WithApplication(customApp) {
        val result = route(gzipApp, FakeRequest(GET, "/action")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        contentAsString(result) must startWith("<?xml version=\"1.0\"?><node><subnode>")
      }
    }

    "compress an async XML document" in new Context {
      new WithApplication(customApp) {
        val result = route(gzipApp, FakeRequest(GET, "/asyncAction")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        contentAsString(result) must startWith("<?xml version=\"1.0\"?><node><subnode>")
      }
    }

    "not compress a non XML result" in new Context {
      new WithApplication(customApp) {
        val result = route(gzipApp, FakeRequest(GET, "/nonXML")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/plain")
        contentAsString(result) must startWith("  <html/>")
      }
    }

    "compress static XML assets" in new Context {
      new WithApplication(customApp) {
        val file = scala.io.Source.fromInputStream(environment.resourceAsStream("static.xml").get).mkString
        val result = route(gzipApp, FakeRequest(GET, "/static")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/xml")
        contentAsString(result) must startWith("<?xml version=\"1.0\"?><node><subnode>")
        header(CONTENT_LENGTH, result) must not beSome file.length.toString
      }
    }
  }

  "The default filter with Gzip Filter" should {
    "first compress then gzip result" in new Context {
      new WithApplication(gzipApp) {
        val original = route(gzipApp, FakeRequest(GET, "/action")).get
        val gzipped = route(gzipApp, FakeRequest(GET, "/action").withHeaders(ACCEPT_ENCODING -> "gzip")).get

        status(gzipped) must beEqualTo(OK)
        contentType(gzipped) must beSome("application/xml")
        header(CONTENT_ENCODING, gzipped) must beSome("gzip")
        Helper.gunzip(contentAsBytes(gzipped)) must_== contentAsBytes(original)
      }
    }

    "not compress already gzipped result" in new Context {
      new WithApplication(gzipApp) {
        // given static.xml.gz == gzip(static.xml)
        // when /static.xml is requested
        // then Assets controller responds with static.xml.gz
        // we don't want to further pass this through XML Compressor

        val original = ByteString(IOUtils.toByteArray(environment.resourceAsStream("static.xml").get))
        val result = route(gzipApp, FakeRequest(GET, "/gzipped").withHeaders(ACCEPT_ENCODING -> "gzip")).get

        status(result) must beEqualTo(OK)
        contentType(result) must beSome("application/xml")
        header(CONTENT_ENCODING, result) must beSome("gzip")
        Helper.gunzip(contentAsBytes(result)) must_== original
      }
    }
  }

  /**
   * The context.
   */
  trait Context extends Scope {

    /**
     * An app with the default XML compressor filter.
     */
    val defaultApp = new GuiceApplicationBuilder()
      .configure("play.http.filters" -> classOf[DefaultFilter].getCanonicalName)
      .configure("play.http.requestHandler" -> classOf[RequestHandler].getCanonicalName)
      .build()

    /**
     * An app with the custom XML compressor filter.
     */
    val customApp = new GuiceApplicationBuilder()
      .overrides(bind[XMLCompressorFilter].to[CustomXMLCompressorFilter])
      .configure("play.http.filters" -> classOf[DefaultFilter].getCanonicalName)
      .configure("play.http.requestHandler" -> classOf[RequestHandler].getCanonicalName)
      .build()

    /**
     * An app with the gzip filter in place.
     */
    val gzipApp = new GuiceApplicationBuilder()
      .in(Environment.simple())
      .configure("play.http.filters" -> classOf[WithGzipFilter].getCanonicalName)
      .configure("play.http.requestHandler" -> classOf[RequestHandler].getCanonicalName)
      .build()

  }
}
