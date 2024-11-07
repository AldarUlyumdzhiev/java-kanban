package model;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    // Проверка равенства эпиков с одинаковыми ID и полями
    @Test
    public void testEqualSameId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        taskManager.createEpic(epic1);
        int id = epic1.getId();

        Epic epic2 = new Epic("Эпик 1", "Описание 1");
        // Устанавливаем тот же ID для второго эпика
        epic2.setId(id);

        // Пытаемся добавить второй эпик
        boolean result = taskManager.createEpic(epic2);

        // Добавление не должно произойти
        assertFalse(result, "Эпик с существующим ID не должен быть добавлен.");

        // Проверяем, что в менеджере только первый эпик
        Epic retrievedEpic = taskManager.getEpicById(id);
        assertEquals(epic1, retrievedEpic, "В менеджере должен остаться исходный эпик с данным ID.");
    }

    // Проверка неравенства эпиков с разными ID
    @Test
    public void testInequalDiffId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        taskManager.createEpic(epic1);

        Epic epic2 = new Epic("Эпик 2", "Описание 2");
        taskManager.createEpic(epic2);

        assertNotEquals(epic1.getId(), epic2.getId(), "Эпики должны иметь разные ID.");
        assertNotEquals(epic1, epic2, "Эпики с разными ID не должны быть равны.");
    }

    // Проверка, что эпик не может добавить сам себя как подзадачу
    @Test
    public void testCantAddItselfAsSubtask() {
        Epic epic = new Epic("Эпик 1", "Описание 1");
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        taskManager.createEpic(epic);

        epic.addSubtaskId(epic.getId());

        assertFalse(epic.getSubtaskIds().contains(epic.getId()), "Эпик не может добавить сам себя как подзадачу.");
    }
}
