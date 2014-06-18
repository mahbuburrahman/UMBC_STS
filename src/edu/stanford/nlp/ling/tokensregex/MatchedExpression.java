package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.ChunkAnnotationUtils;
import edu.stanford.nlp.pipeline.CoreMapAttributeAggregator;
import edu.stanford.nlp.util.*;

import java.util.*;

/**
 * Matched Expression represents a chunk of text that was matched from an original segment of text)
 *
 * @author Angel Chang
 */
public class MatchedExpression<T> {
  /** Text representing the matched expression */
  protected String text;

  /** Character offsets (relative to original text) */
  protected Interval<Integer> charOffsets;
  /**Token offsets (relative to original text tokenization) */
  protected Interval<Integer> tokenOffsets;
  /** Chunk offsets (relative to chunking on top of original text) */
  protected Interval<Integer> chunkOffsets;
  protected CoreMap annotation;

  /** Function indicating how to extract an annotation from this expression */
  protected SingleAnnotationExtractor<T> extractFunc;

  protected Object value;
  protected Map<String,String> attributes;

  // Used to disambiguate matched expressions
  double score;
  int order;

  /**
   * Function that takes a CoreMap, applies a extraction function to it, to get a value and annotation.
   */
  public static class SingleAnnotationExtractor<T> implements Function<CoreMap, T> {
    String name;
    double priority;      // Priority/Order in which this rule should be applied with respect to others
    double weight;        // Weight given to the rule (how likely is this rule to fire)
    public Class annotationField;  // Annotation field to apply rule over: text or tokens or numerizedtokens
    public Class resultAnnotationField;  // Annotation field to put new annotation
    public Class resultNestedAnnotationField; // Annotation field for child/nested annotations
    boolean isComposite;
    boolean includeNested = false;
    public Function<CoreMap, T> extractFunc;

    public T apply(CoreMap in) {
      return extractFunc.apply(in);
    }
  }


  public MatchedExpression(MatchedExpression<T> me)
  {
    this.annotation = me.annotation;
    this.extractFunc = me.extractFunc;
    this.text = me.text;
    this.value = me.value;
    this.attributes = me.attributes;
    this.score = me.score;
    this.order = me.order;
    this.charOffsets = me.charOffsets;
    this.tokenOffsets = me.tokenOffsets;
    this.chunkOffsets = me.tokenOffsets;
  }

  public MatchedExpression(CoreMap annotation, SingleAnnotationExtractor<T> extractFunc, double score)
  {
    this.annotation = annotation;
    this.extractFunc = extractFunc;
    this.text = annotation.get(CoreAnnotations.TextAnnotation.class);               
    this.value = extractFunc.apply(annotation);
    this.score = score;
  }

  public MatchedExpression(Interval<Integer> charOffsets, Interval<Integer> tokenOffsets, SingleAnnotationExtractor<T> extractFunc, double score)
  {
    this.charOffsets = charOffsets;
    this.tokenOffsets = tokenOffsets;
    this.chunkOffsets = tokenOffsets;
    this.extractFunc = extractFunc;
    this.score = score;
  }

  public boolean extractAnnotation(SequencePattern.Env env, CoreMap sourceAnnotation)
  {
    Map<Class, CoreMapAttributeAggregator> tokenAggregators = CoreMapAttributeAggregator.DEFAULT_NUMERIC_TOKENS_AGGREGATORS;
    return extractAnnotation(sourceAnnotation, extractFunc.annotationField,
            tokenAggregators, extractFunc.resultAnnotationField, extractFunc.resultNestedAnnotationField);
  }

