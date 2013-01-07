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
var mapBackground = "#252";
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
var worldPath;
var worldProviderType;
var worldName;
var worldTime;
var worldSeed;
var player;
var jm_version;
var mc_version;
var latest_journeymap_version;
var latest_minecraft_version;
var refreshDataTimer;
var mobs;
var others;
var mobImages = new Object();
var otherImages = new Array();
var chunks = new Object();
var chunkScale = mapScale*16;
var playerLastPos = "0,0";
var clientRefreshRate = 1500;


/** Before OnLoad **/
   $(function() {
        // Init about box
        $("a[rel]").overlay({
            mask: 'darkred',
            effect: 'apple',
            onBeforeLoad: function() {
                var wrap = this.getOverlay().find(".contentWrap");
                wrap.load(this.getTrigger().attr("href"));
            }
        });
    });
    
/** OnLoad **/
$(document).ready(init);

function init() {
	
   // IE check
   if(!(window.badBrowser===undefined)) {
        return;
   }
   
   // Check for l10n messages
   if(typeof JML10N === "undefined") {
	   alert('There was a problem getting localized messages for your locale. Please report this error on the JourneyMap helpdesk:\nhttp://journeymap.techbrew.net/helpdesk/');
	   return;
   }

   // Set window size for iPhone
   if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i))) {
      window.scrollTo(0, 1);
   }
   
   // Init mob images
   initImages();
   
   // Init canvases
   canvas = $("#mapCanvas")[0];
   tempCanvas = document.createElement("canvas");
   playerCanvas = document.createElement("canvas");
   
   // Init slider
   $(function() {
        $( "#slider-vertical" ).slider({
            orientation: "vertical",
            range: "min",
            min: minMapScale,
            max: maxMapScale,
            value: 2,
            step:1,
            slide: function( event, ui ) {
                setZoom(ui.value);
            }
        });
    });
   
   // Init map type
   setMapType('day');
   
   // Size the canvases and setup for custom event handling
   registerEvents();   
   
   // Init world info   
   initWorld();  
}

function checkVersion() {

   $("#version").attr("innerHTML", jm_version + " for Minecraft " + mc_version);
   if(latest_journeymap_version>jm_version) {
       $("#versionButton").attr("title", "<b>" + JML10N.update_available + "</b><br/>JourneyMap " + latest_journeymap_version + " for Minecraft " + latest_minecraft_version);
       $("#versionButton").css("visibility", "visible");
       $("#versionButton").tooltip({
           effect: 'slide',
           opacity: .9,
        }).dynamic({ bottom: { direction: 'down', bounce: true } });
       //$("#versionButton").tooltip.show();
   }
   
   _gaq.push(['_setCustomVar', 1, 'jm_version', jm_version, 2]);
   _gaq.push(['_trackEvent', 'Client', 'CheckVersion', jm_version]);
}

function initImages() {

    // Init toolbar button tooltips
    $("#dayButton").attr("title", "<b>" + JML10N.day_button_title + "</b><br/>" + JML10N.day_button_desc);
    $("#nightButton").attr("title", "<b>" + JML10N.night_button_title + "</b><br/>" + JML10N.night_button_desc);
    $("#followButton").attr("title", "<b>" + JML10N.follow_button_title + "</b><br/>" + JML10N.follow_button_desc);
    $("#caveButton").attr("title", "<b>" + JML10N.cave_button_title + "</b><br/>" + JML10N.cave_button_desc);
    $("#monstersButton").attr("title", "<b>" + JML10N.monsters_button_title + "</b><br/>" + JML10N.monsters_button_desc);
    $("#saveButton").attr("title", "<b>" + JML10N.save_button_title + "</b><br/>" + JML10N.save_button_desc);
    $("#aboutButton").attr("title", "<b>" + JML10N.about_button_title + "</b><br/>" + JML10N.about_button_desc);

    $("#toolbar img[title]").tooltip({
       effect: 'slide',
       opacity: .9,
    }).dynamic({ bottom: { direction: 'down', bounce: true } });
    
    
    $("#infobar img[title]").tooltip({
       effect: 'slide',
       opacity: .9,
    }).dynamic({ bottom: { direction: 'down', bounce: true } });

   // Init player marker
   playerImage=document.createElement("img");
   playerImage.id="playerImage";
   playerImage.style.position = "absolute";
   playerImage.style.height = "32px";
   playerImage.style.width = "32px";
   playerImage.style.cursor = "hand";
   playerImage.src="arrow.png";
   playerImage.onclick=function(){
        setCenterOnPlayer(true);
        refreshData();
   };
   document.body.appendChild(playerImage);
   
   // Init mob images
   initMobImage("Creeper");
   initMobImage("Skeleton");
   initMobImage("Zombie");
   initMobImage("PigZombie");
   initMobImage("Spider");
   initMobImage("Enderman");
   initMobImage("Silverfish");
   initMobImage("Ghast");
   initMobImage("Dragon");
   initMobImage("Slime");
   initMobImage("MagmaCube");
   initMobImage("Blaze");
   initMobImage("Wolf");
}

