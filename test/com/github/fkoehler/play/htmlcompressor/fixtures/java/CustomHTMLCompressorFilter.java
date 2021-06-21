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
package com.github.fkoehler.play.htmlcompressor.fixtures.java;

import akka.stream.Materializer;
import com.github.fkoehler.play.htmlcompressor.HTMLCompressorFilter;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import play.Environment;
import play.Mode;
import play.api.Configuration;

import javax.inject.Inject;

import scala.concurrent.ExecutionContext;

/**
 * Custom implementation of the HTML compressor filter.
 */
public class CustomHTMLCompressorFilter extends HTMLCompressorFilter {

    private Configuration configuration;
    private Environment environment;
    private Materializer mat;
    private ExecutionContext executionContext;

    @Inject
    public CustomHTMLCompressorFilter(Configuration configuration, Environment environment, Materializer mat, ExecutionContext executionContext) {
        this.configuration = configuration;
        this.environment = environment;
        this.mat = mat;
        this.executionContext = executionContext;
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public HtmlCompressor compressor() {
        HtmlCompressor compressor = new HtmlCompressor();
        if (environment.mode() == Mode.DEV) {
            compressor.setPreserveLineBreaks(true);
        }

        compressor.setRemoveComments(true);
        compressor.setRemoveIntertagSpaces(true);
        compressor.setRemoveHttpProtocol(true);
        compressor.setRemoveHttpsProtocol(true);

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
