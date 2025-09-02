import engine.engine.Engine;
import engine.engine.EngineImpl;

import java.util.Scanner;

public class ConsoleUI {
    private final Engine engine;
    private final Scanner scanner;

    public ConsoleUI() {
        this.engine = EngineImpl.getInstance();
        this.scanner = new Scanner(System.in);
    }

    public void start() {

    }
}
