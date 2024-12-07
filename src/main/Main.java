package main;

import manager.FileBackedTaskManager;
import manager.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        // Создаем файл для хранения задач
        File file = new File("tasks.csv");

        // Создаем менеджер задач с сохранением в файл
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        // Создаем две задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание задачи 2", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        task2.setDuration(Duration.ofMinutes(120));

        manager.createTask(task1);
        manager.createTask(task2);

        // Создаем эпик с двумя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", TaskStatus.NEW);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 2, 14, 0));
        subtask1.setDuration(Duration.ofMinutes(90));
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 2, 16, 0));
        subtask2.setDuration(Duration.ofMinutes(60));

        manager.createSubtask(subtask1, epic1);
        manager.createSubtask(subtask2, epic1);

        // Просматриваем задачи для добавления в историю
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());

        // Просматриваем задачу снова
        manager.getTaskById(task1.getId());

        // Выводим историю просмотров
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        // Удаляем задачу
        manager.deleteTaskById(task1.getId());

        // Выводим историю после удаления задачи
        System.out.println("\nИстория после удаления задачи 1:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        // Загружаем менеджер из файла
        try {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

            // Выводим задачи из загруженного менеджера
            System.out.println("\nЗадачи из загруженного менеджера:");
            for (Task task : loadedManager.getAllTasks()) {
                System.out.println(task);
            }

            for (Epic epic : loadedManager.getAllEpics()) {
                System.out.println(epic);
            }

            for (Subtask subtask : loadedManager.getAllSubtasks()) {
                System.out.println(subtask);
            }

            // Выводим историю из загруженного менеджера
            System.out.println("\nИстория из загруженного менеджера:");
            for (Task task : loadedManager.getHistory()) {
                System.out.println(task);
            }

        } catch (ManagerSaveException e) {
            System.err.println("Ошибка при загрузке менеджера из файла: " + e.getMessage());
        }
    }
}
