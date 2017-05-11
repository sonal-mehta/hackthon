/**
 * Created by sonalmehta on 5/8/17.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Swipe {
	static List<SwipeRecord> recordsMapList = new ArrayList<SwipeRecord>();
	static SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	static Date startDate, endDate;

	public static void main(String[] args) throws IOException {
		try {
			List<Date> presentDate = new ArrayList<Date>();
			String excelFilePath = "Ref data.xlsx";
			String excelFilePathNamely = "PTO.xlsx";
			if(args.length > 0){
				excelFilePath = args[0];
				excelFilePathNamely  = args[0];
			}
			recordsMapList = getList(excelFilePath);
			for (SwipeRecord record : recordsMapList) {
				List<Date> days = Month.getWorkDays(startDate, endDate);
				presentDate = record.getPresentDates();
				days.removeAll(presentDate);
				record.setAbsentDates(days);
				setPTO(record,excelFilePathNamely);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		MailClient.checkEmail(recordsMapList);

		MailClient.sendMailsForWFHandPTO(recordsMapList);
	}

	private static void setPTO (SwipeRecord swipeRecord, String excelFilePath) throws IOException, ParseException {


		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet sheet = workbook.getSheetAt(0);
		boolean isFirstTime = true;
		String name = "";
		Date  leaveStartDate= null;
		Date  leaveEndDate= null;
		boolean isApproved = true;
		Iterator<Row> itr = sheet.iterator();
		List<Date> lstPtoDate = new ArrayList<Date>();
		while (itr.hasNext()) {
			Row row = itr.next();
			if (!isFirstTime) {
				if(row.getCell(0) != null && row.getCell(1) != null) {
					name = row.getCell(0).getStringCellValue() + " " + row.getCell(1).getStringCellValue();
				}
				if(row.getCell(6) != null) {
					leaveStartDate= row.getCell(6).getDateCellValue();
				}
				if(row.getCell(7) != null) {
					leaveEndDate= row.getCell(7).getDateCellValue();
				}
				if(row.getCell(11) != null) {
					isApproved  = row.getCell(11).getBooleanCellValue();
				}
			}
			isFirstTime = false;
			if(name.equalsIgnoreCase(swipeRecord.getEmpName())){
				if (isApproved != false) {
					lstPtoDate.addAll(Month.getWorkDays(leaveStartDate, leaveEndDate));
				}
			}
		}
		Iterator<Date> iterator =  lstPtoDate.iterator();
		while (iterator.hasNext()){
			Date date = iterator.next();
			if(date.compareTo(Swipe.startDate)<0 || date.compareTo(Swipe.endDate)>0){
				iterator.remove();
			}
		}
		swipeRecord.setPto(lstPtoDate);
	}

	public static List<SwipeRecord> getList(String excelFilePath) throws IOException {
		excelFilePath = excelFilePath.replace("//", File.separator);
		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
		String[] startEndDates;

		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet firstSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = firstSheet.iterator();
		try {

			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Iterator<Cell> cellIterator = nextRow.cellIterator();
				List<Date> dates = new ArrayList<Date>();
				SwipeRecord swipeRecord = null;
				boolean isExisting = false;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getRowIndex() == 1 && cell.getColumnIndex() == 0) {
						String heading = cell.getStringCellValue();
						startEndDates = getStartEndDate(heading);
						startDate = fmt.parse(startEndDates[0]);
						endDate = fmt.parse(startEndDates[1]);
					} else if (cell.getRowIndex() > 4) {
						int colIndex = cell.getColumnIndex();
						switch (colIndex) {
							case 0:
								String empId = cell.getStringCellValue();
								if (recordsMapList.isEmpty()) {
									swipeRecord = new SwipeRecord();
								} else if (isExisting(empId) == null) {
									swipeRecord = new SwipeRecord();
								} else {
									swipeRecord = isExisting(empId);
									isExisting = true;
								}
								swipeRecord.setEmpID(empId);
								break;
							case 1:
								swipeRecord.setEmpName(cell.getStringCellValue());
								break;
							case 5:
								Date dateValue = fmt.parse(cell.getStringCellValue());
								if (isExisting) {
									swipeRecord.getPresentDates().add(dateValue);
								} else {
									dates.add(dateValue);
									swipeRecord.setPresentDates(dates);
								}
								break;
							case 6:
								swipeRecord.setFirstIn(cell.getStringCellValue());
								break;
							case 7:
								swipeRecord.setLastOut(cell.getStringCellValue());
								break;
						}
					}
				}
				if (!isExisting && swipeRecord != null) {
					recordsMapList.add(swipeRecord);
				}
				recordsMapList.size();
			}
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recordsMapList;
	}

	private static SwipeRecord isExisting(String empId) {
		for (SwipeRecord record : recordsMapList) {
			if (record != null && record.getEmpID().equalsIgnoreCase(empId)) {
				return record;
			}
		}
		return null;
	}

	private static String[] getStartEndDate(String desc) {
		int count = 0;
		String[] allMatches = new String[2];
		Matcher m = Pattern.compile("[\\d]{2}[\\/][\\d]{2}[\\/][\\d]{4}").matcher(desc);
		while (m.find()) {
			allMatches[count] = m.group();
			count++;
		}
		return allMatches;
	}

}
