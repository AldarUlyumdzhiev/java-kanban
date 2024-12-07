package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Comparator;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected int idCounter = 0;

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    // TreeSet для хранения задач по приоритету(сначала с ближайшим startTime, задачи с starTime==null в конце)
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(
                    Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
            )
    );

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
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
            prioritizedTasks.remove(tasks.get(id)); // Удаление из TreeSet
        }
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
            return false; // ID уже существует
        }
        if (isOverlappingWithExistingTasks(task)) {
            throw new IllegalArgumentException("Задача пересекается с уже существующей.");
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        prioritizedTasks.add(task);
        return true;
    }

    @Override
    public boolean updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return false;
        }

        prioritizedTasks.remove(tasks.get(task.getId())); // Удаляем старую версию
        if (isOverlappingWithExistingTasks(task)) {
            throw new IllegalArgumentException("Задача пересекается с другой задачей.");
        }

        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return true;
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task); // Удаление из TreeSet
            historyManager.remove(id);
        }
    }

    // Методы для эпиков (Epic)
    @Override
    public boolean createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
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
        for (int id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);

            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
    }

    // Методы для подзадач (Subtask)
    @Override
    public boolean createSubtask(Subtask subtask, Epic epic) {
        if (subtasks.containsKey(subtask.getId())) {
            return false;
        }
        if (!epics.containsKey(epic.getId())) {
            return false;
        }
        if (isOverlappingWithExistingTasks(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается с другой задачей.");
        }
        int id = generateId();
        subtask.setId(id);
        subtask.setEpicId(epic.getId());
        subtasks.put(id, subtask);
        prioritizedTasks.add(subtask);
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

        prioritizedTasks.remove(subtasks.get(subtask.getId())); // Удаляем старую версию
        if (isOverlappingWithExistingTasks(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается с другой задачей.");
        }

        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        return true;
    }

    public boolean updateSubtaskEpic(Subtask subtask, Epic newEpic) {
        if (subtask == null || newEpic == null) {
            return false;
        }

        if (!epics.containsKey(newEpic.getId())) {
            return false;
        }

        Epic oldEpic = epics.get(subtask.getEpicId());
        if (oldEpic != null) {
            oldEpic.removeSubtaskId(subtask.getId());
            updateEpicStatus(oldEpic);
        }

        subtask.setEpicId(newEpic.getId());
        newEpic.addSubtaskId(subtask.getId());
        updateEpicStatus(newEpic);

        return true;
    }

    @Override
    public void deleteAllSubtasks() {
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
        }
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
            prioritizedTasks.remove(subtask); // Удаление из TreeSet
            historyManager.remove(id);
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
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    // Метод для обновления статуса эпика
    protected void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }

        boolean areAllSubtasksNew = true;
        boolean areAllSubtasksDone = true;

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (int id : subtaskIds) {
            Subtask subtask = subtasks.get(id);
            if (subtask == null) continue;

            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) areAllSubtasksNew = false;
            if (status != TaskStatus.DONE) areAllSubtasksDone = false;

            if (subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }
                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        if (areAllSubtasksNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (areAllSubtasksDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        epic.setStartTime(earliestStart);
        epic.setDuration(totalDuration);
    }

    // Метод для получения истории просмотров
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Метод для получения списка задач по приоритету
    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Метод для проверки пересечения двух задач по времени
    protected boolean isOverlapping(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }
        return !(task1.getEndTime().isBefore(task2.getStartTime()) ||
                task2.getEndTime().isBefore(task1.getStartTime()));
    }

    // Метод для проверки пересечения с уже существующими задачами в prioritizedTasks
    protected boolean isOverlappingWithExistingTasks(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }
        for (Task existingTask : prioritizedTasks) {
            if (existingTask.getStartTime() != null &&
                    isOverlapping(newTask, existingTask)) {
                return true;
            }
        }
        return false;
    }
}
