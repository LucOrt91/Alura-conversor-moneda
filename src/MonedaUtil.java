import java.util.*;

public class MonedaUtil {
    // Lista ampliada de monedas
    public static final List<String> MONEDAS_DISPONIBLES = Arrays.asList(
            "ARS", "BOB", "BRL", "CLP", "COP", "USD",
            "EUR", "MXN", "PEN", "UYU", "VES", "PYG"
    );

    // Nombres descriptivos de las monedas
    public static final Map<String, String> NOMBRES_MONEDAS = Map.ofEntries(
            Map.entry("ARS", "Peso argentino"),
            Map.entry("BOB", "Boliviano"),
            Map.entry("BRL", "Real brasileño"),
            Map.entry("CLP", "Peso chileno"),
            Map.entry("COP", "Peso colombiano"),
            Map.entry("USD", "Dólar estadounidense"),
            Map.entry("EUR", "Euro"),
            Map.entry("MXN", "Peso mexicano"),
            Map.entry("PEN", "Sol peruano"),
            Map.entry("UYU", "Peso uruguayo"),
            Map.entry("VES", "Bolívar venezolano"),
            Map.entry("PYG", "Guaraní paraguayo")
    );
}