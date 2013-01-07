package net.techbrew.mcjm.client;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Properties;

import net.minecraft.src.ModLoader;
import net.minecraft.src.Packet250CustomPayload;
import net.techbrew.mcjm.JourneyMap;
import net.techbrew.mcjm.log.LogFormatter;

public class ChannelClient {
	
	public static final String CHANNEL_NAME = "JOURNEYMAPCH";
	
	public boolean packetReceived = false;
	public boolean helloSent = false;
	
	private final JourneyMap journeyMap;
	
	public ChannelClient(JourneyMap journeyMap) {
		this.journeyMap = journeyMap;
	}
	
	public void receiveCustomPacket(Packet250CustomPayload payload) {
		
		if(payload.channel.equalsIgnoreCase(CHANNEL_NAME)) {
			String data = new String(payload.data);
			parseWorldProperties(data);
		} 
		
	}
	
	public void reset() {
		packetReceived = false;
		helloSent = false;
	}
	
	public void sendHello() {
		sendCustomPacket(journeyMap.getVersion());
	}
	
	private void sendCustomPacket(String message) {
		Packet250CustomPayload payload = new Packet250CustomPayload();
        payload.channel = CHANNEL_NAME;
        payload.data = message.getBytes(Charset.forName("UTF8"));
        payload.length = payload.data.length;
        ModLoader.clientCustomPayload(payload);
	}
	
	private void parseWorldProperties(String data) {
		Properties properties = new Properties();
		try {
			properties.load(new StringReader(data));
			journeyMap.remoteWorldProperties.putAll(properties);
		} catch (IOException e) {
			journeyMap.getLogger().severe("Could not parse world properties from custom packet: " + data);
			journeyMap.getLogger().severe(LogFormatter.toString(e));
		}
	}
}
