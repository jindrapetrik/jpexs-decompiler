package com.jpexs.proxy;

class RetryRequestException extends Exception {

    RetryRequestException() {
        super();
    }

    RetryRequestException(String message) {
        super(message);
    }
}
