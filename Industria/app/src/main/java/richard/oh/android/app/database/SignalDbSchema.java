package richard.oh.android.app.database;

/**
 * Created by Richard on 25/07/2017.
 */

public class SignalDbSchema {

    public static class  SignalTable {
        public static final String NAME = "signal";

        public static final class Cols {
            public static final String DATE = "date";
            public static final String WIFI_STRENGTH = "wifi_strength";
            public static final String CELLULAR_TYPE = "cellular_type";
            public static final String CELLULAR_STRENGTH = "cellular_strength";
        }
    }
}
