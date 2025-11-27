package gemini_lite.server;

import gemini_lite.Request;
import gemini_lite.Reply;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerIO {
    private final int port;
    private final RequestHandler handler;
    private volatile boolean running = false;

    public ServerIO(int port, RequestHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    public void start() throws IOException {
        running = true;
        try (ServerSocket server = new ServerSocket(port)) {
            System.err.println("gemini-lite server listening on port :" + port);
            while (running) {
                try {
                    Socket s = server.accept();
                    handleOneConnection(s);
                } catch (IOException acceptErr) {

                }
            }
        }
    }

    private void handleOneConnection(Socket socket) {
        try (socket) {
            socket.setSoTimeout(30000);

            InputStream in  = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            Reply reply;
            try {
                Request req = Request.parse(in);

                reply = handler.handle(req);
                if (reply == null) {
                    reply = new Reply(50, "Server error occurred", null);
                }
            } catch (Exception e) {
                reply = new Reply(50, "Server error occurred", null);
            }

            try {
                reply.writeTo(out);
            } catch (IOException ignored) {
            }
        } catch (IOException ignored) {

        }
    }
}
