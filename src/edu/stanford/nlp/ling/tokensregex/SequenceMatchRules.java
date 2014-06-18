package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.util.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rules for matching sequences using regular expressions
 *
 * 2 types of statements/rules:
 * 1. Assignments: Variable name = B
 * 2. Extraction Rules:
 *    Pattern => B
 *
 * Types:
 * - FUNCTION: (input) => (output)
 * - REGEX: Regular expression pattern (TOKENREGEX, STRINGREGEX)
 * - REGEXMATCHVAR - Variable that refers to variable resulting from a regex match
 * - STRING
 * - NUMBER: INTEGER/REAL
 * - COMPOSITE
 * - VAR - Variable
 *
 * @author Angel Chang
 */
public class SequenceMatchRules {
  public static final String TYPE_VAR = "VAR";
  public static final String TYPE_FUNCTION = "FUNCTION";
  public static final String TYPE_REGEX = "REGEX";
  //public static final String TYPE_TOKEN_REGEX = "TOKEN_REGEX";
  public static final String TYPE_REGEXMATCHVAR = "REGEXMATCHVAR";
  public static final String TYPE_STRING = "STRING";
  public static final String TYPE_NUMBER = "NUMBER";
  public static final String TYPE_COMPOSITE = "COMPOSITE";
  public static final String TYPE_SEQUENCE = "SEQUENCE";
  public static final String TYPE_SET = "SET";
  public static final String TYPE_CLASS = "CLASS";
  public static final String TYPE_TOKENS = "TOKENS";

  public static interface Rule {
  }

  public static class AssignmentRule implements Rule {
    String varname;
    Expression value;

    public AssignmentRule(String varname, Expression value) {
      this.varname = varname;
      this.value = value;
    }
  }

  // Input: CoreMap
  //   Annotation field to apply rule over: text or tokens or numerizedtokens
  //   Rule type: token string match, pattern string match
  //   Resulting action: annotate field with extracted values

  public static class AnnotationExtractRule<S, T extends MatchedExpression<?>> implements Rule {
    String name;
    double priority;      // Priority/Order in which this rule should be applied with respect to others
    double weight;        // Weight given to the rule (how likely is this rule to fire)
    Class annotationField;  // Annotation field to apply rule over: text or tokens or numerizedtokens
    Class resultAnnotationField;  // Annotation field to put new annotation
    Class resultNestedAnnotationField; // Annotation field for child/nested annotations
    // TODO: Combine ruleType and isComposite
    String ruleType;
    boolean isComposite;
    boolean includeNested;
    ExtractRule<S, T> extractRule;
  }

  public static interface Expression {
    public Collection<String> getTags();
    public boolean hasTag(String tag);
    public void addTag(String tag);
    public void removeTag(String tag);

    public String getType();
    public Value evaluate(SequencePattern.Env env, Object... args);
  }

  public static class Value<T> implements Expression {
    Set<String> tags;
    String typename;
    T value;
    Value evaluated;

    public Value(String typename, T value, String... tags) {
      this.typename = typename;
      this.value = value;
      if (tags != null) {
        this.tags = CollectionUtils.asSet(tags);
      }
    }

    public Collection<String> getTags() {
      return tags;
    }
    
    public boolean hasTag(String tag) {
      return (tags != null)? tags.contains(tag): false;
    }
    
    public void addTag(String tag) {
      if (tags == null) { tags = new HashSet<String>(1); }
      tags.add(tag);
    }
    
    public void removeTag(String tag) {
      if (tags != null) { tags.remove(tag); }
    }
    
    public String getType() {
      return typename;
    }

    public T get() {
      return value;
    }

    protected Value doEvaluation(SequencePattern.Env env, Object... args) {
      throw new UnsupportedOperationException("Cannot evaluate type: " + typename);
    }

    public Value evaluate(SequencePattern.Env env, Object... args) {
      if (evaluated == null) {
        evaluated = doEvaluation(env, args);
      }
      return evaluated;
    }

