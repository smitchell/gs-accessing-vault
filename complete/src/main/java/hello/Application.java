package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.VaultLoginException;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.VaultMount;
import org.springframework.vault.support.VaultResponse;

@SpringBootApplication
public class Application implements CommandLineRunner{

	@Autowired
	private VaultTemplate vaultTemplate;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		try {
			// You usually would not print a secret to stdout
			VaultResponse response = vaultTemplate
					.opsForKeyValue("secret", KeyValueBackend.KV_2).get("github");
			if (response != null && response.getData() != null) {
				System.out.println("Value of github.oauth2.key");
				System.out.println("-------------------------------");
				System.out.println(response.getData().get("github.oauth2.key"));
				System.out.println("-------------------------------");
			} else {
				System.out.println("No data found for key 'github'");
			}

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
		} catch (VaultLoginException e) {
			System.out.println("Vault login failed. Check your configuration: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
