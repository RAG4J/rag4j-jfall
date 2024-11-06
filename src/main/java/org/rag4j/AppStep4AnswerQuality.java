package org.rag4j;

import org.rag4j.integrations.ollama.OllamaAccess;
import org.rag4j.integrations.ollama.OllamaChatService;
import org.rag4j.integrations.ollama.OllamaEmbedder;
import org.rag4j.integrations.weaviate.WeaviateAccess;
import org.rag4j.integrations.weaviate.retrieval.WeaviateRetriever;
import org.rag4j.rag.embedding.Embedder;
import org.rag4j.rag.generation.ObservedAnswerGenerator;
import org.rag4j.rag.generation.chat.ChatService;
import org.rag4j.rag.generation.quality.AnswerQuality;
import org.rag4j.rag.generation.quality.AnswerQualityService;
import org.rag4j.rag.retrieval.RetrievalStrategy;
import org.rag4j.rag.retrieval.Retriever;
import org.rag4j.rag.retrieval.strategies.DocumentRetrievalStrategy;
import org.rag4j.rag.retrieval.strategies.TopNRetrievalStrategy;
import org.rag4j.rag.tracker.LoggingRAGObserverPersistor;
import org.rag4j.rag.tracker.RAGObserver;
import org.rag4j.rag.tracker.RAGObserverPersistor;
import org.rag4j.rag.tracker.RAGTracker;
import org.rag4j.util.keyloader.KeyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.rag4j.integrations.ollama.OllamaConstants.MODEL_LLAMA3;

/**
 * In the previous step you looked at the quality of the retriever. In this step,
 * you will look at the quality of the answer. The quality of the answer is
 * determined by the quality of the answer related to the question and the quality
 * of the answer related to the context. For both cases we use Ollama's LLM to
 * determine the quality of the answer.
 *
 * TODO 1: Understand the mechanism to determine the quality of the answer using one document.
 * TODO 2: Use all the documents in the Weaviate collection to determine the quality of the answer.
 */
public class AppStep4AnswerQuality {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStep4AnswerQuality.class);
    private static final KeyLoader keyLoader = new KeyLoader();

    private static final String source = "Generative AI is here to stay. Tools to generate text, images, or data are now " +
            "common goods. Large Language models (LLMs) only have the knowledge they acquired through learning, and " +
            "even that knowledge does not include all the details. To overcome the knowledge problem, the Retrieval " +
            "Augmented Generation (RAG) pattern arose. An essential part of RAG is the retrieval part. Retrieval is " +
            "not new. The search or retrieval domain is rich with tools, metrics and research. The new kid on the " +
            "block is semantic search using vectors. Vector search got a jump start with the rise of LLMs and RAG.\n\n" +
            "This workshop aims to build a high-quality retriever, integrate the retriever into your LLM solution " +
            "and measure the overall quality of your RAG system.\n\n" +
            "The workshop uses our Rag4j/Rag4p framework, which we created especially for workshops. It is easy to " +
            "learn, so you can focus on understanding and building the details of the components during the workshop. " +
            "You experiment with different chunking mechanisms (sentence, max tokens, semantic). After that, you use " +
            "various strategies to construct the context for the LLM (TopN, Window, Document, Hierarchical). To find " +
            "the optimum combination, you'll use quality metrics for the retriever as well as the other components of " +
            "the RAG system.\n\n" +
            "You can do the workshop using Python or Java. We provide access to a remote LLM. You can also run an " +
            "open-source LLM on Ollama on your local machine.";

    public static void main(String[] args) {
        // TODO 1: Run the code and inspect the quality of the answer.
        String question = "What session does this text describe?";
        String context = source;

        printQualityOfAnswer(question, context);

        // TODO 2: Create a context that can be used to answer the question,
        //  use Weaviate to retrieve the most relevant document, and
        //  construct the context  using an appropriate strategy and determine
        //  the quality of the answer.
//        Retriever retriever = createWeaviateRetriever("JfallOllamaMaxToken");
//        question = "Who are speaking about RAG?";
        // BEGIN SOLUTION
//        RetrievalStrategy retrievalStrategy = null;
//        context = null;
//        printQualityOfAnswer(question, context);
        // END SOLUTION

        // TODO 3: Ask other questions and determine the quality of the answers.
        // TODO 4: Change the collection in the retriever and determine the quality of the answers.
        // TODO 5: Inspect the prompt to determine the quality of the answer, you can try to improve it.
        //  use methods quality_of_answer_to_question_system_prompt and quality_of_answer_from_context_system_prompt
    }

    private static void printQualityOfAnswer(String question, String context) {
        ChatService chatService = new OllamaChatService(new OllamaAccess(), MODEL_LLAMA3);
        ObservedAnswerGenerator observedAnswerGenerator = new ObservedAnswerGenerator(chatService);
        String answer = observedAnswerGenerator.generateAnswer(question, context);
        LOGGER.info("Answer: {}", answer);

        RAGObserver observer = RAGTracker.getRAGObserver();
        RAGTracker.cleanup();

        RAGObserverPersistor persistor = new LoggingRAGObserverPersistor();
        persistor.persist(observer);

        AnswerQualityService answerQuality = new AnswerQualityService(chatService);
        AnswerQuality quality = answerQuality.determineQualityOfAnswer(observer);
        LOGGER.info("Quality of answer compared to the question: {}}, Reason: {}}",
                quality.getAnswerToQuestionQuality().getQuality(), quality.getAnswerToQuestionQuality().getReason());
        LOGGER.info("Quality of answer coming from the context: {}}, Reason {}}",
                quality.getAnswerFromContextQuality().getQuality(), quality.getAnswerFromContextQuality().getReason());
    }

    /**
     * Create a Weaviate retriever.
     */
    private static Retriever createWeaviateRetriever(String collection) {
        WeaviateAccess weaviateAccess = new WeaviateAccess(keyLoader);
        Embedder embedder = new OllamaEmbedder(new OllamaAccess());
        return new WeaviateRetriever(weaviateAccess, embedder, false, List.of("title", "time", "room", "speakers", "tags"), collection);
    }
}
