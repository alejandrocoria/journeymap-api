
var mapScale = 4;
var minMapScale = 1;
var maxMapScale = 15;
var smoothScale = false;
var mapBounds = {x1:0,z1:0,x2:0,z2:0};

var showLight = false;
var showCaves = true;
var centerOnPlayer = true;

var showAnimals = true;
var showPets = true;
var showMobs = true;
var showVillagers = true;
var showPlayers = true;

var mapBackground = "#222";

var canvas;
var bgCanvas;
var fgCanvas;

var userPanning=false;
var mx,my;
var msx,msy;

var tempMapImage;
var latestMapImage;
var playerImage;

var mobImages = new Object();
var otherImages = new Array();
var chunkScale = mapScale*16;

var JmIcon;
var halted = false;
var uiInitialized = false;
var versionChecked = false;
var immediateUpdateMap = false;
var updatingMap = false;
var drawingMap = false;

var timers = {};

var playerOverrideMap = false;

var JM = {
	debug: false,
	messages: null,
	game: null,
    mobs:[],
    animals:[],
    players:[],
    villagers:[]
};

/**
 * JQuery add-on for disableSelection
 */
(function($){
    $.fn.disableSelection = function() {
        return this
	         .attr('unselectable', 'on')
	         .css('user-select', 'none')
	         .on('selectstart', false); // ie
    };
})(jQuery);


/** Debug helper **/
var logEntry = function(name) {
	if(JM.debug) {
		console.log(">>> " + name);
	}
}

/** Delay helper **/
var delay = (function(){
	  var timer = 0;
	  return function(callback, ms){
	    clearTimeout (timer);
	    timer = setTimeout(callback, ms);
	  };
	})();


/**
 * Load I18N messages once.
 * @returns
 */
var initMessages = function() {
	logEntry("initMessages");	
	
    $.ajax({
        url: "/data/messages", 
        dataType: "jsonp",
        contentType: "application/javascript; charset=utf-8",
        async: false})
    .fail(handleError)
	.done(function(data, textStatus, jqXHR) { 
		JM.messages = data;
		initUI();
	});
}

/**
 * Initialize UI once.
 * @returns
 */
