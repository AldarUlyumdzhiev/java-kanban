package main;

import http.HttpTaskServer;
import manager.FileBackedTaskManager;
import manager.TaskManager;

import java.io.File;
import java.io.IOException;



public class Main {
    public static void main(String[] args) {
        File file = new File("tasks.csv");

        // Создаём менеджер с сохранением в файл
        TaskManager manager = new FileBackedTaskManager(file);

        // Запускаем Http сервер
        try {
            HttpTaskServer httpServer = new HttpTaskServer(manager);
            httpServer.start();
        } catch (IOException e) {
            System.err.println("Не удалось запустить HTTP-сервер: " + e.getMessage());
        }
    }
}
