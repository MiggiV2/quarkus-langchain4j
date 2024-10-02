package io.quarkiverse.langchain4j.test;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.langchain4j.agent.tool.JsonSchemaProperty.type;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ToolProviderTest
{
	private static final Logger log = LoggerFactory.getLogger(ToolProviderTest.class);

	@Inject
	MyServiceWithToolProvider myServiceWithTools;

	@Inject
	MyServiceWithoutToolProvider myServiceButNoTools;

	@ApplicationScoped
	public static class MyCustomToolProvider implements ToolProvider
	{
		@Override
		public ToolProviderResult provideTools(ToolProviderRequest request)
		{
			ToolSpecification toolSpecification = ToolSpecification.builder()
				.name("get_booking_details")
				.description("Returns booking details")
				.addParameter("bookingNumber", type("string"))
				.build();
			ToolExecutor toolExecutor = (t, m) -> "0";
			return ToolProviderResult.builder()
				.add(toolSpecification, toolExecutor)
				.build();
		}
	}

	@RegisterAiService(
		toolProvider = MyCustomToolProvider.class,
		chatLanguageModelSupplier = BlockingChatLanguageModelSupplierTest.MyModelSupplier.class,
		chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class
	)
	interface MyServiceWithToolProvider
	{
		String chat(String msg);
	}

	@RegisterAiService(
		chatLanguageModelSupplier = BlockingChatLanguageModelSupplierTest.MyModelSupplier.class,
		chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class
	)
	interface MyServiceWithoutToolProvider
	{
		String chat(String msg);
	}

	@RegisterExtension
	static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
			.addClasses(MyServiceWithToolProvider.class, ToolProviderTest.MyCustomToolProvider.class, BlockingChatLanguageModelSupplierTest.MyModelSupplier.class));

	@Test
	@ActivateRequestContext
	void testCall() {
		assertThrows(
			IllegalArgumentException.class,
			() -> myServiceWithTools.chat("hello"),
			"Tools are currently not supported by this model"
		);
	}

	@Test
	@ActivateRequestContext
	void testCallNoTools() {
		myServiceButNoTools.chat("hello");
	}
}