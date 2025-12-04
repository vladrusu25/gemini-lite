package gemini_lite.client;

import java.net.*;

public class Redirects {
    private Redirects(){}

    public static URI buildRedirect(URI currentUri, String target) throws ProtocolException, URISyntaxException {
        if(target ==null || target.isEmpty()){
            throw new ProtocolException("Null/empty redirect target");
        }

        final URI targetUri;
        try {
            targetUri = new URI(target.trim());
        }catch(IllegalArgumentException e){
            throw new ProtocolException("Invalid redirect target: " + target);
        }

        final URI newUri;

        if(targetUri.isAbsolute()){
            newUri = targetUri;
        }
        else{
            newUri = currentUri.resolve(targetUri);
        }

        UriUtil.validateUri(newUri);
        return newUri;
    }
}