var initUI = function() {
	logEntry("initUI");
	
	// Ensure messages are loaded first.
	if(!JM.messages) {
		logEntry("initUI called without JM.messages"); // shouldn't happen
		loadMessages();
		return;
	}
	
    // Init canvas
    $("#mapCanvas").offset({ top: 0, left: 0})
    	.mousedown(myDown)
    	.mouseup(myUp)
    	.dblclick(myDblClick)
    	.disableSelection();
        
    // Init canvases
    canvas = $("#mapCanvas")[0];
    bgCanvas = $(document.createElement("canvas")).attr('id','bgCanvas')[0];
    fgCanvas = $(document.createElement("canvas")).attr('id','fgCanvas')[0];
    
    sizeMap();
        
    // Set page language, although at this point it may be too late to matter.
    $('html').attr('lang', JM.messages.locale.split('_')[0]);
    
    // Set RSS feed title
    $("link #rssfeed").attr("title", JM.messages.rss_feed_title);
    
    // Init toolbar button tooltips
    $("#dayButton").html(JM.messages.day_button_title)
    	.attr("title", JM.messages.day_button_desc)
	    .click(function() {
	       playerOverrideMap = true;
	 	   setMapType('day');
	    });
    
    $("#nightButton").html(JM.messages.night_button_title)
    	.attr("title", JM.messages.night_button_desc)
	    .click(function() {
	       playerOverrideMap = true;
	 	   setMapType('night');
	    });
    
    $("#followButton").html(JM.messages.follow_button_title)
    	.attr("title", JM.messages.follow_button_desc)
	    .click(function() {
	 	   setCenterOnPlayer(!centerOnPlayer);
	 	   updateMap();
	    });
    
    $("#caveButton").html(JM.messages.cave_button_title)
    	.attr("title", JM.messages.cave_button_desc)
	    .click(function() {
	 	   setShowCaves(!showCaves);
	    });
    
    $("#monstersButton").attr("title", "<b>" + JM.messages.monsters_button_title + "</b><br/>" + JM.messages.monsters_button_desc);
    
    $("#saveButton").attr("title", JM.messages.save_button_title)
	    .click(function() {
	    	saveMapImage();
	     });
    
    $("#aboutButton").attr("title", JM.messages.about_button_title);
    
    $("#rssLink").attr("title", JM.messages.rss_feed_desc);
    $("#rssLinkText").html(JM.messages.rss_feed_title);
    
    $("#emailLink").attr("title", JM.messages.email_sub_desc);
    $("#emailLinkText").html(JM.messages.email_sub_title);
    
    $("#twitterLink").attr("title", JM.messages.follow_twitter);
    $("#twitterLinkText").html(JM.messages.follow_twitter);
    
    // Tooltip for slider
    var tooltip = $('<div id="slider-tooltip" />').css({
    	width: '1em',
    	textAlign:'center',
        position: 'absolute',
        top: 0,
        left: 0
    }).hide();
    
    // Slider
    $("#slider-vertical" ).slider({
        orientation: "vertical",
        range: "min",
        title: JM.messages.zoom_slider_name,
        min: minMapScale,
        max: maxMapScale,
        value: mapScale,
        slide: function( event, ui ) {
        	tooltip.text(ui.value);
            setZoom(ui.value);
        }
    }).find(".ui-slider-handle")
    	.append(tooltip).hover(
    		function() { tooltip.show() }, 
    		function() { tooltip.hide() }
    );
    
    $("#worldInfo").hide();
    $("#worldNameTitle").html(JM.messages.worldname_title);
    $("#worldTimeTitle").html(JM.messages.worldtime_title);
    $("#playerBiomeTitle").html(JM.messages.biome_title);
    $("#playerLocationTitle").html(JM.messages.location_title);
    $("#playerElevationTitle").html(JM.messages.elevation_title);
    
    $("#checkSmoothScale").prop('checked', smoothScale)
    .click(function() {
    	smoothScale = (this.checked===true);    	
    	drawMap();
    });
    
    $("#checkShowAnimals").prop('checked', showAnimals)
    $("#checkShowAnimals").click(function() {
    	showAnimals = (this.checked===true);    	
    	drawMap();
    });
    
    $("#checkShowPets").prop('checked', showPets)
    $("#checkShowPets").click(function() {
    	showPets = (this.checked===true);    	
    	drawMap();
    });
    
    $("#checkShowMobs").prop('checked', showMobs)
    $("#checkShowMobs").click(function() {
    	showMobs = (this.checked===true);    	
    	drawMap();
    });
    
    $("#checkShowVillagers").prop('checked', showVillagers)
    $("#checkShowVillagers").click(function() {
    	showVillagers = (this.checked===true);    	
    	drawMap();
    });
    
    $("#checkShowPlayers").prop('checked', showPlayers)
    $("#checkShowPlayers").click(function() {
    	showPlayers = (this.checked===true);    	
    	drawMap();
    });
    
    // Init images
    initImages();
    
	// Disable selection events by default
	$("*").live('selectstart dragstart', function(evt){ evt.preventDefault(); return false; });
   
	// Mouse events for canvas
	$(window).mousewheel(myMouseWheel);
	
	// Keyboard events
	$(document).keypress(myKeyPress);
	
	// Get game data
	$.ajax({url: "/data/game", dataType: "jsonp"})
	.fail(handleError)
	.done(function(data, textStatus, jqXHR) { 		
		
		JM.game = data;			
		
		logEntry("initUI() -> /data/game");
		
		// Update UI with game info
		$("#version").html(JM.game.jm_version);
		if(JM.game.latest_journeymap_version>JM.game.jm_version) {
			// TODO: This is sloppy L10N
			$("#versionButton").attr("title", JM.messages.update_available + " : JourneyMap " + JM.game.latest_journeymap_version + " for Minecraft " + JM.world.latest_minecraft_version);
			$("#versionButton").css("visibility", "visible");
		}
	   
		// GA event
		if(versionChecked!=true) {
			_gaq.push(['_setCustomVar', 1, 'jm_version', JM.game.jm_version, 2]);
			_gaq.push(['_trackEvent', 'Client', 'CheckVersion', JM.game.jm_version]);
			versionChecked = true;
		}
			
		// Set flag so this function doesn't get called twice
		uiInitialized = true;
			
		// Continue with initialization
		initWorld();
   	});
	
}

	
/**
 * Initialize World.
 * @returns
 */
var initWorld = function() {
	
	logEntry("initWorld");
	
	logEntry("Initializing world...");
    
    // Clear existing timers (if any)
	clearTimers();
	
	// Reset state
	halted = false;
	updatingMap = false;
	setCenterOnPlayer(true);
	
	// Ensure the map is sized
	sizeMap();
	
	// Get world data once
	refreshWorldData(function(){
		
		// Update the player data and map first
		refreshPlayerData(function(){
			updateMap();
			refreshPlayerData(); // auto refresh
		});
		
		// Let the other data auto-refresh
		refreshMobsData();
		refreshPlayersData();
		refreshAnimalsData();
		refreshVillagersData();
		refreshWorldData(); 
	});
	
	// Turn on UI elements that may have been hidden
	$(".jmtoggle").each(function(){$(this).show()});
	$("#slider-vertical").show();
	 
	// Resize events can now update the map
	$(window).resize(function() {
		$("#playerImage").hide();
	    delay(function(){
			sizeMap();
	    	updateMap();
	    }, 200);
	});
		
}

/**
 * Preload any images as needed.
 */
