//>>label: Accordion
//>>group: Component
//>>description: Displays collapsible content panels for presenting information in a limited amount of space.
//>>docs: 
//>>demos: 
//>>css.structure: ../themes/base/core.css
//>>css.structure: ../themes/base/accordion.css
//>>css.theme: ../themes/base/theme.css
$.component( "coral.view", {
	version: "@version",
	options: {
		controllerName: null,
		heightStyle: "auto"
	},
	
	_create: function(e, ui) {
		var that = this;
		var options = this.options;
		this.element.addClass( "coral-view coral-helper-reset" );
		$.controller(this.options.controllerName, {
			view: this.element,
			controllerName: this.options.controllerName
		});
		var c = $.controller(that.options.controllerName);
		$.parseDone({
			fun: c.onInit,
			context: c,
			args: [e, ui]
		});
		this._refresh();
	},
	_destroy: function() {
	},
	_setOption: function(key, value) {
		this._super( key, value );
	},
	refresh: function() {
		this._refresh();
	},
	_refresh: function() {
		$.coral.refreshChild(this.element);
	}
});