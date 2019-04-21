package datacollection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

/** Grid Object class.
 * @author Jessica Su
 */
public class Grid {
    private double minLat;
    private double maxLat;
    private double minLong;
    private double maxLong;
    private final int bins = 5;
    private LocalDateTime dateTime;

    private ArrayList<PoliceCall>[][] ra;
    private int[][][] sums;

    /** Grid Constructor Used to set Default Min/Max
     *  Min set to max possible for comparison purposes.
     */
    public Grid() {
        minLat = 100.;
        maxLat = 0.;
        minLong = 200.;
        maxLong = 0.;
        ra = new ArrayList[this.bins][this.bins];
        sums = new int[this.bins][this.bins][4];
    }

    /** Set Maximum Latitude. */
    public void setMaxLat(double lat) {
        this.maxLat = lat;
    }

    /** Set Minimum Latitude. */
    public void setMinLat(double lat) {
        this.minLat = lat;
    }

    /** Set Maximum Longitude. */
    public void setMaxLong(double lon) {
        this.maxLong = lon;
    }

    /** Set Minimum Longitude. */
    public void setMinLong(double lon) {
        this.minLong = lon;
    }

    /** Set Date and Time. */
    public void setDateTime(LocalDateTime datetime) {
        this.dateTime = datetime;
    }

    /** Return Date and Time. */
    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    /** Return Maximum Latitude. */
    public double getMaxLat() {
        return this.maxLat;
    }

    /** Return Minimum Latitude. */
    public double getMinLat() {
        return this.minLat;
    }

    /** Return Maximum Longitude. */
    public double getMaxLong() {
        return this.maxLong;
    }

    /** Return Minimum Longitude. */
    public double getMinLong() {
        return this.minLong;
    }

    /** Return bins. */
    public int getBins() {
        return this.bins;
    }

    /** Insert police call into the grid.
     * @param call to be inserted
     */
    public void insertCall(PoliceCall call) {
        if (this.ra == null) {
            throw new IllegalArgumentException("Bins haven't been set");
        }
        int rows = rowsCalc(call.getLatitude());
        int col = colCalc(call.getLongitude());
        if (this.ra[rows][col] == null) {
            this.ra[rows][col] = new ArrayList<>();
        }
        this.ra[rows][col].add(call);
    }

    /** Calculate the row that the call at lat belongs to.
     * @param lat the latitude of the call
     * @return the row of the grid
     */
    private int rowsCalc(double lat) {
        if (lat == this.maxLat) {
            return 0;
        } else if (lat == this.minLat) {
            return this.bins - 1;
        } else {
            double latInc = (this.maxLat - this.minLat) / this.bins;
            int x = (int) Math.floor((lat - this.minLat) / latInc);
            return this.bins - x - 1; // latitude increases bottom up so rows must be reversed
        }
    }

    /** Calculate the column that the call at lon belongs to.
     * @param lon the longitude of the call
     * @return the column of the grid
     */
    private int colCalc(double lon) {
        if (lon == this.minLong) {
            return 0;
        } else if (lon == this.maxLong) {
            return this.bins - 1;
        } else {
            double longInc = (this.maxLong - this.minLong) / this.bins;
            int x = (int) Math.floor((lon - this.minLong) / longInc);
            return x;
        }
    }

    /** Return the list of police calls at a certain box.
     * @param row the row
     * @param col the column
     * @return the list of calls
     */
    public ArrayList<PoliceCall> getCallsPerBox(int row, int col) {
        return this.ra[row][col];
    }

    /** Calculate the total occurrences of each severity.
     * @return the 3D array of sums
     */
    public int[][][] calcSeverities() {
        for (int i = 0; i < this.bins; i++) {
            for (int j = 0; j < this.bins; j++) {
                if (this.ra[i][j] != null) {
                    for (PoliceCall call : this.ra[i][j]) {
                        int sev = call.getSeverity();
                        this.sums[i][j][sev] += 1;
                    }
                }
            }
        }
        return this.sums;
    }
}