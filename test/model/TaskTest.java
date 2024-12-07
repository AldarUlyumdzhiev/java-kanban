package model;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    // Проверка равенства задач при одинаковых полях и ID
    @Test
    public void testEqualSameId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        taskManager.createTask(task1);
        int id = task1.getId();

        Task task2 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        // Устанавливаем тот же ID для второй задачи
        task2.setId(id);

        // Пытаемся добавить вторую задачу
        boolean result = taskManager.createTask(task2);

        // Добавление не должно произойти
        assertFalse(result, "Задача с существующим ID не должна быть добавлена.");

        // Проверяем, что в менеджере только первая задача
        Task retrievedTask = taskManager.getTaskById(id);
        assertEquals(task1, retrievedTask, "В менеджере должна остаться исходная задача с данным ID.");
    }

    // Проверка неравенства задач с разными ID
    @Test
    public void testInequalDiffId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        taskManager.createTask(task1);

        Task task2 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь разные ID.");
        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны.");
    }

    // Проверка работы сеттеров класса Task
    @Test
    public void testSetters() {
        Task task = new Task("Тестовая задача", "Тестовое описание", TaskStatus.NEW);
        task.setName("Обновлённая задача");
        task.setDescription("Обновлённое описание");
        task.setStatus(TaskStatus.IN_PROGRESS);

        assertEquals("Обновлённая задача", task.getName());
        assertEquals("Обновлённое описание", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    // Проверка отсутствия пересечения задач
    @Test
    public void testTaskNoOverlap() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2023, 12, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(task1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2023, 12, 1, 11, 0));
        task2.setDuration(Duration.ofMinutes(60));
        taskManager.createTask(task2);

        assertEquals(task1, taskManager.getTaskById(task1.getId()), "Первая задача должна быть сохранена.");
        assertEquals(task2, taskManager.getTaskById(task2.getId()), "Вторая задача должна быть сохранена.");
    }
}
