package ntu.celt.eUreka2.services.attachFiles;

public class AttachedFileNotFoundException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1610693763384215411L;

	public AttachedFileNotFoundException() {
		super("AttachedFile Not Found");
	}

	public AttachedFileNotFoundException(String message) {
		super(message);
	}

	public AttachedFileNotFoundException(Throwable cause) {
		super(cause);
	}

	public AttachedFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
