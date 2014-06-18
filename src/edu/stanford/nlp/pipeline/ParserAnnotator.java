package edu.stanford.nlp.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CyclicCoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParserQuery;
import edu.stanford.nlp.parser.lexparser.ParserConstraint;
import edu.stanford.nlp.parser.lexparser.ParserAnnotations.ConstraintAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.StringUtils;

/**
 * This class will add parse information to an Annotation.
 * It assumes that the Annotation already contains the tokenized words
 * as a {@code List<List<CoreLabel>>} under
 * {@code DeprecatedAnnotations.WordsPLAnnotation.class}.
 * If the words have POS tags, they will be used.
 *
 * If the input does not already have sentences, it adds parse information
 * to the Annotation under the key
 * {@code DeprecatedAnnotations.ParsePLAnnotation.class} as a {@code List<Tree>}.
 * Otherwise, they are added to each sentence's coremap (get with
 * {@code CoreAnnotations.SentencesAnnotation}) under
 * {@code CoreAnnotations.TreeAnnotation}).
 *
 * @author Jenny Finkel
 */
public class ParserAnnotator implements Annotator {

  private final boolean VERBOSE;
  private final LexicalizedParser parser;

  private final Function<Tree, Tree> treeMap;

  /** Do not parse sentences larger than this sentence length */
  int maxSentenceLength;

  public static final String[] DEFAULT_FLAGS = { "-retainTmpSubcategories" };

  public ParserAnnotator() {
    this(true, -1, DEFAULT_FLAGS);
  }

  public ParserAnnotator(boolean verbose, int maxSent) {
    this(verbose, maxSent, DEFAULT_FLAGS);
  }

  public ParserAnnotator(boolean verbose, int maxSent, String[] flags) {
    this(System.getProperty("parser.model", LexicalizedParser.DEFAULT_PARSER_LOC), verbose, maxSent, flags);
  }

  public ParserAnnotator(String parserLoc,
                         boolean verbose,
                         int maxSent,
                         String[] flags) {
    this(loadModel(parserLoc, verbose, flags), verbose, maxSent);
  }

  public ParserAnnotator(LexicalizedParser parser, boolean verbose, int maxSent) {
    this(parser, verbose, maxSent, null);
  }

  public ParserAnnotator(LexicalizedParser parser, boolean verbose, int maxSent, Function<Tree, Tree> treeMap) {
    VERBOSE = verbose;
    this.parser = parser;
    this.maxSentenceLength = maxSent;
    this.treeMap = treeMap;
  }


  public ParserAnnotator(String annotatorName, Properties props) {
    String model = props.getProperty(annotatorName + ".model");
    if (model == null) {
      throw new IllegalArgumentException("No model specified for " + 
                                         "Parser annotator " + 
                                         annotatorName);
    }
    this.VERBOSE = 
      PropertiesUtils.getBool(props, annotatorName + ".debug", true);
    
    String[] flags = 
      convertFlagsToArray(props.getProperty(annotatorName + ".flags"));
    this.parser = loadModel(model, VERBOSE, flags);
    this.maxSentenceLength = 
      PropertiesUtils.getInt(props, annotatorName + ".maxlen", 
                             Integer.MAX_VALUE);
    // TODO: add a parameter for the treeMap?
    this.treeMap = null;
  }

  public static String[] convertFlagsToArray(String parserFlags) {
    if (parserFlags == null) {
      return DEFAULT_FLAGS;
    } else if (parserFlags.trim().equals("")) {
      return StringUtils.EMPTY_STRING_ARRAY;
    } else {
      return parserFlags.trim().split("\\s+");
    }
  }

  private static LexicalizedParser loadModel(String parserLoc, 
                                                    boolean verbose,
                                                    String[] flags) {
    if (verbose) {
      System.err.println("Loading Parser Model [" + parserLoc + "] ...");
    }
    LexicalizedParser result = LexicalizedParser.loadModel(parserLoc, flags);
    // lp.setOptionFlags(new String[]{"-outputFormat", "penn,typedDependenciesCollapsed", "-retainTmpSubcategories"});
    // treePrint = lp.getTreePrint();
    
    return result;
  }

  public void annotate(Annotation annotation) {
    if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
      // parse a tree for each sentence
      for (CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
        Tree tree = null;
        List<CoreLabel> words = sentence.get(CoreAnnotations.TokensAnnotation.class);
        if (VERBOSE) {
          System.err.println("Parsing: " + words);
        }
        // generate the constituent tree
        if (maxSentenceLength <= 0 || words.size() < maxSentenceLength) {
          List<ParserConstraint> constraints = sentence.get(ConstraintAnnotation.class);
          LexicalizedParserQuery pq = parser.parserQuery();
          pq.setConstraints(constraints);
          pq.parse(words);
          tree = pq.getBestParse();
        } else {
          tree = ParserAnnotatorUtils.xTree(words);
        }

        if (treeMap != null) {
          tree = treeMap.apply(tree);
        }

        ParserAnnotatorUtils.fillInParseAnnotations(VERBOSE, sentence, tree);
      }
    } else {
      throw new RuntimeException("unable to find sentences in: " + annotation);
    }
  }

  @SuppressWarnings("unused")
  private Tree doOneSentence(List<? extends CoreLabel> words) {
    // convert to CyclicCoreLabels because the parser hates CoreLabels
    List<CyclicCoreLabel> newWords = new ArrayList<CyclicCoreLabel>();
    for (CoreLabel fl : words) {
      CyclicCoreLabel ml = new CyclicCoreLabel();
      ml.setWord(fl.word());
      ml.setValue(fl.word());
      newWords.add(ml);
    }

    if(maxSentenceLength <= 0 || newWords.size() < maxSentenceLength) {
      return parser.apply(newWords);
    } else {
      return ParserAnnotatorUtils.xTree(newWords);
    }
  }
}
