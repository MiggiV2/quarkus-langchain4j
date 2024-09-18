package io.quarkiverse.langchain4j.guardrails;

import java.util.Arrays;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.rag.AugmentationResult;
import io.smallrye.common.annotation.Experimental;

/**
 * An output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets the
 * expectations.
 * <p>
 * Implementation should be exposed as a CDI bean, and the class name configured in {@link OutputGuardrails#value()} annotation.
 * <p>
 * In the case of reprompting, the reprompt message is added to the LLM context and the request is retried.
 * <p>
 * The maximum number of retries is configurable using {@code quarkus.langchain4j.guardrails.max-retries}, defaulting to 3.
 */
@Experimental("This feature is experimental and the API is subject to change")
public interface OutputGuardrail extends Guardrail<OutputGuardrail.OutputGuardrailParams, OutputGuardrailResult> {

    /**
     * Validates the response from the LLM.
     *
     * @param responseFromLLM the response from the LLM
     */
    default OutputGuardrailResult validate(AiMessage responseFromLLM) {
        return failure("Validation not implemented");
    }

    /**
     * Validates the response from the LLM.
     * <p>
     * Unlike {@link #validate(AiMessage)}, this method allows to access the memory and the augmentation result (in the
     * case of a RAG).
     * <p>
     * Implementation must not attempt to write to the memory or the augmentation result.
     *
     * @param params the parameters, including the response from the LLM, the memory (maybe null),
     *        and the augmentation result (maybe null). Cannot be {@code null}
     */
    @Override
    default OutputGuardrailResult validate(OutputGuardrailParams params) {
        return validate(params.responseFromLLM());
    }

    /**
     * Represents the parameter passed to {@link #validate(OutputGuardrailParams)}.
     *
     * @param responseFromLLM the response from the LLM
     * @param memory the memory, can be {@code null} or empty
     * @param augmentationResult the augmentation result, can be {@code null}
     */
    record OutputGuardrailParams(AiMessage responseFromLLM, ChatMemory memory,
            AugmentationResult augmentationResult) implements GuardrailParams {
    }

    /**
     * @return The result of a successful output guardrail validation.
     */
    default OutputGuardrailResult success() {
        return OutputGuardrailResult.success();
    }

    /**
     * @param message A message describing the failure.
     * @return The result of a failed output guardrail validation.
     */
    default OutputGuardrailResult failure(String message) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message)), false);
    }

    /**
     * @param message A message describing the failure.
     * @param cause The exception that caused this failure.
     * @return The result of a failed output guardrail validation.
     */
    default OutputGuardrailResult failure(String message, Throwable cause) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message, cause)), false);
    }

    /**
     * @param message A message describing the failure.
     * @return The result of a fatally failed output guardrail validation, blocking the evaluation of any other subsequent
     *         validation.
     */
    default OutputGuardrailResult fatal(String message) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message)), true);
    }

    /**
     * @param message A message describing the failure.
     * @param cause The exception that caused this failure.
     * @return The result of a fatally failed output guardrail validation, blocking the evaluation of any other subsequent
     *         validation.
     */
    default OutputGuardrailResult fatal(String message, Throwable cause) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message, cause)), true);
    }

    /**
     * @param message A message describing the failure.
     * @return The result of a fatally failed output guardrail validation, blocking the evaluation of any other subsequent
     *         validation and triggering a retry with the same user prompt.
     */
    default OutputGuardrailResult retry(String message) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message, null, true)), true);
    }

    /**
     * @param message A message describing the failure.
     * @param cause The exception that caused this failure.
     * @return The result of a fatally failed output guardrail validation, blocking the evaluation of any other subsequent
     *         validation and triggering a retry with the same user prompt.
     */
    default OutputGuardrailResult retry(String message, Throwable cause) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message, cause, true)), true);
    }

    /**
     * @param message A message describing the failure.
     * @param reprompt The new prompt to be used for the retry.
     * @return The result of a fatally failed output guardrail validation, blocking the evaluation of any other subsequent
     *         validation and triggering a retry with a new user prompt.
     */
    default OutputGuardrailResult reprompt(String message, String reprompt) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message, null, true, reprompt)), true);
    }

    /**
     * @param message A message describing the failure.
     * @param cause The exception that caused this failure.
     * @param reprompt The new prompt to be used for the retry.
     * @return The result of a fatally failed output guardrail validation, blocking the evaluation of any other subsequent
     *         validation and triggering a retry with a new user prompt.
     */
    default OutputGuardrailResult reprompt(String message, Throwable cause, String reprompt) {
        return new OutputGuardrailResult(Arrays.asList(new OutputGuardrailResult.Failure(message, cause, true, reprompt)),
                true);
    }
}