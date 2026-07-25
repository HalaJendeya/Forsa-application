package com.example.forsa.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtils() {
    }

    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec specification = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = factory.generateSecret(specification).getEncoded();
            specification.clearPassword();

            String encodedSalt = Base64.encodeToString(salt, Base64.NO_WRAP);
            String encodedHash = Base64.encodeToString(hash, Base64.NO_WRAP);

            return encodedSalt + ":" + encodedHash;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to hash password",
                    exception
            );
        }
    }

    public static boolean verifyPassword(
            String enteredPassword,
            String storedPassword
    ) {
        try {
            String[] parts = storedPassword.split(":");

            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.decode(
                    parts[0],
                    Base64.NO_WRAP
            );

            byte[] storedHash = Base64.decode(
                    parts[1],
                    Base64.NO_WRAP
            );

            PBEKeySpec specification = new PBEKeySpec(
                    enteredPassword.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] enteredHash =
                    factory.generateSecret(specification).getEncoded();

            specification.clearPassword();

            return MessageDigest.isEqual(
                    storedHash,
                    enteredHash
            );

        } catch (Exception exception) {
            return false;
        }
    }
}