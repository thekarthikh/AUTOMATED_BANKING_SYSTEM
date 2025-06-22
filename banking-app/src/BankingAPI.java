import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;

public class BankingAPI {
    private BankingService bankingService;
    private Gson gson;

    public BankingAPI() {
        this.bankingService = new BankingService();
        this.gson = new Gson();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/accounts", new AccountHandler());
        server.createContext("/api/deposit", new DepositHandler());
        server.createContext("/api/withdraw", new WithdrawHandler());
        server.createContext("/api/transfer", new TransferHandler());
        server.createContext("/api/balance", new BalanceHandler());
        server.createContext("/api/transactions", new TransactionHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Banking API Server started on port 8080");
    }

    private class AccountHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                Map<String, Object> data = gson.fromJson(requestBody, Map.class);

                String accountNumber = bankingService.createAccount(
                    (String) data.get("name"),
                    (String) data.get("email"),
                    new BigDecimal(data.get("initialDeposit").toString()),
                    (String) data.get("accountType")
                );

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("accountNumber", accountNumber != null ? accountNumber : "");
                String response = gson.toJson(responseMap);
                sendResponse(exchange, 200, response);
            } else if ("GET".equals(exchange.getRequestMethod())) {
                String response = gson.toJson(bankingService.getAllAccounts());
                sendResponse(exchange, 200, response);
            }
        }
    }

    private class DepositHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                Map<String, Object> data = gson.fromJson(requestBody, Map.class);

                boolean success = bankingService.deposit(
                    (String) data.get("accountNumber"),
                    new BigDecimal(data.get("amount").toString()),
                    (String) data.get("description")
                );

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", success);
                String response = gson.toJson(responseMap);
                sendResponse(exchange, 200, response);
            }
        }
    }

    private class WithdrawHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                Map<String, Object> data = gson.fromJson(requestBody, Map.class);

                boolean success = bankingService.withdraw(
                    (String) data.get("accountNumber"),
                    new BigDecimal(data.get("amount").toString()),
                    (String) data.get("description")
                );

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", success);
                String response = gson.toJson(responseMap);
                sendResponse(exchange, 200, response);
            }
        }
    }

    private class TransferHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                Map<String, Object> data = gson.fromJson(requestBody, Map.class);

                boolean success = bankingService.transfer(
                    (String) data.get("fromAccount"),
                    (String) data.get("toAccount"),
                    new BigDecimal(data.get("amount").toString()),
                    (String) data.get("description")
                );

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", success);
                String response = gson.toJson(responseMap);
                sendResponse(exchange, 200, response);
            }
        }
    }

    private class BalanceHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String accountNumber = query.split("=")[1];

                BigDecimal balance = bankingService.getBalance(accountNumber);
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("balance", balance);
                String response = gson.toJson(responseMap);
                sendResponse(exchange, 200, response);
            }
        }
    }

    private class TransactionHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String accountNumber = query.split("=")[1];

                String response = gson.toJson(bankingService.getTransactionHistory(accountNumber));
                sendResponse(exchange, 200, response);
            }
        }
    }

    private void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private String getRequestBody(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static void main(String[] args) {
        try {
            new BankingAPI().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
