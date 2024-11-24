package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    // Конструктор, принимающий файл для автосохранения
    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод для сохранения текущего состояния менеджера в файл
    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Записываем заголовок
            writer.write("id,type,name,status,description,epic\n");

            // Сохраняем задачи
            for (Task task : tasks.values()) {
                writer.write(taskToString(task));
                writer.newLine();
            }

            // Сохраняем эпики
            for (Epic epic : epics.values()) {
                writer.write(taskToString(epic));
                writer.newLine();
            }

            // Сохраняем подзадачи
            for (Subtask subtask : subtasks.values()) {
                writer.write(taskToString(subtask));
                writer.newLine();
            }

            // Пустая строка для разделения задач и истории
            writer.newLine();

            // Сохраняем историю
            String history = historyToString(historyManager);
            if (!history.isEmpty()) {
                writer.write(history);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage());
        }
    }

    // Метод для преобразования задачи в строку CSV
    private String taskToString(Task task) {
        StringBuilder taskStringBuilder = new StringBuilder();
        taskStringBuilder.append(task.getId()).append(",");
        taskStringBuilder.append(getType(task)).append(",");
        taskStringBuilder.append(task.getName()).append(",");
        taskStringBuilder.append(task.getStatus()).append(",");
        taskStringBuilder.append(task.getDescription()).append(",");

        if (task instanceof Subtask) {
            taskStringBuilder.append(((Subtask) task).getEpicId());
        }

        return taskStringBuilder.toString();
    }

    // Метод для определения типа задачи
    private String getType(Task task) {
        if (task instanceof Epic) {
            return "EPIC";
        } else if (task instanceof Subtask) {
            return "SUBTASK";
        } else {
            return "TASK";
        }
    }

    // Статический метод для создания задачи из строки CSV
    private static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String type = fields[1];
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case "TASK":
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            case "EPIC":
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case "SUBTASK":
                int epicId = Integer.parseInt(fields[5]);
                Subtask subtask = new Subtask(name, description, status);
                subtask.setId(id);
                subtask.setEpicId(epicId);
                return subtask;
            default:
                return null;
        }
    }

    // Статический метод для загрузки менеджера из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());

            int maxId = 0;
            int lineIndex  = 1; // Пропускаем заголовок

            // Читаем задачи до пустой строки
            while (lineIndex < lines.size() && !lines.get(lineIndex ).isEmpty()) {
                Task task = fromString(lines.get(lineIndex ));
                if (task != null) {
                    int id = task.getId();
                    if (id > maxId) {
                        maxId = id;
                    }
                    if (task instanceof Epic) {
                        manager.epics.put(id, (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(id, (Subtask) task);
                    } else {
                        manager.tasks.put(id, task);
                    }
                }
                lineIndex++;
            }

            // Обновляем счетчик ID
            manager.idCounter = maxId;

            // Восстанавливаем связи между эпиками и подзадачами
            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                }
            }

            // Пропускаем пустую строку
            lineIndex++;

            // Читаем и восстанавливаем историю
            if (lineIndex < lines.size()) {
                String historyLine = lines.get(lineIndex);
                List<Integer> historyIds = historyFromString(historyLine);
                for (Integer id : historyIds) {
                    if (manager.tasks.containsKey(id)) {
                        manager.historyManager.add(manager.tasks.get(id));
                    } else if (manager.epics.containsKey(id)) {
                        manager.historyManager.add(manager.epics.get(id));
                    } else if (manager.subtasks.containsKey(id)) {
                        manager.historyManager.add(manager.subtasks.get(id));
                    }
                }
            }

            // Обновляем статусы эпиков
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + e.getMessage());
        }
        return manager;
    }

    // Метод для преобразования истории в строку CSV
    private static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder historyStringBuilder = new StringBuilder();
        for (Task task : history) {
            historyStringBuilder.append(task.getId()).append(",");
        }
        if (!historyStringBuilder.isEmpty()) {
            historyStringBuilder.deleteCharAt(historyStringBuilder.length() - 1); // Удаляем последнюю запятую
        }
        return historyStringBuilder.toString();
    }

    // Метод для восстановления истории из строки CSV
    private static List<Integer> historyFromString(String value) {
        String[] ids = value.split(",");
        List<Integer> historyIds = new ArrayList<>();
        for (String id : ids) {
            historyIds.add(Integer.parseInt(id));
        }
        return historyIds;
    }

    // Переопределяем методы, изменяющие состояние, чтобы добавить вызов save()
    @Override
    public boolean createTask(Task task) {
        boolean result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public boolean createEpic(Epic epic) {
        boolean result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public boolean createSubtask(Subtask subtask, Epic epic) {
        boolean result = super.createSubtask(subtask, epic);
        save();
        return result;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean result = super.updateSubtask(subtask);
        save();
        return result;
    }

    @Override
    public boolean updateSubtaskEpic(Subtask subtask, Epic newEpic) {
        boolean result = super.updateSubtaskEpic(subtask, newEpic);
        save();
        return result;
    }


    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    // Переопределяем методы получения задач для обновления истории и сохранения
    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }
}
