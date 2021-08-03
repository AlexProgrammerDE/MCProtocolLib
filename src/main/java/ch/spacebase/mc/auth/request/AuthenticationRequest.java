package ch.spacebase.mc.auth.request;

import ch.spacebase.mc.auth.UserAuthentication;

@SuppressWarnings("unused")
public class AuthenticationRequest {

    private final Agent agent;
    private final String username;
    private final String password;
    private final String clientToken;
    private final boolean requestUser = true;

    public AuthenticationRequest(UserAuthentication auth, String username, String password) {
        this.agent = new Agent("Minecraft", 1);
        this.username = username;
        this.clientToken = auth.getClientToken();
        this.password = password;
    }

}
