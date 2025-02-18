package hello;

import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

/**
 * See https://stackoverflow.com/questions/67096521/error-with-spring-approleauthentication-uri-is-not-absolute
 */
public class AppRoleAuthenticationService extends AbstractVaultConfiguration {

    private String roleId;
    private String secretId;
    private String host;
    private String scheme;
    private String port;

    public AppRoleAuthenticationService(String roleId, String secretId, String host, String scheme, String port) {
        this.roleId = roleId;
        this.secretId = secretId;
        this.host = host;
        this.scheme = scheme;
        this.port = port;
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        int portInt = Integer.parseInt(port);
        VaultEndpoint ep = VaultEndpoint.create(host, portInt);
        if (scheme != null) {
            ep.setScheme(scheme);
        }

        return ep;
    }

    @Override
    public ClientAuthentication clientAuthentication() {

        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(roleId))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(secretId)).build();

        return new AppRoleAuthentication(options, restOperations());
    }
}
