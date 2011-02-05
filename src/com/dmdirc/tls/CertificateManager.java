/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.tls;

import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.ListenerList;
import com.dmdirc.util.StreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509TrustManager;

import net.miginfocom.Base64;

/**
 * Manages storage and validation of certificates used when connecting to
 * SSL servers.
 *
 * @since 0.6.3m1
 */
public class CertificateManager implements X509TrustManager {
    
    /** List of listeners. */
    private final ListenerList listeners = new ListenerList();

    /** The server name the user is trying to connect to. */
    private final String serverName;

    /** The configuration manager to use for settings. */
    private final ConfigManager config;

    /** The set of CAs from the global cacert file. */
    private final Set<X509Certificate> globalTrustedCAs = new HashSet<X509Certificate>();

    /** Whether or not to check specified parts of the certificate. */
    private boolean checkDate, checkIssuer, checkHost;

    /** Used to synchronise the manager with the certificate dialog. */
    private final Semaphore actionSem = new Semaphore(0);

    /** The action to perform. */
    private CertificateAction action;
    
    /** A list of problems encountered most recently. */
    private final List<CertificateException> problems = new ArrayList<CertificateException>();
    
    /** The chain of certificates currently being validated. */
    private X509Certificate[] chain;

    /**
     * Creates a new certificate manager for a client connecting to the
     * specified server.
     *
     * @param serverName The name the user used to connect to the server
     * @param config The configuration manager to use
     */
    public CertificateManager(final String serverName, final ConfigManager config) {
        this.serverName = serverName;
        this.config = config;
        this.checkDate = config.getOptionBool("ssl", "checkdate");
        this.checkIssuer = config.getOptionBool("ssl", "checkissuer");
        this.checkHost = config.getOptionBool("ssl", "checkhost");

        loadTrustedCAs();
    }

