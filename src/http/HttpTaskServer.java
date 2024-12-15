package http;

import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;



public class HttpTaskServer {
    private final HttpServer server;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Создаю хендлеры
        server.createContext("/tasks", new TaskHandler(taskManager));
        server.createContext("/subtasks", new SubtaskHandler(taskManager));
        server.createContext("/epics", new EpicHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        server.start();
        System.out.println("Http сервер работает на порту 8080");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Http сервер остановлен");
    }
}
