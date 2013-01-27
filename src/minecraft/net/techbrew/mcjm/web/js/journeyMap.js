var headerSize = 0;
var mapScale = 2;
var minMapScale = 1;
var maxMapScale = 8;
var mapBounds = {x1:0,z1:0,x2:0,z2:0};
var mapDataMode = "png";
var showLight = false;
var showCaves = true;
var showMonsters = true;
var centerOnPlayer = true;
var autoRefresh = true;
var mapBackground = "#222";
var chunks = new Object();
var canvas;
var ctx;
var isScroll=false;
var mx,my;
var msx,msy;
var tempCanvasImage;
var tempCanvas;
var playerCanvas;
var useTempCanvas = false;
var lastChunksImage;
var playerImage;

var refreshDataTimer;

var mobImages = new Object();
var otherImages = new Array();
var chunks = new Object();
var chunkScale = mapScale*16;
var playerLastPos = "0,0";
var clientRefreshRate = 1500;

var JmIcon;
var halted = false;

var JML10N = {};

var JM = {
    mobs:[],
    animals:[],
    players:[]
};


/** OnLoad **/
$(document).ready(init);

function init() {
	
    // Offset canvas
    $("#mapCanvas").offset({ top: headerSize, left: 0});
    
    // Init canvases
    canvas = $("#mapCanvas")[0];
    tempCanvas = document.createElement("canvas");
    playerCanvas = document.createElement("canvas");
        
	// Get L10N messages, set strings on success
    $.ajax({
        url: "/data/messages", 
        dataType: "jsonp",
        contentType: "application/javascript; charset=utf-8",
        async: false})
    .fail(handleError)
	.done(function(data, textStatus, jqXHR) { 
		
        JML10N = data;
        
        // Set page language, although at this point it may be too late to matter.
        $('html').attr('lang', JML10N.locale.split('_')[0]);
        
        // Set RSS feed title
        $("link #rssfeed").attr("title", JML10N.rss_feed_title);
        
        // Init toolbar button tooltips
        $("#dayButton").html("<a href='#' title='" + JML10N.day_button_desc +"'>" + JML10N.day_button_title + "</a>");
        
        $("#nightButton").html("<a href='#' title='" + JML10N.night_button_desc +"'>" + JML10N.night_button_title + "</a>");
        $("#followButton").html("<a href='#' title='" + JML10N.follow_button_desc +"'>" + JML10N.follow_button_title + "</a>");
        $("#caveButton").html("<a href='#' title='" + JML10N.cave_button_desc +"'>" + JML10N.cave_button_title + "</a>");
        
        $("#monstersButton").attr("title", "<b>" + JML10N.monsters_button_title + "</b><br/>" + JML10N.monsters_button_desc);
        $("#saveButton").attr("title", JML10N.save_button_title);
        $("#aboutButton").attr("title", JML10N.about_button_title);
        
        // TODO:
        // JML10N.rss_feed_desc 
        //JML10N.email_sub_desc
        //JML10N.follow_twitter
        //JML10N.zoom_slider_name
        
        // Init mob images
        initImages();
        
        // Get header height
        //headerSize = $("#header").height();
        
        // Init slider
        $(function() {
             $( "#slider-vertical" ).slider({
                 orientation: "vertical",
                 range: "min",
                 title: JML10N.zoom_slider_name,
                 min: minMapScale,
                 max: maxMapScale,
                 value: 2,
                 slide: function( event, ui ) {
                     setZoom(ui.value);
                 }
             });
         });
        
        // Init buttons
        $("#dayButton").click(function() {
     	   setMapType('day');
     	   refreshData();
        });
        
        $("#nightButton").click(function() {
     	   setMapType('night');
     	   refreshData();
        });
        
        $("#caveButton").click(function() {
     	   setShowCaves(!showCaves);
        });
        
        $("#followButton").click(function() {
     	   setCenterOnPlayer(!centerOnPlayer);
     	   refreshData();
        });
                
        // Init map type
        setMapType('day');
        
        // Size the canvases and setup for custom event handling
        registerEvents();   
        
        // Init world info   
        initWorld();  
   });
}


function checkVersion() {

   $("#version").attr("innerHTML", JM.world.jm_version + " for Minecraft " + JM.world.mc_version);
   if(JM.world.latest_journeymap_version>JM.world.jm_version) {
       $("#versionButton").attr("title", "<b>" + JML10N.update_available + "</b><br/>JourneyMap " + JM.world.latest_journeymap_version + " for Minecraft " + JM.world.latest_minecraft_version);
       $("#versionButton").css("visibility", "visible");
       $("#versionButton").tooltip({
           effect: 'slide',
           opacity: .9,
        }).dynamic({ bottom: { direction: 'down', bounce: true } });
       //$("#versionButton").tooltip.show();
   }
   
   _gaq.push(['_setCustomVar', 1, 'jm_version', JM.world.jm_version, 2]);
   _gaq.push(['_trackEvent', 'Client', 'CheckVersion', JM.world.jm_version]);
}

