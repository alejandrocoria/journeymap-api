
var mapScale = 2;
var minMapScale = 1;
var maxMapScale = 8;
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
var ctx;
var isScroll=false;
var mx,my;
var msx,msy;
var tempCanvasImage;
var tempCanvas;

var useTempCanvas = false;
var tempChunksImage;
var lastChunksImage;
var playerImage;

var mobImages = new Object();
var otherImages = new Array();
var chunkScale = mapScale*16;

var JmIcon;
var halted = false;
var uiInitialized = false;
var versionChecked = false;
var updatingMap = false;

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
		console.log("initUI called without JM.messages"); // shouldn't happen
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
    tempCanvas = document.createElement("canvas");    
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
	 	  drawMap();
	    });
    
    $("#nightButton").html(JM.messages.night_button_title)
    	.attr("title", JM.messages.night_button_desc)
	    .click(function() {
	       playerOverrideMap = true;
	 	   setMapType('night');
	 	  drawMap();
	    });
    
    $("#followButton").html(JM.messages.follow_button_title)
    	.attr("title", JM.messages.follow_button_desc)
	    .click(function() {
	 	   setCenterOnPlayer(!centerOnPlayer);
	 	   drawMap();
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
    
    $("#slider-vertical" ).slider({
        orientation: "vertical",
        range: "min",
        title: JM.messages.zoom_slider_name,
        min: minMapScale,
        max: maxMapScale,
        value: 2,
        slide: function( event, ui ) {
            setZoom(ui.value);
        }
    });
    
    $("#worldInfo").hide();
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
		$("#version").attr("innerHTML", JM.game.jm_version + " for Minecraft " + JM.game.mc_version);
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
	
	console.log("Initializing world...");
    
    // Clear existing timers (if any)
	clearTimers();
	
	// Reset state
	halted = false;
	updatingMap = false;
	setCenterOnPlayer(true);
	
	// Turn on UI elements that may have been hidden
	$(".jmtoggle").each(function(){$(this).show()});
	$("#slider-vertical").show();
	
	// Ensure the map is sized
	sizeMap();
	
	// Get world data once
	refreshWorldData(function(){
		
		// Update the map first
		updateMap();
		
		// Let the other data auto-refresh
		refreshMobsData();
		refreshPlayersData();
		refreshAnimalsData();
		refreshVillagersData();
		refreshWorldData(); 
	});
	 
	// Resize events can now update the map
	$(window).resize(function() {
		$("#playerImage").hide();
	    delay(function(){
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
	   playerImage=document.createElement("img");
	   playerImage.id="playerImage";
	   playerImage.style.position = "absolute";
	   playerImage.style.height = "64px";
	   playerImage.style.width = "64px";
	   playerImage.style.cursor = "hand";
	   playerImage.src="/img/locator-blue.png";
	   playerImage.onclick=function(){
	        setCenterOnPlayer(true);
	        drawMap();
	   };
	   document.body.appendChild(playerImage);
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

function getCanvasWidth() {
   var ctx = getContext();
   if(ctx.canvas.width) {
       return ctx.canvas.width; // Chrome, FF
   } else {
       return ctx.canvas.style.pixelWidth;  // IE
   }
}

function setCanvasWidth(width) {
   var ctx = getContext();
   if(ctx.canvas.width) {
       ctx.canvas.width  = width; // Chrome, FF
   } else {
       ctx.canvas.style.pixelWidth  = width;  // IE
   }
}

function getCanvasHeight() {
   var ctx = getContext();
   if(ctx.canvas.height) {
       return ctx.canvas.height; // Chrome, FF
   } else {
       return ctx.canvas.style.pixelHeight;  // IE
   }
}

function setCanvasHeight(height) {
   var ctx = getContext();
   if(ctx.canvas.height) {
       ctx.canvas.height  = height; // Chrome, FF
   } else {
       ctx.canvas.style.pixelHeight  = height;  // IE
   }
}

function centerMapOnPlayer() {
	if(JM.player) {
		centerMapOnChunk(Math.round(JM.player.chunkCoordX), Math.round(JM.player.chunkCoordZ));
	}
}

function centerMapOnChunk(chunkX, chunkZ) {
 
   var ctx = getContext();
   var maxChunksWide = Math.ceil(getCanvasWidth()/mapScale/16);
   var maxChunksHigh = Math.ceil(getCanvasHeight()/mapScale/16);  
   
   mapBounds.x1 = chunkX - Math.round(maxChunksWide/2) +1;
   mapBounds.z1 = chunkZ - Math.round(maxChunksHigh/2) +1;
   
   checkBounds();
}

function checkBounds() {
   // determine how many chunks we can display
   var ctx = getContext();
   var maxChunksWide = Math.ceil(getCanvasWidth()/mapScale/16);
   var maxChunksHigh = Math.ceil(getCanvasHeight()/mapScale/16);
   mapBounds.x2 = mapBounds.x1 + maxChunksWide;
   mapBounds.z2 = mapBounds.z1 + maxChunksHigh;  
}

function setMapType(mapType) {
   
   if(mapType==="day") {
      showLight = false;
      mapBackground = '#222';      
      $("#dayButton").addClass("active");
      $("#nightButton").removeClass("active");
      
   } else if(mapType==="night") {
      showLight = true;
      mapBackground = '#000';
      $("#dayButton").removeClass("active");
      $("#nightButton").addClass("active");
   } else {
      console.log("Error: Can't set mapType: " + mapType);
   }
   
   $('body').css('backgroundColor',mapBackground);   
   
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
   showCaves = show;
   if(showCaves===true) {
      $("#caveButton").addClass("active");
   } else {
      $("#caveButton").removeClass("active");
   }
   if(JM.player.underground===true) {
	   checkShowCaves();
	   drawMap();
   }
}


function checkShowCaves() {
   if(JM.player.underground===true && showCaves) {
	   mapBackground = "#000";
   } else {
	   if(showLight) {
		   setMapType('night');
	   } else {
		   setMapType('day');
	   }
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
   var ctx = getContext();
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

	var mapUrl = getMapDataUrl();
	var newChunksImage = $(document.createElement('img')).attr('src', mapUrl)
		.error(handleError) // TODO: check whether this will ever be called
	    .load(function() {
	        if (!this.complete || typeof this.naturalWidth == "undefined" || this.naturalWidth === 0) {
	            console.log('Map image incomplete!');
	        }       
	        lastChunksImage = newChunksImage[0];
	        $(newChunksImage).remove();
	                
	        if(callback) {
	        	callback();
	        } 
	    });
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
		
   		// Update UI
		$("#playerBiome").html(JM.player.biome);
		$("#playerLocation").html(JM.player.posX + "," + JM.player.posZ);
		
		$("#playerElevationTitle").attr('title', JM.messages.elevation_title + " " + (JM.player.posY>>4));
		$("#playerElevation").html(JM.player.posY + "&nbsp;(" + (JM.player.posY>>4) + ")");				
				
		// Do callback
		if(callback) {
        	callback();
        } 		
   	});

}



var updateMap = function() {  	
	
   logEntry("updateMap");
	
   if(isScroll===false && updatingMap===false) {
	   
	   updatingMap = true;
	   $("#mapCanvas").css('cursor', 'wait');
	   
	   refreshPlayerData(function() { 
						
			// With the player data updated, we can get the map data						
			if(centerOnPlayer===true) {
				centerMapOnPlayer();
			} else {
				checkBounds();
			}
						
			// Update the map image, then draw it
			refreshMapImage(function() {
				
				drawMap();
				updatingMap = false;
				$("#mapCanvas").css('cursor', 'auto');
				addTimer("updateMap", updateMap, JM.game.browser_poll);
			});	   
			
	   	});	  
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
		} else {
			displayError = data.statusText;
		}
	}	

	// Format UI
	$('body').css('backgroundColor',mapBackground); 
    sizeMap();
    
    var ctx = getContext();
    
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
        
    // Remove others
	$.each(otherImages, function(index, img) { 
		document.body.removeChild(img);
	});
	
    // Restart in 5 seconds
	if(!halted) {
		halted = true;
		console.log("Will re-check game state in 5 seconds.");
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

/**
 * Get the context of the current canvas
 */
var getContext = function() {

	// Which canvas to use
	var theCanvas;
	if(useTempCanvas) {
	   theCanvas = tempCanvas;
	} else {
	   theCanvas = canvas;
	}
	return theCanvas.getContext("2d");

}

// Draw the map
var drawMap = function() {
	
   logEntry("drawMap");
	
   $('body').css('cursor', 'wait');
   useTempCanvas = true;
   
   // init canvas dimensions
   sizeMap();
   var ctx = getContext();
   
   // check for showing caves
   checkShowCaves();
   
   // map data
   drawImageChunks();
   
   // player position
   drawPlayer();
   
   // mobs
   drawMobs();
   
   // other players
   if(JM.world.singlePlayer!=true) {
	   drawMultiplayers();
   }
   
   // Copy the result
   if(ctx.getImageData) {
      tempCanvasImage = ctx.getImageData(0, 0, getCanvasWidth(), getCanvasHeight());
   }
   
   // Now put on the visible canvas
   useTempCanvas = false;
   sizeMap();
   ctx = getContext();
   ctx.globalAlpha=1;
   if(ctx.putImageData) {
      ctx.putImageData(tempCanvasImage, 0, 0);
   }
   //getContext().drawImage(tempCanvas, 0,0,getCanvasWidth(), getCanvasHeight());
   
   // Cursor reset
   $('body').css('cursor', 'default');
}


// Draw the player location
var drawPlayer = function() {
	
   logEntry("drawPlayer");

   var player = JM.player;
   var x = getScaledChunkX(player.posX/16);
   var z = getScaledChunkZ(player.posZ/16);
   
   if(x>=0 &&
      x<=getCanvasWidth() &&  
      z>=0 &&
      z<=getCanvasWidth()) {     

       var ctx = getContext();
//       ctx.globalAlpha=.4;
//       if(showLight===false) {
//          ctx.fillStyle = "#000000";
//       } else {
//          ctx.fillStyle = "#ffffff";
//       }
//       ctx.beginPath();
//       ctx.arc(x, z, 20, 0, Math.PI*2, true); 
//       ctx.closePath();
//       ctx.fill();
       
       // Update player image
       var rotate = "rotate(" + player.heading + "deg)";
       $("#playerImage")
       		.css("left", x-32)
       		.css("top", z-32)
       		.css("zIndex", 2)
       		.css("-webkit-transform", rotate)
       		.css("-moz-transform", rotate)
       		.css("-o-transform", rotate)
       		.css("-ms-transform", rotate)
       		.css("transform", rotate);
       $("#playerImage").show();
     
   } 

}

// Draw the location of mobs
var drawMobs = function() {
	
	logEntry("drawMobs");
	
	var canvasWidth = getCanvasWidth();
	var canvasHeight = getCanvasHeight();
	
	if(showMobs===true && JM.mobs) {
	    $.each(JM.mobs, function(index, mob) {
			drawEntity(mob, canvasWidth, canvasHeight, false);
		});
	}
    
    if((showAnimals===true || showPets===true) && JM.animals) {
	    $.each(JM.animals, function(index, mob) {
			drawEntity(mob, canvasWidth, canvasHeight, true);
		});
    }
    
    if(showVillagers===true && JM.villagers) {
	    $.each(JM.villagers, function(index, mob) {
			drawEntity(mob, canvasWidth, canvasHeight, true);
		});
    }
    
}

//Draw the location of an entity
var drawEntity = function(mob, canvasWidth, canvasHeight, friendly) {
	
   var x = getScaledChunkX(mob.posX/16);
   var z = getScaledChunkZ(mob.posZ/16);
   
   if(x>=0 &&
      x<=canvasWidth &&  
      z>=0 &&
      z<=canvasHeight) {
	   
       var ctx = getContext();        
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
var drawMultiplayers = function() {

	logEntry("drawMultiplayers");
	
	var others = JM.players;
	if(!others) return;
	
	// Remove old
	$.each(otherImages, function(index, img) { 
		document.body.removeChild(img);
	});
	otherImages = new Array();
	
	// Make new
    $.each(others, function(index, other) { 
       var x = getScaledChunkX(other.posX/16);
       var z = getScaledChunkZ(other.posZ/16);
       if(other.username!=player.name) {
	       if(x>=0 &&
	          x<=getCanvasWidth() &&  
	          z>=0 &&
	          z<=getCanvasWidth()) {

               var ctx = getContext();
               ctx.globalAlpha=.85;
               ctx.strokeStyle = "#0f0";
               ctx.lineWidth = 2;
               ctx.beginPath();
               ctx.arc(x, z, 20, 0, Math.PI*2, true); 
               ctx.stroke();
               ctx.globalAlpha=1.0;
               
               ctx.globalAlpha=1;               
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

// Draw the chunks
// scaled image code via phrogz.net/tmp/canvas_image_zoom.html
var drawImageChunks = function() {

   logEntry("drawImageChunks");
   
   // draw the png to the canvas
   if(lastChunksImage) {
	   
	    
		var zoom = mapScale;
	    var width = lastChunksImage.width;
	    var height = lastChunksImage.height;
	    
	    var ctx = getContext();
	    
	    if(smoothScale===true) {
	    	ctx.drawImage(lastChunksImage, 0, 0, width*mapScale, height*mapScale);
	    } else {
	    
		    ctx.clearRect(0,0,width,height);
		    ctx.drawImage(lastChunksImage,0,0);
			var imgData = ctx.getImageData(0,0,width,height).data;
			ctx.clearRect(0,0,width,height);
			
			for (var x=0;x<width;++x){
				for (var y=0;y<height;++y){
					var i = (y*width + x)*4;
					var r = imgData[i  ];
					var g = imgData[i+1];
					var b = imgData[i+2];
					var a = imgData[i+3];
					ctx.fillStyle = "rgba("+r+","+g+","+b+","+(a/255)+")";
					ctx.fillRect(x*zoom,y*zoom,zoom,zoom);
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
      isScroll=false;
   }
   if(isScroll){

      var xOffset = Math.floor(mouseDragX / chunkScale);
      var zOffset = Math.floor(mouseDragY / chunkScale);
      //console.log("mouseDragX=" + mouseDragX + ", mouseDragY=" + mouseDragY);
      //console.log("xOffset=" + xOffset + ", zOffset=" + zOffset);

      mapBounds.x1 = mapBounds.x1 - xOffset -1;
      mapBounds.z1 = mapBounds.z1 - zOffset -1;
      isScroll=false;
      
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

   var centerChunkX = Math.floor(getCanvasWidth()/chunkScale/2) + mapBounds.x1;
   var centerChunkZ = Math.floor(getCanvasHeight()/chunkScale/2) + mapBounds.z1;

   $( "#slider-vertical" ).slider( "value", scale );
   setScale(scale);
   centerMapOnChunk(centerChunkX, centerChunkZ);
   updateMap();
}

function scrollCanvas(e){
	
   if(halted===true) return;
   
   $('body').css('cursor', 'move');
   isScroll=true;
   getMouse(e);
   msx=mx;
   msy=my;

   // Draw snapshot in current position
   var ctx = tempCanvas.getContext("2d");
   ctx.globalAlpha=.5;
   ctx.drawImage(canvas,0,0);
   $("#playerImage").hide();
   document.onmousemove = scrollingCanvas;
}

var scrollingCanvas = function(e){
   
   if(isScroll) {
	   $('body').css('cursor', 'move');
	  $("#playerImage").hide();
      getMouse(e);

      var mouseDragX = (mx-msx);
      var mouseDragY = (my-msy);
      var xOffset = Math.ceil(mouseDragX / chunkScale);
      var zOffset = Math.ceil(mouseDragY / chunkScale);
      
      if(Math.abs(xOffset)>0 || Math.abs(zOffset)>0) {
        setCenterOnPlayer(false);
      } 

      var ctx = getContext();
      
      // Draw background
      ctx.globalAlpha = 1;
      ctx.fillStyle = mapBackground;
      ctx.fillRect(0, 0, getCanvasWidth(), getCanvasHeight());
      
      // Draw temp canvas
      ctx.drawImage(tempCanvas,(xOffset*chunkScale), (zOffset*chunkScale));
      //console.log("Scrolling: " + xOffset + "," + zOffset);
      
   } else {
	  logEntry("scrollingCanvas done");
	   
      tempCanvasImage = null; // why?
      document.onmousemove = null;
      $('body').css('cursor', 'default');      
   }
}

function moveCanvas(dir){
   //console.log("moveCanvas " + dir);
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