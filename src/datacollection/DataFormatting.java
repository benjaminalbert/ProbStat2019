package datacollection;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
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
    private static final int rBins = 15;
    private static final int cBins = 15;

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
    public static void Formatting(WeatherReport[] weather, PoliceCall[] policeCalls, String saveFilePath) throws IOException {
        // Debugging purposes --> must match printed total in CSV
        System.out.println("Total # of Calls: " + policeCalls.length);
        int total = 0;
        // Set grid bounds based on all of the police calls
        MaxMin(policeCalls);
        // Name of formatted file
        String fileName = "Formatted_" + rBins + "x" + cBins + "_Towed_Data.csv";
        File file = new File(saveFilePath + fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        String bounds = boundstoCSV();
        bufferedWriter.write(bounds);
        String content;
        // Sort the array of all policecalls using the Date/Time
        Arrays.sort(policeCalls, PoliceCall.DATE_TIME_COMPARATOR);
        int k = 0;
        LocalDateTime beginDatetime;
        LocalDateTime endDatetime;
        ArrayList<PoliceCall> quarterCalls;
        for (int i = 0; i < weather.length; i++) {
        //while (k < policeCalls.length) {
            beginDatetime = weather[i].getStartDateTime();
            endDatetime = weather[i].getEndDateTime();
            quarterCalls = new ArrayList<>();
            // insert the first call to dailyCalls
            // if the next call has the same date as the last, continue adding to the same list
            while (k < policeCalls.length && (policeCalls[k].getDatetime().isEqual(beginDatetime) || policeCalls[k].getDatetime().isAfter(beginDatetime)) && policeCalls[k].getDatetime().isBefore(endDatetime)) {
                quarterCalls.add(policeCalls[k]);
                k++;
            }
            if (quarterCalls.size() != 0) {
                binning(quarterCalls);
                QuarterDayData quarter = new QuarterDayData(grid, weather[i]);
                content = quarter.toCSV();
                // add csv descriptive min/max lat/long
                total += quarter.callsPerQuarterDay();
                bufferedWriter.write(content);
            }
        }

        // Debugging purposes --> Must match previous printed value
        System.out.println("Total in CSV: " + total);
        debugPrints(policeCalls);
        /*
        if (saveFilePath.endsWith("json")) {
            content = new Gson().toJson(policeCalls);
        } else {
            content = toCSV(policeCalls);
        }*/

        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private static String boundstoCSV() {
        CSVBuilder csvBuilder = new CSVBuilder();
        csvBuilder
            .append("Min Lat: ")
            .append(minLat)
            .append("Max Lat: ")
            .append(maxLat)
            .append("Min Long: ")
            .append(minLong)
            .append("Max Long: ")
            .append(maxLong);
        csvBuilder.newline();
        return csvBuilder.toCSV();
    }

    public static void debugPrints(PoliceCall[] policeCalls) {
        int sum = 0;
        boolean latZero = false;
        boolean longZero = false;
        for (PoliceCall call : policeCalls) {
            if (call.getSeverity() == -1) {
                sum += 1;
            }
            if (call.getLatitude() == 0.) {
                latZero = true;
            }
            if (call.getLongitude() == 0.) {
                longZero = true;
            }
        }
        System.out.println("Bad Sev: " + sum);
        System.out.println("0 Latitude: " + latZero);
        System.out.println("0 Longitude: " + longZero);

    }
}