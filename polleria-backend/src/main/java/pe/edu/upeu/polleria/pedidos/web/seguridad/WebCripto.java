package pe.edu.upeu.polleria.pedidos.web.seguridad;

import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class WebCripto {
    private static final SecureRandom RANDOM = new SecureRandom();

    private WebCripto() {
    }

    public static String secreto() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static String hash(String valor) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible");
        }
    }

    public static boolean igual(String a, String b) {
        return a != null && b != null
                && MessageDigest.isEqual(a.getBytes(StandardCharsets.US_ASCII), b.getBytes(StandardCharsets.US_ASCII));
    }

    public static boolean valido(String s) {
        return s != null && s.matches("[A-Za-z0-9_-]{43}");
    }

    public static String csrf(String token) {
        return hash("polleria-neyos-csrf:" + token);
    }

    /**
     * Campos con longitud explícita: evita colisiones por separadores en datos
     * libres.
     */
    public static String huella(Object... valores) {
        StringBuilder s = new StringBuilder();
        for (Object o : valores) {
            String v = o == null ? "" : o.toString();
            s.append(v.length()).append(':').append(v);
        }
        return hash(s.toString());
    }
}
