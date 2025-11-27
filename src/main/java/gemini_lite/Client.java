package gemini_lite;

import gemini_lite.client.*;
import java.io.*;
import java.net.*;
// run
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://demo.svc.leastfixedpoint.nl/
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost/
public class Client {

    private static final int DEFAULT_PORT = 1958;
    private static final int MAX_REDIRECTS = 5;
    private static final int MAX_SLOWDOWN = 60;
    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            System.err.println("You need to run with a URI argument : Client <uri> [<input>]");
            System.exit(1);
        }

        String input = null;
        if (args.length >= 2) {
            input = args[1];
        }
        boolean inputUsed = false;

        final var uri = new URI(args[0]);
        int redirect_count = 0;
        URI currentUri = uri;

        try{
            while(true){
                String host =  UriUtil.hostOf(currentUri);
                int port = UriUtil.portOf(currentUri, DEFAULT_PORT);

                try(Socket socket = new Socket(host, port)){
                    var in = socket.getInputStream();
                    var out = socket.getOutputStream();

                    Request request = new Request(currentUri);
                    request.writeTo(out);

                    Reply reply = Reply.parse(in);
                    int status_code = reply.getStatusCode();
                    int status_class = status_code / 10;

                    if(status_class == 1){
                        boolean isSensitive = false;
                        if(status_code == 11) isSensitive = true;
                        String meta = reply.getMeta();

                        String pendingInput = null;
                        if(!inputUsed && input != null){
                            pendingInput = input;
                            inputUsed = true;
                        }
                        currentUri = Inputs.buildUri(currentUri, meta, pendingInput, isSensitive);
                        continue;
                    }
                    else if(status_class == 2) {
                        String meta = reply.getMeta();
                        if(!isValidMimeType(meta)){
                            throw new ProtocolException("Invalid or missing mimetype");
                        }
                        InputStream body = reply.getBody();
                        if (body != null) body.transferTo(System.out);
                        System.out.flush();
                        System.exit(0);
                    }
                    else if(status_class == 3) {
                        currentUri = Redirects.buildRedirect(currentUri, reply.getMeta());
                        redirect_count++;
                        if(redirect_count > MAX_REDIRECTS){
                            throw new ProtocolException("Too many redirects (exceeds " + MAX_REDIRECTS + ")");
                        }
                        continue;
                    }
                    else if(status_class == 4) {
                        if(status_code==44){
                            int seconds = UriUtil.parsePositiveInt(reply.getMeta(),1);
                            if(seconds > MAX_SLOWDOWN) seconds = MAX_SLOWDOWN;
                            try{
                                Thread.sleep(seconds * 1000L);
                            }catch (InterruptedException ignored) {}
                            continue;
                        }
                        System.err.println(reply.getMeta());
                        System.exit(status_code);
                    }
                    else if(status_class == 5) {
                        System.err.println("Server error: " + reply.getMeta());
                        System.exit(status_code);
                    }

                    else {
                        throw new ProtocolException("Unknown status code class: " + status_code);
                    }
                }
            }
        } catch (Exception e) {
            System.exit(1);
        }
    }
    private static boolean isValidMimeType(String meta){
        if (meta == null || meta.isEmpty()) return false;
        String trimmed = meta.trim();
        if(trimmed.equals(meta)) return false;

        int slashIndex = meta.indexOf('/');
        if(slashIndex == -1|| slashIndex == trimmed.length()-1) return false;
        if(meta.indexOf('/', slashIndex + 1) != -1) return false;

        return true;
    }
}
