package datacollection;

import java.util.ArrayList;
import java.util.Comparator;

/** Grid Object class.
 * @author Jessica Su
 */
public class Grid {
    private double minLat;;
    private double maxLat;
    private double minLong;
    private double maxLong;
    private final int bins = 5;
    private ArrayList<PoliceCall>[][] ra; // Must manually set bins before ra is created

    /** Grid Constructor Used to set Default Min/Max
     *  Min set to max possible for comparison purposes.
     */
    public Grid() {
        minLat = 100.;
        maxLat = 0.;
        minLong = 200.;
        maxLong = 0.;
        ra = new ArrayList[this.bins][this.bins];
    }

    public void setMaxLat(double lat) {
        this.maxLat = lat;
    }

    public void setMinLat(double lat) {
        this.minLat = lat;
    }

    public void setMaxLong(double lon) {
        this.maxLat = lon;
    }

    public void setMinLong(double lon) {
        this.minLong = lon;
    }

    public double getMaxLat() {
        return this.maxLat;
    }

    public double getMinLat() {
        return this.minLat;
    }

    public double getMaxLong() {
        return this.maxLong;
    }

    public double getMinLong() {
        return this.minLong;
    }

    public int getBins() {
        return this.bins;
    }

    public void insertCall(PoliceCall call) {
        if (this.ra == null) {
            throw new IllegalArgumentException("Bins haven't been set");
        }
        int rows = rowsCalc(call.getLatitude()) - 1; // index starts at 0
        int col = colCalc(call.getLongitude()) - 1; // index starts at 0
        if (this.ra[rows][col] == null) {
            this.ra[rows][col] = new ArrayList<>();
        }
        this.ra[rows][col].add(call);
    }

    private int rowsCalc(double lat) {
        double latInc = (this.maxLat - this.minLat) / this.bins;
        int x = (int) Math.round(lat / latInc);
        return this.bins - x; // latitude increases bottom up so rows must be reversed
    }

    private int colCalc(double lon) {
        double longInc = (this.maxLong - this.minLong) / this.bins;
        return (int) Math.round(lon / longInc);
    }

    public ArrayList<PoliceCall> getCallsPerBox(int row, int col) {
        return this.ra[row][col];
    }
}