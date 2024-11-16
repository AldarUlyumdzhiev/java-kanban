package main;

import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager manager = Managers.getDefault();

        // Create two tasks
        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Create an epic with two subtasks
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW);
        manager.createSubtask(subtask1, epic1);
        manager.createSubtask(subtask2, epic1);

        // View tasks
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());

        // View task again
        manager.getTaskById(task1.getId());

        // Print history
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        // Delete a task
        manager.deleteTaskById(task1.getId());

        // Print history after deletion
        System.out.println("\nИстория после удаления задачи 1:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
