package gemini_lite.client;

import java.net.*;

public class Redirects {
    private Redirects(){}

    static URI buildRedirect(URI currentUri, String target) throws ProtocolException, URISyntaxException {
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
        if(newUri.getScheme()==null || !"gemini-lite".equalsIgnoreCase(newUri.getScheme())){
            throw new ProtocolException("Redirect target must use 'gemini-lite' scheme");
        }

        if(newUri.isAbsolute() && newUri.getHost()==null){
            throw new ProtocolException("Redirect target must contain a host");
        }

        if(newUri.getFragment()!=null){
            throw new ProtocolException("Redirect target cannot contain fragment");
        }
        return newUri;
    }
}
