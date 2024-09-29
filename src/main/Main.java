package main;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import manager.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager manager = new TaskManager();

        // Создание двух задач
        Task task1 = new Task("Задача 1", "Описание задачи 1", 0, TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", 0, TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        // Создание эпика с двумя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1", 0);
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", 0, TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", 0, TaskStatus.NEW, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Создание эпика с одной подзадачей
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2", 0);
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", 0, TaskStatus.NEW, epic2.getId());
        manager.createSubtask(subtask3);

        // Вывод всех задач, эпиков и подзадач
        System.out.println("Все задачи: " + manager.getAllTasks());
        System.out.println("Все эпики: " + manager.getAllEpics());
        System.out.println("Все подзадачи: " + manager.getAllSubtasks());

        // Изменение статусов
        task1.setStatus(TaskStatus.DONE);
        manager.updateTask(task1);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);

        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask2);

        // Проверка обновления статуса эпика
        System.out.println("Статус эпика 1 после изменения подзадач: " + manager.getEpicById(epic1.getId()).getStatus());

        // Удаление задачи и эпика
        manager.deleteTaskById(task2.getId());
        manager.deleteEpicById(epic2.getId());

        // Вывод оставшихся задач и эпиков
        System.out.println("Оставшиеся задачи: " + manager.getAllTasks());
        System.out.println("Оставшиеся эпики: " + manager.getAllEpics());

    }
}
