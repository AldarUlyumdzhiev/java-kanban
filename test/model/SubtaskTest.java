package model;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    // Проверка равенства подзадач с одинаковыми ID и полями
    @Test
    public void testEqualSameId() {
        TaskManager taskManager = new InMemoryTaskManager();

        // Создаём Epic
        Epic epic = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic);

        // Создаём первую подзадачу
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);
        int subtaskId = subtask1.getId();

        // Создаём вторую подзадачу и устанавливаем тот же ID
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW);
        subtask2.setId(subtaskId);

        // Пытаемся добавить вторую подзадачу
        boolean result = taskManager.createSubtask(subtask2, epic);

        // Ожидаем, что добавление не произойдёт, так как ID уже существует
        assertFalse(result, "Подзадача с существующим ID не должна быть добавлена.");

        // Проверяем, что в менеджере по-прежнему только первая подзадача
        Subtask retrievedSubtask = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtask1, retrievedSubtask, "В менеджере должна остаться исходная подзадача с данным ID.");
    }

    // Проверка неравенства подзадач с разными ID
    @Test
    public void testInequalDiffId() {
        TaskManager taskManager = new InMemoryTaskManager();

        // Создаём Epic
        Epic epic = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic);

        // Создаём две подзадачи
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.IN_PROGRESS);
        taskManager.createSubtask(subtask2, epic);

        assertNotEquals(subtask1.getId(), subtask2.getId(), "Подзадачи должны иметь разные ID.");
        assertNotEquals(subtask1, subtask2, "Подзадачи с разными ID не должны быть равны.");
    }

    // Проверка, что подзадача не может быть добавлена с несуществующим эпиком
    @Test
    public void testSubtaskForNonexistentEpic() {
        TaskManager taskManager = new InMemoryTaskManager();

        // Создаём подзадачу
        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW);

        // Создаём эпик, но не добавляем его в менеджер (несуществующий эпик)
        Epic nonexistentEpic = new Epic("Nonexistent Epic", "Description");

        // Пытаемся добавить подзадачу с несуществующим эпиком
        boolean result = taskManager.createSubtask(subtask, nonexistentEpic);

        // Ожидаем, что добавление не произойдёт
        assertFalse(result, "Подзадача с несуществующим эпиком не должна быть добавлена.");

        // Проверяем, что подзадача не добавлена в менеджер
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не должна существовать в менеджере.");
    }
}
