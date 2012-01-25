package com.hp.hpl.jena.grddl.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.net.URL;
import java.rmi.server.UID;
import java.security.Permission;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PropertyPermission;
import java.util.Set;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.Sender;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.SystemFunctionLibrary;
import net.sf.saxon.functions.SystemProperty;
import net.sf.saxon.style.XSLGeneralIncorporate;
import net.sf.saxon.style.XSLStylesheet;
import net.sf.saxon.type.TypeHierarchy;

import org.apache.xerces.impl.XMLEntityManager;

import sun.net.NetworkClient;
import sun.net.www.http.HttpClient;
import sun.net.www.http.KeepAliveCache;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.BrokenException;

public class SecManager extends SecurityManager {

	static {
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecManager());
	}

	private class SecContext {
		final boolean untrusted;

		final Object fromSuper;

		SecContext(Object fS) {
			fromSuper = fS;
			untrusted = untrustedThreadGroup();
		}
	}

	public SecManager() {
	}

	static private ThreadGroup notTrustedTG = new ThreadGroup("untrusted");

	private static int sb = 0;

	boolean untrustedThreadGroup() {
		return Thread.currentThread().getThreadGroup() == notTrustedTG;
	}

	public Object getSecurityContext() {
		return new SecContext(super.getSecurityContext());
	}

	// static private String filesep = System.getProperty("file.separator");
	static private String javaHome = System.getProperty("java.home");

	static private Set<String> onClassPath = new HashSet<String>();

	static private Set<String> allowProps = new HashSet<String>();

	static private Set<String> nonJarsOnClassPath = new HashSet<String>();

	static {
		String classpath = System.getProperty("java.class.path");
		String pathsep = System.getProperty("path.separator");
		String cp[] = classpath.split(pathsep);
		for (int i = 0; i < cp.length; i++) {
			File f = new File(cp[i]);
			try {
				boolean isJar = false;
				String absP = f.getAbsolutePath();
				onClassPath.add(absP);
				String absPLC = absP.toLowerCase();
				if (absPLC.endsWith(".jar"))
					isJar = true;
				if (absPLC.endsWith(".zip"))
					isJar = true;
				if (!isJar)
					nonJarsOnClassPath.add(absP);
				String canP = f.getCanonicalPath();
				onClassPath.add(canP);
				if (!isJar)
					nonJarsOnClassPath.add(canP);
			} catch (IOException e) {
				// TODO panic message
				e.printStackTrace();
			}
		}
		allowProps.add("javax.xml.parsers.SAXParserFactory");
		allowProps.add("org.apache.xerces.xni.parser.XMLParserConfiguration");
		allowProps.add("java.home");
		allowProps.add("line.separator");
		allowProps.add("jaxp.debug");
		allowProps.add("java.version");
	}

	public void checkPermission(Permission p) {
		checkPerm(p);
	}

	private void checkPerm(Permission p) {
		String actions = p.getActions();
		String name = p.getName();
		if (untrustedThreadGroup()) {
			try {
				checkPerm(p, actions, name);
			} catch (SecurityException e) {
				trace(false, p, name, actions);
				throw e;
			}
			trace(true, p, name, actions);
		}
	}

	private void trace(boolean permit, Permission p, String name, String actions) {
		 if (true)
		 return;
		if (p instanceof FilePermission)
			return;
		if (permit)
			return;
		System.err.println((permit ? "Allow: " : "Deny: ") + p);
		if ((!permit)
		// ||name.equals("createClassLoader")
		)
			dumpClassContext();

	}

	private void checkPerm(Permission p, String actions, String name) {

		if (p instanceof FilePermission) {
			File f = new File(name);
			if (actions.equals("read")
					&& (onClassPath.contains(name)
							|| onClassPath.contains(f.getAbsolutePath()) || startsWithOKDir(name)))
				return;
		}
		if (p instanceof PropertyPermission) {
			if (actions.equals("read")) {
				if (!checkStack(noSystemProperty)) {
					if (allowProps.contains(name))
						return;
					if (p.getName().toLowerCase().contains("proxy"))
						return;
					if (isJenaAnonId(getClassContext())) {
						return;
					}
				}
			}
		}

		if (p instanceof NetPermission
				&& (name.equals("getCookieHandler")
						|| name.equals("getResponseCache") || name
						.equals("getProxySelector"))) {
			if (checkStack(xmlParserStack))
				return;
			if (checkStack(preparedStylesheetStack))
				return;
			if (checkStack(importStack))
				return;
		}
		if (p instanceof SocketPermission) {
			if (checkStack(xmlParserStack))
				return;
			if (checkStack(preparedStylesheetStack))
				return;
			if (checkStack(importStack))
				return;
		}
		if (p instanceof ReflectPermission
				&& name.equals("suppressAccessChecks")) {

			if (isJenaAnonId(getClassContext())) {
				return;
			}

			if (checkStack(classLoaderMiniStack))
				return;
			if (checkStack(accesibleMiniStack))
				return;
			if (checkStack(accesibleMethodStack))
				return;
			if (checkStack(methodInvokeStack))
				return;

		}
		if (p instanceof RuntimePermission
				&& name.startsWith("accessClassInPackage.")) {
			// TODO more specific

			if (isJenaAnonId(getClassContext())) {
				return;
			}
		}
		if (p instanceof RuntimePermission
				&& name.equals("writeFileDescriptor")) {

			if (checkStack(preparedStyleSheetWriteSocket))
				return;
			if (checkStack(xmlParserWriteSocket))
				return;
			if (checkStack(importWriteSocket))
				return;
		}
		if (p instanceof RuntimePermission && name.equals("readFileDescriptor")) {

			if (checkStack(preparedStyleSheetReadSocket))
				return;
			if (checkStack(xmlParserReadSocket))
				return;
			if (checkStack(importReadSocket))
				return;
		}
		if (p instanceof RuntimePermission && name.equals("createClassLoader")) {

			if (checkStack(classLoaderMiniStack))
				return;

			if (checkStack(methodInvokeStack))
				return;
		}
		if (p instanceof RuntimePermission
				&& (name.equals("modifyThreadGroup") || name
						.equals("modifyThread"))) {
			if (checkStack(httpClientStack))
				return;
		}
		if (p instanceof SecurityPermission
				&& (name.startsWith("getProperty.") || name
						.startsWith("putProviderProperty."))) {
			if (isJenaAnonId(getClassContext()))
				return;

		}
		throw new SecurityException(p.toString());
	}

	private boolean checkStack(String[][] pattern) {
		Class st[] = getClassContext();
		int next = 0;
		int matchedSoFar = 0;
		for (int i = 0; i < st.length; i++) {
			if (next > pattern.length)
				return false;
			if (pattern[next][0].equals("weak")
					&& match(pattern[next + 1][1], st[i].getName())) {
				next++;
			}
			if (pattern[next][0].equals("return")) {
				if (pattern[next][1].equals("allowedNewInstance")) {
					// System.err.println("Allowing new instance from: "+
					// st[i].getName());
					return allowedNewInstance.contains(st[i].getName());
				}
				throw new BrokenException("Internal error: detail: "
						+ pattern[next][1]);
			}
			while (!match(pattern[next][1], st[i].getName())) {
				if ((pattern[next][0].equals("some") && matchedSoFar > 0)
						|| pattern[next][0].equals("optional")
						|| pattern[next][0].equals("any")) {
					next++;
					matchedSoFar = 0;
					continue;
				}
				return false;
			}
			if (pattern[next][0].equals("1")
					|| pattern[next][0].equals("optional")) {
				next++;
				matchedSoFar = 0;
			} else {
				matchedSoFar++;
			}
		}
		return next==pattern.length || next==pattern.length-1;
	}

	private static boolean match(String pattern, String name) {
		boolean result = name.startsWith(pattern);
		return result;
	}

	private boolean startsWithOKDir(String name) {
		if (name.startsWith(javaHome)) {
			return true;
		}
		Iterator<String> it = nonJarsOnClassPath.iterator();
		while (it.hasNext()) {
			if (name.startsWith(it.next()))
				return true;
		}
		return false;
	}

	private boolean isJenaAnonId(Class[] classContext) {
		int anonId = -1;
		for (int i = 0; i < classContext.length; i++)
			if (classContext[i] == AnonId.class) {
				anonId = i;
				break;
			}
		if (anonId == -1)
			return false;
		dumpClassContext();
		// TODO: check stack??
		if (true)
			return true;

		for (int i = 0; i < anonId; i++) {
			Class c = classContext[i];
			if (c == SecManager.class)
				continue;
			if (c == UID.class)
				continue;
			if (c == Security.class)
				continue;
			if (c == SecureRandom.class)
				continue;
			// if (c==Providers.class)
			// continue;
			// if (c==ProviderList.class)
			// continue;
			if (c.getName().startsWith("sun.security.jca.Provider"))
				continue;
			// continue;
			return false;
		}
		return true;
	}

	public void checkPermission(Permission p, Object o) {
		checkPerm(p, o);
	}

	public void checkPerm(Permission p, Object o) {
		SecContext sc = (SecContext) o;
		if (sc.untrusted) {
			try {
				super.checkPermission(p, sc.fromSuper);
			} catch (SecurityException e) {
				System.err.println("Deny: " + p);
				throw e;
			}
			System.err.println("Permit: " + p);
		}
	}

	private void dumpClassContext() {
		Class st[] = getClassContext();
		for (int i = 0; i < st.length; i++)
			System.err.println(i + ": " + st[i].getName());

	}

	// TODO: max stack size param
	public static void sandbox(Runnable r) {
		Thread t = new Thread(notTrustedTG, r, "SandBoxed-" + sb++);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final String[][] socketOutputSubstack = new String[][] {
			{ "1", FileOutputStream.class.getName() },
			{ "1", "java.net.SocketOutputStream" }, };

	private static final String[][] socketInputSubstack = new String[][] {
			{ "1", FileInputStream.class.getName() },
			{ "1", "java.net.SocketInputStream" }, };

	private static final String[][] xmlParserSubstack = new String[][] {
			{ "any", "java.net." },
			{ "optional", NetworkClient.class.getName() },
			{ "any", HttpClient.class.getName() },
			{ "some", "sun.net.www.protocol.http." },
			{ "optional", URL.class.getName() },
			{ "some", XMLEntityManager.class.getName() },
			{ "some", "org.apache.xerces." },
			{ "some", Sender.class.getName() },
			{ "1", Controller.class.getName() } };

	private static final String[][] preparedStyleSheetSubstack = new String[][] {
			{ "any", "java.net." },
			{ "optional", NetworkClient.class.getName() },
			{ "any", HttpClient.class.getName() },
			{ "some", "sun.net.www.protocol.http." },
			{ "optional", URL.class.getName() },
			{ "some", XMLEntityManager.class.getName() },
			{ "some", "org.apache.xerces." },
			{ "some", Sender.class.getName() },
			{ "some", PreparedStylesheet.class.getName() },
			{ "some", TransformerFactoryImpl.class.getName() } };

	private static final String[][] importSubstack = new String[][] {
			{ "any", "java.net." },
			{ "optional", NetworkClient.class.getName() },
			{ "any", HttpClient.class.getName() },
			{ "some", "sun.net.www.protocol.http." },
			{ "optional", URL.class.getName() },
			{ "some", XMLEntityManager.class.getName() },
			{ "some", "org.apache.xerces." },
			{ "some", Sender.class.getName() },
			{ "some", PreparedStylesheet.class.getName() },
			{ "some", XSLGeneralIncorporate.class.getName() },
			{ "some", XSLStylesheet.class.getName() },
			{ "some", PreparedStylesheet.class.getName() },
			{ "some", TransformerFactoryImpl.class.getName() } };

	static private String[][] xmlParserStack = topTail(xmlParserSubstack);

	static private String[][] preparedStylesheetStack = topTail(preparedStyleSheetSubstack);

	static private String[][] importStack = topTail(importSubstack);

	static private String[][] xmlParserWriteSocket = topTail(

	concat(socketOutputSubstack, xmlParserSubstack));

	static private String[][] preparedStyleSheetWriteSocket = topTail(concat(
			socketOutputSubstack, preparedStyleSheetSubstack));

	static private String[][] importWriteSocket = topTail(

	concat(socketOutputSubstack, importSubstack));

	static private String[][] xmlParserReadSocket = topTail(

	concat(socketInputSubstack, xmlParserSubstack));

	static private String[][] importReadSocket = topTail(

	concat(socketInputSubstack, importSubstack));

	static private String[][] preparedStyleSheetReadSocket = topTail(concat(
			socketInputSubstack, preparedStyleSheetSubstack));

	static private String[][] noSystemProperty = new String[][] {
			{ "weak", "" }, { "1", SystemProperty.class.getName() },
			{ "some", "" }, };

	static private String[][] classLoaderMiniStack = new String[][] {
			{ "weak", "" }, { "1", Constructor.class.getName() },
			{ "1", Class.class.getName() }, { "1", Class.class.getName() },
			{ "return", "allowedNewInstance" }, };
	static private String[][] accesibleMiniStack = new String[][] {
		{ "weak", "" }, 
		{ "1", AccessibleObject.class.getName() },
		{ "1", Class.class.getName() }, 
		{ "1", Class.class.getName() }, 
		{ "1", Class.class.getName() },
		{ "return", "allowedNewInstance" }, };
	static private String[][] accesibleMethodStack = new String[][] {
		{ "weak", "" }, 
		{ "1", AccessibleObject.class.getName() },
		{ "1", Class.class.getName() }, 
		{ "1", Class.class.getName() }, 
		{ "1", Class.class.getName() },
		{ "some", "sun.reflect.MethodAccessorGenerator" },
		
		{ "some", "" }, };
	static private String[][] httpClientStack = new String[][] {
			{ "some", SecManager.class.getName() },
			{ "optional", SecurityManager.class.getName() },
			{ "any", ThreadGroup.class.getName() },
			{ "any", Thread.class.getName() },
			{ "any", KeepAliveCache.class.getName() },
			{ "some", HttpClient.class.getName() }, { "some", "" } };

	static private String[][] methodInvokeStack = new String[][] {
		{ "weak", "" },
		{ "some", "sun.reflect.ClassDefiner" },
		{ "some", "sun.reflect.MethodAccessorGenerator" },
		{ "some", "" } };

	private static String[][] topTail(String[][] strings) {
		String[][] result = new String[strings.length + 4][];
		System.arraycopy(strings, 0, result, 2, strings.length);
		result[0] = new String[] { "some", SecManager.class.getName() };
		result[1] = new String[] { "optional", SecurityManager.class.getName() };
		result[result.length - 1] = new String[] { "1", Thread.class.getName() };
		result[result.length - 2] = new String[] { "some",
				GRDDL.class.getName() };
		return result;
	}

	private static final Set<String> allowedNewInstance = new HashSet<String>(
			Arrays.asList(new String[] {
					"org.apache.xerces.impl.dv.ObjectFactory",
					SystemFunction.class.getName(),
					SystemFunctionLibrary.class.getName(),
					Configuration.class.getName(),
					"javax.xml.parsers.FactoryFinder",
					TypeHierarchy.class.getName(), }));

	private static String[][] concat(String[][] a, String[][] b) {
		String rslt[][] = new String[a.length + b.length][];
		System.arraycopy(a, 0, rslt, 0, a.length);
		System.arraycopy(b, 0, rslt, a.length, b.length);
		return rslt;
	}
}

