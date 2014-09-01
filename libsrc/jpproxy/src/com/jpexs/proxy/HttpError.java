package com.jpexs.proxy;

class HttpError {

    StringBuffer content = null;
    Reply reply = null;

    HttpError(int code, String message) {
        String error;
        switch (code) {
            case 400:
                error = "Bad Request";
                break;

            case 403:
                error = "Forbidden";
                break;

            case 404:
                error = "Not found";
                break;

            case 503:
                error = "Service Unavailable";
                break;

            default:
                error = "Error";
                break;
        }

        reply = new Reply();
        reply.statusLine = "HTTP/1.0 " + code + " " + error;
        reply.setHeaderField("Content-type", "text/html");
        reply.setHeaderField("Server", ProxyConfig.appName + "/" + ProxyConfig.appVersion);

        content = new StringBuffer();
        content.append(message);
    }

    Reply getReply() {
        return reply;
    }

    String getContent() {
        if (content == null) {
            return null;
        }
        return content.toString();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (reply != null) {
            buf.append(reply.toString());
        }
        if (content != null) {
            buf.append(content.toString());
        }
        return buf.toString();
    }
}