    public String toString() {
      return getType() + "(" + value + ")";
    }
  }

  public static class PrimitiveValue<T> extends Value<T> {
    public PrimitiveValue(String typename, T value, String... tags) {
      super(typename, value, tags);
      evaluated = this;
    }
  }

  public static class RegexValue extends Value<String> {
    public RegexValue(String regex, String... tags) {
      super(TYPE_REGEX, regex, tags);
    }

    public Value doEvaluation(SequencePattern.Env env, Object... args) {
      return this;
    }
  }

  public static class VarValue extends Value<String> {
    public VarValue(String varname, String... tags) {
      super(TYPE_VAR, varname, tags);
    }
    public Value doEvaluation(SequencePattern.Env env, Object... args) {
      Expression exp = (Expression) env.get(value);
/*      if (exp != null) {
        return exp.evaluate(env); 
      } else {
        throw new RuntimeException("Unknown variable: " + value);
      } */
      return exp != null? exp.evaluate(env, args): null;
    }
  }

  private static final Pattern DIGITS_PATTERN = Pattern.compile("\\d+");
  public static class RegexMatchVarValue extends Value {
    public RegexMatchVarValue(String groupname, String... tags) {
      super(TYPE_REGEXMATCHVAR, groupname, tags);
    }
    public RegexMatchVarValue(Integer groupid, String... tags) {
      super(TYPE_REGEXMATCHVAR, groupid, tags);
    }
    public static RegexMatchVarValue valueOf(String group) {
      if (DIGITS_PATTERN.matcher(group).matches()) {
        Integer n = Integer.valueOf(group);
        return new RegexMatchVarValue(n);
      } else {
        return new RegexMatchVarValue(group);
      }
    }
    public Value doEvaluation(SequencePattern.Env env, Object... args) {
      if (args != null && args.length > 0) {
        if (args[0] instanceof SequenceMatchResult) {
          SequenceMatchResult mr = (SequenceMatchResult) args[0];
          Object v = get();
          if (v instanceof String) {
            // TODO: depending if TYPE_STRING, use string version...
            return new PrimitiveValue<List>(TYPE_TOKENS, mr.groupNodes((String) v));
          } else if (v instanceof Integer) {
            return new PrimitiveValue<List>(TYPE_TOKENS, mr.groupNodes((Integer) v));
          } else {
            throw new UnsupportedOperationException("String match result must be refered to by group id");
          }
        } else if (args[0] instanceof MatchResult) {
          MatchResult mr = (MatchResult) args[0];
          Object v = get();
          if (v instanceof Integer) {
            String str = mr.group((Integer) get());
            return new PrimitiveValue<String>(TYPE_STRING, str);
          } else {
            throw new UnsupportedOperationException("String match result must be refered to by group id");
          }
        }
      }
      return this;
    }
  }

  public static class CompositeValue extends Value<Map<String,Expression>> {
    public CompositeValue(String... tags) {
      super(TYPE_COMPOSITE, new HashMap<String,Expression>(), tags);
    }

    public CompositeValue(Map<String,Expression> m, boolean isEvaluated, String... tags) {
      super(TYPE_COMPOSITE, m, tags);
      if (isEvaluated) {
        evaluated = this;
      }
    }
    
    public Set<String> getAttributes() {
      return value.keySet();
    }
    
    public Expression getExpression(String attr) {
      return value.get(attr);
    }

    public <T> T get(String attr) {
      Expression expr = value.get(attr);
      if (expr == null) return null;
      if (expr instanceof Value) {
        return ((Value<T>) expr).get();
      }
      throw new UnsupportedOperationException("Expression was not evaluated....");
    }

