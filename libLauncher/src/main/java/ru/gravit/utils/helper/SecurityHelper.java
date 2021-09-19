package ru.gravit.utils.helper;

import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyFactory;
import ru.gravit.launcher.LauncherAPI;
import java.security.interfaces.RSAPrivateKey;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.interfaces.RSAKey;
import java.security.GeneralSecurityException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.Cipher;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.net.URL;
import java.nio.file.Path;
import java.io.IOException;
import java.security.MessageDigest;
import java.io.InputStream;
import java.security.SecureRandom;

public final class SecurityHelper
{
    public static final String RSA_ALGO = "RSA";
    public static final String RSA_SIGN_ALGO = "SHA256withRSA";
    public static final String RSA_CIPHER_ALGO = "RSA/ECB/PKCS1Padding";
    public static final int TOKEN_LENGTH = 16;
    public static final int AES_KEY_LENGTH = 8;
    public static final int TOKEN_STRING_LENGTH = 32;
    public static final int RSA_KEY_LENGTH_BITS = 2048;
    public static final int RSA_KEY_LENGTH = 256;
    public static final int CRYPTO_MAX_LENGTH = 2048;
    public static final String HEX = "0123456789abcdef";
    public static final byte[] NUMBERS;
    public static final SecureRandom secureRandom;
    private static final char[] VOWELS;
    private static final char[] CONS;
    
    public static byte[] digest(final DigestAlgorithm algo, final byte[] bytes) {
        return newDigest(algo).digest(bytes);
    }
    
    public static byte[] digest(final DigestAlgorithm algo, final InputStream input) throws IOException {
        final byte[] buffer = IOHelper.newBuffer();
        final MessageDigest digest = newDigest(algo);
        for (int length = input.read(buffer); length != -1; length = input.read(buffer)) {
            digest.update(buffer, 0, length);
        }
        return digest.digest();
    }
    
    public static byte[] digest(final DigestAlgorithm algo, final Path file) throws IOException {
        try (final InputStream input = IOHelper.newInput(file)) {
            return digest(algo, input);
        }
    }
    
    public static byte[] digest(final DigestAlgorithm algo, final String s) {
        return digest(algo, IOHelper.encode(s));
    }
    
    public static byte[] digest(final DigestAlgorithm algo, final URL url) throws IOException {
        try (final InputStream input = IOHelper.newInput(url)) {
            return digest(algo, input);
        }
    }
    
    public static KeyPair genRSAKeyPair() {
        return genRSAKeyPair(newRandom());
    }
    
