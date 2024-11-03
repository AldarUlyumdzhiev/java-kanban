package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import java.util.List;

public interface TaskManager {
    // Методы для задач
    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTaskById(int id);
    boolean createTask(Task task);
    boolean updateTask(Task task);
    void deleteTaskById(int id);

    // Методы для эпиков
    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpicById(int id);
    boolean createEpic(Epic epic);
    boolean updateEpic(Epic epic);
    void deleteEpicById(int id);

    // Методы для подзадач
    List<Subtask> getAllSubtasks();
    void deleteAllSubtasks();
    Subtask getSubtaskById(int id);
    boolean createSubtask(Subtask subtask, Epic epic);
    boolean updateSubtask(Subtask subtask);
    void deleteSubtaskById(int id);

    // Получение списка подзадач эпика
    List<Subtask> getSubtasksOfEpic(int epicId);

    // Метод для получения истории просмотров
    List<Task> getHistory();
}
