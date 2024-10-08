package model;

import manager.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    // Проверка равенства задач при одинаковых полях и ID
    @Test
    public void testTasksEqualityWithSameIdAndFields() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        taskManager.createTask(task1);
        int id = task1.getId();

        Task task2 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        // Устанавливаем тот же ID для второго задания
        task2.setId(id);

        // Пытаемся добавить вторую задачу
        boolean result = taskManager.createTask(task2);

        // добавление не произойдет
        assertFalse(result, "Задача с существующим ID не должна быть добавлена.");

        // Проверяем, что в менеджере только первая задача
        Task retrievedTask = taskManager.getTaskById(id);
        assertEquals(task1, retrievedTask, "В менеджере должна остаться исходная задача с данным ID.");
    }

    // Проверка неравенства задач с разными ID
    @Test
    public void testTasksInequalityWithDifferentId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        taskManager.createTask(task1);

        Task task2 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Задачи должны иметь разные ID.");
        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны.");
    }

    // Проверка работы сеттеров Task
    @Test
    public void testSetters() {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setName("Updated Task");
        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.IN_PROGRESS);

        assertEquals("Updated Task", task.getName());
        assertEquals("Updated Description", task.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }
}