/*
 * java.net.ResponseCache 5: sun.net.www.protocol.http.HttpURLConnection$3 6:
 * sun.net.www.protocol.http.HttpURLConnection 7:
 * sun.net.www.protocol.http.Handler 8: sun.net.www.protocol.http.Handler 9:
 * java.net.URL 10: org.apache.xerces.impl.XMLEntityManager 11:
 * org.apache.xerces.impl.XMLEntityManager 12:
 * org.apache.xerces.impl.XMLEntityManager 13:
 * org.apache.xerces.impl.XMLDTDScannerImpl 14:
 * org.apache.xerces.impl.XMLDocumentScannerImpl$DTDDispatcher 15:
 * org.apache.xerces.impl.XMLDocumentFragmentScannerImpl 16:
 * org.apache.xerces.parsers.XML11Configuration 17:
 * org.apache.xerces.parsers.XML11Configuration 18:
 * org.apache.xerces.parsers.XMLParser 19:
 * org.apache.xerces.parsers.AbstractSAXParser 20: net.sf.saxon.event.Sender 21:
 * net.sf.saxon.event.Sender 22: net.sf.saxon.event.Sender 23:
 * net.sf.saxon.Controller 24: com.hp.hpl.jena.grddl.impl.GRDDL$1 25:
 * com.hp.hpl.jena.grddl.impl.GRDDL$2 26: java.lang.Thread
 */
