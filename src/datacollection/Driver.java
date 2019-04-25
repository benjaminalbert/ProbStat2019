package datacollection;

import datacollection.PoliceCall.Filter;
import datacollection.WeatherReport.StationReport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Benjamin Albert
 */
public class Driver {
    
    public static final String WORKING_DIRECTORY = System.getProperty("user.dir") + File.separator;
    public static final String DATA_SAVE_DIR = WORKING_DIRECTORY + "data" + File.separator;
    
    public static final String RAW_CRIME_FILE_NAME = "raw_crime_data.csv";
    public static final String FILTERED_CRIME_FILE_NAME = "filtered_crime_data.json";
    
    public static final String RAW_WEATHER_FILE_NAME = "raw_weather_data.csv";

    public static void main(String[] args) {
        try {
            File saveDir = new File(DATA_SAVE_DIR);
            if (!saveDir.exists()){
                saveDir.mkdirs();
            }

//            downloadCrimeData();
            //downloadWeatherData();

//            downloadCrimeData();
//            downloadWeatherData();
            
            Filter filter = null;
            //filter = makeHighSeverityFilter();
            PoliceCall[] policeCalls = readPoliceCalls(filter);

            StationReport[] stationReports = readStationReports();
            WeatherReport[] weatherReports = generateWeatherReports(stationReports);

            System.out.println("formatting data...");
            DataFormatting.Formatting(weatherReports, policeCalls, DATA_SAVE_DIR);



//            PoliceCall.write(policeCalls, DATA_SAVE_DIR + FILTERED_CRIME_FILE_NAME);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadCrimeData() throws IOException{
        System.out.println("downloading crime data (may take a few minutes)...");
        DataDownloader.download(DataDownloader.getCrimeDataURL(), new File(DATA_SAVE_DIR + RAW_CRIME_FILE_NAME));
    }

    public static void downloadWeatherData() throws IOException{
        System.out.println("downloading weather data (may take a few minutes)...");
        DataDownloader.download(DataDownloader.getWeatherDataURL(), new File(DATA_SAVE_DIR + RAW_WEATHER_FILE_NAME));
    }

    public static Filter makeLowSeverityFilter() {
        Filter filter = new Filter();
        filter.getSeverities().addAll(Arrays.asList(new Integer[]{0, 1}));
        filter.setRequireCoordinate(true);
        filter.getDescriptionBlacklist().add("911/NO  VOICE");
        return filter;
    }

    public static Filter makeHighSeverityFilter() {
        Filter filter = new Filter();
        filter.getSeverities().addAll(Arrays.asList(new Integer[]{2, 3}));
        filter.setRequireCoordinate(true);
        return filter;
    }
    
    public static PoliceCall[] readPoliceCalls(Filter filter) throws FileNotFoundException, IOException{
        System.out.println("parsing crime data...");
        return PoliceCall.readPoliceCalls(DATA_SAVE_DIR + RAW_CRIME_FILE_NAME, filter);
    }
    
    public static StationReport[] readStationReports() throws IOException {
        System.out.println("parsing weather station data...");
        return StationReport.readStationReports(DATA_SAVE_DIR + RAW_WEATHER_FILE_NAME);
    }
    
    public static WeatherReport[] generateWeatherReports(StationReport[] stationReports){
        System.out.println("generating weather reports...");
        return WeatherReport.generateWeatherReports(stationReports, 6, true, new String[]{"DMH", "BWI"});
    }
}
