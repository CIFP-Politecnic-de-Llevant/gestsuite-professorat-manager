package cat.iesmanacor.webiesmanacordepartaments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableEurekaClient
@EnableFeignClients
@SpringBootApplication
public class WebIesmanacorDepartamentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebIesmanacorDepartamentsApplication.class, args);
	}

}
