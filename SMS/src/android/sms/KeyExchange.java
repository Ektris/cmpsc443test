package android.sms;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
/* Can you send public key as a message?
 * Pair up in certain way: assume know person and can compare.
 * --CAN SHARE PASSWORD to derive symmetric key;
 * --Assume offline exchange
 * Test on phones
 * Look up existing apps and see what they provide
 * --Serves as channel to help establish security;
 * Ex. Enable encryption or not
 * Attachments
 * Drop down for accessing keys?
 * Integrate contact fill for recipient
 * Main view = list
 * */
public class KeyExchange {

	  private static BigInteger g = new BigInteger("7961C6D7913FDF8A034593294FA52D6F8354E9EDFE3EDC8EF082D36662D69DFE8CA7DC7480121C98B9774DFF915FB710D79E1BCBA68C0D429CD6B9AD73C0EF20", 16);
	  private static BigInteger p = new BigInteger("00AC86AB9A1F921B251027BD10B93D0A8D9A260364974648E2543E8CD5C48DB4FFBEF0C3843465BA8DE20FFA36FFAF840B8CF26C9EB865BA184642A5F84606AEC5", 16);
  	
  	public static byte[] getSecretKey() throws Exception 
  	{
  		DHParameterSpec dhParams = new DHParameterSpec(p, g);
  		
  		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
  		keyGen.initialize(dhParams, new SecureRandom());
  		
  		// start a keyagreement object using DH (Diffie-Hellman)
  		KeyAgreement ka = KeyAgreement.getInstance("DH");
  		
  		//generate a privateKey
  		KeyPair keypair = keyGen.generateKeyPair();
  		ka.init(keypair.getPrivate());
          
  		// somehow get B's public key
  		KeyPair peerPublicKey = getpeerkey(); 
  		
  		ka.doPhase(peerPublicKey.getPublic(), true);

  		SecretKey secretKey = ka.generateSecret("AES");

  		return secretKey.getEncoded();
  	}
  	
  	private static KeyPair getpeerkey() throws Exception
  	{
  		KeyPair BpublicKey;
  		
  		// useless code but I needed something to compile
  		DHParameterSpec dhParams = new DHParameterSpec(p, g);
  		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
  		keyGen.initialize(dhParams, new SecureRandom());
  		BpublicKey = keyGen.generateKeyPair();
  		
  		return BpublicKey;
  	}
}