function initMobImage(name) {
   var img =new Image();
   img['class']='mobImage';
   img.src=name +'.png';
   mobImages[name] = img;
}

function saveMapImage() {
	var mapType = (player.underground && showCaves) ? "underground" : (showLight ? "night" : "day") ;  
    var depth = player.chunkCoordY;
    var path = worldPath.replace("/jm", "/save");
    var request = path + "&mapType=" + mapType + "&depth=" + depth + "&t=" + new Date().getTime();
	window.open(request);
}

function initWorld() {

   // Get initial settings
   $.ajax({
           url: "/jm",
           dataType: "script",
           success : function() {          
               
               if(worldName!=null) {
            	   $("#worldNameHeader").attr("innerHTML",unescape(worldName).replace("\\+"," "));            	   
                    setCenterOnPlayer(true);
               }
               // Auto-refresh the map once per N seconds
               if(mapScale>minMapScale) {
            	   var refreshRate = clientRefreshRate;
               } else {
            	   var refreshRate = clientRefreshRate * 2;
               }
               refreshDataTimer = setInterval(function() {
                  if(autoRefresh && !isScroll) {
                     refreshData();
                  }
                  //console.log("Refreshed");
               }, refreshRate);
               sizeMap();
               refreshData();
               // Check version
               checkVersion();
   
           },
           error : function(jqXHR, textStatus, errorThrown) {
                     handleError(errorThrown);
                  }
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
   setCanvasHeight($(window).height());
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
   centerMapOnChunk(Math.round(player.chunkCoordX), Math.round(player.chunkCoordZ));
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
      mapBackground = "#252";     
      $("#dayButton").removeClass("imgButton").addClass("imgButtonSelected");
      $("#nightButton").removeClass("imgButtonSelected").addClass("imgButton");
      
   } else if(mapType=="night") {
      showLight = true;
      mapBackground = "#000";
      $("#dayButton").removeClass("imgButtonSelected").addClass("imgButton");
      $("#nightButton").removeClass("imgButton").addClass("imgButtonSelected");
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
      $("#followButton").removeClass("imgButton").addClass("imgButtonToggle");
   } else {
      $("#followButton").removeClass("imgButtonToggle").addClass("imgButton");
   }
}

function setShowCaves(show) {	   
   showCaves = show;
   if(showCaves==true) {
      $("#caveButton").removeClass("imgButton").addClass("imgButtonToggle");
   } else {
      $("#caveButton").removeClass("imgButtonToggle").addClass("imgButton");
   }
   if(player.underground==true) {
	   refreshData();
   }
}

function setShowMonsters(show) {	   
   showMonsters = show;
   if(showMonsters==true) {
      $("#monstersButton").removeClass("imgButton").addClass("imgButtonToggle");
   } else {
      $("#monstersButton").removeClass("imgButtonToggle").addClass("imgButton");
   }
}

function checkShowCaves() {
   if(player.underground==true && showCaves) {
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
   var mapType = (player.underground && showCaves) ? "underground" : (showLight ? "night" : "day") ;  
   var depth = player.chunkCoordY;
   var request = worldPath + "&mapType=" + mapType + "&depth=" + depth + "&x1=" + mapBounds.x1+ "&z1=" + mapBounds.z1 + 
                             "&x2=" + mapBounds.x2 + "&z2=" + mapBounds.z2 + "&width=" + width + "&height=" + height +
                             "&t=" + new Date().getTime();
   return request;
}


function refreshData() {  
   if(isScroll==false) {
	  refreshImageData();
   }
}

function refreshImageData() {
   $.ajax({
        url: "/jm",
        dataType: "script",
        success : function() {          
            
            if(centerOnPlayer) {    
               centerMapOnPlayer();                
            } else {
               checkBounds();
            }
            lastChunksImage = new Image();
            lastChunksImage.onload = function () {          
                // Draw the image on the canvas
                var ctx = getContext();         
                updateUI();
            }    
            lastChunksImage.src=getMapDataUrl();
        },
        error : function(jqXHR, textStatus, errorThrown) {
                  handleError(errorThrown);
               }
      });
}

function handleError(error) {
    if(error=="" || error==null) {
       error = JML10N.error_world_not_connected;
    } else if(error.substring && error.substring(1,8)=="JMERR09") {
       error = JML10N.error_world_not_opened;
    }
    document.body.style.backgroundColor = "#000";
    clearInterval(refreshDataTimer);
    sizeMap();
    var creeper = new Image();
    creeper.onload=function() {
        ctx.drawImage(creeper, getCanvasWidth()/2-68, getCanvasHeight()/2-110);
        creeper.onload=null;
    };
    creeper.src="/creeper.jpg";
    var ctx = getContext();
    ctx.globalAlpha=1;
    ctx.fillStyle = "red";
    ctx.font = "bold 16px Arial";
    ctx.textAlign="center";
    ctx.fillText(error, getCanvasWidth()/2, (getCanvasHeight()/2) + 10);
    
    
    // Remove others
	$.each(otherImages, function(index, img) { 
		document.body.removeChild(img);
	});
	
    setTimeout(initWorld,5000);
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
   
   // update world info
   updateWorldInfo();
   
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
    if(worldProviderType==-1) {
 	   table +="<tr><th colspan='2' style='color:#a00;font-weight:bold;text-align:center'>"+ JML10N.world_name_nether +"</th></tr>";
    } else if(worldProviderType==1) {
 	   table +="<tr><th colspan='2' style='color:#a00;font-weight:bold;text-align:center'>"+ JML10N.world_name_end +"</th></tr>";
    } 
    
    table += "<tr><th colspan='2'>";
    if(worldProviderType==0) {
       if(worldTime<12000) {
     	  table += JML10N.sunset_begins;
       } else if(worldTime<13800) {
     	  table += JML10N.night_begins;
       } else if(worldTime<22200) {
     	  table += JML10N.sunrise_begins;
       } else if(worldTime<23999) {
     	  table += JML10N.day_begins;
       } 
    }
    table += "</th></tr>";
    
    /*
    0 is the start of daytime, 12000 is the start of sunset, 13800 is the start of nighttime, 22200 is the start of sunrise, and 24000 is daytime again. 
   */
   var allsecs = worldTime/20;
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
   
   if(worldProviderType==0) {
	   table += "<tr><th>" + JML10N.biome_title + "</th><td>" + player.biome + "</td></tr>";
   }
   
   table+="</tbody></table>";
   //console.log(table);
   
   $("#worldInfo").attr("innerHTML", table);
}

// Draw the player location
function drawPlayer() {

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
	
	if(showMonsters==false) return;

    $.each(mobs, function(index, mob) { 
       var x = getScaledChunkX(mob.posX/16);
       var z = getScaledChunkZ(mob.posZ/16);
       
       if(x>=0 &&
          x<=getCanvasWidth() &&  
          z>=0 &&
          z<=getCanvasWidth()) {
      
           var ctx = getContext();
           ctx.globalAlpha=.85;
           ctx.strokeStyle = "#f00";
           ctx.lineWidth = 2;
           ctx.beginPath();
           var radius = 16;
           if(mob.type=='Ghast' || mob.type=='Dragon') {
        	   radius = 24;
           } 
           ctx.arc(x, z, radius, 0, Math.PI*2, true); 
           ctx.stroke();
           ctx.globalAlpha=1.0;
           
           var type = mob.type;       
           var mobImage = mobImages[mob.type];
           if(mobImage) {
                ctx.drawImage(mobImage, x-radius, z-radius, radius*2,radius*2);
           }
       }
    });
    
}

//Draw the location of other players
function drawOthers() {

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
   
   // Touch events
   if(document.addEventListener) {
       document.addEventListener("touchstart", touchHandler, true);
       document.addEventListener("touchmove", touchHandler, true);
       document.addEventListener("touchend", touchHandler, true);
       document.addEventListener("touchcancel", touchHandler, true);
   } 
   
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

// From http://ross.posterous.com/2008/08/19/iphone-touch-events-in-javascript/
function touchHandler(event)
{
    var touches = event.changedTouches,
        first = touches[0],
        type = "";
         switch(event.type)
    {
        case "touchstart": type="mousedown"; break;
        case "touchmove":  type="mousemove"; break;        
        case "touchend":   type="mouseup";   break;
        default: return;
    }

    var simulatedEvent = document.createEvent("MouseEvent");
    simulatedEvent.initMouseEvent(type, true, true, window, 1, 
                              first.screenX, first.screenY, 
                              first.clientX, first.clientY, false, 
                              false, false, false, 0/*left*/, null);

    first.target.dispatchEvent(simulatedEvent);
    event.preventDefault();
}

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