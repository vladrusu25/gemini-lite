package gemini_lite;

import java.net.*;
public class Request {
    private final URI uri;
    public Request(URI uri){
        this.uri = uri;
    }

    public URI getUri(){
        return uri;
    }

}
