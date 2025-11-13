package gemini_lite;

import java.io.*;
import java.net.*;

// in order to run in windows powershell:
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
        try {
            var is_reader = new InputStreamReader(socket.getInputStream());
            var in = new BufferedReader(is_reader);
            var out = socket.getOutputStream();

            String request = in.readLine();
            System.err.println("Received request: " + request);

            String header = "20 text/gemini\r\n";
            String body = "Hello from my localhost server!\r\n";

            out.write((header + body).getBytes());
            out.flush();
        }
        finally {
            socket.close();
        }
    }

}
