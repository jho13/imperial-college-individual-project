package richard.oh.android.app.database;

/**
 * Created by Richard on 18/07/2017.
 */

public class AppDbSchema {

    public static class AppTable {
        public static final String NAME = "app";

        public static final class Cols {
            public static final String DATE = "date"; //long
            public static final String BATTERY = "battery"; //double
            public static final String ACTUAL_BATTERY = "actual_battery"; //double
            public static final String APP_NAME = "app_name"; //String
            public static final String APP_PACKAGE_NAME = "app_package_name"; //String
            public static final String APP_CPU = "app_cpu"; //double
            public static final String APP_MEMORY = "app_memory";
            public static final String APP_UPLOAD = "app_upload"; //long
            public static final String APP_DOWNLOAD = "app_download"; //long
            public static final String MONITOR_CYCLE = "charge_cycle"; //int
            public static final String RESTART_CYCLE = "restart_cycle"; //int
            public static final String IS_FOREGROUND = "is_foreground"; //int
        }
    }
}
