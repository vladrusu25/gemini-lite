package gemini_lite;

import gemini_lite.server.ProxyRequestHandler;
import gemini_lite.server.ServerIO;

public class Proxy {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("You need to run : java -cp target/bcs2110-2025.jar gemini_lite.Proxy <port>");
            System.exit(1);
        }

        final int port;
        try {
            port = Integer.parseInt(args[0].trim());
            if (port < 1 || port > 65535) throw new IllegalArgumentException();
        } catch (Exception e) {
            System.err.println("You need to run : java -cp target/bcs2110-2025.jar gemini_lite.Proxy <port>");
            System.exit(1);
            return;
        }

        try {
            var handler = new ProxyRequestHandler();
            new ServerIO(port, handler).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
