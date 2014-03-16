/*
 * @(#)AbortException.java  
 *
 * Copyright (c) 1999-2012 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media;

/**
 * This exception is thrown when the production of an image
 * has been aborted.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 *
 * @version  $Id: AbortException.java 299 2013-01-03 07:40:18Z werner $
 */
public class AbortException extends Exception {

    public static final long serialVersionUID = 1L;

    /**
    Creates a new exception.
     */
    public AbortException() {
        super();
    }

    /**
    Creates a new exception.
    
     */
    public AbortException(String message) {
        super(message);
    }
}
