package com.kaigan.game27.desktop;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Azmi on 9/11/2017.
 */

public class CodeBlob extends URLClassLoader {

    // Identity
    private final String[] packages;

    // Hashing, ciphers and compression
    private final MessageDigest sha1;
    private final MessageDigest md5;
    private final Cipher aes;
    private final Inflater inflater;
    private final CRC32 crc;

    // Data
    private final int[] lookup;
    private final int lookupMask;
    private final int[] descriptor;
    private final byte[] blob;

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> type = findLoadedClass(name);
        if(type != null)
            return type;
        // Override if name matches the packages
        if(contains(name)) {
            // Else load from blob
            byte[] bytecode = read(name);
            if(bytecode != null)
                type = defineClass(name, bytecode, 0, bytecode.length);
            else
                throw new ClassNotFoundException("Class not found: " + name);
            if(resolve)
                resolveClass(type);
            return type;
        }
        return super.loadClass(name, resolve);
    }

    private boolean contains(String name) {
        for(int c = 0; c < packages.length; c++) {
            if(name.startsWith(packages[c]))
                return true;
        }
        return false;       // not found
    }

    private byte[] read(String name) {
        // Generate lookup hash and key hash using name
        byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));

        // Hash name
        byte[] nameHash = sha1.digest(nameBytes);           // 160 bits, 20 bytes
        int name1 = (nameHash[19] << 24) | (nameHash[18] << 16) | (nameHash[17] << 8) | nameHash[16];      // 17 ~ 20
        int name2 = (nameHash[15] << 24) | (nameHash[14] << 16) | (nameHash[13] << 8) | nameHash[12];      // 13 ~ 16
        int name3 = (nameHash[11] << 24) | (nameHash[10] << 16) | (nameHash[9] << 8) | nameHash[8];        // 9 ~ 12
        int name4 = (nameHash[7] << 24) | (nameHash[6] << 16) | (nameHash[5] << 8) | nameHash[4];          // 5 ~ 8
        int name5 = (nameHash[3] << 24) | (nameHash[2] << 16) | (nameHash[1] << 8) | nameHash[0];          // 1 ~ 4

        // Lookup
        int start = name1 & lookupMask;
        int offset = 0;
        int compressedSize = 0;
        int uncompressedSize = 0;
        int crc32 = 0;
        for(int c = 0; c < lookup.length; c++) {
            int index = lookup[(start + c) % lookup.length];
            if(index == -1)
                return null;        // not found in lookup
            // Else there is an entry, make sure its correct
            index *= 9;
            int compare1 = descriptor[index];
            int compare2 = descriptor[index + 1];
            int compare3 = descriptor[index + 2];
            int compare4 = descriptor[index + 3];
            int compare5 = descriptor[index + 4];
            // Check if matches
            if(name1 == compare1 && name2 == compare2 && name3 == compare3 && name4 == compare4 && name5 == compare5) {
                // Found a match, extract entry
                offset = descriptor[index + 5];
                compressedSize = descriptor[index + 6];
                uncompressedSize = descriptor[index + 7];
                crc32 = descriptor[index + 8];
                break;      // found
            }
        }

        // Retrieved descriptor, now decrypt blob
        byte[] compressed = new byte[compressedSize];
        byte[] uncompressed = new byte[uncompressedSize];

        // Generate key
        byte[] keyHash = md5.digest(nameBytes);
        SecretKeySpec keySpec = new SecretKeySpec(keyHash, "AES");

        // Generate iv
        IvParameterSpec ivSpec = new IvParameterSpec(blob, offset, 16);

        // Decrypt
        try {
            aes.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            int encryptedSize = aes.getOutputSize(compressedSize);
            aes.doFinal(blob, offset + 16, encryptedSize, compressed, 0);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to decrypt class \"" + name + "\"", e);
        }

        // Check crc
        crc.reset();
        crc.update(compressed);
        int checksum = (int) crc.getValue();
        if(checksum != crc32)
            return null;        // Invalid checksum, either blob was tampered with or name was wrong (hash collision with descriptor)

        // Else deflate
        try {
            inflater.setInput(compressed);
            int position = 0;
            while(!inflater.finished())
                position += inflater.inflate(uncompressed, position, uncompressedSize - position);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to inflate class \"" + name + "\"", e);
        } finally  {
            inflater.reset();
        }

        // Done
        return uncompressed;
    }

    public CodeBlob(String filename, String ... packages) {
        this(CodeBlob.class.getClassLoader(), filename, packages);
    }

    private static URL[] getCurrentJAR() {
        URL location = CodeBlob.class.getProtectionDomain().getCodeSource().getLocation();
        return new URL[] { location };
    }

    public CodeBlob(ClassLoader parent, String filename, String ... packages) {
        super(getCurrentJAR(), parent);

        // Constants
        final int MAGIC = 0x4b434231;
        final float MAX_LOAD_FACTOR = 0.75f;
        final int MIN_DEFLATE_BUFFER_SIZE = 1024;        // 1kb
        final int INITIAL_BUFFER_SIZE = 10 * 1024;       // 10kb

        // Identity
        this.packages = packages;

        // Load digests, ciphers and compression
        try {
            sha1 = MessageDigest.getInstance("SHA-1");          // 160bit, 5 32-bit ints
            md5 = MessageDigest.getInstance("MD5");
            aes = Cipher.getInstance("AES/CTR/NoPadding");      // AES-128b, CTR with no padding
            inflater = new Inflater();
            crc = new CRC32();
        } catch (Throwable e) {
            throw new RuntimeException("Initialization failure", e);
        }


        // Build unlock key
        byte[] buffer = new byte[INITIAL_BUFFER_SIZE];
        // Build a stack trace and hash all the bytecodes of the classes in the stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        ClassLoader cl = getClass().getClassLoader();
        HashSet<Class<?>> hashedClasses = new HashSet<>();
        for(int c = 0; c < stackTrace.length; c++) {
            // Find class name
            StackTraceElement trace = stackTrace[c];
            String className = trace.getClassName();
            try {
                Class<?> type = Class.forName(className);
                while(!hashedClasses.contains(type)) {
                    // This is a new class, hash it
                    className = type.getName();
                    // Digest class file
                    String classFilename = className.replace('.', '/') + ".class";
                    try (InputStream s = cl.getResourceAsStream(classFilename)) {
                        if(s != null) {
                            int read;
                            while ((read = s.read(buffer)) != -1) {
                                sha1.update(buffer, 0, read);           // digest this
                            }
                        }
                    } catch (Throwable e) {
                        throw new RuntimeException("Unable to read class bytecode for \"" + className + "\"");
                    }
                    // Done with this class, remember and add supertype
                    hashedClasses.add(type);
                    type = type.getSuperclass();
                    if(type == null)
                        break;
                }
            } catch (Throwable e) {
                throw new RuntimeException("Error reading class \"" + className + "\"", e);
            }
        }
        byte[] unlockHash = sha1.digest();
        int unlock1 = (unlockHash[19] << 24) | (unlockHash[18] << 16) | (unlockHash[17] << 8) | unlockHash[16];      // 17 ~ 20
        int unlock2 = (unlockHash[15] << 24) | (unlockHash[14] << 16) | (unlockHash[13] << 8) | unlockHash[12];      // 13 ~ 16
        int unlock3 = (unlockHash[11] << 24) | (unlockHash[10] << 16) | (unlockHash[9] << 8) | unlockHash[8];        // 9 ~ 12
        int unlock4 = (unlockHash[7] << 24) | (unlockHash[6] << 16) | (unlockHash[5] << 8) | unlockHash[4];          // 5 ~ 8
        int unlock5 = (unlockHash[3] << 24) | (unlockHash[2] << 16) | (unlockHash[1] << 8) | unlockHash[0];          // 1 ~ 4

        // Check if file exists
        File file = new File(filename);
        if(!file.exists()) {
            // Load all classes
            HashMap<String, byte[]> classes = new HashMap<>();

            for(URL url : getURLs()) {
                try {
                    String path = url.toURI().getSchemeSpecificPart();
                    File jarFile = new File(path);
                    if(!jarFile.exists() || jarFile.isDirectory())
                        continue;
                             // Assume to be jar
                    try(JarFile jar = new JarFile(jarFile)) {
                        Enumeration<JarEntry> entries = jar.entries();
                        for (JarEntry entry; entries.hasMoreElements() && (entry = entries.nextElement()) != null; ) {
                            String name = entry.getName();

                            if (!name.endsWith(".class"))
                                continue;       // not a class

                            // Convert name from path
                            name = name.substring(0, name.length() - 6).replace('/', '.');

                            if (!contains(name) || classes.containsKey(name))
                                continue;       // not the correct package or already added

                            // Else its a class, read
                            int size = (int) entry.getSize();
                            byte[] bytes = new byte[size];
                            int read = 0;
                            try (InputStream s = jar.getInputStream(entry)) {
                                while (read < size) {
                                    int actual = s.read(bytes, read, size - read);
                                    if (actual == -1)
                                        throw new EOFException("Premature EOF reached");
                                    read += actual;
                                }
                            } catch (Throwable e) {
                                throw new RuntimeException("Failed to read remaining " + (size - read) + " bytes");
                            }

                            // Added
                            classes.put(name, bytes);
                        }
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to process \"" + url + "\"", e);
                }
            }

            // Prepare class data
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            SecureRandom random = new SecureRandom();
            int numEntries = classes.size();
            int[] descriptor = new int[numEntries * 9];         // array of sha-160 (5 ints), offset, compressedSize, uncompressedSize, crc32
            byte[] blob = new byte[INITIAL_BUFFER_SIZE];        // code blob
            int di = 0;
            int position = 0;
            for(Map.Entry<String, byte[]> entry : classes.entrySet()) {
                String name = entry.getKey();
                byte[] data = entry.getValue();

                // Compress data
                int compressedSize = 0;
                try {
                    deflater.setInput(data, 0, data.length);
                    deflater.finish();
                    while(!deflater.finished()) {
                        int available = buffer.length - compressedSize;
                        if(available < MIN_DEFLATE_BUFFER_SIZE) {
                            buffer = Arrays.copyOf(buffer, buffer.length * 2);      // double buffer size
                            available = buffer.length - compressedSize;
                        }
                        // Deflate
                        compressedSize += deflater.deflate(buffer, compressedSize, available);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to deflate class \"" + name + "\"", e);
                } finally {
                    deflater.reset();
                }


                // Generate lookup hash and key hash using name
                byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));

                // Generate name hash and add descriptor entry
                byte[] nameHash = sha1.digest(nameBytes);           // 160 bits, 20 bytes
                descriptor[di] = (nameHash[19] << 24) | (nameHash[18] << 16) | (nameHash[17] << 8) | nameHash[16];          // 17 ~ 20
                descriptor[di + 1] = (nameHash[15] << 24) | (nameHash[14] << 16) | (nameHash[13] << 8) | nameHash[12];      // 13 ~ 16
                descriptor[di + 2] = (nameHash[11] << 24) | (nameHash[10] << 16) | (nameHash[9] << 8) | nameHash[8];        // 9 ~ 12
                descriptor[di + 3] = (nameHash[7] << 24) | (nameHash[6] << 16) | (nameHash[5] << 8) | nameHash[4];          // 5 ~ 8
                descriptor[di + 4] = (nameHash[3] << 24) | (nameHash[2] << 16) | (nameHash[1] << 8) | nameHash[0];          // 1 ~ 4
                descriptor[di + 5] = position;
                descriptor[di + 6] = compressedSize;
                descriptor[di + 7] = data.length;        // uncompressed size

                // Calculate CRC32
                crc.reset();
                crc.update(buffer, 0, compressedSize);
                descriptor[di + 8] = (int) crc.getValue();

                // Encrypt

                // Generate key
                byte[] keyHash = md5.digest(nameBytes);
                SecretKeySpec keySpec = new SecretKeySpec(keyHash, "AES");

                // Generate iv
                byte[] iv = new byte[16];
                random.nextBytes(iv);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);

                // Encrypt data and append to blob
                try {
                    aes.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec, random);
                    int encryptedSize = aes.getOutputSize(compressedSize);

                    // Prepare output buffer
                    int requiredSize = position + encryptedSize + 16;     // 16 for iv
                    int blobSize = blob.length;
                    if(blobSize < requiredSize) {
                        do {
                            blobSize *= 2;
                        } while(blobSize < requiredSize);
                        blob = Arrays.copyOf(blob, blobSize);
                    }

                    // Write IV
                    System.arraycopy(iv, 0, blob, position, 16);
                    position += 16;

                    // Write encrypted
                    position += aes.doFinal(buffer, 0, compressedSize, blob, position);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to encrypt class \"" + name + "\"", e);
                }

                // Done, next class
                di += 9;
            }

            // Check for collisions (highly unlikely), and encrypt hash name
            for(int c = 0; c < numEntries; c++) {
                // Pull name
                int offset1 = c * 9;
                int name1 = descriptor[offset1];
                int name2 = descriptor[offset1 + 1];
                int name3 = descriptor[offset1 + 2];
                int name4 = descriptor[offset1 + 3];
                int name5 = descriptor[offset1 + 4];
                // Compare with the rest
                for(int i = c + 1; i < numEntries; i++) {
                    int offset2 = i * 9;
                    int compare1 = descriptor[offset2];
                    int compare2 = descriptor[offset2 + 1];
                    int compare3 = descriptor[offset2 + 2];
                    int compare4 = descriptor[offset2 + 3];
                    int compare5 = descriptor[offset2 + 4];

                    if(name1 == compare1 && name2 == compare2 && name3 == compare3 && name4 == compare4 && name5 == compare5)
                        throw new RuntimeException("Name collision for " + name1 + " " + name2 + " " + name3 + " " + name4 + " " + name5);
                }

                // Encrypt name
                descriptor[offset1] ^= unlock1;
                descriptor[offset1 + 1] ^= unlock2;
                descriptor[offset1 + 2] ^= unlock3;
                descriptor[offset1 + 3] ^= unlock4;
                descriptor[offset1 + 4] ^= unlock5;
            }

            // Write to file
            try {
                if (!file.createNewFile())
                    throw new RuntimeException("Failed to create new file \"" + filename + "\"");
            } catch (Throwable e) {
                throw new RuntimeException("Error creating new file \"" + filename + "\"", e);
            }
            try (DataOutputStream output = new DataOutputStream(new FileOutputStream(file))) {
                // Write magic
                output.writeInt(MAGIC);
                // Write descriptor
                output.writeInt(numEntries);
                for(int c = 0; c < descriptor.length; c++) {
                    output.writeInt(descriptor[c]);
                }
                // Write blob
                output.writeInt(position);
                output.write(blob, 0, position);
            } catch (Throwable e) {
                throw new RuntimeException("Failed to write blob to \"" + filename + "\n");
            }

            // Done
        }

        // Read file
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            // Read magic
            if (input.readInt() != MAGIC)
                throw new RuntimeException("Invalid magic");
            // Read num entries
            int numEntries = input.readInt();
            // Read descriptor
            descriptor = new int[numEntries * 9];
            for (int c = 0; c < descriptor.length; c++)
                descriptor[c] = input.readInt();
            // Create lookup table
            int size = 1;
            while (((float) numEntries / (float) size) > MAX_LOAD_FACTOR) {
                size <<= 1;
            }
            lookupMask = size - 1;
            lookup = new int[size];
            // Reset
            for (int c = 0; c < size; c++)
                lookup[c] = -1;
            // Populate with decrypted name hash
            for (int c = 0; c < numEntries; c++) {
                int offset = c * 9;
                descriptor[offset] ^= unlock1;
                descriptor[offset + 1] ^= unlock2;
                descriptor[offset + 2] ^= unlock3;
                descriptor[offset + 3] ^= unlock4;
                descriptor[offset + 4] ^= unlock5;
                int start = descriptor[offset] & lookupMask;
                // Find the nearest empty slot, wrap around if needed
                for (int i = 0; i < size; i++) {
                    int lookupOffset = (start + i) % size;
                    if (lookup[lookupOffset] == -1) {
                        // Found empty slot, place it
                        lookup[lookupOffset] = c;
                        break;
                    }
                }
            }
            // Read blob
            size = input.readInt();
            blob = new byte[size];
            int read = 0;
            while (read < size) {
                int actual = input.read(blob, read, size - read);
                if (actual == -1)
                    throw new RuntimeException("Premature EOF while reading blob");
                read += actual;
            }
            // Done
        } catch (Throwable e) {
            throw new RuntimeException("Failed to read blob \"" + filename + "\n");
        }
    }


}
