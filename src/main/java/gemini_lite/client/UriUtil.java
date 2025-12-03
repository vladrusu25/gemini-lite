package gemini_lite.client;

import java.net.*;
import java.nio.charset.StandardCharsets;

public final class UriUtil {
    private UriUtil() {}

    public static String hostOf(URI u) {
        String h = u.getHost();

        if (h == null) throw new IllegalArgumentException("URI must contain a host");
        return h;
    }

    public static int portOf(URI uri, int defaultPort) {
        int p = uri.getPort();
        if (p == -1) return defaultPort;
        return p;
    }

    public static int parsePositiveInt(String s, int defaultVal) {
        if (s == null) return defaultVal;
        try {
            int v = Integer.parseInt(s.trim());
            if (v < 0) return defaultVal;
            return v;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static void validateUri(URI uri) throws ProtocolException {
        int MAX_REQUEST_BYTES = 1024;
        if (uri == null || !uri.isAbsolute())
            throw new ProtocolException("URI must be absolute");
        if (uri.getScheme() == null || !"gemini-lite".equalsIgnoreCase(uri.getScheme()))
            throw new ProtocolException("Expected gemini-lite scheme");
        if (uri.getHost() == null)
            throw new ProtocolException("Host is missing");
        if (uri.getFragment() != null)
            throw new ProtocolException("Fragment not allowed");
        if (uri.getUserInfo() != null)
            throw new ProtocolException("User info not allowed");
        if (uri.toString().getBytes(StandardCharsets.UTF_8).length > MAX_REQUEST_BYTES)
            throw new ProtocolException("URL too long");
    }
}
