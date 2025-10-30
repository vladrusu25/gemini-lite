import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TerribleServer {
    public static void main(String[] args) {
        try {
            new TerribleServer(1958).run();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private final int port;

    public TerribleServer(int port) {
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
            // TODO: read request. This server is so terrible it doesn't even wait for a request before sending a failure reply!
            OutputStream o = socket.getOutputStream();
            o.write("59 Terrible Server\r\n".getBytes());
            o.flush();
        } finally {
            socket.close();
        }
    }
}
