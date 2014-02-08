package net.techbrew.journeymap;

import net.techbrew.journeymap.io.PropertyManager;
import net.techbrew.journeymap.log.LogFormatter;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

public class VersionCheck {

	private static volatile Boolean updateCheckEnabled = PropertyManager.getInstance().getBoolean(PropertyManager.Key.UPDATE_CHECK_ENABLED);
	private static volatile Boolean versionIsCurrent = true;
	private static volatile Boolean versionIsChecked;
	private static volatile String versionAvailable;

	public static Boolean getVersionIsCurrent() {
		if(versionIsChecked==null) {
			checkVersion();
		}
		return versionIsCurrent;
	}

	public static Boolean getVersionIsChecked() {
		if (versionIsChecked == null) {
			checkVersion();
		}
		return versionIsChecked;
	}
	
	public static String getVersionAvailable() {
		if(versionIsChecked==null) {
			checkVersion();
		}
		return versionAvailable;
	}

	private static synchronized void checkVersion() {
		
		if(updateCheckEnabled) {
			JourneyMap.getLogger().info("Checking for updated version: " + JourneyMap.VERSION_URL); //$NON-NLS-1$
			BufferedReader in;
			try {
				URL uri = URI.create(JourneyMap.VERSION_URL).toURL();
				HttpsURLConnection connection = (HttpsURLConnection) uri.openConnection();
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(3000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Referer", "http://journeymap.techbrew.net/?client=" + JourneyMap.JM_VERSION); //$NON-NLS-1$ //$NON-NLS-2$
				connection.setRequestProperty("Host", "localhost");
				connection.setRequestProperty("User-Agent", "Java/" + System.getProperty("java.version"));
				in = new BufferedReader(new InputStreamReader(uri.openStream()));
				StringBuffer sb = new StringBuffer();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					Boolean done = checkLine(inputLine);
					if(done!=null && done) break;
				}
				in.close();
				JourneyMap.getLogger().info("Version available online: " + versionAvailable); //$NON-NLS-1$
			} catch (Throwable e) {
				JourneyMap.getLogger().log(Level.SEVERE, "Could not check version URL", e); //$NON-NLS-1$
				versionIsChecked = false;
				versionIsCurrent = true;
				versionAvailable = "0"; //$NON-NLS-1$
				updateCheckEnabled = false;
			}
		} else {
			JourneyMap.getLogger().info("Update check disabled in properties file."); //$NON-NLS-1$
			versionIsChecked = false;
			versionIsCurrent = true;
			versionAvailable = "0"; //$NON-NLS-1$
		}
	}
	
	private static Boolean checkLine(String line) {
		if(line.startsWith("//")) { //$NON-NLS-1$
			if(line.contains("JM=")) { //$NON-NLS-1$
				String version = line.split("JM=")[1]; //$NON-NLS-1$
				if(version!=null) {
					versionIsChecked = true;
					versionAvailable = version;
					if(version.compareTo(JourneyMap.JM_VERSION)>0) {
						versionIsCurrent = false;
					}
				}
			}
		}
		return versionIsChecked;
	}
	
	public static void launchWebsite() {
		String url = JourneyMap.WEBSITE_URL;
		try {
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (Throwable e) {
			JourneyMap.getLogger().log(Level.SEVERE, "Could not launch browser with URL: " + url, LogFormatter.toString(e)); //$NON-NLS-1$
		}
	}
}