    /**
     * Loads the trusted CA certificates from the Java cacerts store.
     */
    protected void loadTrustedCAs() {
        FileInputStream is = null;

        try {
            final String filename = System.getProperty("java.home")
                + "/lib/security/cacerts".replace('/', File.separatorChar);
            is = new FileInputStream(filename);
            final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, null);

            final PKIXParameters params = new PKIXParameters(keystore);
            for (TrustAnchor anchor : params.getTrustAnchors()) {
                globalTrustedCAs.add(anchor.getTrustedCert());
            }
        } catch (CertificateException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load trusted certificates", ex);
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load trusted certificates", ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load trusted certificates", ex);
        } catch (KeyStoreException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load trusted certificates", ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to load trusted certificates", ex);
        } finally {
            StreamUtil.close(is);
        }
    }

    /**
     * Retrieves a KeyManager[] for the client certificate specified in the
     * configuration, if there is one.
     *
     * @return A KeyManager to use for the SSL connection
     */
    public KeyManager[] getKeyManager() {
        if (config.hasOptionString("ssl", "clientcert.file")) {
            FileInputStream fis = null;
            try {
                final char[] pass;

                if (config.hasOptionString("ssl", "clientcert.pass")) {
                    pass = config.getOption("ssl", "clientcert.pass").toCharArray();
                } else {
                    pass = null;
                }

                fis = new FileInputStream(config.getOption("ssl", "clientcert.file"));
                final KeyStore ks = KeyStore.getInstance("pkcs12");
                ks.load(fis, pass);

                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                        KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(ks, pass);

                return kmf.getKeyManagers();
            } catch (FileNotFoundException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Certificate file not found", ex);
            } catch (KeyStoreException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to get key manager", ex);
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.MEDIUM, "Unable to get key manager", ex);
            } catch (CertificateException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to get key manager", ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to get key manager", ex);
            } catch (UnrecoverableKeyException ex) {
                Logger.appError(ErrorLevel.MEDIUM, "Unable to get key manager", ex);
            } finally {
                StreamUtil.close(fis);
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException {
        throw new CertificateException("Not supported.");
    }

    /**
     * Determines if the specified certificate is trusted by the user.
     *
     * @param certificate The certificate to be checked
     * @return True if the certificate matches one in the trusted certificate
     * store, or if the certificate's details are marked as trusted in the
     * DMDirc configuration file.
     */
    public TrustResult isTrusted(final X509Certificate certificate) {
        try {
            final String sig = Base64.encodeToString(certificate.getSignature(), false);

            if (config.hasOptionString("ssl", "trusted") && config.getOptionList("ssl",
                    "trusted").contains(sig)) {
                return TrustResult.TRUSTED_MANUALLY;
            } else {
                for (X509Certificate trustedCert : globalTrustedCAs) {
                    if (Arrays.equals(certificate.getSignature(), trustedCert.getSignature())
                            && certificate.getIssuerDN().getName()
                            .equals(trustedCert.getIssuerDN().getName())) {
                        certificate.verify(trustedCert.getPublicKey());
                        return TrustResult.TRUSTED_CA;
                    }
                }
            }
        } catch (Exception ex) {
           return TrustResult.UNTRUSTED_EXCEPTION;
        }

        return TrustResult.UNTRUSTED_GENERAL;
    }

    /**
     * Determines whether the given certificate has a valid CN or alternate
     * name for this server's hostname.
     *
     * @param certificate The certificate to be validated
     * @return True if the certificate is valid for this server's host, false
     * otherwise
     */
    public boolean isValidHost(final X509Certificate certificate) {
        final Map<String, String> fields = getDNFieldsFromCert(certificate);
        if (fields.containsKey("CN") && isMatchingServerName(fields.get("CN"))) {
            return true;
        }

        try {
            if (certificate.getSubjectAlternativeNames() != null) {
                for (List<?> entry : certificate.getSubjectAlternativeNames()) {
                    final int type = ((Integer) entry.get(0)).intValue();

                    // DNS or IP
                    if ((type == 2 || type == 7) && isMatchingServerName((String) entry.get(1))) {
                        return true;
                    }
                }
            }
        } catch (CertificateParsingException ex) {
            return false;
        }

        return false;
    }

    /**
     * Checks whether the specified target matches the server name this
     * certificate manager was initialised with.
     *
     * Target names may contain wildcards per RFC2818.
     *
     * @since 0.6.5
     * @param target The target to compare to our server name
     * @return True if the target matches, false otherwise
     */
    protected boolean isMatchingServerName(final String target) {
        final String[] targetParts = target.split("\\.");
        final String[] serverParts = serverName.split("\\.");

        if (targetParts.length != serverParts.length) {
            // Fail fast if they don't match
            return false;
        }

        for (int i = 0; i < serverParts.length; i++) {
            if (!serverParts[i].matches("\\Q" + targetParts[i].replace("*", "\\E.*\\Q") + "\\E")) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType)
            throws CertificateException {
        this.chain = chain;
        problems.clear();
        
        boolean verified = false;
        boolean manual = false;

        if (checkHost) {
            // Check that the cert is issued to the correct host
            verified = isValidHost(chain[0]);

            if (!verified) {
                problems.add(new CertificateDoesntMatchHostException(
                        "Certificate was not issued to " + serverName));
            }

            verified = false;
        }

        for (X509Certificate cert : chain) {
            final TrustResult trustResult = isTrusted(cert);

            if (checkDate) {
                // Check that the certificate is in-date
                try {
                    cert.checkValidity();
                } catch (CertificateException ex) {
                    problems.add(ex);
                }
            }

            if (checkIssuer) {
                // Check that we trust an issuer

                verified |= trustResult.isTrusted();
            }

            if (trustResult == TrustResult.TRUSTED_MANUALLY) {
                manual = true;
            }
        }

        if (!verified && checkIssuer) {
            problems.add(new CertificateNotTrustedException("Issuer is not trusted"));
        }

        if (!problems.isEmpty() && !manual) {
            for (CertificateProblemListener listener : listeners.get(CertificateProblemListener.class)) {
                listener.certificateProblemEncountered(chain, problems, this);
            }

            try {
                actionSem.acquire();
            } catch (InterruptedException ie) {
                throw new CertificateException("Thread aborted", ie);
            } finally {
                problems.clear();
                
                for (CertificateProblemListener listener : listeners.get(CertificateProblemListener.class)) {
                    listener.certificateProblemResolved(this);
                }
            }

            switch (action) {
                case DISCONNECT:
                    throw new CertificateException("Not trusted");
                case IGNORE_PERMANENTY:
                    final List<String> list = new ArrayList<String>(config
                            .getOptionList("ssl", "trusted"));
                    list.add(Base64.encodeToString(chain[0].getSignature(), false));
                    IdentityManager.getConfigIdentity().setOption("ssl",
                            "trusted", list);
                    break;
                case IGNORE_TEMPORARILY:
                    // Do nothing, continue connecting
                    break;
            }
        }
    }
    
    /**
     * Gets the chain of certificates currently being validated, if any.
     *
     * @return The chain of certificates being validated
     */
    public X509Certificate[] getChain() {
        return chain;
    }
    
    /**
     * Gets the set of problems that were encountered with the last certificate.
     *
     * @return The set of problems encountered, or any empty collection if there
     * is no current validation attempt ongoing.
     */
    public Collection<CertificateException> getProblems() {
        return problems;
    }

    /**
     * Sets the action to perform for the request that's in progress.
     *
     * @param action The action that's been selected
     */
    public void setAction(final CertificateAction action) {
        this.action = action;

        actionSem.release();
    }

    /**
     * Retrieves the name of the server to which the user is trying to connect.
     *
     * @return The name of the server that the user is trying to connect to
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Reads the fields from the subject's designated name in the specified
     * certificate.
     *
     * @param cert The certificate to read
     * @return A map of the fields in the certificate's subject's designated
     * name
     */
    public static Map<String, String> getDNFieldsFromCert(final X509Certificate cert) {
        final Map<String, String> res = new HashMap<String, String>();

        try {
            final LdapName name = new LdapName(cert.getSubjectX500Principal().getName());
            for (Rdn rdn : name.getRdns()) {
                res.put(rdn.getType(), rdn.getValue().toString());
            }
        } catch (InvalidNameException ex) {
            // Don't care
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return globalTrustedCAs.toArray(new X509Certificate[globalTrustedCAs.size()]);
    }
    
   /**
     * Adds a new certificate problem listener to this manager.
     *
     * @param listener The listener to be added
     */
    public void addCertificateProblemListener(final CertificateProblemListener listener) {
        listeners.add(CertificateProblemListener.class, listener);
    }
    
    /**
     * Removes the specified listener from this manager.
     *
     * @param listener The listener to be removed
     */
    public void removeCertificateProblemListener(final CertificateProblemListener listener) {
        listeners.remove(CertificateProblemListener.class, listener);
    }

}