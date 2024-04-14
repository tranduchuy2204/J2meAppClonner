package vn.lapro;

import jdk.nashorn.internal.objects.annotations.Getter;

import java.util.Calendar;
import java.util.prefs.Preferences;

public class AppPreferences {
    private static final String INPUT_JAR_PATH_KEY = "inputJarPath";
    private static final String OUTPUT_FOLDER_KEY = "outputFolder";

    private static final String AUTHOR = "Trần Đức Huy";
    private static final String APP_NAME = "J2me App Cloner";
    public static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    public static final String CURRENT_VERSION = "1.0.1";

    private static final Preferences preferences = Preferences.userNodeForPackage(AppPreferences.class);

    public static String getInputJarPath() {
        return preferences.get(INPUT_JAR_PATH_KEY, "");
    }

    public static void setInputJarPath(String inputJarPath) {
        preferences.put(INPUT_JAR_PATH_KEY, inputJarPath);
    }

    public static String getOutputFolder() {
        return preferences.get(OUTPUT_FOLDER_KEY, "");
    }

    public static void setOutputFolder(String outputFolder) {
        preferences.put(OUTPUT_FOLDER_KEY, outputFolder);
    }

    public static String getAuthor() {
        return AUTHOR;
    }

    public static String getAppName() {
        return APP_NAME;
    }
}
