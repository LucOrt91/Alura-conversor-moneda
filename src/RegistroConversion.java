import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroConversion {
    private final LocalDateTime timestamp;
    private final String monedaOrigen;
    private final String monedaDestino;
    private final BigDecimal cantidadOrigen;
    private final BigDecimal cantidadDestino;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public RegistroConversion(String monedaOrigen, String monedaDestino, BigDecimal cantidadOrigen, BigDecimal cantidadDestino) {
        this.timestamp = LocalDateTime.now();
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.cantidadOrigen = cantidadOrigen;
        this.cantidadDestino = cantidadDestino;
    }

    @Override
    public String toString() {
        return String.format("[%s] %.2f %s → %.2f %s",
                timestamp.format(FORMATO_FECHA),
                cantidadOrigen, monedaOrigen,
                cantidadDestino, monedaDestino);
    }
}