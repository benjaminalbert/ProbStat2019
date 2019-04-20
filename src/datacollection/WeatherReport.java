package datacollection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Benjamin Albert
 */
public class WeatherReport {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private StationReport[] stationReports;
    private Double fahrenheit;
    private Double relativeHumidity;
    private Double perceivedFahrenheit;
    private Double hourlyPrecipitationInches;

    public WeatherReport(StationReport... stationReports) {
        this.stationReports = stationReports;
    }

    public int getStationIndex(String stationName) {
        for (int x = 0; x < stationReports.length; x++) {
            if (stationReports[x].stationName.equals(stationName)) {
                return x;
            }
        }
        return -1;
    }

    public void fillNull(String stationName) {
        fillNull(getStationIndex(stationName));
    }

    public void fillNull(int station) {
        if (this.fahrenheit == null) {
            this.fahrenheit = stationReports[station].fahrenheit;
        }
        if (this.relativeHumidity == null) {
            this.relativeHumidity = stationReports[station].relativeHumidity;
        }
        if (this.perceivedFahrenheit == null) {
            this.perceivedFahrenheit = stationReports[station].perceivedFahrenheit;
        }
        if (this.hourlyPrecipitationInches == null) {
            this.hourlyPrecipitationInches = stationReports[station].hourlyPrecipitationInches;
        }
    }

    public static class StationReport {

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
        
        
    }
}
