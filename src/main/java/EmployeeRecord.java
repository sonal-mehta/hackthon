import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sonalmehta on 5/8/17.
 */
public class EmployeeRecord {

	private String empID;
	private String empName;
	private Date presentDate;
	private List<Date> presentDates = new ArrayList<Date>();
	private List<Date> absentDates = new ArrayList<Date>();
	private Map<Date, Long> presentDayTime = new HashMap<Date, Long>();
	private String firstIn;
	private String lastOut;
	private List<Date> ptoAppliedInNamelyMailNotSent = new ArrayList<Date>();
	private List<Date> ptoMailSentNotAppliedInNamely = new ArrayList<Date>();
	private List<Date> noPTOnoWFH = new ArrayList<Date>();
	private List<Date> pto = new ArrayList<Date>();
	private List<Date> halfDay = new ArrayList<Date>();
	private String firstName;
	private String lastName;
	private String emailAddress;

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}


	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


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

	public List<Date> getPtoAppliedInNamelyMailNotSent() {
		return ptoAppliedInNamelyMailNotSent;
	}

	public void setAbsentDates(List<Date> absentDates) {
		this.absentDates = absentDates;
	}

	public List<Date> getPtoMailSentNotAppliedInNamely() {
		return ptoMailSentNotAppliedInNamely;
	}

	public List<Date> getNoPTOnoWFH() {
		return noPTOnoWFH;
	}

	public List<Date> getPto() {
		return pto;
	}

	public void setPtoAppliedInNamelyMailNotSent(List<Date> ptoAppliedInNamelyMailNotSent) {
		this.ptoAppliedInNamelyMailNotSent = ptoAppliedInNamelyMailNotSent;
	}

	public void setPtoMailSentNotAppliedInNamely(List<Date> ptoMailSentNotAppliedInNamely) {
		this.ptoMailSentNotAppliedInNamely = ptoMailSentNotAppliedInNamely;
	}

	public void setNoPTOnoWFH(List<Date> noPTOnoWFH) {
		this.noPTOnoWFH = noPTOnoWFH;
	}

	public void setPto(List<Date> pto) {
		this.pto = pto;
	}

	public Map<Date, Long> getPresentDayTime() {
		return presentDayTime;
	}

	public void setPresentDayTime(Map<Date, Long> presentDayTime) {
		this.presentDayTime = presentDayTime;
	}

	public Date getPresentDate() {
		return presentDate;
	}

	public void setPresentDate(Date presentDate) {
		this.presentDate = presentDate;
	}

	public List<Date> getHalfDay() {
		return halfDay;
	}

	public void setHalfDay(List<Date> halfDay) {
		this.halfDay = halfDay;
	}
}
