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
	var mapScale = 4;
	var minMapScale = 1;
	var maxMapScale = 10;
	var smoothing = false;
	var mapBounds = {};

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

	var mapBackground = "#112";

	var canvas;
	var bgCanvas;
	var fgCanvas;
	
	var lastImageCheck = 0;
	var skipImageCheck = false;

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
	var otherImage;

	var mobImages = {};
	var chunkScale = mapScale * 16;

	var JmIcon;
	var halted = false;
	var uiInitialized = false;
	var versionChecked = false;
	var updatingMap = false;
	var drawingMap = false;

	var playerOverrideMap = false;
	
	var mapOverlay;

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
		waypoints : [],
		images : null
	};
	
	var RAD_DEG=57.2957795

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

		// Set page language
		$('html').attr('lang', JM.messages.locale.split('_')[0]);

		// Set RSS feed title
		$("link #rssfeed").attr("title", getMessage('rss_feed_title'));

		// Init Day/Night button
		$("#dayNightText").html(getMessage('day_button_text'));
		$("#dayNightButton").attr("title", getMessage('day_button_title'));
		$("#dayNightButton").click(function() {
			playerOverrideMap = true;
			if(isNightMap===true) {
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
		setTextAndTitle("#gridMenuItem", "grid_menu_item_text", "grid_menu_item_title");
		
		// Show menu checkboxes
		$("#checkShowCaves").prop('checked', showCaves)		
		$("#checkShowCaves").click(function(event) {
			showCaves = (this.checked === true);
			postPreference("preference_show_caves", showCaves);
			if (JM.player.underground === true) {
				refreshMap();
			}			
		});
		
		$("#checkShowGrid").prop('checked', showGrid)		
		$("#checkShowGrid").click(function(event) {
			showGrid = (this.checked === true);
			postPreference("preference_show_grid", showGrid);
			drawMap();
		});
		
		$("#checkShowWaypoints").prop('checked', showWaypoints)		
		$("#checkShowWaypoints").click(function(event) {
			showWaypoints = (this.checked === true);
			postPreference("preference_show_waypoints", showWaypoints);
			drawMap();
		});
		if(JM.game && JM.game.waypoints_enabled!==true) {
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
		
		// World info
		$("#worldInfo").hide();
		$("#worldNameLabel").html(getMessage('worldname_text'));
		$("#worldTimeLabel").html(getMessage('worldtime_text'));
		$("#playerBiomeLabel").html(getMessage('biome_text'));
		$("#playerLocationLabel").html(getMessage('location_text'));
		$("#playerElevationLabel").html(getMessage('elevation_text'));

		// Init images
		initImages();

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

		// Ensure the map is sized
		sizeMap();

		var finishUI = function() {

			$(".jmtoggle").each(function() {

				$(this).show()
			});
			$("#slider-vertical").show();
			
			mcMap = new MCMap(document.getElementById('map-canvas'));
			setCenterOnPlayer(true);
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
		
		if(!otherImage) {
			otherImage = new Image();
			otherImage.src = "/img/entity/steve.png";
			otherImage.width = 48;
			otherImage.height = 48;
		}

	}

	/**
	 * Invoke saving map file
	 */
	var saveMapImage = function() {

		alert("TODO");
	}


	var sizeMap = function() {
	}


	function checkBounds() {
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
			if (JM.debug)
				console.log(">>> " + "Error: Can't set mapType: " + mapType);
		}
		
		if(typeChanged && JM.player.underground === false) {		
			if (JM.debug) console.log("setMapType(" + mapType + ")");
			refreshMap();
		}

	}

	function setCenterOnPlayer(onPlayer) {

		centerOnPlayer = onPlayer;
		if (onPlayer === true) {
			drawPlayer();
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
		
		// Params for dirty image check
		var params = "";
		if(mcMap) {
			if(!lastImageCheck) lastImageCheck = new Date().getTime();
			params = "?images.since=" + lastImageCheck;
		}

		// Get all the datas
		fetchData("/data/all" + params, function(data) {
			
			JM.animals = data.animals;
			JM.images = data.images;
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
			if(mcMap) {
				mapOverlay.refreshTiles();
				drawMap();
			}

			if (timerId === null) {
				var dur = JM.game ? JM.game.browser_poll : 1000 || 1000;
				timerId = setInterval(queryServer, Math.max(1000, JM.game.browser_poll));
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

		if (JM.debug)
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
		if (JM.debug) console.log(">>> " + "refreshMap");
		clearTimer();		
		skipImageCheck = true; // Don't need tiles to be checked by queryServer
		queryServer(function(){
			// After data retrieved,force the tile refresh
			skipImageCheck = false;
			lastImageCheck = new Date().getTime();
			mapOverlay.refreshTiles(true); // Force all tiles to be renewed
		});			
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

		var displayError;
		if (data.status === 503 || data.status === 0) {
			displayError = getMessage('error_world_not_opened');
		} else {
			displayError = "";
		}
		
		// Display error
		$('#map-canvas').empty().html("<center><h4 style='color:red;margin-top:30px'>" + displayError + "</h4></center>");
		
		if (JmIcon) {
			$('#map-canvas').prepend(JmIcon);
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

		// Get canvas dimensions
		var canvasWidth = null;
		var canvasHeight = null;
		
		// Refresh GM
		if(mapOverlay) {
			// TODO: only refresh tiles that have been updated
		}

		// mobs
		drawMobs(canvasWidth, canvasHeight);

		// other players
		drawMultiplayers(canvasWidth, canvasHeight);
		
		// waypoints
		drawWaypoints(canvasWidth, canvasHeight);

		// player
		drawPlayer();

		drawingMap = false;

	}

	// Draw the player icon
	var drawPlayer = function(canvasWidth, canvasHeight) {
		
		if (JM.debug)
			console.log(">>> " + "drawPlayer");
		
		// Get current player position
		var pos = blockPosToLatLng(JM.player.posX, JM.player.posZ);
		var heading = JM.player.heading;
		
		// Ensure marker
		if(!mcMap.markers['player']) {

			var img = new Image();
			$(img).attr('id','player-marker')
			      .attr('src','/img/locator-player.png')
			      .css('width','64px')
			      .css('height','64px');
			
			mcMap.markers['player'] = new RichMarker({
				position: pos,
			    map: mcMap.map,
			    draggable: false,
			    flat: true,
			    anchor: RichMarkerPosition.MIDDLE,
			    content: img
	        });
			
			google.maps.event.addListener(mcMap.map, 'dragstart', function() {
				setCenterOnPlayer(false);
			});
			
		} 
		
		// Update marker position and heading
		var marker = mcMap.markers['player'];
		marker.setPosition(pos);
		$('#player-marker').rotate(heading*RAD_DEG);
		
		// Center if needed
		if(centerOnPlayer===true) {
			mcMap.map.panTo(pos);
		}
	}

	// Draw the location of mobs
	var drawMobs = function(canvasWidth, canvasHeight) {

		if (JM.debug)
			console.log(">>> " + "drawMobs");

		var ctx = null; // TODO

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


		return;
		
		var x = getScaledChunkX(mob.posX / 16) - (mapScale * 1.5);
		var z = getScaledChunkZ(mob.posZ / 16) - (mapScale * 1.5);
		
		
		// TODO

		if (x >= 0 && x <= canvasWidth && z >= 0 && z <= canvasHeight) {

			var mobLocator;

			if (mob.hostile !== true && mob.owner) {
				if(mob.owner === JM.player.username) {			
					if (showPets === false)
						return;
					mobLocator = petMobImage;
					ctx.strokeStyle = "#0000ff";
				} else {
					mobLocator = neutralMobImage;
				}
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

			if (ctx.drawImage) {
				var locRadius = mobLocator.width / 2;
				ctx.save();
				ctx.globalAlpha = 1;
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
								
				if(mob.heading>Math.PI) {
					ctx.translate(mobImage.width, 0);
					ctx.scale(-1, 1);
				}
				
				ctx.drawImage(mobImage, 0, 0, radius * 2, radius * 2);
				ctx.restore();
			}
			
			// Label if customName exists
			if(mob.customName) {
				// Draw label background			
				ctx.font = "bold 11px Arial";
				ctx.textAlign = "center";
				ctx.fillStyle = "#000";
				
				var labelZ = z + 36; 
				
				// Get label dimensions
				var metrics = ctx.measureText(mob.customName);
				var width = metrics.width + 6;
				ctx.globalAlpha = 0.7;
				ctx.fillRect(x-(width/2), labelZ-12, width, 16);
				
				// Draw label
				ctx.globalAlpha = 1.0;
				ctx.fillStyle = "#fff";
				ctx.fillText(mob.customName, x, labelZ);
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
		
		if(showPlayers!==true)
			return;

		var ctx = null; // TODO
		
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
	var drawWaypoints = function(canvasWidth, canvasHeight) {
		
		return;

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
				if(waypoint.type==1) {
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
				if(waypoint.tType==1) {
					ctx.fillStyle = "#f00";
				} else {
					ctx.fillStyle = waypoint.color;
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
	        streetViewControl: false
	    });
	    mapOverlay = new MCMapType();
	    this.map.mapTypes.set('jm', mapOverlay);
	    this.map.setMapTypeId('jm');
	    this.markers = {};
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
		var mapType = (JM.player && JM.player.underground === true && showCaves === true) ? "underground" : (isNightMap === true ? "night" : "day");
		var dimension = (JM.player.dimension);
		var depth = (JM.player && JM.player.chunkCoordY != undefined) ? JM.player.chunkCoordY : 4;
		return "&mapType=" + mapType + "&dim=" + dimension + "&depth=" + depth + "&ts=" + lastImageCheck;
	}
	
	// Adapted from http://code.martinpearman.co.uk/deleteme/MyOverlayMap.js
	MCMapType.prototype.getTile = function (coord, zoom, ownerDocument) {
		var me = this;

		var tileUrl = "/tile?zoom=" + zoom + "&x=" + coord.x + "&z=" + coord.y;
		var tileId = 'x_' + coord.x + '_y_' + coord.y + '_zoom_' + zoom;
		
		var tile = ownerDocument.createElement('div');
		$(tile).css('width', this.tileSize.height + 'px')
			   .css('width', this.tileSize.height + 'px')
			   .css('height', this.tileSize.height + 'px')
			   .attr('data-tileid', tileId);
		
		if (JM.debug) {
			var label = ownerDocument.createElement('span');
			$(label).css('color','white')
			        .css('float','left')
			        .html(coord.toString() + " zoom " + zoom);
			$(tile).append(label);
		}
		
		var img = $('<img>')
			.attr('src', tileUrl += me.getTileState())
			.on('load', function() {
				$(tile).prepend(img);
			});
		
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
		
		if (JM.debug) console.log(">>> " + "refreshTile " + $(tile).data('tileid'));
		
		var tileData = me.loadedTiles[$(tile).data('tileid')];
		if(tileData) {
			var url = tileData.tileUrl + me.getTileState();			
			$(tile).find('img').attr('src', url);
		}
	}
	
	MCMapType.prototype.refreshTiles = function (force) {
		var me = this;
		
		if (JM.debug) console.log(">>> " + "refreshTiles " + force||false);
		
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
			if (JM.debug) console.log("No regions have changed: ", JM.images);
			lastImageCheck = JM.images.queryTime || new Date().getTime();
			return;
		}
		
		lastImageCheck = JM.images.queryTime || new Date().getTime();
		
		if (JM.debug) {
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
					if (JM.debug) console.log("    tile " + coord + " zoom " + zoom + " in region: ", tileRegion);
					me.refreshTile(tile);
					return false;
				} else {
					if (JM.debug) console.log("    tile " + coord + " zoom " + zoom + " not in region: ", tileRegion);
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
	
	JM.debug = 'true' === getURLParameter('debug');
	
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