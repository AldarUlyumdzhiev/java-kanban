package model;

import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksIds; // Список идентификаторов подзадач

    public Epic(String name, String description, int id) {
        super(name, description, id, TaskStatus.NEW);
        this.subtasksIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void addSubtaskId(int id) {
        subtasksIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtasksIds.remove(Integer.valueOf(id));
    }

    @Override
    public String toString() {
        return "model.Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasksIds=" + subtasksIds +
                '}';
    }
}
