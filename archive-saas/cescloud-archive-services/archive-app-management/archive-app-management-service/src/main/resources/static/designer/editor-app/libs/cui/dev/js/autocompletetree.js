$.component( "coral.autocompletetree", $.coral.autocomplete, {
	requestIndex: 0,
	pending: 0,
	options: {
		allowPushParent: false,
		rootNode: false,
		multiLineLabel: false,
		radioType: "level",
		showRootNode: true,
		multiple : false,
        triggers: null, // 覆盖 validate 里的 triggers
        excluded: false // true 则不单独校验
	},
	_create: function() {
		this.options.render = "tree";
		this._super();
	},
	_filter: function( array, request, response  ){
		$( this.menu.element ).tree("reload", array);
		$( this.menu.element ).tree("filterNodesByParam", {"name": request.term} );
		response( array );
	},
	_initData: function(){
		var that = this;
		this.menu = {};
		var u = $( "<ul>" )[0];
		$( u )
		.uniqueId()
		.addClass( "coral-autocomplete-panel coral-front" )
		.appendTo( this._appendTo() )
		.tree({
			showLine: false,
			showIcon: true,
			radioType: this.options.radioType,
			showRootNode:this.options.showRootNode,
			rootNode:this.options.rootNode,
			componentCls: "coral-autocomplete-tree",
			/*addDiyDom : function (treeId, treeNode) {
				var spaceWidth = 15;
				var switchObj = $("#" + treeNode.tId + "_switch"), icoObj = $("#" + treeNode.tId + "_ico");
				icoObj.before(switchObj);

				if (treeNode.level > 0) {
					var spaceStr = "<span style='display: inline-block;width:"
							+ (spaceWidth * treeNode.level) + "px'></span>";
					switchObj.before(spaceStr);
				}
			},*/
			beforeClick : function ( treeId, treeNode ) {
				if ( !that.options.allowPushParent && treeNode.isParent ) {
					return false;
				} 
			},
			onClick: function( event, treeId, treeNode ){
				var item = treeNode,
					previous = that.previous;
				// only trigger when focus was lost (click on menu)
				if ( that.element[ 0 ] !== that.document[ 0 ].activeElement ) {
					that.element.focus();
					that.previous = previous;
					// #6109 - IE triggers two focus events and the second
					// is asynchronous, so we need to reset the previous
					// term synchronously and asynchronously :-(
					that._delay(function() {
						that.previous = previous;
						that.selectedItem = item;
					});
				}

				if ( false !== that._trigger( "beforeNodeClick", event, { item: item ,value: that.getValue() } ) ) {
					that.selectedItem = item;
					that.selectedItems.push( item );
					var value = that.getValue(),
					text = that.getText(),
					vv = value==""?[]:value.split(that.options.separator),
					vt = text==""?[]:text.split(that.options.separator);
					if ( that.options.multiple ) {
						if ( !that.options.allowRepeat && $.inArray(treeNode.id , vv) > -1 ) {
							return ;
						}
						vv.push( treeNode.id );
						vt.push( treeNode.name );
					} else {
						vv = [ treeNode.id ];
						vt = [ treeNode.name ];
					}
					that.setText( vt.join(that.options.separator) );
					that.setValue( vv.join(that.options.separator), true );
				}
				that._trigger( "onNodeClick", event, { item: item ,value: that.getValue() } );
				// reset the term after the select event
				// this allows custom select handling to work properly
				//that.term = that._text();
				that.term = that._text();
				that.close( event );
			}
		})
		.hide();
		this.menu.element = $( u );
		if ( isNaN( this.options.panelHeight ) ) {
			$(".coral-autocomplete-panel").css( {
				"max-height": this.options.maxPanelHeight +"px"
			} );
		}
	}
});