package gemini_lite;

import java.io.*;
import java.net.*;
// run
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://demo.svc.leastfixedpoint.nl/
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost/
public class Client {
    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            System.err.println("You need to run with a URI argument : Client <uri>");
            System.exit(1);
        }

        final var uri = new URI(args[0]);
        final var host = uri.getHost();
        if (host == null) {
            System.err.println("URI must contain a host");
            System.exit(1);
        }
        var port = uri.getPort();
        if (port == -1) {
            port = 1958;
            System.err.println("Port not specified, using default port " + port);
        }

        try (final var socket = new Socket(host, port)) {
            final var in = socket.getInputStream();
            final var out = socket.getOutputStream();

            Request request = new Request(uri);
            request.writeTo(out);

        }
    }
}
