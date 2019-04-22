package datacollection;
import java.time.LocalDateTime;

public class QuarterDayData {

    private LocalDateTime beginDatetime;
    private LocalDateTime endDatetime;
    private int[][][] sums;
    private Double perceivedFahrenheit;
    private Double relativeHumidity;
    private Double hourlyPrecipitationInches;

    public QuarterDayData(Grid grid, WeatherReport.StationReport report) {
        this.sums = grid.calcSeverities();
        this.beginDatetime = report.getDatetime();
        // this.endDatetime = report.getEndDatetime(); --> not a function yet
        this.perceivedFahrenheit = report.getPerceivedFahrenheit();
        this.relativeHumidity = report.getRelativeHumidity();
        this.hourlyPrecipitationInches = report.getHourlyPrecipitationInches();
    }

    public int callsPerQuarterDay() {
        int rows = this.sums.length;
        int col = this.sums[0].length;
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

    public String toCSV() {
        CSVBuilder csvBuilder = new CSVBuilder();
        int col = this.sums[0].length;
        int rows = this.sums.length;
        int calls = callsPerQuarterDay();
        csvBuilder
            .append(this.beginDatetime.toString())
            .append(this.endDatetime.toString())
            .append(rows)
            .append(col)
            .append(calls)
            .append(this.perceivedFahrenheit)
            .append(this.hourlyPrecipitationInches)
            .append(this.relativeHumidity);
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