function initImages() {

   // Init player marker
   playerImage=document.createElement("img");
   playerImage.id="playerImage";
   playerImage.style.position = "absolute";
   playerImage.style.height = "32px";
   playerImage.style.width = "32px";
   playerImage.style.cursor = "hand";
   playerImage.src="/img/arrow.png";
   playerImage.onclick=function(){
        setCenterOnPlayer(true);
        refreshData();
   };
   document.body.appendChild(playerImage);

}

function saveMapImage() {
	var mapType = (player.underground && showCaves) ? "underground" : (showLight ? "night" : "day") ;  
    var depth = player.chunkCoordY;
    var path = worldPath.replace("/jm", "/save");
    var request = path + "&mapType=" + mapType + "&depth=" + depth + "&t=" + new Date().getTime();
	window.open(request);
}

function initWorld() {
	
	// Clear the refresh interval, if any
    if(refreshDataTimer) {
    	clearInterval(refreshDataTimer); 
    }
	
	// Start with the time data
	$.ajax({url: "/data/time", dataType: "jsonp"})
	.fail(handleError)
	.done(function(data, textStatus, jqXHR) { 		
		JM.time = data;
		
		// Now get the world data
		$.ajax({url: "/data/world", dataType: "jsonp"})
		.fail(handleError)
		.done(function(data, textStatus, jqXHR) { 
			
			JM.world = data;
			
			 if(JM.world.name!=null) {
		  	   $("#worldNameHeader").html(unescape(JM.world.name).replace("\\+"," "));            	   		          
		     }
			 
			 setCenterOnPlayer(true);
			 
		     // Auto-refresh the data once per N seconds
		     if(mapScale>minMapScale) {
		  	   var refreshRate = clientRefreshRate;
		     } else {
		  	   var refreshRate = clientRefreshRate * 2;
		     }		    
		     
		     refreshDataTimer = setInterval(function() {
		        if(autoRefresh && !isScroll) {
		           refreshData();
		        }		        
		     }, refreshRate);
	     		     
		     sizeMap();
		     refreshData();
		     
		     // Check version
		     checkVersion();
		});    
		
	}); 

}

var delay = (function(){
   var timer = 0;
   return function(callback, ms){
      clearTimeout (timer);
      timer = setTimeout(callback, ms);
   };
})();

function setScale(newScale) {
   mapScale = newScale;
   chunkScale = mapScale*16;
}

