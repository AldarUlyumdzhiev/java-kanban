package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;



public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public TaskHandler(TaskManager manager) {
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
            // Получить все задачи
            List<Task> tasks = manager.getAllTasks();
            String json = gson.toJson(tasks);
            sendText(exchange, json, 200);
        } else {
            // Берем id из URI
            Integer taskId = parseIdFromQuery(query);
            if (taskId == null) {
                sendNotFound(exchange, "Вы указали неправильный id");
                return;
            }
            Task task = manager.getTaskById(taskId);
            if (task == null) {
                sendNotFound(exchange, "Задача с id = " + taskId + " не найдена.");
            } else {
                sendText(exchange, gson.toJson(task), 200);
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        // Считываем JSON из body
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        boolean isNew = (task.getId() == 0); // Если id=0 или пустое, то создаём новую задачу
        if (isNew) {
            boolean created = manager.createTask(task);
            if (created) {
                sendText(exchange, gson.toJson(task), 201);
            } else {
                sendError(exchange, "Ошибка при создании задачи.");
            }
        } else {
            boolean updated = manager.updateTask(task);
            if (updated) {
                sendText(exchange, gson.toJson(task), 200);
            } else {
                sendNotFound(exchange, "Такой задачи не существует: " + task.getId());
            }
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
        if (query == null) {
            // Удалить все задачи
            manager.deleteAllTasks();
            sendText(exchange, "Все задачи удалены.", 200);
        } else {
            Integer taskId = parseIdFromQuery(query);
            if (taskId == null) {
                sendNotFound(exchange, "Вы ввели неправильный id задачи.");
                return;
            }
            manager.deleteTaskById(taskId);
            sendText(exchange, "Задача " + taskId + " удалена.", 200);
        }
    }

    private Integer parseIdFromQuery(String query) {
        // Ищем параметр id=число
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
