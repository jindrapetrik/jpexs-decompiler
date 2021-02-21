/*
 * Copyright (c) 2003-2010 Flamingo Kirill Grouchnikov
 * and <a href="http://www.topologi.com">Topologi</a>. 
 * Contributed by <b>Rick Jelliffe</b> of <b>Topologi</b> 
 * in January 2006.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Flamingo Kirill Grouchnikov Topologi nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.flamingo.api.bcb;

import java.io.InputStream;
import java.util.List;

import org.pushingpixels.flamingo.api.common.StringValuePair;

/**
 * The application callback that can be set on {@link JBreadcrumbBar}.
 * 
 * @param <T>
 *            Type of data associated with each breadcrumb bar item.
 */
public abstract class BreadcrumbBarCallBack<T> {
	/**
	 * Sets up the callback.
	 * 
	 * @throws BreadcrumbBarException
	 *             Runtime exception that wraps the cause. Is thrown only when
	 *             {@link #setThrowsExceptions(boolean)} has been called with
	 *             <code>true</code> parameter.
	 */
	public void setup() throws BreadcrumbBarException {
	}

	/**
	 * If <code>true</code>, some of the operations will throw
	 * {@link BreadcrumbBarException}.
	 */
	protected boolean throwsExceptions;

	/**
	 * Sets the indication whether the operations of this breadcrumb bar will
	 * throw {@link BreadcrumbBarException}.
	 * 
	 * @param throwsExceptions
	 *            If <code>true</code>, the operations of this breadcrumb bar
	 *            will throw {@link BreadcrumbBarException}.
	 */
	public void setThrowsExceptions(boolean throwsExceptions) {
		this.throwsExceptions = throwsExceptions;
	}

	/**
	 * Returns the choice element that corresponds to the specified path. If the
	 * path is empty, <code>null</code> should be returned. If path is
	 * <code>null</code>, the "root" elements should be returned
	 * 
	 * @param path
	 *            Breadcrumb bar path.
	 * @return The choice element that corresponds to the specified path
	 * @throws BreadcrumbBarException
	 *             Runtime exception that wraps the cause. Is thrown only when
	 *             {@link #setThrowsExceptions(boolean)} has been called with
	 *             <code>true</code> parameter.
	 */
	public List<StringValuePair<T>> getPathChoices(List<BreadcrumbItem<T>> path)
			throws BreadcrumbBarException {
		return null;
	}

	/**
	 * Returns the choice element that corresponds to the specified path. If the
	 * path is empty, <code>null</code> should be returned. If path is
	 * <code>null</code>, the "root" elements should be returned
	 * 
	 * @param path
	 *            Breadcrumb bar path.
	 * @return The choice element that corresponds to the specified path
	 * @throws BreadcrumbBarException
	 *             Runtime exception that wraps the cause. Is thrown only when
	 *             {@link #setThrowsExceptions(boolean)} has been called with
	 *             <code>true</code> parameter.
	 */
	public List<StringValuePair<T>> getLeafs(List<BreadcrumbItem<T>> path)
			throws BreadcrumbBarException {
		return null;
	}

	/**
	 * Returns the input stream with the leaf content. Some implementations may
	 * return <code>null</code> if this is not applicable.
	 * 
	 * @param leaf
	 *            Leaf.
	 * @return Input stream with the leaf content. May be <code>null</code> if
	 *         this is not applicable.
	 * @throws BreadcrumbBarException
	 *             Runtime exception that wraps the cause. Is thrown only when
	 *             {@link #setThrowsExceptions(boolean)} has been called with
	 *             <code>true</code> parameter.
	 */
	public InputStream getLeafContent(T leaf) throws BreadcrumbBarException {
		return null;
	}
}
