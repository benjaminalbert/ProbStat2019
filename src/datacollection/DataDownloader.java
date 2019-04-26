package datacollection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import org.apache.commons.io.FileUtils;

/**
 * Baltimore Police Departmnet 911 Calls Website:
 * https://data.baltimorecity.gov/Public-Safety/911-Police-Calls-for-Service/xviu-ezkt
 * 
 * Weather Data Website:
 * https://mesonet.agron.iastate.edu/request/download.phtml?network=MD_ASOS
 * @author Benjamin Albert
 */
public class DataDownloader {
    
    public static final String CRIME_DATA_FILE_NAME = "crime_data.csv";
    public static final int CONNECTION_TIMEOUT = (int) 1E5;
    public static final int READ_TIMEOUT = (int) 1E6;
    
    public static String getCrimeDataURL(){
        return new URLBuilder("https://data.baltimorecity.gov/api/views/xviu-ezkt/rows.csv")
                .addArg("accesType", "DOWNLOAD")
                .toURL();
    }
    
    public static String getWeatherDataURL() throws IOException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return new URLBuilder("https://mesonet.agron.iastate.edu/cgi-bin/request/asos.py")
                .addArg("station", "DMH")
                .addArg("station", "BWI")
                .addArg("data", "tmpf")
                .addArg("data", "relh")
                .addArg("data", "feel")
                .addArg("data", "p01i")
                .addArg("year1", "2013")
                .addArg("month1", "6")
                .addArg("day1", "30")
                .addArg("year2", String.valueOf(currentDateTime.getYear()))
                .addArg("month2", String.valueOf(currentDateTime.getMonthValue()))
                .addArg("day2", String.valueOf(currentDateTime.getDayOfMonth()))
                .addArg("tz", "America%2FNew_York")
                .addArg("format", "onlycomma")
                .addArg("latlon", "yes")
                .addArg("missing", "null")
                .addArg("trace", "null")
                .addArg("direct", "no")
                .addArg("report_type", "1")
                .addArg("report_type", "2")
                .toURL();
    }

    public static void download(String url, File saveFile) throws IOException {
        download(url, saveFile, CONNECTION_TIMEOUT, READ_TIMEOUT);
    }

    public static void download(String url, File saveFile, int connectionTimeout, int readTimeout) throws IOException {
        FileUtils.copyURLToFile(new URL(url), saveFile, connectionTimeout, readTimeout);
    }
}
