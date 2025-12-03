package gemini_lite;

import gemini_lite.client.*;
import java.net.*;
// run
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://demo.svc.leastfixedpoint.nl/
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost/
public class Client {

    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            System.err.println("You need to run : java -cp target/bcs2110-2025.jar gemini_lite.Client <uri> [<input>]");
            System.exit(1);
        }

        String input = null;
        if(args.length >= 2) {
            input = args[1];
        }
        final var uri = new URI(args[0]);

        try {
            boolean isProxy = false;
            Reply reply = new ClientEngine().run(uri,input,isProxy,System.out);
            int status_code = reply.getStatusCode();
            if(status_code >=40 && status_code<=59) System.exit(status_code);
            else System.exit(0);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}