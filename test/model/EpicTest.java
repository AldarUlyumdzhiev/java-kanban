package model;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.time.Duration;

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

    // Проверка расчёта продолжительности и времени старта/завершения эпика
    @Test
    public void testEpicTimeCalculation() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик с подзадачами", "Описание эпика");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW);
        subtask1.setStartTime(LocalDateTime.of(2023, 12, 1, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(120));
        taskManager.createSubtask(subtask1, epic);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW);
        subtask2.setStartTime(LocalDateTime.of(2023, 12, 1, 13, 0));
        subtask2.setDuration(Duration.ofMinutes(90));
        taskManager.createSubtask(subtask2, epic);

        // Проверяем продолжительность эпика
        assertEquals(Duration.ofMinutes(210), epic.getDuration(), "Продолжительность эпика должна быть равна сумме подзадач.");
        // Проверяем время начала эпика
        assertEquals(LocalDateTime.of(2023, 12, 1, 10, 0), epic.getStartTime(), "Время начала эпика должно совпадать с началом первой подзадачи.");
        // Проверяем время завершения эпика
        assertEquals(LocalDateTime.of(2023, 12, 1, 14, 30), epic.getEndTime(), "Время завершения эпика должно совпадать с концом последней подзадачи.");
    }

    // Проверка обновления статуса эпика на основе статусов подзадач
    @Test
    public void testEpicStatusUpdate() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик со статусами", "Описание эпика");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);
        taskManager.createSubtask(subtask2, epic);

        // Все подзадачи NEW
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Если все подзадачи NEW, статус эпика должен быть NEW.");

        // Одна подзадача IN_PROGRESS
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Если хотя бы одна подзадача IN_PROGRESS, статус эпика должен быть IN_PROGRESS.");

        // Все подзадачи DONE
        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Если все подзадачи DONE, статус эпика должен быть DONE.");
    }
}
