package datacollection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;

/**
 * Baltimore Police Departmnet 911 Calls Website:
 * https://data.baltimorecity.gov/Public-Safety/911-Police-Calls-for-Service/xviu-ezkt
 *
 * @author Benjamin Albert
 */
public class DataDownloader {
    
    public static final String CRIME_DATA_FILE_NAME = "crime_data.csv";
    public static final String CRIME_DATA_URL = "https://data.baltimorecity.gov/api/views/xviu-ezkt/rows.csv?accessType=DOWNLOAD";
    public static final int CONNECTION_TIMEOUT = (int) 1E5;
    public static final int READ_TIMEOUT = (int) 1E6;

    public static void downloadCrimeData() throws IOException {
        download(CRIME_DATA_URL, new File(Config.DEFAULT_SAVE_DIR + File.separator + CRIME_DATA_FILE_NAME));
    }

    public static void download(String url, File saveFile) throws IOException {
        download(url, saveFile, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    public static void download(String url, File saveFile, int connectionTimeout, int readTimeout) throws IOException {
        FileUtils.copyURLToFile(new URL(url), saveFile, connectionTimeout, readTimeout);
    }
}
