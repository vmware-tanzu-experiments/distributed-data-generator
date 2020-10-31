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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedFileFormat
{
	private static final String kBouncyCastleProviderName = "BC";
    private static final String kBouncyCastleRSACipherSig = "RSA/ECB/PKCS1Padding";
    //private static final String kRSACipherSig = "RSA/ECB/PKCS#1";
    File ioFile;
    FilePackage ioFilePackage;
	private PublicKey publicKey;
	private Key sessionKey;
	static final byte [] fileSignature = {'i','n','d','l','e','n','c','t'};
	static final String kBouncyCastleAESCipherSig = "Rijndael/ECB/PKCS5Padding";
	/**
	 * 
	 */
	public EncryptedFileFormat(File ioFile)
	{
		this.ioFile = ioFile;
	}
	
	public EncryptedFileFormat(FilePackage ioFilePackage)
	{
		this.ioFilePackage = ioFilePackage;
	}
	
	public EncryptedFileFormat()
	{
		ioFile = null;
		ioFilePackage = null;
	}
	
	public void setEncryptionKeys(PublicKey newPublicKey, Key newSessionKey)
	{
		publicKey = newPublicKey;
		sessionKey = newSessionKey;
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		FileOutputStream fileOutStream = new FileOutputStream(ioFile);
		ForceMinLengthOutputStream returnStream = getOutputStream(fileOutStream);
		return (returnStream);
	}

	public ForceMinLengthOutputStream getOutputStream(OutputStream fileOutStream) throws IOException, InternalError 
	{
		fileOutStream.write(fileSignature);

		Cipher rsa;
		try
		{
			rsa = Cipher.getInstance(kBouncyCastleRSACipherSig, kBouncyCastleProviderName);
			rsa.init(Cipher.ENCRYPT_MODE, publicKey);
		}
		catch (Exception e)
		{
			throw new IOException("RSA cipher initialization failed - "+e.getMessage());
		}
		byte [] sessionKeyPT = sessionKey.getEncoded();
		byte [] sessionKeyET; 
		try
		{
			sessionKeyET = rsa.doFinal(sessionKeyPT);
		}
		catch (Exception e)
		{
			throw new IOException("RSA encryption of session key failed - "+e.getMessage());
		}
		byte [] lengthBytes = new byte[4];
		BitTwiddle.intToJavaByteArray(sessionKeyET.length, lengthBytes, 0);
		fileOutStream.write(lengthBytes);
		fileOutStream.write(sessionKeyET);
		MessageDigest MD5Generator;
		try
		{
			MD5Generator = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e1)
		{
			throw new InternalError("MD5 algorithm missing");
		}
		byte [] sessionKeyMD5 = MD5Generator.digest(sessionKeyET);
		BitTwiddle.intToJavaByteArray(sessionKeyMD5.length, lengthBytes, 0);
		fileOutStream.write(lengthBytes);
		fileOutStream.write(sessionKeyMD5);		
		Cipher aesCipher;
		try
		{
			aesCipher = Cipher.getInstance(kBouncyCastleAESCipherSig);
			aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey);
		}
		catch (Exception e)
		{
			throw new IOException("AES cipher initialization failed - "+e.getMessage());
		}
		CipherOutputStream cipherOut = new CipherOutputStream(fileOutStream, aesCipher);
		ForceMinLengthOutputStream returnStream = new ForceMinLengthOutputStream(cipherOut, 32);
		return returnStream;
	}
	
	public InputStream getInputStream(PrivateKey decryptKey)
	throws IOException
	{
		InputStream returnStream = null;
		if (ioFile != null)
		{
			FileInputStream fileInStream = new FileInputStream(ioFile);	
			returnStream = getInputStream(fileInStream,
					decryptKey);
		}
		if (ioFilePackage != null)
		{
			InputStream filePackageInStream = ioFilePackage.getDataDescriptorForFork("data").getInputStream();
			returnStream = getInputStream(filePackageInStream,
					decryptKey);
		}
		return(returnStream);
	}

	public InputStream getInputStream(InputStream fileInStream,
			PrivateKey decryptKey) throws IOException, InternalError {
		byte [] signatureBuf = new byte[8];
		if (fileInStream.read(signatureBuf) != signatureBuf.length)
			throw new IOException("Premature end of file reading signature");
		if (!Arrays.equals(signatureBuf, fileSignature))
				throw new IOException("File signature incorrect");		
		
		byte [] sessionKeyLengthBytes = new byte[4];
		if (fileInStream.read(sessionKeyLengthBytes) != sessionKeyLengthBytes.length)
			throw new IOException("Premature end of file reading session key length");
		int sessionKeyLength = BitTwiddle.javaByteArrayToInt(sessionKeyLengthBytes, 0);
		byte [] encryptedSessionKeyBytes = new byte[sessionKeyLength];
		if (fileInStream.read(encryptedSessionKeyBytes) != encryptedSessionKeyBytes.length)
		{
			throw new IOException("Premature end of file reading encrypted session key");		
		}

		byte [] sessionKeyMD5LengthBytes = new byte[4];
		if (fileInStream.read(sessionKeyMD5LengthBytes) != sessionKeyMD5LengthBytes.length)
			throw new IOException("Premature end of file reading session key MD5 length");
		int sessionKeyMD5Length = BitTwiddle.javaByteArrayToInt(sessionKeyMD5LengthBytes, 0);
		byte [] sessionKeyMD5Bytes = new byte[sessionKeyMD5Length];
		if (fileInStream.read(sessionKeyMD5Bytes) != sessionKeyMD5Bytes.length)
		{
			throw new IOException("Premature end of file reading session key MD5");		
		}
		MessageDigest MD5Generator;
		try
		{
			MD5Generator = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e1)
		{
			throw new InternalError("MD5 algorithm missing");
		}
		byte [] checkMD5 = MD5Generator.digest(encryptedSessionKeyBytes);

		if (checkMD5.length != sessionKeyMD5Bytes.length)
			throw new IOException("Session Key MD5 byte length incorrect in file");
		if (!Arrays.equals(checkMD5, sessionKeyMD5Bytes))
				throw new IOException("Session key MD5 differs");
		
		Cipher rsa;
		try
		{
			rsa = Cipher.getInstance(kBouncyCastleRSACipherSig, kBouncyCastleProviderName);
			rsa.init(Cipher.DECRYPT_MODE, decryptKey);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new IOException("RSA cipher initialization failed");
		}
		byte [] decryptedSessionKeyBytes;
		try
		{
			decryptedSessionKeyBytes = rsa.doFinal(encryptedSessionKeyBytes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new IOException("Decryption of session key failed");
		}
		//RawSecretKey sessionKey = new RawSecretKey("Rijndael", decryptedSessionKeyBytes);

        Cipher aesCipher;
		try
		{
            //sessionKey = SecretKeyFactory.getInstance("AES").generateSecret(new SecretKeySpec(decryptedSessionKeyBytes, "AES"));
			sessionKey = new SecretKeySpec(decryptedSessionKeyBytes, "AES");
            aesCipher = Cipher.getInstance(kBouncyCastleAESCipherSig);
			aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new IOException("AES cipher initialization failed");
		}
		CipherInputStream returnStream = new CipherInputStream(fileInStream, aesCipher);
		return returnStream;
	}
}
