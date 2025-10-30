import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

public class UriTests {
    @Test
    public void testQueryFormatting() throws URISyntaxException {
        final var u = new URI(null, null, null, "test query", null);
        assertEquals("?test%20query", u.toString());
    }

    @Test
    public void testQueryReplacement1() throws URISyntaxException {
        final var u1 = new URI("gemini-lite://test.example/foo/bar?some%20input");
        final var u2 = new URI("?other%20input");
        assertEquals("?other%20input", u2.toString());
        assertNotEquals("gemini-lite://test.example/foo/bar?other%20input",
            u1.resolve(u2).toString()); // the "bar" part goes missing!
    }
}
