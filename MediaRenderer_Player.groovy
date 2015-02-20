/** 
 *  MediaRenderer Player
 *
 *  Author: SmartThings - Ulises Mujica (Ule)
 *
 *  Version 1.5
 */

preferences {
		input(name: "customDelay", type: "enum", title: "Delay before msg (seconds)", options: ["0","1","2","3","4","5"])
		input(name: "actionsDelay", type: "enum", title: "Delay between actions (seconds)", options: ["0","1","2","4"])
}
metadata {
	// Automatically generated. Make future change here.
	definition (name: "DLNA Player", namespace: "mujica", author: "SmartThings-Mod-Ule") {
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
		capability "Polling"

		attribute "model", "string"
		attribute "trackUri", "string"
		attribute "transportUri", "string"
		attribute "trackNumber", "string"
		attribute "model", "string"
		attribute "doNotDisturb", "string"
		


		command "subscribe"
		command "getVolume"
		command "getCurrentMedia"
		command "getCurrentStatus"
		command "seek"
		command "unsubscribe"
		command "setLocalLevel", ["number"]
		command "tileSetLevel", ["number"]
		command "playTrackAtVolume", ["string","number"]
		command "playTrackAndResume", ["string","number","number"]
		command "playTextAndResume", ["string","number"]
		command "playTrackAndRestore", ["string","number","number"]
		command "playTextAndRestore", ["string","number"]
		command "playSoundAndTrack", ["string","number","json_object","number"]
		command "playTextAndResume", ["string","json_object","number"]
		command "setDoNotDisturb", ["string"]
		command "switchDoNotDisturb"
	}

	// Main
	standardTile("main", "device.status", width: 1, height: 1, canChangeIcon: true) {
		state "playing", label:'Playing', action:"music Player.stop", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#79b821"
		state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "no_media_present", label:'No Media', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		state "grouped", label:'Grouped', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
	}

	// Row 1
	standardTile("nextTrack", "device.status", width: 1, height: 1, decoration: "flat") {
		state "next", label:'', action:"music Player.nextTrack", icon:"st.sonos.next-btn", backgroundColor:"#ffffff"
	}
	standardTile("play", "device.status", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"music Player.play", icon:"st.sonos.play-btn", nextState:"playing", backgroundColor:"#ffffff"
		state "grouped", label:'', action:"music Player.play", icon:"st.sonos.play-btn", backgroundColor:"#ffffff"
	}
	standardTile("previousTrack", "device.status", width: 1, height: 1, decoration: "flat") {
		state "previous", label:'', action:"music Player.previousTrack", icon:"st.sonos.previous-btn", backgroundColor:"#ffffff"
	}

	// Row 2
	standardTile("status", "device.status", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
		state "playing", label:'Playing', action:"music Player.stop", icon:"st.Electronics.electronics16", nextState:"paused", backgroundColor:"#ffffff"
		state "stopped", label:'Stopped', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
		state "no_media_present", label:'No Media', icon:"st.Electronics.electronics16", backgroundColor:"#ffffff"
		state "paused", label:'Paused', action:"music Player.play", icon:"st.Electronics.electronics16", nextState:"playing", backgroundColor:"#ffffff"
	}
	standardTile("stop", "device.status", width: 1, height: 1, decoration: "flat") {
		state "default", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
		state "grouped", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
	}
	standardTile("mute", "device.mute", inactiveLabel: false, decoration: "flat") {
		state "unmuted", label:"", action:"music Player.mute", icon:"st.custom.sonos.unmuted", backgroundColor:"#ffffff", nextState:"muted"
		state "muted", label:"", action:"music Player.unmute", icon:"st.custom.sonos.muted", backgroundColor:"#ffffff", nextState:"unmuted"
	}

	// Row 3
	controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
		state "level", action:"tileSetLevel", backgroundColor:"#ffffff"
	}

	// Row 4
	valueTile("currentSong", "device.trackDescription", inactiveLabel: true, height:1, width:3, decoration: "flat") {
		state "default", label:'${currentValue}', backgroundColor:"#ffffff"
	}

	
	// Row 5
	standardTile("refresh", "device.status", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
	}
	standardTile("doNotDisturb", "device.doNotDisturb", width: 1, height: 1, decoration: "flat", canChangeIcon: true) {
		state "off", label:"MSG Enabled", action:"switchDoNotDisturb", icon:"st.alarm.beep.beep",nextState:"on"
		state "on", label:"MSG Disabled", action:"switchDoNotDisturb", icon:"st.custom.sonos.muted",nextState:"off"
	}
	standardTile("unsubscribe", "device.status", width: 1, height: 1, decoration: "flat") {
		state "previous", label:'Unsubscribe', action:"unsubscribe", backgroundColor:"#ffffff"
	}
	


	main "main"

	details([
		"previousTrack","play","nextTrack",
		"status","stop","mute",
		"levelSliderControl",
		"currentSong",
		"refresh", "doNotDisturb"
		
		
		//,"unsubscribe"
	])
}

