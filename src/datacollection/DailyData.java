package datacollection;

import java.time.LocalDateTime;
import java.util.ArrayList;

/** Object holding daily data.
 * @author Jessica Su
 */

public class DailyData {

    private LocalDateTime dateTime;
    private int[][][] sums; // 3D array of sums of severity occurrences [row][column][severity]

    public DailyData(Grid grid) {
        this.sums = grid.calcSeverities();
        this.dateTime = grid.getDateTime();
        // add weather data
    }

    public String toCSV() {
        CSVBuilder csvBuilder = new CSVBuilder();
        int col = sums[0][0].length;
        int rows = sums[0].length;

        csvBuilder
            .append(this.dateTime.toString())
            .append(rows)
            .append(col);
            //append weather data

        // Append Severities --> Order is row column and level of severity
        // Ex. (Row, Col, Sev) -> (1, 1, 0) (1, 1, 1) (1, 1, 2) (1, 1, 3) (1, 2, 0), etc.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < 4; k++) {
                    csvBuilder.append(this.sums[i][j][k]);
                }
            }
        }

        return csvBuilder.toCSV();
    }

}