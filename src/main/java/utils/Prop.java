package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ronald.langat
 */
public final class Prop {

    private transient Properties props;
    private transient List<String> loadErrors;
    private final transient String error1 = "ERROR: %s is <= 0 or may not have been set";
    private final transient String error2 = "ERROR: %s may not have been set";
    private static final String PROPS_FILE = System.getProperty("user.dir") + File.separator + "appconfig" + File.separator + "configurations.properties";

    private transient String LOGS_PATH;
    private transient String DATABASE_DRIVER;
    private transient String DATABASE_IP;
    private transient String DATABASE_PORT;
    private transient String DATABASE_NAME;
    private transient String DATABASE_USER;
    private transient String DATABASE_PASSWORD;
    private transient String DATABASE_SERVER_TIME_ZONE;
    private transient String SYSTEM_PORT;
    private transient String SYSTEM_HOST;

    /**
     * Instantiates a new Props.
     */
    public Prop() {
        loadProperties(PROPS_FILE);
    }

    private void loadProperties(final String propsFileName) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propsFileName);
            props = new Properties();
            props.load(inputStream);

            LOGS_PATH = readString("LOGS_PATH").trim();

            DATABASE_DRIVER = readString("DATABASE_DRIVER").trim();
            DATABASE_IP = readString("DATABASE_IP").trim();
            DATABASE_PORT = readString("DATABASE_PORT").trim();
            DATABASE_NAME = readString("DATABASE_NAME").trim();
            DATABASE_USER = readString("DATABASE_USER").trim();
            DATABASE_PASSWORD = readString("DATABASE_PASSWORD").trim();
            DATABASE_SERVER_TIME_ZONE = readString("DATABASE_SERVER_TIME_ZONE").trim();
            SYSTEM_PORT = readString("SYSTEM_PORT").trim();
            SYSTEM_HOST = readString("SYSTEM_HOST").trim();

        } catch (IOException ex) {
            Logger.getLogger(Prop.class.getName()).log(Level.SEVERE, "ERROR: Failed to load properties file.\nCause: \n", ex);

        } catch (Exception ex) {
            Logger.getLogger(Prop.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(Prop.class.getName()).log(Level.SEVERE, "ERROR: Failed to load properties file.\nCause: \n", ex);
            }
        }
    }

    public String readString(String propertyName) {
        String property = props.getProperty(propertyName);
        if (property.isEmpty()) {
            getLoadErrors().add(String.format(error2, propertyName));
        }
        return property;
    }
    
    /**
     * Gets load errors.
     *
     * @return the load errors
     */
    public List<String> getLoadErrors() {
        return loadErrors;
    }

    /**
     * Gets logs path.
     *
     * @return the logs path
     */
    public String getLogsPath() {
        return LOGS_PATH;
    }

    public String getDATABASE_DRIVER() {
        return DATABASE_DRIVER;
    }

    public String getDATABASE_IP() {
        return DATABASE_IP;
    }

    public String getDATABASE_PORT() {
        return DATABASE_PORT;
    }

    public String getDATABASE_NAME() {
        return DATABASE_NAME;
    }

    public String getDATABASE_USER() {
        return DATABASE_USER;
    }

    public String getDATABASE_PASSWORD() {
        return DATABASE_PASSWORD;
    }

    public String getDATABASE_SERVER_TIME_ZONE() {
        return DATABASE_SERVER_TIME_ZONE;
    }

    public String getSYSTEM_PORT() {
        return SYSTEM_PORT;
    }

    public String getSYSTEM_HOST() {
        return SYSTEM_HOST;
    }
}
