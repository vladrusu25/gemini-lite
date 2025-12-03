package gemini_lite.client;

import gemini_lite.Reply;
import gemini_lite.Request;

import java.io.InputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URI;

public class ClientEngine {

    private static final int DEFAULT_PORT = 1958;
    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_SLOWDOWN = 60;

    public void run(URI uri, String cliInput) throws Exception {
        UriUtil.validateUri(uri);

        String input = cliInput;
        boolean inputUsed = false;

        int redirect_count = 0;
        URI currentUri = uri;

        while (true) {
            String host = UriUtil.hostOf(currentUri);
            int port = UriUtil.portOf(currentUri, DEFAULT_PORT);

            try (Socket socket = new Socket(host, port)) {
                var in  = socket.getInputStream();
                var out = socket.getOutputStream();

                Request request = new Request(currentUri);
                request.writeTo(out);

                Reply reply = Reply.parse(in);
                int status_code  = reply.getStatusCode();
                int status_class = status_code / 10;

                if (status_class == 1) {
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
                    String meta = reply.getMeta();
                    if (!MimeUtil.isValidMimeType(meta)) {
                        throw new ProtocolException("Invalid or missing mimetype");
                    }
                    InputStream body = reply.getBody();
                    if (body != null) body.transferTo(System.out);
                    System.out.flush();
                    System.exit(0);
                }
                else if (status_class == 3) {
                    currentUri = Redirects.buildRedirect(currentUri, reply.getMeta());
                    redirect_count++;
                    if (redirect_count > MAX_REDIRECTS) {
                        throw new ProtocolException("Too many redirects");
                    }
                    continue;
                }
                else if (status_class == 4) {
                    if (status_code == 44) {
                        int seconds = UriUtil.parsePositiveInt(reply.getMeta(), 1);
                        if (seconds > MAX_SLOWDOWN) seconds = MAX_SLOWDOWN;
                        try {
                            Thread.sleep(seconds * 1000L);
                        } catch (InterruptedException ignored) { }
                        continue;
                    }
                    System.err.println(reply.getMeta());
                    System.exit(status_code);
                }
                else if (status_class == 5) {
                    System.err.println("Server error: " + reply.getMeta());
                    System.exit(status_code);
                }
                else {
                    throw new ProtocolException("Unknown status code class: " + status_code);
                }
            }
        }
    }
}
