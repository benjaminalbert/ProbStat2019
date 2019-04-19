package datacollection;

import datacollection.PoliceCall.Filter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jessica Su
 */
public class DriverJess {
    public static void main(String[] args){
        try{
 //           System.out.println("downloading data (may take a few minutes)...");
 //           DataDownloader.downloadCrimeData();
            System.out.println("parsing data...");
            Filter filter = new Filter();
            ArrayList<Integer> severities = new ArrayList<>();
            severities.addAll(Arrays.asList(new Integer[]{0,1}));
            filter.setSeverities(severities);
            filter.setRequireCoordinate(true);
            filter.getDescriptionBlacklist().add("911/NO  VOICE");
            PoliceCall[] policeCalls = PoliceCall.readPoliceCalls(Config.DEFAULT_SAVE_DIR + File.separator + DataDownloader.CRIME_DATA_FILE_NAME, filter);

            System.out.println("Formatting...");
            DataFormatting.Formatting(policeCalls, "D:\\JHU\\Prob Stat\\ProbStat2019-master\\FormattedCSV.csv");

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}