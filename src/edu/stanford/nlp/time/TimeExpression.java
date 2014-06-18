package edu.stanford.nlp.time;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchRules;
import edu.stanford.nlp.ling.tokensregex.SequencePattern;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.Interval;

import java.util.List;

/**
 * Time Expression
 *
 *
 * @author Angel Chang
 */
public class TimeExpression extends MatchedExpression<SUTime.Temporal> {
  /**
   * The CoreMap key for storing a TimeExpression annotation
   */
  public static class Annotation implements CoreAnnotation<TimeExpression> {
    public Class<TimeExpression> getType() {
      return TimeExpression.class;
    }
  }

  /**
   * The CoreMap key for storing a nested annotations
   */
  public static class ChildrenAnnotation implements CoreAnnotation<List<? extends CoreMap>> {
    public Class<List<? extends CoreMap>> getType() {
      return ErasureUtils.<Class<List<? extends CoreMap>>> uncheckedCast(List.class);
    }
  }
  //String text; // Text representing the time
  int tid;     // Time ID // TODO: Populate

 /* private Interval<Integer> charOffsets;
  Interval<Integer> tokenOffsets;
  Interval<Integer> chunkOffsets;
  Map<String,String> attributes;  */
 // SUTime.Temporal temporal;
  SUTime.Temporal origTemporal;
/*  CoreMap annotation;
  Function<CoreMap, SUTime.Temporal> temporalFunc;  */
  int anchorTimeId = -1;
//  boolean includeNested = false;

  // Used to disambiguate time expressions
/*  double score;
  int order; */

  public TimeExpression(MatchedExpression expr)
  {
    super(expr);
  }

  public TimeExpression(CoreMap annotation, Function<CoreMap, SUTime.Temporal> temporalFunc, double score)
  {
    super(annotation, getSingleAnnotationExtractor(temporalFunc), score);
  }

  public TimeExpression(Interval<Integer> charOffsets, Interval<Integer> tokenOffsets, Function<CoreMap, SUTime.Temporal> temporalFunc, double score)
  {
    super(charOffsets, tokenOffsets, getSingleAnnotationExtractor(temporalFunc), score);
  }

  private static SingleAnnotationExtractor<SUTime.Temporal> getSingleAnnotationExtractor(Function<CoreMap, SUTime.Temporal> temporalFunc)
  {
    SingleAnnotationExtractor<SUTime.Temporal> extractFunc = new SingleAnnotationExtractor<SUTime.Temporal>();
    extractFunc.extractFunc = temporalFunc;
    extractFunc.annotationField = CoreAnnotations.NumerizedTokensAnnotation.class;
    extractFunc.resultAnnotationField = TimeExpression.Annotation.class;
    extractFunc.resultNestedAnnotationField = TimeExpression.ChildrenAnnotation.class;
    return extractFunc;
  }

  public boolean addMod()
  {
    if (value != null) {
      if (value != SUTime.TIME_NONE_OK) {
        value = TimeExpressionPatterns.addMod(text, getTemporal());
        return true;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }

  public boolean extractAnnotation(SequencePattern.Env env, CoreMap sourceAnnotation)
  {
    boolean okay = super.extractAnnotation(env, sourceAnnotation);
            //super.extractAnnotation(sourceAnnotation, CoreAnnotations.NumerizedTokensAnnotation.class,
            //CoreMapAttributeAggregator.DEFAULT_NUMERIC_TOKENS_AGGREGATORS,
            //TimeExpression.Annotation.class, TimeExpression.ChildrenAnnotation.class);
    if (okay) {
      return addMod();
    } else {
      return false;
    }
  }

  public boolean extractAnnotation(SequencePattern.Env env, List<? extends CoreMap> source)
  {
    boolean okay = super.extractAnnotation(env, source);
            //super.extractAnnotation(source, CoreMapAttributeAggregator.getDefaultAggregators(),
            //TimeExpression.Annotation.class, TimeExpression.ChildrenAnnotation.class);
    if (okay) {
      return addMod();
    } else {
      return false;
    }
  }

  public int getTid() {
    return tid;
  }

  public SUTime.Temporal getTemporal() {
    if (value instanceof SequenceMatchRules.Value) {
      SequenceMatchRules.Value v = ((SequenceMatchRules.Value) value);
      if (v.get() instanceof SUTime.Temporal) {
        return (SUTime.Temporal) v.get();
      }
    } else {
      if (value instanceof SUTime.Temporal) {
        return (SUTime.Temporal) value;
      }
    }
    return null;
  }
  public void setTemporal(SUTime.Temporal temporal) {
    this.value = temporal;
  }

/*  public String toString()
  {
    return text;
  } */

/*  public Timex getTimex(SUTime.TimeIndex timeIndex) {
    Timex timex = temporal.getTimex(timeIndex);
    timex.text = text;
    timex.xml = timex
    assert(timex.tid == tid);
  } */


}