function sizeMap() {
   // Update canvas size   
   setCanvasWidth($(window).width());
   setCanvasHeight($(window).height()-headerSize);
   document.body.style.backgroundColor = mapBackground;
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

function setMapType(mapType, refresh) {
   
   //console.log('SetMapType: ' + mapType);
   if(mapType=="day") {
      showLight = false;
      mapBackground = "#222";     
      $("#dayButton").addClass("active");
      $("#nightButton").removeClass("active");
      
   } else if(mapType=="night") {
      showLight = true;
      mapBackground = "#000";
      $("#dayButton").removeClass("active");
      $("#nightButton").addClass("active");
   } else {
      console.log("Error: Can't set mapType: " + mapType);
      return;  
   }
   
   document.body.style.backgroundColor = mapBackground;   
   if(refresh==true) {
     refreshData();
   } 
   
}

function setCenterOnPlayer(onPlayer) {
   
   centerOnPlayer = onPlayer;
   if(onPlayer==true) {
      centerMapOnPlayer();
      $("#followButton").addClass("active");
   } else {
      $("#followButton").removeClass("active");
   }
}

function setShowCaves(show) {	   
   showCaves = show;
   if(showCaves==true) {
      $("#caveButton").addClass("active");
   } else {
      $("#caveButton").removeClass("active");
   }
   if(player.underground==true) {
	   refreshData();
   }
}

function setShowMonsters(show) {	   
   showMonsters = show;
   if(showMonsters==true) {
      $("#monstersButton").addClass("active");
   } else {
      $("#monstersButton").removeClass("active");
   }
}

function checkShowCaves() {
   if(JM.player.underground==true && showCaves) {
	   mapBackground = "#000";
   } else {
	   if(showLight) {
		   setMapType('night');
	   } else {
		   setMapType('day');
	   }
   }
}

////////////// DRAW ////////////////////

// Get the context of the current canvas
function getContext() {
   
   // Which canvas to use
   var theCanvas;
   if(useTempCanvas) {
      theCanvas = tempCanvas;
   } else {
      theCanvas = canvas;
   }
   return theCanvas.getContext("2d");
   
}

function getMapDataUrl() {
   var ctx = getContext();
   var width = getCanvasWidth();
   var height = getCanvasHeight();
   var mapType = (JM.player.underground && showCaves) ? "underground" : (showLight ? "night" : "day") ;  
   var depth = JM.player.chunkCoordY;
   var request = "/map.png?mapType=" + mapType + "&depth=" + depth + "&x1=" + mapBounds.x1+ "&z1=" + mapBounds.z1 + 
                             "&x2=" + mapBounds.x2 + "&z2=" + mapBounds.z2 + "&width=" + width + "&height=" + height;
   return request;
}


function refreshData() {  
	
	$.ajax({url: "/data/time", dataType: "jsonp"})
	.fail(handleError)
	.done(function(data, textStatus, jqXHR) { 		
		JM.time = data;
	}); 
	
   if(isScroll==false) {
	   
	   $.ajax({url: "/data/player", dataType: "jsonp"})
		.fail(handleError)
	   	.done(function(data, textStatus, jqXHR) { 
			JM.player = data;
			
			// With the player data updated, we can get the map data			
			// Update bounds first
			checkBounds();			
			
			if(centerOnPlayer) {
				centerMapOnPlayer();
			}
			
			// Update the lastChunksImage with the map data
			lastChunksImage = new Image();
			lastChunksImage.onload = function () {          
			    // Draw the image on the canvas
			    var ctx = getContext();         
			    updateUI();
			}    
			lastChunksImage.src=getMapDataUrl();
	   	});
	   
	   $.ajax({url: "/data/players", dataType: "jsonp"})
		.fail(handleError)
	   	.done(function(data, textStatus, jqXHR) { 
	   		JM.players = data.players;
	   	});
	   
	   $.ajax({url: "/data/mobs", dataType: "jsonp"})
		.fail(handleError)
	   	.done(function(data, textStatus, jqXHR) { 
	   		JM.mobs = data.mobs;
	   	});
	   
	   $.ajax({url: "/data/animals", dataType: "jsonp"})
		.fail(handleError)
	   	.done(function(data, textStatus, jqXHR) { 
	   		JM.animals = data.animals;
	   	});
	   
	   $.ajax({url: "/data/villagers", dataType: "jsonp"})
		.fail(handleError)
	   	.done(function(data, textStatus, jqXHR) { 
	   		JM.villagers = data.villagers;
	   	});
	   
	   
   }
}


// Ajax request got an error from the server
function handleError(data, error, jqXHR) {
	
	console.log("Server returned error: " + data.status + ": " + jqXHR);
	
	clearInterval(refreshDataTimer);
	refreshDataTimer = null;
	
	var displayError;
	if(data.status==503 || data.status==0) {
		if(JML10N.error_world_not_opened) {
			displayError = JML10N.error_world_not_opened;
		} else {
			displayError = data.statusText;
		}
	}	

	// Format UI
    document.body.style.backgroundColor = "#000";
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
		setTimeout(function(){
			console.log("Trying to re-initialize");
			halted = false;
			init();
		},5000);
	}
}


// Draw the map
function updateUI() {
   document.body.style.cursor = "wait";
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
   drawOthers();
   
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
   document.body.style.cursor = "default";
}

// Fill in the background of the canvas
function drawBackground() {
   var ctx = getContext();
   ctx.globalAlpha = 1;
   ctx.fillStyle = mapBackground;
   ctx.fillRect(0, 0, getCanvasWidth(), getCanvasHeight());
}

