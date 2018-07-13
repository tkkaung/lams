package ntu.celt.eUreka2.dao;


public class BackupRestoreException extends RuntimeException{

	
	private static final long serialVersionUID = 1L;
	public BackupRestoreException() {
		super("Method to backup Module is not defined");
	}
	public BackupRestoreException(String message) {
		super(message);
	}
	public BackupRestoreException(Throwable cause) {
		super(cause);
	}

	public BackupRestoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
