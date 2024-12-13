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

import static org.junit.jupiter.api.Assertions.*;



public class PrioritizedHandlerTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public PrioritizedHandlerTest() throws IOException {
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
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task #1", "Description for Task #1", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Task #2", "Description for Task #2", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.now().plusMinutes(40));
        task2.setDuration(Duration.ofMinutes(20));
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Сервер должен вернуть код 200");

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length, "Ожидаются две задачи в ответе");
        assertEquals("Task #1", tasks[0].getName(), "Первая задача должна быть Task #1");
    }
}
