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
    /**
     * Parses a gemini-lite request from the given InputStream.
     * @param in the InputStream to read the request from
     * @return a Request object containing the parsed URI
     * @throws IOException if an I/O error occurs or if the request format is invalid
     */
    static Request parse (InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(MAX_REQUEST_LENGTH);
        int byteCount =0;
        boolean seenCR = false;
        while(true){
            int curr = in.read();

            if (curr == -1) {
                throw new ProtocolException("CRLF not found before end of stream");
            }

            if(seenCR){
                if(curr!= '\n'){
                    throw new ProtocolException("Invalid line: expected LF after CR");
                }
                break;
            }

            if(curr == '\r'){
                seenCR = true;
                continue;
            }
            if (curr == '\n') {
                throw new ProtocolException("Invalid line: LF without preceding CR");
            }
            if (byteCount == MAX_REQUEST_LENGTH) {
                throw new ProtocolException("Request line too long (exceeds " + MAX_REQUEST_LENGTH + " bytes)");
            }


            buffer.write(curr);
            byteCount++;
        }

        if(byteCount == 0){
            throw new ProtocolException("Empty request line");
        }

        String requestLine = buffer.toString(StandardCharsets.UTF_8);
        URI uri;
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

    /**
     * Writes the gemini-lite request to the given OutputStream.
     * @param out the OutputStream to write the request to
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(OutputStream out) throws IOException {
        String requestLine = uri.toString();
        out.write(requestLine.getBytes(StandardCharsets.UTF_8));
        out.write('\r');
        out.write('\n');
        out.flush();
    }

}
