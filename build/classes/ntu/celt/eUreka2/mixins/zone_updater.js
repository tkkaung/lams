// A class that updates a zone on any client-side event.
// Based on http://tinybits.blogspot.com/2010/03/new-and-better-zoneupdater.html
// 

function addParameter(name, value, url) {
	if (url.indexOf('?') > -1) {
		url += '&';
	} else {
		url += '?'
	}
	value = escape(value);
	url += name + '=' + value;
	return url;
}

var ZoneUpdater = Class.create({
	altElement : {},
	initialize : function(spec) {
		this.element = $(spec.elementId);
		this.param1 = $(spec.param1Id);
		this.param2 = $(spec.param2Id);
		
		this.url = spec.url;
		$T(this.element).zoneId = spec.zone;
		if (spec.event) {
			this.event = spec.event;
			this.element.observe(this.event, this.updateZone
					.bindAsEventListener(this));
			
		}
	},
	
	updateZone : function() {
		var zoneManager = Tapestry.findZoneManager(this.element);
		
		if (!zoneManager) {
			return;
		}

		var updatedUrl = this.url;
		
		if (this.element.value) {
			var param = this.element.value;
			if (param) {
				updatedUrl = addParameter('param', param, updatedUrl);
			}
		}
		if (this.param1) {
			if (this.param1.value) {
				updatedUrl = addParameter('param1', this.param1.value, updatedUrl);
			}
		}
		if (this.param2) {
			if (this.param2.value) {
				updatedUrl = addParameter('param2', this.param2.value, updatedUrl);
			}
		}
		
		zoneManager.updateFromURL(updatedUrl);
	}
	
	
});
