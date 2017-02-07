package com.github.thiagotgm.blakebot.module.admin;

import com.github.alphahelix00.discordinator.d4j.handler.CommandHandlerD4J;
import com.github.alphahelix00.ordinator.Ordinator;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

/**
 * Main module manager for the 'admin' module.
 * 
 * @author ThiagoTGM
 * @version 0.1.0
 * @since 2017-02-04
 */
public class AdminModule implements IModule {

    private static final String MODULE_NAME = "Admin Module";
    
    public static final String PREFIX = "^";
    
    static IDiscordClient client;
    
    @Override
    public void disable() {
        
        AdminModule.client = null;

    }

    @Override
    public boolean enable( IDiscordClient arg0 ) {

        AdminModule.client = arg0;
        
        CommandHandlerD4J commandHandler;
        commandHandler = (CommandHandlerD4J) Ordinator.getCommandRegistry().getCommandHandler();
        registerCommands( commandHandler );
        return true;
        
    }

    /**
     * Registers all commands in this module with the command handler.
     * 
     * @param handler Hander the commands should be registered with.
     */
    private void registerCommands( CommandHandlerD4J handler ) {
        
        handler.registerAnnotatedCommands( new BlacklistCommand() );
        
    }

    @Override
    public String getAuthor() {

        return "ThiagoTGM";
        
    }

    @Override
    public String getMinimumDiscord4JVersion() {

        return "2.7.0";
        
    }

    @Override
    public String getName() {

        return MODULE_NAME;
        
    }

    @Override
    public String getVersion() {

        return getClass().getPackage().getImplementationVersion();
        
    }

}