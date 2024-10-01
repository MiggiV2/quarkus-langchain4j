package io.quarkiverse.langchain4j.test;

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

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ToolProviderTest
{
	@Inject
	MyService myService;

	@ApplicationScoped
	public static class MyCustomToolProvider implements ToolProvider
	{
		@Override
		public ToolProviderResult provideTools(ToolProviderRequest request)
		{
			throw new EmptyToolsException();
		}
	}

	public static class EmptyToolsException extends RuntimeException {
	}

	@RegisterAiService(
		toolProvider = MyCustomToolProvider.class,
		chatLanguageModelSupplier = BlockingChatLanguageModelSupplierTest.MyModelSupplier.class,
		chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class
	)
	interface MyService
	{
		String chat(String msg);
	}

	@RegisterExtension
	static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
		.setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
			.addClasses(ToolProviderTest.MyService.class, ToolProviderTest.MyCustomToolProvider.class, BlockingChatLanguageModelSupplierTest.MyModelSupplier.class));

	@Test
	@ActivateRequestContext
	void testCall() {
		assertThrows(
			EmptyToolsException.class,
			() -> myService.chat("hello")
		);
	}
}
