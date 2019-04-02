package datacollection;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Benjamin Albert
 */
public class PoliceCall {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

    private String recordId;
    private LocalDateTime datetime;
    private String address;
    private String description;
    private int severity;
    private String district;
    private String number;
    private String incidentLocation;
    private String callLocation;
    private double latitude;
    private double longitude;

    public static class Filter {

        private ArrayList<Integer> severities;
        private ArrayList<String> descriptionBlacklist;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private boolean requireCoordinate;

        public Filter() {
            severities = new ArrayList<>();
            descriptionBlacklist = new ArrayList<>();
            severities.addAll(Arrays.asList(new Integer[]{0, 1, 2, 3}));
            startDate = LocalDateTime.MIN;
            endDate = LocalDateTime.MAX;
            requireCoordinate = false;
        }

        public boolean pass(PoliceCall policeCall) {
            return severities.contains(policeCall.severity)
                    && startDate.isBefore(policeCall.datetime)
                    && endDate.isAfter(policeCall.datetime)
                    && ((requireCoordinate && (policeCall.latitude != 0 && policeCall.longitude != 0)) || !requireCoordinate);
        }

        public ArrayList<Integer> getSeverities() {
            return severities;
        }

        public void setSeverities(ArrayList<Integer> severities) {
            this.severities = severities;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
        }

        public boolean isRequireCoordinate() {
            return requireCoordinate;
        }

        public void setRequireCoordinate(boolean requireCoordinate) {
            this.requireCoordinate = requireCoordinate;
        }

        public ArrayList<String> getDescriptionBlacklist() {
            return descriptionBlacklist;
        }

        public void setDescriptionBlacklist(ArrayList<String> descriptionBlacklist) {
            this.descriptionBlacklist = descriptionBlacklist;
        }
    }

    /**
     * @param crimeDataFile csv file with columns:
     * <ol>
     * <li>record id (int)</li>
     * <li>call date and time (MM/dd/yyyy hh:mm:00 AM/PM)</li>
     * <li>severity (Non-Emergency, Low, Medium, High)</li>
     * <li>district (e.g. ND, SW, NE, etc)</li>
     * <li>description (e.g. SILENT ALARM, DISORDERLY, 911/HANGUP, Private Tow,
     * 911/NO VOICE, etc)</li>
     * <li>incident location (e.g. 400 WINSTON AV)</li>
     * <li>call location (e.g. "400 WINSTON AV)</li>
     * </ol>
     *
     * NOTE: the data is messy: call location often spans multiple rows with a
     * distinct but inconsistent pattern: before the city and state (BALTIMORE,
     * MD) data, there is usually line break. However, not always as in the case
     * of ("COPPIN STATE UNIVERSITY BALTIMORE, MD) which appears on a single
     * line. The longitude and latitude (which usually provided, but not always)
     * appear on the next line as follows in the pattern: (39.316763,
     * -76.595269)" the entry almost always ends with quotation mark (")
     *
     * @return array of PoliceCall objects
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static PoliceCall[] readPoliceCalls(String crimeDataFile, Filter filter) throws FileNotFoundException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(crimeDataFile));
        boolean skipLine = true;
        String line;
        ArrayList<PoliceCall> policeCalls = new ArrayList<>();
        PoliceCall policeCall = new PoliceCall();
        while ((line = bufferedReader.readLine()) != null) {

            if (skipLine) {
                skipLine = false;
                continue;
            }

            try {

                if (line.length() > 30) {
                    String[] tokens = line.split(",");
                    policeCall.setRecordId(tokens[0]);
                    policeCall.setDatetime(LocalDateTime.parse(tokens[1], dateTimeFormatter));
                    policeCall.setSeverity(PoliceCall.severity(tokens[2]));
                    policeCall.setDistrict(tokens[3]);
                    policeCall.setDescription(tokens[4]);
                    policeCall.setNumber(tokens[5].substring(1));
                    policeCall.setIncidentLocation(tokens[6]);
                    policeCall.setCallLocation(tokens[7].substring(1));
                } else if (line.contains("BALTIMORE")) {
                    policeCall.setCallLocation(policeCall.getCallLocation() + " " + (line.endsWith("\"") ? line.substring(0, line.length() - 1) : line));
                } else {
                    policeCall.setLongitude(Double.valueOf(line.substring(1, line.indexOf(","))));
                    policeCall.setLatitude(Double.valueOf(line.substring(line.indexOf(" ") + 1, line.indexOf(")"))));
                }
                if (line.endsWith("\"") && (filter == null || filter.pass(policeCall))) {
                    policeCalls.add(policeCall);
                    policeCall = new PoliceCall();
                }
            } catch (Exception e) {
                policeCall = new PoliceCall();
            }
        }

        return policeCalls.toArray(new PoliceCall[0]);
    }

    public static void write(PoliceCall[] policeCalls, String saveFilePath) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(saveFilePath));
        String content;
        if (saveFilePath.endsWith("json")) {
            content = new Gson().toJson(policeCalls);
        } else {
            content = toCSV(policeCalls);
        }
        bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private static String toCSV(PoliceCall[] policeCalls) {
        CSVBuilder csvBuilder = new CSVBuilder();
        for (PoliceCall policeCall : policeCalls) {
            csvBuilder
                    .append(policeCall.severity)
                    .append(policeCall.datetime.toString())
                    .append(policeCall.longitude)
                    .append(policeCall.latitude)
                    .append(policeCall.description)
                    .newline();
        }
        return csvBuilder.toCSV();
    }

    public static int severity(String severity) {
        switch (severity) {
            case "Non-Emergency":
                return 0;
            case "Low":
                return 1;
            case "Medium":
                return 2;
            case "High":
                return 3;
            default:
                return -1;
        }
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(String incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public String getCallLocation() {
        return callLocation;
    }

    public void setCallLocation(String callLocation) {
        this.callLocation = callLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
