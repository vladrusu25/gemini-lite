import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TerribleClient {
    public static void main(String[] args) throws Throwable {
        try (final var s = new Socket("demo.svc.leastfixedpoint.nl", 1958)) {
            final var i = s.getInputStream();
            final var o = s.getOutputStream();
            o.write("gemini-lite://demo.svc.leastfixedpoint.nl/\r\n".getBytes());
            o.flush();
            try (final var r = new BufferedReader(new InputStreamReader(i))) {
                final var rep = r.readLine();
                if (rep.startsWith("2")) {
                    try (final var w = new PrintWriter(System.out)) {
                        r.transferTo(w);
                    }
                } else {
                    System.err.println(rep);
                    System.exit(Integer.parseInt(rep.substring(0, 2)));
                }
            }
        }
    }
}
