package org.rag4j;

import com.azure.ai.openai.OpenAIClient;
import org.rag4j.indexing.InputDocument;
import org.rag4j.indexing.Splitter;
import org.rag4j.indexing.SplitterChain;
import org.rag4j.indexing.splitters.MaxTokenSplitter;
import org.rag4j.indexing.splitters.SectionSplitter;
import org.rag4j.indexing.splitters.SemanticSplitter;
import org.rag4j.indexing.splitters.SentenceSplitter;
import org.rag4j.integrations.openai.OpenAIChatService;
import org.rag4j.integrations.openai.OpenAIEmbedder;
import org.rag4j.integrations.openai.OpenAIFactory;
import org.rag4j.rag.embedding.Embedder;
import org.rag4j.rag.generation.AnswerGenerator;
import org.rag4j.rag.generation.chat.ChatService;
import org.rag4j.rag.generation.knowledge.KnowledgeExtractorService;
import org.rag4j.rag.model.Chunk;
import org.rag4j.rag.model.RelevantChunk;
import org.rag4j.rag.retrieval.RetrievalOutput;
import org.rag4j.rag.retrieval.Retriever;
import org.rag4j.rag.retrieval.strategies.TopNRetrievalStrategy;
import org.rag4j.rag.store.local.InternalContentStore;
import org.rag4j.util.keyloader.KeyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class demonstrates the basic usage of the Rag4j framework.
 * Use the main() method to run the class. The class contains a number
 * of TODOs that you need to complete. Below is a summary of the tasks
 *
 * TODO 1: Check the results of the different basic splitters.
 * TODO 2: Use the SemanticSplitter.
 * TODO 3: Initialize the content store and retrieve best matching chunks.
 * TODO 4: Use a retrieval strategy to answer a question using the context.
 */
public class AppStep1Ingestion {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStep1Ingestion.class);
    private static final KeyLoader keyLoader = new KeyLoader();
    private static final OpenAIClient openAIClient = OpenAIFactory.obtainsClient(keyLoader.getOpenAIKey());

    // Content that is used in this class. It is the description of this workshop.
    private static String source = "Generative AI is here to stay. Tools to generate text, images, or data are now " +
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
        InputDocument inputDocument = InputDocument.builder()
                .documentId("jfall-talk-jettro-daniel")
                .text(source)
                .build();

        // TODO 1: Check TODO in the basicSplitting method.
        // TODO 1: Notice the different results of the splitters.
        List<Chunk> chunks = basicSplitting(inputDocument);

        // TODO 2: Check TODO in the semanticSplitting method and uncomment the line below.
        // TODO 2: Notice the results of the semantic splitter and the difference with the basic splitter.
        // TODO 2: Check the prompt in the class OpenaiKnowledgeExtractor, you can try to improve it.
//        chunks = semanticSplitting(inputDocument);

        printChunks(chunks);
        LOGGER.info("*********************************");


        // Initialize the content store and retrieve best matching chunks.
        // TODO 3: Uncomment the code below and run the main method.
        // TODO 3: Comment the semanticSplitting method call above and use basicSplitting with your preferred splitter.
        // TODO 3: Check the results of the retrieval, notice the scores of the chunks.
        // TODO 3: Try different splitters and compare the results.
//        String question = "What is the workshop about?";
//        InternalContentStore contentStore = initContentStore(chunks);
//        retrieveAndPrintRelevantChunks(question, contentStore);

        // TODO 4: Uncomment the code below and check the results
        // TODO 4: Notice the answer, try the sentence splitter and the semantic splitter
        // TODO 4: Look at the prompt in the AnswerGenerator class, you can try to improve it.
//        String answer = rag(question, contentStore);
//        LOGGER.info("Question: {}", question);
//        LOGGER.info("Answer: {}", answer);
    }

    /**
     * Split the input document into chunks using a basic splitter. You
     * create the splitter and call the split method on the splitter.
     */
    private static List<Chunk> basicSplitting(InputDocument inputDocument) {
        // TODO 1: Check the results of the different splitters. Look at the chunk ids and the text of the chunks
        //  - Initialize the SentenceSplitter and run the main method.
        //  - Replace the SentenceSplitter with a MaxTokenSplitter of 100 tokens and run the main method.
        //  - Replace the MaxTokenSplitter with a SplitterChain containing the following
        //  splitters: SectionSplitter, SentenceSplitter. Run the main method. What differences do you see if you
        //  compare the three?

        Splitter splitter;
        // BEGIN SOLUTION
        splitter = null;
        // END
        return splitter.split(inputDocument);
    }

    /**
     * Split the input document into chunks using a semantic splitter. You
     * create the splitter and call the split method on the splitter.
     */
    private static List<Chunk> semanticSplitting(InputDocument inputDocument) {
        Splitter splitter;
        ChatService chatService = new OpenAIChatService(openAIClient);
        KnowledgeExtractorService service = new KnowledgeExtractorService(chatService);

        // TODO 2: Use the SemanticSplitter to split the source document into chunks.
        //  Uncomment line calling semanticSplitting in the main method above and run the main method.
        //  Check the difference in results between the basic splitters and this semantic splitter.
        //  This splitter uses an LLM to split the text into chunks.

        // BEGIN SOLUTION
        splitter = null;
        // END
        return splitter.split(inputDocument);
    }

    /**
     * Use the provided chunks to initialize the internal content store. The
     * store uses an embedder to create embeddings for the chunks. The
     * store uses an in memory storage to store the chunks.
     */
    private static InternalContentStore initContentStore(List<Chunk> chunks) {
        Embedder embedder = new OpenAIEmbedder(openAIClient);
        InternalContentStore contentStore = new InternalContentStore(embedder);
        contentStore.store(chunks);
        return contentStore;
    }

    /**
     * Retrieve the most relevant chunks for the provided question and print the results.
     */
    private static void retrieveAndPrintRelevantChunks(String question, InternalContentStore contentStore) {
        List<RelevantChunk> relevantChunks = contentStore.findRelevantChunks(question, 2);
        for (RelevantChunk relevantChunk : relevantChunks) {
            LOGGER.info("Document id: {}", relevantChunk.getDocumentId());
            LOGGER.info("Chunk id: {}", relevantChunk.getChunkId());
            LOGGER.info("Text: {}", relevantChunk.getText());
            LOGGER.info("Score: {}", relevantChunk.getScore());
            LOGGER.info("---------------------------------------");
        }
    }

    /**
     * Use a retrieval strategy to find the most relevant chunks for the
     * provided question and answer it using the LLM.
     */
    private static String rag(String question, Retriever contentStore) {
        TopNRetrievalStrategy topNRetrievalStrategy = new TopNRetrievalStrategy(contentStore);
        RetrievalOutput retrieve = topNRetrievalStrategy.retrieve(question, 2);
        ChatService chatService = new OpenAIChatService(openAIClient);
        AnswerGenerator answerGenerator = new AnswerGenerator(chatService);
        return answerGenerator.generateAnswer(question, retrieve.constructContext());
    }

    private static void printChunks(List<Chunk> chunks) {
        LOGGER.info("Number of chunks: {}", chunks.size());
        for (Chunk chunk : chunks) {
            LOGGER.info("Chunk id: {}, {}", chunk.getChunkId(), chunk.getText());
        }
    }
}
