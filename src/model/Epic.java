package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    // Метод для получения списка ID подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Методы для доабвления и удаления подзадач
    public void addSubtaskId(int id) {
        if (id == this.id) {
            System.out.println("Epic не может добавить сам себя как подзадачу.");
            return;
        }
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        if (!subtaskIds.contains(id)) {
            System.out.println("Подзадачи с ID " + id + " нет в этом Epic.");
            return;
        }
        subtaskIds.remove(Integer.valueOf(id));
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
