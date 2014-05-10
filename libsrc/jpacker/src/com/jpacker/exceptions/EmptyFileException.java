/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jpacker.exceptions;

import java.io.IOException;

/**
 *
 * @author poly
 */
public class EmptyFileException extends IOException {    

    public EmptyFileException(){
        super();        
    }

    public EmptyFileException(String message){
        super(message);
    }

    private EmptyFileException(String path, String reason) {
	super(path + ((reason == null)
		      ? ""
		      : " (" + reason + ")"));
    }
}
