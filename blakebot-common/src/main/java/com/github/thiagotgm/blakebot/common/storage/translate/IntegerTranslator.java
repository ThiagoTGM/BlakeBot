/*
 * This file is part of BlakeBot.
 *
 * BlakeBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlakeBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BlakeBot. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.thiagotgm.blakebot.common.storage.translate;

import com.github.thiagotgm.blakebot.common.storage.Data;
import com.github.thiagotgm.blakebot.common.storage.Translator;

/**
 * Translator for Integers.
 * 
 * @version 1.0
 * @author ThiagoTGM
 * @since 2018-09-02
 */
public class IntegerTranslator implements Translator<Integer> {

	@Override
	public Data toData( Integer obj ) throws TranslationException {
		
		return Data.numberData( obj );
		
	}
	
	/**
	 * Retrieves an integer from a Data instance.
	 * <p>
	 * If the value of the data is too large to be stored in an Integer,
	 * {@link Integer#MAX_VALUE} is returned. Similarly, if it is
	 * smaller than than the minimum possible Integer, {@link Integer#MIN_VALUE}
	 * is returned.
	 */
	@Override
	public Integer fromData( Data data ) throws TranslationException {
		
		if ( !data.isNumber() ) {
			throw new TranslationException( "Given data is not a number." );
		}

		long n = data.getNumberInteger();
		return (int) Long.max( Integer.MIN_VALUE, Long.min( Integer.MAX_VALUE, n ) );
		
	}

}