var initImages = function() {
	
	logEntry("initImages");

	// Init player marker
	if(!playerImage) {
		playerImage=$(document.createElement('img'))
			.attr("id", "playerImage")
			.attr("src", "/img/locator-blue.png")
			.css("position", "absolute")
			.css("height", "64px")
			.css("width", "64px")
			.css("cursor", "pointer")
			.click(function(){
				if(centerOnPlayer===false) {
			        setCenterOnPlayer(true);
			        updateMap();
				}
			})
			.appendTo('body');
	}

}

/**
 * Invoke saving map file
 */
var saveMapImage = function() {
    document.location = getMapDataUrl().replace("/map.png", "/save");
}

/**
 * Clear existing timers
 */
var clearTimers = function() {
	
	logEntry("clearTimers");
	
	// Clear existing timers (if any)
	if(timers.ids && timers.ids.length) {
		$.each(timers.ids, function(index, id) { 
			clearTimeout(id);
		});
	}
	
	timers = {};
}


/**
 * Add a named timeout timer to the timers array, minimum being 500.
 * If there is a previous timeout already in place, the later one
 * replaces it.
 */
var addTimer = function(name, func, timespan) {
	
	logEntry("addTimer: " + name + " = " + timespan);
	
	if(halted===true) return;
	
	if(!timers.ids) timers.ids = [];
	
	if(timers[name]) {
		logEntry("!! overriding timer: " + name);
		clearTimeout(timers[name]);
		delete timers[name];
	}
	
	var wrapper = function() {
		delete timers[name];
		func();
	}
	
	var timerId = setTimeout(wrapper, Math.max(timespan, 500));
	timers.ids.push(timerId);
	timers[name] = timerId;
}

/**
 * Set map scale
 */
var setScale = function(newScale) {
   mapScale = newScale;
   chunkScale = mapScale*16;
}

/**
 * Ensure canvas is sized as needed
 */
var sizeMap = function() {
	
   logEntry("sizeMap");
	
   // Update canvas size   
   setCanvasWidth($(window).width());
   setCanvasHeight($(window).height());

}

var getCanvasWidth = function() {
	return $(canvas).attr('width');
}

var setCanvasWidth = function(width) {
	$(canvas).attr('width', width);
}

var getCanvasHeight = function() {
	return $(canvas).attr('height');
}

var setCanvasHeight = function(height) {
	$(canvas).attr('height', height);
}

var centerMapOnPlayer = function() {
	if(JM.player) {
		centerMapOnChunk(Math.round(JM.player.chunkCoordX), Math.round(JM.player.chunkCoordZ));
	}
}

function centerMapOnChunk(chunkX, chunkZ) {
 
   var maxChunksWide = Math.ceil(getCanvasWidth()/mapScale/16);
   var maxChunksHigh = Math.ceil(getCanvasHeight()/mapScale/16);  
   
   mapBounds.x1 = chunkX - Math.round(maxChunksWide/2) +1;
   mapBounds.z1 = chunkZ - Math.round(maxChunksHigh/2) +1;
   
   checkBounds();
}

function checkBounds() {
   // determine how many chunks we can display
   var maxChunksWide = Math.ceil(getCanvasWidth()/mapScale/16);
   var maxChunksHigh = Math.ceil(getCanvasHeight()/mapScale/16);
   mapBounds.x2 = mapBounds.x1 + maxChunksWide;
   mapBounds.z2 = mapBounds.z1 + maxChunksHigh;  
}

function setMapType(mapType) {
   
   if(mapType==="day") {
	  if(showLight===false) return;
      showLight = false;    
      $("#dayButton").addClass("active");
      $("#nightButton").removeClass("active");
      
   } else if(mapType==="night") {
	   if(showLight===true) return;
      showLight = true;
      $("#dayButton").removeClass("active");
      $("#nightButton").addClass("active");
   } else {
      logEntry("Error: Can't set mapType: " + mapType);
   }
   
   if(JM.player.underground!==true) {
	   updateMap();
   }

}

function setCenterOnPlayer(onPlayer) {
   
   centerOnPlayer = onPlayer;
   if(onPlayer===true) {
      centerMapOnPlayer();
      $("#followButton").addClass("active");
   } else {
      $("#followButton").removeClass("active");
   }
}

function setShowCaves(show) {
	
	if(show===showCaves) return;
	showCaves = show;
	
	if(showCaves===true) {
		$("#caveButton").addClass("active");
	} else {
		$("#caveButton").removeClass("active");
	}
	
    if(JM.player.underground===true) {
  	  updateMap();
    }
  
}

////////////// DATA ////////////////////

/**
 * Fetch JsonP data.  Generic error handling,
 * callback invoked on success
 */
var fetchData = function(dataUrl, callback) {

	logEntry("fetchData " + dataUrl);
	
	$.ajax({url: dataUrl, dataType: "jsonp"})
	.fail(handleError)
   	.done(callback);
}

