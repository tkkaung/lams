package ntu.celt.eUreka2.services.email;

public class SendEmailFailException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 6702306213390694312L;

	public SendEmailFailException() {
		super("Fail to send email");
	}

	public SendEmailFailException(String message) {
		super(message);
	}

	public SendEmailFailException(Throwable cause) {
		super(cause);
	}

	public SendEmailFailException(String message, Throwable cause) {
		super(message, cause);
	}

}
