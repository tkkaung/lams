package ntu.celt.eUreka2.dao;

public class DuplicateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateException() {
		super("Item exists.");
	}

	public DuplicateException(String message) {
		super(message);
	}

	public DuplicateException(Throwable cause) {
		super(cause);
	}

	public DuplicateException(String message, Throwable cause) {
		super(message, cause);
	}

}