var refreshPlayersData = function(callback) {

	if(JM.world && JM.world.singlePlayer===true) return;
	
	fetchData("/data/players", function(data){
		JM.players = data.players;	
   		addTimer("refreshPlayersData", refreshPlayersData, JM.game.browser_playersdata_poll);
   		if(callback) callback();
	});

}

var refreshMobsData = function(callback) {
	
	if(showMobs!==true) return;
	
	fetchData("/data/mobs", function(data){
   		JM.mobs = data.mobs;
   		addTimer("refreshMobsData", refreshMobsData, JM.game.browser_mobsdata_poll);		
   		if(callback) callback();
   	});
}

var refreshAnimalsData = function(callback) {
	
	if(showAnimals!==true && showPets!==true) return;
	
	fetchData("/data/animals?pets=" + showPets, function(data){
   		JM.animals = data.animals;
		addTimer("refreshAnimalsData", refreshAnimalsData, JM.game.browser_animalsdata_poll);	
		if(callback) callback();
   	});
}

var refreshVillagersData = function(callback) {
	
	if(showVillagers!==true) return;
	
	fetchData("/data/villagers", function(data){
   		JM.villagers = data.villagers;
   		addTimer("refreshVillagersData", refreshVillagersData, JM.game.browser_villagersdata_poll);
   		if(callback) callback();
   	});
}

/**
 * Refresh the world data.
 * 
 * @param callback Optional function to call when this is complete.
 */
var refreshWorldData = function(callback) {
	
	logEntry("refreshWorldData");

	fetchData("/data/world", function(data){
		
		JM.world = data;
		
		// Interpret data
		var dimensionName = "";
		if(JM.world.dimension===-1) {
			dimensionName = JM.messages.world_name_nether;
	    } else if(JM.world.dimension===1) {
	    	dimensionName = JM.messages.world_name_end;
	    }
		
		var timeCycleInfo = "";
		if(JM.world.dimension===0) {
	       if(JM.world.time<12000) {
	    	   timeInfo = JM.messages.sunset_begins;
	       } else if(JM.world.time<13800) {
	    	   timeInfo = JM.messages.night_begins;
	       } else if(JM.world.time<22200) {
	    	   timeInfo = JM.messages.sunrise_begins;
	       } else if(JM.world.time<23999) {
	    	   timeInfo = JM.messages.day_begins;
	       } 
	    }
	
		 
	   // 0 is the start of daytime, 12000 is the start of sunset, 13800 is the start of nighttime, 22200 is the start of sunrise, and 24000 is daytime again. 
	   var allsecs = JM.world.time/20;
	   var mins = Math.floor(allsecs / 60);
	   var secs = Math.ceil(allsecs % 60);
	   if(mins<10) mins = "0"+mins;
	   if(secs<10) secs = "0"+secs;
	   var currentTime = mins + ":" + secs;
	   	   
		// Update UI elements
	   	$("#worldName").html(unescape(JM.world.name).replace("\\+"," "));        		
		$("#worldTime").html(currentTime);		
		$("#worldInfo").show();

		// Set map based on time
		if(playerOverrideMap != true) {
			if(JM.world.dimension===0 && JM.player && JM.player.underground!=true) {
		       if(JM.world.time<13800) {
		    	   setMapType('day');
		       } else {
		    	   setMapType('night');
		       }
			}
		}

		// Do callback
		if(callback) {
        	callback();
        } else {
        	addTimer("refreshWorldData", refreshWorldData, JM.game.browser_mapimg_poll);
        }

	}); 
	
}

/**
 * Get the url for the current map state
 */
var getMapDataUrl = function() {
   var width = getCanvasWidth();
   var height = getCanvasHeight();
   var mapType = (JM.player && JM.player.underground===true && showCaves===true) ? "underground" : (showLight===true ? "night" : "day") ;  
   var depth = (JM.player && JM.player.chunkCoordY) ? JM.player.chunkCoordY : 4;
   var request = "/map.png?mapType=" + mapType + "&depth=" + depth + "&x1=" + mapBounds.x1+ "&z1=" + mapBounds.z1 + 
                             "&x2=" + mapBounds.x2 + "&z2=" + mapBounds.z2 + "&width=" + width + "&height=" + height;
   return request;
}

/**
 * Refresh the map image.
 * 
 * @param callback Optional function to call when this is complete.
 */
var refreshMapImage = function(callback) {
	
	logEntry("refreshMapImage");

	if(!tempMapImage) {
		tempMapImage = $(document.createElement('img'))
			.error(handleError); // TODO: check whether error will ever be called			
	}
	
	$(tempMapImage).load(function() {
        if (!this.complete || typeof this.naturalWidth == "undefined" || this.naturalWidth === 0) {
            logEntry('Map image incomplete!');
        } else {
        	latestMapImage = $(tempMapImage)[0];
        }             
        if(callback) {
        	callback();
        } 
    }).attr('src', getMapDataUrl());
		
	    
}


