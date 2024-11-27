package br.com.tx.storageInterface;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StorageInterfaceApplication {

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		var context = SpringApplication.run(StorageInterfaceApplication.class, args);
		SpringContext.setSpringContext(context);

		System.out.println("http://localhost:9090/swagger-ui/index.html#/file-manager-controller/fileManager");
	}
	
	
	

}
