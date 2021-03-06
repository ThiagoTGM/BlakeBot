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

package com.github.thiagotgm.blakebot.module.fun;

import java.io.InputStream;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

/**
 * Command that shows the "For the glory of satan, of course!" meme.
 *
 * @version 1.0
 * @author ThiagoTGM
 * @since 2017-09-14
 */
public class ForTheGloryCommand {

    @MainCommand(
            name = "Why? For the glory of Satan, of course!",
            aliases = "why",
            description = "Shows the best reason to do anything.",
            usage = "{}why"
            )
    public void forTheGlory( CommandContext context ) {
        
        InputStream imageStream = getClass().getResourceAsStream( "/images/ForTheGlory.png" );
        context.getReplyBuilder().withFile( imageStream, "ForTheGlory.png" ).send();
        
    }

}
