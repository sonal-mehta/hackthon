import java.util.Date;
import java.util.List;

/**
 * Created by sonalmehta on 5/8/17.
 */
public class SwipeRecord {

	private String empID;
	private String empName;
	private List<Date> date;
	private String firstIn;
	private String lastOut;
	private List<Date> ptoAppliedInNamelyMailNotSent;
	private List<Date> ptoMailSentNotAppliedInNamely;
	private List<Date> noPTOnoWFH;

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

	public List<Date> getDate() {
		return date;
	}

	public void setDate(List<Date> date) {
		this.date = date;
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


}
