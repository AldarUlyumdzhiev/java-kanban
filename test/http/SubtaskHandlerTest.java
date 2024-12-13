package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
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



public class SubtaskHandlerTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public SubtaskHandlerTest() throws IOException {
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic #1", "Description for Epic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask #1", "Description for Subtask", TaskStatus.NEW);
        subtask.setEpicId(epic.getId());
        subtask.setDuration(Duration.ofMinutes(30));
        subtask.setStartTime(LocalDateTime.now());

        String subtaskJson = gson.toJson(subtask);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Сервер должен вернуть код 201");

        assertEquals(1, manager.getAllSubtasks().size(), "Подзадача не добавлена");
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic #1", "Description for Epic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask #1", "Description for Subtask", TaskStatus.NEW);
        subtask.setEpicId(epic.getId());
        manager.createSubtask(subtask, epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Сервер должен вернуть код 200");

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length, "Ожидается одна подзадача в ответе");
    }

    @Test
    public void testDeleteSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic #1", "Description for Epic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask #1", "Description for Subtask", TaskStatus.NEW);
        subtask.setEpicId(epic.getId());
        manager.createSubtask(subtask, epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Сервер должен вернуть код 200");

        assertTrue(manager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
    }
}