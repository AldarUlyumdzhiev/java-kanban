package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
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



public class EpicHandlerTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public EpicHandlerTest() throws IOException {
        taskServer = new HttpTaskServer(manager);
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic #1", "Description for Epic");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Сервер должен вернуть код 201");

        assertEquals(1, manager.getAllEpics().size(), "Эпик не добавлен");
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic #1", "Description for Epic");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Сервер должен вернуть код 200");

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(1, epics.length, "Ожидается один эпик в ответе");
    }

    @Test
    public void testDeleteEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic #1", "Description for Epic");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Сервер должен вернуть код 200");

        assertTrue(manager.getAllEpics().isEmpty(), "Все эпики должны быть удалены");
    }
}