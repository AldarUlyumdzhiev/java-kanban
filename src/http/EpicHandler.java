package http;

import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;



public class EpicHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public EpicHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();

            switch (method) {
                case "GET":
                    handleGet(exchange, query);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, query);
                    break;
                default:
                    sendText(exchange, "такого метода нет", 405);
                    break;
            }
        } catch (Exception e) {
            sendError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String query) throws IOException {
        if (query == null) {
            List<Epic> epics = manager.getAllEpics();
            sendText(exchange, gson.toJson(epics), 200);
        } else {
            Integer epicId = parseIdFromQuery(query);
            if (epicId == null) {
                sendNotFound(exchange, "Неверный параметр 'id'.");
                return;
            }
            Epic epic = manager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange, "Эпик с id " + epicId + " не найден.");
            } else {
                sendText(exchange, gson.toJson(epic), 200);
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        boolean isNew = (epic.getId() == 0);
        if (isNew) {
            boolean created = manager.createEpic(epic);
            if (created) {
                sendText(exchange, gson.toJson(epic), 201);
            } else {
                sendError(exchange, "Не удалось создать эпик.");
            }
        } else {
            boolean updated = manager.updateEpic(epic);
            if (updated) {
                sendText(exchange, gson.toJson(epic), 200);
            } else {
                sendNotFound(exchange, "Эпик с id " + epic.getId() + " не существует.");
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
        if (query == null) {
            manager.deleteAllEpics();
            sendText(exchange, "Все эпики удалены.", 200);
        } else {
            Integer id = parseIdFromQuery(query);
            if (id == null) {
                sendNotFound(exchange, "Неверный параметр 'id'.");
                return;
            }
            manager.deleteEpicById(id);
            sendText(exchange, "Эпик " + id + " удалён.", 200);
        }
    }

    private Integer parseIdFromQuery(String query) {
        if (query.startsWith("id=")) {
            String strId = query.substring(3);
            try {
                return Integer.parseInt(strId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