/**
 * Refresh the player data.
 * 
 * @param callback Optional function to call when this is complete.
 */
var refreshPlayerData = function(callback) {
	
	logEntry("refreshPlayerData");

	$.ajax({url: "/data/player", dataType: "jsonp"})
	.fail(handleError)
   	.done(function(data, textStatus, jqXHR) { 
		JM.player = data;
		
		// Update Player Image location
		drawPlayer();
		
   		// Update UI
		$("#playerBiome").html(JM.player.biome);
		$("#playerLocation").html(JM.player.posX + "," + JM.player.posZ);
		
		$("#playerElevationTitle").attr('title', JM.messages.elevation_title + " " + (JM.player.posY>>4));
		$("#playerElevation").html(JM.player.posY + "&nbsp;(" + (JM.player.posY>>4) + ")");				
				
		// Do callback
		if(callback) {
        	callback();
        } else {
        	addTimer("refreshPlayerData", refreshPlayerData, JM.game.browser_poll);
        }	
   	});

}



var updateMap = function() {  	
	
   logEntry("updateMap");
	
   if(userPanning===false && updatingMap===false) {
	   	   
		updatingMap = true;
		
		// Center map or check bounds to ensure correct map image retrieved		
		if(centerOnPlayer===true) {
			centerMapOnPlayer();
		} else {
			checkBounds();
		}
					
		// Retrieve the map image, then draw it with entities
		refreshMapImage(function() {
								
//			var start = new Date().getTime();
			var success = drawMap();
			updatingMap = false;
//			var stop = new Date().getTime();
//			if(success!==false) {
//				logEntry("Redrew map: " + (stop-start) + "ms");
//			}
			
			if(immediateUpdateMap===true) {
				immediateUpdateMap = false;
				updateMap();
			} else {
				addTimer("updateMap", updateMap, JM.game.browser_mapimg_poll);
			}
		});	
		
   } else if(updatingMap===true) {
	   immediateUpdateMap = true;
	   return false;
   }
}


// Ajax request got an error from the server
var handleError = function(data, error, jqXHR) {
	
	logEntry("handleError");
	
	clearTimers();
	
	// Secondary errors will be ignored
	if(halted===true) return;
	
	console.log("Server returned error: " + data.status + ": " + jqXHR);
	
	$(".jmtoggle").each(function(){$(this).hide()});
	$("#worldInfo").hide();
	$("#playerImage").hide();
	$("#slider-vertical").hide();
	
	var displayError;
	if(data.status===503 || data.status===0) {
		if(JM.messages.error_world_not_opened) {
			displayError = JM.messages.error_world_not_opened;
		} else if(data.statusText) {
			displayError = data.statusText;
		} else {
			displayError = "";
		}
	}	
	
    // Remove others
	$.each(otherImages, function(index, img) { 
		document.body.removeChild(img);
	});

	// Format UI
	$('body').css('backgroundColor',mapBackground); 
    sizeMap();
    
    var ctx = canvas.getContext("2d");
    
    if(!JmIcon) {
    	JmIcon = new Image();
    	JmIcon.onload=function() {
	        ctx.drawImage(JmIcon, getCanvasWidth()/2-72, getCanvasHeight()/2-160);
	        JmIcon.onload=null;
	    };
	    JmIcon.src="/ico/apple-touch-icon-144x144-precomposed.png";
	} else {
		ctx.drawImage(JmIcon, getCanvasWidth()/2-72, getCanvasHeight()/2-160);
    }
    
    ctx.globalAlpha=1;
    ctx.fillStyle = "red";
    ctx.font = "bold 16px Arial";
    ctx.textAlign="center";
    ctx.fillText(displayError, getCanvasWidth()/2, (getCanvasHeight()/2) + 10);       
	
    // Restart in 5 seconds
	if(!halted) {
		halted = true;
		logEntry("Will re-check game state in 5 seconds.");
		setTimeout(function(){
			
			halted = false;
			
			if(!JM.messages) {
				initMessages();
			} else if(uiInitialized!=true) {
				initUI();
			} else {
				initWorld();
			}
			
		},5000);
	}
}

//////////////DRAW ////////////////////