  protected boolean extractAnnotation(CoreMap sourceAnnotation,
                                      Class tokensAnnotationKey, Map<Class, CoreMapAttributeAggregator> aggregators,
                                      Class extractedChunkAnnotationKey, Class extractedChildrenAnnotationKey)
  {
    if (chunkOffsets != null) {
      annotation = ChunkAnnotationUtils.getMergedChunk((List<? extends CoreMap>) sourceAnnotation.get(tokensAnnotationKey),
              chunkOffsets.getBegin(), chunkOffsets.getEnd(), aggregators );
      if (sourceAnnotation.containsKey(CoreAnnotations.TextAnnotation.class)) {
        ChunkAnnotationUtils.annotateChunkText(annotation, sourceAnnotation);
      }
      if (tokenOffsets != null) {
        if (annotation.get(CoreAnnotations.TokenBeginAnnotation.class) == null) {
          annotation.set(CoreAnnotations.TokenBeginAnnotation.class, tokenOffsets.getBegin());
        }
        if (annotation.get(CoreAnnotations.TokenEndAnnotation.class) == null) {
          annotation.set(CoreAnnotations.TokenEndAnnotation.class, tokenOffsets.getEnd());
        }
      }

      charOffsets = Interval.toInterval(annotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), annotation.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
      tokenOffsets = Interval.toInterval(annotation.get(CoreAnnotations.TokenBeginAnnotation.class),
              annotation.get(CoreAnnotations.TokenEndAnnotation.class), Interval.INTERVAL_OPEN_END);
    } else {
      Integer baseCharOffset = sourceAnnotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
      if (baseCharOffset == null) {
        baseCharOffset = 0;
      }

      chunkOffsets = ChunkAnnotationUtils.getChunkOffsetsUsingCharOffsets((List<? extends CoreMap>) sourceAnnotation.get(tokensAnnotationKey),
              charOffsets.getBegin() + baseCharOffset, charOffsets.getEnd()  + baseCharOffset);
      CoreMap annotation2 = ChunkAnnotationUtils.getMergedChunk((List<? extends CoreMap>) sourceAnnotation.get(tokensAnnotationKey),
              chunkOffsets.getBegin(), chunkOffsets.getEnd(), aggregators );

      annotation = ChunkAnnotationUtils.getAnnotatedChunkUsingCharOffsets(sourceAnnotation, charOffsets.getBegin(), charOffsets.getEnd());
      tokenOffsets = Interval.toInterval(annotation.get(CoreAnnotations.TokenBeginAnnotation.class),
              annotation.get(CoreAnnotations.TokenEndAnnotation.class), Interval.INTERVAL_OPEN_END);
      annotation.set(tokensAnnotationKey, annotation2.get(tokensAnnotationKey));
    }
    if (extractedChunkAnnotationKey != null) {
      annotation.set(extractedChunkAnnotationKey, this);
    }
    if (extractedChildrenAnnotationKey != null) {
      annotation.set(extractedChildrenAnnotationKey, annotation.get(tokensAnnotationKey));
    }
    text = annotation.get(CoreAnnotations.TextAnnotation.class);
    value = extractFunc.apply(annotation);
    return true;
  }

  public boolean extractAnnotation(SequencePattern.Env env, List<? extends CoreMap> source)
  {
    return extractAnnotation(source, CoreMapAttributeAggregator.getDefaultAggregators(),
            extractFunc.resultAnnotationField, extractFunc.resultNestedAnnotationField);
  }

  protected boolean extractAnnotation(List<? extends CoreMap> source, Map<Class, CoreMapAttributeAggregator> chunkAggregators,
                                      Class extractedChunkAnnotationKey, Class extractedChildrenAnnotationKey)
  {
    annotation = ChunkAnnotationUtils.getMergedChunk(source,  chunkOffsets.getBegin(), chunkOffsets.getEnd(), chunkAggregators);
    charOffsets = Interval.toInterval(annotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
            annotation.get(CoreAnnotations.CharacterOffsetEndAnnotation.class), Interval.INTERVAL_OPEN_END);
    tokenOffsets = Interval.toInterval(annotation.get(CoreAnnotations.TokenBeginAnnotation.class),
              annotation.get(CoreAnnotations.TokenEndAnnotation.class), Interval.INTERVAL_OPEN_END);
    if (extractedChunkAnnotationKey != null) {
      annotation.set(extractedChunkAnnotationKey, this);
    }
    if (extractedChildrenAnnotationKey != null) {
      annotation.set(extractedChildrenAnnotationKey, source.subList(chunkOffsets.getBegin(), chunkOffsets.getEnd()));
    }
    text = annotation.get(CoreAnnotations.TextAnnotation.class);
    value = extractFunc.apply(annotation);
    return true;
  }

  public Interval<Integer> getCharOffsets() {
    return charOffsets;
  }

  public Interval<Integer> getTokenOffsets() {
    return tokenOffsets;
  }

