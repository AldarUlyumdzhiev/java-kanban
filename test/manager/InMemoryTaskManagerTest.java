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

    // Проверка добавления и получения задач по ID
    @Test
    public void testAddAndGetTasksById() {
        Task task = new Task("Задача 1", "Описание задачи", TaskStatus.NEW);
        Epic epic = new Epic("Эпик 1", "Описание эпика");

        taskManager.createTask(task);
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи", TaskStatus.NEW);
        taskManager.createSubtask(subtask, epic);

        assertEquals(task, taskManager.getTaskById(task.getId()), "Задача должна быть доступна по своему ID.");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Эпик должен быть доступен по своему ID.");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Подзадача должна быть доступна по своему ID.");
    }

    // Проверка отсутствия конфликтов сгенерированных ID
    @Test
    public void testNoIdConflict() {
        Task task = new Task("Задача 1", "Описание задачи", TaskStatus.NEW);
        task.setId(100);
        taskManager.createTask(task);

        Task newTask = new Task("Новая задача", "Новое описание", TaskStatus.NEW);
        taskManager.createTask(newTask);

        assertNotEquals(100, newTask.getId(), "Сгенерированный ID не должен конфликтовать с существующими ID.");
    }

    // Проверка обновления задачи
    @Test
    public void testTaskModify() {
        Task task = new Task("Задача 1", "Описание задачи", TaskStatus.NEW);
        taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());
        savedTask.setName("Обновлённое название задачи");
        taskManager.updateTask(savedTask);

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals("Обновлённое название задачи", updatedTask.getName(), "Имя задачи должно быть обновлено.");
    }

    // Проверка менеджера истории
    @Test
    public void testHistoryManager() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Просмотр задач
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Проверка истории
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи.");
        assertEquals(task1, history.get(0), "Первая задача в истории должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача в истории должна быть task2.");
    }

    // Проверка менеджера истории с удалением задачи
    @Test
    public void testHistoryManagerWithTaskDeletion() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Просмотр задач
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Удаление первой задачи
        taskManager.deleteTaskById(task1.getId());

        // Проверка истории
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "После удаления история должна содержать одну задачу.");
        assertEquals(task2, history.get(0), "Оставшаяся задача должна быть task2.");
    }

    // Проверка удаления подзадачи из эпика
    @Test
    public void testSubtaskDeletionFromEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);

        // Убедимся, что подзадача добавлена в эпик
        assertTrue(epic.getSubtaskIds().contains(subtask1.getId()), "Эпик должен содержать ID подзадачи.");

        // Удаление подзадачи
        taskManager.deleteSubtaskById(subtask1.getId());

        // Убедимся, что подзадача удалена из эпика
        assertFalse(epic.getSubtaskIds().contains(subtask1.getId()), "Эпик не должен содержать удалённый ID подзадачи.");
    }

    // Проверка того, что удаление эпика удаляет его подзадачи
    @Test
    public void testEpicDeletionRemovesSubtasks() {
        Epic epic = new Epic("Эпик 1", "Описание эпика");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);
        taskManager.createSubtask(subtask2, epic);

        // Удаление эпика
        taskManager.deleteEpicById(epic.getId());

        // Убедимся, что подзадачи также удалены
        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна быть удалена вместе с эпиком.");
        assertNull(taskManager.getSubtaskById(subtask2.getId()), "Подзадача 2 должна быть удалена вместе с эпиком.");
    }

    // Проверка изменения ассоциации подзадачи с эпиком
    @Test
    public void testSubtaskEpicIdChangeThroughManager() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", TaskStatus.NEW);
        taskManager.createSubtask(subtask, epic1);

        boolean updated = ((InMemoryTaskManager) taskManager).updateSubtaskEpic(subtask, epic2);
        assertTrue(updated, "Должно успешно обновиться ассоциация подзадачи с эпиком.");

        assertFalse(epic1.getSubtaskIds().contains(subtask.getId()), "Подзадача не должна быть в старом эпике.");
        assertTrue(epic2.getSubtaskIds().contains(subtask.getId()), "Подзадача должна быть в новом эпике.");
    }
}
