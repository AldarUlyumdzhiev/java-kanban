package model;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    // Проверка равенства эпиков с одинаковыми ID и полями
    @Test
    public void testEpicsEqualityWithSameIdAndFields() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic1 = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic1);
        int id = epic1.getId();

        Epic epic2 = new Epic("Epic 1", "Description 1");
        // Устанавливаем тот же ID для второго эпика
        epic2.setId(id);

        // Пытаемся добавить второй эпик
        boolean result = taskManager.createEpic(epic2);

        // добавление не произойдет
        assertFalse(result, "Epic с существующим ID не должен быть добавлен.");

        // Проверяем, что в менеджере только первый эпик
        Epic retrievedEpic = taskManager.getEpicById(id);
        assertEquals(epic1, retrievedEpic, "В менеджере должен остаться исходный Epic с данным ID.");
    }

    // Проверка неравенства эпиков с разными ID
    @Test
    public void testEpicsInequalityWithDifferentId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic1 = new Epic("Epic 1", "Description 1");
        taskManager.createEpic(epic1);

        Epic epic2 = new Epic("Epic 2", "Description 2");
        taskManager.createEpic(epic2);

        assertNotEquals(epic1.getId(), epic2.getId(), "Epics должны иметь разные ID.");
        assertNotEquals(epic1, epic2, "Epics с разными ID не должны быть равны.");
    }

    // Проверка, что Epic не может добавить сам себя как подзадачу
    @Test
    public void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic("Epic 1", "Description 1");
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        taskManager.createEpic(epic);

        epic.addSubtaskId(epic.getId());

        assertFalse(epic.getSubtaskIds().contains(epic.getId()), "Epic не может добавить сам себя как подзадачу.");
    }
}
