package ntu.celt.eUreka2.services.email;

public class InvalidEmailAddressException extends RuntimeException {

	private static final long serialVersionUID = 8029503682512223961L;

	public InvalidEmailAddressException() {
		super("Invalid Email address");
	}

	public InvalidEmailAddressException(String message) {
		super(message);
	}

	public InvalidEmailAddressException(Throwable cause) {
		super(cause);
	}

	public InvalidEmailAddressException(String message, Throwable cause) {
		super(message, cause);
	}

}