// parse events into attributes
def parse(description) {
	def results = []
	try {
		def msg = parseLanMessage(description)
		if (msg.headers)
		{
			def hdr = msg.header.split('\n')[0]
			if (hdr.size() > 36) {
				hdr = hdr[0..35] + "..."
			}

			def uuid = ""
			def sid = ""
			if (msg.headers["SID"])
			{
				sid = msg.headers["SID"]
				sid -= "uuid:"
				sid = sid.trim()

				def pos = sid.lastIndexOf("_")
				if (pos > 0) {
					uuid = sid[0..pos-1]
				}
			}

			if (!msg.body) {
				if (sid) {
					updateSid(sid)
				}
			}
			else if (msg.xml) {

				// Process response to getVolume()
				def node = msg.xml.Body.GetVolumeResponse
				if (node.size()) {
					sendEvent(name: "level",value: node.CurrentVolume.text())
				}

				// Process response to getCurrentStatus()
				node = msg.xml.Body.GetTransportInfoResponse
				if (node.size()) {
					def currentStatus = statusText(node.CurrentTransportState.text())
					if (currentStatus) {
						if (currentStatus != "TRANSITIONING") {
							sendEvent(name: "status", value: currentStatus, data: [source: 'xml.Body.GetTransportInfoResponse'])
							sendEvent(name: "switch", value: currentStatus=="playing" ? "on" : "off", displayed: false)

						}
					}
				}
				node = msg.xml.Body.GetVolumeResponse
				if (node.size()) {
					def currentVolume = statusText(node.CurrentVolume.text())
					if (currentVolume) {
                        sendEvent(name: "level", value: currentVolume, description: description)
					}
				}

				

				// Process subscription update
				node = msg.xml.property.LastChange
				if (node?.text()?.size()>0) {
					
					def xml1 = parseXml(node.text())

					// Play/pause status
					def currentStatus = statusText(xml1.InstanceID.TransportState.'@val'.text())
					if (currentStatus) {
						if (currentStatus != "TRANSITIONING") {
							updateDataValue('currentStatus', currentStatus)
							sendEvent(name: "status", value: currentStatus, data: [source: 'xml.property.LastChange.InstanceID.TransportState'])
							sendEvent(name: "switch", value: currentStatus=="playing" ? "on" : "off", displayed: false)
						}
					}

					// Volume level
					def currentLevel = xml1.InstanceID.Volume.find{it.'@channel' == 'Master'}.'@val'.text()
					if (currentLevel) {
						sendEvent(name: "level", value: currentLevel, description: description)
					}

					// Mute status
					def currentMute = xml1.InstanceID.Mute.find{it.'@channel' == 'Master'}.'@val'.text()
					if (currentMute) {
						def value = currentMute == "1" ? "muted" : "unmuted"
						sendEvent(name: "mute", value: value, descriptionText: "$device.displayName is $value")
					}

					// Track data
					def trackUri = xml1.InstanceID.CurrentTrackURI.'@val'.text()
					def transportUri = xml1.InstanceID.AVTransportURI.'@val'.text()
					def trackNumber = xml1.InstanceID.CurrentTrack.'@val'.text()
										

					
					
					
					if (trackUri.contains("//s3.amazonaws.com/smartapp-") || transportUri.contains("//s3.amazonaws.com/smartapp-") ) {
						log.trace "Skipping event generation for sound file $trackUri"
					}
					else {
						
						transportUri = transportUri ? transportUri : state.transportUri
						transportUri = trackNumber.length() >0 ? transportUri.replaceAll(/fii%3d.*?%26/, "fii%3d${Math.max(trackNumber.toInteger() - 1,0)}%26") : transportUri
						state.transportUri = transportUri
					
						def trackMeta = xml1.InstanceID.CurrentTrackMetaData.'@val'.text()
						def transportMeta = xml1.InstanceID.AVTransportURIMetaData.'@val'.text()
						

						if (trackMeta || transportMeta) {
							def metaDataLoad = trackMeta  ? trackMeta : transportMeta
							def metaData = metaDataLoad?.startsWith("<item") ?  "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">$metaDataLoad</DIDL-Lite>": metaDataLoad 
							def stationMetaXml = metaData ? parseXml(metaData) : null
							
							
							
							// Use the track metadata for song ID unless it's a radio station
							//def trackXml = (trackMeta && !isRadioStation) || !stationMetaXml ? parseXml(trackMeta) : stationMetaXml
							def trackXml = stationMetaXml;
							// Song properties
							def currentName = trackXml.item.title.text()
							def currentArtist = trackXml.item.creator.text()
							def currentAlbum  = trackXml.item.album.text()
							def currentItemId  = trackXml.item.'@id'.text()
							def currentParentId  = trackXml.item.'@parentID'.text()
							def currentTrackDescription = currentName
							
							if (transportUri.contains(currentParentId) && currentItemId){
								transportUri = transportUri.replaceAll(/%26fid%3d.*?%26/, "%26fid%3d$currentItemId%26")
								state.transportUri = transportUri
							}
							
							
							def descriptionText = "$device.displayName is playing $currentTrackDescription"
							if (currentArtist) {
								currentTrackDescription += " - $currentArtist"
								descriptionText += " by $currentArtist"
							}

							// Track Description Event
							sendEvent(name: "trackDescription",
								value: currentTrackDescription,
								descriptionText: descriptionText
							)

							// Have seen cases where there is no engueued or transport metadata. Haven't figured out how to resume in that case
							// so not creating a track data event.
							//
							if (stationMetaXml) {
								// Track Data Event
								// Use track description for the data event description unless it is a queued song (to support resumption & use in mood music)
								def station =  currentName


								def uri = transportUri ?  transportUri : trackUri
								def previousState = device.currentState("trackData")?.jsonValue
								def isDataStateChange = !previousState || (previousState.station != station || previousState.metaData != metaData)

								if (transportUri?.startsWith("x-rincon-queue:")) {
									updateDataValue("queueUri", transportUri)
								}

								def trackDataValue = [
									station: station,
									name: currentName,
									artist: currentArtist,
									album: currentAlbum,
									trackNumber: trackNumber,
									status: currentStatus,
									level: currentLevel,
									uri: uri,
									trackUri: trackUri,
									transportUri: transportUri,
									enqueuedUri: "",
									metaData: metaData,
								]
								results << createEvent(name: "trackData",
									value: trackDataValue.encodeAsJSON(),
									descriptionText: currentDescription,
									displayed: false,
									isStateChange: isDataStateChange
								)
							}
						}
					}
				}
				if (!results) {
					def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
						.replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
						.replaceAll('\n\n','\n').encodeAsHTML() : ""
					results << createEvent(
						name: "mediaRendererMessage",
						value: "${msg.body.encodeAsMD5()}",
						description: description,
						descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
						data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
						isStateChange: false, displayed: false)
				}
			}
			else {
				def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
					.replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
					.replaceAll('\n\n','\n').encodeAsHTML() : ""
				results << createEvent(
					name: "unknownMessage",
					value: "${msg.body.encodeAsMD5()}",
					description: description,
					descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
					data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
					isStateChange: true, displayed: true)
			}
		}
	}
	catch (Throwable t) {
		//results << createEvent(name: "parseError", value: "$t")
		sendEvent(name: "parseError", value: "$t", description: description)
		throw t
	}
	results
}

