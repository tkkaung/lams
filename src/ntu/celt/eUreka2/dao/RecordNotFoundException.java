package ntu.celt.eUreka2.dao;

public class RecordNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8029503682512223961L;

	public RecordNotFoundException() {
		super("Record Not Found");
	}

	public RecordNotFoundException(String message) {
		super(message);
	}

	public RecordNotFoundException(Throwable cause) {
		super(cause);
	}

	public RecordNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
