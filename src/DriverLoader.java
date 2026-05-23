import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;

public class DriverLoader {

    private static boolean isLoaded = false;
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    public static void load() {
        if (isLoaded)
            return;

        try {
            // First, try standard loading
            Class.forName(DRIVER_CLASS);
            isLoaded = true;
            System.out.println("[DriverLoader] Driver found in standard classpath.");
            return;
        } catch (ClassNotFoundException ignored) {
            System.out.println("[DriverLoader] Driver NOT found in classpath. Attempting dynamic load...");
        }

        // Search in common locations
        File jarFile = findJarFile();
        if (jarFile == null || !jarFile.exists()) {
            System.err.println(
                    "[DriverLoader] CRITICAL: MySQL JAR file not found in any standard location (lib/, ../lib/).");
            return;
        }

        try {
            System.out.println("[DriverLoader] Found JAR: " + jarFile.getAbsolutePath());
            URL url = jarFile.toURI().toURL();
            URLClassLoader ucl = new URLClassLoader(new URL[] { url });

            Class<?> driverClass = Class.forName(DRIVER_CLASS, true, ucl);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

            // Register via Shim to bypass ClassLoader mismatch in DriverManager
            DriverManager.registerDriver(new DriverShim(driver));

            isLoaded = true;
            System.out.println("[DriverLoader] Driver successfully loaded dynamically!");

        } catch (Exception e) {
            System.err.println("[DriverLoader] Failed to load driver dynamically.");
            e.printStackTrace();
        }
    }

    private static File findJarFile() {
        String[] searchPaths = {
                "lib",
                "../lib",
                "../../lib",
                "skyport/lib"
        };

        for (String path : searchPaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.startsWith("mysql-connector") && name.endsWith(".jar"));
                if (files != null && files.length > 0) {
                    return files[0];
                }
            }
        }

        // Try Absolute path fallback if current dir is skewed
        File absLib = new File("C:/Users/Administrator/Desktop/SkyPort - Copy/skyport/lib");
        if (absLib.exists()) {
            File[] files = absLib.listFiles((d, name) -> name.startsWith("mysql-connector") && name.endsWith(".jar"));
            if (files != null && files.length > 0)
                return files[0];
        }

        return null; // Not found
    }
}