// Show world name, type, player coords, etc.
function updateWorldInfo() {
	
	var table = "<table><tbody>";
    if(JM.world.dimension==-1) {
 	   table +="<tr><th colspan='2' style='color:#a00;font-weight:bold;text-align:center'>"+ JML10N.world_name_nether +"</th></tr>";
    } else if(JM.world.dimension==1) {
 	   table +="<tr><th colspan='2' style='color:#a00;font-weight:bold;text-align:center'>"+ JML10N.world_name_end +"</th></tr>";
    } 
    
    table += "<tr><th colspan='2'>";
    if(JM.world.dimension==0) {
       if(JM.time.worldCurrentTime<12000) {
     	  table += JML10N.sunset_begins;
       } else if(JM.time.worldCurrentTime<13800) {
     	  table += JML10N.night_begins;
       } else if(JM.time.worldCurrentTime<22200) {
     	  table += JML10N.sunrise_begins;
       } else if(JM.time.worldCurrentTime<23999) {
     	  table += JML10N.day_begins;
       } 
    }
    table += "</th></tr>";
    
    /*
    0 is the start of daytime, 12000 is the start of sunset, 13800 is the start of nighttime, 22200 is the start of sunrise, and 24000 is daytime again. 
   */
   var allsecs = JM.time.worldCurrentTime/20;
   var mins = Math.floor(allsecs / 60);
   var secs = Math.ceil(allsecs % 60);
   if(mins<10) mins = "0"+mins;
   if(secs<10) secs = "0"+secs;
   var currentTime = mins + ":" + secs;
   table += "<tr><th>" + JML10N.worldtime_title + "</th><td>" + currentTime + "</td></tr>";
   
   var playerPos = player.posX + "," + player.posZ;
   if(playerPos!=playerLastPos) {
        playerLastPos = playerPos;
   }
   
   table += "<tr><th>" + JML10N.location_title + "</th><td>" + playerPos + "</td></tr>"; 
   table += "<tr><th title='" + JML10N.location_title + " " + (player.posY>>4) + "'>" + JML10N.elevation_title + "</th><td>" + player.posY + "&nbsp;(" + (player.posY>>4) + ")</td></tr>"; 
   
   if(JM.world.dimension==0) {
	   table += "<tr><th>" + JML10N.biome_title + "</th><td>" + player.biome + "</td></tr>";
   }
   
   table+="</tbody></table>";
   //console.log(table);
   
   $("#worldInfo").attr("innerHTML", table);
}

// Draw the player location
function drawPlayer() {

   var player = JM.player;
   var x = getScaledChunkX(player.posX/16);
   var z = getScaledChunkZ(player.posZ/16);
   
   if(x>=0 &&
      x<=getCanvasWidth() &&  
      z>=0 &&
      z<=getCanvasWidth()) {     

       var ctx = getContext();
       ctx.globalAlpha=.4;
       if(showLight==false) {
          ctx.fillStyle = "#000000";
       } else {
          ctx.fillStyle = "#ffffff";
       }
       ctx.beginPath();
       ctx.arc(x, z, 20, 0, Math.PI*2, true); 
       ctx.closePath();
       ctx.fill();

       $("#playerImage").css("visibility","visible");
       $("#playerImage").css("left", x-16);
       $("#playerImage").css("top", z-16);
       $("#playerImage").css("zIndex", 2);
       
       var rotate = "rotate(" + player.heading + "deg)";
       $("#playerImage").css("-webkit-transform", rotate);
       $("#playerImage").css("-moz-transform", rotate);
       $("#playerImage").css("-o-transform", rotate);
       $("#playerImage").css("-ms-transform", rotate);
       $("#playerImage").css("transform", rotate);
       
       $("#playerImage").attr("title",player.name);        
          
   } else {
        //console.log("Player offscreen");
   }

}

// Draw the location of mobs
function drawMobs() {
	
	if(showMonsters==false) return; // TODO

	var mobs = JM.mobs;
	var mobs = mobs.concat(JM.animals); // TODO
	
	var canvasWidth = getCanvasWidth();
	var canvasHeight = getCanvasHeight();
	var func = 
	
    $.each(JM.mobs, function(index, mob) {
		drawEntity(mob, canvasWidth, canvasHeight, false);
	});
    $.each(JM.animals, function(index, mob) {
		drawEntity(mob, canvasWidth, canvasHeight, true);
	});
    $.each(JM.villagers, function(index, mob) {
		drawEntity(mob, canvasWidth, canvasHeight, true);
	});
    
}

//Draw the location of an entity
function drawEntity(mob, canvasWidth, canvasHeight, friendly) {
	
   var x = getScaledChunkX(mob.posX/16);
   var z = getScaledChunkZ(mob.posZ/16);
   
   if(x>=0 &&
      x<=canvasWidth &&  
      z>=0 &&
      z<=canvasHeight) {
  
       var ctx = getContext();
       ctx.globalAlpha=.85;
       ctx.strokeStyle = friendly ? "#ccc" : "#f00";
       ctx.lineWidth = 2;
       ctx.beginPath();
       var radius = 16;
       var type = mob.type; 
       if(type=='Ghast' || type=='Dragon' || type=='Wither') {
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
    	   mobImage.onload = function () {  
    		   // Draw after loaded
    		   ctx.drawImage(mobImage, x-radius, z-radius, radius*2,radius*2);
		   }        	   
    	   mobImage.src='img/entity/' + type +'.png';
    	   mobImages[type] = mobImage;
	   	   console.log("Inited image for mob type: " + type);	   	   
       } else {
    	   // Draw now
    	   ctx.drawImage(mobImage, x-radius, z-radius, radius*2,radius*2);
       }
   }
    
}

