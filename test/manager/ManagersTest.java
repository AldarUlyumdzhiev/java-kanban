package manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    // Проверка инициализации TaskManager
    @Test
    public void testGetDefaultTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Метод Managers.getDefault() должен возвращать проинициализированный экземпляр TaskManager.");
    }

    // Проверка инициализации HistoryManager
    @Test
    public void testGetDefaultHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Метод Managers.getDefaultHistory() должен возвращать проинициализированный экземпляр HistoryManager.");
    }
}
