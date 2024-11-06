package org.rag4j;

import com.azure.ai.openai.OpenAIClient;
import org.rag4j.indexing.InputDocument;
import org.rag4j.indexing.Splitter;
import org.rag4j.indexing.splitters.SectionSplitter;
import org.rag4j.integrations.openai.OpenAIChatService;
import org.rag4j.integrations.openai.OpenAIEmbedder;
import org.rag4j.integrations.openai.OpenAIFactory;
import org.rag4j.integrations.weaviate.WeaviateAccess;
import org.rag4j.integrations.weaviate.retrieval.WeaviateRetriever;
import org.rag4j.rag.embedding.Embedder;
import org.rag4j.rag.generation.QuestionGenerator;
import org.rag4j.rag.generation.QuestionGeneratorService;
import org.rag4j.rag.generation.chat.ChatService;
import org.rag4j.rag.model.Chunk;
import org.rag4j.rag.retrieval.ObservedRetriever;
import org.rag4j.rag.retrieval.Retriever;
import org.rag4j.rag.retrieval.quality.QuestionAnswerRecord;
import org.rag4j.rag.retrieval.quality.RetrievalQuality;
import org.rag4j.rag.retrieval.quality.RetrievalQualityService;
import org.rag4j.rag.store.local.InternalContentStore;
import org.rag4j.util.keyloader.KeyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * In this class, you explore the quality of the retriever. You use an LLM to generate
 * questions for all chunks in the document. The question should be answered by the
 * chunk it was generated from. This combination of questions and answers is used to
 * create a judgement list. The quality of the retriever is determined by comparing the
 * expected answers to the actual answers.
 *
 * TODO 1: Learn the concepts with just one document.
 * TODO 2: Use the judgement list for all chunks to determine the quality of the retriever.
 */
public class AppStep3RetrievalQuality {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStep3RetrievalQuality.class);
    private static final KeyLoader keyLoader = new KeyLoader();
    private static final OpenAIClient openAIClient = OpenAIFactory.obtainsClient(keyLoader.getOpenAIKey());

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

        // The next method call creates questions for all chunks in the above document.
        // After that, the quality of the retriever is determined.
        // TODO 1: Run the main method and check the quality of the retriever.
        // TODO 1: Change the splitter to the sentence splitter and check the quality.
        // TODO 1: Inspect the prompt to generate the questions in the class QuestionGenerator.
        qualityForSingleDocument();

        // TODO 2: Comment previous line and uncomment the next line.
        // TODO 2: Inspect the contents of the file jfall_questions_answers.csv.
        // TODO 2: Run the main method and check the quality of the retriever.
//        qualityForAllDocuments();

    }

    public static void qualityForSingleDocument() {
        InputDocument inputDocument = InputDocument.builder()
                .documentId("jfall-talk-jettro-daniel")
                .text(source)
                .build();

        // TODO 1: Change the splitter here.
        Splitter splitter = new SectionSplitter();
        List<Chunk> chunks = splitter.split(inputDocument);

        Embedder embedder = new OpenAIEmbedder(openAIClient);
        InternalContentStore contentStore = new InternalContentStore(embedder);
        contentStore.store(chunks);

        ChatService chatService = new OpenAIChatService(OpenAIFactory.obtainsClient(keyLoader.getOpenAIKey()), "gpt-4o-mini");
        QuestionGenerator questionGenerator = new QuestionGenerator(chatService);
        QuestionGeneratorService questionGeneratorService = new QuestionGeneratorService(contentStore, questionGenerator);
        Path savedFilePath = questionGeneratorService.generateQuestionAnswerPairsAndSaveToTempFile("jfall_questions_answers_sample.csv");

        ObservedRetriever observedRetriever = new ObservedRetriever(contentStore);
        RetrievalQualityService retrievalQualityService = new RetrievalQualityService(observedRetriever);
        List<QuestionAnswerRecord> questionAnswerRecords = retrievalQualityService.readQuestionAnswersFromFilePath(savedFilePath, true);
        RetrievalQuality retrievalQuality = retrievalQualityService.obtainRetrievalQuality(questionAnswerRecords, embedder);

        LOGGER.info("Correct: {}", retrievalQuality.getCorrect());
        LOGGER.info("Incorrect: {}", retrievalQuality.getIncorrect());
        LOGGER.info("Quality using precision: {}", retrievalQuality.getPrecision());
        LOGGER.info("Total questions: {}", retrievalQuality.totalItems());
    }

    public static void qualityForAllDocuments() {
        WeaviateAccess weaviateAccess = new WeaviateAccess(keyLoader);
        Embedder embedder = new OpenAIEmbedder(OpenAIFactory.obtainsClient(keyLoader.getOpenAIKey()));
        Retriever retriever = new WeaviateRetriever(weaviateAccess, embedder, false,
                List.of("title", "time", "room", "speakers", "tags"), "JfallOpenAiMaxToken");

        RetrievalQualityService retrievalQualityService = new RetrievalQualityService(retriever);
        List<QuestionAnswerRecord> questionAnswerRecords = retrievalQualityService.readQuestionAnswersFromFile("/data/jfall/jfall_questions_answers.csv");

        LOGGER.info("Number of question answer records: {}", questionAnswerRecords.size());

        RetrievalQuality retrievalQuality = retrievalQualityService.obtainRetrievalQuality(questionAnswerRecords, embedder);
        LOGGER.info("Correct: {}", retrievalQuality.getCorrect());
        LOGGER.info("Incorrect: {}", retrievalQuality.getIncorrect());
        LOGGER.info("Quality using precision: {}", retrievalQuality.getPrecision());
        LOGGER.info("Total questions: {}", retrievalQuality.totalItems());
    }
}
