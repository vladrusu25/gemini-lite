package gemini_lite.client;

public final class MimeUtil {
    private MimeUtil() {}

    public static boolean isValidMimeType(String meta) {
        if (meta == null || meta.isEmpty()) return false;
        String trimmed = meta.trim();
        if (!trimmed.equals(meta)) return false;

        int slashIndex = trimmed.indexOf('/');
        if (slashIndex <= 0 || slashIndex == trimmed.length() - 1) return false;
        if (trimmed.indexOf('/', slashIndex + 1) != -1) return false;

        return true;
    }
}
