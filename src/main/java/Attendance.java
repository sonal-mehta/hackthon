/**
 * Created by sonalmehta on 5/8/17.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Attendance {
	static List<EmployeeRecord> recordsMapList = new ArrayList<EmployeeRecord>();
	static SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	static Date startDate, endDate;

	public static void main(String[] args) throws IOException {
		try {
			List<Date> presentDate = new ArrayList<Date>();
			String excelFilePath = "Ref data.xlsx";
			String excelFilePathNamely = "PTO.xlsx";
			String excelFilePathEmployeeInfo = "Employee.xlsx";
			if (args.length > 0) {
				excelFilePath = args[0];
				excelFilePathNamely = args[1];
				excelFilePathEmployeeInfo = args[2];
			}

			recordsMapList = getEmployeeList(excelFilePathEmployeeInfo);
			recordsMapList = getSwipeDataForEmployee(excelFilePath);

			for (EmployeeRecord record : recordsMapList) {
				List<Date> days = DateUtils.getWorkDays(startDate, endDate);
				presentDate = record.getPresentDates();
				//System.out.println(record.getEmpName());
				days.removeAll(presentDate);
				record.setAbsentDates(days);
				setPTO(record, excelFilePathNamely);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		MailClient.checkEmail(recordsMapList);

		MailClient.sendMailsForWFHandPTO(recordsMapList);
	}

	private static void setPTO(EmployeeRecord employeeRecord, String excelFilePath) throws IOException, ParseException {


		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet sheet = workbook.getSheetAt(0);
		boolean isFirstTime = true;
		String name = "";
		Date leaveStartDate = null;
		Date leaveEndDate = null;
		boolean isApproved = true;
		double daysOff ;
		Iterator<Row> itr = sheet.iterator();
		List<Date> lstPtoDate = new ArrayList<Date>();
		List<Date> lsthalfPtoDate = new ArrayList<Date>();
		while (itr.hasNext()) {
			Row row = itr.next();
			if (!isFirstTime) {
				if (row.getCell(0) != null && row.getCell(1) != null) {
					name = row.getCell(0).getStringCellValue() + " " + row.getCell(1).getStringCellValue();
				}
				if (row.getCell(6) != null) {
					leaveStartDate = row.getCell(6).getDateCellValue();
				}
				if (row.getCell(7) != null) {
					leaveEndDate = row.getCell(7).getDateCellValue();
				}
				if (row.getCell(11) != null) {
					isApproved = row.getCell(11).getBooleanCellValue();
				}
				if (row.getCell(9) != null) {
					daysOff = row.getCell(9).getNumericCellValue();
				}
			}
			isFirstTime = false;
			if (name.equalsIgnoreCase(employeeRecord.getEmpName())) {
				if (isApproved != false) {
					List<Date> lstBetDates = DateUtils.getWorkDays(leaveStartDate, leaveEndDate);
					for(Date date:lstBetDates) {
						if(employeeRecord.getPresentDates().contains(date)){
							lsthalfPtoDate.add(date);
						}else{
							lstPtoDate.add(date);
						}
					}
				}
			}
		}
		Iterator<Date> iterator = lstPtoDate.iterator();
		Iterator<Date> iteratorHalf = lsthalfPtoDate.iterator();
		while (iterator.hasNext()) {
			Date date = iterator.next();
			if (date.compareTo(Attendance.startDate) < 0 || date.compareTo(Attendance.endDate) > 0) {
				iterator.remove();
			}
		}
		while (iteratorHalf.hasNext()) {
			Date date = iteratorHalf.next();
			if (date.compareTo(Attendance.startDate) < 0 || date.compareTo(Attendance.endDate) > 0) {
				iteratorHalf.remove();
			}
		}
		employeeRecord.setPto(lstPtoDate);
		employeeRecord.setPtoHalfDay(lsthalfPtoDate);
	}

	private static EmployeeRecord isExisting(String empName) {
		for (EmployeeRecord record : recordsMapList) {
			if (record != null && record.getEmpName().trim().equalsIgnoreCase(empName)) {
				return record;
			}
		}
		//System.out.println(empName);
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

	public static List<EmployeeRecord> getEmployeeList(String excelFilePath) throws IOException {
		excelFilePath = excelFilePath.replace("//", File.separator);
		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet firstSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = firstSheet.iterator();
		try {
			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Iterator<Cell> cellIterator = nextRow.cellIterator();
				EmployeeRecord employeeRecord = null;
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getRowIndex() >= 1) {
						int colIndex = cell.getColumnIndex();
						switch (colIndex) {
							case 0:
								employeeRecord = new EmployeeRecord();
								employeeRecord.setEmpName(cell.getStringCellValue());
								break;
							case 1:
								employeeRecord.setFirstName(cell.getStringCellValue());
								break;
							case 2:
								employeeRecord.setLastName(cell.getStringCellValue());
								break;
							case 3:
								employeeRecord.setEmailAddress(cell.getStringCellValue());
								break;
							case 4:
								employeeRecord.setEmpID(cell.getStringCellValue());
								break;
						}
					}
				}
				if (employeeRecord != null) {
					recordsMapList.add(employeeRecord);
				}
			}
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recordsMapList;

	}

	public static List<EmployeeRecord> getSwipeDataForEmployee(String excelFilePath) throws IOException {
		excelFilePath = excelFilePath.replace("//", File.separator);
		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
		String[] startEndDates;
		int rowIndex=0;

		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet firstSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = firstSheet.iterator();
		try {

			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				Iterator<Cell> cellIterator = nextRow.cellIterator();
				List<Date> dates = new ArrayList<Date>();
				EmployeeRecord employeeRecord = null;

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getRowIndex() == 1 && cell.getColumnIndex() == 0) {
						String heading = cell.getStringCellValue();
						startEndDates = getStartEndDate(heading);
						startDate = fmt.parse(startEndDates[0]);
						endDate = fmt.parse(startEndDates[1]);
					} else if (cell.getRowIndex() > 4) {
						rowIndex=cell.getRowIndex();
						int colIndex = cell.getColumnIndex();
						switch (colIndex) {
							case 1:
								String empName = cell.getStringCellValue().trim();
								employeeRecord = isExisting(empName);
								break;
							case 5:
								if (employeeRecord != null) {
									Date dateValue = fmt.parse(cell.getStringCellValue());
									employeeRecord.setPresentDate(dateValue);
									if (employeeRecord.getPresentDates() != null) {
										employeeRecord.getPresentDates().add(dateValue);
									} else {
										dates.add(dateValue);
										employeeRecord.setPresentDates(dates);
									}
								}
								break;
							case 6:
								if (employeeRecord != null) {
									employeeRecord.setFirstIn(cell.getStringCellValue());
								}
								break;
							case 7:
								if (employeeRecord != null) {
									employeeRecord.setLastOut(cell.getStringCellValue());
									break;
								}
						}
					}
				}
				if (rowIndex > 4) {
					if (StringUtils.isNotBlank(employeeRecord.getFirstIn()) && StringUtils.isNotBlank(employeeRecord.getLastOut())) {
						LocalTime firstIn = LocalTime.parse(employeeRecord.getFirstIn(), timeFormatter);
						LocalTime lastOut = LocalTime.parse(employeeRecord.getLastOut(), timeFormatter);
						Long difference = Duration.between(firstIn,lastOut).toMinutes();
						employeeRecord.getPresentDayTime().put(employeeRecord.getPresentDate(), difference);
						if (difference <= 240) {
							employeeRecord.getHalfDay().add(employeeRecord.getPresentDate());
						}
					}
				}
			}
			recordsMapList.size();

			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recordsMapList;
	}

}
