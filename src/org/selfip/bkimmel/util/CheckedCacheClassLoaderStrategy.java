/**
 *
 */
package org.selfip.bkimmel.util;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.selfip.bkimmel.util.classloader.ClassLoaderStrategy;

/**
 * @author brad
 *
 */
public class CheckedCacheClassLoaderStrategy implements ClassLoaderStrategy {

	private final CheckedCache cache;
	private final ClassLoaderStrategy fallback;
	private final String digestAlgorithm;

	public CheckedCacheClassLoaderStrategy(CheckedCache cache, ClassLoaderStrategy fallback) {
		this(cache, "MD5", fallback);
	}

	public CheckedCacheClassLoaderStrategy(CheckedCache cache, String digestAlgorithm, ClassLoaderStrategy fallback) {
		this.cache = cache;
		this.digestAlgorithm = digestAlgorithm;
		this.fallback = fallback;
	}

	/* (non-Javadoc)
	 * @see org.selfip.bkimmel.util.classloader.ClassLoaderStrategy#getClassDefinition(java.lang.String)
	 */
	public ByteBuffer getClassDefinition(String name) {

		ByteBuffer def = null;
		String baseName = name.replaceAll("\\$.*$", "").replace('.', '/');
		String classPath = baseName + ".class";

		try {

			def = cache.get(classPath, null);

		} catch (DigestException e) {

			cache.remove(classPath);

		}

		if (def == null) {

			def = fallback.getClassDefinition(name);

			try {

				MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
				cache.put(classPath, def, digest);

			} catch (NoSuchAlgorithmException e) {

				e.printStackTrace();

			}

		}

		return def;

	}

}