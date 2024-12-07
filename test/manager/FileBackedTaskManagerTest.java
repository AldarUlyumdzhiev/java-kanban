package manager;

import model.Task;
import model.Epic;
import model.Subtask;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    // Проверка сохранения и загрузки задачи из файла
    @Test
    public void testSaveAndLoadTasks() throws IOException {
        File file = new File("test_save_load.csv");
        TaskManager taskManager = new FileBackedTaskManager(file);

        // Создание задачи
        Task task = new Task("Задача 1", "Описание задачи", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2023, 12, 1, 10, 0));
        taskManager.createTask(task);

        // Сохранение и загрузка
        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверка задачи
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertEquals(task, loadedTask, "Загруженная задача должна совпадать с сохранённой.");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время старта должно совпадать.");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Продолжительность должна совпадать.");
    }

    // Проверка сохранения и загрузки эпика с подзадачами из файла
    @Test
    public void testSaveAndLoadEpicWithSubtasks() throws IOException {
        File file = new File("test_epic_with_subtasks.csv");
        TaskManager taskManager = new FileBackedTaskManager(file);

        // Создание эпика и подзадач
        Epic epic = new Epic("Эпик 1", "Описание эпика");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи", TaskStatus.NEW);
        subtask1.setDuration(Duration.ofMinutes(30));
        subtask1.setStartTime(LocalDateTime.of(2023, 12, 1, 10, 0));
        taskManager.createSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи", TaskStatus.DONE);
        subtask2.setDuration(Duration.ofMinutes(45));
        subtask2.setStartTime(LocalDateTime.of(2023, 12, 1, 11, 0));
        taskManager.createSubtask(subtask2, epic);

        // Сохранение и загрузка
        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверка эпика
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(epic, loadedEpic, "Загруженный эпик должен совпадать с сохранённым.");
        assertEquals(epic.getSubtaskIds().size(), loadedEpic.getSubtaskIds().size(),
                "Количество подзадач должно совпадать.");

        // Проверка подзадач
        Subtask loadedSubtask1 = loadedManager.getSubtaskById(subtask1.getId());
        Subtask loadedSubtask2 = loadedManager.getSubtaskById(subtask2.getId());
        assertEquals(subtask1, loadedSubtask1, "Первая подзадача должна совпадать.");
        assertEquals(subtask2, loadedSubtask2, "Вторая подзадача должна совпадать.");
    }

    // Проверка обработки некорректного файла
    @Test
    public void testLoadFromCorruptedFile() {
        File file = new File("corrupted_file.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Некорректные данные");
        } catch (IOException e) {
            fail("Ошибка при записи тестового файла.");
        }

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(file),
                "Должно выбрасываться исключение при загрузке повреждённого файла.");
    }

    // Проверка сохранения и загрузки истории задач из файла
    @Test
    public void testHistorySaveAndLoad() throws IOException {
        File file = new File("test_history.csv");
        TaskManager taskManager = new FileBackedTaskManager(file);

        // Создание задач
        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2", TaskStatus.IN_PROGRESS);
        taskManager.createTask(task2);

        // Просмотр задач для добавления в историю
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Сохранение и загрузка
        TaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверка истории
        List<Task> history = loadedManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи.");
        assertEquals(task1, history.getFirst(), "Первая задача в истории должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача в истории должна быть task2.");
    }
}
