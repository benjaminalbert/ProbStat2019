package datacollection;

import java.time.LocalDateTime;

/** Object holding daily data.
 * @author Jessica Su
 */

public class DailyData {

    private LocalDateTime dateTime;
    // 3D array of sums of severity occurrences [row][column][severity]
    private int[][][] sums;
    private Double fahrenheit;
    private Double hourlyPrecipitationInches;
    private Double relativeHumidity;
    //private WeatherReport weatherReport;

    public DailyData(Grid grid, WeatherReport report) {
        this.sums = grid.calcSeverities();
        this.dateTime = grid.getDateTime();
        this.fahrenheit = report.getFahrenheit();
        this.hourlyPrecipitationInches = report.getHourlyPrecipitationInches();
        this.relativeHumidity = report.getRelativeHumidity();
    }

    /** Calculate total number of calls per day.
     *  Used primarily for debugging purposes
     * @return calls per day
     */
    public int callsPerDay() {
        int rows = sums.length;
        int col = sums[0].length;
        int sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < 4; k++) {
                    sum += this.sums[i][j][k];
                }
            }
        }
        return sum;
    }

    /** Create a string representation of DailyData.
     * @return the string
     */
    public String toCSV() {
        CSVBuilder csvBuilder = new CSVBuilder();
        int col = sums[0].length;
        int rows = sums.length;
        int calls = callsPerDay();
        csvBuilder
            .append(this.dateTime.toString())
            .append(rows)
            .append(col)
            .append(calls);

        // Used for temporary solution to unavailable data at a single station
        if (this.fahrenheit == null) {
            csvBuilder.append("null");
        } else {
            csvBuilder.append(this.fahrenheit.toString());
        }

        if (this.hourlyPrecipitationInches == null) {
            csvBuilder.append("null");
        } else {
            csvBuilder.append(this.hourlyPrecipitationInches.toString());
        }

        if (this.relativeHumidity == null) {
            csvBuilder.append("null");
        } else {
            csvBuilder.append(this.relativeHumidity.toString());
        }

        // Append Severities --> Order is row column and level of severity
        // Ex. (Row, Col, Sev) -> (1, 1, 0) (1, 1, 1) (1, 1, 2) (1, 1, 3) (1, 2, 0), etc.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < 4; k++) {
                    csvBuilder.append(this.sums[i][j][k]);
                }
            }
        }
        csvBuilder.newline();

        return csvBuilder.toCSV();
    }

}