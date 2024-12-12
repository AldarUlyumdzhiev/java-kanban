package manager;

import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    // Проверка добавления задач в историю
    @Test
    public void testAddTasksToHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals(task1, history.getFirst(), "Первая задача должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача должна быть task2.");
    }

    // Проверка отсутствия дубликатов в истории
    @Test
    public void testNoDuplicatesInHistory() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История не должна содержать дубликаты.");
    }

    // Проверка удаления задачи из истории
    @Test
    public void testRemoveTaskFromHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История должна содержать 1 задачу после удаления.");
        assertEquals(task2, history.getFirst(), "Оставшаяся задача должна быть task2.");
    }

    // Проверка порядка задач при повторном добавлении
    @Test
    public void testTaskOrderAfterReAdding() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setId(2);

        Task task3 = new Task("Задача 3", "Описание 3", TaskStatus.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Повторно добавляем task2
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "История должна содержать 3 задачи.");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1.");
        assertEquals(task3, history.get(1), "Вторая задача должна быть task3.");
        assertEquals(task2, history.get(2), "Третья задача должна быть task2.");
    }

    // Проверка поведения при пустой истории
    @Test
    public void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой, если задачи не добавлены.");
    }

    // Проверка удаления задачи из начала истории
    @Test
    public void testRemoveFirstTaskFromHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        // Удаляем первую задачу
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "После удаления первой задачи история должна содержать 1 задачу.");
        assertEquals(task2, history.getFirst(), "Оставшаяся задача должна быть task2.");
    }

    // Проверка удаления задачи из середины истории
    @Test
    public void testRemoveMiddleTaskFromHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setId(2);

        Task task3 = new Task("Задача 3", "Описание 3", TaskStatus.NEW);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем вторую задачу
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "После удаления второй задачи история должна содержать 2 задачи.");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1.");
        assertEquals(task3, history.get(1), "Вторая задача должна быть task3.");
    }

    // Проверка удаления задачи из конца истории
    @Test
    public void testRemoveLastTaskFromHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        // Удаляем последнюю задачу
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "После удаления последней задачи история должна содержать 1 задачу.");
        assertEquals(task1, history.getFirst(), "Оставшаяся задача должна быть task1.");
    }
}
