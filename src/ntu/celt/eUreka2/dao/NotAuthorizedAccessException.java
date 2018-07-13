package ntu.celt.eUreka2.dao;


public class NotAuthorizedAccessException extends RuntimeException{

	
	private static final long serialVersionUID = 1L;
	public NotAuthorizedAccessException() {
		super("You are not authorized to view the page.");
	}
	public NotAuthorizedAccessException(String message) {
		super(message);
	}
	public NotAuthorizedAccessException(Throwable cause) {
		super(cause);
	}

	public NotAuthorizedAccessException(String message, Throwable cause) {
		super(message, cause);
	}
}
