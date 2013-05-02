package net.techbrew.mcjm.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
	 
	private static final MessageFormat messageFormat = new MessageFormat("{0,date,hh:mm:ss} {1} [{2}]: {3}.{4}() {5}\n"); //$NON-NLS-1$
	
	public LogFormatter() {
		super();
	}
	
	@Override public String format(LogRecord record) {
		Object[] arguments = new Object[6];
		int i = 0;
		arguments[i++] = new Date(record.getMillis());
		arguments[i++] = record.getLevel();
		arguments[i++] = Thread.currentThread().getName();
		arguments[i++] = record.getSourceClassName().replaceFirst("net.techbrew.mcjm.","");
		arguments[i++] = record.getSourceMethodName();
		arguments[i++] = record.getMessage();
		return messageFormat.format(arguments);
	}	
	
	public static String toString(Throwable t) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		t.printStackTrace(ps);
		ps.flush();
		return baos.toString();
	}
 
}