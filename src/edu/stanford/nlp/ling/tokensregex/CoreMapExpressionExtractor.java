package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a list of extraction rules over sequence patterns
 *
 * @author Angel Chang
 */
public class CoreMapExpressionExtractor<T extends MatchedExpression<?>> {
  // TODO: Remove templating of MachedExpressions<?>  (keep for now until TimeExpression rules can be decoupled)
  private Logger logger = Logger.getLogger(CoreMapExpressionExtractor.class.getName());
  TokenSequencePattern.Env env;
  Class tokensAnnotationKey = CoreAnnotations.NumerizedTokensAnnotation.class;
//  Class tokensAnnotationKey = CoreAnnotations.TokensAnnotation.class;
  // Rule to extract matched  expressions directly from tokens
  SequenceMatchRules.ExtractRule<CoreMap, T> basicExtractRule;
  // Rule to extract composite expressions (use when some tokens have already been grouped into matched expressions)
  SequenceMatchRules.ExtractRule<List<? extends CoreMap>, T> compositeExtractRule;

  public CoreMapExpressionExtractor() {
  }

  public CoreMapExpressionExtractor(TokenSequencePattern.Env env) {
    this.env = env;
  }

  public CoreMapExpressionExtractor(TokenSequencePattern.Env env, List<SequenceMatchRules.Rule> rules) {
    this.env = env;
    appendRules(rules);
  }

