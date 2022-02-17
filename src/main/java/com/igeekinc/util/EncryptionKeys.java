/*
 * Copyright 2002-2014 iGeek, Inc.
 * All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.igeekinc.util;

import com.igeekinc.util.jdk14keycompat.PublicKeyWorkaroundInputStream;
import com.igeekinc.util.jdk14keycompat.RSAPublicKeyOverride;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;




/**
 * EncryptionKeys hold the keys for encrypting/decrypting
 * 
 * The encryption scheme uses a combination of RSA and AES.  First, the user enters a pass phrase that will
 * be used for decryption.  We generate an RSA key and encrypt the private key using AES with the pass phrase as the
 * key.  Each file is encrypted with a random AES key and that key is encrypted using the RSA public key.  When we decrypt,
 * the user enters the pass phrase and the RSA private key is decrypted and used to decrypt each file's AES key which is
 * then used to decrypt the actual file.
 * 
 * This allows us to encrypt files without having the pass phrase.
 * 
 * @author David L. Smith-Uchida
 *
 */
public class EncryptionKeys implements Serializable
{
	int keySize;
	PublicKey encryptionKey; 
	byte [] escrowedPassword;
	byte [] serializedEncryptedKey, wrappedEncryptedKey;
	byte [] checkMD5;
	
	static final String aesAlgorithm = "AES/ECB/NoPadding"; // Rijndael == AES //$NON-NLS-1$
	static final String rsaAlgorithm = "RSA"; //$NON-NLS-1$
	
	static final long serialVersionUID = -5420370882757153558L;
	
