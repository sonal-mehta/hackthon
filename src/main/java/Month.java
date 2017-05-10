import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by sonalmehta on 5/9/17.
 */
public class Month {
	static SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	InputStream inputStream;

	public static List<Date> getWorkDays(Date startDate, Date endDate) {
		List<String> workDays = new ArrayList<String>();
		List<String> holiDays = new ArrayList<String>();
		List<Date> masterDays = new ArrayList<Date>();
		try {
			//	Date startDate = fmt.parse("20/03/2017");
			//Date endDate = fmt.parse("20/04/2017");
			workDays = getWorkingDaysBetweenTwoDates(startDate, endDate);
			holiDays = getHolidays();
			workDays.removeAll(holiDays);

			for (String work : workDays) {
				masterDays.add(fmt.parse(work));
			}

			return masterDays;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> getWorkingDaysBetweenTwoDates(Date startDate, Date endDate) {
		List<String> workDays = new ArrayList<String>();
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);

		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);

		if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
			return null;
		}

		if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
			startCal.setTime(endDate);
			endCal.setTime(startDate);
		}

		do {
			if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				workDays.add(fmt.format(startCal.getTime()));
			}
			startCal.add(Calendar.DAY_OF_MONTH, 1);
		} while (startCal.getTimeInMillis() <= endCal.getTimeInMillis());
		return workDays;
	}

	public static List<String> getHolidays() throws IOException {
		try {
			PropertiesConfiguration prop = new PropertiesConfiguration("holidayList.properties");
			String[] holidays = prop.getStringArray("holiday");
			List<String> holidayList = Arrays.asList(holidays);
			return holidayList;
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		return null;
	}

}
