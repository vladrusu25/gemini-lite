package gemini_lite.server;

import gemini_lite.Reply;
import gemini_lite.Request;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.nio.file.*;
import java.net.*;

public class FileSystemRequestHandler implements RequestHandler {

    private final Path root;

    public FileSystemRequestHandler(File rootDirectory) {
        this.root = rootDirectory.toPath();
    }
    @Override
    public Reply handle(Request request) throws Exception {
        try {
            URI uri = request.getUri();
            String path = uri.getPath();

            if (path == null || path.isEmpty()) path = "/";

            Path targetPath = root.resolve("." + path).normalize();
            File file = targetPath.toFile().getAbsoluteFile();

            Path foundTargetPath = file.toPath();
            if (!foundTargetPath.startsWith(root)) {
                return new Reply(51, "File not found", null);
            }

            if (file.isDirectory()) {
                File idx = new File(file, "index.gmi");
                if (idx.isFile()) {
                    return fileReply(idx);
                }
                return directoryListing(file, uri);
            }

            if (file.isFile()) {
                return fileReply(file);
            }

            return new Reply(51, "File not found", null);
        }catch (FileNotFoundException e) {
            return new Reply(51, "File not found", null);
        }catch (Exception e) {
            return new Reply(50, "Server error occurred", null);
        }
    }


    private Reply directoryListing(File dir, URI requestUri) {
        File[] children = dir.listFiles();
        if (children == null) return new Reply(51, "Not found", null);

        String displayedPath = requestUri.getPath();
        if (displayedPath == null || displayedPath.isEmpty()) displayedPath = "/";
        if (!displayedPath.endsWith("/")) displayedPath = displayedPath + "/";

        StringBuilder gmi = new StringBuilder(128);
        gmi.append("# Directory listing for ").append(displayedPath).append("\n\n");

        // list directories
        gmi.append("## Directories\n\n");
        boolean seenDirectory = false;
        for (File file : children) {
            if (file.isDirectory()) {
                seenDirectory = true;
                gmi.append("=> ").append(file.getName()).append("\n");
            }
        }
        if (!seenDirectory) gmi.append("(none)\n");
        gmi.append("\n");

        // list files
        gmi.append("## Files\n\n");
        boolean seenFile = false;
        for (File file : children) {
            if (file.isFile()) {
                seenFile = true;
                gmi.append("=> ").append(file.getName()).append("\n");
            }
        }
        if (!seenFile) gmi.append("(none)\n");

        byte[] body = gmi.toString().getBytes(StandardCharsets.UTF_8);
        return new Reply(20, "text/gemini", new java.io.ByteArrayInputStream(body));
    }

    private static String mime(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".gmi")) return "text/gemini";
        if (fileName.endsWith(".txt")) return "text/plain";
        // fallback
        return "application/octet-stream";
    }
    private Reply fileReply(File file) throws IOException {
        InputStream body = new BufferedInputStream(new FileInputStream(file));
        String meta = mime(file);
        return new Reply(20, meta, body);
    }

}
