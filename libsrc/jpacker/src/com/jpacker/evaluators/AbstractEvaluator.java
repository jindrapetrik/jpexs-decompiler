/**
 * Packer version 3.0 (final)
 * Copyright 2004-2007, Dean Edwards
 * Web: {@link http://dean.edwards.name/}
 * 
 * This software is licensed under the MIT license
 * Web: {@link http://www.opensource.org/licenses/mit-license}
 * 
 * Ported to Java by Pablo Santiago based on C# version by Jesse Hansen, <twindagger2k @ msn.com>
 * Web: {@link http://jpacker.googlecode.com/}
 * Email: <pablo.santiago @ gmail.com>
 */
package com.jpacker.evaluators;

import com.jpacker.JPackerPattern;

/**
 * Abstract class for {@link Evaluator} objects. Its purpose is to provide the
 * implementation of a getter and setter of a {@link JPackerPattern} object.
 * 
 * @author Pablo Santiago <pablo.santiago @ gmail.com>
 */
public abstract class AbstractEvaluator implements Evaluator {

	private JPackerPattern	jpattern;

	/**
	 * Gets the {@link JPackerPattern} object
	 * 
	 * @return The {@link JPackerPattern} object
	 */
	@Override
	public JPackerPattern getJPattern() {
		return jpattern;
	}

	/**
	 * Sets the {@link JPackerPattern} object for this {@link Evaluator} object
	 * 
	 * @param jpattern
	 *            The {@link JPackerPattern} object to set
	 */
	@Override
	public void setJPattern(JPackerPattern jpattern) {
		this.jpattern = jpattern;
	}

}
