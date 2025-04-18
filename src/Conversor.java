import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Conversor {
    private final String API_KEY = "1e72987ddeb81b4c6c77d493";
    private final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private final HttpClient httpClient;
    private final Gson gson;

    // Historial de conversiones
    private final List<RegistroConversion> historialConversiones = new ArrayList<>();
    private static final int MAX_HISTORIAL = 10; // Máximo número de conversiones a almacenar

    // Clases modelo para mapeo directo de JSON a objetos Java
    static class ExchangeRateResponse {
        String result;
        String documentation;
        String terms_of_use;
        long time_last_update_unix;
        String time_last_update_utc;
        long time_next_update_unix;
        String time_next_update_utc;
        String base_code;
        Map<String, Double> conversion_rates;
    }

    static class PairConversionResponse {
        String result;
        String documentation;
        String terms_of_use;
        long time_last_update_unix;
        String time_last_update_utc;
        long time_next_update_unix;
        String time_next_update_utc;
        String base_code;
        String target_code;
        double conversion_rate;
    }

    public Conversor() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public Map<String, Double> obtenerTasasDeCambio(String monedaBase) throws IOException, InterruptedException {
        String url = BASE_URL + API_KEY + "/latest/" + monedaBase;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", "ConversorMoneda/1.0")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = enviarSolicitud(request);
        verificarRateLimits(response);

        ExchangeRateResponse tasasResponse = deserializarRespuesta(response, ExchangeRateResponse.class);

        if (!"success".equals(tasasResponse.result)) {
            throw new IOException("Error en la respuesta de la API");
        }

        return tasasResponse.conversion_rates;
    }

    // Metodo para filtrar solo las monedas seleccionadas
    public Map<String, Double> obtenerTasasDeCambioFiltradas(String monedaBase) throws IOException, InterruptedException {
        Map<String, Double> todasLasTasas = obtenerTasasDeCambio(monedaBase);

        return todasLasTasas.entrySet().stream()
                .filter(entry -> MonedaUtil.MONEDAS_DISPONIBLES.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public BigDecimal convertir(String monedaOrigen, String monedaDestino, BigDecimal cantidad)
            throws IOException, InterruptedException {
        Map<String, Double> tasas = obtenerTasasDeCambio(monedaOrigen);

        if (!tasas.containsKey(monedaDestino)) {
            throw new IllegalArgumentException("Moneda destino no disponible: " + monedaDestino);
        }

        double tasaConversion = tasas.get(monedaDestino);
        BigDecimal resultado = cantidad.multiply(BigDecimal.valueOf(tasaConversion)).setScale(2, RoundingMode.HALF_UP);

        // Registrar la conversión en el historial
        registrarConversion(monedaOrigen, monedaDestino, cantidad, resultado);

        return resultado;
    }

    // Metodo para registrar una conversión en el historial
    private void registrarConversion(String monedaOrigen, String monedaDestino, BigDecimal cantidadOrigen, BigDecimal cantidadDestino) {
        RegistroConversion registro = new RegistroConversion(monedaOrigen, monedaDestino, cantidadOrigen, cantidadDestino);
        historialConversiones.add(0, registro); // Añadir al inicio (más reciente primero)

        // Limitar el tamaño del historial
        if (historialConversiones.size() > MAX_HISTORIAL) {
            historialConversiones.remove(historialConversiones.size() - 1);
        }
    }

    // Metodo para obtener el historial de conversiones
    public List<RegistroConversion> obtenerHistorial() {
        return new ArrayList<>(historialConversiones);
    }

    // Metodo genérico para deserializar cualquier respuesta a una clase modelo
    private <T> T deserializarRespuesta(HttpResponse<String> response, Class<T> claseModelo) throws IOException {
        try {
            T resultado = gson.fromJson(response.body(), claseModelo);
            if (resultado == null) {
                throw new IOException("No se pudo convertir la respuesta al modelo " + claseModelo.getSimpleName());
            }
            return resultado;
        } catch (Exception e) {
            throw new IOException("Error al deserializar JSON: " + e.getMessage(), e);
        }
    }

    private HttpResponse<String> enviarSolicitud(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IOException("Error de conexión: " + e.getMessage(), e);
        }

        switch (response.statusCode()) {
            case 200:
                return response;
            case 404:
                throw new IOException("Recurso no encontrado: verifique monedas ingresadas");
            case 429:
                throw new IOException("Límite de solicitudes excedido. Intente más tarde");
            default:
                throw new IOException("Error HTTP " + response.statusCode());
        }
    }

    private void verificarRateLimits(HttpResponse<String> response) {
        response.headers().firstValue("X-RateLimit-Remaining").ifPresent(remaining -> {
            try {
                int remainingRequests = Integer.parseInt(remaining);
                if (remainingRequests < 10) {
                    System.out.println("⚠️ Advertencia: Quedan pocas solicitudes disponibles (" + remainingRequests + ")");
                }
            } catch (NumberFormatException e) {
                // Ignora si el valor no es un número
            }
        });
    }

    /**
     * Muestra el menú principal del conversor
     */
    private static void mostrarMenuPrincipal() {
        System.out.println("""

                ===== CONVERSOR DE MONEDAS LATAM =====
                1. Realizar nueva conversión
                2. Ver historial de conversiones
                0. Salir
                Elija una opción válida: """);
    }

    /**
     * Muestra el menú para seleccionar la moneda origen/destino
     */
    private static void mostrarMenuMonedas(String tipo) {
        StringBuilder menuBuilder = new StringBuilder("\n===== SELECCIONE MONEDA " + tipo + " =====\n");
        int i = 1;
        for (String moneda : MonedaUtil.MONEDAS_DISPONIBLES) {
            menuBuilder.append(i).append(". ").append(MonedaUtil.NOMBRES_MONEDAS.get(moneda))
                    .append(" (").append(moneda).append(")\n");
            i++;
        }
        menuBuilder.append("0. Volver al menú principal\n");
        menuBuilder.append("Elija una opción válida: ");

        System.out.print(menuBuilder);
    }

    /**
     * Muestra el historial de conversiones
     */
    private void mostrarHistorialConversiones() {
        List<RegistroConversion> historial = obtenerHistorial();

        if (historial.isEmpty()) {
            System.out.println("\n⚠️ No hay conversiones registradas en el historial");
            return;
        }

        System.out.println("""

                ===== HISTORIAL DE CONVERSIONES =====
                """);

        int i = 1;
        for (RegistroConversion registro : historial) {
            System.out.printf("%d. %s%n", i++, registro);
        }
    }

    /**
     * Realiza una nueva conversión entre monedas
     */
    private void realizarNuevaConversion(Scanner scanner) throws IOException, InterruptedException {
        // Seleccionar moneda origen
        mostrarMenuMonedas("ORIGEN");
        int opcionOrigen;
        try {
            opcionOrigen = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Por favor, ingrese un número válido");
            return;
        }

        if (opcionOrigen == 0) return;
        if (opcionOrigen < 1 || opcionOrigen > MonedaUtil.MONEDAS_DISPONIBLES.size()) {
            System.out.println("⚠️ Opción no válida");
            return;
        }

        String monedaOrigen = MonedaUtil.MONEDAS_DISPONIBLES.get(opcionOrigen - 1);

        // Seleccionar moneda destino
        mostrarMenuMonedas("DESTINO");
        int opcionDestino;
        try {
            opcionDestino = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Por favor, ingrese un número válido");
            return;
        }

        if (opcionDestino == 0) return;
        if (opcionDestino < 1 || opcionDestino > MonedaUtil.MONEDAS_DISPONIBLES.size()) {
            System.out.println("⚠️ Opción no válida");
            return;
        }

        String monedaDestino = MonedaUtil.MONEDAS_DISPONIBLES.get(opcionDestino - 1);

        // Validar que sean monedas diferentes
        if (monedaOrigen.equals(monedaDestino)) {
            System.out.println("⚠️ La moneda origen y destino son iguales");
            return;
        }

        // Ingresar cantidad a convertir
        System.out.print("\nIngrese cantidad a convertir: ");
        try {
            BigDecimal cantidad = new BigDecimal(scanner.nextLine());
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("⚠️ La cantidad debe ser mayor que cero");
                return;
            }

            BigDecimal resultado = convertir(monedaOrigen, monedaDestino, cantidad);

            System.out.println("""

                    ===== RESULTADO DE LA CONVERSIÓN =====
                    """);
            System.out.printf("%.2f %s (%s) = %.2f %s (%s)%n",
                    cantidad, monedaOrigen, MonedaUtil.NOMBRES_MONEDAS.get(monedaOrigen),
                    resultado, monedaDestino, MonedaUtil.NOMBRES_MONEDAS.get(monedaDestino));

        } catch (NumberFormatException e) {
            System.out.println("⚠️ Cantidad no válida");
        }
    }

    public static void main(String[] args) {
        Conversor conversor = new Conversor();
        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        try {
            System.out.println("""
                    *************************************************
                    *           CONVERSOR DE MONEDAS 2025           *
                    *      Con Historial y Monedas Ampliadas        *
                    *************************************************
                    """);

            System.out.println("Inicializando conexión con API de tasas de cambio...");

            do {
                mostrarMenuPrincipal();

                try {
                    opcion = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Por favor, ingrese un número válido");
                    continue;
                }

                // Procesar opción del menu principal
                switch (opcion) {
                    case 1:
                        conversor.realizarNuevaConversion(scanner);
                        break;
                    case 2:
                        conversor.mostrarHistorialConversiones();
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("⚠️ Opción no válida");
                }

                if (opcion != 0) {
                    System.out.print("\nPresione ENTER para continuar...");
                    scanner.nextLine();
                }

            } while (opcion != 0);

            System.out.println("""

                    ¡Gracias por usar el Conversor de Monedas!
                    Desarrollado como parte del Challenge Alura - Oracle ONE
                    """);

        } catch (Exception e) {
            System.err.println("""

                    ⚠️ Ha ocurrido un error en la aplicación:
                    """ + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}