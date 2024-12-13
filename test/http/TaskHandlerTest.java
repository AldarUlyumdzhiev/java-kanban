package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskHandlerTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public TaskHandlerTest() throws IOException {
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // Создаём задачу
        Task task = new Task("Test Task", "Testing task creation", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.now());

        // Конвертируем задачу в JSON
        String taskJson = gson.toJson(task);

        // Создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // Отправляем запрос и проверяем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Ответ сервера не соответствует ожидаемому");

        // Проверяем, что задача добавлена в менеджер
        List<Task> tasks = manager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.size(), "Неверное количество задач");
        assertEquals("Test Task", tasks.getFirst().getName(), "Имя задачи не совпадает");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        // Добавляем задачу в менеджер
        Task task = new Task("Test Task", "Testing task retrieval", TaskStatus.NEW);
        manager.createTask(task);

        // Создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        // Отправляем запрос и проверяем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Ответ сервера не соответствует ожидаемому");

        // Проверяем содержимое ответа
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Ответ не содержит задач");
        assertEquals(1, tasks.length, "Неверное количество задач в ответе");
        assertEquals("Test Task", tasks[0].getName(), "Имя задачи не совпадает");
    }

    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        // Добавляем задачу в менеджер
        Task task = new Task("Test Task", "Testing task deletion", TaskStatus.NEW);
        manager.createTask(task);

        // Создаём HTTP-клиент и запрос на удаление всех задач
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        // Отправляем запрос и проверяем ответ
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Ответ сервера не соответствует ожидаемому");

        // Проверяем, что задачи удалены из менеджера
        List<Task> tasks = manager.getAllTasks();
        assertNotNull(tasks, "Список задач должен быть пустым");
        assertTrue(tasks.isEmpty(), "Задачи не удалены");
    }
}