def installed() {
	def result = [delayAction(5000)]
	result << refresh()
	result.flatten()
}

def on(){
	play()
}

def off(){
	stop()
}



def setModel(String model)
{
	sendEvent(name:"model",value:model,isStateChange:true)
}

def setAVTCURL(String avtcurl)
{
	state.avtcurl = avtcurl
}
def setAVTEURL(String avteurl)
{
	state.avteurl = avteurl
}
def setRCCURL(String rccurl)
{
	state.rccurl = rccurl
}
def setRCEURL(String rceurl)
{
	state.rceurl = rceurl
}


def poll() {
	refresh()
}

def refresh() {
	def eventTime = new Date().time
	if( eventTime > state.secureEventTime ?:0)
	{
		def result = subscribe()
		result << getCurrentStatus()
		result << getVolume()
		result.flatten()
	}
}

def setLevel(val)
{
	setLocalLevel(val)
}

def tileSetLevel(val)
{
	setLocalLevel(val)

}
def setDoNotDisturb(val)
{
	sendEvent(name:"doNotDisturb",value:val,isStateChange:true)
}

// Always sets only this level
def setLocalLevel(val, delay=0) {
	def v = Math.max(Math.min(Math.round(val), 100), 0)
	def result = []
	if (delay) {
		result << delayAction(delay)
	}
	result << mediaRendererAction("SetVolume", "RenderingControl", state.rccurl , [InstanceID: 0, Channel: "Master", DesiredVolume: v])
	//result << delayAction(50)
	result << mediaRendererAction("GetVolume", "RenderingControl", state.rccurl, [InstanceID: 0, Channel: "Master"])
	result
}


