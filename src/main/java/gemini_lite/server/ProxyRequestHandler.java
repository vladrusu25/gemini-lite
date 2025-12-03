package gemini_lite.server;

import gemini_lite.Reply;
import gemini_lite.Request;
import gemini_lite.client.ClientEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.ProtocolException;

public class ProxyRequestHandler implements RequestHandler {
    private final ClientEngine engine = new ClientEngine();
    @Override
    public Reply handle(Request request) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            boolean isProxy = true;
            Reply header = engine.run(request.getUri(), null, isProxy, buffer);

            byte[] bytes = buffer.toByteArray();
            ByteArrayInputStream body = new ByteArrayInputStream(bytes);

            Reply reply = new Reply(header.getStatusCode(), header.getMeta(), body);
            return reply;
        } catch (ProtocolException e) {
            return new Reply(43, "proxy error: " + e.getMessage(), null);
        }
        catch (Exception e) {
            return new Reply(43, "proxy error", null);
        }
    }
}
