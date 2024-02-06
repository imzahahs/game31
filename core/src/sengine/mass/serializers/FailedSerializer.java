package sengine.mass.serializers;

import sengine.mass.Mass;
import sengine.mass.MassException;
import sengine.mass.Serializer;
import sengine.mass.io.Input;
import sengine.mass.io.Output;

public class FailedSerializer implements Serializer<Object> {
	
	public final String errorMessage;
	public final Throwable error;
	
	public FailedSerializer(String errorMessage, Throwable error) {
		this.errorMessage = errorMessage;
		this.error = error;
	}

	@Override
	public Object read(Mass m, Input s, Class<Object> type) {
		throw new MassException(errorMessage, error);
	}

	@Override
	public void write(Mass m, Output s, Object o) {
		throw new MassException(errorMessage, error);
	}
}
