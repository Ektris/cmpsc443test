package android.sms;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;

public class DisplaySMSActivity extends Activity
{
	EditText password;
	TextView senderNum;
	TextView encryptedMessage;
	TextView decryptedMessage;
	Button submit;
	Button cancel;
	String originNum = "";
	String msgContent = "";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onreceive);

		senderNum = (TextView) findViewById(R.id.senderNum);
		encryptedMessage = (TextView) findViewById(R.id.encryptedMsg);
		decryptedMessage = (TextView) findViewById(R.id.decryptedMsg);
		password = (EditText) findViewById(R.id.password);
		submit = (Button) findViewById(R.id.submit);
		cancel = (Button) findViewById(R.id.cancel);

		Bundle extras = getIntent().getExtras();

		if(extras != null)
		{			
			originNum = extras.getString("originNum");
			
			msgContent = extras.getString("msgContent");
		
			senderNum.setText(originNum);
			encryptedMessage.setText(msgContent);
		}
		else
		{
			Toast.makeText(getBaseContext(), "Error!",
			Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// Return when Cancel is pressed	
		cancel.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v) { finish();	}
		});
		
		// Decrypt the message when Submit is pressed	
		submit.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				// Generate key string by combining a hash of the password and the combined phone numbers
				TelephonyManager mTelephonyMgr;
				mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String yourNumber = mTelephonyMgr.getLine1Number();
				
				String secretKeyString = String.valueOf(Math.abs(password.getText().toString().hashCode()))
						+ String.valueOf(Math.abs((originNum + yourNumber).hashCode()));

				if (password.length() > 0)
				{
					try
					{
						byte[] msg = hexToByte(msgContent.getBytes());					
						
						// Decrypt the message
						byte[] result = decryptSMS(secretKeyString, msg);				
						decryptedMessage.setText(new String(result));
					}
					catch (Exception e)
					{
						decryptedMessage.setText("Message Cannot Be Decrypted!");		
					}
				}
				else
				{
					Toast.makeText(getBaseContext(), "You must provide a password.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	public static byte[] hexToByte(byte[] b)
	// Converts a hex stream to byte array
	{
		byte[] b2 = new byte[b.length/2];

		for (int n = 0; n < b.length; n += 2)
		{
			String temp = new String(b, n, 2);
			b2[n/2] = (byte) Integer.parseInt(temp, 16);
		}
		return b2;
	}
	
	public static byte[] decryptSMS(String keyString, byte[] message) throws Exception
	// Decrypt the message with the given key
	{
		Key key = generateKey(keyString);
		
		// Decrypt the message
		Cipher c = Cipher.getInstance("AES");	
		c.init(Cipher.DECRYPT_MODE, key);	
		byte[] decryptedBytes = c.doFinal(message);		

		return decryptedBytes;
	}

	private static Key generateKey(String keyString) throws Exception
	// Creates a key spec from the given key string
	{
		byte[] keyBytes = keyString.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		keyBytes = sha.digest(keyBytes);
		keyBytes = Arrays.copyOf(keyBytes, 16);
		Key key = new SecretKeySpec(keyBytes, "AES");	
		return key;	
	}
}