package br.com.tx.storageInterface.configurations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Habilita CORS para todas as rotas
				.allowedOrigins("*") // Substitua pelo seu domínio
                .allowedMethods("GET", "POST", "PUT", "DELETE") // Métodos permitidos
				.allowedHeaders("*"); // Todos os cabeçalhos permitidos
//                .allowCredentials(true); // Permitir cookies
    }
}
