package gemini_lite.client;

import gemini_lite.Reply;
import gemini_lite.Request;

import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URI;

public class ClientEngine {

    private static final int DEFAULT_PORT = 1958;
    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_SLOWDOWN = 60;

    public Reply run(URI uri, String cliInput, boolean isProxy, OutputStream out) throws Exception {
        UriUtil.validateUri(uri);

        String proxyHost = null;
        Integer proxyPort = null;

        if(!isProxy) {
            String env_var = System.getenv("GEMINI_LITE_PROXY");
            if(env_var != null) {
                String[] env_parts = env_var.split(":",2);
                if(env_parts.length == 2) {
                    proxyHost = env_parts[0];
                    proxyPort = Integer.parseInt(env_parts[1]);
                }
            }
        }

        String input = cliInput;
        boolean inputUsed = false;

        int backoff_seconds =0;
        String backoffOrigin = null;
        int redirect_count = 0;
        URI currentUri = uri;

        while (true) {
            String host;
            int port;
            if(!isProxy && proxyHost != null && proxyPort != null ){
                host = proxyHost;
                port = proxyPort;
            }
            else{
                host = UriUtil.hostOf(currentUri);
                port = UriUtil.portOf(currentUri, DEFAULT_PORT);
            }


            String origin = host + ":" + port;
            if (!origin.equals(backoffOrigin)) {
                backoffOrigin = origin;
                backoff_seconds = 0;
            }

            try (Socket socket = new Socket(host, port)) {
                var in  = socket.getInputStream();
                var socketOut = socket.getOutputStream();

                Request request = new Request(currentUri);
                request.writeTo(socketOut);

                Reply reply = Reply.parse(in);
                int status_code  = reply.getStatusCode();
                int status_class = status_code / 10;

                if (status_class == 1) {
                    backoff_seconds =0;

                    if(isProxy){
                        return reply;
                    }

                    boolean isSensitive = (status_code == 11);
                    String meta = reply.getMeta();

                    String pendingInput = null;
                    if (!inputUsed && input != null) {
                        pendingInput = input;
                        inputUsed = true;
                    }
                    currentUri = Inputs.buildUri(currentUri, meta, pendingInput, isSensitive);

                    continue;
                }
                else if (status_class == 2) {
                    backoff_seconds =0;

                    String meta = reply.getMeta();
                    if (!MimeUtil.isValidMimeType(meta)) {
                        throw new ProtocolException("Invalid or missing mimetype");
                    }
                    var body = reply.getBody();
                    if(body != null){
                        body.transferTo(out);
                    }
                    out.flush();
                    return reply;
                }
                else if (status_class == 3) {
                    backoff_seconds =0;

                    currentUri = Redirects.buildRedirect(currentUri, reply.getMeta());
                    redirect_count++;
                    if (redirect_count > MAX_REDIRECTS) {
                        throw new ProtocolException("Too many redirects");
                    }
                    continue;
                }
                else if (status_class == 4) {
                    //handle exponential backoff time for status code 44
                    if (status_code == 44) {
                        int suggested_backoff_seconds = UriUtil.parsePositiveInt(reply.getMeta(), 1);

                        if (backoff_seconds == 0) {
                            backoff_seconds = Math.min(suggested_backoff_seconds, MAX_SLOWDOWN);
                        }
                        else {
                            backoff_seconds = Math.max(
                                    suggested_backoff_seconds,
                                    Math.min(MAX_SLOWDOWN, backoff_seconds * 2)
                            );
                        }
                        try {
                            Thread.sleep(backoff_seconds * 1000L);
                        }catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                        continue;
                    }

                    backoff_seconds =0;

                    if(!isProxy) {
                        reply.writeTo(out);
                        out.flush();
                    }
                    return reply;
                }
                else if (status_class == 5) {
                    backoff_seconds =0;
                    if(!isProxy) {
                        reply.writeTo(out);
                        out.flush();
                    }
                    return reply;
                }
                else {
                    throw new ProtocolException("Unknown status code class: " + status_code);
                }
            }
        }
    }
}
