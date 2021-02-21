package org.pushingpixels.flamingo.api.svg;

import java.io.Writer;

/**
 * Transcoder listener.
 * 
 * @author Kirill Grouchnikov.
 */
public interface TranscoderListener {
	/**
	 * Returns the writer for the Java2D contents.
	 * 
	 * @return Writer for the Java2D contents.
	 */
	public Writer getWriter();

	/**
	 * Called when the transcoding process is finished.
	 */
	public void finished();
}