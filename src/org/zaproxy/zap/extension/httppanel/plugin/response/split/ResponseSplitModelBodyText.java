package org.zaproxy.zap.extension.httppanel.plugin.response.split;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.text.HttpPanelTextModelInterface;

public class ResponseSplitModelBodyText extends HttpPanelTextModelInterface {

	public ResponseSplitModelBodyText(HttpMessage httpMessage) {
		setHttpMessage(httpMessage);
	}
	
	@Override
	public String getData() {
		if (HttpHeader.GZIP.equals(this.getHttpMessage().getResponseHeader().getHeader(HttpHeader.CONTENT_ENCODING))) {
			// Uncompress gziped content
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(this.getHttpMessage().getResponseBody().getBytes());
				GZIPInputStream gis = new GZIPInputStream(bais);
				InputStreamReader isr = new InputStreamReader(gis);
				BufferedReader br = new BufferedReader(isr);
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				isr.close();
				gis.close();
				bais.close();
				return sb.toString();
			} catch (IOException e) {
				//this.log.error(e.getMessage(), e);
				System.out.println(e);
			}
		}
		
		return httpMessage.getResponseBody().toString();
	}

	@Override
	public void setData(String data) {
		httpMessage.getResponseBody().setBody(data);
	}
}