    public static KeyPair genRSAKeyPair(final SecureRandom random) {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, random);
            return generator.genKeyPair();
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }
    
    public static boolean isValidSign(final byte[] bytes, final byte[] sign, final RSAPublicKey publicKey) throws SignatureException {
        final Signature signature = newRSAVerifySignature(publicKey);
        try {
            signature.update(bytes);
        }
        catch (SignatureException e) {
            throw new InternalError(e);
        }
        return signature.verify(sign);
    }
    
    public static boolean isValidSign(final InputStream input, final byte[] sign, final RSAPublicKey publicKey) throws IOException, SignatureException {
        final Signature signature = newRSAVerifySignature(publicKey);
        updateSignature(input, signature);
        return signature.verify(sign);
    }
    
    public static boolean isValidSign(final Path path, final byte[] sign, final RSAPublicKey publicKey) throws IOException, SignatureException {
        try (final InputStream input = IOHelper.newInput(path)) {
            return isValidSign(input, sign, publicKey);
        }
    }
    
    public static boolean isValidSign(final URL url, final byte[] sign, final RSAPublicKey publicKey) throws IOException, SignatureException {
        try (final InputStream input = IOHelper.newInput(url)) {
            return isValidSign(input, sign, publicKey);
        }
    }
    
    public static boolean isValidToken(final CharSequence token) {
        return token.length() == 32 && token.chars().allMatch(ch -> "0123456789abcdef".indexOf(ch) >= 0);
    }
    
    private static Cipher newCipher(final String algo) {
        try {
            return Cipher.getInstance(algo);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException ex2) {
            final GeneralSecurityException ex;
            final GeneralSecurityException e = ex2;
            throw new InternalError(e);
        }
    }
    
    public static MessageDigest newDigest(final DigestAlgorithm algo) {
        VerifyHelper.verify(algo, a -> a != DigestAlgorithm.PLAIN, "PLAIN digest");
        try {
            return MessageDigest.getInstance(algo.name);
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }
    
    public static SecureRandom newRandom() {
        return new SecureRandom();
    }
    
    private static Cipher newRSACipher(final int mode, final RSAKey key) {
        final Cipher cipher = newCipher("RSA/ECB/PKCS1Padding");
        try {
            cipher.init(mode, (Key)key);
        }
        catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return cipher;
    }
    
    @LauncherAPI
    public static Cipher newRSADecryptCipher(final RSAPrivateKey key) {
        return newRSACipher(2, key);
    }
    
    @LauncherAPI
    public static Cipher newRSAEncryptCipher(final RSAPublicKey key) {
        return newRSACipher(1, key);
    }
    
    private static KeyFactory newRSAKeyFactory() {
        try {
            return KeyFactory.getInstance("RSA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }
    
    private static Signature newRSASignature() {
        try {
            return Signature.getInstance("SHA256withRSA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }
    
    public static Signature newRSASignSignature(final RSAPrivateKey key) {
        final Signature signature = newRSASignature();
        try {
            signature.initSign(key);
        }
        catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }
    
    public static Signature newRSAVerifySignature(final RSAPublicKey key) {
        final Signature signature = newRSASignature();
        try {
            signature.initVerify(key);
        }
        catch (InvalidKeyException e) {
            throw new InternalError(e);
        }
        return signature;
    }
    
    public static byte[] randomBytes(final int length) {
        return randomBytes(newRandom(), length);
    }
    
    public static byte[] randomBytes(final Random random, final int length) {
        final byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
    
    @LauncherAPI
    public static String randomStringToken() {
        return randomStringToken(newRandom());
    }
    
    @LauncherAPI
    public static String randomStringToken(final Random random) {
        return toHex(randomToken(random));
    }
    
    public static byte[] randomToken() {
        return randomToken(newRandom());
    }
    
    public static byte[] randomToken(final Random random) {
        return randomBytes(random, 16);
    }
    
    public static String randomStringAESKey() {
        return toHex(randomAESKey(newRandom()));
    }
    
    public static String randomStringAESKey(final Random random) {
        return toHex(randomAESKey(random));
    }
    
    public static byte[] randomAESKey() {
        return randomAESKey(newRandom());
    }
    
    public static byte[] randomAESKey(final Random random) {
        return randomBytes(random, 8);
    }
    
    public static String randomUsername() {
        return randomUsername(newRandom());
    }
    
    public static String randomUsername(final Random random) {
        int usernameLength = 3 + random.nextInt(7);
        final int prefixType = random.nextInt(7);
        String prefix;
        if (usernameLength >= 5 && prefixType == 6) {
            prefix = (random.nextBoolean() ? "Mr" : "Dr");
            usernameLength -= 2;
        }
        else if (usernameLength >= 6 && prefixType == 5) {
            prefix = "Mrs";
            usernameLength -= 3;
        }
        else {
            prefix = "";
        }
        final int suffixType = random.nextInt(7);
        String suffix;
        if (usernameLength >= 5 && suffixType == 6) {
            suffix = String.valueOf(10 + random.nextInt(90));
            usernameLength -= 2;
        }
        else if (usernameLength >= 7 && suffixType == 5) {
            suffix = String.valueOf(1990 + random.nextInt(26));
            usernameLength -= 4;
        }
        else {
            suffix = "";
        }
        int consRepeat = 0;
        boolean consPrev = random.nextBoolean();
        final char[] chars = new char[usernameLength];
        for (int i = 0; i < chars.length; ++i) {
            if (i > 1 && consPrev && random.nextInt(10) == 0) {
                chars[i] = chars[i - 1];
            }
            else {
                if (consRepeat < 1 && random.nextInt() == 5) {
                    ++consRepeat;
                }
                else {
                    consRepeat = 0;
                    consPrev ^= true;
                }
                final char[] alphabet = consPrev ? SecurityHelper.CONS : SecurityHelper.VOWELS;
                chars[i] = alphabet[random.nextInt(alphabet.length)];
            }
        }
        if (!prefix.isEmpty() || random.nextBoolean()) {
            chars[0] = Character.toUpperCase(chars[0]);
        }
        return VerifyHelper.verifyUsername(prefix + new String(chars) + suffix);
    }
    
    public static byte[] sign(final byte[] bytes, final RSAPrivateKey privateKey) {
        final Signature signature = newRSASignSignature(privateKey);
        try {
            signature.update(bytes);
            return signature.sign();
        }
        catch (SignatureException e) {
            throw new InternalError(e);
        }
    }
    
    public static byte[] sign(final InputStream input, final RSAPrivateKey privateKey) throws IOException {
        final Signature signature = newRSASignSignature(privateKey);
        updateSignature(input, signature);
        try {
            return signature.sign();
        }
        catch (SignatureException e) {
            throw new InternalError(e);
        }
    }
    
    public static byte[] sign(final Path path, final RSAPrivateKey privateKey) throws IOException {
        try (final InputStream input = IOHelper.newInput(path)) {
            return sign(input, privateKey);
        }
    }
    
    public static String toHex(final byte[] bytes) {
        int offset = 0;
        final char[] hex = new char[bytes.length << 1];
        for (final byte currentByte : bytes) {
            final int ub = Byte.toUnsignedInt(currentByte);
            hex[offset] = "0123456789abcdef".charAt(ub >>> 4);
            ++offset;
            hex[offset] = "0123456789abcdef".charAt(ub & 0xF);
            ++offset;
        }
        return new String(hex);
    }
    
    public static RSAPrivateKey toPrivateRSAKey(final byte[] bytes) throws InvalidKeySpecException {
        return (RSAPrivateKey)newRSAKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
    
    public static RSAPublicKey toPublicRSAKey(final byte[] bytes) throws InvalidKeySpecException {
        return (RSAPublicKey)newRSAKeyFactory().generatePublic(new X509EncodedKeySpec(bytes));
    }
    
    private static void updateSignature(final InputStream input, final Signature signature) throws IOException {
        final byte[] buffer = IOHelper.newBuffer();
        for (int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
            try {
                signature.update(buffer, 0, length);
            }
            catch (SignatureException e) {
                throw new InternalError(e);
            }
        }
    }
    
    public static void verifySign(final byte[] bytes, final byte[] sign, final RSAPublicKey publicKey) throws SignatureException {
        if (!isValidSign(bytes, sign, publicKey)) {
            throw new SignatureException("Invalid sign");
        }
    }
    
    public static void verifySign(final InputStream input, final byte[] sign, final RSAPublicKey publicKey) throws SignatureException, IOException {
        if (!isValidSign(input, sign, publicKey)) {
            throw new SignatureException("Invalid stream sign");
        }
    }
    
    public static void verifySign(final Path path, final byte[] sign, final RSAPublicKey publicKey) throws SignatureException, IOException {
        if (!isValidSign(path, sign, publicKey)) {
            throw new SignatureException(String.format("Invalid file sign: '%s'", path));
        }
    }
    
    public static void verifySign(final URL url, final byte[] sign, final RSAPublicKey publicKey) throws SignatureException, IOException {
        if (!isValidSign(url, sign, publicKey)) {
            throw new SignatureException(String.format("Invalid URL sign: '%s'", url));
        }
    }
    
    public static String verifyToken(final String token) {
        return VerifyHelper.verify(token, SecurityHelper::isValidToken, String.format("Invalid token: '%s'", token));
    }
    
    private SecurityHelper() {
    }
    
    public static byte[] encrypt(final String seed, final byte[] cleartext) throws Exception {
        final byte[] rawKey = getRawKey(seed.getBytes());
        final byte[] result = encrypt(rawKey, cleartext);
        return result;
    }
    
    public static byte[] encrypt(final String seed, final String cleartext) throws Exception {
        return encrypt(seed, cleartext.getBytes());
    }
    
    private static byte[] getRawKey(final byte[] seed) throws Exception {
        final KeyGenerator kGen = KeyGenerator.getInstance("AES");
        final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kGen.init(128, sr);
        final SecretKey sKey = kGen.generateKey();
        return sKey.getEncoded();
    }
    
    public static byte[] encrypt(final byte[] raw, final byte[] clear) throws Exception {
        final SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES");
        cipher.init(1, sKeySpec);
        return cipher.doFinal(clear);
    }
    
    public static byte[] decrypt(final byte[] raw, final byte[] encrypted) throws Exception {
        final SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
        final Cipher cipher = Cipher.getInstance("AES");
        cipher.init(2, sKeySpec);
        return cipher.doFinal(encrypted);
    }
    
    public static byte[] HexToByte(final String hexString) {
        final int len = hexString.length() / 2;
        final byte[] result = new byte[len];
        for (int i = 0; i < len; ++i) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }
    
    static {
        NUMBERS = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
        secureRandom = new SecureRandom();
        VOWELS = new char[] { 'e', 'u', 'i', 'o', 'a' };
        CONS = new char[] { 'r', 't', 'p', 's', 'd', 'f', 'g', 'h', 'k', 'l', 'c', 'v', 'b', 'n', 'm' };
    }
    
    public enum DigestAlgorithm
    {
        PLAIN("plain", -1), 
        MD5("MD5", 128), 
        SHA1("SHA-1", 160), 
        SHA224("SHA-224", 224), 
        SHA256("SHA-256", 256), 
        SHA512("SHA-512", 512);
        
        private static final Map<String, DigestAlgorithm> ALGORITHMS;
        public final String name;
        public final int bits;
        public final int bytes;
        
        public static DigestAlgorithm byName(final String name) {
            return VerifyHelper.getMapValue(DigestAlgorithm.ALGORITHMS, name, String.format("Unknown digest algorithm: '%s'", name));
        }
        
        private DigestAlgorithm(final String name, final int bits) {
            this.name = name;
            this.bits = bits;
            this.bytes = bits / 8;
            assert bits % 8 == 0;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
        
        public byte[] verify(final byte[] digest) {
            if (digest.length != this.bytes) {
                throw new IllegalArgumentException("Invalid digest length: " + digest.length);
            }
            return digest;
        }
        
        static {
            final DigestAlgorithm[] algorithmsValues = values();
            ALGORITHMS = new HashMap<String, DigestAlgorithm>(algorithmsValues.length);
            for (final DigestAlgorithm algorithm : algorithmsValues) {
                DigestAlgorithm.ALGORITHMS.put(algorithm.name, algorithm);
            }
        }
    }
}
