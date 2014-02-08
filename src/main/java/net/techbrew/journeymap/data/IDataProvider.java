package net.techbrew.journeymap.data;

import java.util.Map;

/**
 * Interface for data providers.
 * 
 * @author mwoodman
 *
 */
public interface IDataProvider {	
	
	/**
	 * Provide an map of data in key-value form.
	 * @return
	 */
	public Map getMap(Map optionalParams);
	
	/**
	 * Provide a list of all possible keys, whether
	 * or not they appear in the results of getMap().
	 * @return
	 */
	public Enum[] getKeys();
	
	/**
	 * Duration in millis that data is valid.
	 * @return
	 */
	public long getTTL();
	
	/**
	 * Whether the data has expired for any reason.
	 * @return
	 */
	public boolean dataExpired();
}
