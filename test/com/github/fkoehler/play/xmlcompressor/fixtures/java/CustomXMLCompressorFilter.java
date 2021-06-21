/**
 * Play HTML Compressor
 * <p>
 * LICENSE
 * <p>
 * This source file is subject to the new BSD license that is bundled
 * with this package in the file LICENSE.md.
 * It is also available through the world-wide-web at this URL:
 * https://github.com/fkoehler/play-html-compressor/blob/master/LICENSE.md
 */
package com.github.fkoehler.play.xmlcompressor.fixtures.java;

import akka.stream.Materializer;
import com.github.fkoehler.play.xmlcompressor.XMLCompressorFilter;
import com.googlecode.htmlcompressor.compressor.XmlCompressor;
import play.api.Configuration;

import javax.inject.Inject;

import scala.concurrent.ExecutionContext;

/**
 * Custom implementation of the XML compressor filter.
 */
public class CustomXMLCompressorFilter extends XMLCompressorFilter {

    private Configuration configuration;
    private Materializer mat;
    private ExecutionContext executionContext;

    @Inject
    public CustomXMLCompressorFilter(Configuration configuration, Materializer mat, ExecutionContext executionContext) {
        this.configuration = configuration;
        this.mat = mat;
        this.executionContext = executionContext;
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public XmlCompressor compressor() {
        XmlCompressor compressor = new XmlCompressor();
        compressor.setRemoveComments(false);

        return compressor;
    }

    @Override
    public Materializer mat() {
        return mat;
    }

    @Override
    public ExecutionContext executionContext() {
        return executionContext;
    }
}