  public Interval<Integer> getChunkOffsets() {
    return chunkOffsets;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public double getScore() {
    return score;
  }

  public int getOrder() {
    return order;
  }

  public boolean isIncludeNested() {
    return extractFunc.includeNested;
  }

  public void setIncludeNested(boolean includeNested) {
    extractFunc.includeNested = includeNested;
  }

  public String getText() {
    return text;
  }

  public CoreMap getAnnotation() {
    return annotation;
  }

  public Object getValue() { return value; }

  public String toString()
  {
    return text;
  }

  public static List<? extends CoreMap> replaceMerged(List<? extends CoreMap> list, List<? extends MatchedExpression<?>> matchedExprs)
  {
    Collections.sort(matchedExprs, EXPR_TOKEN_OFFSET_COMPARATOR);
    List<CoreMap> merged = new ArrayList<CoreMap>(list.size());   // Approximate size
    int last = 0;
    for (MatchedExpression<?> expr:matchedExprs) {
      int start = expr.chunkOffsets.first();
      int end = expr.chunkOffsets.second();
      if (start >= last) {
        merged.addAll(list.subList(last,start));
        CoreMap m = expr.getAnnotation();
        merged.add(m);
        last = end;
      }
    }
    // Add rest of elements
    if (last < list.size()) {
      merged.addAll(list.subList(last, list.size()));
    }
    return merged;
  }

  public static <T extends MatchedExpression<?>> List<T> removeNested(List<T> chunks)
  {
    if (chunks.size() > 1) {
      // TODO: presort chunks by priority, length, given order
      for (int i = 0; i < chunks.size(); i++) {
        chunks.get(i).order = i;
      }
      return IntervalTree.getNonOverlapping(chunks, EXPR_TO_TOKEN_OFFSETS_INTERVAL_FUNC, EXPR_PRIORITY_LENGTH_COMPARATOR);
    } else {
      return chunks;
    }
  }

  @SuppressWarnings("unused")
  public static Function<CoreMap, Interval<Integer>> COREMAP_TO_TOKEN_OFFSETS_INTERVAL_FUNC =
    new Function<CoreMap, Interval<Integer>>() {
      public Interval<Integer> apply(CoreMap in) {
        return Interval.toInterval(
              in.get(CoreAnnotations.TokenBeginAnnotation.class),
              in.get(CoreAnnotations.TokenEndAnnotation.class));
      }
    };

  public static Function<MatchedExpression<?>, Interval<Integer>> EXPR_TO_TOKEN_OFFSETS_INTERVAL_FUNC =
    new Function<MatchedExpression<?>, Interval<Integer>>() {
      public Interval<Integer> apply(MatchedExpression<?> in) {
        return in.tokenOffsets;
      }
    };

  public static Comparator<MatchedExpression<?>> EXPR_PRIORITY_COMPARATOR =
    new Comparator<MatchedExpression<?>>() {
    public int compare(MatchedExpression<?> e1, MatchedExpression<?> e2) {
      double s1 = e1.getScore();
      double s2 = e2.getScore();
      if (s1 == s2) {
        return 0;
      } else {
        return (s1 > s2)? -1:1;
      }
    }
  };

  public static Comparator<MatchedExpression<?>> EXPR_ORDER_COMPARATOR =
    new Comparator<MatchedExpression<?>>() {
    public int compare(MatchedExpression<?> e1, MatchedExpression<?> e2) {
      int s1 = e1.getOrder();
      int s2 = e2.getOrder();
      if (s1 == s2) {
        return 0;
      } else {
        return (s1 < s2)? -1:1;
      }
    }
  };

  // Compares two matched expressions.
  // Use to order matched expressions by:
  //    length (longest first), then whether it has value or not (has value first),
  // Returns -1 if e1 is longer than e2, 1 if e2 is longer
  // If e1 and e2 are the same length:
  //    Returns -1 if e1 has value, but e2 doesn't (1 if e2 has value, but e1 doesn't)
  //    Otherwise, both e1 and e2 has value or no value
  public static Comparator<MatchedExpression<?>> EXPR_LENGTH_COMPARATOR =
    new Comparator<MatchedExpression<?>>() {
      public int compare(MatchedExpression<?> e1, MatchedExpression<?> e2) {
        if (e1.getValue() == null && e2.getValue() != null) {
          return 1;
        }
        if (e1.getValue() != null && e2.getValue() == null) {
          return -1;
        }
        int len1 = e1.tokenOffsets.getEnd() - e1.tokenOffsets.getBegin();
        int len2 = e2.tokenOffsets.getEnd() - e2.tokenOffsets.getBegin();
        if (len1 == len2) {
          return 0;
        } else {
          return (len1 > len2)? -1:1;
        }
      }
    };

  public static Comparator<MatchedExpression<?>> EXPR_TOKEN_OFFSET_COMPARATOR =
    new Comparator<MatchedExpression<?>>() {
      public int compare(MatchedExpression<?> e1, MatchedExpression<?> e2) {
        return (e1.tokenOffsets.compareTo(e2.tokenOffsets));
      }
    };

  public static Comparator<MatchedExpression<?>> EXPR_TOKEN_OFFSETS_NESTED_FIRST_COMPARATOR =
    new Comparator<MatchedExpression<?>>() {
      public int compare(MatchedExpression<?> e1, MatchedExpression<?> e2) {
        Interval.RelType rel = e1.tokenOffsets.getRelation(e2.tokenOffsets);
        if (rel.equals(Interval.RelType.CONTAIN)) {
          return 1;
        } else if (rel.equals(Interval.RelType.INSIDE)) {
          return -1;
        } else {
          return (e1.tokenOffsets.compareTo(e2.tokenOffsets));
        }
      }
    };

  // Compares two matched expressions.
  // Use to order matched expressions by:
   //   score
  //    length (longest first), then whether it has value or not (has value first),
  //    original order
  //    and then begining token offset (smaller offset first)
  public static Comparator<MatchedExpression<?>> EXPR_PRIORITY_LENGTH_COMPARATOR =
          Comparators.chain(EXPR_PRIORITY_COMPARATOR, EXPR_LENGTH_COMPARATOR, EXPR_ORDER_COMPARATOR, EXPR_TOKEN_OFFSET_COMPARATOR);

}
