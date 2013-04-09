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
	var showWaypoints = true;

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

	var JmIcon, LoadingIcon;
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
		properties : null,
		game : null,
		mobs : [],
		animals : [],
		players : [],
		villagers : [],
		waypoints : []
	};
	
	var isTouchable = !!('ontouchstart' in window) || !!('onmsgesturechange' in window);

	/**
	 * JQuery add-on for disableSelection
	 */
	(function($) {
		$.fn.disableSelection = function() {
			return this.attr('unselectable', 'on').css('user-select', 'none').on('selectstart', false); // ie
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
			
			url : "/properties",
			dataType : "jsonp",
			contentType : "application/javascript; charset=utf-8",
			async : false
			
		}).fail(handleError).done(function(data, textStatus, jqXHR) {
			JM.properties = data;
			
			// Set global vars of prefs
			showCaves = JM.properties.preference_show_caves;
			showMobs = JM.properties.preference_show_mobs;
			showAnimals = JM.properties.preference_show_animals;
			showVillagers = JM.properties.preference_show_villagers;
			showPets = JM.properties.preference_show_pets;
			showPlayers = JM.properties.preference_show_players;
			showWaypoints = JM.properties.preference_show_waypoints;
			
			// Get L10N messages
			$.ajax({
				url : "/data/messages",
				dataType : "jsonp",
				contentType : "application/javascript; charset=utf-8",
				async : false
			}).fail(handleError).done(function(data, textStatus, jqXHR) {

				JM.messages = data;
				initGame();
			});
			
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

		$.ajax({
			url : "/data/game",
			dataType : "jsonp",
			contentType : "application/javascript; charset=utf-8",
			async : false
		}).fail(handleError).done(
				function(data, textStatus, jqXHR) {

					JM.game = data;

					// Update UI with game info
					$("#version").html(JM.game.jm_version);
					
					// Init update button 
					$("#updateButton").hide().click(function(e){
						var url = document.getElementById('webLink').href;
						window.open(url, '_new', '');
					});
					
					// Show update button
					if (JM.game.latest_journeymap_version > JM.game.jm_version) {
						var text = getMessage('.update_button_title');
						text = text.replace("{0}", JM.game.latest_journeymap_version);
						text = text.replace("{1}", JM.game.mc_version);
						$("#updateButton").attr("title", text);
						$("#updateButton").delay(2000).slideDown();
					}

					// GA event
					if (versionChecked != true) {
						_gaq.push(['_setCustomVar', 1, 'jm_version', JM.game.jm_version, 2]);
						_gaq.push(['_trackEvent', 'Client', 'CheckVersion', JM.game.jm_version]);
						versionChecked = true;
					}

					// Splash
					if (!JmIcon) {
						JmIcon = new Image();
						JmIcon.src = "/ico/apple-touch-icon.png";
						JmIcon.title = "JourneyMap";
						JmIcon.alt = "JourneyMap";
						JmIcon.style.position = "absolute";
						JmIcon.style.visibility = "visible";
						JmIcon.style.left = ($(window).width() / 2 - 72) + "px";
						JmIcon.style.top = ($(window).height() / 2 - 72) + "px";
						JmIcon.style.zIndex = 100;
						document.body.appendChild(JmIcon);
						$(JmIcon).delay(1000).fadeOut(1000);
					}
					
					// Loading
					if(!LoadingIcon) {
						LoadingIcon = new Image();
						LoadingIcon.src = "/img/loading.gif";
						LoadingIcon.alt = "";
						LoadingIcon.style.position = "absolute";
						LoadingIcon.style.visibility = "visible";
						LoadingIcon.style.left = ($(window).width() / 2 - 16) + "px";
						LoadingIcon.style.top = ($(window).height() / 2 - 16) + "px";
						LoadingIcon.style.zIndex = 90;
						document.body.appendChild(LoadingIcon);
						$(LoadingIcon).hide();
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
		canvas = $("#mapCanvas")[0];
		$("#mapCanvas").offset({
			top : 0,
			left : 0
		}).mousedown(myDown).mouseup(myUp).dblclick(myDblClick).disableSelection();
				
		$("#mapCanvas").bind("touchstart", function(event) {
			$("#slider-vertical").hide();			
			if(event.originalEvent.targetTouches.length===1){
				scrollCanvas(event);
			} 
		});

		$("#mapCanvas").bind("touchmove", function(event) {		  
			event.preventDefault();
		    if(userPanning===true && event.originalEvent.targetTouches.length===1){		    	
		    	scrollingCanvas(event);
			} 
		    
		});

		$("#mapCanvas").bind("touchend", function(event) {
			if(event.originalEvent.targetTouches.length===0){
				event.preventDefault();
				$(LoadingIcon).show();
				endScroll();
			}
		});
		
		$("#mapCanvas").bind("gesturestart", function(event) {
			if(userPanning===true) {
				userPanning = false;
			}
		});
		
		$("#mapCanvas").bind("gestureend", function(event) {

			event.preventDefault();
			var e = event.originalEvent;

			if (e.scale < 1.0 && mapScale < maxMapScale) {
				$(LoadingIcon).show();
				setZoom(mapScale + 1);
			} else if (e.scale >= 1.0 && mapScale > minMapScale) {
				$(LoadingIcon).show();
				setZoom(mapScale - 1);
			}
		});
		
		// Init canvases		
		bgCanvas = $(document.createElement("canvas")).attr('id', 'bgCanvas')[0];
		fgCanvas = $(document.createElement("canvas")).attr('id', 'fgCanvas')[0];

		sizeMap();

		// Set page language
		$('html').attr('lang', JM.messages.locale.split('_')[0]);

		// Set RSS feed title
		$("link #rssfeed").attr("title", getMessage('rss_feed_title'));

		// Init Day/Night button
		$("#dayNightText").html(getMessage('day_button_text'));
		$("#dayNightButton").attr("title", getMessage('day_button_title'));
		$("#dayNightButton").click(function() {
			playerOverrideMap = true;
			if(showLight===true) {
				setMapType('day');
			} else {								
				setMapType('night');
			}
		});

		// Follow button
		$("#followButton").attr("title", getMessage('follow_button_title')).click(function() {
			setCenterOnPlayer(!centerOnPlayer);
			refreshMap();
		});

		// JourneyMap menu / homepage link
		$("#webLink").attr("title", getMessage('web_link_title'));
		$("#webLinkText").html(getMessage('web_link_text'));
		
		// JourneyMap menu / forums link
		$("#forumLink").attr("title", getMessage('forum_link_title'));
		$("#forumLinkText").html(getMessage('forum_link_text'));
		
		// JourneyMap menu / RSS feed link
		$("#rssLink").attr("title", getMessage('rss_feed_title'));
		$("#rssLinkText").html(getMessage('rss_feed_text'));

		// JourneyMap menu / Email subscription link
		$("#emailLink").attr("title", getMessage('email_sub_title'));
		$("#emailLinkText").html(getMessage('email_sub_text'));

		// JourneyMap menu / Follow on Twitter link
		$("#twitterLink").attr("title", getMessage('follow_twitter_title'));
		$("#twitterLinkText").html(getMessage('follow_twitter_text'));
		
		// JourneyMap menu / Donate link
		$("#donateLink").attr("title", getMessage('donate_title'));
		$("#donateLinkText").html(getMessage('donate_text'));

		// Show menu
		$("#showMenuText").html(getMessage('show_menu_text'));
		$("#showMenu").click(function(event) {
			event.stopPropagation();
		});
		
		// Show menu items
		setTextAndTitle("#cavesMenuItem", "caves_menu_item_text", "caves_menu_item_title");
		setTextAndTitle("#animalsMenuItem", "animals_menu_item_text", "animals_menu_item_title");
		setTextAndTitle("#petsMenuItem", "pets_menu_item_text", "pets_menu_item_title");
		setTextAndTitle("#mobsMenuItem", "mobs_menu_item_text", "mobs_menu_item_title");
		setTextAndTitle("#villagersMenuItem", "villagers_menu_item_text", "villagers_menu_item_title");
		setTextAndTitle("#playersMenuItem", "players_menu_item_text", "players_menu_item_title");
		setTextAndTitle("#waypointsMenuItem", "waypoints_menu_item_text", "waypoints_menu_item_title");
		
		// Show menu checkboxes
		$("#checkShowCaves").prop('checked', showCaves)		
		$("#checkShowCaves").click(function(event) {
			showCaves = (this.checked === true);
			postPreference("preference_show_caves", showCaves);
			setShowCaves(showCaves);
		});
		
		$("#checkShowWaypoints").prop('checked', showWaypoints)		
		$("#checkShowWaypoints").click(function(event) {
			showWaypoints = (this.checked === true);
			postPreference("preference_show_waypoints", showWaypoints);
			drawMap();
		});
		if(JM.game.waypoints_enabled!==true) {
			$("#checkShowWaypoints").attr("disabled", true);
		}
		
		$("#checkShowAnimals").prop('checked', showAnimals)
		$("#checkShowAnimals").click(function(event) {
			showAnimals = (this.checked === true);
			postPreference("preference_show_animals", showAnimals);			
			drawMap();
		});

		$("#checkShowPets").prop('checked', showPets)
		$("#checkShowPets").click(function(event) {
			showPets = (this.checked === true);
			postPreference("preference_show_pets", showPets);			
			drawMap();
		});

		$("#checkShowMobs").prop('checked', showMobs)
		$("#checkShowMobs").click(function() {
			showMobs = (this.checked === true);
			postPreference("preference_show_mobs", showMobs);			
			drawMap();
		});

		$("#checkShowVillagers").prop('checked', showVillagers)
		$("#checkShowVillagers").click(function() {
			showVillagers = (this.checked === true);
			postPreference("preference_show_villagers", showVillagers);			
			drawMap();
		});

		$("#checkShowPlayers").prop('checked', showPlayers)
		$("#checkShowPlayers").click(function() {
			showPlayers = (this.checked === true);
			postPreference("preference_show_players", showPlayers);			
			drawMap();
		});
		
		// Save map button
		$("#saveButton").attr("title", getMessage('save_button_title')).click(function() {
			saveMapImage();
		});
		
		// Tooltip for slider
		var tooltip = $('<div id="slider-tooltip" />').hide();

		// Slider
		$("#slider-vertical").slider({
			orientation : "vertical",
			range : "min",
			min : minMapScale,
			max : maxMapScale,
			value : mapScale,
			slide : function(event, ui) {
				tooltip.text(ui.value);
				setZoom(ui.value);
			}
		}).prop('title', getMessage('zoom_slider_name'))
		.find(".ui-slider-handle").append(tooltip).hover(function() {
			tooltip.show()
		}, function() {
			tooltip.hide()
		});
		
		// World info
		$("#worldInfo").hide();
		$("#worldNameLabel").html(getMessage('worldname_text'));
		$("#worldTimeLabel").html(getMessage('worldtime_text'));
		$("#playerBiomeLabel").html(getMessage('biome_text'));
		$("#playerLocationLabel").html(getMessage('location_text'));
		$("#playerElevationLabel").html(getMessage('elevation_text'));

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
	 * Post preference to server
	 */
	var postPreference = function(prefName, prefValue) {
		
		$.ajax({
		  type: "POST",
		  url: "/properties",
		  data: prefName +"="+ prefValue
		}).fail(function(data, error, jqXHR){
			if (JM.debug)
				console.log(">>> postPreference failed: " + data.status, error);
		}).done(function(){
			if (JM.debug)
				console.log(">>> postPreference done: " + prefName);
		});
	}
	
	/**
	 * Set text and title on same object
	 */
	var setTextAndTitle = function(selector, text, title) {
		$(selector).html(getMessage(text)).prop('title', getMessage(title));
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
			centerMapOnChunk(Math.round(JM.player.chunkCoordX), Math.round(JM.player.chunkCoordZ));
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
			$("#dayNightButton").removeClass("inverse");
			$("#dayNightButton").addClass("btn-warning");
			$("#dayNightText").html(getMessage('day_button_text'));
			$("#dayNightButton").attr("title", getMessage('day_button_title'));			
		} else if (mapType === "night") {
			if (showLight === true)
				return;
			showLight = true;
			$("#header").addClass("navbar-inverse");
			$("#dayNightButton").addClass("inverse");
			$("#dayNightButton").removeClass("btn-warning");
			$("#dayNightText").html(getMessage('night_button_text'));
			$("#dayNightButton").attr("title", getMessage('night_button_title'));
		} else {
			if (JM.debug)
				console.log(">>> " + "Error: Can't set mapType: " + mapType);
		}

		if (JM.player.underground !== true) {
			$(LoadingIcon).show();
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

		if (show === showCaves) {
			return;
		}
		showCaves = show;

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
			JM.waypoints = data.waypoints;
			JM.world = data.world;

			// Update UI
			$("#playerBiome").html(JM.player.biome);
			$("#playerLocation").html(JM.player.posX + "," + JM.player.posZ);

			$("#playerElevationLabel").attr('title', getMessage('slice_text') + " " + (JM.player.posY >> 4));
			$("#playerElevation").html(JM.player.posY + "&nbsp;(" + (JM.player.posY >> 4) + ")");

			// 0 is the start of daytime, 12000 is the start of sunset, 13800 is
			// the start of nighttime, 22200 is the start of sunrise, and 24000 is
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
				if (JM.world.dimension === 0 && JM.player && JM.player.underground != true) {
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
					timerId = setInterval(queryServer, Math.max(1000, JM.game.browser_poll));
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
		var mapType = (JM.player && JM.player.underground === true && showCaves === true) ? "underground" : (showLight === true ? "night" : "day");
		var dimension = (JM.player.dimension);
		var depth = (JM.player && JM.player.chunkCoordY != undefined) ? JM.player.chunkCoordY : 4;
		var request = "/map.png?mapType=" + mapType + "&dimension=" + dimension + "&depth=" + depth + "&x1=" + mapBounds.x1 + "&z1=" + mapBounds.z1 + "&x2="
				+ mapBounds.x2 + "&z2=" + mapBounds.z2 + "&width=" + width + "&height=" + height;
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
			tempMapImage = $(document.createElement('img')).error(function(error) {

				console.log("refreshMapImage error: ", error)
			});

		}

		$(tempMapImage).unbind('load');
		$(tempMapImage).bind('load', function() {

			if (!this.complete || typeof this.naturalWidth == "undefined" || this.naturalWidth === 0) {
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
	
	/**
	 * Get L10N message by key
	 */
	var getMessage = function(key) {
		if(!JM.messages || !JM.messages[key]) {
			console.log("Missing L10N message: " + key);
			return "!" + key + "!";
		} else {
			return JM.messages[key];
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

		// Hide togggle-able components
		$(".jmtoggle").each(function() {

			$(this).hide()
		});
		$("#worldInfo").hide();
		$("#slider-vertical").hide();
		
		// Hide loading image if shown
		if(LoadingIcon) {
			$(LoadingIcon).hide();
		}

		var displayError;
		if (data.status === 503 || data.status === 0) {
			displayError = getMessage('error_world_not_opened');
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

					ctx.drawImage(JmIcon, getCanvasWidth() / 2 - 72, getCanvasHeight() / 2 - 160);
					JmIcon.onload = null;
				};
				JmIcon.src = "/ico/apple-touch-icon.png";
			} else {
				ctx.drawImage(JmIcon, getCanvasWidth() / 2 - 72, getCanvasHeight() / 2 - 160);
			}

			ctx.globalAlpha = 1;
			ctx.fillStyle = "red";
			ctx.font = "bold 16px Arial";
			ctx.textAlign = "center";
			ctx.fillText(displayError, getCanvasWidth() / 2, (getCanvasHeight() / 2) + 10);
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
		
		// Turn off loading image
		$(LoadingIcon).hide();

		// Draw background map image
		drawBackgroundCanvas(canvasWidth, canvasHeight);

		// clear foreground canvas
		var ctx = fgCanvas.getContext("2d");
		ctx.clearRect(0, 0, canvasWidth, canvasHeight);

		// mobs
		drawMobs(canvasWidth, canvasHeight);

		// other players
		drawMultiplayers(canvasWidth, canvasHeight);
		
		// waypoints
		drawWaypoints(canvasWidth, canvasHeight);

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

			if (mob.hostile !== true && mob.owner && mob.owner === JM.player.username) {
				if (showPets === false)
					return;
				mobLocator = petMobImage;
				ctx.strokeStyle = "#0000ff";
			} else if (mob.hostile !== true) {
				if (showAnimals === false && !(mob.filename === 'villager.png'))
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
			var filename = mob.filename;
			if (filename === 'ghast.png' || filename === 'ender.png' || filename === 'wither.png') {
				radius = 48;
			}

			if (ctx.drawImage) {
				var locRadius = mobLocator.width / 2;
				ctx.save();
				ctx.globalAlpha = .6;
				ctx.translate(x, z);
				ctx.rotate(mob.heading);
				ctx.translate(-locRadius, -locRadius);
				ctx.drawImage(mobLocator, 0, 0);
				ctx.restore();
			}

			ctx.globalAlpha = 1.0;

			// Get pre-loaded image, or lazy-load as needed
			var mobImage = mobImages[filename];
			if (!mobImage) {
				mobImage = new Image();
				mobImage['class'] = 'mobImage';
				$(mobImage).one('error', function() {

					this.src = 'img/entity/unknown.png';
				});
				mobImage.src = 'img/entity/' + filename;
				mobImages[filename] = mobImage;
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
					otherImage.src = "/img/entity/char.png";
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
	
	// Draw the location of waypoints
	var drawWaypoints = function(canvasWidth, canvasHeight) {

		if (JM.debug)
			console.log(">>> " + "drawWaypoints");

		if(!showWaypoints==true || !JM.game.waypoints_enabled==true)
			return;
		
		var waypoints = JM.waypoints;
		if (!waypoints)
			return;

		if (!canvasWidth || !canvasHeight) {
			canvasWidth = getCanvasWidth();
			canvasHeight = getCanvasHeight();
		}

		var ctx = fgCanvas.getContext("2d");		

		// Draw waypoints
		$.each(waypoints, function(index, waypoint) {

			var x = getScaledChunkX(waypoint.x / 16) - (mapScale / 2);
			var z = getScaledChunkZ(waypoint.z / 16) - (mapScale / 2);
			var outofbounds = false;			
			var diameter = 6;
			var min = diameter;
			
			if(x<0) {
				x = 0;
				outofbounds = true;
			} else if(x > canvasWidth) {
				x = canvasWidth;
				outofbounds = true;
			}
			
			if(z<52) {
				z = 52;
				outofbounds = true;
			} else if(z > canvasHeight) {
				z = canvasHeight;
				outofbounds = true;
			}
			
			if(!waypoint.color) {
				waypoint.color = "rgb(" 
					+ waypoint.r + "," 
					+ waypoint.g + "," 
					+ waypoint.b + ")";    
			}
			
			// Draw waypoint
			ctx.strokeStyle = "#000";
			ctx.lineWidth = 2;
			ctx.fillStyle = waypoint.color;
			
			if(!outofbounds) {
				
				// Draw marker
				if(waypoint.reiType && waypoint.reiType==1) {
					// X death spot
					diameter = 6;
					ctx.strokeStyle = "#000";
					ctx.lineWidth = 6;
					ctx.lineCap = 'round';
										
					ctx.beginPath();
					ctx.moveTo(x-diameter, z-diameter);
					ctx.lineTo(x+diameter, z+diameter);
					ctx.closePath();
					ctx.stroke();
					
					ctx.beginPath();
					ctx.moveTo(x+diameter, z-diameter);
					ctx.lineTo(x-diameter, z+diameter);
					ctx.closePath();
					ctx.stroke();
					
					ctx.strokeStyle = waypoint.color;
					ctx.lineWidth = 2;
					
					ctx.lineCap = 'butt';
					
					ctx.beginPath();
					ctx.moveTo(x-diameter, z-diameter);
					ctx.lineTo(x+diameter, z+diameter);
					ctx.closePath();
					ctx.stroke();
					
					ctx.beginPath();
					ctx.moveTo(x+diameter, z-diameter);
					ctx.lineTo(x-diameter, z+diameter);
					ctx.closePath();
					ctx.stroke();
					
				} else {
					// Diamond
					ctx.lineCap = 'butt';
					
					ctx.beginPath();
					ctx.moveTo(x-diameter, z);
					ctx.lineTo(x, z-diameter);
					ctx.lineTo(x+diameter, z);
					ctx.lineTo(x, z+diameter);
					ctx.lineTo(x-diameter, z);
					ctx.closePath();
					ctx.fill();
					ctx.stroke();
					
					ctx.globalAlpha = 0.1;
					ctx.strokeStyle = "#fff";
					ctx.beginPath();
					ctx.moveTo(x-diameter, z);
					ctx.lineTo(x+diameter, z);
					ctx.moveTo(x, z-diameter);
					ctx.lineTo(x, z+diameter);
					ctx.closePath();
					ctx.stroke();
				}
			
				// Draw label background			
				ctx.font = "bold 12px Arial";
				ctx.textAlign = "center";
				ctx.fillStyle = "#000";
				
				var labelZ = z - (diameter*2)+2; 
				
				// Get label dimensions
				var metrics = ctx.measureText(waypoint.name);
				var width = metrics.width + 6;
				ctx.globalAlpha = 0.7;
				ctx.fillRect(x-(width/2), labelZ-12, width, 16);
				
				// Draw label
				ctx.globalAlpha = 1.0;
				if(waypoint.reiType && waypoint.reiType==1) {
					ctx.fillStyle = "#f00";
				} else {
					ctx.fillStyle = "#fff";
				}
				ctx.fillText(waypoint.name, x, labelZ);
			} else {
				
				// Circle on edge of map
				ctx.lineWidth = 4;
				ctx.beginPath();
				ctx.arc(x, z, 8, 0, Math.PI * 2, true);
				ctx.closePath();
				ctx.fill();
				ctx.stroke();
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
			ctx.drawImage(latestMapImage, 0, 0, width * mapScale, height * mapScale);

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
		
		if(event.originalEvent && event.originalEvent.targetTouches) {
			event = event.originalEvent.targetTouches[0];
		}
		
		if(event.pageX) {
			mx = event.pageX;
			my = event.pageY;
		}

	}

	function myDown(e) {

		scrollCanvas(e);
	}

	function myUp(e) {
		getMouse(e);
		endScroll();
	}
	
	function endScroll() {
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
			setCenterOnPlayer(false);
			userPanning = false;
		}
	}

	function myDblClick(e) {

		getMouse(e);
	}

	function myKeyPress(e) {

		var key = (e) ? e.which : e.keyCode;
		switch (String.fromCharCode(key)) {
			case '-' :
				zoom('out');
				break;
			case '=' :
				zoom('in');
				break;
			case 'w' :
			case 'W' :
				moveCanvas('up');
				break;
			case 'a' :
			case 'A' :
				moveCanvas('right');
				break;
			case 's' :
			case 'S' :
				moveCanvas('down');
				break;
			case 'd' :
			case 'D' :
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

		var centerChunkX = Math.floor(getCanvasWidth() / chunkScale / 2) + mapBounds.x1;
		var centerChunkZ = Math.floor(getCanvasHeight() / chunkScale / 2) + mapBounds.z1;

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
			case 'left' :
				mapBounds.x1++;
				break;
			case 'right' :
				mapBounds.x1--;
				break;
			case 'up' :
				mapBounds.z1--;
				break;
			case 'down' :
				mapBounds.z1++;
				break;
		}
		setCenterOnPlayer(false);
		drawMap();
	}

	var getURLParameter = function(name) {

		return decodeURI((RegExp(name + '=' + '(.+?)(&|$)').exec(location.search) || [, null])[1]);
	}

	JM.debug = 'true' === getURLParameter('debug');

	// public API
	return {
		start : initMessages
	};
})();

// Google Analytics
var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-28839029-1']);
_gaq.push(['_setDomainName', 'none']);
_gaq.push(['_setAllowLinker', true]);
_gaq.push(['_trackPageview']);

(function() {

	var ga = document.createElement('script');
	ga.type = 'text/javascript';
	ga.async = true;
	ga.src = ('https:' === document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
	var s = document.getElementsByTagName('script')[0];
	s.parentNode.insertBefore(ga, s);
})();

/** OnLoad * */
$(document).ready(JourneyMap.start());