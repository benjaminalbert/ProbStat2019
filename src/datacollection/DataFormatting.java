package datacollection;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


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

    /** Produce a formatted CSV file with Weather and Crime Data.
     * @param weather the weather data
     * @param policeCalls the crime data
     * @param saveFilePath the save directory
     * @throws IOException when file is not found
     */
    public static void Formatting(WeatherReport.StationReport[] weather, PoliceCall[] policeCalls, String saveFilePath) throws IOException {
        // Debugging purposes --> must match printed total in CSV
        System.out.println("Total # of Calls: " + policeCalls.length);
        int total = 0;
        // Set grid bounds based on all of the police calls
        MaxMin(policeCalls);
        // Name of formatted file
        String fileName = "Formatted_data.csv";
        File file = new File(saveFilePath + fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        String content;
        // Sort the array of all policecalls using the Date/Time
        Arrays.sort(policeCalls, PoliceCall.DATE_TIME_COMPARATOR);
        int k = 0;
        ArrayList<PoliceCall> dailyCalls;
        String currCallDate;
        String nextCallDate;
        while (k < policeCalls.length) {
            // Create list of all police calls of a day
            dailyCalls = new ArrayList<>();
            // insert the first call to dailyCalls
            // if the next call has the same date as the last, continue adding to the same list
            do {
                nextCallDate = null;
                dailyCalls.add(policeCalls[k]);
                currCallDate = policeCalls[k].getDatetime().toString().substring(0, 10);
                k++;
                if (k < policeCalls.length) {
                    nextCallDate = policeCalls[k].getDatetime().toString().substring(0, 10);
                }
            } while (currCallDate.equals(nextCallDate));
            // after creating daily list of calls, bin them according to pre-set grid
            binning(dailyCalls);
            int i = findWeatherReport(weather, currCallDate);
            // if weather report is not found for the current data, throw an exception
            if (i == -1) {
                throw new IOException("Weather Report Not Found");
            }
            DailyData day = new DailyData(grid, weather[i]);
            // each DailyData object has the information for each row of the CSV
            content = day.toCSV();
            // write the data into the CSV file
            bufferedWriter.write(content);
            total += day.callsPerDay();
        }

        // Debugging purposes --> Must match previous printed value
        System.out.println("Total in CSV: " + total);

        /*
        if (saveFilePath.endsWith("json")) {
            content = new Gson().toJson(policeCalls);
        } else {
            content = toCSV(policeCalls);
        }*/

        bufferedWriter.flush();
        bufferedWriter.close();
    }

    // Find weather report for a given date.
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