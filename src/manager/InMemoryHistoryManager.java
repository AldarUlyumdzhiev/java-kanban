package manager;

import model.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int HISTORY_LIMIT = 10;
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return; // Не добавляем null задачи в историю
        }
        if (history.size() >= HISTORY_LIMIT) {
            history.removeFirst(); // Удаляем самый старый элемент (первый в списке)
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