private childLevel(previousMaster, newMaster, previousChild)
{
	if (previousMaster) {
		if (previousChild) {
			Math.round(previousChild * (newMaster / previousMaster))
		}
		else {
			newMaster
		}
	}
	else {
		newMaster
	}
}


def play() {
	mediaRendererAction("Play")
	
}

def stop() {
	mediaRendererAction("Stop")
}

def pause() {
	mediaRendererAction("Pause")
}

def nextTrack() {
	mediaRendererAction("Next")
}

def previousTrack() {
	mediaRendererAction("Previous")
}

def seek(trackNumber) {
	mediaRendererAction("Seek", "AVTransport", state.avtcurl , [InstanceID: 0, Unit: "TRACK_NR", Target: trackNumber])
}

def mute()
{
	// TODO - handle like volume?
	mediaRendererAction("SetMute", "RenderingControl", state.rccurl, [InstanceID: 0, Channel: "Master", DesiredMute: 1])
}

def unmute()
{
	// TODO - handle like volume?
	mediaRendererAction("SetMute", "RenderingControl", state.rccurl, [InstanceID: 0, Channel: "Master", DesiredMute: 0])
}

def setPlayMode(mode)
{
	mediaRendererAction("SetPlayMode", [InstanceID: 0, NewPlayMode: mode])
}
def switchDoNotDisturb(){
	setDoNotDisturb(device.currentValue("doNotDisturb") == "on" ? "off":"on") 
}

def playTextAndResume(text, volume=null)
{
	def sound = textToSpeech(text)
	playTrackAndResume(sound.uri, (sound.duration as Integer) + 1, volume)
}

