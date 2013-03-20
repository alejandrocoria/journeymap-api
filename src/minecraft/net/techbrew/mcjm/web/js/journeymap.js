"use strict";
/**
 * JourneyMap web client 
 * http://journeymap.techbrew.net 
 * Copyright (C) 2011-2013.
 * Mark Woodman / TechBrew.net All rights reserved. 
 * May not be modified or distributed without express written consent.
 */
var JourneyMap = (function() {
    var mapScale = 4;
    var minMapScale = 1;
    var maxMapScale = 10;
    var smoothing = false;
    var mapBounds = {};

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

    var userPanning = false;
    var mx, my;
    var msx, msy;

    var tempMapImage;
    var latestMapImage;

    var playerImage;
    var neutralMobImage;
    var hostileMobImage;
    var petMobImage;
    var otherPlayerMobImage;

    var mobImages = {};
    var otherImages = [];
    var chunkScale = mapScale * 16;

    var JmIcon;
    var halted = false;
    var uiInitialized = false;
    var versionChecked = false;
    var updatingMap = false;
    var drawingMap = false;

    var playerOverrideMap = false;

    var timerId = null;

    var JM = {
	debug : false,
	messages : null,
	game : null,
	mobs : [],
	animals : [],
	players : [],
	villagers : []
    };

    /**
     * JQuery add-on for disableSelection
     */
    (function($) {

	$.fn.disableSelection = function() {

	    return this.attr('unselectable', 'on').css('user-select', 'none')
		    .on('selectstart', false); // ie
	};
    })(jQuery);

    /** Delay helper * */
    var delay = (function() {

	var timer = 0;
	return function(callback, ms) {

	    clearTimeout(timer);
	    timer = setTimeout(callback, ms);
	};
    })();

    /**
     * Load I18N messages once.
     * 
     * @returns
     */
    var initMessages = function() {

	if (JM.debug)
	    console.log(">>> " + "initMessages");

	$.ajax({
	    url : "/data/messages",
	    dataType : "jsonp",
	    contentType : "application/javascript; charset=utf-8",
	    async : false
	}).fail(handleError).done(function(data, textStatus, jqXHR) {

	    JM.messages = data;
	    initGame();
	});
    }

    /**
     * Load Game info once.
     * 
     * @returns
     */
    var initGame = function() {

	if (JM.debug)
	    console.log(">>> " + "initGame");

	$
		.ajax({
		    url : "/data/game",
		    dataType : "jsonp",
		    contentType : "application/javascript; charset=utf-8",
		    async : false
		})
		.fail(handleError)
		.done(
			function(data, textStatus, jqXHR) {

			    JM.game = data;

			    // Update UI with game info
			    $("#version").html(JM.game.jm_version);
			    if (JM.game.latest_journeymap_version > JM.game.jm_version) {
				// TODO: This is sloppy L10N
				$("#versionButton")
					.attr(
						"title",
						JM.messages.update_available
							+ " : JourneyMap "
							+ JM.game.latest_journeymap_version
							+ " for Minecraft "
							+ JM.world.latest_minecraft_version);
				$("#versionButton")
					.css("visibility", "visible");
			    }

			    // GA event
			    if (versionChecked != true) {
				_gaq.push([ '_setCustomVar', 1, 'jm_version',
					JM.game.jm_version, 2 ]);
				_gaq.push([ '_trackEvent', 'Client',
					'CheckVersion', JM.game.jm_version ]);
				versionChecked = true;
			    }

			    // Splash
			    if (!JmIcon) {
				JmIcon = new Image();
				JmIcon.src = "/ico/apple-touch-icon-144x144-precomposed.png";
				JmIcon.title = "JourneyMap";
				JmIcon.style.position = "absolute";
				JmIcon.style.visibility = "visible";
				JmIcon.style.left = ($(window).width() / 2 - 72)
					+ "px";
				JmIcon.style.top = ($(window).height() / 2 - 160)
					+ "px";
				JmIcon.style.zIndex = 100;
				document.body.appendChild(JmIcon);
				$(JmIcon).delay(1000).fadeOut(1000);
			    }

			    // Init UI
			    initUI();
			});
    }

    /**
     * Initialize UI once.
     * 
     * @returns
     */
    var initUI = function() {

	if (JM.debug)
	    console.log(">>> " + "initUI");

	// Ensure messages are loaded first.
	if (!JM.messages) {
	    throw ("initUI called without JM.messages"); // shouldn't happen
	}

	// Init canvas
	$("#mapCanvas").offset({
	    top : 0,
	    left : 0
	}).mousedown(myDown).mouseup(myUp).dblclick(myDblClick)
		.disableSelection();

	// Init canvases
	canvas = $("#mapCanvas")[0];
	bgCanvas = $(document.createElement("canvas")).attr('id', 'bgCanvas')[0];
	fgCanvas = $(document.createElement("canvas")).attr('id', 'fgCanvas')[0];

	sizeMap();

	// Set page language, although at this point it may be too late to
	// matter.
	$('html').attr('lang', JM.messages.locale.split('_')[0]);

	// Set RSS feed title
	$("link #rssfeed").attr("title", JM.messages.rss_feed_title);

	// Init toolbar button tooltips
	$("#dayButton").html(JM.messages.day_button_title).attr("title",
		JM.messages.day_button_desc).click(function() {

	    playerOverrideMap = true;
	    setMapType('day');
	});

	$("#nightButton").html(JM.messages.night_button_title).attr("title",
		JM.messages.night_button_desc).click(function() {

	    playerOverrideMap = true;
	    setMapType('night');
	});

	$("#followButton").html(JM.messages.follow_button_title).attr("title",
		JM.messages.follow_button_desc).click(function() {

	    setCenterOnPlayer(!centerOnPlayer);
	    refreshMap();
	});

	$("#caveButton").html(JM.messages.cave_button_title).attr("title",
		JM.messages.cave_button_desc).click(function() {

	    setShowCaves(!showCaves);
	});

	$("#monstersButton").attr(
		"title",
		"<b>" + JM.messages.monsters_button_title + "</b><br/>"
			+ JM.messages.monsters_button_desc);

	$("#saveButton").attr("title", JM.messages.save_button_title).click(
		function() {

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
	    width : '1em',
	    textAlign : 'center',
	    position : 'absolute',
	    top : 0,
	    left : 0
	}).hide();

	// Slider
	$("#slider-vertical").slider({
	    orientation : "vertical",
	    range : "min",
	    title : JM.messages.zoom_slider_name,
	    min : minMapScale,
	    max : maxMapScale,
	    value : mapScale,
	    slide : function(event, ui) {

		tooltip.text(ui.value);
		setZoom(ui.value);
	    }
	}).find(".ui-slider-handle").append(tooltip).hover(function() {

	    tooltip.show()
	}, function() {

	    tooltip.hide()
	});

	$("#worldInfo").hide();
	$("#worldNameTitle").html(JM.messages.worldname_title);
	$("#worldTimeTitle").html(JM.messages.worldtime_title);
	$("#playerBiomeTitle").html(JM.messages.biome_title);
	$("#playerLocationTitle").html(JM.messages.location_title);
	$("#playerElevationTitle").html(JM.messages.elevation_title);

	$("#showMenuText").html(JM.messages.show_menu_text);
	// $("#smoothPixelsMenuItem").html(JM.messages.smooth_pixels_menu_item);
	$("#animalsMenuItem").html(JM.messages.animals_menu_item);
	$("#petsMenuItem").html(JM.messages.pets_menu_item);
	$("#mobsMenuItem").html(JM.messages.mobs_menu_item);
	$("#villagersMenuItem").html(JM.messages.villagers_menu_item);
	$("#playersMenuItem").html(JM.messages.players_menu_item);

	$("#showMenu").click(function(event) {
	    event.stopPropagation();
	});

	// Test canvas to see if smooth scaling can be toggled
	var ctx = bgCanvas.getContext("2d");

	$("#checkShowAnimals").prop('checked', showAnimals)
	$("#checkShowAnimals").click(function(event) {
	    showAnimals = (this.checked === true);
	    drawMap();
	});

	$("#checkShowPets").prop('checked', showPets)
	$("#checkShowPets").click(function(event) {
	    event.stopPropagation();
	    showPets = (this.checked === true);
	    drawMap();
	});

	$("#checkShowMobs").prop('checked', showMobs)
	$("#checkShowMobs").click(function() {
	    showMobs = (this.checked === true);
	    drawMap();
	});

	$("#checkShowVillagers").prop('checked', showVillagers)
	$("#checkShowVillagers").click(function() {
	    showVillagers = (this.checked === true);
	    drawMap();
	});

	$("#checkShowPlayers").prop('checked', showPlayers)
	$("#checkShowPlayers").click(function() {
	    showPlayers = (this.checked === true);
	    drawMap();
	});

	// Init images
	initImages();

	// Disable selection events by default
	$("*").on('selectstart dragstart', function(evt) {
	    evt.preventDefault();
	    return false;
	});

	// Mouse events for canvas
	$(canvas).mousewheel(myMouseWheel);

	// Keyboard events
	$(document).keypress(myKeyPress);

	// Resize events will update the map
	$(window).resize(function() {

	    delay(function() {

		sizeMap();
		refreshMap();
	    }, 200);
	});

	// Set flag so this function doesn't get called twice
	uiInitialized = true;

	// Continue
	initWorld();

    }

    /**
     * Initialize World.
     * 
     * @returns
     */
    var initWorld = function() {

	if (JM.debug)
	    console.log(">>> " + "initWorld");

	// Reset state
	halted = false;
	updatingMap = false;
	clearTimer();
	setCenterOnPlayer(true);

	// Ensure the map is sized
	sizeMap();

	var finishUI = function() {

	    $(".jmtoggle").each(function() {

		$(this).show()
	    });
	    $("#slider-vertical").show();

	}

	queryServer(finishUI);

    }

    /**
     * Preload any images as needed.
     */
    var initImages = function() {

	if (JM.debug)
	    console.log(">>> " + "initImages");

	// Init player marker
	if (!playerImage) {
	    playerImage = new Image();
	    playerImage.src = "/img/locator-player.png";
	    playerImage.width = 64;
	    playerImage.height = 64;
	}

	if (!neutralMobImage) {
	    neutralMobImage = new Image();
	    neutralMobImage.src = "/img/locator-neutral.png";
	    neutralMobImage.width = 64;
	    neutralMobImage.height = 64;
	}

	if (!hostileMobImage) {
	    hostileMobImage = new Image();
	    hostileMobImage.src = "/img/locator-hostile.png";
	    hostileMobImage.width = 64;
	    hostileMobImage.height = 64;
	}

	if (!petMobImage) {
	    petMobImage = new Image();
	    petMobImage.src = "/img/locator-pet.png";
	    petMobImage.width = 64;
	    petMobImage.height = 64;
	}

	if (!otherPlayerMobImage) {
	    otherPlayerMobImage = new Image();
	    otherPlayerMobImage.src = "/img/locator-other.png";
	    otherPlayerMobImage.width = 64;
	    otherPlayerMobImage.height = 64;
	}

    }

    var enableSmoothing = function(ctx, smoothing) {

	$(ctx).prop('imageSmoothingEnabled', smoothing)
	$(ctx).prop('mozImageSmoothingEnabled', smoothing)
	$(ctx).prop('webkitImageSmoothingEnabled', smoothing)

    }

    /**
     * Invoke saving map file
     */
    var saveMapImage = function() {

	document.location = getMapDataUrl().replace("/map.png", "/save");
    }

    /**
     * Set map scale
     */
    var setScale = function(newScale) {

	mapScale = newScale;
	chunkScale = mapScale * 16;
    }

    /**
     * Ensure canvas is sized as needed
     */
    var sizeMap = function() {

	if (JM.debug)
	    console.log(">>> " + "sizeMap");

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

	if (JM.player) {
	    centerMapOnChunk(Math.round(JM.player.chunkCoordX), Math
		    .round(JM.player.chunkCoordZ));
	}
    }

    function centerMapOnChunk(chunkX, chunkZ) {

	var maxChunksWide = Math.ceil(getCanvasWidth() / mapScale / 16);
	var maxChunksHigh = Math.ceil(getCanvasHeight() / mapScale / 16);

	mapBounds.x1 = chunkX - Math.round(maxChunksWide / 2) + 1;
	mapBounds.z1 = chunkZ - Math.round(maxChunksHigh / 2) + 1;

	checkBounds();
    }

    function checkBounds() {

	// determine how many chunks we can display
	var maxChunksWide = Math.ceil(getCanvasWidth() / mapScale / 16);
	var maxChunksHigh = Math.ceil(getCanvasHeight() / mapScale / 16);
	mapBounds.x2 = mapBounds.x1 + maxChunksWide;
	mapBounds.z2 = mapBounds.z1 + maxChunksHigh;
    }

    function setMapType(mapType) {

	if (mapType === "day") {
	    if (showLight === false)
		return;
	    showLight = false;
	    $("#header").removeClass("navbar-inverse");
	    $("#dayButton").addClass("active");
	    $("#nightButton").removeClass("active");

	} else if (mapType === "night") {
	    if (showLight === true)
		return;
	    showLight = true;
	    $("#header").addClass("navbar-inverse");
	    $("#dayButton").removeClass("active");
	    $("#nightButton").addClass("active");
	} else {
	    if (JM.debug)
		console.log(">>> " + "Error: Can't set mapType: " + mapType);
	}

	if (JM.player.underground !== true) {
	    refreshMap();
	}

    }

    function setCenterOnPlayer(onPlayer) {

	centerOnPlayer = onPlayer;
	if (onPlayer === true) {
	    centerMapOnPlayer();
	    $("#followButton").addClass("active");
	} else {
	    $("#followButton").removeClass("active");
	}
    }

    function setShowCaves(show) {

	if (show === showCaves)
	    return;
	showCaves = show;

	if (showCaves === true) {
	    $("#caveButton").addClass("active");
	} else {
	    $("#caveButton").removeClass("active");
	}

	if (JM.player.underground === true) {
	    refreshMap();
	}

    }

    // //////////// DATA ////////////////////

    var queryServer = function(callback) {

	if (halted === true)
	    return;

	fetchData("/data/all", function(data) {

	    JM.animals = data.animals;
	    JM.mobs = data.mobs;
	    JM.player = data.player;
	    JM.players = data.players;
	    JM.villagers = data.villagers;
	    JM.world = data.world;

	    // Update UI
	    $("#playerBiome").html(JM.player.biome);
	    $("#playerLocation").html(JM.player.posX + "," + JM.player.posZ);

	    $("#playerElevationTitle").attr('title',
		    JM.messages.elevation_title + " " + (JM.player.posY >> 4));
	    $("#playerElevation").html(
		    JM.player.posY + "&nbsp;(" + (JM.player.posY >> 4) + ")");

	    // Interpret data
	    var dimensionName = "";
	    if (JM.world.dimension === -1) {
		dimensionName = JM.messages.world_name_nether;
	    } else if (JM.world.dimension === 1) {
		dimensionName = JM.messages.world_name_end;
	    }

	    var timeCycleInfo = "";
	    if (JM.world.dimension === 0) {
		if (JM.world.time < 12000) {
		    timeCycleInfo = JM.messages.sunset_begins;
		} else if (JM.world.time < 13800) {
		    timeCycleInfo = JM.messages.night_begins;
		} else if (JM.world.time < 22200) {
		    timeCycleInfo = JM.messages.sunrise_begins;
		} else if (JM.world.time < 23999) {
		    timeCycleInfo = JM.messages.day_begins;
		}
	    }

	    // 0 is the start of daytime, 12000 is the start of sunset, 13800 is
	    // the
	    // start of nighttime, 22200 is the start of sunrise, and 24000 is
	    // daytime again.
	    var allsecs = JM.world.time / 20;
	    var mins = Math.floor(allsecs / 60);
	    var secs = Math.ceil(allsecs % 60);
	    if (mins < 10)
		mins = "0" + mins;
	    if (secs < 10)
		secs = "0" + secs;
	    var currentTime = mins + ":" + secs;

	    // Update UI elements
	    $("#worldName").html(unescape(JM.world.name).replace("\\+", " "));
	    $("#worldTime").html(currentTime);
	    $("#worldInfo").show();

	    // Set map type based on time
	    if (playerOverrideMap != true) {
		if (JM.world.dimension === 0 && JM.player
			&& JM.player.underground != true) {
		    if (JM.world.time < 13800) {
			setMapType('day');
		    } else {
			setMapType('night');
		    }
		}
	    }

	    if (halted === true)
		return;

	    // Draw the map
	    refreshMapImage(function() {

		if (halted === true)
		    return;

		drawMap();

		if (timerId === null) {
		    timerId = setInterval(queryServer, Math.max(1000,
			    JM.game.browser_poll));
		}
		if (callback) {
		    callback();
		}
	    });

	});

    }

    /**
     * Fetch JsonP data. Generic error handling, callback invoked on success
     */
    var fetchData = function(dataUrl, callback) {

	if (JM.debug)
	    console.log(">>> " + "fetchData " + dataUrl);

	$.ajax({
	    url : dataUrl,
	    dataType : "jsonp"
	}).fail(handleError).done(callback);
    }

    /**
     * Get the url for the current map state
     */
    var getMapDataUrl = function() {

	// Center map or check bounds to ensure correct map image retrieved
	if (centerOnPlayer === true) {
	    centerMapOnPlayer();
	} else {
	    checkBounds();
	}

	var width = getCanvasWidth();
	var height = getCanvasHeight();
	var mapType = (JM.player && JM.player.underground === true && showCaves === true) ? "underground"
		: (showLight === true ? "night" : "day");
	var dimension = (JM.player.dimension);
	var depth = (JM.player && JM.player.chunkCoordY != undefined) ? JM.player.chunkCoordY
		: 4;
	var request = "/map.png?mapType=" + mapType + "&dimension=" + dimension
		+ "&depth=" + depth + "&x1=" + mapBounds.x1 + "&z1="
		+ mapBounds.z1 + "&x2=" + mapBounds.x2 + "&z2=" + mapBounds.z2
		+ "&width=" + width + "&height=" + height;
	return request;
    }

    /**
     * Refresh the map image.
     * 
     * @param callback
     *                Optional function to call when this is complete.
     */
    var refreshMapImage = function(callback) {

	if (JM.debug)
	    console.log(">>> " + "refreshMapImage");

	if (!tempMapImage) {
	    tempMapImage = $(document.createElement('img')).error(
		    function(error) {

			console.log("refreshMapImage error: ", error)
		    });

	}

	$(tempMapImage).unbind('load');
	$(tempMapImage).bind(
		'load',
		function() {

		    if (!this.complete
			    || typeof this.naturalWidth == "undefined"
			    || this.naturalWidth === 0) {
			if (JM.debug)
			    console.log(">>> " + 'Map image incomplete!');
		    } else {
			latestMapImage = this;
		    }
		    if (callback) {
			callback();
		    }
		});
	$(tempMapImage).attr('src', getMapDataUrl());

    }

    /**
     * Force immediate update
     */
    var refreshMap = function() {
	clearTimer();
	queryServer();
    }

    /**
     * Clear the timer
     */
    var clearTimer = function() {
	if (timerId !== null) {
	    clearInterval(timerId);
	    timerId = null;
	}
    }

    // Ajax request got an error from the server
    var handleError = function(data, error, jqXHR) {

	if (JM.debug)
	    console.log(">>> " + "handleError");

	// Secondary errors will be ignored
	if (halted === true)
	    return;

	// Clear the timer
	clearTimer();

	console.log("Server returned error: " + data.status + ": " + jqXHR);

	$(".jmtoggle").each(function() {

	    $(this).hide()
	});
	$("#worldInfo").hide();
	$("#slider-vertical").hide();

	var displayError;
	if (data.status === 503 || data.status === 0) {
	    displayError = JM.messages.error_world_not_opened;
	} else {
	    displayError = "";
	}

	// Remove others
	$.each(otherImages, function(index, img) {

	    document.body.removeChild(img);
	});

	// Format UI
	$('body').css('backgroundColor', mapBackground);
	sizeMap();

	if (canvas) {
	    var ctx = canvas.getContext("2d");

	    if (!JmIcon) {
		JmIcon = new Image();
		JmIcon.onload = function() {

		    ctx.drawImage(JmIcon, getCanvasWidth() / 2 - 72,
			    getCanvasHeight() / 2 - 160);
		    JmIcon.onload = null;
		};
		JmIcon.src = "/ico/apple-touch-icon-144x144-precomposed.png";
	    } else {
		ctx.drawImage(JmIcon, getCanvasWidth() / 2 - 72,
			getCanvasHeight() / 2 - 160);
	    }

	    ctx.globalAlpha = 1;
	    ctx.fillStyle = "red";
	    ctx.font = "bold 16px Arial";
	    ctx.textAlign = "center";
	    ctx.fillText(displayError, getCanvasWidth() / 2,
		    (getCanvasHeight() / 2) + 10);
	}

	// Restart in 5 seconds
	if (!halted) {
	    halted = true;
	    if (JM.debug)
		console.log(">>> " + "Will re-check game state in 5 seconds.");
	    setTimeout(function() {

		halted = false;

		if (!JM.messages) {
		    initMessages();
		} else if (uiInitialized != true) {
		    initUI();
		} else {
		    initWorld();
		}

	    }, 5000);
	}
    }

    // ////////////DRAW ////////////////////

    // Draw the map
    var drawMap = function() {

	if (JM.debug)
	    console.log(">>> " + "drawMap");

	if (userPanning === true) {
	    if (JM.debug)
		console.log(">>> " + "Can't draw while userPanning");
	    return false;
	}

	if (drawingMap === true) {
	    if (JM.debug)
		console.log(">>> " + "Avoided concurrent drawMap()");
	    return false;
	}

	drawingMap = true;

	// Ensure map centered
	if (centerOnPlayer === true) {
	    centerMapOnPlayer();
	} else {
	    checkBounds();
	}

	// Get canvas dimensions
	var canvasWidth = getCanvasWidth();
	var canvasHeight = getCanvasHeight();

	// Size working canvases
	$(bgCanvas).attr('width', canvasWidth).attr('height', canvasHeight);
	$(fgCanvas).attr('width', canvasWidth).attr('height', canvasHeight);

	// set background color
	if (showLight === true || JM.player.underground === true) {
	    mapBackground = '#000';
	} else {
	    mapBackground = '#222';
	}

	// Draw background map image
	drawBackgroundCanvas(canvasWidth, canvasHeight);

	// clear foreground canvas
	var ctx = fgCanvas.getContext("2d");
	ctx.clearRect(0, 0, canvasWidth, canvasHeight);

	// mobs
	drawMobs(canvasWidth, canvasHeight);

	// other players
	drawMultiplayers(canvasWidth, canvasHeight);

	// player
	drawPlayer();

	// Now put on the visible canvas
	ctx = canvas.getContext("2d");
	if (ctx.drawImage) {
	    ctx.globalAlpha = 1.0;
	    ctx.drawImage(bgCanvas, 0, 0, canvasWidth, canvasHeight);
	    ctx.drawImage(fgCanvas, 0, 0, canvasWidth, canvasHeight);
	}

	drawingMap = false;

    }

    // Draw the player icon
    var drawPlayer = function(canvasWidth, canvasHeight) {

	if (JM.debug)
	    console.log(">>> " + "drawPlayer");

	if (userPanning === true)
	    return;

	if (!canvasWidth || !canvasHeight) {
	    canvasWidth = getCanvasWidth();
	    canvasHeight = getCanvasHeight();
	}

	var player = JM.player;
	var x = getScaledChunkX(player.posX / 16) - (mapScale / 2);
	var z = getScaledChunkZ(player.posZ / 16) - (mapScale / 2);

	if (x >= 0 && x <= canvasWidth && z >= 0 && z <= canvasHeight) {

	    var ctx = fgCanvas.getContext("2d");
	    var radius = playerImage.width / 2;

	    if (ctx.drawImage) {
		ctx.save();
		ctx.translate(x, z);
		ctx.rotate(player.heading);
		ctx.translate(-radius, -radius);
		ctx.drawImage(playerImage, 0, 0);
		ctx.restore();
	    }

	}

    }

    // Draw the location of mobs
    var drawMobs = function(canvasWidth, canvasHeight) {

	if (JM.debug)
	    console.log(">>> " + "drawMobs");

	if (!canvasWidth || !canvasHeight) {
	    canvasWidth = getCanvasWidth();
	    canvasHeight = getCanvasHeight();
	}

	var ctx = fgCanvas.getContext("2d");

	if (showMobs === true && JM.mobs) {
	    $.each(JM.mobs, function(index, mob) {

		drawEntity(ctx, mob, canvasWidth, canvasHeight);
	    });
	}

	if ((showAnimals === true || showPets === true) && JM.animals) {
	    $.each(JM.animals, function(index, mob) {

		drawEntity(ctx, mob, canvasWidth, canvasHeight);
	    });
	}

	if (showVillagers === true && JM.villagers) {
	    $.each(JM.villagers, function(index, mob) {

		drawEntity(ctx, mob, canvasWidth, canvasHeight);
	    });
	}

    }

    // Draw the location of an entity
    var drawEntity = function(ctx, mob, canvasWidth, canvasHeight) {

	var x = getScaledChunkX(mob.posX / 16) - (mapScale * 1.5);
	var z = getScaledChunkZ(mob.posZ / 16) - (mapScale * 1.5);

	if (x >= 0 && x <= canvasWidth && z >= 0 && z <= canvasHeight) {

	    var mobLocator;

	    if (mob.hostile !== true && mob.owner
		    && mob.owner === JM.player.username) {
		if (showPets === false)
		    return;
		mobLocator = petMobImage;
		ctx.strokeStyle = "#0000ff";
	    } else if (mob.hostile !== true) {
		if (showAnimals === false && !(mob.type === 'Villager'))
		    return;
		mobLocator = neutralMobImage;
		ctx.strokeStyle = "#cccccc";
	    } else {
		mobLocator = hostileMobImage;
		ctx.strokeStyle = "#ff0000";
	    }

	    ctx.lineWidth = 2;
	    ctx.beginPath();
	    var radius = 32;
	    var type = mob.type;
	    if (type === 'Ghast' || type === 'Dragon' || type === 'Wither') {
		radius = 48;
	    }

	    if (ctx.drawImage) {
		var locRadius = mobLocator.width / 2;
		ctx.save();
		ctx.globalAlpha = .45;
		ctx.translate(x, z);
		ctx.rotate(mob.heading);
		ctx.translate(-locRadius, -locRadius);
		ctx.drawImage(mobLocator, 0, 0);
		ctx.restore();
	    }

	    // ctx.arc(x, z, radius, 0, Math.PI * 2, true);
	    // ctx.stroke();

	    ctx.globalAlpha = 1.0;

	    // Get pre-loaded image, or lazy-load as needed
	    var mobImage = mobImages[type];
	    if (!mobImage) {
		mobImage = new Image();
		mobImage['class'] = 'mobImage';
		$(mobImage).one('error', function() {

		    this.src = 'img/entity/unknown.png';
		});
		mobImage.src = 'img/entity/' + type + '.png';
		mobImages[type] = mobImage;
	    }

	    // Draw if image exists
	    if (mobImage.height > 0) {
		radius = mobImage.width / 2;
		ctx.save();
		ctx.translate(x - radius, z - radius);
		ctx.drawImage(mobImage, 0, 0, radius * 2, radius * 2);
		ctx.restore();
	    }
	}
    }

    // Draw the location of other players
    var drawMultiplayers = function(canvasWidth, canvasHeight) {

	if (JM.debug)
	    console.log(">>> " + "drawMultiplayers");

	var others = JM.players;
	if (!others)
	    return;

	if (!canvasWidth || !canvasHeight) {
	    canvasWidth = getCanvasWidth();
	    canvasHeight = getCanvasHeight();
	}

	// Remove old
	$.each(otherImages, function(index, img) {

	    document.body.removeChild(img);
	});
	otherImages = new Array();

	var ctx = fgCanvas.getContext("2d");

	// Make new
	$.each(others, function(index, other) {

	    var x = getScaledChunkX(other.posX / 16) - (mapScale / 2);
	    var z = getScaledChunkZ(other.posZ / 16) - (mapScale / 2);
	    if (other.username != JM.player.name) {
		if (x >= 0 && x <= canvasWidth && z >= 0 && z <= canvasHeight) {

		    // Draw locator
		    var locRadius = otherPlayerMobImage.width / 2;
		    ctx.save();
		    ctx.globalAlpha = .8;
		    ctx.translate(x, z);
		    ctx.rotate(other.heading);
		    ctx.translate(-locRadius, -locRadius);
		    ctx.drawImage(otherPlayerMobImage, 0, 0);
		    ctx.restore();

		    // Draw label
		    ctx.globalAlpha = 1.0;
		    ctx.font = "bold 12px Arial";
		    ctx.textAlign = "center";
		    ctx.fillStyle = "#000";
		    ctx.fillText(other.username, x - 2, z + 28);
		    ctx.fillText(other.username, x + 2, z + 32);
		    ctx.fillStyle = "#0f0";
		    ctx.fillText(other.username, x, z + 30);

		    // show image
		    var otherImage = new Image();
		    otherImage.src = "/img/entity/Player.png";
		    otherImage['class'] = 'mobImage';
		    otherImage.title = other.username;
		    otherImage.style.position = "absolute";
		    otherImage.style.visibility = "visible";
		    otherImage.style.height = "20px";
		    otherImage.style.width = "20px";
		    otherImage.style.left = (x - 10) + "px";
		    otherImage.style.top = (z - 10) + "px";
		    otherImage.style.zIndex = 1;
		    document.body.appendChild(otherImage);
		    otherImages.push(otherImage);
		}
	    }
	});

    }

    // Draw the map image to the background canvas
    var drawBackgroundCanvas = function(canvasWidth, canvasHeight) {

	if (JM.debug)
	    console.log(">>> " + "drawBackgroundCanvas");

	// draw the png to the canvas
	if (latestMapImage !== undefined) {

	    var width = latestMapImage.width;
	    var height = latestMapImage.height;

	    if (!canvasWidth || !canvasHeight) {
		canvasWidth = getCanvasWidth();
		canvasHeight = getCanvasHeight();
	    }

	    // Fill background
	    var ctx = bgCanvas.getContext("2d");
	    enableSmoothing(ctx, smoothing);
	    ctx.fillStyle = mapBackground;
	    ctx.fillRect(0, 0, canvasWidth, canvasHeight);

	    // Draw map image
	    ctx.drawImage(latestMapImage, 0, 0, width * mapScale, height
		    * mapScale);

	}
    }

    function getScaledChunkX(chunkX) {

	var xOffset = ((mapBounds.x1) * chunkScale);
	return (chunkX * chunkScale) - xOffset;
    }

    function getScaledChunkZ(chunkZ) {

	var zOffset = ((mapBounds.z1) * chunkScale);
	return (chunkZ * chunkScale) - zOffset;
    }

    /**
     * Update mouse coordinates based on the last event.
     */
    function getMouse(event) {

	if (!event) { /* For IE. */
	    event = window.event;
	}
	mx = event.pageX;
	my = event.pageY;
    }

    function myDown(e) {

	scrollCanvas(e);
    }

    function myUp(e) {

	getMouse(e);

	var mouseDragX = (mx - msx);
	var mouseDragY = (my - msy);
	if (mouseDragX === 0 && mouseDragY === 0) {
	    userPanning = false;
	}
	if (userPanning === true) {

	    var xOffset = Math.floor(mouseDragX / chunkScale);
	    var zOffset = Math.floor(mouseDragY / chunkScale);

	    mapBounds.x1 = mapBounds.x1 - xOffset - 1;
	    mapBounds.z1 = mapBounds.z1 - zOffset - 1;
	    userPanning = false;

	}
    }

    function myDblClick(e) {

	getMouse(e);
    }

    function myKeyPress(e) {

	var key = (e) ? e.which : e.keyCode;
	switch (String.fromCharCode(key)) {
	case '-':
	    zoom('out');
	    break;
	case '=':
	    zoom('in');
	    break;
	case 'w':
	case 'W':
	    moveCanvas('up');
	    break;
	case 'a':
	case 'A':
	    moveCanvas('right');
	    break;
	case 's':
	case 'S':
	    moveCanvas('down');
	    break;
	case 'd':
	case 'D':
	    moveCanvas('left');
	    break;
	}
    }

    function myMouseWheel(event, delta) {

	if (halted === true)
	    return;

	if (delta > 0) {
	    zoom('in');
	} else if (delta < 0) {
	    zoom('out');
	}

    }

    function zoom(dir) {

	if (dir === 'in' && mapScale < maxMapScale) {
	    setZoom(mapScale + 1);
	} else if (dir === 'out' && mapScale > minMapScale) {
	    setZoom(mapScale - 1);
	}
    }

    function setZoom(scale) {

	var centerChunkX = Math.floor(getCanvasWidth() / chunkScale / 2)
		+ mapBounds.x1;
	var centerChunkZ = Math.floor(getCanvasHeight() / chunkScale / 2)
		+ mapBounds.z1;

	$("#slider-vertical").slider("value", scale);
	setScale(scale);
	centerMapOnChunk(centerChunkX, centerChunkZ);
	refreshMap();
    }

    function scrollCanvas(e) {

	if (halted === true)
	    return;

	$('body').css('cursor', 'move');
	userPanning = true;
	getMouse(e);
	msx = mx;
	msy = my;

	document.onmousemove = scrollingCanvas;
    }

    var scrollingCanvas = function(e) {

	if (userPanning === true) {
	    $('body').css('cursor', 'move');
	    getMouse(e);

	    var mouseDragX = (mx - msx);
	    var mouseDragY = (my - msy);
	    var xOffset = Math.ceil(mouseDragX / chunkScale);
	    var zOffset = Math.ceil(mouseDragY / chunkScale);

	    if (Math.abs(xOffset) > 0 || Math.abs(zOffset) > 0) {
		setCenterOnPlayer(false);
	    }

	    var canvasWidth = getCanvasWidth();
	    var canvasHeight = getCanvasHeight();
	    var drawX = xOffset * chunkScale;
	    var drawZ = zOffset * chunkScale;

	    // Clear canvas
	    var ctx = canvas.getContext("2d");
	    ctx.globalAlpha = 1;
	    ctx.fillStyle = mapBackground;
	    ctx.fillRect(0, 0, canvasWidth, canvasHeight);

	    if (drawingMap !== true) {
		ctx.drawImage(bgCanvas, drawX, drawZ);
		ctx.drawImage(fgCanvas, drawX, drawZ);
		drawPlayer(canvasWidth, canvasHeight, drawX, drawZ);
	    }

	} else {
	    if (JM.debug)
		console.log(">>> " + "scrollingCanvas done");

	    document.onmousemove = null;
	    $('body').css('cursor', 'default');
	}
    }

    function moveCanvas(dir) {

	switch (dir) {
	case 'left':
	    mapBounds.x1++;
	    break;
	case 'right':
	    mapBounds.x1--;
	    break;
	case 'up':
	    mapBounds.z1--;
	    break;
	case 'down':
	    mapBounds.z1++;
	    break;
	}
	setCenterOnPlayer(false);
	drawMap();
    }

    var getURLParameter = function(name) {

	return decodeURI((RegExp(name + '=' + '(.+?)(&|$)').exec(
		location.search) || [ , null ])[1]);
    }

    JM.debug = 'true' === getURLParameter('debug');

    // public API
    return {
	start : initMessages
    };
})();

// Google Analytics
var _gaq = _gaq || [];
_gaq.push([ '_setAccount', 'UA-28839029-1' ]);
_gaq.push([ '_setDomainName', 'none' ]);
_gaq.push([ '_setAllowLinker', true ]);
_gaq.push([ '_trackPageview' ]);

(function() {

    var ga = document.createElement('script');
    ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' === document.location.protocol ? 'https://ssl'
	    : 'http://www')
	    + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
})();

/** OnLoad * */
$(document).ready(JourneyMap.start());