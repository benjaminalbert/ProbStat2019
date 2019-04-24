package datacollection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author Benjamin Albert
 */
public class WeatherReport {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private HashMap<String, ArrayList<StationReport>> stationReports;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double fahrenheit;
    private Double relativeHumidity;
    private Double perceivedFahrenheit;
    private Double hourlyPrecipitationInches;

    public static enum AVERAGEABLE_STATION_REPORT_FIELD {
        FAHRENHEIT("fahrenheit"),
        RELATIVE_HUMIDITY("relativeHumidity"),
        PERCEIVED_FAHRENHEIT("perceivedFahrenheit"),
        HOURLY_PRECIPITATION_INCHES("hourlyPrecipitationInches");

        public final String fieldName;

        private AVERAGEABLE_STATION_REPORT_FIELD(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    public WeatherReport(ArrayList<StationReport> stationReports) {
        this.stationReports = new HashMap<>();
        for (StationReport stationReport : stationReports) {
            if (!this.stationReports.keySet().contains(stationReport.stationName)) {
                this.stationReports.put(stationReport.stationName, new ArrayList<>());
            }
            this.stationReports.get(stationReport.stationName).add(stationReport);
        }
    }

    public void fillNull(String... stationNames) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        if (this.fahrenheit == null) {
            this.fahrenheit = averageStationReportValue(AVERAGEABLE_STATION_REPORT_FIELD.FAHRENHEIT, stationNames);
        }
        if (this.relativeHumidity == null) {
            this.relativeHumidity = averageStationReportValue(AVERAGEABLE_STATION_REPORT_FIELD.RELATIVE_HUMIDITY, stationNames);
        }
        if (this.perceivedFahrenheit == null) {
            this.perceivedFahrenheit = averageStationReportValue(AVERAGEABLE_STATION_REPORT_FIELD.PERCEIVED_FAHRENHEIT, stationNames);
        }
        if (this.hourlyPrecipitationInches == null) {
            this.hourlyPrecipitationInches = averageStationReportValue(AVERAGEABLE_STATION_REPORT_FIELD.HOURLY_PRECIPITATION_INCHES, stationNames);
        }
    }

    private Double averageStationReportValue(AVERAGEABLE_STATION_REPORT_FIELD averageableStationReportField) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        return averageStationReportValue(averageableStationReportField, stationReports.keySet().toArray(new String[0]));
    }

