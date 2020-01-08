package datacollection;
import java.time.LocalDateTime;

public class QuarterDayData {

    private int[][][] sums;
    private WeatherReport weatherReport;

    public QuarterDayData(Grid grid, WeatherReport report) {
        this.sums = grid.calcSeverities();
        this.weatherReport = report;
    }

    public int callsPerQuarterDay() {
        int rows = this.sums.length;
        int col = this.sums[0].length;
        int sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < 5; k++) {
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
            .append(this.weatherReport.getStartDateTime().toString())
            .append(this.weatherReport.getEndDateTime().toString())
            .append(rows)
            .append(col)
            .append(calls);

        if (this.weatherReport.getFahrenheit() == null) {
            csvBuilder.append("null");
        } else {
            csvBuilder.append(this.weatherReport.getFahrenheit());
        }

        if (this.weatherReport.getHourlyPrecipitationInches() == null) {
            csvBuilder.append("null");
        } else {
            csvBuilder.append(this.weatherReport.getHourlyPrecipitationInches());
        }

        if (this.weatherReport.getRelativeHumidity() == null) {
            csvBuilder.append("null");
        } else {
            csvBuilder.append(this.weatherReport.getRelativeHumidity());
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < 5; k++) {
                    csvBuilder.append(this.sums[i][j][k]);
                }
            }
        }
        csvBuilder.newline();
        return csvBuilder.toCSV();
    }

}