/*
 * Copyright 2019 Chris Cromer
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package cl.cromer.game.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class handles formatting the log into a html table
 */
public class HtmlFormatter extends Formatter {
	/**
	 * Create a log row in the html table
	 *
	 * @param logRecord The record to insert into the table
	 * @return Returns the log row in html
	 */
	public String format(LogRecord logRecord) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\t<tr>\n");

		// colorize any levels >= WARNING in red
		if (logRecord.getLevel().intValue() >= Level.WARNING.intValue()) {
			stringBuilder.append("\t\t<td style=\"color:red\">");
			stringBuilder.append("<b>");
			stringBuilder.append(StringUtils.encodeHtml(logRecord.getLevel().getName()));
			stringBuilder.append("</b>");
		}
		else {
			stringBuilder.append("\t\t<td>");
			stringBuilder.append(StringUtils.encodeHtml(logRecord.getLevel().getName()));
		}
		stringBuilder.append("</td>\n");

		stringBuilder.append("\t\t<td>");
		stringBuilder.append(StringUtils.encodeHtml(calculateDate(logRecord.getMillis())));
		stringBuilder.append("</td>\n");

		stringBuilder.append("\t\t<td>");
		stringBuilder.append(StringUtils.encodeHtml(logRecord.getSourceClassName()));
		stringBuilder.append("</td>\n");

		stringBuilder.append("\t\t<td>");
		stringBuilder.append(StringUtils.encodeHtml(logRecord.getSourceMethodName()));
		stringBuilder.append("</td>\n");

		stringBuilder.append("\t\t<td>");
		stringBuilder.append(StringUtils.encodeHtml(formatMessage(logRecord)));
		stringBuilder.append("</td>\n");

		stringBuilder.append("\t</tr>\n");

		return stringBuilder.toString();
	}

	/**
	 * Calculate the date and time for the log row
	 *
	 * @param milliseconds The time the log record happened in milliseconds
	 * @return Returns the formatted time
	 */
	private String calculateDate(long milliseconds) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		Date resultDate = new Date(milliseconds);
		return dateFormat.format(resultDate);
	}

	/**
	 * This method is called to get the head of the log
	 *
	 * @param handler The handler
	 * @return Returns the header of the log
	 */
	public String getHead(Handler handler) {
		return "<!DOCTYPE html>\n<head>\n<style>\n"
				+ "table { width: 100%; border: 1px solid black; border-collapse: collapse; }\n"
				+ "th { font:bold 10pt monospaced; border: 1px solid black; border-collapse: collapse; }\n"
				+ "td { font:normal 10pt monospaced; border: 1px solid black; border-collapse: collapse; }\n"
				+ "h1 {font:normal 11pt monospaced;}\n"
				+ "</style>\n"
				+ "<title>Log</title>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<h1>" + (StringUtils.encodeHtml(new Date().toString())) + "</h1>\n"
				+ "<table border=\"0\" cellpadding=\"5\" cellspacing=\"3\">\n"
				+ "\t<tr align=\"left\">\n"
				+ "\t\t<th style=\"width:10%\">Loglevel</th>\n"
				+ "\t\t<th style=\"width:15%\">Time</th>\n"
				+ "\t\t<th style=\"width:10%\">Class</th>\n"
				+ "\t\t<th style=\"width:10%\">Method</th>\n"
				+ "\t\t<th style=\"width:55%\">Log Message</th>\n"
				+ "\t</tr>\n";
	}

	/**
	 * This method is called to get the tail of the log
	 *
	 * @param handler The handler
	 * @return Returns the tail of the log
	 */
	public String getTail(Handler handler) {
		return "</table>\n</body>\n</html>";
	}
}