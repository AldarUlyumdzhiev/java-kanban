package model;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    // Проверка равенства подзадач с одинаковыми ID и полями
    @Test
    public void testSubtasksEqualityWithSameIdAndFields() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        // Создаем Epic
        Epic epic = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic);

        // Создаем первую подзадачу
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        int subtaskId = subtask1.getId();

        // Создаем вторую подзадачу с тем же ID
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        subtask2.setId(subtaskId);

        // Пытаемся добавить вторую подзадачу
        boolean result = taskManager.createSubtask(subtask2);

        // Ожидаем, что добавление не произойдет
        assertFalse(result, "Подзадача с существующим ID не должна быть добавлена.");

        // Проверяем, что в менеджере по-прежнему только первая подзадача
        Subtask retrievedSubtask = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtask1, retrievedSubtask, "В менеджере должна остаться исходная подзадача с данным ID.");
    }

    // Проверка неравенства подзадач с разными ID
    @Test
    public void testSubtasksInequalityWithDifferentId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        // Создаем Epic
        Epic epic = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic);

        // Создаем две подзадачи
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.IN_PROGRESS, epic.getId());
        taskManager.createSubtask(subtask2);

        assertNotEquals(subtask1.getId(), subtask2.getId(), "Подзадачи должны иметь разные ID.");
        assertNotEquals(subtask1, subtask2, "Подзадачи с разными ID не должны быть равны.");
    }

    // Проверка, что Subtask не может быть своим собственным эпиком
    @Test
    public void testSubtaskCannotBeCreatedForNonexistentEpic() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        // Пытаемся создать Subtask с несуществующим epicId
        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 999);

        // Пытаемся добавить подзадачу
        boolean result = taskManager.createSubtask(subtask);

        // Ожидаем, что добавление не произойдет
        assertFalse(result, "Подзадача с несуществующим epicId не должна быть добавлена.");

        // Проверяем, что подзадача не добавлена в менеджер
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача не должна существовать в менеджере.");
    }
}
