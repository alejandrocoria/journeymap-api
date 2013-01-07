package net.techbrew.mcjm;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.minecraft.client.Minecraft;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetClientHandler;
//import net.minecraft.src.NetworkManager;

public class ReflectionHelper {
	
//	static NetworkManager getNetworkManager() {
//		NetClientHandler nch = Minecraft.getMinecraft().getSendQueue();
//		try
//        {
//			Field[] fields = NetClientHandler.class.getDeclaredFields();
//			for(Field field : fields) {
//				field.setAccessible(true);
//				if(field.getType().equals(NetworkManager.class)) {
//					return (NetworkManager) field.get(nch);
//				}
//			}
//			return null;
//        }
//        catch(IllegalAccessException illegalaccessexception)
//        {
//        	ModLoader.throwException("An impossible error has occured!", illegalaccessexception); //$NON-NLS-1$
//            return null;
//        }
//	}
//	
//	static InetSocketAddress getServerAddress() {
//		NetworkManager nm = getNetworkManager();
//		if(nm!=null) {
//			try
//	        {
//				Field[] fields = NetworkManager.class.getDeclaredFields();
//				for(Field field : fields) {
//					field.setAccessible(true);
//					if(field.getType().equals(SocketAddress.class)) {
//						return (InetSocketAddress) field.get(nm);
//					}
//				}
//				return null;
//	        }
//	        catch(IllegalAccessException illegalaccessexception)
//	        {
//	        	ModLoader.throwException("An impossible error has occured!", illegalaccessexception); //$NON-NLS-1$
//	            return null;
//	        }
//		}
//		return null;
//	}
//	
//	public static String getServerName() {
//		InetSocketAddress address = getServerAddress();
//		if(address!=null) { 
//			return address.getHostName() + "_" + address.getPort(); //$NON-NLS-1$
//		} else {
//			return null;
//		}
//	}
	
	static Frame getMinecraftFrame() {
		Minecraft minecraft = Minecraft.getMinecraft();
		Frame frame = null;
		try
        {
			Field[] fields = Minecraft.class.getDeclaredFields();
			for(Field field : fields) {
				field.setAccessible(true);
				if(field.getType().equals(Frame.class)) {
					return (Frame) field.get(minecraft);
				}
			}
			return null;
        }
        catch(IllegalAccessException illegalaccessexception)
        {
        	ModLoader.throwException("An impossible error has occured!", illegalaccessexception); //$NON-NLS-1$
            return null;
        }
	}

}
