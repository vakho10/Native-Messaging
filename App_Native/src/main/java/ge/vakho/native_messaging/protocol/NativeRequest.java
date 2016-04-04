package ge.vakho.native_messaging.protocol;

import javax.xml.bind.annotation.XmlElement;

public class NativeRequest {

	@XmlElement(name = "message")
	private String message;
	
	public NativeRequest() {
		super();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