  public void appendRules(List<SequenceMatchRules.Rule> rules)
  {
    SequenceMatchRules.ListExtractRule<CoreMap, T> basicRules;
    if (basicExtractRule instanceof SequenceMatchRules.ListExtractRule) {
      basicRules = (SequenceMatchRules.ListExtractRule<CoreMap, T>) basicExtractRule;
    } else {
      basicRules = new SequenceMatchRules.ListExtractRule<CoreMap, T>();
      if (basicExtractRule != null) {
        basicRules.addRules(basicExtractRule);
      }
    }
    SequenceMatchRules.ListExtractRule<List<? extends CoreMap>, T> compositeRules;
    if (compositeExtractRule instanceof SequenceMatchRules.ListExtractRule) {
      compositeRules = (SequenceMatchRules.ListExtractRule<List<? extends CoreMap>, T>) compositeExtractRule;
    } else {
      compositeRules = new SequenceMatchRules.ListExtractRule<List<? extends CoreMap>, T>();
      if (compositeExtractRule != null) {
        compositeRules.addRules(compositeExtractRule);
      }
    }
    for (SequenceMatchRules.Rule r:rules) {
      if (r instanceof SequenceMatchRules.AssignmentRule) {
        // Add assignments to environment
        SequenceMatchRules.AssignmentRule ar = (SequenceMatchRules.AssignmentRule) r;
        env.bind(ar.varname, ar.value);
        try {
          ar.value.evaluate(env);
        } catch (Exception ex) {
          // TODO: Throw error indicating unresolved variable....
          logger.log(Level.WARNING, "Error evaluating variable: " + ar.varname, ex);
        }
      } else if (r instanceof SequenceMatchRules.AnnotationExtractRule) {
        SequenceMatchRules.AnnotationExtractRule aer = (SequenceMatchRules.AnnotationExtractRule) r;
        if (aer.isComposite) {
          compositeRules.addRules(aer.extractRule);
        } else {
          basicRules.addRules(aer.extractRule);
        }
      }
    }
    this.basicExtractRule = basicRules;
    this.compositeExtractRule = compositeRules;

  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setExtractRules(SequenceMatchRules.ExtractRule<CoreMap, T> basicExtractRule,
                              SequenceMatchRules.ExtractRule<List<? extends CoreMap>, T> compositeExtractRule)
  {
    this.basicExtractRule = basicExtractRule;
    this.compositeExtractRule = compositeExtractRule;
  }

  public static CoreMapExpressionExtractor createExtractorFromFiles(TokenSequencePattern.Env env, List<String> filenames) throws RuntimeException {
    CoreMapExpressionExtractor extractor = new CoreMapExpressionExtractor(env);
    for (String filename:filenames) {
      try {
        BufferedReader br = IOUtils.getBufferedFileReader(filename);
        TokenSequenceParser parser = new TokenSequenceParser();
        parser.updateExpressionExtractor(extractor, br);
      } catch (Exception ex) {
        throw new RuntimeException("Error parsing file: " + filename, ex);
      }
    }
    return extractor;
  }

  public static CoreMapExpressionExtractor createExtractorFromFile(TokenSequencePattern.Env env, String filename) throws RuntimeException {
    try {
      BufferedReader br = IOUtils.getBufferedFileReader(filename);
      TokenSequenceParser parser = new TokenSequenceParser();
      CoreMapExpressionExtractor extractor = parser.getExpressionExtractor(env, br);
      return extractor;
      /*  String str = IOUtils.slurpFile(filename);
   return createExtractorFromString(env, str); */
    } catch (Exception ex) {
      throw new RuntimeException("Error parsing file: " + filename, ex);
    }
  }

  public static CoreMapExpressionExtractor createExtractorFromString(TokenSequencePattern.Env env, String str) throws IOException, ParseException {
    TokenSequenceParser parser = new TokenSequenceParser();
    CoreMapExpressionExtractor extractor = parser.getExpressionExtractor(env, new StringReader(str));
    return extractor;
  }

  public SequenceMatchRules.Value getValue(String varname)
  {
    SequenceMatchRules.Expression expr = (SequenceMatchRules.Expression) env.get(varname);
    if (expr != null) {
      return expr.evaluate(env);
    } else {
      throw new RuntimeException("Unable get expression for variable " + varname);
    }
  }

  public List<T> extractExpressions(CoreMap annotation)
  {
    // Extract potential expressions
    List<T> matchedExpressions = new ArrayList<T>();
    basicExtractRule.extract(annotation, matchedExpressions);

    annotateExpressions(annotation, matchedExpressions);
    matchedExpressions = MatchedExpression.removeNested(matchedExpressions);

    if (compositeExtractRule != null) {
      // TODO: Get key from coremap
      List<? extends CoreMap> merged = MatchedExpression.replaceMerged(
              (List<? extends CoreMap>) annotation.get(tokensAnnotationKey), matchedExpressions);

      // Apply higher order rules
      boolean done = false;
      while (!done) {
        List<T> newExprs = new ArrayList<T>();
        boolean extracted = compositeExtractRule.extract(merged, newExprs);
        if (extracted) {
          annotateExpressions(merged, newExprs);
          newExprs = MatchedExpression.removeNested(newExprs);
          merged = MatchedExpression.replaceMerged(merged, newExprs);
          matchedExpressions.addAll(newExprs);
          matchedExpressions = MatchedExpression.removeNested(matchedExpressions);
        }
        done = !extracted;
      }
    }
    Collections.sort(matchedExpressions, MatchedExpression.EXPR_TOKEN_OFFSETS_NESTED_FIRST_COMPARATOR);
    return matchedExpressions;
  }

  private void annotateExpressions(CoreMap annotation, List<T> expressions)
  {
    // TODO: Logging can be excessive
    List<MatchedExpression> toDiscard = new ArrayList<MatchedExpression>();
    for (MatchedExpression te:expressions) {
      // Add attributes and all
      try {
        boolean extrackOkay = te.extractAnnotation(env, annotation);
        if (!extrackOkay) {
          // Things didn't turn out so well
          toDiscard.add(te);
          logger.log(Level.WARNING, "Error extracting annotation from " + te /*+ ", " + te.getExtractErrorMessage() */);
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, "Error extracting annotation from " + te, ex);
      }
    }
    expressions.removeAll(toDiscard);
  }

  private void annotateExpressions(List<? extends CoreMap> chunks, List<T> expressions)
  {
    // TODO: Logging can be excessive
    List<MatchedExpression> toDiscard = new ArrayList<MatchedExpression>();
    for (MatchedExpression te:expressions) {
      // Add attributes and all
      try {
        boolean extrackOkay = te.extractAnnotation(env, chunks);
        if (!extrackOkay) {
          // Things didn't turn out so well
          toDiscard.add(te);
          logger.log(Level.WARNING, "Error extracting annotation from " + te /*+ ", " + te.getExtractErrorMessage() */);
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, "Error extracting annotation from " + te, ex);
      }
    }
    expressions.removeAll(toDiscard);
  }

}
