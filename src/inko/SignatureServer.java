/*
 *
 */

package inko;

/**
 *
 * @author  Martin Pröhl alias MythGraphics
 * @version 1.0.0
 *
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import util.EnumHelper;

public class SignatureServer {

    public final static int PORT = 8080;

    private SignatureServer() {}

    public static BufferedImage convertBytesToImage(byte[] imageBytes) throws IOException {
        try ( ByteArrayInputStream in = new ByteArrayInputStream( imageBytes )) {
            return ImageIO.read(in);
        }
    }

    private static Patient getPatientById(int id) {
        // Hier suchst du den Patienten aus deiner App-Datenbank/Liste
        // return meinPatientenManager.getPatientById(id);
        return null;
    }

    private static void refreshUI(Patient p) {
        // Da der HttpServer in einem Hintergrundthread läuft,
        // musst du GUI-Updates in Swing über den EventQueue/EDT jagen:
        // java.awt.EventQueue.invokeLater(() -> { meinLabel.repaint(); });
    }

    public static HttpServer startServer() throws IOException {
        // Server starten
        HttpServer server = HttpServer.create( new InetSocketAddress( PORT ), 0 );
        server.createContext( "/signature", new SignatureHandler() ); // Kontext für die Unterschriften-Seite
        server.setExecutor(null); // Standard-Executor nutzen
        server.start();
        System.out.println("Server gestartet an Port " + PORT + ".");
        return server;
    }

    private static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }
        return result;
    }

    // Hilfsmethode zum Senden von Antworten
    private static void sendResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] response = text.getBytes(UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        try ( OutputStream os = exchange.getResponseBody() ) {
            os.write(response);
        }
    }

    static class SignatureHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            URI requestedUri = exchange.getRequestURI();

            // Parameter aus der URL parsen
            Map<String, String> params = parseQueryParameters( requestedUri.getQuery() );
            String patientId = params.get("id");
            SignableDocument document = EnumHelper.getEnumFromString( SignableDocument.class, params.get( "document" ));

            if ( "GET".equalsIgnoreCase( method )) {
                // HTML-Seite ausliefern
                String html = getHtmlTemplate();
                byte[] response = html.getBytes(UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, response.length);
                try ( OutputStream os = exchange.getResponseBody() ) {
                    os.write(response);
                }

            } else if ( "POST".equalsIgnoreCase( method )) {
                if ( patientId == null || patientId.isEmpty() || document == null ) {
                    sendResponse(exchange, 400, "Fehler: Keine Patienten-ID übergeben oder Dokument ungültig.");
                    return;
                }
                // PNG-Daten empfangen
                InputStream in = exchange.getRequestBody();
                BufferedReader reader = new BufferedReader( new InputStreamReader( in, UTF_8 ));

                StringBuilder json = new StringBuilder();
                String line;
                while (( line = reader.readLine() ) != null ) {
                    json.append(line);
                }

                // simples Extrahieren des Base64-Strings aus dem JSON
                // (In der Praxis gerne einen echten JSON-Parser oder präzisen Regex nutzen)
                String payload = json.toString();
                if ( payload.contains( "image/png;base64" )) {
                    // Base64 extrahieren und dekodieren
                    String base64Data = payload.split("image/png;base64,")[1].split("\"")[0];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);

                    BufferedImage signImage = convertBytesToImage(imageBytes);
                    Patient patient = getPatientById( Integer.parseInt( patientId ));
                    if (patient != null) {
                        patient.setSignature( new Signature( document, signImage ));
                        refreshUI(patient);
                        sendResponse(exchange, 200, "OK");
                    } else {
                        sendResponse(exchange, 404, "Patient nicht gefunden.");
                    }
                } else {
                    sendResponse(exchange, 400, "Ungültige Bilddaten.");
                }
            }
        }

        private String getHtmlTemplate() {
            try ( InputStream in = getClass().getResourceAsStream( "/inko/PNGSigner.html" )) {
                if (in == null) {
                    System.err.println("Fehler: PNGSigner.html nicht gefunden.");
                    return "<h1>Server-Fehler: Vorlage fehlt.</h1>";
                }
                try ( BufferedReader reader = new BufferedReader( new InputStreamReader( in, UTF_8 ))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while (( line = reader.readLine() ) != null ) {
                        sb.append(line).append("\n");
                    }
                    return sb.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "<h1>Server-Fehler beim Laden der Vorlage.</h1>";
            }
        }
    }

}