// Draw the map
var drawMap = function() {
	
   logEntry("drawMap");
   
   if(userPanning===true) {
	   logEntry("Can't draw while userPanning");
	   return false;
   }
   
   if(drawingMap===true) {
	   logEntry("Avoided concurrent drawMap()");
	   return false;
   }
   drawingMap = true;
   
   // Get canvas dimensions
   var canvasWidth = getCanvasWidth();
   var canvasHeight = getCanvasHeight();
   
   // Size working canvases
   $(bgCanvas).attr('width', canvasWidth).attr('height', canvasHeight);
   $(fgCanvas).attr('width', canvasWidth).attr('height', canvasHeight);
   
   // set background color
   if(showLight===true || JM.player.underground===true) {
	   mapBackground = '#000';
   } else {
	   mapBackground = '#222';
   }
   $('body').css('backgroundColor',mapBackground);  
   
   // Draw background map image
   drawBackgroundCanvas(canvasWidth, canvasHeight);
   
   // clear foreground canvas
   var ctx = fgCanvas.getContext("2d");
   ctx.clearRect(0,0,canvasWidth,canvasHeight);
   
   // mobs
   drawMobs(canvasWidth, canvasHeight);
   
   // other players
   if(JM.world.singlePlayer!=true) {
	   drawMultiplayers(canvasWidth, canvasHeight);
   }
      
   // Now put on the visible canvas
   ctx = canvas.getContext("2d");
   if(ctx.drawImage) {
	  ctx.globalAlpha=1.0;   
	  ctx.drawImage(bgCanvas, 0,0, canvasWidth, canvasHeight);
	  ctx.drawImage(fgCanvas, 0,0, canvasWidth, canvasHeight);
   }
   
   
   drawingMap = false;
   
   // Update player position
   drawPlayer();

}


// Draw the player icon
var drawPlayer = function(canvasWidth, canvasHeight, xOffset, zOffset) {
	
   logEntry("drawPlayer");
   
   if(drawingMap===true || userPanning===true) return;
   
   if(!canvasWidth || !canvasHeight) {
		canvasWidth = getCanvasWidth();
		canvasHeight = getCanvasHeight();
   }

   var player = JM.player;
   var x = getScaledChunkX(player.posX/16);
   var z = getScaledChunkZ(player.posZ/16);
   
   if(xOffset) x = x + xOffset;
   if(zOffset) z = z + zOffset;
   
   if(x>=0 &&
      x<=canvasWidth &&  
      z>=0 &&
      z<=canvasHeight) {     
       
	   // Get player location value
	   var loc = JM.player.posX + "," + JM.player.posZ + "  (" + JM.player.posY + ")"
	   
       // Update player image
	   var cursor = (centerOnPlayer===true) ? "default" : "pointer";
       var rotate = "rotate(" + player.heading + "deg)";
       $("#playerImage")
       		.attr('title', loc)
       		.css("left", x-32)
       		.css("top", z-32)
       		.css("zIndex", 2)
       		.css("cursor", cursor)
       		.css("-webkit-transform", rotate)
       		.css("-moz-transform", rotate)
       		.css("-o-transform", rotate)
       		.css("-ms-transform", rotate)
       		.css("transform", rotate)
       		.show();
     
   } else {
	   $("#playerImage").hide();
   }

}

// Draw the location of mobs
var drawMobs = function(canvasWidth, canvasHeight) {
	
	logEntry("drawMobs");
	
	if(!canvasWidth || !canvasHeight) {
    	canvasWidth = getCanvasWidth();
    	canvasHeight = getCanvasHeight();
    }
	
	var ctx = fgCanvas.getContext("2d");
	
	if(showMobs===true && JM.mobs) {
	    $.each(JM.mobs, function(index, mob) {
			drawEntity(ctx, mob, canvasWidth, canvasHeight, false);
		});
	}
    
    if((showAnimals===true || showPets===true) && JM.animals) {
	    $.each(JM.animals, function(index, mob) {
			drawEntity(ctx, mob, canvasWidth, canvasHeight, true);
		});
    }
    
    if(showVillagers===true && JM.villagers) {
	    $.each(JM.villagers, function(index, mob) {
			drawEntity(ctx, mob, canvasWidth, canvasHeight, true);
		});
    }
    
}

//Draw the location of an entity
var drawEntity = function(ctx, mob, canvasWidth, canvasHeight, friendly) {
	
   var x = getScaledChunkX(mob.posX/16);
   var z = getScaledChunkZ(mob.posZ/16);
   
   if(x>=0 &&
      x<=canvasWidth &&  
      z>=0 &&
      z<=canvasHeight) {
	      
	   if(friendly===true && mob.owner && mob.owner===JM.player.username) {
		   if(showPets===false) return;
		   ctx.strokeStyle = "#0000ff";
	   } else if(friendly===true) {
		   if(showAnimals===false && !(mob.type==='Villager')) return;
		   ctx.strokeStyle = "#cccccc";
	   } else {
		   ctx.strokeStyle = "#ff0000";		   
	   }
	   
       ctx.globalAlpha=.85;
       ctx.lineWidth = 2;
       ctx.beginPath();
       var radius = 16;
       var type = mob.type; 
       if(type==='Ghast' || type==='Dragon' || type==='Wither') {
    	   radius = 24;
       } 
       ctx.arc(x, z, radius, 0, Math.PI*2, true); 	   
	   ctx.stroke();
       ctx.globalAlpha=1.0;
       
       // Get pre-loaded image, or lazy-load as needed
       var mobImage = mobImages[type];
       if(!mobImage) {
    	   mobImage =new Image();    	   
    	   mobImage['class']='mobImage';
    	   $(mobImage).one('error', function() { this.src = 'img/entity/unknown.png'; }); 
    	   mobImage.src='img/entity/' + type +'.png';
    	   mobImages[type] = mobImage;   	   
       } 
       
	   // Draw if image exists
       if(mobImage.height>0) {
    	   ctx.drawImage(mobImage, x-radius, z-radius, radius*2,radius*2);
       }
    }    
}

