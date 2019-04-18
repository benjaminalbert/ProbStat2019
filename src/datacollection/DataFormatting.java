package datacollection;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Jessica Su
 */

public class DataFormatting {

    private static Grid grid;

    public static void binning(PoliceCall[] policeCalls) {
        grid = new Grid();
        MaxMin(policeCalls);
        for (PoliceCall call : policeCalls) {
            grid.insertCall(call);
        }
        int bins = grid.getBins();
        for (int i = 0; i < bins; i++) {
            for (int j = 0; j < bins; j++) {
                sortEachBox(grid.getCallsPerBox(i, j));
            }
        }
    }

    private static void MaxMin(PoliceCall[] policeCalls) {
        double minLat = 100.;
        double maxLat = 0.;
        double minLong = 200.;
        double maxLong = 0.;
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
        grid.setMaxLat(maxLat);
        grid.setMinLat(minLat);
        grid.setMinLong(minLong);
        grid.setMaxLong(maxLong);
    }

    private static void sortEachBox(ArrayList<PoliceCall> calls) {
        if (calls != null) {
            Collections.sort(calls, new DateComparator());
        }
    }

    // add weather input
    public static void Formatting(String saveFilePath) throws IOException {
        File file = new File(saveFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        String content;
        int bins = grid.getBins();
        for (int i = 0; i < bins; i++) {
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
        }

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