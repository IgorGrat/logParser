package ua.edg.logparser.gui;

import javafx.application.Application;

public class LauncherJavaFX {
    public static void main(String[] args) {
        // Цей метод запустить JavaFX клас в обхід модульних обмежень
        Application.launch(JavaFX.class, args);
    }
}