//Draw the location of other players
var drawMultiplayers = function(canvasWidth, canvasHeight) {

	logEntry("drawMultiplayers");
	
	var others = JM.players;
	if(!others) return;
	
	if(!canvasWidth || !canvasHeight) {
    	canvasWidth = getCanvasWidth();
    	canvasHeight = getCanvasHeight();
    }
	
	// Remove old
	$.each(otherImages, function(index, img) { 
		document.body.removeChild(img);
	});
	otherImages = new Array();
	
	ctx = canvas.getContext("2d");
	
	// Make new
    $.each(others, function(index, other) { 
       var x = getScaledChunkX(other.posX/16);
       var z = getScaledChunkZ(other.posZ/16);
       if(other.username!=player.name) {
	       if(x>=0 &&
	          x<=canvasWidth &&  
	          z>=0 &&
	          z<=canvasHeight) {

               ctx.globalAlpha=.85;
               ctx.strokeStyle = "#0f0";
               ctx.lineWidth = 2;
               ctx.beginPath();
               ctx.arc(x, z, 20, 0, Math.PI*2, true); 
               ctx.stroke();
               
               ctx.globalAlpha=1.0;             
               ctx.font = "bold 12px Arial";
               ctx.textAlign="center";
               ctx.fillStyle = "#000";
               ctx.fillText(other.username, x-2, z+28);
               ctx.fillText(other.username, x+2, z+32);
               ctx.fillStyle = "#0f0";
               ctx.fillText(other.username, x, z+30);

	           var otherImage = new Image();
	           otherImage.src = "other.png";
	           otherImage['class']='mobImage';
	           otherImage.title = other.username;
	           otherImage.style.position="absolute";
	           otherImage.style.visibility = "visible";
	           otherImage.style.height = "20px";
	           otherImage.style.width = "20px";
	           otherImage.style.left = (x-10) + "px";
	           otherImage.style.top = (z-10) + "px";
	           otherImage.style.zIndex = 1;
	           document.body.appendChild(otherImage);
	           otherImages.push(otherImage);
	       }
       }
    });
    
}

// Draw the map image to the background canvas
var drawBackgroundCanvas = function(canvasWidth, canvasHeight) {

   logEntry("drawBackgroundCanvas");
   
   // draw the png to the canvas
   if(latestMapImage) {
	  		
	    var width = latestMapImage.width;
	    var height = latestMapImage.height;
	    
	    if(!canvasWidth || !canvasHeight) {
	    	canvasWidth = getCanvasWidth();
	    	canvasHeight = getCanvasHeight();
	    }
	    
	    // Fill background
	    var ctx = bgCanvas.getContext("2d");
	    ctx.fillStyle = mapBackground;
        ctx.fillRect(0, 0 ,canvasWidth, canvasHeight);
        
        var autoScale = ctx.imageSmoothingEnabled || ctx.mozImageSmoothingEnabled || ctx.webkitImageSmoothingEnabled;
        if(autoScale) {
        	$(ctx).prop('imageSmoothingEnabled', smoothScale)
        		.prop('mozImageSmoothingEnabled', smoothScale)
        		.prop('webkitImageSmoothingEnabled', smoothScale)
        }

	    if(smoothScale===true || autoScale==true || mapScale==1) {
	    	ctx.fillStyle = mapBackground;
	        ctx.fillRect(0, 0, canvasWidth, canvasHeight);
	    	ctx.drawImage(latestMapImage, 0, 0, width*mapScale, height*mapScale);
	    } else {
	    	// phrogz.net/tmp/canvas_image_zoom.html
	    	// Copy pixels and scale with code-generated squares
		    ctx.drawImage(latestMapImage,0,0);
			var imgData = ctx.getImageData(0,0,width,height).data;
			//ctx.clearRect(0,0,width,height);
			
			for (var x=0;x<width;++x){
				for (var y=0;y<height;++y){
					var i = (y*width + x)*4;
					var r = imgData[i  ];
					var g = imgData[i+1];
					var b = imgData[i+2];
					var a = imgData[i+3];
					ctx.fillStyle = "rgba("+r+","+g+","+b+","+(a/255)+")";
					ctx.fillRect(x*mapScale,y*mapScale,mapScale,mapScale);
				}
			}
	    }	   
   }
}

function getScaledChunkX(chunkX) {
   var xOffset = ((mapBounds.x1) * chunkScale);
   return (chunkX * chunkScale) - xOffset;
}