    private Double averageStationReportValue(AVERAGEABLE_STATION_REPORT_FIELD averageableStationReportField, String... stationNames) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        if (this.stationReports == null || this.stationReports.isEmpty()) {
            return null;
        }
        double total = 0;
        int reports = 0;
        Field stationReportField = StationReport.class.getDeclaredField(averageableStationReportField.fieldName);
        for (String stationName : stationNames) {
            if (this.stationReports.get(stationName) == null) {
                continue;
            }
            for (StationReport stationReport : this.stationReports.get(stationName)) {
                if (Modifier.isPrivate(stationReportField.getModifiers())) {
                    stationReportField.setAccessible(true);
                }
                Object fieldValue = stationReportField.get(stationReport);
                if (fieldValue != null && (fieldValue instanceof Double || fieldValue instanceof Integer)) {
                    reports++;
                    if (fieldValue instanceof Double) {
                        total += ((Double) fieldValue);
                    } else if (fieldValue instanceof Integer) {
                        total += ((Integer) fieldValue);
                    }
                }
            }
        }
        if (reports == 0){
            return null;
        }
        return total / reports;
    }

    public static WeatherReport[] generateWeatherReports(StationReport[] stationReports, long hoursBetweenReports, boolean startIntervalsAtMidnight, String[] orderedStationPreferences) {
        if (24.0 % hoursBetweenReports != 0) {
            System.err.println("reportsPerDay (" + hoursBetweenReports + ") must be a factor of 24");
        }
        Collections.sort(Arrays.asList(stationReports), StationReport.DATE_TIME_COMPARATOR);
        LocalDateTime intervalBeginning = stationReports[0].datetime;
        /* if we do not start from the first station report, intervals start at midnight */
        if (startIntervalsAtMidnight) {
            intervalBeginning = LocalDateTime.of(intervalBeginning.getYear(), intervalBeginning.getMonth(), intervalBeginning.getDayOfMonth(), 0, 0);
        }
        LocalDateTime intervalEnding = intervalBeginning.plusHours(hoursBetweenReports);
        ArrayList<WeatherReport> weatherReports = new ArrayList<>();
        ArrayList<StationReport> currentStationReports = new ArrayList<>();
        for (StationReport stationReport : stationReports) {
            /* if stationReport is within the time interval */
            if (stationReport.datetime.isEqual(intervalBeginning) || (stationReport.datetime.isAfter(intervalBeginning) && stationReport.datetime.isBefore(intervalEnding))) {
                currentStationReports.add(stationReport);
            } else {
                if (!currentStationReports.isEmpty()) {
                    WeatherReport weatherReport = new WeatherReport(currentStationReports);
                    weatherReport.startDateTime = LocalDateTime.of(
                            intervalBeginning.getYear(),
                            intervalBeginning.getMonth(),
                            intervalBeginning.getDayOfMonth(),
                            intervalBeginning.getHour(),
                            intervalBeginning.getMinute());
                    weatherReport.endDateTime = LocalDateTime.of(
                            intervalEnding.getYear(),
                            intervalEnding.getMonth(),
                            intervalEnding.getDayOfMonth(),
                            intervalEnding.getHour(),
                            intervalEnding.getMinute());
                    try {
                        for (String stationName : orderedStationPreferences) {
                            weatherReport.fillNull(stationName);
                        }
                        weatherReports.add(weatherReport);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    currentStationReports.clear();
                }
                intervalBeginning = intervalEnding;
                intervalEnding = intervalBeginning.plusHours(hoursBetweenReports);
            }
        }
        return weatherReports.toArray(new WeatherReport[0]);
    }

    public HashMap<String, ArrayList<StationReport>> getStationReports() {
        return stationReports;
    }

    public void setStationReports(HashMap<String, ArrayList<StationReport>> stationReports) {
        this.stationReports = stationReports;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Double getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(Double fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public Double getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(Double relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public Double getPerceivedFahrenheit() {
        return perceivedFahrenheit;
    }

    public void setPerceivedFahrenheit(Double perceivedFahrenheit) {
        this.perceivedFahrenheit = perceivedFahrenheit;
    }

    public Double getHourlyPrecipitationInches() {
        return hourlyPrecipitationInches;
    }

    public void setHourlyPrecipitationInches(Double hourlyPrecipitationInches) {
        this.hourlyPrecipitationInches = hourlyPrecipitationInches;
    }

    public static class StationReport {

        public static final Comparator<StationReport> DATE_TIME_COMPARATOR = (StationReport p1, StationReport p2) -> p1.datetime.compareTo(p2.datetime);

        private final String stationName;
        private final LocalDateTime datetime;
        private final Double latitude;
        private final Double longitude;
        private final Double fahrenheit;
        private Double relativeHumidity;
        /* accounts for wind chill and heat index factors */
        private Double perceivedFahrenheit;
        private Double hourlyPrecipitationInches;

        public StationReport(String csv) throws Exception {
            String[] tokens = csv.split(",");
            stationName = tokens[0];
            datetime = LocalDateTime.parse(tokens[1], DATE_TIME_FORMATTER);
            longitude = Double.valueOf(tokens[2]);
            latitude = Double.valueOf(tokens[3]);
            fahrenheit = Double.valueOf(tokens[4]);
            try {
                relativeHumidity = Double.valueOf(tokens[5]);
            } catch (NumberFormatException e) {
            }
            try {
                perceivedFahrenheit = Double.valueOf(tokens[6]);
            } catch (NumberFormatException e) {
            }
            try {
                hourlyPrecipitationInches = Double.valueOf(tokens[7]);
            } catch (NumberFormatException e) {
            }
        }

        public static StationReport[] readStationReports(String weatherDataFile) throws FileNotFoundException, IOException {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(weatherDataFile));
            boolean skipLine = true;
            String line;
            ArrayList<StationReport> stationReports = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                if (skipLine) {
                    skipLine = false;
                    continue;
                }
                try {
                    stationReports.add(new StationReport(line));
                } catch (Exception e) {
                }
            }
            return stationReports.toArray(new StationReport[0]);
        }

        public Double getRelativeHumidity() {
            return relativeHumidity;
        }

        public void setRelativeHumidity(Double relativeHumidity) {
            this.relativeHumidity = relativeHumidity;
        }

        public Double getPerceivedFahrenheit() {
            return perceivedFahrenheit;
        }

        public void setPerceivedFahrenheit(Double perceivedFahrenheit) {
            this.perceivedFahrenheit = perceivedFahrenheit;
        }

        public Double getHourlyPrecipitationInches() {
            return hourlyPrecipitationInches;
        }

        public void setHourlyPrecipitationInches(Double hourlyPrecipitationInches) {
            this.hourlyPrecipitationInches = hourlyPrecipitationInches;
        }

        public LocalDateTime getDatetime() {
            return datetime;
        }

    }
}
