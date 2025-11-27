package gemini_lite;

import gemini_lite.client.*;
import java.net.*;
// run
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://demo.svc.leastfixedpoint.nl/
// java -cp target/bcs2110-2025.jar gemini_lite.Client gemini-lite://localhost/
public class Client {

    public static void main(String[] args) throws Throwable {
        if (args.length < 1) {
            System.err.println("You need to run with a URI argument : Client <uri> [<input>]");
            System.exit(1);
        }

        String input = null;
        if(args.length >= 2) {
            input = args[1];
        }
        final var uri = new URI(args[0]);

        try {
            new ClientEngine().run(uri, input);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}