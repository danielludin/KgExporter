package ch.gpb.elexis.kgexporter.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {
    protected static Logger log = LoggerFactory.getLogger(DateUtil.class.getName());

    public DateUtil() {
	// TODO Auto-generated constructor stub
    }

    public static Date getDateFromGermanFormat(String sDate) {
	DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	Date date = null;

	try {
	    date = df.parse(sDate);
	} catch (ParseException e) {
	    log.error("Error converting date: " + e.getMessage());
	    //System.out.println("Error converting date: " + e.getMessage());
	}
	return date;

    }

    /**
     * Converts the "elexis-date-format" (String like "yyyyMMdd") into a date object
     * @param sDate
     * @return date
     */
    /*
    public static Date getDateFromCompact(String sDate) {
    DateFormat df = new SimpleDateFormat("yyyyMMdd");
    try {
        return df.parse(sDate);
    } catch (ParseException e) {
        log.error("Error parsing date: /" + sDate + "/" + e.toString(), Log.ERRORS);
        System.out.println("Error parsing date: /" + sDate + "/" + e.toString());
    }
    return null;
    }
     */
}
