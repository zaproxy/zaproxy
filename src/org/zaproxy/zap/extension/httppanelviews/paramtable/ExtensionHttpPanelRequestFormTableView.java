package org.zaproxy.zap.extension.httppanelviews.paramtable;

import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JComboBox;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.network.HtmlParameter;
import org.zaproxy.zap.extension.httppanel.component.split.request.RequestSplitComponent;
import org.zaproxy.zap.extension.httppanel.view.DefaultHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelViewModel;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;

public class ExtensionHttpPanelRequestFormTableView extends ExtensionAdaptor {

	public static final String NAME = "ExtensionHttpPanelRequestFormTableView";
	
	public ExtensionHttpPanelRequestFormTableView() {
		super(NAME);
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
		
		HttpPanelManager.getInstance().addRequestView(RequestSplitComponent.NAME, new RequestSplitBodyViewFactory());
	}
	
	private static final class RequestSplitBodyViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new HttpPanelFormParamTableView(new DefaultHttpPanelViewModel(), new HttpPanelFormParamTableModel());
		}

		@Override
		public Object getOptions() {
			return RequestSplitComponent.ViewComponent.BODY;
		}
	}

	private static class HttpPanelFormParamTableView extends HttpPanelParamTableView {

		public HttpPanelFormParamTableView(HttpPanelViewModel model, HttpPanelParamTableModel tableModel) {
			super(model, tableModel);
		}

		@Override
		public JComboBox getComboBoxTypes() {
			JComboBox comboBoxTypes = new JComboBox();
			
			comboBoxTypes.addItem(HtmlParameter.Type.form);
			
			return comboBoxTypes;
		}
	}
	
	private static class HttpPanelFormParamTableModel extends HttpPanelParamTableModel {

		private static final long serialVersionUID = -251253954319359635L;

		@Override
		protected void loadAllParams() {
			allParams.addAll(httpMessage.getFormParams());
		}

		@Override
		public void saveAllParams() {
			TreeSet<HtmlParameter> form = new TreeSet<HtmlParameter>();
			
			Iterator<HtmlParameter> it = allParams.iterator();
			while (it.hasNext()) {
				HtmlParameter htmlParameter = it.next();
				if(!htmlParameter.getName().isEmpty()) {
					switch (htmlParameter.getType()) {
					case url:
						break;
					case cookie:
						break;
					case form:
						form.add(htmlParameter);
						break;
					default:
						break;
					}
				}
			}
			
			httpMessage.setFormParams(form);
		}

		@Override
		protected HtmlParameter getDefaultHtmlParameter() {
			return new HtmlParameter(HtmlParameter.Type.form, "", "");
		}
	}
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
}
