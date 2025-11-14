package gemini_lite;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Request {
    private final URI uri;
    private static final int MAX_REQUEST_LENGTH = 1024;
    public Request(URI uri){
        this.uri = uri;
    }

    public URI getUri(){
        return uri;
    }

    static Request parse (InputStream in) throws IOException, ProtocolException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(MAX_REQUEST_LENGTH);
        int prev = -1;
        int byteCount =0;

        while(true){
            int curr = in.read();

            if (curr == -1) {
                throw new ProtocolException("CRLF not found before end of stream");
            }
            if(prev == '\r'){
                if(curr == '\n') break;
                else throw new ProtocolException("Invalid line: expected LF after CR");
            }
            if(curr == '\n'){
                throw new ProtocolException("Invalid line: CR expected before LF");
            }

            buffer.write(curr);
            byteCount++;

            if(byteCount > MAX_REQUEST_LENGTH){
                throw new ProtocolException("Request line too long (exceeds " + MAX_REQUEST_LENGTH + " bytes)");
            }
            prev = curr;
        }

        if(byteCount == 0){
            throw new ProtocolException("Empty request line");
        }

        String requestLine = buffer.toString(StandardCharsets.UTF_8);
        URI uri = null;
        try {
            uri = new URI(requestLine);
        } catch (URISyntaxException e) {
            throw new ProtocolException("Invalid URI syntax: " + e.getMessage());
        }

        if(uri.getScheme() == null || !uri.getScheme().equalsIgnoreCase("gemini-lite")){
            throw new ProtocolException("Expected 'gemini-lite' scheme");
        }
        if(uri.getHost() == null){
            throw new ProtocolException("Host is missing");
        }
        if(uri.getFragment() != null){
            throw new ProtocolException("Request cannot contain fragment");
        }

        String path;
        if(uri.getPath() == null || uri.getPath().isEmpty()){
            path = "/";
        } else {
            path = uri.getPath();
        }

        URI normalizedUri = null;
        try {
            normalizedUri = new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                path,
                uri.getQuery(),
                null
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return new Request(normalizedUri);
    }

    public void writeTo(OutputStream out) throws IOException {
        String requestLine = uri.toString();
        out.write(requestLine.getBytes(StandardCharsets.UTF_8));
        out.write('\r');
        out.write('\n');
        out.flush();
    }

}
