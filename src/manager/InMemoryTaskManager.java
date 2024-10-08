package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int idCounter = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return ++idCounter;
    }

    // Методы для задач (Task)
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public boolean createTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            // Задача с таким ID уже существует
            return false;
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return true;
    }

    @Override
    public boolean updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return false;
        }
        tasks.put(task.getId(), task);
        return true;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    // Методы для эпиков (Epic)
    @Override
    public boolean createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            // Epic с таким ID уже существует
            return false;
        }
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return true;
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return false;
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        return true;
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    // Методы для подзадач (Subtask)
    @Override
    public boolean createSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            // Подзадача с таким ID уже существует
            return false;
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            // Epic с указанным epicId не найден
            return false;
        }

        int id = generateId();
        subtask.setId(id);

        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic);
        return true;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            return false;
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        return true;
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    // Получение списка всех подзадач определённого эпика
    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        List<Subtask> subtasksOfEpic = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    subtasksOfEpic.add(subtask);
                }
            }
        }
        return subtasksOfEpic;
    }

    // Метод для обновления статуса эпика
    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean areAllSubtasksNew = true;
        boolean areAllSubtasksDone = true;

        for (int id : subtaskIds) {
            TaskStatus status = subtasks.get(id).getStatus();
            if (status != TaskStatus.NEW) {
                areAllSubtasksNew = false;
            }
            if (status != TaskStatus.DONE) {
                areAllSubtasksDone = false;
            }
        }

        if (areAllSubtasksNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (areAllSubtasksDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    // Метод для получения истории просмотров
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
