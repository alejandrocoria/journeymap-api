"use strict";
/**
 * JourneyMap web client 
 * http://journeymap.techbrew.net 
 * Copyright (C) 2011-2013.
 * Mark Woodman / TechBrew.net All rights reserved. 
 * May not be modified or distributed without express written consent.
 */
var JourneyMap = (function() {
	var mcMap;

	var isNightMap = false;
	var showCaves = true;
	var centerOnPlayer = true;

	var showAnimals = true;
	var showPets = true;
	var showMobs = true;
	var showVillagers = true;
	var showPlayers = true;
	var showWaypoints = true;
	var showGrid = true;

	var lastImageCheck = 0;
	var skipImageCheck = false;
	
	var JmIcon;
	var halted = false;
	var uiInitialized = false;
	var versionChecked = false;
	var updatingMap = false;
	var drawingMap = false;

	var playerOverrideMap = false;
	var playerUnderground = false;
	
	var mapOverlay;

	var timerId = null;

	var JM = {
		debug : false,
		messages : null,
		properties : null,
		game : null,
		mobs : null,
		animals : null,
		players : null,
		villagers : null,
		waypoints : null,
		images : null
	};
	var markers = {};
	
	var entityTemplate = [
	    '<div class="entityMarker" id="">',
	    '<div class="entityName"/>',
	    '<div class="entityImages">',			    
	    '<img class="entityLocator" src="/img/pixel.png">',
	    '<img class="entityIcon" src="/img/pixel.png" >',
	    '</div>',
	    '</div>'
	].join('');
	
	var dialogTemplate = [
		'<div class="dialog">',
		'<img src="/ico/journeymap144.png">',
		'<div></div>',
		'</div>'
	].join('');	
	
	var RAD_DEG=57.2957795;
	
	var errorDialog = null;
	var splashDialog = null;
	var debug = false;
	
	// Preload images
	$('<img>').attr('src','/img/pixel.png');

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

		if (debug)
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
			showGrid = JM.properties.preference_show_grid;
			
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

		if (debug)
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
					$("#version").html("JourneyMap " + JM.game.jm_version);				
					
					// GA event
					if (versionChecked != true) {
						_gaq.push(['_setCustomVar', 1, 'jm_version', JM.game.jm_version, 2]);
						_gaq.push(['_trackEvent', 'Client', 'CheckVersion', JM.game.jm_version]);
						versionChecked = true;
					}
					
					// Splash
					if(!errorDialog) {
						splashDialog = createDialog("JourneyMap " + JM.game.jm_version + "<br><small>by techbrew</small>");
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

		if (debug)
			console.log(">>> " + "initUI");

		// Ensure messages are loaded first.
		if (!JM.messages) {
			throw ("initUI called without JM.messages"); // shouldn't happen
		}

		// Set page language
		$('html').attr('lang', JM.messages.locale.split('_')[0]);

		// Set RSS feed title
		$("link #rssfeed").attr("title", getMessage('rss_feed_title'));		

		// Main menu	
		$("#jm-menu").menu().hide();
		$("#jm-button").button({
			icons: {
				secondary: "ui-icon-triangle-1-s"
	        }
		}).click(function(){
			var menu = $("#jm-menu");
			if ( $(menu).is(':visible') ) {
				menu.hide();
			} else {
				var button = $("#jm-button");
				menu.show().position({ my: "left top", at: "left bottom", of: button });
				$( document ).one( "click", function() { menu.hide(); });
			}
	        return false;
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
		
		// Toggles / Day/Night button
		$("#dayNightButton")
			.attr("title", getMessage('day_button_title'))
			.click(function() {
				playerOverrideMap = true;
				if(isNightMap===true) {
					setMapType('day');
				} else {								
					setMapType('night');
				}
			});

		$("#dayNightButton").parent().buttonset();
		// Toggles / Follow button
		$("#followButton")
			.attr("title", getMessage('follow_button_title'))
			.click(function() {
				setCenterOnPlayer(!centerOnPlayer);
				refreshMap();
			});
		$("#jm-toggles").buttonset();
		
		// Options Menu and Button		
		$("#jm-options-menu").menu().hide();
		$("#jm-options-button").attr("title", getMessage('show_menu_text'));
		$("#jm-options-button").button({
			icons: {
				secondary: "ui-icon-triangle-1-s"
	        }
		}).click(function(){
			$("#jm-actions-menu").hide();
			var menu = $("#jm-options-menu");
			if ( $(menu).is(':visible') ) {
				menu.hide();
			} else {
				var button = $("#jm-options-button");
				var x = $('#map-canvas').width() - button.offset().left - button.width();
				menu.css('margin-right', x + "px");
				menu.show();
				$( document ).one( "click", function() { menu.hide(); });
			}			
	        return false;
		});
		
		$("#jm-options-menu").click( function(event){
			event.stopPropagation();
		});
		
		
		// Options Menu items
		setTextAndTitle("#cavesMenuItem", "caves_menu_item_text", "caves_menu_item_title");
		setTextAndTitle("#animalsMenuItem", "animals_menu_item_text", "animals_menu_item_title");
		setTextAndTitle("#petsMenuItem", "pets_menu_item_text", "pets_menu_item_title");
		setTextAndTitle("#mobsMenuItem", "mobs_menu_item_text", "mobs_menu_item_title");
		setTextAndTitle("#villagersMenuItem", "villagers_menu_item_text", "villagers_menu_item_title");
		setTextAndTitle("#playersMenuItem", "players_menu_item_text", "players_menu_item_title");
		setTextAndTitle("#waypointsMenuItem", "waypoints_menu_item_text", "waypoints_menu_item_title");
		setTextAndTitle("#gridMenuItem", "grid_menu_item_text", "grid_menu_item_title");
		
		// Options Menu checkboxes
		$("#checkShowCaves").prop('checked', showCaves)		
		$("#checkShowCaves").click(function(event) {
			showCaves = (this.checked === true);
			postPreference("preference_show_caves", showCaves);
			if (playerUnderground === true) {
				refreshMap();
			}			
		});
		
		$("#checkShowGrid").prop('checked', showGrid)		
		$("#checkShowGrid").click(function(event) {
			showGrid = (this.checked === true);
			postPreference("preference_show_grid", showGrid);
			refreshMap();
		});
		
		$("#checkShowWaypoints").prop('checked', showWaypoints)		
		$("#checkShowWaypoints").click(function(event) {
			showWaypoints = (this.checked === true);
			postPreference("preference_show_waypoints", showWaypoints);
			if(!showWaypoints) {
				JM.waypoints = null;				
			}
		});
		if(JM.game && JM.game.waypoints_enabled!==true) {
			$("#checkShowWaypoints").attr("disabled", true);
		}
		
		$("#checkShowAnimals").prop('checked', showAnimals)
		$("#checkShowAnimals").click(function(event) {
			showAnimals = (this.checked === true);
			postPreference("preference_show_animals", showAnimals);			
			drawMobs();
		});

		$("#checkShowPets").prop('checked', showPets)
		$("#checkShowPets").click(function(event) {
			showPets = (this.checked === true);
			postPreference("preference_show_pets", showPets);			
			drawMobs();
		});

		$("#checkShowMobs").prop('checked', showMobs)
		$("#checkShowMobs").click(function() {
			showMobs = (this.checked === true);
			postPreference("preference_show_mobs", showMobs);			
			drawMobs();
		});

		$("#checkShowVillagers").prop('checked', showVillagers)
		$("#checkShowVillagers").click(function() {
			showVillagers = (this.checked === true);
			postPreference("preference_show_villagers", showVillagers);			
			drawMobs();
		});

		$("#checkShowPlayers").prop('checked', showPlayers)
		$("#checkShowPlayers").click(function() {
			showPlayers = (this.checked === true);
			postPreference("preference_show_players", showPlayers);			
			drawMultiplayers();
		});
		
		// Actions Menu Button
		$("#jm-actions-menu").menu().hide();
		$("#jm-options-button").attr("title", getMessage('actions_title'));
		$("#jm-actions-button").button({
			icons: {
				secondary: "ui-icon-triangle-1-s"
	        }
		}).click(function(){
			$("#jm-options-menu").hide();
			var menu = $("#jm-actions-menu");
			if ( $(menu).is(':visible') ) {
				menu.hide();
			} else {
				var button = $("#jm-actions-button");
				var x = $('#map-canvas').width() - button.offset().left - button.width();
				menu.css('margin-right', x + "px");
				menu.show();
				$( document ).one( "click", function() { menu.hide(); });
			}
	        return false;
		});
		
		// Save map button
		$("#saveButton").attr("title", getMessage('save_button_title')).click(function() {
			saveMapImage();
		});
		$("#saveButtonText").html(getMessage('save_button_text'));
		
		// Automap button
		$("#autoMapButton").attr("title", getMessage('automap_title')).click(function() {
			
		});
		$("#autoMapButtonText").html(getMessage('automap_text'));
		
		// World info
		$("#worldInfo").hide();
		$("#worldNameLabel").html(getMessage('worldname_text'));
		$("#worldTimeLabel").html(getMessage('worldtime_text'));
		$("#playerBiomeLabel").html(getMessage('biome_text'));
		$("#playerLocationLabel").html(getMessage('location_text'));
		$("#playerElevationLabel").html(getMessage('elevation_text'));
		
		// Disable selection on nav elements
		$(".nav").disableSelection();

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
			if (debug)
				console.log(">>> postPreference failed: " + data.status, error);
		}).done(function(){
			if (debug)
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

		if (debug)
			console.log(">>> " + "initWorld");

		// Reset state
		halted = false;
		updatingMap = false;
		playerUnderground = false;
		clearTimer();		
		
		JM.mobs = {};
		JM.animals = {};
		JM.players = {};
		JM.villagers = {};
		JM.waypoints = {};
		JM.images = {};
		
		markers = {
			mobs : {},
			animals : {},
			players : {},
			villagers : {},
			waypoints : {},
			player: {}
		}

		queryServer(setupMap);

	}
	
	var setupMap = function() {
		
		// Google Map
		mcMap = new MCMap(document.getElementById('map-canvas'));
		
		mcMap.map.controls[google.maps.ControlPosition.TOP_LEFT].push(document.getElementById('jm-logo'));
		mcMap.map.controls[google.maps.ControlPosition.TOP_LEFT].push(document.getElementById('jm-toolbar'));
		mcMap.map.controls[google.maps.ControlPosition.TOP_LEFT].push(document.getElementById('jm-toggles'));			
		mcMap.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(document.getElementById('jm-options'));
		mcMap.map.controls[google.maps.ControlPosition.TOP_RIGHT].push(document.getElementById('jm-actions'));	
		mcMap.map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('jm-actions-menu'));
		mcMap.map.controls[google.maps.ControlPosition.RIGHT_TOP].push(document.getElementById('jm-options-menu'));		
		mcMap.map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(document.getElementById('worldInfo'));
					
		// Close error
		if(errorDialog) {
			$('.ui-dialog').remove();
			errorDialog = null;
		}
		
		// Close splash
		if(splashDialog) {
			$(splashDialog).parent().delay(1000).fadeOut(1000, function(){
				$('.ui-dialog').remove();
				splashDialog = null;
			});
		}

		setCenterOnPlayer(true);
		
		// Show update button
		if (JM.game.latest_journeymap_version > JM.game.jm_version) {
			var text = getMessage('update_button_title');
			text = text.replace("{0}", JM.game.latest_journeymap_version);
			text = text.replace("{1}", JM.game.mc_version);
			$("#jm-update-button").button()
				.attr("title", text)
				.click(function(e){
					var url = document.getElementById('webLink').href;
					window.open(url, '_new', '');
			});
			
			mcMap.map.controls[google.maps.ControlPosition.TOP_CENTER].push(document.getElementById('jm-alerts'));
		}

	}

	/**
	 * Invoke saving map file
	 */
	var saveMapImage = function() {

		alert("TODO");
	}

	function setMapType(mapType) {

		var typeChanged = false;
		if (mapType === "day") {
			if (isNightMap === false) return;
			isNightMap = false;
			typeChanged = true;

			$("#dayNightText").html(getMessage('day_button_text'));
			$("#dayNightButton").attr("title", getMessage('day_button_title'));
			$("#dayNightButtonImg").attr('src', '/img/sun.png');

		} else if (mapType === "night") {
			if (isNightMap === true) return;
			isNightMap = true;
			typeChanged = true;

			$("#dayNightText").html(getMessage('night_button_text'));
			$("#dayNightButton").attr("title", getMessage('night_button_title'));
			$("#dayNightButtonImg").attr('src', '/img/moon.png');
	
		} else {
			if (debug)
				console.log(">>> " + "Error: Can't set mapType: " + mapType);
		}
		
		if(typeChanged && playerUnderground === false) {		
			if (debug) console.log("setMapType(" + mapType + ")");
			refreshMap();
		}

	}

	function setCenterOnPlayer(onPlayer) {	
		centerOnPlayer = onPlayer;
		
		if(onPlayer) {
			$("#followButtonImg").attr('src', '/img/follow.png');
		} else {
			$("#followButtonImg").attr('src', '/img/follow-off.png');
		}
		
		if (onPlayer === true) {
			drawPlayer();
		} 
	}

	function setShowCaves(show) {

		if (show === showCaves) {
			return;
		}
		showCaves = show;

		if (playerUnderground === true) {
			refreshMap();
		}		

	}

	// //////////// DATA ////////////////////

	var queryServer = function(callback) {

		if (halted === true)
			return;
		
		// Params for dirty image check
		var params = "";
		if(mcMap) {
			if(!lastImageCheck) lastImageCheck = new Date().getTime();
			params = "?images.since=" + lastImageCheck;
		}

		// Get all the datas
		fetchData("/data/all" + params, function(data) {
			
			// Apply data
			JM.animals = data.animals;
			JM.images = data.images;
			JM.mobs = data.mobs;
			JM.player = data.player;
			JM.players = data.players;
			JM.villagers = data.villagers;
			JM.waypoints = data.waypoints;
			JM.world = data.world;
			
			// Update underground state
			var wasUnderground = playerUnderground;
			playerUnderground = JM.player.underground;

			// Update UI
			$("#playerBiome").html(JM.player.biome);
			$("#playerLocation").html(JM.player.posX + "," + JM.player.posZ);
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
				if (JM.world.dimension === 0 && playerUnderground === false) {
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
			if(mcMap) {
				if(wasUnderground !== playerUnderground) {
					refreshMap();
				} else {
					mapOverlay.refreshTiles();
				}
				drawMap();
			}

			if (timerId === null) {
				var dur = (JM.game && JM.game.browser_poll) ? JM.game.browser_poll : 1000;
				timerId = setInterval(queryServer, Math.max(1000, dur));
			}
			
			if (callback) {
				callback();
			}

		});

	}

	/**
	 * Fetch JsonP data. Generic error handling, callback invoked on success
	 */
	var fetchData = function(dataUrl, callback) {

		if (debug)
			console.log(">>> " + "fetchData " + dataUrl);

		$.ajax({
			url : dataUrl,
			dataType : "jsonp"
		}).fail(handleError).done(callback);
	}

	

	/**
	 * Force immediate update
	 */
	var refreshMap = function() {
		if (debug) console.log(">>> " + "refreshMap");
			
		lastImageCheck = 1;
		var zoom = mcMap.map.getZoom();		
		var delta = (zoom==MapConfig.maxZoom) ? -0.0000001 : 0.0000001 ;
		var center = mcMap.map.getCenter();
		
		
		// This hack forces the tiles to be replaced but doesn't visibly change the map
		mcMap.map.setZoom(zoom + delta);
		mcMap.map.panTo(center);
		mcMap.map.setZoom(zoom);
		mcMap.map.panTo(center);
				
		// The old way:
		//      clearTimer();
		//		queryServer(function(){
		//		// After data retrieved,force the tile refresh
		//		//skipImageCheck = false;
		//		lastImageCheck = new Date().getTime();
		//		mapOverlay.refreshTiles(true); // Force all tiles to be renewed
		//	    });	
		
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

		if (debug)
			console.log(">>> " + "handleError");

		// Secondary errors will be ignored
		if (halted === true)
			return;

		// Clear the timer
		clearTimer();

		console.log("Server returned error: " + data.status + ": " + jqXHR);

		// Move nav components back to holder
		$(".nav").appendTo( $("#nav-holder") );
		
		// Destroy Google Map
		$("#map-canvas").empty();
		
		// Ensure splash destroyed
		if(splashDialog) {
			$(splashDialog).remove();
			splashDialog = null;
		}

		// Display error
		var displayError;
		if (data.status === 503 || data.status === 0) {
			displayError = getMessage('error_world_not_opened');
		} else {
			displayError = "";
		}
		
		if(!errorDialog) {			
			errorDialog = createDialog(displayError, true);
		}
		$(errorDialog).find('div').html(displayError);
			
		// Restart in 5 seconds
		if (!halted) {
			halted = true;
			if (debug)
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
	
	var createDialog = function(text, modal) {
		if(!modal) modal=false;
		var dialog = $(dialogTemplate).dialog({ modal: modal });
		dialog.parent().find('.ui-dialog-titlebar').remove();
		dialog.css('z-index', google.maps.Marker.MAX_ZINDEX + 1);
		if(text) {
			dialog.find('div').html(text);
		}
		return dialog;
	}

	// ////////////DRAW ////////////////////

	// Draw the map
	var drawMap = function() {

		if (debug)
			console.log(">>> " + "drawMap");

		if (drawingMap === true) {
			if (debug)
				console.log(">>> " + "Avoided concurrent drawMap()");
			return false;
		}

		drawingMap = true;

		// mobs
		drawMobs();

		// other players
		drawMultiplayers();
		
		// waypoints
		drawWaypoints();

		// player
		drawPlayer();

		drawingMap = false;

	}

	// Draw the player icon
	var drawPlayer = function() {
		
		if (debug)
			console.log(">>> " + "drawPlayer");
		
		// Get current player position
		var pos = blockPosToLatLng(JM.player.posX, JM.player.posZ);
		var heading = JM.player.heading;
		var imgId = 'player'+JM.player.entityId;
		
		// Ensure marker
		if(!markers.playerMarker) {

			var img = new Image();
			$(img).attr('id', imgId)
			      .attr('src','/img/locator-player.png')
			      .css('width','64px')
			      .css('height','64px')
			      .rotate(heading*RAD_DEG);
			markers.playerMarker = new RichMarker({
				position: pos,
			    map: mcMap.map,
			    draggable: false,
			    flat: true,
			    anchor: RichMarkerPosition.MIDDLE,
			    content: img,
			    zIndex: google.maps.Marker.MAX_ZINDEX + 1,
			    tooltip : new RichMarker({
					position: null,
				    map: null,
				    draggable: false,
				    flat: true,
				    anchor: RichMarkerPosition.BOTTOM,
				    content: '<div class="playerInfo">' + JM.player.username + '</div>',
		        })
	        });
			
			google.maps.event.addListener(markers.playerMarker, 'mouseover', function(args) {
				var tooltip = markers.playerMarker.tooltip;
				if(tooltip.timeout) {
					window.clearTimeout(tooltip.timeout);
				}
				if(!tooltip.getMap()) {
					tooltip.setPosition(markers.playerMarker.position);
					tooltip.setMap(markers.playerMarker.map);
					tooltip.setZIndex(google.maps.Marker.MAX_ZINDEX + 1);
				}
			});
			
			google.maps.event.addListener(markers.playerMarker, 'mouseout', function(args) {
				
				var tooltip = markers.playerMarker.tooltip;
				if(tooltip.getMap()) {
					if(tooltip.timeout) {
						window.clearTimeout(tooltip.timeout);
					}
					tooltip.timeout = window.setTimeout(function(){
						delete tooltip.timeout;
						tooltip.setMap(null);
					}, 500)					
				}
			});
			

			google.maps.event.addListener(mcMap.map, 'dragstart', function() {
				setCenterOnPlayer(false);
			});
			
			google.maps.event.addListener(mcMap.map, 'zoom_changed', function() {
				if(centerOnPlayer===true) {
					mcMap.map.panTo(markers.playerMarker.getPosition());
				}
				
				if(mcMap.map.getZoom()<3) {
					$('img.entityLocator').css('visibility','hidden');
				} else {
					$('img.entityLocator').css('visibility','visible');
				}
				
				if(mcMap.map.getZoom()==0) {
					$('img.entityMarker').css('visibility','hidden');
				} else {
					$('img.entityMarker').css('visibility','visible');
				}
				
			});

		} 
		
		// Update marker position and heading
		markers.playerMarker.setPosition(pos);
		$('#'+imgId).rotate(heading*RAD_DEG);
		
		// Keep on top
		if(markers.playerMarker.getZIndex()<google.maps.Marker.MAX_ZINDEX){
			markers.playerMarker.setZIndex(google.maps.Marker.MAX_ZINDEX + 1);
		}
		
		// Center if needed
		if(centerOnPlayer===true) {
			mcMap.map.panTo(pos);
		}
	}
	
	// Remove markers not in current JM data
	var removeObsoleteMarkers = function(jmMap, markerMap) {
		$.each(markerMap, function(id, marker) {
			if(!jmMap || !(id in jmMap) ){
				marker.setMap(null);
				if(debug) console.log("Marker removed for " + id);
				delete markerMap[id];
			}
		});
	}

	// Draw the location of mobs
	var drawMobs = function() {

		if (debug)
			console.log(">>> " + "drawMobs");
		
		if(mcMap.map.getZoom()===0) return;
		
		// Mobs
		removeObsoleteMarkers(JM.mobs, markers.mobs);		
		if (showMobs === true && JM.mobs) {
			$.each(JM.mobs, function(index, mob) {
				updateEntityMarker(mob,markers.mobs);
			});
		}

		// Animals
		removeObsoleteMarkers(JM.animals, markers.animals);	
		if ((showAnimals === true || showPets === true) && JM.animals) {
			$.each(JM.animals, function(index, mob) {
				updateEntityMarker(mob,markers.animals);
			});
		}

		// Villagers
		removeObsoleteMarkers(JM.villagers, markers.villagers);	
		if (showVillagers === true && JM.villagers) {
			$.each(JM.villagers, function(index, mob) {
				updateEntityMarker(mob,markers.villagers);
			});
		}
		
	}

	// Create or update marker
	var updateEntityMarker = function(entity, markerMap) {

		// Check current entity position		
		var pos = blockPosToLatLng(entity.posX, entity.posZ);
		if(!mcMap.map.getBounds().contains(pos)) {
			return; // Don't bother with marker
		}
		
		var id = 'id' + entity.entityId;
		var heading = entity.heading;

		var locatorUrl;
		var iconSize = 32;
		var iconColor = "#cccccc";
		var iconLabel = null;
		var marker = markerMap[id];
		
		if (entity.hostile !== true && entity.owner) {
			if(entity.owner === JM.player.username) {			
				if (showPets === true) {
					locatorUrl = "/img/locator-pet.png";
					iconColor = "#0000ff";
				}
			} else {
				locatorUrl = "/img/locator-neutral.png";
			}
		} else if (entity.hostile !== true) {
			if (showAnimals === false && !(entity.filename === 'villager.png')) {
			} else {
				locatorUrl = "/img/locator-neutral.png";
			}
		} else {
			locatorUrl = "/img/locator-hostile.png";
			iconColor = "#ff0000";
		}
		
		// No locator means no marker
		if(!locatorUrl) {
			if(marker) {
				marker.setMap(null);
				delete markerMap[id];
				if(debug) console.log("Marker invalid for " + id + " - " + entity.filename);
				return;
			} else {
				if(debug) console.log("Pending marker invalid for " + id + " - " + entity.filename);
				return;
			}		
		}

		// Create marker if needed
		if(!marker) {	
			
			var contentDiv = $(entityTemplate).attr('id',id);
					
			marker = new RichMarker({
				position: pos,
			    map: mcMap.map,
			    draggable: false,
			    flat: true,
			    anchor: RichMarkerPosition.MIDDLE,
			    content: contentDiv[0]
	        });
			markerMap[id] = marker;
			
			if(debug) console.log("Marker added for " + id);
		}
	
		// Label if customName exists
		var contentDiv = $('#'+id);
		
		if(entity.customName) {
			$(contentDiv).find('.entityName').css('visibility','visible').html(entity.customName);
		} else {
			$(contentDiv).find('.entityName').css('visibility','hidden').html();
		}
		
		// Entity icon
		$(contentDiv).find('.entityIcon').attr('src','/img/entity/' + entity.filename);

		// Entity locator		
		$(contentDiv).find('.entityLocator').attr('src', locatorUrl).rotate(heading*RAD_DEG);		
	
		// Update marker position
		marker.setPosition(pos);
	}

	// Draw the location of other players
	var drawMultiplayers = function() {

		if (debug)
			console.log(">>> " + "drawMultiplayers");

		var others = JM.players;
		if (!others)
			return;
		
		if(showPlayers!==true)
			return;
		
		return;
		
		// TODO

		$.each(others, function(index, other) {

			var x = getScaledChunkX(other.posX / 16) - (mapScale / 2);
			var z = getScaledChunkZ(other.posZ / 16) - (mapScale / 2);
			if (other.username != JM.player.name) {
				if (x >= 0 && x <= canvasWidth && z >= 0 && z <= canvasHeight) {

					// Draw locator
					var locRadius = otherPlayerMobImage.width / 2;
					ctx.save();
					ctx.globalAlpha = 1;
					ctx.translate(x, z);
					ctx.rotate(other.heading);
					ctx.translate(-locRadius, -locRadius);
					ctx.drawImage(otherPlayerMobImage, 0, 0);
					ctx.restore();
					
					// show image
					if(otherImage.width) {
						var radius = otherImage.width / 2;
						ctx.save();
						ctx.translate(x - radius, z - radius);
						if(other.heading>Math.PI) {
							ctx.translate(otherImage.width, 0);
							ctx.scale(-1, 1);
						}
						ctx.drawImage(otherImage, 0, 0, radius * 2, radius * 2);
						ctx.restore();
					}
					// Draw label background			
					ctx.font = "bold 11px Arial";
					ctx.textAlign = "center";
					ctx.fillStyle = "#000";
					
					var labelZ = z + 36; 
					
					// Get label dimensions
					var metrics = ctx.measureText(other.username);
					var width = metrics.width + 6;
					ctx.globalAlpha = 0.7;
					ctx.fillRect(x-(width/2), labelZ-12, width, 16);
					
					// Draw label
					ctx.globalAlpha = 1.0;
					ctx.fillStyle = "#0f0";
					ctx.fillText(other.username, x, labelZ);
				}
			}
		});

	}
	
	// Draw the location of waypoints
	var drawWaypoints = function() {
		
		if (debug)
			console.log(">>> " + "drawWaypoints");

		removeObsoleteMarkers(JM.waypoints, markers.waypoints);
		
		if(!showWaypoints==true || !JM.game.waypoints_enabled==true || !JM.waypoints) {
			return;
		}	

		$.each(JM.waypoints, function(index, waypoint) {
			updateWaypointMarker(waypoint,markers.waypoints);
		});
			
	}
	
	// Create or update marker
	var updateWaypointMarker = function(waypoint, markerMap) {

		// Get current waypoint position		
		var pos = blockPosToLatLng(waypoint.x, waypoint.z);
		if(!mcMap.map.getBounds().contains(pos)) {
			//return; // Don't bother with marker
			// TODO: Put on edge of map as an arrow?
		}

		var id = waypoint.id;
		waypoint.color = rgbToHex(waypoint.r, waypoint.g, waypoint.b);
		
		var marker = markerMap[id];
		
		if(!marker) {
			var icon = {
				fillOpacity: 0.85,
				scale: 1,
				fillColor: waypoint.color,
				strokeColor: 'white'
			};
			var labelClass = "waypoint";

			if(waypoint.type==1) {
				// death point: X marks the spot
				icon.path = 'M -10,-10 0,-4 10,-10 14,-8 4,0 14,8 10,10 0,4 -10,10 -14,8 -4,0 -14,-8 z';
				icon.strokeWeight = 2;
				labelClass = labelClass + " death";
			} else {
				// diamond
				icon.path = 'M 0,-16 16,0 0,16 -16,0 0,-16 z';
				icon.strokeWeight = 3;
				labelClass = labelClass;
			}
			
			var title = [
			    getMessage('location_text'),
			    waypoint.x + "," + waypoint.z,
			    getMessage('elevation_text'),
			    waypoint.y,
			    "(" + (waypoint.y >> 4) + ")"
			].join(' ');

			marker = new MarkerWithLabel({
		       position: pos,
		       map: mcMap.map,
		       draggable: false,
		       clickable: false,
		       icon: icon,
		       title: title,
		       labelContent: waypoint.display,
		       labelClass: labelClass,
		       labelAnchor: new google.maps.Point(-18,14)
		     });
			markerMap[id] = marker;	
			
			if(debug) console.log("Marker added for " + id);
			
		} else {
			
			// Update marker color		
			if(marker.icon.fillColor!==waypoint.color) {
				marker.icon.fillColor = waypoint.color;
				marker.setIcon(marker.icon);
			}
		}
	}
	
	function rgbToHex(r, g, b) {
	    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
	}
	
	/** Google Maps Code **/
	
	google.maps.visualRefresh = true;
	
	var MapConfig = {
		tileSize : 512,
		defaultZoom : 0,
		minZoom : 0,
		maxZoom : 5
	}
	MapConfig.perPixel = 1.0 / MapConfig.tileSize;
	
	var blockPosToLatLng = function (x, y) {
		var me = this;		
		var center = .5 * MapConfig.perPixel;		
		var lat = (y * MapConfig.perPixel) + center;
		var lng = (x * MapConfig.perPixel) + center;		
		return new google.maps.LatLng(lat, lng);
	}
	
	var toChunkRange = function (coord, zoom) {
		var scale = Math.pow(2, zoom);
		var distance = 32/scale;
		var minChunkX = coord.x * distance;
		var minChunkZ = coord.y * distance;
		var maxChunkX = minChunkX + distance - 1;
		var maxChunkZ = minChunkZ + distance - 1;
		return {
			min: { x:minChunkX, z:minChunkZ },
			max: { x:maxChunkX, z:maxChunkZ },
		};
	}
	
	var toRegion = function (coord, zoom) {
		var scale = Math.pow(2, zoom);
		var regionX = coord.x / scale;
		var regionZ = coord.y / scale;
		return { x:regionX, z:regionZ };
	}
		
	var MCMap = function (container) {
		this.map = new google.maps.Map(container, {
	        zoom: MapConfig.defaultZoom,
	        center: new google.maps.LatLng(0,0),
	        mapTypeControl: false,
	        streetViewControl: false,
	        panControl: true,
		    panControlOptions: {
		        position: google.maps.ControlPosition.LEFT_TOP
		    },
		    zoomControl: true,
		    zoomControlOptions: {
		        style: google.maps.ZoomControlStyle.AUTO,
		        position: google.maps.ControlPosition.LEFT_TOP
		    },
		    overviewMapControl: true
	    });
	    mapOverlay = new MCMapType();
	    this.map.mapTypes.set('jm', mapOverlay);
	    this.map.setMapTypeId('jm');
	};

	var MCMapType = function () {
		this.loadedTiles = {};
		this.projection = new MCMapProjection(MapConfig.tileSize);
		this.tileSize = new google.maps.Size(MapConfig.tileSize,MapConfig.tileSize);
		this.minZoom = MapConfig.minZoom;
		this.maxZoom = MapConfig.maxZoom;
		//this.isPng = true;
	};
	
	MCMapType.prototype.getTileState = function() {
		var mapType = (playerUnderground === true && showCaves === true) ? "underground" : (isNightMap === true ? "night" : "day");
		var dimension = (JM.player.dimension);
		var depth = (JM.player && JM.player.chunkCoordY != undefined) ? JM.player.chunkCoordY : 4;
		return "&mapType=" + mapType + "&dim=" + dimension + "&depth=" + depth + "&ts=" + lastImageCheck;
	}
	
	// Adapted from http://code.martinpearman.co.uk/deleteme/MyOverlayMap.js
	MCMapType.prototype.getTile = function (coord, zoom, ownerDocument) {
		var me = this;

		zoom = Math.floor(zoom);
		
		var tileUrl = "/tile?zoom=" + zoom + "&x=" + coord.x + "&z=" + coord.y;
		var tileId = 'x_' + coord.x + '_y_' + coord.y + '_zoom_' + zoom;
		
		var tile = ownerDocument.createElement('div');
		$(tile).css('width', this.tileSize.height + 'px')
			   .css('width', this.tileSize.height + 'px')
			   .css('height', this.tileSize.height + 'px')
			   .attr('data-tileid', tileId);
		
		if (debug) {
			var label = ownerDocument.createElement('span');
			$(label).css('color','white')
			        .css('float','left')
			        .html(coord.toString() + " zoom " + zoom);
			$(tile).append(label);
		}
		
		var img = $('<img>')
			.attr('src', tileUrl += me.getTileState());
		
		if(img.width()>0) {
			$(tile).prepend(img);
		} else {
			$(img).on('load', function() {
				$(tile).prepend(img);
			});
		}
			
		me.loadedTiles[tileId] = {
			tile: tile,
			tileUrl: tileUrl,
			coord: coord,
			zoom: zoom
		}
			
		return tile;
	};
	
	MCMapType.prototype.refreshTile = function(tile) {
		var me = this;
		
		if (debug) console.log(">>> " + "refreshTile " + $(tile).data('tileid'));
		
		var tileData = me.loadedTiles[$(tile).data('tileid')];
		if(tileData) {
			var url = tileData.tileUrl + me.getTileState();			
			$(tile).find('img').attr('src', url);
		}
	}
	
	MCMapType.prototype.refreshTiles = function (force) {
		var me = this;
		
		if(!force) force==false;
		if (debug) console.log(">>> " + "refreshTiles: " + force);
		
		if(force) {			
			for (var tileId in me.loadedTiles) {
				var tileData = me.loadedTiles[tileId];
				var tile = tileData.tile;
				me.refreshTile(tile);
			}
			lastImageCheck = JM.images.queryTime;
			return;
		}
			
		if(JM.images.regions.length===0) {
			if (debug) console.log("No regions have changed: ", JM.images);
			lastImageCheck = JM.images.queryTime || new Date().getTime();
			return;
		}
		
		lastImageCheck = JM.images.queryTime || new Date().getTime();
		
		if (debug) {
			console.log("Regions changed since ", JM.images.since);
			JM.images.regions.forEach(function(region) {
				console.log("\t", region);
			});
		}

		for (var tileId in me.loadedTiles) {
			
			var tileData = me.loadedTiles[tileId];
			if(!tileData) continue;
			
			var tile = tileData.tile;
			var zoom = tileData.zoom;
			var scale = Math.pow(2,zoom);
			var coord = tileData.coord;
			
			var tileRegion = [parseInt(coord.x / scale), parseInt(coord.y / scale)];
						
			JM.images.regions.forEach(function(region) {
				if(tileRegion[0]==region[0] && tileRegion[1]==region[1]) {
					if (debug) console.log("    tile " + coord + " zoom " + zoom + " in region: ", tileRegion);
					me.refreshTile(tile);
					return false;
				} else {
					if (debug) console.log("    tile " + coord + " zoom " + zoom + " not in region: ", tileRegion);
					return true;
				}
			});				
		}
		
	};
	
	MCMapType.prototype.releaseTile = function (tile) {
		delete this.loadedTiles[tile.tileId];
		tile = null;
	};
	
	var getURLParameter = function(name) {
		return decodeURI((RegExp(name + '=' + '(.+?)(&|$)').exec(location.search) || [, null])[1]);
	}
	
	debug = ('true' === getURLParameter('debug'));
	
	// Public API for JourneyMap
	return {
		start : initMessages
	};
})();

/** Adapted from https://port70.net/svn/misc/minecraft/positionweb/active.html */
function MCMapProjection(tileSize) {
	this.tileSize = tileSize;
    this.inverseTileSize = 1.0 / tileSize;
}
  
MCMapProjection.prototype.fromLatLngToPoint = function(latLng) {
	var me = this;
	var x = latLng.lng() * me.tileSize;
	var y = latLng.lat() * me.tileSize;
	return new google.maps.Point(x, y);
};

MCMapProjection.prototype.fromPointToLatLng = function(point) {
	var me = this;
    var lng = point.x * me.inverseTileSize;
    var lat = point.y * me.inverseTileSize;
    return new google.maps.LatLng(lat, lng);
};


/** Google Analytics **/
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

/** OnLoad **/
google.maps.event.addDomListener(window, 'load', function () {    
    JourneyMap.start();
});