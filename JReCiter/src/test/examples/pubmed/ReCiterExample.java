package test.examples.pubmed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.database.dao.ArticleDao;
import main.reciter.algorithm.cluster.ReCiterClusterer;
import main.reciter.lucene.DocumentIndexReader;
import main.reciter.lucene.DocumentIndexWriter;
import main.reciter.lucene.DocumentTranslator;
import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.author.AuthorAffiliation;
import main.reciter.model.author.AuthorName;
import main.reciter.model.author.ReCiterAuthor;
import main.reciter.model.author.TargetAuthor;
import main.reciter.utils.Analysis;
import main.reciter.utils.InputCsvParser;
import main.reciter.utils.ReCiterConfigProperty;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.MedlineCitationArticleAuthor;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.scopus.ScopusXmlFetcher;
import main.xml.scopus.model.ScopusEntry;
import main.xml.translator.ArticleTranslator;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReCiterExample {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterExample.class);	

	public static void main(String[] args) throws IOException {
		// Read Property file from config.properties.
		ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
		reCiterConfigProperty.loadProperty("config.properties");

		// Keep track of exeuction time of ReCiter.
		long startTime = System.currentTimeMillis();

		// Run ReCiter.
		ReCiterExample reCiterExample = new ReCiterExample();
		reCiterExample.runExample(reCiterConfigProperty);
		
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Total execution time: " + elapsedTime + " ms.");
	}

	/**
	 * Setup the data to run the ReCiter algorithm.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 */
	public void runExample(ReCiterConfigProperty reCiterConfigProperty) {

		String lastName = reCiterConfigProperty.getLastName();
		String middleName = reCiterConfigProperty.getMiddleName();
		String firstName = reCiterConfigProperty.getFirstName();
		String affiliation = reCiterConfigProperty.getAuthorAffiliation();
		String firstInitial = firstName.substring(0, 1);
		String cwid = reCiterConfigProperty.getCwid();
		
		// Try reading from Lucene Index:
		DocumentIndexReader documentIndexReader = new DocumentIndexReader();

		// Lucene Index doesn't contain this cwid's files.
		if (!documentIndexReader.isIndexed(cwid)) {

			// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
			PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
			pubmedXmlFetcher.setPerformRetrievePublication(reCiterConfigProperty.isPerformRetrievePublication());
			List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial, cwid);

			// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			List<ScopusEntry> scopusEntryList = scopusXmlFetcher.getScopusEntryList(lastName, firstInitial, cwid);

			// Map the pmid to a ScopusEntry.
			Map<String, ScopusEntry> pmidToScopusEntry = new HashMap<String, ScopusEntry>();
			for (ScopusEntry entry : scopusEntryList) {
				pmidToScopusEntry.put(entry.getPubmedID(), entry);
			}

			// Need to integrate the Scopus information into PubmedArticle. Add a fake author which contains the
			// Scopus Affiliation. The fake author has pmid as last name and first name.
			for (PubmedArticle pubmedArticle : pubmedArticleList) {
				String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();

				if (pmidToScopusEntry.containsKey(pmid)) {
					String scopusAffiliation = pmidToScopusEntry.get(pmid).affiliationConcatForm();
					MedlineCitationArticleAuthor fakeAuthor = new MedlineCitationArticleAuthor();
					fakeAuthor.setLastName(pmid);
					fakeAuthor.setForeName(pmid);
					fakeAuthor.setAffiliation(scopusAffiliation);
					if (pubmedArticle.getMedlineCitation().getArticle().getAuthorList() == null) {
						pubmedArticle.getMedlineCitation().getArticle().setAuthorList(new ArrayList<MedlineCitationArticleAuthor>());
					}
					pubmedArticle.getMedlineCitation().getArticle().getAuthorList().add(fakeAuthor);
				}
			}

			// Convert PubmedArticle to ReCiterArticle.
			List<ReCiterArticle> reCiterArticleList = ArticleTranslator.translateAll(pubmedArticleList);

			// Convert ReCiterArticle to Lucene Document
			List<Document> luceneDocumentList = DocumentTranslator.translateAll(reCiterArticleList);

			// If Lucene index already exist for this cwid, read from index. Else write to index.
			// Use Lucene to write to index.
			DocumentIndexWriter docIndexWriter = new DocumentIndexWriter(cwid);
			docIndexWriter.indexAll(luceneDocumentList);
		}

		// Read the index from directory data/lucene and convert the indexed files to a list of ReCiterArticle.
		List<ReCiterArticle> reCiterArticleList = documentIndexReader.readIndex(cwid);

		// Run the Clustering algorithm.

		// Define Singleton target author.
		TargetAuthor.init(new AuthorName(firstName, middleName, lastName), new AuthorAffiliation(affiliation));

		// Sort articles on completeness score.
		Collections.sort(reCiterArticleList);

		// Cluster.
		ReCiterClusterer reCiterClusterer = new ReCiterClusterer();
		reCiterClusterer.setArticleList(reCiterArticleList);

		// Report results.
		ArticleDao articleDao = new ArticleDao();
		Set<Integer> pmidSet = articleDao.getPmidList(cwid);

		Analysis analysis = new Analysis(pmidSet);
		reCiterClusterer.cluster(0.1, 0.1, analysis);	
	}
}