	public EncryptionKeys(PBEKeySpec passPhrase, boolean escrowPassword)
		throws
			InvalidKeySpecException,
			NoSuchPaddingException,
			InvalidKeyException,
			IllegalStateException,
			IllegalBlockSizeException,
			BadPaddingException, IOException, NoSuchAlgorithmException, NoSuchProviderException
	{
		
		// Generate a random key for RSA
		KeyPairGenerator rsaKeyGenerator;
		KeyPair rsaKeys;
		// Use a keyfactory in order to create a key
		// for the PBE algorithm.
		rsaKeyGenerator = KeyPairGenerator.getInstance(rsaAlgorithm/*, "CryptixCrypto"*/); //$NON-NLS-1$
		// Generate the secret key.
		rsaKeyGenerator.initialize(1024);
		rsaKeys = rsaKeyGenerator.generateKeyPair();
		
		serializedEncryptedKey = encryptRSAPrivateKeyWithAES(passPhrase, rsaKeys.getPrivate());
		
		wrappedEncryptedKey = null;
		//decryptionKey = aes.wrap(rsaKeys.getPrivate());
		// And keep track of the public key
		encryptionKey = rsaKeys.getPublic();
		// TODO - generate the escrowed password using RSA
	}
	private byte[] encryptRSAPrivateKeyWithAES(PBEKeySpec passPhrase, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException
	{
		// Generate a key (secret) for
		// AES

		SecretKeySpec aesKey;
		aesKey = generateAESKeyFromPBE(passPhrase);

		// Now, we encrypt the RSA private key (decryption key) using our
		// AES (Rijndael) key based on the passphrase
		Cipher aes = Cipher.getInstance(aesAlgorithm/*, "CryptixCrypto"*/); //$NON-NLS-1$
		aes.init(Cipher.ENCRYPT_MODE, aesKey);

		ObjectOutputStream keyOutStream;
		ByteArrayOutputStream byteOutStream;
		byteOutStream = new ByteArrayOutputStream();
		keyOutStream = new ObjectOutputStream(byteOutStream);
		keyOutStream.writeObject(privateKey);
		keyOutStream.close();
		byte [] serializedUnencryptedObject = byteOutStream.toByteArray();
		int blockSize = aes.getBlockSize();
		int enLen = serializedUnencryptedObject.length + (blockSize - (serializedUnencryptedObject.length % blockSize));
		byte [] encryptInput = new byte[enLen];
		System.arraycopy(serializedUnencryptedObject, 0, encryptInput, 0, serializedUnencryptedObject.length);
		byte [] serializedEncryptedKeyLocal =
			aes.doFinal(encryptInput);
		MessageDigest MD5Generator = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		checkMD5 = MD5Generator.digest(encryptInput);

		// Zero out the keys for good measure
		for (int curByteNum = 0;curByteNum < serializedUnencryptedObject.length; curByteNum++)
			serializedUnencryptedObject[curByteNum] = 0;
		for (int curByteNum = 0;curByteNum < encryptInput.length; curByteNum++)
			encryptInput[curByteNum] = 0;
		return serializedEncryptedKeyLocal;
	}
	public PublicKey getEncryptionKey()
	{
		return encryptionKey;
	}
	
	public byte [] getDecryptionKey()
	{
		return serializedEncryptedKey;
	}
	
	SecretKeySpec generateAESKeyFromPBE(PBEKeySpec pbe) throws NoSuchAlgorithmException
	{
		// First, from the passphrase we generate a key (secret) for
		// AES
		char [] passPhraseChars = pbe.getPassword();
		byte [] passPhraseBytes = new byte[passPhraseChars.length * 2];
		for (int curCharNum = 0; curCharNum < passPhraseChars.length; curCharNum++)
		{
			passPhraseBytes[curCharNum * 2] = (byte)((passPhraseChars[curCharNum] >> 8) & 0xff);
			passPhraseBytes[(curCharNum * 2)+1] = (byte)(passPhraseChars[curCharNum] & 0xff);

		}
		MessageDigest MD5Generator = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		byte [] md5PassPhrase = MD5Generator.digest(passPhraseBytes);
		
		SecretKeySpec aesKey;
		aesKey = new SecretKeySpec(md5PassPhrase, "Rijndael"); //$NON-NLS-1$
		return aesKey;
	}
	public PrivateKey decryptDecryptionKey(PBEKeySpec passPhrase) 
	throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, OptionalDataException, IOException
	{
		// First, from the passphrase we generate a key (secret) for
		// AES
		
		SecretKeySpec aesKey;
		aesKey = generateAESKeyFromPBE(passPhrase);
		
		Cipher aes = Cipher.getInstance(aesAlgorithm);
		
		// Now, decipher & unwrap the key
		aes.init(Cipher.DECRYPT_MODE, aesKey);
		byte [] decryptedKey = aes.doFinal(serializedEncryptedKey);
		
		MessageDigest MD5Generator = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		/* Generate an MD5 hash of the decrypted output to check against our orignal checkMD5 */
		byte [] trialMD5 = MD5Generator.digest(decryptedKey);
		
		if (checkMD5.length != trialMD5.length)
			throw new InternalError("Key did not decrypt"); //$NON-NLS-1$
		for (int i = 0; i< trialMD5.length; i++)
			if (checkMD5[i] != trialMD5[i])
				throw new InternalError("Key did not decrypt"); //$NON-NLS-1$


		ByteArrayInputStream byteStream = new ByteArrayInputStream(decryptedKey);
		ObjectInputStream objectStream;
		PrivateKey returnKey = null;
        if (System.getProperty("java.version").startsWith("1.4"))
        {
        	objectStream = new ObjectInputStream(byteStream);
        	try
			{
				returnKey = (PrivateKey)objectStream.readObject();
			} catch (ClassNotFoundException e)
			{
				LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
				throw new InternalError("Cannot find class when deserializing private key");
			}
        }
        else
        {
        	try
        	{
        		objectStream = new PublicKeyWorkaroundInputStream(byteStream);	// Use the workaround - 1.4 default RSA keys (from the private sun SSL package) are not compatible with 1.5+
        		try
        		{
        			returnKey = (PrivateKey)objectStream.readObject();
        		} catch (ClassNotFoundException e)
        		{
        			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
        			throw new InternalError("Cannot find class when deserializing private key");
        		}
        	} catch (IOException e)
        	{
        		// This is gross.  But, the deserialization of these damned RSA classes is not working right.  So, if it fails, just slam the damn bytes in
        		byte [] privateExponent = new byte [128];
        		byte [] modulus = new byte[128];
        		System.arraycopy(decryptedKey, 508, modulus, 0, 128);	// Evil, evil magic numbers - FIX ME
        		System.arraycopy(decryptedKey, 659, privateExponent, 0, 128);
        		RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(new BigInteger(1, modulus), new BigInteger(1, privateExponent));
        		try {
    				KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
    				returnKey = rsaFactory.generatePrivate(privateKeySpec);
    			} catch (NoSuchAlgorithmException e1) 
    			{
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			} catch (InvalidKeySpecException e1) {
    				// TODO Auto-generated catch block
    				e1.printStackTrace();
    			}
        	}
        }

		
		for (int i=0; i< decryptedKey.length; i++)
			decryptedKey[i] = 0;
		return(returnKey);
	}
	
	public boolean checkPassPhrase(PBEKeySpec passPhrase)
	throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, OptionalDataException, ClassNotFoundException, IOException
	{
		// First, from the passphrase we generate a key (secret) for
		// AES
		
		SecretKeySpec aesKey;
		aesKey = generateAESKeyFromPBE(passPhrase);
		
		Cipher aes = Cipher.getInstance(aesAlgorithm);
		
		// Now, decipher & unwrap the key
		aes.init(Cipher.DECRYPT_MODE, aesKey);
		byte [] decryptedKey = aes.doFinal(serializedEncryptedKey);
		MessageDigest MD5Generator = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		/* Generate an MD5 hash of the decrypted output to check against our orignal checkMD5 */
		byte [] trialMD5 = MD5Generator.digest(decryptedKey);
		
		for (int i=0; i< decryptedKey.length; i++)
			decryptedKey[i] = 0;
		if (checkMD5.length != trialMD5.length)
			return false;
		for (int i = 0; i< trialMD5.length; i++)
			if (checkMD5[i] != trialMD5[i])
				return false;
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
 @Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof EncryptionKeys)
		{
			EncryptionKeys checkKeys = (EncryptionKeys)obj;
			if (	Arrays.equals(serializedEncryptedKey, checkKeys.serializedEncryptedKey))
				return true;
		}
		return false;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
		// prepare to read the alternate persistent fields
        ObjectInputStream.GetField fields = ois.readFields();
        Object keyField = fields.get("encryptionKey", null);
        if (keyField instanceof RSAPublicKeyOverride)
        {
        	RSAPublicKeySpec keySpec = new RSAPublicKeySpec(((RSAPublicKeyOverride) keyField).getModulus(), ((RSAPublicKeyOverride) keyField).getPublicExponent());
        	try {
				KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
				encryptionKey = rsaFactory.generatePublic(keySpec);
			} catch (NoSuchAlgorithmException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        else
        	encryptionKey = (PublicKey)keyField;
        keySize = fields.get("keySize", 0);
        checkMD5 = (byte [])fields.get("checkMD5", new byte[0]);
        escrowedPassword = (byte [])fields.get("escrowedPassword", new byte[0]);
        serializedEncryptedKey = (byte [])fields.get("serializedEncryptedKey", new byte[0]);
        wrappedEncryptedKey = (byte [])fields.get("wrappedEncryptedKey", new byte[0]);
        
    }

	public void changePassPhrase(PBEKeySpec oldPassPhrase, PBEKeySpec newPassPhrase) throws InvalidKeyException, OptionalDataException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException
	{
		PrivateKey decryptedKey = decryptDecryptionKey(oldPassPhrase);
		serializedEncryptedKey = encryptRSAPrivateKeyWithAES(newPassPhrase, decryptedKey);
	}
}
