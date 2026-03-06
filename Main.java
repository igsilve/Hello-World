import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream("app.config")) {
            config.load(in);
        }

        int port = Integer.parseInt(config.getProperty("server.port", "8080"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            String response = """
                <!DOCTYPE html>
                <html>
                <head><title>Hello App</title></head>
                <body>
                  <h1>What is your name?</h1>
                  <form method="POST" action="/greet">
                    <input type="text" name="name" placeholder="Enter your name" required />
                    <button type="submit">Submit</button>
                  </form>
                </body>
                </html>
                """;
            sendResponse(exchange, response);
        });

        server.createContext("/greet", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            String name = "World";
            for (String param : body.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && kv[0].equals("name")) {
                    name = java.net.URLDecoder.decode(kv[1], "UTF-8");
                }
            }
            String response = """
                <!DOCTYPE html>
                <html>
                <head><title>Hello App</title></head>
                <body>
                  <h1>Hello, %s!</h1>
                  <a href="/">Go back</a>
                </body>
                </html>
                """.formatted(name);
            sendResponse(exchange, response);
        });

        server.start();
        System.out.println("Server started at http://localhost:" + port);
    }

    private static void sendResponse(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
