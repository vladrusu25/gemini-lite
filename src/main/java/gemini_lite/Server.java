package gemini_lite;

import java.io.*;
import java.net.*;

// in order to run in Windows powershell:
// mvn clean package (this will create the updated jar if there are any updates)
// java -cp target/bcs2110-2025.jar gemini_lite.Server

public class Server {
    public static void main(String[] args) throws IOException {
        final int PORT  = 1958;
        try {
            new Server(1958).run();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }

    }

    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        try (final var server = new ServerSocket(port)) {
            System.err.println("Listening on port " + port);
            while (true) {
                final var socket = server.accept();
                handleConnection(socket);
            }

        }
    }

    public void handleConnection(Socket socket) throws IOException {
        try (socket) {
            InputStream in  = new BufferedInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();

            Request req;
            try {
                req = Request.parse(in);
                System.err.println("Request for " + req.getUri());
            } catch (java.net.ProtocolException pe) {
                new Reply(59, "Bad request: " + pe.getMessage(), null).writeTo(out);
                return;
            } catch (IOException ioe) {
                new Reply(50, "I/O error", null).writeTo(out);
                return;
            }

            byte[] bodyBytes = "Hello from my localhost server!\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);

            Reply ok = new Reply(20, "text/gemini", new ByteArrayInputStream(bodyBytes));
            ok.writeTo(out);
        }
    }
}

