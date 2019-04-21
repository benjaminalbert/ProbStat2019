package datacollection;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @author Jessica Su
 */

public class DataFormatting {

    private static Grid grid;
    private static double minLat;
    private static double maxLat;
    private static double minLong;
    private static double maxLong;

    // Binning Per Day -- New Grid is Created for each day
    private static void binning(ArrayList<PoliceCall> policeCalls) {
        grid = new Grid();
        grid.setDateTime(policeCalls.get(0).getDatetime());
        grid.setMaxLat(maxLat);
        grid.setMinLat(minLat);
        grid.setMinLong(minLong);
        grid.setMaxLong(maxLong);
        for (PoliceCall call : policeCalls) {
            grid.insertCall(call);
        }
    }

    // Calculate Max/Min Longitude/Latitude for grid bounds
    private static void MaxMin(PoliceCall[] policeCalls) {
        minLat = 100.;
        maxLat = -100.;
        minLong = 200.;
        maxLong = -200;
        for (PoliceCall policeCall : policeCalls) {
            if (minLat > policeCall.getLatitude()) {
                minLat = policeCall.getLatitude();
            }
            if (maxLat < policeCall.getLatitude()) {
                maxLat = policeCall.getLatitude();
            }
            if (minLong > policeCall.getLongitude()) {
                minLong = policeCall.getLongitude();
            }
            if (maxLong < policeCall.getLongitude()) {
                maxLong = policeCall.getLongitude();
            }
        }

    }

    // add weather input
    public static void Formatting(WeatherReport.StationReport[] weather, PoliceCall[] policeCalls, String saveFilePath) throws IOException {
        System.out.println("Total # of Calls: " + policeCalls.length);
        int total = 0;
        MaxMin(policeCalls); // Set grid bounds based on all of the police calls
        String fileName = "Formatted_data.csv";
        File file = new File(saveFilePath + fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        String content;
        Arrays.sort(policeCalls, PoliceCall.DATE_TIME_COMPARATOR); // Sort the array of all policecalls using the Date/Time
        int k = 0;
        ArrayList<PoliceCall> dailyCalls;
        String currCallDate;
        String nextCallDate;
        //while (k < 2584) {
        while (k < policeCalls.length) {
            dailyCalls = new ArrayList<>(); // Create list of all police calls of a day
            do {
                nextCallDate = null;
                dailyCalls.add(policeCalls[k]);
                currCallDate = policeCalls[k].getDatetime().toString().substring(0, 10);
                k++;
                //if (k < 2584) {
                if (k < policeCalls.length) {
                    nextCallDate = policeCalls[k].getDatetime().toString().substring(0, 10);
                }
            } while (currCallDate.equals(nextCallDate));
            //System.out.println(k);
            binning(dailyCalls); // after creating daily list of calls, bin them according to pre-set grid
            int i = findWeatherReport(weather, currCallDate);
            if (i == -1) {
                throw new IllegalArgumentException("Weather Report Not Found");
            }
            DailyData day = new DailyData(grid, weather[i]);
            content = day.toCSV(); // each DailyData object has the information for each row of the CSV
            bufferedWriter.write(content); // write the data into the CSV file
            total += day.callsPerDay();
        }

        System.out.println("Total in CSV: " + total);

        /*
        if (saveFilePath.endsWith("json")) {
            content = new Gson().toJson(policeCalls);
        } else {
            content = toCSV(policeCalls);
        }*/
        //content = toCSV(policeCalls);
        //bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private static int findWeatherReport(WeatherReport.StationReport[] weather, String curr) {
        int i = 0;
        while (i < weather.length) {
            if (weather[i].getDatetime().toString().substring(0, 10).equals(curr)) {
                return i;
            }
            i++;
        }
        return -1;
    }
}