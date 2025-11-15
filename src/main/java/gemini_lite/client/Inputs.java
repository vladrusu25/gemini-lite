package gemini_lite.client;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Inputs {
    public static URI buildUri(URI currentUri, String meta, String input, boolean isSensitive) throws URISyntaxException {
        if (input == null || input.isEmpty()) {
            var console = System.console();
            if (console == null) {
                throw new IllegalStateException("No console available for input");
            }

            if (meta == null || meta.isEmpty()) {
                System.err.println("No meta provided for input request");
            }

            if (isSensitive) {
                input = new String(console.readPassword());
            }
            else input = console.readLine();

            if(input == null) input = "";
        }

        String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8);

        return rebuildUri(currentUri,"query=" + encoded);
    }

    private static URI rebuildUri(URI currentUri, String query) throws URISyntaxException {
        String newPath;
        if (currentUri.getPath() == null || currentUri.getPath().isEmpty()){
            newPath = "/";
        } else {
            newPath = currentUri.getPath();
        }
        return new URI(
            currentUri.getScheme(),
            currentUri.getUserInfo(),
            currentUri.getHost(),
            currentUri.getPort(),
            newPath,
            query,
                null
        );
    }
}