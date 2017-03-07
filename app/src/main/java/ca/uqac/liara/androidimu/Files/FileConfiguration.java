package ca.uqac.liara.androidimu.Files;

/**
 * Created by FlorentinTh on 2/20/2017.
 */
public class FileConfiguration {

    private FileConfiguration() {}

    private static FileConfiguration instance = null;

    public static FileConfiguration getInstance() {
        if (instance == null) {
            instance = new FileConfiguration();
        }

        return instance;
    }

    private String baseFolder = "AndroidIMU";

    public String getBaseFolder() { return baseFolder; }

    private String format = ".csv";

    public String getFormat() { return format; }

    private String[] headers = {"acc_x", "acc_y", "acc_z", "gyr_x", "gyr_y", "gyr_z", "magn_x", "magn_y", "magn_z", "azimuth", "pitch", "roll", "floor", "people", "sensor_location"};

    public String[] getHeaders() {
        return headers;
    }

    private String recordSeparator = ",";

    public String getRecordSeparator() {
        return recordSeparator;
    }

    private String lineSeparator = "\n";

    public String getLineSeparator() { return lineSeparator; }
}