function getScaledChunkZ(chunkZ) {
   var zOffset = ((mapBounds.z1) * chunkScale);
   return (chunkZ * chunkScale) - zOffset ;
}

/**
 * Update mouse coordinates based on the last event.
 */
function getMouse(event){
   if (!event) { /* For IE. */
      event = window.event;
   }
   mx=event.pageX;
   my=event.pageY;
}

function myDown(e){
   scrollCanvas(e);
}

function myUp(e){
   getMouse(e);
   
   var mouseDragX = (mx-msx);
   var mouseDragY = (my-msy);
   if(mouseDragX===0 && mouseDragY===0) 
   {
      userPanning=false;
   }
   if(userPanning===true){

      var xOffset = Math.floor(mouseDragX / chunkScale);
      var zOffset = Math.floor(mouseDragY / chunkScale);

      mapBounds.x1 = mapBounds.x1 - xOffset -1;
      mapBounds.z1 = mapBounds.z1 - zOffset -1;
      userPanning=false;
      
      updateMap();
   }
}

function myDblClick(e){
   getMouse(e);
}


function myKeyPress(e){
   
   var key=(e)?e.which:e.keyCode;
   switch(String.fromCharCode(key)){
      case'-':
      zoom('out');
      break;
      case'=':
      zoom('in');
      break;
      case'w':case'W':
      moveCanvas('up');
      break;
      case'a':case'A':
      moveCanvas('right');
      break;
      case's':case'S':
      moveCanvas('down');
      break;
      case'd':case'D':
      moveCanvas('left');
      break;
   }
}

function myMouseWheel(event, delta){
	
   if(halted===true) return;

   if(delta>0) {
	   zoom('in');
   } else if(delta<0) {
	   zoom('out');
   }

}

function zoom(dir){
   if(dir==='in' && mapScale<maxMapScale){
      setZoom(mapScale+1);
   }else if(dir==='out' && mapScale>minMapScale){
      setZoom(mapScale-1);
   }
}

function setZoom(scale) {
	
	console.log("zoom to scale: " + scale);

   var centerChunkX = Math.floor(getCanvasWidth()/chunkScale/2) + mapBounds.x1;
   var centerChunkZ = Math.floor(getCanvasHeight()/chunkScale/2) + mapBounds.z1;

   $( "#slider-vertical" ).slider( "value", scale );
   setScale(scale);
   centerMapOnChunk(centerChunkX, centerChunkZ);
   updateMap();
}

function scrollCanvas(e){
	
   if(halted===true) return;
   
   $("#playerImage").hide();
   $('body').css('cursor', 'move');
   userPanning=true;
   getMouse(e);
   msx=mx;
   msy=my;

   document.onmousemove = scrollingCanvas;
}

var scrollingCanvas = function(e){
   
   if(userPanning===true) {
	  $('body').css('cursor', 'move');
      getMouse(e);

      var mouseDragX = (mx-msx);
      var mouseDragY = (my-msy);
      var xOffset = Math.ceil(mouseDragX / chunkScale);
      var zOffset = Math.ceil(mouseDragY / chunkScale);
      
      if(Math.abs(xOffset)>0 || Math.abs(zOffset)>0) {
        setCenterOnPlayer(false);
      } 
      
      var canvasWidth = getCanvasWidth();
      var canvasHeight = getCanvasHeight();
      var drawX = xOffset*chunkScale;
      var drawZ = zOffset*chunkScale;

      // Clear canvas
      var ctx = canvas.getContext("2d");
      ctx.globalAlpha = 1;
      ctx.fillStyle = mapBackground;
      ctx.fillRect(0, 0, canvasWidth, canvasHeight);
      
      if(drawingMap!==true) {
	      ctx.drawImage(bgCanvas, drawX, drawZ);
	      ctx.drawImage(fgCanvas, drawX, drawZ);
	      drawPlayer(canvasWidth, canvasHeight, drawX, drawZ);
      }
      
   } else {
	  logEntry("scrollingCanvas done");

      document.onmousemove = null;
      $('body').css('cursor', 'default');      
   }
}

function moveCanvas(dir){
   switch(dir){
      case'left':
      mapBounds.x1++;
      break;
      case'right':
      mapBounds.x1--;
      break;
      case'up':
      mapBounds.z1--;
      break;
      case'down':
      mapBounds.z1++;
      break;
   }
   setCenterOnPlayer(false);
   drawMap();
}

var getURLParameter = function(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}
JM.debug = 'true'===getURLParameter('debug');

// Google Analytics
var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-28839029-1']);
_gaq.push(['_setDomainName', 'none']);
_gaq.push(['_setAllowLinker', true]);
_gaq.push(['_trackPageview']);

(function() {
  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
  ga.src = ('https:' === document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
})();

/** OnLoad **/
$(document).ready(initMessages);