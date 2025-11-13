package gemini_lite;

import java.io.*;
import java.net.*;


// run
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://demo.svc.leastfixedpoint.nl/
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost
public class Client {
    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            System.err.println("Usage: TerribleClient <uri>");
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

        }

        try (final var socket = new Socket(host, port)) {
            final var in = socket.getInputStream();
            final var out = socket.getOutputStream();
            out.write((uri + "\r\n").getBytes());
            out.flush();
            try (final var r = new BufferedReader(new InputStreamReader(in))) {
                final var rep = r.readLine();
                if (rep != null && rep.startsWith("2")) {
                    try (final var w = new PrintWriter(System.out)) {
                        r.transferTo(w);
                    }
                } else {
                    System.err.println(rep);
                    System.exit(1);
                }
            }
        }
    }
}