def playTrackAndResume(uri, duration, volume=null) {
	def eventTime = new Date().time
	def currentTrack = device.currentState("trackData")?.jsonValue
	def currentVolume = device.currentState("level")?.integerValue
	def currentStatus = device.currentValue("status")
	def level = volume as Integer
	def actionsDelayTime = actionsDelay ? (actionsDelay as Integer) * 1000 :0
	def result = []
	if( device.currentValue("doNotDisturb") != "on"  && eventTime > state.secureEventTime ?:0){
		result << mediaRendererAction("Stop")
		if(actionsDelayTime > 0){
			result << delayAction(actionsDelayTime)
		}
		if (level) {
			result << setLocalLevel(level)
			result << delayAction(actionsDelayTime + 500)
		}
		
		result << setTrack(uri)
		if(actionsDelayTime > 0){
			result << delayAction(actionsDelayTime)
		}
		result << mediaRendererAction("Play")
		if (duration == "1"){
			def matcher = uri =~ /[^\/]+.mp3/
			if (matcher){
				duration =  Math.max(Math.round(matcher[0].length()/5), 5) 
			}
		}
		def delayTime = ((duration as Integer) * 1000) 
		if (level) {
			delayTime += 1000
		}
		delayTime = customDelay ? ((customDelay as Integer) * 1000) + delayTime : delayTime
		state.secureEventTime = eventTime + delayTime + 10000
		if (currentTrack ) {
			result << delayAction(delayTime)
			result << mediaRendererAction("Stop")
			if(actionsDelayTime > 0){
				result << delayAction(actionsDelayTime)
			}
			if (level) {
				result << setLocalLevel(currentVolume)
				result << delayAction(actionsDelayTime + 500)
			}
			result << setTrack(currentTrack)
			if (currentStatus == "playing") {
				if(actionsDelayTime > 0){
					result << delayAction(actionsDelayTime)
				}
				result << mediaRendererAction("Play")
			}
		}
		result = result.flatten()
	}
	else{
		log.trace "previous notification in progress or Do Not Disturb Activated"
	}
	result
}

def playTextAndRestore(text, volume=null)
{
	def sound = textToSpeech(text)
	playTrackAndRestore(sound.uri, (sound.duration as Integer) + 1, volume)
}

def playTrackAndRestore(uri, duration, volume=null) {
	def eventTime = new Date().time
	def currentTrack = device.currentState("trackData")?.jsonValue
	def currentVolume = device.currentState("level")?.integerValue
	def currentStatus = device.currentValue("status")
	def level = volume as Integer
	def actionsDelayTime = actionsDelay ? (actionsDelay as Integer) * 1000 :0
	def result = []
	if( device.currentValue("doNotDisturb") != "on"  && eventTime > state.secureEventTime ?:0){
		result << mediaRendererAction("Stop")
		if(actionsDelayTime > 0){
			result << delayAction(actionsDelayTime)
		}
		if (level) {
			result << setLocalLevel(level)
			result << delayAction(actionsDelayTime + 500)
		}
		
		result << setTrack(uri)
		if(actionsDelayTime > 0){
			result << delayAction(actionsDelayTime)
		}
		result << mediaRendererAction("Play")
		if (duration == "1"){
			def matcher = uri =~ /[^\/]+.mp3/
			if (matcher){
				duration =  Math.max(Math.round(matcher[0].length()/5), 5) 
			}
		}
		def delayTime = ((duration as Integer) * 1000) 
		if (level) {
			delayTime += 1000
		}
		delayTime = customDelay ? ((customDelay as Integer) * 1000) + delayTime : delayTime
		state.secureEventTime = eventTime + delayTime + 10000
		if (currentTrack ) {
			result << delayAction(delayTime)
			result << mediaRendererAction("Stop")
			if(actionsDelayTime > 0){
				result << delayAction(actionsDelayTime)
			}
			if (level) {
				result << setLocalLevel(currentVolume)
				result << delayAction(actionsDelayTime + 500)
			}
			result << setTrack(currentTrack)
		}
		result = result.flatten()
	}
	else{
		log.trace "previous notification in progress or Do Not Disturb Activated"
	}
	result
}

