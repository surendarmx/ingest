package com.bt.orchestration.ingest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		// other mapper configs
		// Customize de-serialization

		/*
		 * JavaTimeModule javaTimeModule = new JavaTimeModule();
		 * javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer());
		 * javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
		 * mapper.registerModule(javaTimeModule);
		 */

		return mapper;
	}

	/*
	 * public class LocalDateSerializer extends JsonSerializer<LocalDate> {
	 * 
	 * @Override public void serialize(LocalDateTime value, JsonGenerator gen,
	 * SerializerProvider serializers) throws IOException {
	 * gen.writeString(value.format(Constant.DATE_TIME_FORMATTER)); } }
	 * 
	 * public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
	 * 
	 * @Override public LocalDate deserialize(JsonParser p, DeserializationContext
	 * ctxt) throws IOException { return LocalDate.parse(p.getValueAsString(),
	 * Constant.DATE_TIME_FORMATTER); } }
	 */
}