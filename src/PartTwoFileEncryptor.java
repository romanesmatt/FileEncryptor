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
public class PartTwoFileEncryptor {
    private static final Logger LOG = Logger.getLogger(PartTwoFileEncryptor.class.getSimpleName());

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
    public static void encrypt(String inputFile, String outputFile, Path tempDirectory, String keyString)
            throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException {



        //This snippet is literally copied from SymmetrixExample
        SecureRandom sr = new SecureRandom();
        byte[] key = new byte[16];
        byte[] initVector = new byte[16];
        sr.nextBytes(initVector); // 16 bytes IV

        //Converts the key Base64
        keyString = Base64.getEncoder().encodeToString(key);

        System.out.println("Random key = " + keyString);
        IvParameterSpec iv = new IvParameterSpec(initVector);
        SecretKeySpec skeySpec = new SecretKeySpec(keyString.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        final Path encryptedPath = tempDirectory.resolve(outputFile);
        try (InputStream fin = PartTwoFileEncryptor.class.getResourceAsStream(inputFile);
             OutputStream fout = Files.newOutputStream(encryptedPath);
             CipherOutputStream cipherOut = new CipherOutputStream(fout, cipher) {
             }) {
            final byte[] bytes = new byte[1024];
            for(int length=fin.read(bytes); length!=-1; length = fin.read(bytes)){
                fout.write(initVector);
                cipherOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to encrypt", e);
        }

        LOG.info("Encryption finished, saved at " + encryptedPath);

        key = null;
        keyString = null;
        initVector = null;

    }

    /**
     *
     * @param tempDirectory
     * @param inputFile
     * @param outputFile
     * @param keyString
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public static void decrypt( Path tempDirectory,
                                String inputFile, String outputFile, String keyString)
            throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        //This snippet is literally copied from SymmetrixExample

        /* For storing the key and iv */
        byte[] key;
        byte[] initVector;

        key = Base64.getDecoder().decode(keyString);
        initVector = new byte[16];

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
            Logger.getLogger(PartTwoFileEncryptor.class.getName()).log(Level.SEVERE, "Unable to decrypt", ex);
        }

        LOG.info("Decryption complete, open " + decryptedPath);

    }

    public static void main(String[] args){

        /* I got assistance implementing the code below */

        System.out.println("TESTING =============================================");

        String inFile, outFile, keyString;
        Path tempDir = Paths.get("");
        String decryption = "dec";
        String encryption  = "enc";

        try {
            if (args.length >= 1) {
                if (args[0].equals(encryption)) {
                    keyString = args[1];
                    inFile = args[2];
                    outFile = args[3];
                    encrypt(inFile, outFile, tempDir, keyString);
                }
            }
        }catch(IOException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }

        try {
            if (args.length >= 1) {
                if (args[0].equals(decryption)) {
                    inFile = args[2];
                    outFile = args[3];
                    keyString = args[1];
                    decrypt(tempDir, inFile, outFile, keyString);
                }
            }
        }catch(NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }



    }
}