def playTextAndTrack(text, trackData, volume=null)
{
	def sound = textToSpeech(text)
	playSoundAndTrack(sound.uri, (sound.duration as Integer) + 1, trackData, volume)
}

def playSoundAndTrack(soundUri, duration, trackData, volume=null) {
	def level = volume as Integer
	def result = []
	result << mediaRendererAction("Stop")
	if (level) {
		result << setLocalLevel(level)
	}

	result << setTrack(soundUri)
	result << mediaRendererAction("Play")

	def delayTime = ((duration as Integer) * 1000)+3000
	result << delayAction(delayTime)

	result << setTrack(trackData)
	result << mediaRendererAction("Play")

	result = result.flatten()
	result
}

def playTrackAtVolume(String uri, volume) {

	def result = []
	result << mediaRendererAction("Stop")
	result << setLocalLevel(volume as Integer)
	result << setTrack(uri, metaData)
	result << mediaRendererAction("Play")
	result.flatten()
}

def playTrack(String uri, metaData="") {
	def result = setTrack(uri, metaData)
	result << mediaRendererAction("Play")
	result.flatten()
}

def playTrack(Map trackData) {
	def result = setTrack(trackData)
	result << mediaRendererAction("Play")
	result.flatten()
}

def setTrack(Map trackData) {
		def data = trackData
		def result = []
		result = setTrack(data.uri, data.metaData)
		result.flatten()
}

def setTrack(String uri, metaData="")
{
	metaData = metaData?:"<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"1\" parentID=\"1\" restricted=\"1\"><upnp:class>object.item.audioItem.musicTrack</upnp:class><upnp:album>SmartThings Catalog</upnp:album><upnp:artist>SmartThings</upnp:artist><upnp:albumArtURI>https://graph.api.smartthings.com/api/devices/icons/st.Entertainment.entertainment2-icn?displaySize=2x</upnp:albumArtURI><dc:title>SmartThings Message</dc:title><res protocolInfo=\"http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0;DLNA.ORG_FLAGS=01500000000000000000000000000000\" >$uri</res></item> </DIDL-Lite>"
	def result = []
	result << mediaRendererAction("SetAVTransportURI", [InstanceID: 0, CurrentURI: uri, CurrentURIMetaData: metaData])
	result
}

def resumeTrack(Map trackData = null) {

	def result = restoreTrack(trackData)
	result << mediaRendererAction("Play")
	result
}

def restoreTrack(Map trackData = null) {

	def result = []
	def data = trackData
	if (!data) {
		data = device.currentState("trackData")?.jsonValue
	}
	if (data) {
		result << mediaRendererAction("SetAVTransportURI", [InstanceID: 0, CurrentURI: data.uri, CurrentURIMetaData: data.metaData])
	}
	else {
		log.warn "Previous track data not found"
	}
	result
}

def playText(String msg) {
	def result = setText(msg)
	result << mediaRendererAction("Play")
}

def setText(String msg) {
		def sound = textToSpeech(msg)
		setTrack(sound.uri)
}

// Custom commands

def subscribe() {
	def result = []
	result << subscribeAction(state.avteurl)
	result << delayAction(10000)
	result << subscribeAction(state.rceurl)
	result
}
def unsubscribe() {
	def result = [
		unsubscribeAction(state.avteurl, device.getDataValue('subscriptionId')),
		unsubscribeAction(state.rceurl, device.getDataValue('subscriptionId')),

		
		unsubscribeAction(state.avteurl, device.getDataValue('subscriptionId1')),
		unsubscribeAction(state.rceurl, device.getDataValue('subscriptionId1')),

		
		unsubscribeAction(state.avteurl, device.getDataValue('subscriptionId2')),
		unsubscribeAction(state.rceurl, device.getDataValue('subscriptionId2'))
	]
	updateDataValue("subscriptionId", "")
	updateDataValue("subscriptionId1", "")
	updateDataValue("subscriptionId2", "")
	result
}

