package gemini_lite.client;

import java.net.*;

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
}
