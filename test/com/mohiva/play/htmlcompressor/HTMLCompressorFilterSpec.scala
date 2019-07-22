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
package com.mohiva.play.htmlcompressor

import akka.util.ByteString
import com.mohiva.play.compressor.Helper
import com.mohiva.play.htmlcompressor.fixtures.{ CustomHTMLCompressorFilter, DefaultFilter, RequestHandler, WithGzipFilter }
import org.apache.commons.io.IOUtils
import org.specs2.mutable._
import org.specs2.specification.Scope
import play.api.Environment
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._

/**
 * Test case for the [[com.mohiva.play.htmlcompressor.HTMLCompressorFilter]] class.
 */
class HTMLCompressorFilterSpec extends Specification {
  val environment = Environment.simple()

  "The default filter" should {
    "compress an HTML page" in new Context {
      new WithApplication(defaultApp) {
        val result = route(defaultApp, FakeRequest(GET, "/action")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/html")
        contentAsString(result) must startWith("<!DOCTYPE html> <html> <head>")
      }
    }

    "compress an async HTML page" in new Context {
      new WithApplication(defaultApp) {
        val result = route(defaultApp, FakeRequest(GET, "/asyncAction")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/html")
        contentAsString(result) must startWith("<!DOCTYPE html> <html> <head>")
      }
    }

    "not compress a non HTML result" in new Context {
      new WithApplication(defaultApp) {
        val result = route(defaultApp, FakeRequest(GET, "/nonHTML")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/plain")
        contentAsString(result) must startWith("  <html/>")
      }
    }

    "compress static assets" in new Context {
      new WithApplication(defaultApp) {
        val file = scala.io.Source.fromInputStream(environment.resourceAsStream("static.html").get).mkString
        val result = route(defaultApp, FakeRequest(GET, "/static")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/html")
        contentAsString(result) must startWith("<!DOCTYPE html> <html> <head>")
        header(CONTENT_LENGTH, result) must not beSome file.length.toString
      }
    }

    "not compress result with chunked HTML result" in new Context {
      new WithApplication(defaultApp) {
        val result = route(defaultApp, FakeRequest(GET, "/chunked")).get
        status(result) must beEqualTo(OK)
        contentType(result) must beSome("text/html")
        header(CONTENT_LENGTH, result) must beNone
      }
    }
  }

  "The custom filter" should {
    "compress an HTML page" in new Context {
      new WithApplication(customApp) {
        val result = route(customApp, FakeRequest(GET, "/action")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/html")
        contentAsString(result) must startWith("<!DOCTYPE html><html><head>")
      }
    }

    "compress an async HTML page" in new Context {
      new WithApplication(customApp) {
        val result = route(customApp, FakeRequest(GET, "/asyncAction")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/html")
        contentAsString(result) must startWith("<!DOCTYPE html><html><head>")
      }
    }

    "not compress a non HTML result" in new Context {
      new WithApplication(customApp) {
        val result = route(customApp, FakeRequest(GET, "/nonHTML")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/plain")
        contentAsString(result) must startWith("  <html/>")
      }
    }

    "compress static assets" in new Context {
      new WithApplication(customApp) {
        val file = scala.io.Source.fromInputStream(environment.resourceAsStream("static.html").get).mkString
        val result = route(customApp, FakeRequest(GET, "/static")).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("text/html")
        contentAsString(result) must startWith("<!DOCTYPE html><html><head>")
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
        contentType(gzipped) must beSome("text/html")
        header(CONTENT_ENCODING, gzipped) must beSome("gzip")
        Helper.gunzip(contentAsBytes(gzipped)) must_== contentAsBytes(original)
      }
    }

    "not compress already gzipped result" in new Context {
      new WithApplication(gzipApp) {
        // given static.html.gz == gzip(static.html)
        // when /static.html is requested
        // then Assets controller responds with static.html.gz
        // we don't want to further pass this through HTML Compressor

        val original = ByteString(IOUtils.toByteArray(environment.resourceAsStream("static.html").get))
        val result = route(gzipApp, FakeRequest(GET, "/gzipped").withHeaders(ACCEPT_ENCODING -> "gzip")).get

        status(result) must beEqualTo(OK)
        contentType(result) must beSome("text/html")
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
     * An app with the default HTML compressor filter.
     */
    val defaultApp = new GuiceApplicationBuilder()
      .configure("play.http.filters" -> classOf[DefaultFilter].getCanonicalName)
      .configure("play.http.requestHandler" -> classOf[RequestHandler].getCanonicalName)
      .build()

    /**
     * An app with the custom HTML compressor filter.
     */
    val customApp = new GuiceApplicationBuilder()
      .configure("play.http.filters" -> classOf[DefaultFilter].getCanonicalName)
      .overrides(bind[HTMLCompressorFilter].to[CustomHTMLCompressorFilter])
      .configure("play.http.requestHandler" -> classOf[RequestHandler].getCanonicalName)
      .build()

    /**
     * An app with the gzip filter in place.
     */
    val gzipApp = new GuiceApplicationBuilder()
      .configure("play.http.filters" -> classOf[WithGzipFilter].getCanonicalName)
      .configure("play.http.requestHandler" -> classOf[RequestHandler].getCanonicalName)
      .build()
  }
}