    private static Value attemptTypeConversion(CompositeValue cv, SequencePattern.Env env, Object... args) {
      Expression typeFieldExpr = cv.value.get("type");
      if (typeFieldExpr != null && typeFieldExpr instanceof Value) {
        Value typeField = (Value) typeFieldExpr;
        // Automatically convert types ....
        if (TYPE_VAR.equals(typeField.getType())) {
          Value typeValue = typeField.evaluate(env, args);
          if (typeValue != null) {
            // TODO: create type ....
            if (TYPE_CLASS.equals(typeValue.getType())) {
              Class c = (Class) typeValue.get();
              try {
                Object obj = c.newInstance();
                for (String s:cv.value.keySet()) {
                  if ("type".equals(s)) {
                    Value v = cv.value.get(s).evaluate(env, args);
                    try {
                      Field f = c.getField(v.getType());
                      f.set(obj, v.get());
                    } catch (NoSuchFieldException ex){
                      throw new RuntimeException("Unknown field " + v.getType() + " for type " + typeField.get(), ex);
                    }
                  }
                }
                return new PrimitiveValue<Object>((String) typeField.get(), obj);
              } catch (InstantiationException ex) {
                throw new RuntimeException("Cannot instantiate " + c, ex);
              } catch (IllegalAccessException ex) {
                throw new RuntimeException("Cannot instantiate " + c, ex);
              }
            } else if (typeValue.get() != null){              
              Class c = typeValue.get().getClass();
              try {
                Method m = c.getMethod("create", CompositeValue.class);
                CompositeValue evaluatedCv = cv.evaluateNoTypeConversion(env, args);
                try {
                  return new PrimitiveValue<Object>((String) typeField.get(), m.invoke(typeValue.get(), evaluatedCv));
                } catch (InvocationTargetException ex) {
                  throw new RuntimeException("Cannot instantiate " + c, ex);
                } catch (IllegalAccessException ex) {
                  throw new RuntimeException("Cannot instantiate " + c, ex);
                }
              } catch (NoSuchMethodException ex) {}
            }
          } else {
            // Predefined types:
            Expression valueField = cv.value.get("value");
            Value value = valueField.evaluate(env, args);
            String type = ((VarValue) typeField).get();
            if (TYPE_CLASS.equals(type)) {
              String className = (String) value.get();
              try {
                return new PrimitiveValue<Class>(TYPE_CLASS, Class.forName(className));
              } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Unknown class " + className, ex);
              }
            } else if (TYPE_STRING.equals(type)) {
              return new PrimitiveValue<String>(TYPE_STRING, (String) value.get());
            } else if (TYPE_REGEX.equals(type)) {
              return new RegexValue((String) value.get());
           /* } else if (TYPE_TOKEN_REGEX.equals(type)) {
              return new PrimitiveValue<TokenSequencePattern>(TYPE_TOKEN_REGEX, (TokenSequencePattern) value.get()); */
            } else if (TYPE_NUMBER.equals(type)) {
              return new PrimitiveValue<Number>(TYPE_NUMBER, (Number) value.get());
            } else {
              // TODO: support other types
              throw new UnsupportedOperationException("Cannot convert type " + type);
            }
          }
        }
      }
      return null;
    }

    private CompositeValue evaluateNoTypeConversion(SequencePattern.Env env, Object... args) {
      Map<String, Expression> m = value;
      Map<String, Expression> res = new HashMap<String,Expression> (m.size());
      for (String s:m.keySet()) {
        res.put(s, m.get(s).evaluate(env, args));
      }
      return new CompositeValue(res, true);
    }

