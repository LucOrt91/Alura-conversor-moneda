# Conversor de Monedas Alura 2025

## Descripción
Aplicación de consola Java que permite convertir entre diferentes monedas latinoamericanas y otras divisas importantes utilizando tasas de cambio en tiempo real. El programa cuenta con un historial de conversiones con marcas de tiempo para llevar un seguimiento de las operaciones realizadas.

## Características principales
- Conversión entre 12 monedas diferentes, con enfoque en monedas latinoamericanas
- Obtención de tasas de cambio en tiempo real mediante API externa
- Historial de las últimas 10 conversiones realizadas con fecha y hora
- Interfaz de consola intuitiva y amigable
- Validaciones completas para evitar errores de entrada

## Monedas disponibles
- Peso argentino (ARS)
- Boliviano (BOB)
- Real brasileño (BRL)
- Peso chileno (CLP)
- Peso colombiano (COP)
- Dólar estadounidense (USD)
- Euro (EUR)
- Peso mexicano (MXN)
- Sol peruano (PEN)
- Peso uruguayo (UYU)
- Bolívar venezolano (VES)
- Guaraní paraguayo (PYG)

## Requisitos
- Java 17 o superior (por el uso de Text Blocks)
- Conexión a Internet (para acceso a la API de tasas de cambio)
- Dependencias:
  - Google Gson (para procesamiento JSON)

## Instalación
1. Clona el repositorio: `git clone https://github.com/LucOrt91/Alura-conversor-moneda.git`
2. Importa el proyecto en tu IDE favorito
3. Asegúrate de tener las dependencias de Gson configuradas
4. Ejecuta la clase `Conversor.java`

## Uso
1. Al iniciar el programa, se mostrará un menú principal con las opciones disponibles
2. Para realizar una conversión:
   - Selecciona la opción "1. Realizar nueva conversión"
   - Elige la moneda de origen
   - Elige la moneda de destino
   - Ingresa la cantidad a convertir
   - El resultado se mostrará en pantalla y se guardará en el historial
3. Para ver el historial de conversiones:
   - Selecciona la opción "2. Ver historial de conversiones"
   - Se mostrarán las últimas 10 conversiones realizadas con fecha y hora

## Estructura del proyecto
- `Conversor.java`: Clase principal que contiene la lógica de conversión y la interfaz de usuario
- `MonedaUtil.java`: Clase utilitaria con las constantes de monedas disponibles
- `RegistroConversion.java`: Clase modelo para almacenar conversiones en el historial

## API utilizada
Este proyecto utiliza la API de [ExchangeRate-API](https://www.exchangerate-api.com/) para obtener tasas de cambio en tiempo real.

## Características técnicas
- Uso de Text Blocks para mejorar la legibilidad de textos multilínea
- Implementación de expresiones switch modernas
- Manejo adecuado de errores y excepciones
- Uso de la API HttpClient de Java para peticiones HTTP
- Serialización/deserialización JSON con Gson
- Formateo de fechas con DateTimeFormatter

## Limitaciones
- La API utilizada tiene un límite de solicitudes gratuitas
- La aplicación tiene una interfaz de consola, no gráfica
- Solo se guardan las últimas 10 conversiones realizadas

## Autor
Desarrollado como parte del Challenge Alura - Oracle ONE

## Licencia
Este proyecto está bajo la Licencia MIT