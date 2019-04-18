package datacollection;

import java.util.Comparator;

/** Custom PoliceCall comparator for date/time.
 *  @author Jessica Su
 */
public class DateComparator implements Comparator<PoliceCall> {
     @Override
     public int compare(PoliceCall call1, PoliceCall call2) {
         return call1.getDatetime().compareTo(call2.getDatetime());
     }
}