    public Value doEvaluation(SequencePattern.Env env, Object... args) {
      Value v = attemptTypeConversion(this, env, args);
      if (v != null) return v;
      Map<String, Expression> m = value;
      Map<String, Expression> res = new HashMap<String,Expression> (m.size());
      for (String s:m.keySet()) {
        res.put(s, m.get(s).evaluate(env, args));
      }
      return new CompositeValue(res, true);
    }
  }
  
  public static interface ValueFunction extends Function<List<Value>, Value> {}

  public static class FunctionExpression implements Expression {
    String function;
    List<Expression> params;
    Set<String> tags;

    public FunctionExpression(String function, List<Expression> params) {
      this.function = function;
      this.params = params;
    }

    public String getType() {
      return TYPE_FUNCTION;
    }

    public Collection<String> getTags() {
      return tags;
    }

    public boolean hasTag(String tag) {
      return (tags != null)? tags.contains(tag): false;
    }

    public void addTag(String tag) {
      if (tags == null) { tags = new HashSet<String>(1); }
      tags.add(tag);
    }

    public void removeTag(String tag) {
      if (tags != null) { tags.remove(tag); }
    }

    public String toString() {
      StringBuilder sb = new StringBuilder("");
      sb.append(function);
      sb.append("(");
      boolean first = true;
      for (Expression param:params) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(param.toString());
      }
      sb.append(")");
      return sb.toString();
    }

    public Value evaluate(SequencePattern.Env env, Object... args) {
      Object funcValue = env.get(function);
      if (funcValue == null) {
        throw new RuntimeException("Unknown function " + function);
      }
      if (funcValue instanceof Value) {
        funcValue = ((Value) funcValue).evaluate(env, args).get();
      }
      if (funcValue instanceof ValueFunction) {
        ValueFunction f = (ValueFunction) funcValue;
        List<Value> evaled = new ArrayList<Value>();
        for (Expression param:params) {
          evaled.add(param.evaluate(env, args));
        }
        return f.apply(evaled);
      } else if (funcValue instanceof Class) {
        Class c = (Class) funcValue;
        List<Value> evaled = new ArrayList<Value>();
        for (Expression param:params) {
          evaled.add(param.evaluate(env, args));
        }
        Class[] paramTypes = new Class[params.size()];
        Object[] objs = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
          Value v = evaled.get(i);
          if (v != null) {
            objs[i] = v.get();
            if (objs[i] != null) {
              paramTypes[i] = objs[i].getClass();
            } else {
              paramTypes[i] = null;
            }
          } else {
            throw new RuntimeException("Missing evaluated value for " + params.get(i));
          }
        }
        try {
          Constructor constructor = null;
          try {
            constructor = c.getConstructor(paramTypes);
          } catch (NoSuchMethodException ex) {
            Constructor[] constructors = c.getConstructors();
            for (Constructor cons:constructors) {
              Class[] consParamTypes = cons.getParameterTypes();
              if (consParamTypes.length == paramTypes.length) {
                boolean compatible = true;
                for (int i = 0; i < consParamTypes.length; i++) {
                  if (paramTypes[i] != null && !consParamTypes[i].isAssignableFrom(paramTypes[i])) {
                    compatible = false;
                    break;
                  }
                }
                if (compatible) {
                  constructor = cons;
                  break;
                }
              }
            }
            if (constructor == null) {
              throw new RuntimeException("Cannot instantiate " + c, ex);
            }
          }
          Object obj = constructor.newInstance(objs);
          return new PrimitiveValue<Object>(function, obj);
        } catch (InvocationTargetException ex) {
          throw new RuntimeException("Cannot instantiate " + c, ex);
        } catch (InstantiationException ex) {
          throw new RuntimeException("Cannot instantiate " + c, ex);
        } catch (IllegalAccessException ex) {
          throw new RuntimeException("Cannot instantiate " + c, ex);
        }
      } else {
        throw new UnsupportedOperationException("Unsupported function value " + funcValue);
      }
    }
  }
  
  protected static AnnotationExtractRule createExtractionRule(SequencePattern.Env env, Map<String,Object> attributes)
  {
    AnnotationExtractRule r = new AnnotationExtractRule();
    for (String s:attributes.keySet()) {
      if ("over".equals(s)) {
        //r.annotationField = ...;
      } else if ("rule".equals(s)) {
        //
      }
    }
    return r;
  }

  protected static AnnotationExtractRule createTokenPatternRule(SequencePattern.Env env, SequencePattern.PatternExpr expr, Expression result)
  {
    AnnotationExtractRule r = new AnnotationExtractRule();
    r.annotationField = CoreAnnotations.TokensAnnotation.class;
    r.ruleType = "tokens";
    r.extractRule = createTokenPatternExtractRule(r, env, expr, result);
    return r;
  }

  protected static AnnotationExtractRule createTextPatternRule(SequencePattern.Env env, String expr, Expression result)
  {
    AnnotationExtractRule r = new AnnotationExtractRule();
    r.annotationField = CoreAnnotations.TextAnnotation.class;
    r.ruleType = "text";
    r.extractRule = createTextPatternExtractRule(r, env, expr, result);
    return r;
  }

  protected static ExtractRule<CoreMap, MatchedExpression> createTokenPatternExtractRule(AnnotationExtractRule r,
                                                                                         SequencePattern.Env env, SequencePattern.PatternExpr expr, Expression result)
  {
    TokenSequencePattern pattern = TokenSequencePattern.compile(expr);
    MatchedExpression.SingleAnnotationExtractor<Value> valueExtractor =
            new MatchedExpression.SingleAnnotationExtractor<Value>();
    valueExtractor.name = r.name;
    valueExtractor.extractFunc =
            new CoreMapFunctionApplier< List<? extends CoreMap>, Value >(
                    r.annotationField,
                    new SequencePatternExtractRule<CoreMap, Value>(
                            pattern,
                            new SequenceMatchResultExtractor<CoreMap>(env, result)));
    valueExtractor.annotationField = r.annotationField;
    valueExtractor.resultAnnotationField = r.resultAnnotationField;
    valueExtractor.resultNestedAnnotationField = r.resultNestedAnnotationField;
    valueExtractor.priority = r.priority;
    valueExtractor.weight = r.weight;
    valueExtractor.isComposite = r.isComposite;
    valueExtractor.includeNested = r.includeNested;
    return new CoreMapExtractRule< List<? extends CoreMap>, MatchedExpression >(
            r.annotationField,
            new SequencePatternExtractRule(pattern,
                    new SequenceMatchedExpressionExtractor<Value>( valueExtractor, 0)));
  }

  protected static ExtractRule<CoreMap, MatchedExpression> createTextPatternExtractRule(AnnotationExtractRule r,
                                                                                        SequencePattern.Env env, String expr, Expression result)
  {
    Pattern pattern = env.getStringPattern(expr);
    MatchedExpression.SingleAnnotationExtractor<Value> valueExtractor =
            new MatchedExpression.SingleAnnotationExtractor<Value>();
    valueExtractor.name = r.name;
    valueExtractor.extractFunc =
            new CoreMapFunctionApplier< String, Value >(
                    r.annotationField,
                    new StringPatternExtractRule<Value>(
                            pattern,
                            new StringMatchResultExtractor(env, result)));
    valueExtractor.annotationField = r.annotationField;
    valueExtractor.resultAnnotationField = r.resultAnnotationField;
    valueExtractor.resultNestedAnnotationField = r.resultNestedAnnotationField;
    valueExtractor.priority = r.priority;
    valueExtractor.weight = r.weight;
    valueExtractor.isComposite = r.isComposite;
    valueExtractor.includeNested = r.includeNested;
    return new CoreMapExtractRule< List<? extends CoreMap>, MatchedExpression >(
            r.annotationField,
            new StringPatternExtractRule(pattern,
                    new StringMatchedExpressionExtractor<Value>( valueExtractor, 0)));
  }

  protected static AssignmentRule createAssignmentRule(SequencePattern.Env env, String varname, Expression result)
  {
    return new AssignmentRule(varname, result);
  }

