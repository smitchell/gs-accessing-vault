package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponse;

@SpringBootApplication
public class Application implements CommandLineRunner {

	// 	@Autowired
	// private VaultTemplate vaultTemplate;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		// See https://stackoverflow.com/questions/67096521/error-with-spring-approleauthentication-uri-is-not-absolute
		AppRoleAuthenticationService appRoleAuth = new AppRoleAuthenticationService("6b7b3d81-d367-32e1-7b5d-aaad7160e5ea", "7115c554-eec4-8d2d-75be-43ad7124f290", "127.0.0.0", "http", "8200");
		VaultEndpoint vaultEp = appRoleAuth.vaultEndpoint();
		ClientAuthentication auth = appRoleAuth.clientAuthentication();

		VaultTemplate vaultTemplate = new VaultTemplate(vaultEp, auth);

		// You usually would not print a secret to stdout
		VaultResponse response = vaultTemplate
				.opsForKeyValue("secret", KeyValueBackend.KV_2).get("github");
		System.out.println("Value of github.oauth2.key");
		System.out.println("-------------------------------");
		System.out.println(response.getData().get("github.oauth2.key"));
		System.out.println("-------------------------------");
		System.out.println();

		// Let's encrypt some data using the Transit backend.
		VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();

		// We need to setup transit first (assuming you didn't set up it yet).
		VaultSysOperations sysOperations = vaultTemplate.opsForSys();

		if (!sysOperations.getMounts().containsKey("transit/")) {

			sysOperations.mount("transit", VaultMount.create("transit"));

			transitOperations.createKey("foo-key");
		}

		// Encrypt a plain-text value
		String ciphertext = transitOperations.encrypt("foo-key", "Secure message");

		System.out.println("Encrypted value");
		System.out.println("-------------------------------");
		System.out.println(ciphertext);
		System.out.println("-------------------------------");
		System.out.println();

		// Decrypt

		String plaintext = transitOperations.decrypt("foo-key", ciphertext);

		System.out.println("Decrypted value");
		System.out.println("-------------------------------");
		System.out.println(plaintext);
		System.out.println("-------------------------------");
		System.out.println();
	}
}
