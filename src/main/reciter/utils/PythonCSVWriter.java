package main.reciter.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.database.dao.ArticleDao;
import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleKeywords.Keyword;
import main.reciter.model.author.ReCiterAuthor;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.translator.ArticleTranslator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class PythonCSVWriter {

	public static void main(String[] args) throws IOException {

		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedXmlFetcher.setPerformRetrievePublication(false);
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle("fernandes", "helen", "hef9020");
		// Convert PubmedArticle to ReCiterArticle.
		List<ReCiterArticle> reCiterArticleList = ArticleTranslator.translateAll(pubmedArticleList);

		initTermSet(reCiterArticleList);
		System.out.println(termSet.size());
		
		write(reCiterArticleList);
		
	}

	private static Set<String> termSet = new HashSet<String>();
	private static Map<String, Integer> termMap = new HashMap<String, Integer>();
	
	public static void initTermSet(List<ReCiterArticle> reCiterArticleList) {
		for (ReCiterArticle article : reCiterArticleList) {
			String title = article.getArticleTitle().getTitle();
			String[] titleArray = title.split("\\s+");
			for (String t : titleArray) {
				termSet.add(t);
			}

			String journal = article.getJournal().getJournalTitle();
			String[] journalArray = journal.split("\\s+");
			for (String j : journalArray) {
				termSet.add(j);
			}

			for (Keyword keyword : article.getArticleKeywords().getKeywords()) {
				termSet.add(keyword.getKeyword());
			}

			for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
				termSet.add(author.getAuthorName().getCSVFormat());
				if (author.getAffiliation().getAffiliation() != null) {
					String[] affiliationArray = author.getAffiliation().getAffiliation().split("\\s+");
					for (String affiliation : affiliationArray) {
						termSet.add(affiliation);
					}
				}
			}
		}
		int i = 0;
		for (String s : termSet) {
			termMap.put(s, i);
			i++;
		}
		
	}

	public static void write(List<ReCiterArticle> reCiterArticleList) throws IOException {
		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		PrintWriter writer = new PrintWriter("csv_python_output.csv", "UTF-8");
		CSVPrinter printer = new CSVPrinter(writer, format.withDelimiter(','));
		ArticleDao articleDao = new ArticleDao();
		Set<Integer> pmidSet = articleDao.getPmidList("hef9020");

		printer.printRecord(termSet, "is_correct");
		
		for (ReCiterArticle article : reCiterArticleList) {
			int[] termExistArray = new int[termSet.size()];
			
			String title = article.getArticleTitle().getTitle();
			String[] titleArray = title.split("\\s+");
			for (String t : titleArray) {
				termExistArray[termMap.get(t)] = 1;
			}

			String journal = article.getJournal().getJournalTitle();
			String[] journalArray = journal.split("\\s+");
			for (String j : journalArray) {
				termExistArray[termMap.get(j)] = 1;
			}

			for (Keyword keyword : article.getArticleKeywords().getKeywords()) {
				termExistArray[termMap.get(keyword.getKeyword())] = 1;
			}

			for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
				termExistArray[termMap.get(author.getAuthorName().getCSVFormat())] = 1;
				if (author.getAffiliation().getAffiliation() != null) {
					String[] affiliationArray = author.getAffiliation().getAffiliation().split("\\s+");
					for (String affiliation : affiliationArray) {
						termExistArray[termMap.get(affiliation)] = 1;
					}
				}
			}
			for (int index : termExistArray) {
				printer.print(index);
			}
			if (pmidSet.contains(article.getArticleID())) {
				printer.print(1);
			} else {
				printer.print(0);
			}
			printer.println();
		}
		printer.close();
		writer.close();
	}

	public static Set<String> getTermSet() {
		return termSet;
	}

	public static void setTermSet(Set<String> termSet) {
		PythonCSVWriter.termSet = termSet;
	}
}
