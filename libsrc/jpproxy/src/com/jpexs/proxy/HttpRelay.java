package com.jpexs.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public interface HttpRelay {

    void sendRequest(Request request) throws IOException, RetryRequestException;

    Reply recvReply(Request request) throws IOException, RetryRequestException;

    void close();
}
