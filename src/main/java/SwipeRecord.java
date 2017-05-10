import java.util.Date;
import java.util.List;

/**
 * Created by sonalmehta on 5/8/17.
 */
public class SwipeRecord {

	private String empID;
	private String empName;
	private List<Date> presentDates;
	private List<Date> absentDates;
	private String firstIn;
	private String lastOut;
	private List<Date> pto;

	public List<Date> getPto() { return pto; }

	public void setPto(List<Date> pto) { this.pto = pto; }

	public String getEmpID() {
		return empID;
	}

	public void setEmpID(String empID) {
		this.empID = empID;
	}

	public String getEmpName() {
		return empName;
	}

	public void setEmpName(String empName) {
		this.empName = empName;
	}

	public List<Date> getPresentDates() {
		return presentDates;
	}

	public void setPresentDates(List<Date> presentDates) {
		this.presentDates = presentDates;
	}

	public String getFirstIn() {
		return firstIn;
	}

	public void setFirstIn(String firstIn) {
		this.firstIn = firstIn;
	}

	public String getLastOut() {
		return lastOut;
	}

	public void setLastOut(String lastOut) {
		this.lastOut = lastOut;
	}

	public List<Date> getAbsentDates() {
		return absentDates;
	}

	public void setAbsentDates(List<Date> absentDates) {
		this.absentDates = absentDates;
	}
}
