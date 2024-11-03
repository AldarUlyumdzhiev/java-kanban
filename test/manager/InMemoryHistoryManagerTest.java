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
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача должна быть task2.");
    }

    // Проверка отсутствия дубликатов в истории
    @Test
    public void testNoDuplicatesInHistory() {
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "История не должна содержать дубликаты.");
    }

    // Проверка удаления задач из истории
    @Test
    public void testRemoveTaskFromHistory() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
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
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        task2.setId(2);

        Task task3 = new Task("Task 3", "Description 3", TaskStatus.NEW);
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
}
