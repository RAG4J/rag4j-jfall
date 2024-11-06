package org.rag4j;

import org.rag4j.integrations.openai.OpenAIChatService;
import org.rag4j.integrations.openai.OpenAIEmbedder;
import org.rag4j.integrations.openai.OpenAIFactory;
import org.rag4j.integrations.weaviate.WeaviateAccess;
import org.rag4j.integrations.weaviate.retrieval.WeaviateRetriever;
import org.rag4j.rag.embedding.Embedder;
import org.rag4j.rag.generation.AnswerGenerator;
import org.rag4j.rag.generation.chat.ChatService;
import org.rag4j.rag.retrieval.RetrievalOutput;
import org.rag4j.rag.retrieval.RetrievalStrategy;
import org.rag4j.rag.retrieval.Retriever;
import org.rag4j.rag.retrieval.strategies.DocumentRetrievalStrategy;
import org.rag4j.rag.retrieval.strategies.HierarchicalRetrievalStrategy;
import org.rag4j.rag.retrieval.strategies.TopNRetrievalStrategy;
import org.rag4j.rag.retrieval.strategies.WindowRetrievalStrategy;
import org.rag4j.util.keyloader.KeyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The focus for this class is on the retrieval of the best matching chunks from the
 * content store and the construction of the context for an LLM to answer the question
 * using retrieval strategies. We use Weaviate to experiment with different retrieval strategies.
 * Below are the tasks you need to complete.
 *
 * TODO 1: Experiment with different retrieval strategies
 * TODO 2: Use different splitting methods through Weaviate collections
 * TODO 3: Find the best strategy to answer a specific question
 */
public class AppStep2Retrieval {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStep2Retrieval.class);
    private static final KeyLoader keyLoader = new KeyLoader();

    public static void main(String[] args) {
        String[] weaviateCollections = {"JfallOpenAiSentence", "JfallOpenAiMaxToken", "JfallOpenAiSemantic", "JfallOpenAiMaxTokenSentence"};
        Retriever weaviateRetriever = createWeaviateRetriever(weaviateCollections[0]);
        String question = "Who are speaking about RAG?";

        // TODO 1: Check the TODOs inside the retrieveContext method.
        String context = retrieveContext(weaviateRetriever, question);
        LOGGER.info("Retrieved context: {}", context);

        // TODO 2: Try different collections from Weaviate on lines 43/44 and run the main method.
        //  These collections represent sets of chunks created with different splitters.
        //  - JfallOpenAiSentence: chunks created with the SentenceSplitter.
        //  - JfallOpenAiMaxToken: chunks created with the MaxTokenSplitter.
        //  - JfallOpenAiSemantic: chunks created with the SemanticSplitter.
        //  - JfallOpenAiMaxTokenSentence: chunks created with a SplitterChain of MaxTokenSplitter and SentenceSplitter.

        // TODO 3: Remove the System.exit(0) line. Then select the right collection (the splitter) and retrieval
        //  strategy to get an answer to the following question: "Who are speaking about RAG?"
        System.exit(3);
        String answer = retrieveAnswer(question, context);
        LOGGER.info("Retrieved answer: {}", answer);
    }

    /**
     * Define a retrieval strategy and retrieve relevant chunks based on your question.
     */
    private static String retrieveContext(Retriever retriever, String question) {
        // TODO 1: Experiment with different retrieval strategies. Run the main method and
        //  pay attention to the results in the logs. Then change the retrieval strategy and run again
        //  until you've seen the results of all strategies.

        // BEGIN SOLUTION
        RetrievalStrategy retrievalStrategy = null;
        // END SOLUTION
        RetrievalOutput output = retrievalStrategy.retrieve(question, 2);

        for (RetrievalOutput.RetrievalOutputItem chunk : output.getItems()) {
            LOGGER.info("Chunk id: {}", chunk.getChunkId());
            LOGGER.info("Text: {}", chunk.getText());
            LOGGER.info("-------------------------------------------------");
        }

        return output.constructContext();
    }

    /**
     * Feed the question and context to the OpenAI LLM to generate an answer.
     */
    private static String retrieveAnswer(String question, String context) {
        ChatService chatService = new OpenAIChatService(OpenAIFactory.obtainsClient(keyLoader.getOpenAIKey()));
        AnswerGenerator answerGenerator = new AnswerGenerator(chatService);
        return answerGenerator.generateAnswer(question, context);
    }

    /**
     * Create a Weaviate retriever.
     */
    private static Retriever createWeaviateRetriever(String collection) {
        WeaviateAccess weaviateAccess = new WeaviateAccess(keyLoader);
        Embedder embedder = new OpenAIEmbedder(OpenAIFactory.obtainsClient(keyLoader.getOpenAIKey()));
        return new WeaviateRetriever(weaviateAccess, embedder, false, List.of("title", "time", "room", "speakers", "tags"), collection);
    }
}
