package ForgeBeta;

import ForgeBeta.command.UserInfoCommand;
import ForgeBeta.command.getDestinyRoles;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;


public class MessageHandler {

    public static void BotSetup() {
        //Create new api using bot API Key
        DiscordApi api = new DiscordApiBuilder().setToken(" INSERT DISCORD TOKEN HERE ").login().join();
        System.out.println("You can invite me by using the following url: " + api.createBotInvite());
        //enable logger
        FallbackLoggerConfiguration.setDebug(true);
        FallbackLoggerConfiguration.setTrace(true);
        //add server join listeners
        api.addServerJoinListener(event -> System.out.println("Joined server " + event.getServer().getName()));
        api.addServerLeaveListener(event -> System.out.println("Left server " + event.getServer().getName()));
        api.updateActivity("trying to figure out this fucking Destiny API");

        //add command listeners
        api.addMessageCreateListener(new UserInfoCommand());
        api.addMessageCreateListener(new getDestinyRoles());

        //get roles of destiny server
//        Server DestinyServer = api.getServerById("384395947318181888").get();
//        Collection<Role> DServerRoles = DestinyServer.getRoles();
//        for (Role Role : DServerRoles){
//            System.out.println(Role.getName() + " " + Role.getId());
//        }


    }

}
