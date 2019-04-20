package datacollection;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
        maxLat = 0.;
        minLong = 200.;
        maxLong = 0.;
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
    public static void Formatting(PoliceCall[] policeCalls, String saveFilePath) throws IOException {
        MaxMin(policeCalls); // Set grid bounds based on all of the police calls
        File file = new File(saveFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        String content;
        Arrays.sort(policeCalls, PoliceCall.DATE_TIME_COMPARATOR); // Sort the array of all policecalls using the Date/Time
        int k = 0;
        ArrayList<PoliceCall> dailyCalls;
        while (k < policeCalls.length) {
            do {
                dailyCalls = new ArrayList<>(); // Create list of all police calls of a day
                dailyCalls.add(policeCalls[k]);
                k++;
            } while (k < policeCalls.length && policeCalls[k-1].getDatetime().compareTo(policeCalls[k].getDatetime()) == 0);
            binning(dailyCalls); // after creating daily list of calls, bin them according to pre-set grid
            DailyData day = new DailyData(grid);
            content = day.toCSV(); // each DailyData object has the information for each row of the CSV
            bufferedWriter.write(content); // write the data into the CSV file
        }

 /*       for (int i = 0; i < bins; i++) {
            for (int j = 0; j < bins; j++) {
                ArrayList<PoliceCall> calls = grid.getCallsPerBox(i, j);
                if (calls != null) {
                    int callSize = calls.size();
                    ArrayList<PoliceCall> dailyCall;
                    int k = 0;
                    while(k < callSize) {
                        do {
                            dailyCall = new ArrayList<>();
                            dailyCall.add(calls.get(k));
                            k++;
                        } while(calls.get(k-1).getDatetime().compareTo(calls.get(k).getDatetime()) == 0);
                        DailyData day = new DailyData(dailyCall);
                        day.countSevs();
                        content = day.toCSV();
                        bufferedWriter.write(content);
                    }
                }
            }
        }*/

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
}