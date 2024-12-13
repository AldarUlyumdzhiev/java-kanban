package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;



public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public SubtaskHandler(TaskManager manager) {
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
                    sendText(exchange, "такого метода нет.", 405);
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String query) throws IOException {
        if (query == null) {
            List<Subtask> subtasks = manager.getAllSubtasks();
            String json = gson.toJson(subtasks);
            sendText(exchange, json, 200);
        } else {
            Integer id = parseIdFromQuery(query);
            if (id == null) {
                sendNotFound(exchange, "Неверный параметр 'id'.");
                return;
            }
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange, "Подзадача с id " + id + " не найдена.");
            } else {
                sendText(exchange, gson.toJson(subtask), 200);
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        boolean isNew = (subtask.getId() == 0);
        if (isNew) {
            int epicId = subtask.getEpicId();
            Epic epic = manager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange, "Эпик с id " + epicId + " не найден. Невозможно создать подзадачу.");
                return;
            }
            boolean created = manager.createSubtask(subtask, epic);
            if (created) {
                sendText(exchange, gson.toJson(subtask), 201);
            } else {
                sendError(exchange, "Не удалось создать подзадачу.");
            }
        } else {
            boolean updated = manager.updateSubtask(subtask);
            if (updated) {
                sendText(exchange, gson.toJson(subtask), 200);
            } else {
                sendNotFound(exchange, "Подзадача с id " + subtask.getId() + " не существует.");
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
        if (query == null) {
            manager.deleteAllSubtasks();
            sendText(exchange, "Все подзадачи удалены.", 200);
        } else {
            Integer id = parseIdFromQuery(query);
            if (id == null) {
                sendNotFound(exchange, "Неверный параметр 'id'.");
                return;
            }
            manager.deleteSubtaskById(id);
            sendText(exchange, "Подзадача " + id + " удалена.", 200);
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
