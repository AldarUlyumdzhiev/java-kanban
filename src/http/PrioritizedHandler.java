package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;



public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, "такого метода нет.", 405);
                return;
            }
            List<Task> prioritized = manager.getPrioritizedTasks();
            sendText(exchange, gson.toJson(prioritized), 200);
        } catch (Exception e) {
            sendError(exchange, "Произошла ошибка при получении задач с приоритетом.");
        }
    }
}
