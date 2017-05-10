/**
 * Created by sonalmehta on 5/8/17.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/**
 * A dirty simple program that reads an Excel file.
 *
 * @author www.codejava.net
 */
public class Swipe {
	static List<SwipeRecord> recordsMapList = new ArrayList<SwipeRecord>();
	static String empId;

	public static void main(String[] args) throws IOException {
		String excelFilePath = "//Users//sonalmehta//Downloads//Ref data.xlsx";
		excelFilePath = excelFilePath.replace("//", File.separator);
		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet firstSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = firstSheet.iterator();


		while (iterator.hasNext()) {
			Row nextRow = iterator.next();
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			List<String> dates = new ArrayList<String>();
			SwipeRecord swipeRecord = null;
			boolean isExisting = false;
			while (cellIterator.hasNext()) {

				Cell cell = cellIterator.next();
				if (cell.getRowIndex() > 4) {

					int colIndex = cell.getColumnIndex();
					switch (colIndex) {
						case 0:
							String empId = cell.getStringCellValue();
							if (recordsMapList.isEmpty()) {
								swipeRecord = new SwipeRecord();
							} else if (isExisting(empId) == null) {
								swipeRecord = new SwipeRecord();
							}else {
									swipeRecord = isExisting(empId);
									isExisting = true;
								}
							swipeRecord.setEmpID(empId);
							break;
						case 1:
							swipeRecord.setEmpName(cell.getStringCellValue());
							break;
						case 5:
							String dateValue = cell.getStringCellValue();
							if (isExisting) {
								swipeRecord.getDate().add(dateValue);
							} else {
								dates.add(dateValue);
								swipeRecord.setDate(dates);
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
	}

	private static SwipeRecord isExisting(String empId) {
		for (SwipeRecord record : recordsMapList) {
			if (record != null && record.getEmpID().equalsIgnoreCase(empId)) {
				return record;
			}
		}

		return null;
	}

}
