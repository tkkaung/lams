package ntu.celt.eUreka2.dao;

public class InvalidFormatException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3104456914007118616L;

	public InvalidFormatException() {
		super("Invalid File Format");
	}

	public InvalidFormatException(String message) {
		super(message);
	}

	public InvalidFormatException(Throwable cause) {
		super(cause);
	}

	public InvalidFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
