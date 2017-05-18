import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by tauseef.husain on 5/10/17.
 */
public class MailClient {

	static SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	static String popHost;
	static String smtphost;
	static String username;
	static String passw;

	static {
		try {
			PropertiesConfiguration prop = new PropertiesConfiguration("holidayList.properties");
			popHost = prop.getString("popHost");
			smtphost = prop.getString("smtphost");
			username = prop.getString("username");
			passw = prop.getString("passw");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

	public static void sendMailsForWFHandPTO(List<EmployeeRecord> employees) {

		for (EmployeeRecord employee : employees) {
			String name = "Hi " + employee.getEmpName() + ",\n";
			if (employee.getNoPTOnoWFH().size() > 0) {
				String abscentContent = "As per our record, you were absent on following dates:\n";
				for (Date date : employee.getNoPTOnoWFH()) {
					if (!(date.compareTo(Attendance.startDate) < 0 || date.compareTo(Attendance.endDate) > 0)) {
						abscentContent = abscentContent + date.toString() + "\n";
					}
				}
				abscentContent = abscentContent + "Note : Please apply leave on namely or in case you were doing WFH then drop a mail to attendance.";
				sendmail(employee.getEmailAddress(), "Notification: Absent Days", name + abscentContent);
			}

			if (employee.getPtoAppliedInNamelyMailNotSent().size() > 0) {
				String sendMailToAttendance = "As per our record, you have applied PTO but didn't sent mail to attendance@appdirect.com for following dates:\n";
				for (Date date : employee.getPtoAppliedInNamelyMailNotSent()) {
					if (!(date.compareTo(Attendance.startDate) < 0 || date.compareTo(Attendance.endDate) > 0)) {
						sendMailToAttendance = sendMailToAttendance + date.toString() + "\n";
					}
				}
				sendmail(employee.getEmailAddress(), "Notification: PTO Mail Not Found", name + sendMailToAttendance);
			}

			if (employee.getPtoMailSentNotAppliedInNamely().size() > 0) {
				String applyLeave = "As per our record, you have sent an email to attendance@appdirect.com for PTO but you have not applied leave on namely for following dates:\n";
				for (Date date : employee.getPtoMailSentNotAppliedInNamely()) {
					if (!(date.compareTo(Attendance.startDate) < 0 || date.compareTo(Attendance.endDate) > 0)) {
						applyLeave = applyLeave + date.toString() + "\n";
					}
				}
				sendmail(employee.getEmailAddress(), "Notification: Apply PTO On Namely", name + applyLeave);
			}
		}
	}

	private static void sendmail(String to, String subject, String content) {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", "587");

		// Get the Session object.
		Session session = Session.getInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, passw);
				}
			});

		try {
			Message message = new MimeMessage(session);
			message.setHeader("X-Priority", "1");
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(content);
			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<EmployeeRecord> checkEmail(List<EmployeeRecord> employees) {
		try {
			Properties properties = new Properties();
			properties.put("mail.pop3.host", popHost);
			properties.put("mail.pop3.port", "995");
			properties.put("mail.pop3.starttls.enable", "true");
			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore("pop3s");
			store.connect(popHost, username, passw);
			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_ONLY);
			Message[] messages = emailFolder.getMessages();

			for (EmployeeRecord employee : employees) {
				Map<String, List<Date>> datesFromMail = getDatesFromMailForEmployee(employee.getEmailAddress(), messages);
				List<Date> noPtoNoWFH = new ArrayList<Date>(employee.getAbsentDates());
				employee.setNoPTOnoWFH(noPtoNoWFH);
				employee.setNoPTOnoWFH(mergeList(employee.getNoPTOnoWFH(), datesFromMail.get("WFH MAIL"), datesFromMail.get("PTO MAIL"), employee.getPto(), datesFromMail.get("FAC MAIL"), datesFromMail.get("OOO MAIL")));

				List<Date> ptoApplied = new ArrayList<Date>(employee.getPto());
				employee.setPtoAppliedInNamelyMailNotSent(ptoApplied);
				if (employee.getPtoAppliedInNamelyMailNotSent().size() > 0) {
					employee.getPtoAppliedInNamelyMailNotSent().removeAll(datesFromMail.get("PTO MAIL"));
				}

				List<Date> ptoNotApplied = new ArrayList<Date>(employee.getPto());
				employee.setPtoMailSentNotAppliedInNamely(datesFromMail.get("PTO MAIL"));
				if (employee.getPtoMailSentNotAppliedInNamely().size() > 0) {
					employee.getPtoMailSentNotAppliedInNamely().removeAll(ptoNotApplied);
				}
			}

			emailFolder.close(false);
			store.close();
			return employees;
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Map<String, List<Date>> getDatesFromMailForEmployee(String email, Message[] messages) {
		try {
			List<Date> WFH = new ArrayList<Date>();
			List<Date> PTO = new ArrayList<Date>();
			List<Date> FAC = new ArrayList<Date>();
			List<Date> OOO = new ArrayList<Date>();
			Map<String, List<Date>> WFHAndPTODate = new HashMap<String, List<Date>>();
			for (Message message : messages) {
				String from = message.getFrom()[0].toString();
				String subject = message.getSubject();

				if(from.contains(email))
				{
					getMailDate(subject, WFH, PTO, FAC, OOO);
				}
			}
			WFHAndPTODate.put("WFH MAIL", WFH);
			WFHAndPTODate.put("PTO MAIL", PTO);
			WFHAndPTODate.put("FAC MAIL", FAC);
			WFHAndPTODate.put("OOO MAIL", OOO);
			return WFHAndPTODate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Date> getMailDate(String subject, List<Date> WFH, List<Date> PTO, List<Date> FAC, List<Date> OOO) {
		try {
			String dateInSubject = subject.trim().split("[|]")[1].trim();
			if (dateInSubject.contains("to")) {
				Date start = fmt.parse(dateInSubject.split("to")[0].trim());
				Date end = fmt.parse(dateInSubject.split("to")[1].trim());
				if(subject.contains("[WFH]"))
				{
					WFH.addAll(DateUtils.getWorkDays(start, end));
				}else if(subject.contains("[PTO]"))
				{
					PTO.addAll(DateUtils.getWorkDays(start, end));
				}else if(subject.contains("[FAC]"))
				{
					FAC.addAll(DateUtils.getWorkDays(start, end));
				}else if(subject.contains("[OOO]"))
				{
					OOO.addAll(DateUtils.getWorkDays(start, end));
				}
			} else {
				if(subject.contains("[WFH]"))
				{
					WFH.add(fmt.parse(dateInSubject));
				}else if(subject.contains("[PTO]"))
				{
					PTO.add(fmt.parse(dateInSubject));
				}else if(subject.contains("[FAC]"))
				{
					FAC.add(fmt.parse(dateInSubject));
				}else if(subject.contains("[OOO]"))
				{
					OOO.add(fmt.parse(dateInSubject));
				}
			}
			return WFH;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return WFH;
	}

	private static List<Date> mergeList(List<Date> noPTONoWFH, List<Date> WFHFromMail, List<Date> PTOFromMail, List<Date> PTOFromNamely, List<Date> FACFromMail, List<Date> OOOFromMail) {
		removeDuplicates(noPTONoWFH, WFHFromMail);
		removeDuplicates(noPTONoWFH, PTOFromMail);
		removeDuplicates(noPTONoWFH, PTOFromNamely);
		removeDuplicates(noPTONoWFH, FACFromMail);
		removeDuplicates(noPTONoWFH, OOOFromMail);

		return noPTONoWFH;
	}

	private static List<Date> removeDuplicates(List<Date> list1, List<Date> list2) {
		Iterator<Date> list1Iterator = list1.iterator();
		while (list1Iterator.hasNext()) {
			Date date1 = list1Iterator.next();
			for (Date date2 : list2) {
				if (date1.compareTo(date2) == 0) {
					list1Iterator.remove();
				}
			}
		}
		return list1;
	}
}
