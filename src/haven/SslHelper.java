/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import javax.net.ssl.*;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

public class SslHelper {
    private KeyStore creds, trusted;
    private SSLContext ctx = null;
    private SSLSocketFactory sfac = null;
    private int tserial = 0;
    private char[] pw;
    private HostnameVerifier ver = null;

    public SslHelper() {
	creds = null;
	try {
	    trusted = KeyStore.getInstance(KeyStore.getDefaultType());
	    trusted.load(null, null);
	} catch(Exception e) {
	    throw(new Error(e));
	}
    }

    private synchronized SSLContext ctx() {
	if(ctx == null) {
	    TrustManagerFactory tmf;
	    KeyManagerFactory kmf;
	    try {
		ctx = SSLContext.getInstance("TLS");
		tmf = TrustManagerFactory.getInstance("PKIX");
		kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyManager[] kms = null;
		tmf.init(trusted);
		if(creds != null) {
		    kmf.init(creds, pw);
		    kms = kmf.getKeyManagers();
		}
		ctx.init(kms, tmf.getTrustManagers(), new SecureRandom());
	    } catch(NoSuchAlgorithmException e) {
		throw(new Error(e));
	    } catch(KeyStoreException e) {
		throw(new RuntimeException(e));
	    } catch(UnrecoverableKeyException e) {
		/* The key should be recoverable at this stage, since
		 * it was loaded successfully. */
		throw(new RuntimeException(e));
	    } catch(KeyManagementException e) {
		throw(new RuntimeException(e));
	    }
	}
	return(ctx);
    }

    private synchronized SSLSocketFactory sfac() {
	if(sfac == null)
	    sfac = ctx().getSocketFactory();
	return(sfac);
    }

    private void clear() {
	ctx = null;
	sfac = null;
    }

    public synchronized void trust(Certificate cert) {
	clear();
	try {
	    trusted.setCertificateEntry("cert-" + tserial++, cert);
	} catch(KeyStoreException e) {
	    /* The keystore should have been initialized and should
	     * not have the generated alias, so this should not
	     * happen. */
	    throw(new RuntimeException(e));
	}
    }

    public static Certificate loadX509(InputStream in) throws IOException, CertificateException {
	CertificateFactory fac = CertificateFactory.getInstance("X.509");
	return(fac.generateCertificate(in));
    }

    public static Collection<? extends Certificate> loadX509s(InputStream in) throws IOException, CertificateException {
	CertificateFactory fac = CertificateFactory.getInstance("X.509");
	return(fac.generateCertificates(in));
    }

    public void trust(InputStream in) throws IOException, CertificateException {
	for(Certificate cert : loadX509s(in))
	    trust(cert);
    }

    public synchronized void loadCredsPkcs12(InputStream in, char[] pw) throws IOException, CertificateException {
	clear();
	try {
	    creds = KeyStore.getInstance("PKCS12");
	    creds.load(in, pw);
	    this.pw = pw;
	} catch(KeyStoreException e) {
	    throw(new Error(e));
	} catch(NoSuchAlgorithmException e) {
	    throw(new Error(e));
	}
    }

    public HttpsURLConnection connect(URL url) throws IOException {
	if(!url.getProtocol().equals("https"))
	    throw(new MalformedURLException("Can only be used to connect to HTTPS servers"));
	HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
	conn.setSSLSocketFactory(sfac());
	if(ver != null)
	    conn.setHostnameVerifier(ver);
	return(conn);
    }

    public HttpsURLConnection connect(String url) throws IOException {
	return(connect(new URL(url)));
    }

    public void ignoreName() {
	ver = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession sess) {
		    return(true);
		}
	    };
    }

    public SSLSocket connect(Socket sk, String host, int port, boolean autoclose) throws IOException {
	return((SSLSocket)sfac().createSocket(sk, host, port, autoclose));
    }

    public SSLSocket connect(String host, int port) throws IOException {
	IOException lerr = null;
	for(InetAddress haddr : InetAddress.getAllByName(host)) {
	    try {
		Socket sk = new HackSocket();
		sk.connect(new InetSocketAddress(haddr, port), 5000);
		return(connect(sk, host, port, true));
	    } catch(IOException e) {
		if(lerr != null)
		    e.addSuppressed(lerr);
		lerr = e;
	    }
	}
	if(lerr != null)
	    throw(lerr);
	throw(new UnknownHostException(host));
    }

    public boolean hasCreds() {
	return(creds != null);
    }
}