//Draw the location of other players
function drawOthers() {

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
function drawImageChunks() {

   var maxHeight = 0;
   var minHeight = 128;
   var key;
   
   // draw the png to the canvas
   var ctx = getContext();
   ctx.drawImage(lastChunksImage, 0, 0, lastChunksImage.width*mapScale, lastChunksImage.height*mapScale);
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
 * Register for user events
 */
function registerEvents() {

    // Disable selection events by default
    $("*").live('selectstart dragstart', function(evt){ evt.preventDefault(); return false; });

   // Disable edit cursor
   canvas.onselectstart = function () { return false; } // ie 
   
   // Mouse events
   canvas.onmousedown=myDown;
   canvas.onmouseup=myUp;
   canvas.ondblclick=myDblClick;
   if (window.addEventListener) {
      window.addEventListener('DOMMouseScroll', myMouseWheel, false);
   }
   window.onmousewheel = document.onmousewheel = myMouseWheel;
   
   
   // Keyboard events
  document.onkeypress=myKeyPress;
   
   // Resize events
   window.onresize = function(event) {
      delay(refreshData, 200);
   }
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
   //console.log("myUp");
   getMouse(e);
   
   var mouseDragX = (mx-msx);
   var mouseDragY = (my-msy);
   if(mouseDragX==0 && mouseDragY==0) 
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
      
      refreshData();
   }
}

function myDblClick(e){
   //console.log("myDblClick");
   getMouse(e);
}


function myKeyPress(e){
   //console.log("myKeyPress");
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

function myMouseWheel(event){
   //console.log("myMouseWheel");

   var delta = 0;

   if (!event) { /* For IE. */
      event = window.event;
   }
   if (event.wheelDelta) { /* IE/Opera. */
      delta = event.wheelDelta/120;
      if (window.opera) delta = -delta;
   } else if (event.detail) { /** Mozilla case. */
      delta = -event.detail/3;
   }

   if(delta>0) zoom('in');
   if(delta<0) zoom('out');

   if (event.preventDefault) {
      event.preventDefault();
   }
   event.returnValue = false;
   event.cancelBubble = true;
}

function zoom(dir){
   if(dir=='in' && mapScale<maxMapScale){
      setZoom(mapScale+1);
   }else if(dir=='out' && mapScale>minMapScale){
      setZoom(mapScale-1);
   }
}

function setZoom(scale) {

   var centerChunkX = Math.floor(getCanvasWidth()/chunkScale/2) + mapBounds.x1;
   var centerChunkZ = Math.floor(getCanvasHeight()/chunkScale/2) + mapBounds.z1;

   $( "#slider-vertical" ).slider( "value", scale );
   setScale(scale);
   centerMapOnChunk(centerChunkX, centerChunkZ);
   refreshData();
}

function scrollCanvas(e){
   isScroll=true;
   getMouse(e);
   msx=mx;
   msy=my;

   // Draw snapshot in current position
   var ctx = tempCanvas.getContext("2d");
   ctx.globalAlpha=.5;
   ctx.drawImage(canvas,0,0);
   playerImage.style.visibility="hidden";
   document.onmousemove = scrollingCanvas;
}

function scrollingCanvas(e){
   if(isScroll) {
      document.body.style.cursor = "move";
      getMouse(e);

      var mouseDragX = (mx-msx);
      var mouseDragY = (my-msy);
      var xOffset = Math.ceil(mouseDragX / chunkScale);
      var zOffset = Math.ceil(mouseDragY / chunkScale);
      
      if(Math.abs(xOffset)>0 || Math.abs(zOffset)>0) {
        setCenterOnPlayer(false);
      } 

      drawBackground();
      var ctx = getContext();
      ctx.globalAlpha=1;
      ctx.drawImage(tempCanvas,(xOffset*chunkScale), (zOffset*chunkScale));
      //console.log("Scrolling: " + xOffset + "," + zOffset);
      
   } else {
      tempCanvasImage = null;
      document.onmousemove = null;
      document.body.style.cursor = "default";
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
   refreshData();
}

// Google Analytics
var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-28839029-1']);
_gaq.push(['_setDomainName', 'none']);
_gaq.push(['_setAllowLinker', true]);
_gaq.push(['_trackPageview']);

(function() {
  var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
  ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
  var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
})();