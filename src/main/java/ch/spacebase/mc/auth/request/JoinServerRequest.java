package ch.spacebase.mc.auth.request;

@SuppressWarnings("unused")
public class JoinServerRequest {

    private final String accessToken;
    private final String selectedProfile;
    private final String serverId;

    public JoinServerRequest(String accessToken, String id, String serverId) {
        this.accessToken = accessToken;
        this.selectedProfile = id;
        this.serverId = serverId;
    }

}
