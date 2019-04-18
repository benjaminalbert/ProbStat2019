package datacollection;

import java.time.LocalDateTime;
import java.util.ArrayList;

/** Object holding daily data.
 * @author Jessica Su
 */

public class DailyData {

    private LocalDateTime dateTime;
    private int totalSev0;
    private int totalSev1;
    private int totalSev2;
    private int totalSev3;
    private ArrayList<PoliceCall> arr;

    public DailyData(ArrayList<PoliceCall> ra) {
        totalSev0 = 0;
        totalSev1 = 0;
        totalSev2 = 0;
        totalSev3 = 0;
        this.arr = ra;
        this.dateTime = ra.get(0).getDatetime();
    }

    public void countSevs() {
        for (PoliceCall call : this.arr) {
            int sev = call.getSeverity();
            if (sev == 0) {
                this.totalSev0 =+ 1;
            } else if (sev == 1) {
                this.totalSev1 =+ 1;
            } else if (sev == 2) {
                this.totalSev2 =+ 1;
            } else {
                this.totalSev3 =+ 1;
            }
        }
    }

    public String toCSV() {
        CSVBuilder csvBuilder = new CSVBuilder();
        csvBuilder
            .append(this.dateTime.toString())
            .append(this.totalSev0)
            .append(this.totalSev1)
            .append(this.totalSev2)
            .append(this.totalSev3);

        return csvBuilder.toCSV();
    }

}