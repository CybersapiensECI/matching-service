package co.edu.escuelaing.alphaeci.matching_service.infrastructure.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableFeignClients(basePackages = {
	"co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.profile.client",
	"co.edu.escuelaing.alphaeci.matching_service.infrastructure.external.geolocation.client"
})
public class FeignConfig {

}
