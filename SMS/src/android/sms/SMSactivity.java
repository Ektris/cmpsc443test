package android.sms;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SMSactivity extends Activity
{
	EditText recNum;	
	EditText password;	
	EditText msgContent;	
	Button send;	
	Button cancel;
	
	@Override	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main);
				
		recNum = (EditText) findViewById(R.id.recNum);
		password = (EditText) findViewById(R.id.password);
		msgContent = (EditText) findViewById(R.id.msgContent);
		send = (Button) findViewById(R.id.Send);
		cancel = (Button) findViewById(R.id.cancel);
				
		// finish the activity when click Cancel button		
		cancel.setOnClickListener(new View.OnClickListener()
		{		
			public void onClick(View v)
			{		
				finish();
			}
		});
		
		send.setOnClickListener(new View.OnClickListener()
		// Encrypt the message and send it when Send is pressed
		{
			public void onClick(View v)
			{
				String recNumString = recNum.getText().toString();				
				String msgContentString = msgContent.getText().toString();				
				
				// Generate key string by combining a hash of the password and the combined phone numbers
				
				TelephonyManager mTelephonyMgr;
				mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String yourNumber = mTelephonyMgr.getLine1Number();
				
				String secretKeyString = String.valueOf(Math.abs(password.getText().toString().hashCode()))
						+ String.valueOf(Math.abs((yourNumber + recNumString).hashCode()));

				if (recNumString.length() > 0 && secretKeyString.length() > 0 && msgContentString.length() > 0)
				{
					// Encrypt the message
					byte[] encryptedMsg = encryptSMS(secretKeyString, msgContentString);

					// Send the message				
					String msgString = byteToHex(encryptedMsg);				
					sendSMS(recNumString, msgString);
			
					finish();
				}
				else
				{
					Toast.makeText(getBaseContext(),
						"Must enter a recipient's phone number, password, and message.",
						Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public static void sendSMS(String recNumString, String encryptedMsg)
	{
		SmsManager smsManager = SmsManager.getDefault();			
		
		// Need to divide message into parts if it exceeds 160 characters		
		ArrayList<String> parts = smsManager.divideMessage(encryptedMsg);
		smsManager.sendMultipartTextMessage(recNumString, null, parts, null, null);
	}

	public static String byteToHex(byte[] b)
	// Converts a byte stream to hex format
	{
		String hs = "";
		String stmp = "";
		
		for (int n = 0; n < b.length; n++)
		{
			stmp = Integer.toHexString(b[n] & 0xFF);	

			if (stmp.length() == 1)
				hs += ("0" + stmp);
			else
				hs += stmp;
		}
		return hs.toUpperCase();
	}

	public static byte[] encryptSMS(String keyString, String message)
	// Encrypt message using keyString
	{	
		try
		{
			byte[] encryptedBytes;

			// Generate key from user input
			Key key = generateKey(keyString);

			// Encrypt the message using AES
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, key);
			encryptedBytes = c.doFinal(message.getBytes());

			return encryptedBytes;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			byte[] returnArray = null;
			return returnArray;
		}
	}

	private static Key generateKey(String secretKeyString) throws Exception
	// Creates a key spec from a given key string
	{
		byte[] keyBytes = secretKeyString.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		keyBytes = sha.digest(keyBytes);
		keyBytes = Arrays.copyOf(keyBytes, 16);
		Key key = new SecretKeySpec(keyBytes, "AES");	
		return key;
	}
}