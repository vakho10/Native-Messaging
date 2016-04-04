package ge.vakho.native_messaging.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ge.vakho.native_messaging.protocol.NativeRequest;
import ge.vakho.native_messaging.protocol.NativeResponse;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class Application {

	private final AtomicBoolean interrompe;

	public Application() {
		this.interrompe = new AtomicBoolean(false);
	}

	public static void main(String[] args) {
		// log("Starting the app...");

		final Application app = new Application();

		ConnectableObservable<String> obs = app.getObservable();
		obs.observeOn(Schedulers.computation()).subscribe(new Observer<String>() {
			public void onCompleted() {
			}

			public void onError(Throwable throwable) {
			}

			public void onNext(String s) {

				// log("Host received " + s);

				// Parse the message
				ObjectMapper mapper = new ObjectMapper();
				NativeRequest request = null;
				try {
					request = mapper.readValue(s, NativeRequest.class);
				} catch (IOException e) {
					e.printStackTrace();
				}

				NativeResponse response = new NativeResponse();
				if ("vakho".equalsIgnoreCase(request.getMessage())) {
					response.setMessage("Hello, Sir. Vakho.");
				} else {
					response.setMessage("Hello, Mr. " + request.getMessage() + ".");
				}

				String responseJson = null;
				try {
					responseJson = mapper.writeValueAsString(response);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

				try {
					app.sendMessage(responseJson);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		obs.connect();

		while (!app.interrompe.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}

		System.exit(0);
	}

	private ConnectableObservable<String> getObservable() {
		ConnectableObservable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				subscriber.onStart();
				try {
					while (true) {
						String _s = readMessage(System.in);
						subscriber.onNext(_s);
					}
				} catch (InterruptedIOException ioe) {
					// log("Blocked communication");
				} catch (Exception e) {
					subscriber.onError(e);
				}
				subscriber.onCompleted();
			}
		}).subscribeOn(Schedulers.io()).publish();

		observable.subscribe(new Observer<String>() {
			public void onCompleted() {
//				log("App closed.");
				interrompe.set(true);
			}

			public void onError(Throwable throwable) {
//				log("Unexpected error!");
				interrompe.set(true);
			}

			public void onNext(String s) {
			}
		});

		return observable;
	}

	private String readMessage(InputStream in) throws IOException {
		byte[] b = new byte[4];
		in.read(b);

		int size = getInt(b);
		// log(String.format("The size is %d", size));

		if (size == 0) {
			throw new InterruptedIOException("Blocked communication");
		}

		b = new byte[size];
		in.read(b);

		return new String(b, "UTF-8");
	}

	private void sendMessage(String message) throws IOException {
		System.out.write(getBytes(message.length()));
		System.out.write(message.getBytes("UTF-8"));
		System.out.flush();
		// log("mandou: " + message);
	}

	public int getInt(byte[] bytes) {
		return (bytes[3] << 24) & 0xff000000 | (bytes[2] << 16) & 0x00ff0000 | (bytes[1] << 8) & 0x0000ff00
				| (bytes[0] << 0) & 0x000000ff;
	}

	public byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (length & 0xFF);
		bytes[1] = (byte) ((length >> 8) & 0xFF);
		bytes[2] = (byte) ((length >> 16) & 0xFF);
		bytes[3] = (byte) ((length >> 24) & 0xFF);
		return bytes;
	}

	// private static void log(String message) {
	// File file = new File(PATH_LOG);
	//
	// try {
	// if (!file.exists()) {
	// file.createNewFile();
	// }
	//
	// FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
	// BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	//
	// DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	// Date date = new Date();
	//
	// bufferedWriter.write(dateFormat.format(date) + ": " + message + "\r\n");
	// bufferedWriter.close();
	// } catch (Exception e) {
	// log("ERROR ==> Method (log)" + e.getMessage());
	// e.printStackTrace();
	// }
	// }

	public static String getThumbPrint(X509Certificate cert)
			throws NoSuchAlgorithmException, CertificateEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		return hexify(digest);
	}

	public static String hexify(byte bytes[]) {

		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		StringBuffer buf = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}

		return buf.toString();
	}

//	private static String stringHexa(byte[] bytes) {
//		StringBuilder stringBuilder = new StringBuilder();
//
//		try {
//			for (int i = 0; i < bytes.length; i++) {
//				int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
//				int parteBaixa = bytes[i] & 0xf;
//				if (parteAlta == 0) {
//					stringBuilder.append('0');
//				}
//
//				stringBuilder.append(Integer.toHexString(parteAlta | parteBaixa));
//			}
//		} catch (Exception e) {
////			log("ERROR ==> Method (stringHexa)" + e.getMessage());
//			e.printStackTrace();
//		}
//
//		return stringBuilder.toString();
//	}

//	private static String getPublicKey(String certThumbprint, Boolean encoded)
//			throws KeyStoreException, NoSuchProviderException {
//		KeyStore keyStore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
//		String encodedPublicKey = "";
//		String aliasPublicKey = "";
//
//		try {
//			/* Load Windows KeyStore */
//			keyStore.load(null, null);
//			String selectedLocalPublicKey = "";
//			Enumeration<String> al = keyStore.aliases();
//			while (al.hasMoreElements()) {
//				String alias = al.nextElement();
//				X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
//
//				if (getThumbPrint(cert).equals(certThumbprint)) {
//					selectedLocalPublicKey = new String(
//							Base64.getEncoder().encodeToString((cert.getPublicKey().getEncoded())));
//					selectedLocalPublicKey = "-----BEGIN PUBLIC KEY-----\n"
//							+ selectedLocalPublicKey.replaceAll("(.{64})", "$1\n") + "\n-----END PUBLIC KEY-----\n";
//					aliasPublicKey = alias;
////					log(alias);
//				}
//			}
//
//			/* Encrypt PK */
//			MessageDigest md = MessageDigest.getInstance("SHA-256");
//			md.update(selectedLocalPublicKey.getBytes("UTF-8"));
//			byte[] byteDigest = md.digest();
//
//			encodedPublicKey = stringHexa(byteDigest);
//		} catch (Exception e) {
////			log("ERROR ==> Method (getPublicKey)" + e.getMessage());
//			e.printStackTrace();
//			return "";
//		}
//
//		if (encoded) {
//			return encodedPublicKey;
//		} else {
//			return aliasPublicKey;
//		}
//	}
}
