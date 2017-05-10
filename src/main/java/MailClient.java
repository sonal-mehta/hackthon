import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by tauseef.husain on 5/10/17.
 */
public class MailClient {

	SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
	String popHost = "pop.gmail.com";
	String username = "equation.academy@gmail.com";
	String passw = "solution@786";// change accordingly

//	public static void main(String[] args) {
//		// Recipient's email ID needs to be mentioned.
//		String to = "ttauseef01@gmail.com";
//
//		// Sender's email ID needs to be mentioned
//		String from = "equation.academy@gmail.com";
//		final String username = "equation.academy@gmail.com";//change accordingly
//		final String passw = "solution@786";//change accordingly
//
//		// Assuming you are sending email through relay.jangosmtp.net
//		String host = "smtp.gmail.com";
//
//		Properties props = new Properties();
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.host", host);
//		props.put("mail.smtp.port", "587");
//
//		// Get the Session object.
//		Session session = Session.getInstance(props,
//			new javax.mail.Authenticator() {
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication(username, passw);
//				}
//			});
//
//		try {
//			// Create a default MimeMessage object.
//			Message message = new MimeMessage(session);
//
//			// Set From: header field of the header.
//			message.setFrom(new InternetAddress(from));
//
//			// Set To: header field of the header.
//			message.setRecipients(Message.RecipientType.TO,
//				InternetAddress.parse(to));
//
//			// Set Subject: header field
//			message.setSubject("Testing Subject 1");
//
//			// Now set the actual message
//			message.setText("Hello, this is sample for to check send " +
//				"email using JavaMailAPI ");
//
//			// Send message
//			Transport.send(message);
//
//			System.out.println("Sent message successfully....");
//
//		} catch (MessagingException e) {
//			throw new RuntimeException(e);
//		}
//	}

	public List<SwipeRecord> checkEmail(List<SwipeRecord> employees) {
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


			}

			emailFolder.close(false);
			store.close();
		}
	} catch(
	NoSuchProviderException e)

	{
		e.printStackTrace();
	} catch(
	MessagingException e)

	{
		e.printStackTrace();
	} catch(
	Exception e)

	{
		e.printStackTrace();
	}
		return null;
}


	private String getEmailAddress(String fullName) {
		return fullName.trim().replace(" ", ".") + "@appdirect.com".toLowerCase();
	}

	private Map<String, List<Date>> getWFHAndPTODatesFromMailForEmployee(String email, Message[] messages) {
		try {
			List<Date> WFH = new ArrayList<Date>();
			List<Date> PTO = new ArrayList<Date>();
			Map<String, List<Date>> WFHAndPTODate = new HashMap<String, List<Date>>();
			for (Message message : messages) {
				if (message.getFrom()[0].toString().contains(email) && message.getSubject().contains("[WFH]")) {

				}
				if (message.getFrom()[0].toString().contains(email) && message.getSubject().contains("[PTO]")) {
				}
				return null;
			}
			WFHAndPTODate.put("PTO MAIL", PTO);
			WFHAndPTODate.put("WFH MAIL", WFH);
			return WFHAndPTODate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Date> getWFHDate(String subject, List<Date> WFH) {
		try {
			String dateInSubject = subject.trim().split("|")[1].trim();
			if (dateInSubject.contains("to")) {
				Date start = fmt.parse(dateInSubject.split("to")[0].trim());
				Date end = fmt.parse(dateInSubject.split("to")[1].trim());
				WFH = Month.getWorkingDaysBetweenTwoDates(start, end);
			} else {
				fmt.parse(dateInSubject);
			}
			return WFH;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return WFH;
	}
}
