import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.PBEKeySpec;


/**
 *
 * @author Erik Costlow
 */
public class PartFourFileEncryptor {
    private static final Logger LOG = Logger.getLogger(PartThreeFileEncryptor.class.getSimpleName());

    private static final String ALGORITHM = "AES";
    private static final String CIPHER = "AES/CBC/PKCS5PADDING";

    /**
     *
     * @param inputFile
     * @param outputFile
     * @param tempDirectory
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    public static void encrypt(String inputFile, String outputFile, Path tempDirectory, String algorithm, String password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {

        /* Generate new password (only required if password has not been specified by the user) */
        if(password == null || password.isEmpty()){

        }  KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        password = Base64.getEncoder().encodeToString(keyGenerator.generateKey().getEncoded());


        //This snippet is literally copied from SymmetrixExample
        SecureRandom sr = new SecureRandom();
        byte[] initVector = new byte[16];
        sr.nextBytes(initVector); // 16 bytes IV

        /* Generating a new key from the given password */
        KeySpec keyFromPassword = new PBEKeySpec(password.toCharArray(), initVector, 65536, 256);

        sr.nextBytes(initVector); // 16 bytes IV
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        IvParameterSpec iv = new IvParameterSpec(initVector);
        SecretKey temporary = factory.generateSecret(keyFromPassword);
        SecretKeySpec skeySpec = new SecretKeySpec(temporary.getEncoded(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        //Look for files here

        final Path encryptedPath = tempDirectory.resolve(outputFile);
        try (InputStream fin = PartThreeFileEncryptor.class.getResourceAsStream(inputFile);
             OutputStream fout = Files.newOutputStream(encryptedPath);
             CipherOutputStream cipherOut = new CipherOutputStream(fout, cipher) {
             }) {
            final byte[] bytes = new byte[1024];
            for(int length=fin.read(bytes); length!=-1; length = fin.read(bytes)){
                cipherOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to encrypt", e);
        }

        LOG.info("Encryption finished, saved at " + encryptedPath);

        /* Ensuring safe security of the program */
        initVector = null;
        password = null;

    }

    /**
     *
     * @param tempDirectory
     * @param inputFile
     * @param outputFile
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static void decrypt( Path tempDirectory,
                                String inputFile, String outputFile, String algorithm, String password)
            throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {


        /* Generate new password (only required if password has not been specified by the user) */
        if(password == null || password.isEmpty()){

        }  KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256);
        password = Base64.getEncoder().encodeToString(keyGenerator.generateKey().getEncoded());

        /* For storing the iv */
        byte[] initVector;
        initVector = new byte[16];

        KeySpec keyFromPassword = new PBEKeySpec(password.toCharArray(), initVector, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        IvParameterSpec iv = new IvParameterSpec(initVector);
        SecretKey temporary = factory.generateSecret(keyFromPassword);
        SecretKeySpec skeySpec = new SecretKeySpec(temporary.getEncoded(), ALGORITHM);

        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        final Path decryptedPath = tempDirectory.resolve(outputFile);
        final Path encryptedPath = tempDirectory.resolve(inputFile);
        try(InputStream encryptedData = Files.newInputStream(encryptedPath);
            CipherInputStream decryptStream = new CipherInputStream(encryptedData, cipher);
            OutputStream decryptedOut = Files.newOutputStream(decryptedPath)){
            final byte[] bytes = new byte[1024];
            for(int length=decryptStream.read(bytes); length!=-1; length = decryptStream.read(bytes)){
                decryptedOut.write(bytes, 0, length);
            }
        } catch (IOException ex) {
            Logger.getLogger(PartThreeFileEncryptor.class.getName()).log(Level.SEVERE, "Unable to decrypt", ex);
        }

        LOG.info("Decryption complete, open " + decryptedPath);

        /* Ensuring safe security */
        initVector = null;
        password = null;

    }

    /**
     * When "info" is specified by the user, the method will display the
     * algorithm used for the encryption and decryption
     * @param algorithm
     */
    public void info(String algorithm, String inputFile){
        System.out.println(algorithm);
    }

    public static void main(String[] args) {

        /* I got assistance implementing the code below */

        System.out.println("TESTING =============================================");

        String inFile, outFile, password, algorithm, info;
        Path tempDir = Paths.get("");
        String decryption = "dec";
        String encryption  = "enc";

        try {
            if (args.length >= 1) {
                if (args[0].equals(encryption)) {
                    algorithm = args[2];
                    password = args[3];
                    inFile = args[4];
                    outFile = args[5];
                    encrypt(inFile, outFile, tempDir, algorithm, password);
                    password = null; //Wiping password after use
                }
            }
        }catch(IOException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException
                | InvalidKeySpecException e){
            e.printStackTrace();
        }

        try {
            if (args.length >= 1) {
                if (args[0].equals(decryption)) {
                    algorithm = args[2];
                    password = args[3];
                    inFile = args[4];
                    outFile = args[5];
                    decrypt(tempDir, inFile, outFile, algorithm, password);
                    password = null; //Wiping password after use
                }
            }
        }catch(NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | InvalidKeySpecException e){
            e.printStackTrace();
        }






    }
}
