package gemini_lite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

public class Reply {
    private final int statusCode;
    private final String meta;
    private final InputStream body;
    private static final int MAX_REPLY_LENGTH = 1024;
    public Reply(int statusCode, String meta, InputStream body) {
        this.statusCode = statusCode;
        this.meta = meta;
        this.body = body;
    }

    public int getStatusCode() {return statusCode;}
    public String getMeta() {return meta;}
    public InputStream getBody() {return body;}

    public static Reply parse(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(MAX_REPLY_LENGTH);
        int byteCount = 0;
        boolean sawCR = false;

        while (true) {
            int curr = in.read();
            if (curr == -1) {
                throw new ProtocolException("CRLF not found before end of stream");
            }

            if (sawCR) {
                if (curr != '\n') throw new ProtocolException("Invalid line: CR not followed by LF");
                break;
            }

            if (curr == '\r') {
                sawCR = true;
                continue;
            }
            if (curr == '\n') {
                throw new ProtocolException("Invalid line: LF without preceding CR");
            }

            if (byteCount == MAX_REPLY_LENGTH) {
                throw new ProtocolException("Reply header too long (exceeds " + MAX_REPLY_LENGTH + " bytes)");
            }
            buffer.write(curr);
            byteCount++;
        }

        if(byteCount == 0){
            throw new ProtocolException("Empty reply line");
        }
        String replyLine = buffer.toString(StandardCharsets.UTF_8);

        if(replyLine.length() < 3
                || !Character.isDigit(replyLine.charAt(0))
                || !Character.isDigit(replyLine.charAt(1))
                || replyLine.charAt(2) !=' '){
            throw new ProtocolException("Invalid reply line format: Expected 'DD meta'");
        }

        int status = Integer.parseInt(replyLine.substring(0,2));
        if (status <10 || status >=60) {
            throw new ProtocolException("Invalid status code: " + status);
        }
        String meta = replyLine.substring(3);
        InputStream body = null;
        if(status >=20 && status <30){
            body = in;
        }
        return new Reply(status,meta,body);
    }

    public void writeTo(OutputStream out) throws IOException {
        String m = (meta == null) ? "" : meta;
        String header = String.format("%02d %s\r\n", statusCode, m);
        out.write(header.getBytes(StandardCharsets.UTF_8));
        if (body != null) {
            body.transferTo(out);
        }
        out.flush();
    }


}
