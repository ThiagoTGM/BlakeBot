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

package com.github.thiagotgm.blakebot.module.status;

import com.github.thiagotgm.modular_commands.api.CommandContext;
import com.github.thiagotgm.modular_commands.command.annotation.MainCommand;

/**
 * Command that simply replies with "pong!".
 * 
 * @author ThiagoTGM
 * @version 1.0
 * @since 2016-12-31
 */
public class PingCommand {
    
    private static final String NAME = "Ping Command";
    
    @MainCommand(
            name = NAME,
            aliases = "ping",
            description = "Pings the bot, and gets a pong response",
            usage = "{}ping"
    )
    public void pingCommand( CommandContext context ) {
        
        context.getReplyBuilder().withContent( "\u200Bpong!" ).build();    

    }

}
