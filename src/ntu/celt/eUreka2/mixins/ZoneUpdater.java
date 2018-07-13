package ntu.celt.eUreka2.mixins;

/**
 * A simple mixin for attaching javascript that updates a zone on any client-side event.
 * Based on http://tinybits.blogspot.com/2010/03/new-and-better-zoneupdater.html
 */


import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/* This annotation tells Tapestry to declare the js file in the page so that the browser will pull it in. */
@Import(library="zone_updater.js")
public class ZoneUpdater {

	// Parameters

	/**
	 * The event to listen for on the client. If not specified, zone update can only be triggered manually through
	 * calling updateZone on the JS object.
	 */
	@Parameter(defaultPrefix = BindingConstants.LITERAL)
	private String clientEvent;

	/**
	 * The event to listen for in your component class
	 */
	@Parameter(defaultPrefix = BindingConstants.LITERAL, required = true)
	private String event1;

	@Parameter(defaultPrefix = BindingConstants.LITERAL, value = "default")
	private String prefix;

	@Parameter
	private Object[] context;
	
	@Parameter(defaultPrefix = BindingConstants.LITERAL)
	private String param1Id;
	@Parameter(defaultPrefix = BindingConstants.LITERAL)
	private String param2Id;


	/**
	 * The zone to be updated by us.
	 */
	@Parameter(defaultPrefix = BindingConstants.LITERAL, required = true)
	private String zone1;  //original is 'zone' but somehow it conflict when use with t:select, so renamed it to 'zone1'


	@Inject
	private ComponentResources resources;

	@Environmental
	private JavaScriptSupport javascriptSupport;

	/**
	 * The element we attach ourselves to
	 */
	@InjectContainer
	private ClientElement element;
	
	// The code

	void afterRender() {
		String url = resources.createEventLink(event1, context).toAbsoluteURI();
		String elementId = element.getClientId();
		JSONObject spec = new JSONObject();
		spec.put("url", url);
		spec.put("elementId", elementId);
		spec.put("event", clientEvent);
		spec.put("zone", zone1);
		spec.put("param1Id", param1Id);
		spec.put("param2Id", param2Id);
		
		
		// Tell Tapestry to add some javascript that instantiates a ZoneUpdater for the element we're mixing into.
		// Tapestry will put it at the end of the page in a section that runs once the DOM has been loaded.
		// The ZoneUpdater class it refers to is NOT THIS class - it is actually the one defined in zone_updater.js.
		javascriptSupport.addScript(" %sZoneUpdater = new ZoneUpdater(%s); ", prefix, spec.toString());
		
	}
}

