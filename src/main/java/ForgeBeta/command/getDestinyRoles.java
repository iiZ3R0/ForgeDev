package ForgeBeta.command;

import ForgeBeta.DestinyUser;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class getDestinyRoles implements MessageCreateListener {

    /*
     * This command can be used to display information about the user who used the command.
     * It's a good example for the MessageAuthor, MessageBuilder and ExceptionLogger class.
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        User MUser;
        // Check if the message content equals "!getroles"
        if (event.getMessage().getContent().equalsIgnoreCase("!getroles") || (event.getMessage().getContent().contains("!getroles") && event.getMessage().getContent().contains("@"))){
            Message m = event.getMessage();

            if(event.getMessage().getContent().contains("@")){
                User MentionedUser = m.getMentionedUsers().get(0);
                MUser = MentionedUser;
            }else {
                //get message info
                MUser = event.getMessage().getAuthor().asUser().get();
            }
            Server MServer = event.getMessage().getServer().get();
            //get users roles
            Collection<Role> MessageAuthorRoles = MUser.getRoles(MServer);
            //get users nickname
            if(!MUser.getNickname(MServer).isPresent() || !MUser.getNickname(MServer).get().contains("#") || !(MUser.getNickname(MServer).get().contains("[EU]") || MUser.getNickname(MServer).get().contains("[NA]") || MUser.getNickname(MServer).get().contains("[EU/NA]")
            || MUser.getNickname(MServer).get().contains("[NA/EU]") || MUser.getNickname(MServer).get().contains("[OCE/EU]") ||  MUser.getNickname(MServer).get().contains("[OCE/NA]"))){
                event.getChannel().sendMessage("Please update your nickname to match your BattleNetID and region ([EU] / [NA/EU] etc). Check #roles-ranks for more info");
                return;
            }
            String UserNickName = MUser.getNickname(MServer).get();
                //remove region from nickname
                UserNickName = UserNickName.replaceAll(".*]", "");
                //replace # with %23 for api use
                DestinyUser DUser = new DestinyUser(UserNickName.replaceAll("#", "%23"));
                boolean ValidUser = false;
                try {
                    //validate user
                    ValidUser = DUser.ValidDestinyUser();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (ValidUser) {
                    try {
                        //if user is valid. Get Character IDs for account
                        DUser.GetCharacterIDs();
                        //count raid completions
                        DUser.CountRaids();
                        //create output message
                        MessageAuthor author = event.getMessage().getAuthor();
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setThumbnail("https://www.bungie.net/common/destiny2_content/icons/c747ddda86261a7916bdcf37f0375da0.png");
                        embed.addInlineField("BattleNetID", "_Validated_");
                        embed.addField("Total Raid Completions", String.valueOf(DUser.getTRaidCount()));
                        embed.addInlineField("Normal Raids", String.valueOf(DUser.getNRaidCount()));
                        embed.addInlineField("Prestige Raids", String.valueOf(DUser.getPRaidCount()));
                        embed.setAuthor(author);
                        embed.setFooter("Requested: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                        embed.setColor(Color.RED);
                        event.getChannel().sendMessage(embed);

                        //assign roles to allocate
                        Role NewRaider = event.getApi().getRoleById("385850260758724620").get();
                        Role NormalRaider = event.getApi().getRoleById("385062829784432642").get();
                        Role PrestigeRaider = event.getApi().getRoleById("385050190530347008").get();
                        Role Questionable = event.getApi().getRoleById("449561870970519562").get();
                        Role Guardian = event.getApi().getRoleById("384401044987183106").get();
                        ArrayList<String> AddRoles = new ArrayList<>();
                        ArrayList<String> RemoveRoles = new ArrayList<>();


                        //if user has less than 5 raid completions give new raider role
                        if (DUser.getTRaidCount() < 5) {
                            if (MessageAuthorRoles.contains(NewRaider)) {
                            } else {
                                AddRoles.add(NewRaider.getName());
                                MUser.addRole(NewRaider).join();
                                Thread.sleep(3000);
                            }
                        }

                        //if user has 5 or more raid completions give normal raider role
                        if (DUser.getTRaidCount() >= 5 && DUser.getPRaidCount() < 5) {
                            if (MessageAuthorRoles.contains(NewRaider)) {
                                AddRoles.add(NormalRaider.getName());
                                RemoveRoles.add(NewRaider.getName());
                                MUser.removeRole(NewRaider).join();
                                MUser.addRole(NormalRaider).join();
                            } else if (MessageAuthorRoles.contains(NormalRaider)) {
                            } else {
                                MUser.addRole(NormalRaider).join();
                                AddRoles.add(NormalRaider.getName());
                                Thread.sleep(3000);
                            }
                        }

                        //if user has 5 or more prestige raid completions give prestige raider role
                        if (DUser.getPRaidCount() >= 5) {
                            if (MessageAuthorRoles.contains(NewRaider)) {
                                AddRoles.add(PrestigeRaider.getName());
                                RemoveRoles.add(NewRaider.getName());
                                MUser.removeRole(NewRaider);
                                MUser.addRole(PrestigeRaider).join();
                            } else if (MessageAuthorRoles.contains(NormalRaider)) {
                                AddRoles.add(PrestigeRaider.getName());
                                RemoveRoles.add(NormalRaider.getName());
                                MUser.removeRole(NormalRaider);
                                MUser.addRole(PrestigeRaider).join();
                            } else if (MessageAuthorRoles.contains(PrestigeRaider)) {
                            } else {
                                AddRoles.add(PrestigeRaider.getName());
                                MUser.addRole(PrestigeRaider).join();
                                Thread.sleep(3000);
                            }
                        }

                        //get roles after allocation
                        Collection<Role> MessageAuthorRolesAfterAllocation = MUser.getRoles(MServer);

                        //check for questionable role
                        if (MessageAuthorRolesAfterAllocation.contains(Questionable) && (MessageAuthorRolesAfterAllocation.contains(NewRaider) || MessageAuthorRolesAfterAllocation.contains(NormalRaider) || MessageAuthorRolesAfterAllocation.contains(PrestigeRaider))) {
                            AddRoles.add(Guardian.getName());
                            RemoveRoles.add(Questionable.getName());
                            MUser.removeRole(Questionable);
                            MUser.addRole(Guardian);
                        } else if (MessageAuthorRolesAfterAllocation.contains(Guardian) && (MessageAuthorRolesAfterAllocation.contains(NewRaider) || MessageAuthorRolesAfterAllocation.contains(NormalRaider) || MessageAuthorRolesAfterAllocation.contains(PrestigeRaider))) {
                        } else if ((!MessageAuthorRolesAfterAllocation.contains(Guardian) && !MessageAuthorRolesAfterAllocation.contains(Questionable)) && (MessageAuthorRolesAfterAllocation.contains(NewRaider) || MessageAuthorRolesAfterAllocation.contains(NormalRaider) || MessageAuthorRolesAfterAllocation.contains(PrestigeRaider))) {
                            MUser.addRole(Guardian);
                            AddRoles.add(Guardian.getName());
                        }

                        String AddRoleString = "None";
                        String RemoveRoleString = "None";

                        if (AddRoles.size()>=2){
                                AddRoleString = StringUtils.join(AddRoles,", ");
                                AddRoleString = AddRoleString+".";
                                //AddRoleString+=(Role + ", ");
                        }

                        if(RemoveRoles.size()>2){
                                RemoveRoleString = StringUtils.join(RemoveRoles,", ");
                                RemoveRoleString = RemoveRoleString+".";
                                //RemoveRoleString+=(Role + ", ");
                        }

                        if (AddRoles.size()==1){
                            for (String Role : AddRoles){
                                AddRoleString+=(Role + ".");
                            }
                        }

                        if(RemoveRoles.size()==1){
                            for (String Role : RemoveRoles){
                                RemoveRoleString+=(Role + ".");
                            }
                        }

                        if (AddRoles.size()>0){
                            AddRoleString = AddRoleString.replaceAll("None", "");
                            event.getChannel().sendMessage("Given Roles: " + AddRoleString);
                        }else if(AddRoles.size()==0){
                            event.getChannel().sendMessage("Given Roles: " + AddRoleString);
                        }

                        if (RemoveRoles.size()>0){
                            RemoveRoleString = RemoveRoleString.replaceAll("None", "");
                            event.getChannel().sendMessage("Removed Roles: " + RemoveRoleString);
                        }else if(RemoveRoles.size()==0){
                            event.getChannel().sendMessage("Removed Roles: " + RemoveRoleString);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //output if user is not valid
                    MessageAuthor author = event.getMessage().getAuthor();
                    EmbedBuilder embed = new EmbedBuilder()
                            .addField("Error:", "Invalid BattlenetID Found. Please check your nickname and try again.", false)
                            .setAuthor(author);
                    event.getChannel().sendMessage(embed);
                }
            }
        }
    }

