package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableFeignClients(basePackages = {
	"com.matchpuff.matchingservice.matching_service.infrastructure.external.profile.client",
	"com.matchpuff.matchingservice.matching_service.infrastructure.external.geolocation.client"
})
public class FeignConfig {

}
