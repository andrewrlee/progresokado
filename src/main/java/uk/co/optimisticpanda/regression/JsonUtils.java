package uk.co.optimisticpanda.regression;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.serializeAllExcept;
import static uk.co.optimisticpanda.regression.Exceptions.propagateAnyError;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonUtils {

	@JsonFilter("filter properties by name")
	private static class PropertyFilterMixIn {
	}

	private final ObjectMapper mapper;
	private final ObjectWriter writer;

	public JsonUtils(final String... fieldsToIgnore) {
		mapper = new ObjectMapper();
		VisibilityChecker<?> checker = mapper.getSerializationConfig().getDefaultVisibilityChecker()
			.withFieldVisibility(ANY)
			.withGetterVisibility(NONE)
			.withSetterVisibility(NONE)
			.withCreatorVisibility(NONE);
		mapper.setVisibilityChecker(checker);
		mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
			private static final long serialVersionUID = 1L;
			@Override
			public Boolean findSerializationSortAlphabetically(Annotated ann) {
				return true;
			}
		});
		FilterProvider filters = new SimpleFilterProvider()
				.addFilter("filter properties by name", serializeAllExcept(fieldsToIgnore));

		mapper.addMixIn(Object.class, PropertyFilterMixIn.class);
		writer = mapper.writer(filters);
	}

	public String toJson(Object o) {
		return propagateAnyError(() -> writer.writeValueAsString(o));
	}
	
	public byte[] toJsonBytes(Object o) {
		return propagateAnyError(() -> writer.writeValueAsBytes(o));
	}
}
