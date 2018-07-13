package ntu.celt.eUreka2.dao;


public class DataBeingUsedException extends RuntimeException{

	
	private static final long serialVersionUID = 1L;
	public DataBeingUsedException() {
		super("Data being used by another class");
	}
	public DataBeingUsedException(String message) {
		super(message);
	}
	public DataBeingUsedException(Throwable cause) {
		super(cause);
	}

	public DataBeingUsedException(String message, Throwable cause) {
		super(message, cause);
	}
}
