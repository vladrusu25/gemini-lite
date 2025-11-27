package gemini_lite;

import gemini_lite.server.FileSystemRequestHandler;
import gemini_lite.server.ServerIO;

import java.io.File;
import java.io.IOException;

public class Server {
    private static final int DEFAULT_PORT = 1958;

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("You need to run : java -cp target/bcs2110-2025.jar gemini_lite.Server <directory> [port]");
            System.exit(1);
        }

        int port = DEFAULT_PORT;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1].trim());
                if (port < 1 || port > 65535) throw new IllegalArgumentException();
            } catch (Exception e) {
                System.err.println("You need to run : java -cp target/bcs2110-2025.jar gemini_lite.Server <directory> [port]");
                System.exit(1);
            }
        }

        File rootDirectory;
        try {
            rootDirectory = new File(args[0]).getCanonicalFile();
        } catch (IOException e) {
            System.err.println("Invalid document root");
            System.exit(1);
            return;
        }
        if (!rootDirectory.exists() || !rootDirectory.isDirectory() || !rootDirectory.canRead()) {
            System.err.println("Document root must be an existing, readable directory");
            System.exit(1);
        }

        try {
            var handler = new FileSystemRequestHandler(rootDirectory);
            new ServerIO(port,handler).start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
