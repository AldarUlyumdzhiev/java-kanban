package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    public void setUp() {
        taskManager = Managers.getDefault();
    }

    // Проверка добавления задач разного типа и поиска их по ID
    @Test
    public void testAddAndGetTasksById() {
        Task task = new Task("Task 1", "Task Description", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Epic Description");

        taskManager.createTask(task);
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask);

        assertEquals(task, taskManager.getTaskById(task.getId()), "Task должен быть доступен по его ID.");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Epic должен быть доступен по его ID.");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Subtask должен быть доступен по его ID.");
    }

    // Проверка, что задачи с заданным ID и сгенерированным ID не конфликтуют
    @Test
    public void testNoIdConflict() {
        Task task = new Task("Task 1", "Task Description", TaskStatus.NEW);
        task.setId(100);
        taskManager.createTask(task);

        Task newTask = new Task("New Task", "New Task Description", TaskStatus.NEW);
        taskManager.createTask(newTask);

        assertNotEquals(100, newTask.getId(), "Генерируемый ID не должен конфликтовать с существующими ID.");
    }

    // Проверка неизменности задачи при добавлении в менеджер
    @Test
    public void testTaskModificationAfterAdding() {
        Task task = new Task("Task 1", "Task Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());
        savedTask.setName("Updated Task Name");

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated Task Name", updatedTask.getName(), "Имя задачи должно обновиться после изменения.");
    }

    // Проверка работы истории просмотров
    @Test
    public void testHistoryManagerFunctionality() {
        Task task1 = new Task("Task 1", "Task Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Task Description 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Просмотрим задачи
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Проверим историю просмотров
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи.");
        assertEquals(task1, history.get(0), "Первая задача в истории должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача в истории должна быть task2.");
    }
}
