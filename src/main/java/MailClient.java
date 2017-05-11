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

/**
 * Created by tauseef.husain on 5/10/17.
 */
public class MailClient {

	static SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
	static String popHost = "pop.gmail.com";
	static String smtphost = "smtp.gmail.com";
	static String username = "equation.academy@gmail.com";
	static String passw = "solution@786";// change accordingly

	public static void sendMailsForWFHandPTO(List<SwipeRecord> employees) {

		for (SwipeRecord employee : employees) {
			String name = "Hi " + employee.getEmpName() + ",\n";
			if (employee.getNoPTOnoWFH().size() > 0) {
				String abscentContent = "As per our record, you were absent on following dates:\n";
				for (Date date : employee.getNoPTOnoWFH()) {
					if (!(date.compareTo(Swipe.startDate) < 0 || date.compareTo(Swipe.endDate) > 0)) {
						abscentContent = abscentContent + date.toString() + "\n";
					}
				}
				abscentContent = abscentContent + "Note : Please apply leave on namely or in case you were doing WFH then drop a mail to attendance.";
				sendmail(getEmailAddress(employee.getEmpName()), "Attendance Reminder", name + abscentContent);
			}

			if (employee.getPtoAppliedInNamelyMailNotSent().size() > 0) {
				String sendMailToAttendance = "As per our record, you have applied PTO but didn't sent mail to attendance@appdirect.com for following dates:\n";
				for (Date date : employee.getPtoAppliedInNamelyMailNotSent()) {
					if (!(date.compareTo(Swipe.startDate) < 0 || date.compareTo(Swipe.endDate) > 0)) {
						sendMailToAttendance = sendMailToAttendance + date.toString() + "\n";
					}
				}
				sendmail(getEmailAddress(employee.getEmpName()), "PTO Notification", name + sendMailToAttendance);
			}

			if (employee.getPtoMailSentNotAppliedInNamely().size() > 0) {
				String applyLeave = "As per our record, you have sent an email to attendance@appdirect.com for PTO but you have not applied leave on namely for following dates:\n";
				for (Date date : employee.getPtoMailSentNotAppliedInNamely()) {
					if (!(date.compareTo(Swipe.startDate) < 0 || date.compareTo(Swipe.endDate) > 0)) {
						applyLeave = applyLeave + date.toString() + "\n";
					}
				}
				sendmail(getEmailAddress(employee.getEmpName()), "PTO Notification", name + applyLeave);
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
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(content);
			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<SwipeRecord> checkEmail(List<SwipeRecord> employees) {
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

			for (SwipeRecord employee : employees) {
				String requiredEmail = getEmailAddress(employee.getEmpName());
				Map<String, List<Date>> WFHAndPTODatesFromMail = getWFHAndPTODatesFromMailForEmployee(requiredEmail, messages);
				employee.setNoPTOnoWFH(employee.getAbsentDates());
				employee.setNoPTOnoWFH(mergeList(employee.getNoPTOnoWFH(), WFHAndPTODatesFromMail.get("WFH MAIL"), WFHAndPTODatesFromMail.get("PTO MAIL"), employee.getPto()));

				employee.setPtoAppliedInNamelyMailNotSent(employee.getPto());
				if (employee.getPtoAppliedInNamelyMailNotSent().size() > 0) {
					employee.getPtoAppliedInNamelyMailNotSent().removeAll(WFHAndPTODatesFromMail.get("PTO MAIL"));
				}


				employee.setPtoMailSentNotAppliedInNamely(WFHAndPTODatesFromMail.get("PTO MAIL"));
				if (employee.getPtoMailSentNotAppliedInNamely().size() > 0) {
					employee.getPtoMailSentNotAppliedInNamely().removeAll(employee.getPto());
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


	private static String getEmailAddress(String fullName) {
		return (fullName.trim().replace(" ", ".") + "@appdirect.com").toLowerCase();
	}

	private static Map<String, List<Date>> getWFHAndPTODatesFromMailForEmployee(String email, Message[] messages) {
		try {
			List<Date> WFH = new ArrayList<Date>();
			List<Date> PTO = new ArrayList<Date>();
			Map<String, List<Date>> WFHAndPTODate = new HashMap<String, List<Date>>();
			for (Message message : messages) {
				String from = message.getFrom()[0].toString();
				String subject = message.getSubject();

				if (from.contains(email) && subject.contains("[WFH]")) {
					getWFHDate(message.getSubject(), WFH);
				}
				if (from.contains(email) && subject.contains("[PTO]")) {
					getPTODate(message.getSubject(), PTO);
				}
			}
			WFHAndPTODate.put("WFH MAIL", WFH);
			WFHAndPTODate.put("PTO MAIL", PTO);
			return WFHAndPTODate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Date> getWFHDate(String subject, List<Date> WFH) {
		try {
			String dateInSubject = subject.trim().split("[|]")[1].trim();
			if (dateInSubject.contains("to")) {
				Date start = fmt.parse(dateInSubject.split("to")[0].trim());
				Date end = fmt.parse(dateInSubject.split("to")[1].trim());
				WFH.addAll(Month.getWorkDays(start, end));
			} else {
				WFH.add(fmt.parse(dateInSubject));
			}
			return WFH;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return WFH;
	}

	private static List<Date> getPTODate(String subject, List<Date> PTO) {
		try {
			String dateInSubject = subject.trim().split("[|]")[1].trim();
			if (dateInSubject.contains("to")) {
				Date start = fmt.parse(dateInSubject.split("to")[0].trim());
				Date end = fmt.parse(dateInSubject.split("to")[1].trim());
				PTO.addAll(Month.getWorkDays(start, end));
			} else {
				PTO.add(fmt.parse(dateInSubject));
			}
			return PTO;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return PTO;
	}

	private static List<Date> mergeList(List<Date> noPTONoWFH, List<Date> WFHFromMail, List<Date> PTOFromMail, List<Date> PTOFromNamely) {
		removeDuplicates(noPTONoWFH, WFHFromMail);
		removeDuplicates(noPTONoWFH, PTOFromMail);
		removeDuplicates(noPTONoWFH, PTOFromNamely);

		return noPTONoWFH;
	}

	private static List<Date> removeDuplicates(List<Date> list1, List<Date> list2) {
		Iterator<Date> list1Iterator = list1.iterator();
		while(list1Iterator.hasNext()) {
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
