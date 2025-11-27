package gemini_lite.client;

import java.net.*;


public class Inputs {

    public static URI buildUri(URI currentUri, String meta, String input, boolean isSensitive) throws URISyntaxException {
        if (input == null || input.isEmpty()) {
            var console = System.console();

            if (meta == null || meta.isEmpty()) {
                System.err.println(meta);
            }

            if (isSensitive) {
                input = new String(console.readPassword());
            }
            else input = console.readLine();

            if(input == null) input = "";
        }

        return rebuildUri(currentUri,input);
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