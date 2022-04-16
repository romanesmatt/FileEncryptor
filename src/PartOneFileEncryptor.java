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
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Erik Costlow
 */
public class PartOneFileEncryptor {
    private static final Logger LOG = Logger.getLogger(PartOneFileEncryptor.class.getSimpleName());

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
    public static void encrypt(String inputFile, String outputFile, Path tempDirectory)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException {

        //This snippet is literally copied from SymmetrixExample
        SecureRandom sr = new SecureRandom();
        byte[] key = new byte[16];
        sr.nextBytes(key); // 128 bit key
        byte[] initVector = new byte[16];
        sr.nextBytes(initVector); // 16 bytes IV

        //Converts the key and IV to Base64
        String encodedKey = Base64.getEncoder().encodeToString(key);
        String encodedIV = Base64.getEncoder().encodeToString(initVector);


        System.out.println("Random key = " + encodedKey);
        System.out.println("initVector = " + encodedIV);
        IvParameterSpec iv = new IvParameterSpec(initVector);
        SecretKeySpec skeySpec = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        final Path encryptedPath = tempDirectory.resolve(outputFile);
        try (InputStream fin = PartOneFileEncryptor.class.getResourceAsStream(inputFile);
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
        key = null;
        initVector = null;
        encodedKey = null;
        encodedIV = null;


    }

    /**
     *
     * @param tempDirectory
     * @param inputFile
     * @param outputFile
     * @param keyString
     * @param ivString
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static void decrypt( Path tempDirectory,
                               String inputFile, String outputFile, String keyString, String ivString)
            throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        //This snippet is literally copied from SymmetrixExample

        /* For storing the key and iv */
        byte[] key;
        byte[] initVector;

        key = Base64.getDecoder().decode(keyString);
        initVector = Base64.getDecoder().decode(ivString);

        SecretKeySpec skeySpec = new SecretKeySpec(key, ALGORITHM);
        IvParameterSpec iv = new IvParameterSpec(initVector);
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
            Logger.getLogger(PartOneFileEncryptor.class.getName()).log(Level.SEVERE, "Unable to decrypt", ex);
        }

        LOG.info("Decryption complete, open " + decryptedPath);

        /* Ensuring safe security of the program */
        key = null;
        initVector = null;

    }

    public static void main(String[] args){

        System.out.println("TESTING =============================================");

        /* I got assistance implementing the code below */

        String inFile, outFile, keyString, ivString;
        Path tempDir = Paths.get("");
        String decryption = "dec";
        String encryption  = "enc";

        try {
            if(args.length >= 1) {
                if (args[0].equals(encryption)) {
                    inFile = args[1];
                    outFile = args[2];
                    encrypt(inFile, outFile, tempDir);
                }
            }
        }catch(IOException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }

        try {
           if(args.length >= 1) {
                if (args[0].equals(decryption)) {
                    inFile = args[1];
                    outFile = args[2];
                    keyString = args[3];
                    ivString = args[4];
                    decrypt(tempDir, keyString
                    , ivString, inFile, outFile);
                }
            }
        }catch(NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }



    }
}
