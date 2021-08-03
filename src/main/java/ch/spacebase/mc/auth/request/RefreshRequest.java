package ch.spacebase.mc.auth.request;

import ch.spacebase.mc.auth.GameProfile;
import ch.spacebase.mc.auth.UserAuthentication;

@SuppressWarnings("unused")
public class RefreshRequest {

    private final String clientToken;
    private final String accessToken;
    private final GameProfile selectedProfile;
    private final boolean requestUser;

    public RefreshRequest(UserAuthentication authService) {
        this(authService, null);
    }

    public RefreshRequest(UserAuthentication authService, GameProfile profile) {
        this.requestUser = true;
        this.clientToken = authService.getClientToken();
        this.accessToken = authService.getAccessToken();
        this.selectedProfile = profile;
    }

}
