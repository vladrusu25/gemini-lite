package gemini_lite.server;

import gemini_lite.Reply;
import gemini_lite.Request;

public interface RequestHandler {
    Reply handle(Request request) throws Exception;
}
