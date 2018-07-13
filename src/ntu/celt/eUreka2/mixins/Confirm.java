// From http://wiki.apache.org/tapestry/Tapestry5AndJavaScriptExplained

package ntu.celt.eUreka2.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;


@Import(library="confirm.js")
public class Confirm {

	@Parameter(value = "Are you sure?", defaultPrefix = BindingConstants.LITERAL)
	private String message;

	@Inject
	private JavaScriptSupport javascriptSupport;

	@InjectContainer
	private ClientElement element;

	@AfterRender
	public void afterRender() {
		javascriptSupport.addScript(String.format("new Confirm('%s', '%s');", element.getClientId(), message));
	}

}
