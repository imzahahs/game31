
package sengine.mass;

public class MassException extends RuntimeException {
	private static final long serialVersionUID = -5081812072970997029L;
	
	public MassException () {
		super();
	}

	public MassException (String message, Throwable cause) {
		super(message, cause);
	}

	public MassException (String message) {
		super(message);
	}

	public MassException (Throwable cause) {
		super(cause);
	}
}