/*  protected static <T> Function<SequenceMatchResult<T>, Value> createSequenceMatchResultExtractor(SequencePattern.Env env, Expression result)
  {
    return new SequenceMatchResultExtractor<T>(env,result);
  }

  protected static Function<MatchResult, Value> createStringMatchResultExtractor(final SequencePattern.Env env, final Expression result)
  {
    return new StringMatchResultExtractor(env,result);
  }                                                            */

  public static class StringMatchResultExtractor implements Function<MatchResult,Value> {
    SequencePattern.Env env;
    Expression expr;

    public StringMatchResultExtractor(SequencePattern.Env env, Expression expr) {
      this.env = env;
      this.expr = expr;
    }

    public Value apply(MatchResult matchResult) {
      return expr.evaluate(env, matchResult);
    }
  }

  public static class SequenceMatchResultExtractor<T> implements Function<SequenceMatchResult<T>,Value> {
    SequencePattern.Env env;
    Expression expr;

    public SequenceMatchResultExtractor(SequencePattern.Env env, Expression expr) {
      this.env = env;
      this.expr = expr;
    }

    public Value apply(SequenceMatchResult<T> matchResult) {
      return expr.evaluate(env, matchResult);
    }
  }

  public static interface ExtractRule<I,O> {
    public boolean extract(I in, List<O> out);
  };

  public static class FilterExtractRule<I,O> implements ExtractRule<I,O>
  {
    Filter<I> filter;
    ExtractRule<I,O> rule;

    public FilterExtractRule(Filter<I> filter, ExtractRule<I,O> rule) {
      this.filter = filter;
      this.rule = rule;
    }

    public FilterExtractRule(Filter<I> filter, ExtractRule<I,O>... rules) {
      this.filter = filter;
      this.rule = new ListExtractRule<I,O>(rules);
    }

    public boolean extract(I in, List<O> out) {
      if (filter.accept(in)) {
        return rule.extract(in,out);
      } else {
        return false;
      }
    }
  }

  public static class ListExtractRule<I,O> implements ExtractRule<I,O>
  {
    List<ExtractRule<I,O>> rules;

    public ListExtractRule(Collection<ExtractRule<I,O>> rules)
    {
      this.rules = new ArrayList<ExtractRule<I,O>>(rules);
    }

    public ListExtractRule(ExtractRule<I,O>... rules)
    {
      this.rules = new ArrayList<ExtractRule<I,O>>(rules.length);
      for (ExtractRule<I,O> rule:rules) {
        this.rules.add(rule);
      }
    }

    public boolean extract(I in, List<O> out) {
      boolean extracted = false;
      for (ExtractRule<I,O> rule:rules) {
        if (rule.extract(in,out)) {
          extracted = true;
        }
      }
      return extracted;
    }

    public void addRules(ExtractRule<I,O>... rules)
    {
      for (ExtractRule<I,O> rule:rules) {
        this.rules.add(rule);
      }
    }

    public void addRules(Collection<ExtractRule<I,O>> rules)
    {
      this.rules.addAll(rules);
    }
  }

  public static class CoreMapExtractRule<T,O> implements ExtractRule<CoreMap, O>
  {
    Class annotationField;
    ExtractRule<T,O> extractRule;

    public CoreMapExtractRule(Class annotationField, ExtractRule<T,O> extractRule) {
      this.annotationField = annotationField;
      this.extractRule = extractRule;
    }

    public boolean extract(CoreMap cm, List<O> out) {
      T field = (T) cm.get(annotationField);
      return extractRule.extract(field, out);
    }

  }

  public static class SequencePatternExtractRule<T,O> implements ExtractRule< List<? extends T>, O>, Function<List<? extends T>, O>
  {
    SequencePattern<T> pattern;
    Function<SequenceMatchResult<T>, O> extractor;

    public SequencePatternExtractRule(SequencePattern.Env env, String regex, Function<SequenceMatchResult<T>, O> extractor) {
      this.extractor = extractor;
      this.pattern = SequencePattern.compile(env, regex);
    }

    public SequencePatternExtractRule(SequencePattern<T> p, Function<SequenceMatchResult<T>, O> extractor) {
      this.extractor = extractor;
      this.pattern = p;
    }

    public boolean extract(List<? extends T> seq, List<O> out) {
      boolean extracted = false;
      SequenceMatcher<T> m = pattern.getMatcher(seq);
      while (m.find()) {
        out.add(extractor.apply(m));
        extracted = true;
      }
      return extracted;
    }

    public O apply(List<? extends T> seq) {
      SequenceMatcher<T> m = pattern.getMatcher(seq);
      if (m.matches()) {
        return extractor.apply(m);
      } else {
        return null;
      }
    }
  }

  public static class StringPatternExtractRule<O> implements ExtractRule<String, O>, Function<String, O>
  {
    Pattern pattern;
    Function<MatchResult, O> extractor;

    public StringPatternExtractRule(Pattern pattern, Function<MatchResult, O> extractor) {
      this.pattern = pattern;
      this.extractor = extractor;
    }

    public StringPatternExtractRule(SequencePattern.Env env, String regex, Function<MatchResult, O> extractor) {
      this(env, regex, extractor, false);
    }

    public StringPatternExtractRule(String regex, Function<MatchResult, O> extractor) {
      this(null, regex, extractor, false);
    }

    public StringPatternExtractRule(SequencePattern.Env env, String regex, Function<MatchResult, O> extractor,
                                    boolean addWordBoundaries) {
      this.extractor = extractor;
      if (addWordBoundaries) { regex = "\\b" + regex + "\\b"; }
      if (env != null) {
        pattern = env.getStringPattern(regex);
      } else {
        pattern = Pattern.compile(regex);
      }
    }

    public boolean extract(String str, List<O> out) {
      boolean extracted = false;
      Matcher m = pattern.matcher(str);
      while (m.find()) {
        out.add(extractor.apply( m ));
        extracted = true;
      }
      return extracted;
    }

    public O apply(String str) {
      Matcher m = pattern.matcher(str);
      if (m.matches()) {
        return extractor.apply(m);
      } else {
        return null;
      }
    }

  }

  static class StringMatchedExpressionExtractor<T> implements Function<MatchResult, MatchedExpression>
  {
    MatchedExpression.SingleAnnotationExtractor<T> extractor;
    int group = 0;

    public StringMatchedExpressionExtractor(MatchedExpression.SingleAnnotationExtractor<T> extractor, int group) {
      this.extractor = extractor;
      this.group = group;
    }

    public MatchedExpression apply(MatchResult matched) {
      MatchedExpression te = new MatchedExpression(Interval.toInterval(matched.start(group), matched.end(group),
              Interval.INTERVAL_OPEN_END), null, extractor, 0);
      return te;
    }
  }

  static class SequenceMatchedExpressionExtractor<T> implements Function<SequenceMatchResult<CoreMap>, MatchedExpression>
  {
    MatchedExpression.SingleAnnotationExtractor<T> extractor;
    int group = 0;

    public SequenceMatchedExpressionExtractor(MatchedExpression.SingleAnnotationExtractor<T> extractor, int group) {
      this.extractor = extractor;
      this.group = group;
    }

    public MatchedExpression apply(SequenceMatchResult<CoreMap> matched) {
      MatchedExpression te = new MatchedExpression(null, Interval.toInterval(matched.start(group), matched.end(group),
              Interval.INTERVAL_OPEN_END), extractor, 0);
      return te;
    }
  }

  public static class CoreMapFunctionApplier<T,O> implements Function<CoreMap, O>
  {
    Class annotationField;
    Function<T,O> func;

    public CoreMapFunctionApplier(Class annotationField, Function<T,O> func) {
      this.annotationField = annotationField;
      this.func = func;
    }

    public O apply(CoreMap cm) {
      T field = (T) cm.get(annotationField);
      return func.apply(field);
    }

  }

}
