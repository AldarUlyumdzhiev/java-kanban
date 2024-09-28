import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private int idCounter = 0;

    private int generateId() {
        return ++idCounter;
    }

    // Методы для задач (Task)
    public ArrayList<Task> getAllTasks() {
        System.out.println(new ArrayList<>(tasks.values()));
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    // Методы для эпиков (Epic)
    public void createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic); // Обновляем статус эпика при изменении
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear(); // Также удаляем связанные с ними подзадачи
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    // Методы для подзадач (Subtask)
    public void createSubtask(Subtask subtask) {
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasksIds().clear();
            updateEpicStatus(epic);
        }
    }

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
    public ArrayList<Subtask> getSubtasksOfEpic(int epicId) {
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtasksIds()) {
                subtasksOfEpic.add(subtasks.get(subtaskId));
            }
        }
        return subtasksOfEpic;
    }

    // Метод для обновления статуса эпика
    private void updateEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIds = epic.getSubtasksIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subtaskIds) {
            TaskStatus status = subtasks.get(id).getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
