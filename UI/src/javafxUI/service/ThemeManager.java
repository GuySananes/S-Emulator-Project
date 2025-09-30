package javafxUI.service;

import javafx.scene.Scene;

public class ThemeManager {

    public enum Theme {
        DARK("/javafxUI/view/css/dark.css"),
        LIGHT("/javafxUI/view/css/light.css");

        private final String cssPath;

        Theme(String cssPath) {
            this.cssPath = cssPath;
        }

        public String getCssPath() {
            return cssPath;
        }
    }

    private static ThemeManager instance;
    private Theme currentTheme = Theme.DARK;
    private Scene scene;

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setTheme(Theme theme) {
        if (scene == null) {
            System.err.println("Warning: Scene not set in ThemeManager");
            return;
        }

        // Clear existing stylesheets
        scene.getStylesheets().clear();

        // Add the new theme stylesheet
        try {
            // Use a more robust way to get the resource
            String cssResource = theme.getCssPath();
            java.net.URL resourceUrl = getClass().getResource(cssResource);

            if (resourceUrl == null) {
                throw new RuntimeException("CSS resource not found: " + cssResource);
            }

            String cssPath = resourceUrl.toExternalForm();
            scene.getStylesheets().add(cssPath);
            this.currentTheme = theme;
            System.out.println("Theme changed to: " + theme.name() + " using path: " + cssPath);

        } catch (Exception e) {
            System.err.println("Failed to load theme: " + theme.name() + " - " + e.getMessage());

            // Try fallback to default theme only if we're not already trying the default
            if (theme != Theme.DARK) {
                loadDefaultTheme();
            } else {
                System.err.println("Cannot load default theme either. App will use JavaFX default styling.");
            }
        }
    }

    public void toggleTheme() {
        Theme newTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        setTheme(newTheme);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public boolean isDarkMode() {
        return currentTheme == Theme.DARK;
    }

    private void loadDefaultTheme() {
        try {
            java.net.URL resourceUrl = getClass().getResource(Theme.DARK.getCssPath());
            if (resourceUrl == null) {
                throw new RuntimeException("Default theme CSS not found: " + Theme.DARK.getCssPath());
            }

            String defaultCss = resourceUrl.toExternalForm();
            scene.getStylesheets().add(defaultCss);
            this.currentTheme = Theme.DARK;
            System.out.println("Loaded default theme successfully");

        } catch (Exception e) {
            System.err.println("Failed to load default theme: " + e.getMessage());
            System.err.println("Available resources in css folder:");
            // Try to list available resources for debugging
            try {
                java.net.URL cssDir = getClass().getResource("/javafxUI/view/css/");
                if (cssDir != null) {
                    System.err.println("CSS directory found at: " + cssDir);
                } else {
                    System.err.println("CSS directory not found at: /javafxUI/view/css/");
                }
            } catch (Exception listEx) {
                System.err.println("Could not list CSS directory contents: " + listEx.getMessage());
            }
        }
    }
}