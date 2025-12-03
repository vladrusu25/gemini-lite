package gemini_lite.server;

import gemini_lite.Reply;
import gemini_lite.Request;

public interface RequestHandler {
    Reply handle(Request request) throws Exception;
}
//implement proxy request handler
// proxy request handler is used to create a server in actual proxy class
// proxy class creates a client engine with a isProxy == true variable, that will be a "flag" for input required status codes and
// 43 proxy error responses