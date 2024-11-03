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

    // Test adding and retrieving tasks by ID
    @Test
    public void testAddAndGetTasksById() {
        Task task = new Task("Task 1", "Task Description", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Epic Description");

        taskManager.createTask(task);
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", TaskStatus.NEW);
        taskManager.createSubtask(subtask, epic);

        assertEquals(task, taskManager.getTaskById(task.getId()), "Task should be retrievable by its ID.");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Epic should be retrievable by its ID.");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Subtask should be retrievable by its ID.");
    }

    // Test that generated IDs do not conflict
    @Test
    public void testNoIdConflict() {
        Task task = new Task("Task 1", "Task Description", TaskStatus.NEW);
        task.setId(100);
        taskManager.createTask(task);

        Task newTask = new Task("New Task", "New Task Description", TaskStatus.NEW);
        taskManager.createTask(newTask);

        assertNotEquals(100, newTask.getId(), "Generated ID should not conflict with existing IDs.");
    }

    // Test task update
    @Test
    public void testTaskModify() {
        Task task = new Task("Task 1", "Task Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(task.getId());
        savedTask.setName("Updated Task Name");
        taskManager.updateTask(savedTask);

        Task updatedTask = taskManager.getTaskById(task.getId());
        assertEquals("Updated Task Name", updatedTask.getName(), "Task name should be updated.");
    }

    // Test history manager
    @Test
    public void testHistoryManager() {
        Task task1 = new Task("Task 1", "Task Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Task Description 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // View tasks
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Check history
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "History should contain two tasks.");
        assertEquals(task1, history.get(0), "First task in history should be task1.");
        assertEquals(task2, history.get(1), "Second task in history should be task2.");
    }

    // Test history manager with task deletion
    @Test
    public void testHistoryManagerWithTaskDeletion() {
        Task task1 = new Task("Task 1", "Task Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Task Description 2", TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // View tasks
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        // Delete first task
        taskManager.deleteTaskById(task1.getId());

        // Check history
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size(), "History should contain one task after deletion.");
        assertEquals(task2, history.get(0), "Remaining task should be task2.");
    }

    // Test subtask deletion from epic
    @Test
    public void testSubtaskDeletionFromEpic() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description 1", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);

        // Ensure subtask is added to epic
        assertTrue(epic.getSubtaskIds().contains(subtask1.getId()), "Epic should contain the Subtask ID.");

        // Delete subtask
        taskManager.deleteSubtaskById(subtask1.getId());

        // Ensure subtask is removed from epic
        assertFalse(epic.getSubtaskIds().contains(subtask1.getId()), "Epic should not contain the deleted Subtask ID.");
    }

    // Test that deleting an epic removes its subtasks
    @Test
    public void testEpicDeletionRemovesSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask Description 1", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask Description 2", TaskStatus.NEW);
        taskManager.createSubtask(subtask1, epic);
        taskManager.createSubtask(subtask2, epic);

        // Delete epic
        taskManager.deleteEpicById(epic.getId());

        // Ensure subtasks are also deleted
        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Subtask 1 should be deleted with the Epic.");
        assertNull(taskManager.getSubtaskById(subtask2.getId()), "Subtask 2 should be deleted with the Epic.");
    }

    // Test changing subtask's epic association
    @Test
    public void testSubtaskEpicIdChangeThroughManager() {
        Epic epic1 = new Epic("Epic 1", "Epic Description 1");
        Epic epic2 = new Epic("Epic 2", "Epic Description 2");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        Subtask subtask = new Subtask("Subtask", "Subtask Description", TaskStatus.NEW);
        taskManager.createSubtask(subtask, epic1);

        // Change subtask's epic association
        boolean updated = ((InMemoryTaskManager) taskManager).updateSubtaskEpic(subtask, epic2);
        assertTrue(updated, "Should successfully update subtask's epic association.");

        // Ensure subtask is no longer in old epic
        assertFalse(epic1.getSubtaskIds().contains(subtask.getId()), "Subtask should not be in the old Epic.");

        // Ensure subtask is now in new epic
        assertTrue(epic2.getSubtaskIds().contains(subtask.getId()), "Subtask should be in the new Epic.");
    }
}