def getVolume()
{
	mediaRendererAction("GetVolume", "RenderingControl", state.rccurl, [InstanceID: 0, Channel: "Master"])
}

def getCurrentMedia()
{
	mediaRendererAction("GetPositionInfo", [InstanceID:0, Channel: "Master"])
}

def getCurrentStatus() //transport info
{
	mediaRendererAction("GetTransportInfo", [InstanceID:0])
}

def getSystemString()
{
	mediaRendererAction("GetString", "SystemProperties", "/SystemProperties/Control", [VariableName: "UMTracking"])
}

private messageFilename(String msg) {
	msg.toLowerCase().replaceAll(/[^a-zA-Z0-9]+/,'_')
}

private getCallBackAddress()
{
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private mediaRendererAction(String action) {
	if(action=="Play"){
		mediaRendererAction(action, "AVTransport", state.avtcurl, [InstanceID:0, Speed:1])
    }else{
		mediaRendererAction(action, "AVTransport", state.avtcurl, [InstanceID:0])
    }
}

private mediaRendererAction(String action, Map body) {
	mediaRendererAction(action, "AVTransport", state.avtcurl, body)
}

private mediaRendererAction(String action, String service, String path, Map body = [InstanceID:0, Speed:1]) {
	def result = new physicalgraph.device.HubSoapAction(
		path:    path ?: "/MediaRenderer/$service/Control",
		urn:     "urn:schemas-upnp-org:service:$service:1",
		action:  action,
		body:    body,
		headers: [Host:getHostAddress(), CONNECTION: "close"]
	)
	result
}

private subscribeAction(path, callbackPath="") {
	def address = getCallBackAddress()
	def ip = getHostAddress()

	def result = new physicalgraph.device.HubAction(
		method: "SUBSCRIBE",
		path: path,
		headers: [
			HOST: ip,
			CALLBACK: "<http://${address}/notify$callbackPath>",
			NT: "upnp:event",
			TIMEOUT: "Second-28800"])
	result
}

private unsubscribeAction(path, sid) {
	def ip = getHostAddress()
	def result = new physicalgraph.device.HubAction(
		method: "UNSUBSCRIBE",
		path: path,
		headers: [
			HOST: ip,
			SID: "uuid:${sid}"])
	result
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}



private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private statusText(s) {
	switch(s) {
		case "PLAYING":
			return "playing"
		case "PAUSED_PLAYBACK":
			return "paused"
		case "STOPPED":
			return "stopped"
		case "NO_MEDIA_PRESENT":
			return "no_media_present"
		default:
			return s
	}
}

private updateSid(sid) {
	if (sid) {
		def sid0 = device.getDataValue('subscriptionId')
		def sid1 = device.getDataValue('subscriptionId1')
		def sid2 = device.getDataValue('subscriptionId2')
		def sidNumber = device.getDataValue('sidNumber') ?: "0"

		if (sidNumber == "0") {
			if (sid != sid1 && sid != sid2) {
				updateDataValue("subscriptionId", sid)
				updateDataValue("sidNumber", "1")
			}
		}
		else if (sidNumber == "1") {
			if (sid != sid0 && sid != sid2) {
				updateDataValue("subscriptionId1", sid)
				updateDataValue("sidNumber", "2")
			}
		}
		else {
			if (sid != sid0 && sid != sid0) {
				updateDataValue("subscriptionId2", sid)
				updateDataValue("sidNumber", "0")
			}
		}
	}
}

private dniFromUri(uri) {
	def segs = uri.replaceAll(/http:\/\/([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+)\/.+/,'$1').split(":")
	def nums = segs[0].split("\\.")
	(nums.collect{hex(it.toInteger())}.join('') + ':' + hex(segs[-1].toInteger(),4)).toUpperCase()
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}