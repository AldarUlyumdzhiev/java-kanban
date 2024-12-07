package model;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime; // Добавляем поле для времени завершения

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
    }

    // Метод для получения времени завершения эпика
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Метод для установки времени завершения эпика
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    // Метод для получения списка ID подзадач
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Методы для добавления и удаления подзадач
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

    // Метод для пересчёта полей duration, startTime и endTime
    public void updateTimes(List<Subtask> subtasks) {
        duration = Duration.ZERO;
        startTime = null;
        LocalDateTime latestEndTime = null;

        for (Subtask subtask : subtasks) {
            duration = duration.plus(subtask.getDuration());

            // Определение самой ранней даты начала подзадачи
            if (startTime == null || (subtask.getStartTime() != null && subtask.getStartTime().isBefore(startTime))) {
                startTime = subtask.getStartTime();
            }

            // Определение самой поздней даты завершения
            LocalDateTime subtaskEndTime = subtask.getEndTime();
            if (latestEndTime == null || (subtaskEndTime != null && subtaskEndTime.isAfter(latestEndTime))) {
                latestEndTime = subtaskEndTime;
            }
        }

        this.setStartTime(startTime);
        this.setDuration(duration);
        this.setEndTime(latestEndTime); // Устанавливаем время завершения эпика
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
