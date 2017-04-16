package il.org.spartan.spartanizer.ast.navigate;

import static il.org.spartan.Utils.*;
import static org.eclipse.jdt.core.dom.ASTNode.*;
import static org.eclipse.jdt.core.dom.Assignment.Operator.*;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;
import static org.eclipse.jdt.core.dom.PrefixExpression.Operator.*;

import static java.util.stream.Collectors.*;

import static il.org.spartan.lisp.*;

import static il.org.spartan.spartanizer.ast.navigate.step.*;
import static il.org.spartan.spartanizer.ast.navigate.step.fragments;
import static il.org.spartan.spartanizer.ast.navigate.step.name;
import static il.org.spartan.spartanizer.ast.navigate.step.statements;

import static il.org.spartan.spartanizer.ast.navigate.extract.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Assignment.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.*;

import il.org.spartan.*;
import il.org.spartan.spartanizer.ast.factory.*;
import il.org.spartan.spartanizer.ast.safety.*;
import il.org.spartan.spartanizer.ast.safety.iz.*;
import il.org.spartan.spartanizer.cmdline.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.engine.nominal.*;
import il.org.spartan.spartanizer.java.*;
import il.org.spartan.spartanizer.tippers.*;
import il.org.spartan.utils.*;
import nano.ly.*;

/** Collection of definitions and functions that capture some of the quirks of
 * the {@link ASTNode} hierarchy.
 * @author Yossi Gil
 * @since 2014 */
@SuppressWarnings("OverlyComplexClass")
public interface wizard {
  interface op {
    Assignment.Operator[] assignment = { ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, TIMES_ASSIGN, DIVIDE_ASSIGN, BIT_AND_ASSIGN, BIT_OR_ASSIGN,
        BIT_XOR_ASSIGN, REMAINDER_ASSIGN, LEFT_SHIFT_ASSIGN, RIGHT_SHIFT_SIGNED_ASSIGN, RIGHT_SHIFT_UNSIGNED_ASSIGN };
    PostfixExpression.Operator DECREMENT_POST = PostfixExpression.Operator.DECREMENT;
    PrefixExpression.Operator DECREMENT_PRE = PrefixExpression.Operator.DECREMENT;
    PostfixExpression.Operator INCREMENT_POST = PostfixExpression.Operator.INCREMENT;
    PrefixExpression.Operator INCREMENT_PRE = PrefixExpression.Operator.INCREMENT;
    PrefixExpression.Operator MINUS1 = PrefixExpression.Operator.MINUS;
    InfixExpression.Operator MINUS2 = InfixExpression.Operator.MINUS;
    /** list of class extending Expression class, that need to be surrounded by
     * parenthesis when put out of method arguments list */
    Class<?>[] np = { InfixExpression.class };
    IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    PrefixExpression.Operator PLUS1 = PrefixExpression.Operator.PLUS;
    InfixExpression.Operator PLUS2 = InfixExpression.Operator.PLUS;
    PostfixExpression.Operator[] postfix = { op.INCREMENT_POST, op.DECREMENT_POST };
    PrefixExpression.Operator[] prefix = { INCREMENT, DECREMENT, PLUS1, MINUS1, COMPLEMENT, NOT, };
  }

  /** This list was generated by manually from {@link #infix2assign}
   * {@link Assignment.Operator} . */
  @SuppressWarnings("serial") Map<Assignment.Operator, InfixExpression.Operator> assign2infix = new HashMap<Assignment.Operator, InfixExpression.Operator>() {
    {
      put(PLUS_ASSIGN, InfixExpression.Operator.PLUS);
      put(MINUS_ASSIGN, InfixExpression.Operator.MINUS);
      put(TIMES_ASSIGN, TIMES);
      put(DIVIDE_ASSIGN, DIVIDE);
      put(BIT_AND_ASSIGN, AND);
      put(BIT_OR_ASSIGN, OR);
      put(BIT_XOR_ASSIGN, XOR);
      put(REMAINDER_ASSIGN, REMAINDER);
      put(LEFT_SHIFT_ASSIGN, LEFT_SHIFT);
      put(RIGHT_SHIFT_SIGNED_ASSIGN, RIGHT_SHIFT_SIGNED);
      put(RIGHT_SHIFT_UNSIGNED_ASSIGN, RIGHT_SHIFT_UNSIGNED);
    }
  };

  @SuppressWarnings("serial") Set<String> boxedTypes = new LinkedHashSet<String>() {
    {
      for (final String ¢ : new String[] { "Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short" }) {
        add(¢);
        add("java.lang." + ¢);
      }
    }
  };
  @SuppressWarnings({ "unchecked", "serial" }) Map<Class<? extends ASTNode>, Integer> //
  classToNodeType = new LinkedHashMap<Class<? extends ASTNode>, Integer>() {
    {
      for (int nodeType = 1;; ++nodeType)
        try {
          // monitor.debug("Found node type number of " + nodeClassForType);
          put(ASTNode.nodeClassForType(nodeType), Integer.valueOf(nodeType));
        } catch (@SuppressWarnings("unused") final IllegalArgumentException ¢) {
          // We must suffer this exception; no other way to find the first
          // unused node type
          break;
        } catch (final Exception ¢) {
          note.bug(this, ¢);
          break;
        }
    }
  };
  Map<InfixExpression.Operator, InfixExpression.Operator> conjugate = new HashMap<InfixExpression.Operator, InfixExpression.Operator>() {
    static final long serialVersionUID = 0x2025E6F34F4C06EL;
    {
      put(GREATER, LESS);
      put(LESS, GREATER);
      put(GREATER_EQUALS, LESS_EQUALS);
      put(LESS_EQUALS, GREATER_EQUALS);
    }
  };
  /** This list was generated by manually editing the original list at
   * {@link Assignment.Operator} . */
  Map<InfixExpression.Operator, Assignment.Operator> infix2assign = new HashMap<InfixExpression.Operator, Assignment.Operator>() {
    static final long serialVersionUID = 0x308618DE1596448CL;
    {
      put(InfixExpression.Operator.PLUS, PLUS_ASSIGN);
      put(InfixExpression.Operator.MINUS, MINUS_ASSIGN);
      put(TIMES, TIMES_ASSIGN);
      put(DIVIDE, DIVIDE_ASSIGN);
      put(AND, BIT_AND_ASSIGN);
      put(OR, BIT_OR_ASSIGN);
      put(XOR, BIT_XOR_ASSIGN);
      put(REMAINDER, REMAINDER_ASSIGN);
      put(LEFT_SHIFT, LEFT_SHIFT_ASSIGN);
      put(RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_SIGNED_ASSIGN);
      put(RIGHT_SHIFT_UNSIGNED, RIGHT_SHIFT_UNSIGNED_ASSIGN);
      put(CONDITIONAL_AND, BIT_AND_ASSIGN);
      put(CONDITIONAL_OR, BIT_OR_ASSIGN);
    }
  };
  InfixExpression.Operator[] infixOperators = { TIMES, DIVIDE, REMAINDER, op.PLUS2, op.MINUS2, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED,
      LESS, GREATER, LESS_EQUALS, GREATER_EQUALS, EQUALS, NOT_EQUALS, XOR, AND, OR, CONDITIONAL_AND, CONDITIONAL_OR, };
  String[] keywords = { //
      "synchronized", //
      "instanceof", //
      "implements", //
      "protected", //
      "interface", //
      "transient", //
      "abstract", //
      "volatile", //
      "strictfp", //
      "continue", //
      "boolean", //
      "package", //
      "private", //
      "extends", //
      "finally", //
      "default", //
      "double", //
      "return", //
      "native", //
      "public", //
      "static", //
      "throw", //
      "switch", //
      "import", //
      "throws", //
      "assert", //
      "const", //
      "catch", //
      "class", //
      "false", //
      "while", //
      "float", //
      "final", //
      "super", //
      "break", //
      "short", //
      "byte", //
      "case", //
      "long", //
      "null", //
      "goto", //
      "this", //
      "true", //
      "void", //
      "char", //
      "else", //
      "enum", //
      "new", //
      "int", //
      "try", //
      "for", //
      "do", //
      "if", //
  };
  Bool resolveBinding = Bool.valueOf(false);
  Collection<String> valueTypes = new LinkedHashSet<String>(boxedTypes) {
    static final long serialVersionUID = -0x134495F1CC662D60L;
    {
      for (final String ¢ : new String[] { "String" }) {
        add(¢);
        add("java.lang." + ¢);
      }
    }
  };
  List<Predicate<Modifier>> visibilityModifiers = as.list(ModifierRedundant.isPublic, ModifierRedundant.isPrivate, ModifierRedundant.isProtected);
  static Range range(final ASTNode ¢) {
    final int $ = ¢.getStartPosition();
    return new Range($, $ + ¢.getLength());
  }

  static Expression addParenthesisIfNeeded(final Expression x) {
    final AST a = x.getAST();
    if (!isParethesisNeeded(x))
      return x;
    final ParenthesizedExpression $ = a.newParenthesizedExpression();
    $.setExpression(copy.of(x));
    return $;
  }

  static Expression applyDeMorgan(final InfixExpression $) {
    return subject.operands(hop.operands(flatten.of($)).stream().map(make::notOf).collect(toList())).to(wizard.negate(operator($)));
  }

  static InfixExpression.Operator assign2infix(final Assignment ¢) {
    return assign2infix.get(¢.getOperator());
  }

  static InfixExpression.Operator assign2infix(final Assignment.Operator ¢) {
    return assign2infix.get(¢);
  }

  /** Converts a string into an AST, depending on it's form, as determined
   * by @link{GuessedContext.find}.
   * @param javaSnippet string to convert
   * @return AST, if string is not a valid AST according to any form, then
   *         null */
  static ASTNode ast(final String javaSnippet) {
    switch (GuessedContext.find(javaSnippet)) {
      case COMPILATION_UNIT_LOOK_ALIKE:
        return into.cu(javaSnippet);
      case EXPRESSION_LOOK_ALIKE:
        return into.e(javaSnippet);
      case METHOD_LOOK_ALIKE:
        return into.m(javaSnippet);
      case OUTER_TYPE_LOOKALIKE:
        return into.t(javaSnippet);
      case STATEMENTS_LOOK_ALIKE:
        return into.s(javaSnippet);
      case BLOCK_LOOK_ALIKE:
        return az.astNode(first(statements(az.block(into.s(javaSnippet)))));
      default:
        for (final int guess : as.intArray(ASTParser.K_EXPRESSION, ASTParser.K_STATEMENTS, ASTParser.K_CLASS_BODY_DECLARATIONS,
            ASTParser.K_COMPILATION_UNIT)) {
          final ASTParser p = wizard.parser(guess);
          p.setSource(javaSnippet.toCharArray());
          final ASTNode $ = p.createAST(op.nullProgressMonitor);
          if (valid($))
            return $;
        }
        assert fault.unreachable() : fault.specifically("Snippet cannot be parsed", javaSnippet);
        return null;
    }
  }

  static ASTNode commonAncestor(final ASTNode n1, final ASTNode n2) {
    final List<ASTNode> ns1 = ancestors.path(n1), ns2 = ancestors.path(n2);
    for (int $ = 0; $ < Math.min(ns1.size(), ns2.size()); ++$)
      if (ns1.get($) == ns2.get($))
        return ns1.get($);
    return null;
  }

  /** the function checks if all the given assignments have the same left hand
   * side(variable) and operator
   * @param base The assignment to compare all others to
   * @param as The assignments to compare
   * @return whether all assignments has the same left hand side and operator as
   *         the first one or false otherwise */
  static boolean compatible(final Assignment base, final Assignment... as) {
    return !hasNull(base, as) && Stream.of(as).noneMatch(λ -> incompatible(base, λ));
  }

  static boolean compatible(final Assignment a1, final Assignment a2) {
    return !incompatible(a1, a2);
  }

  static boolean compatible(final Assignment.Operator o1, final InfixExpression.Operator o2) {
    return infix2assign.get(o2) == o1;
  }

  static CompilationUnit compilationUnitWithBinding(final File ¢) {
    return (CompilationUnit) makeAST.COMPILATION_UNIT.makeParserWithBinding(¢).createAST(null);
  }

  static CompilationUnit compilationUnitWithBinding(final String ¢) {
    return (CompilationUnit) makeAST.COMPILATION_UNIT.makeParserWithBinding(¢).createAST(null);
  }

  static <T> String completionIndex(final List<T> ts, final T t) {
    final String $ = ts.size() + "";
    String i = ts.indexOf(t) + 1 + "";
    while (i.length() < $.length())
      i = " " + i;
    return i + "/" + $;
  }

  /** Makes an opposite operator from a given one, which keeps its logical
   * operation after the node swapping. ¢.¢. "&" is commutative, therefore no
   * change needed. "<" isn't commutative, but it has its opposite: ">=".
   * @param ¢ The operator to flip
   * @return correspond operator - ¢.¢. "<=" will become ">", "+" will stay
   *         "+". */
  static InfixExpression.Operator conjugate(final InfixExpression.Operator ¢) {
    return wizard.conjugate.getOrDefault(¢, ¢);
  }

  /** @param ns unknown number of nodes to check
   * @return whetherone of the nodes is an Expression Statement of type Post or
   *         Pre Expression with ++ or -- operator. false if none of them are or
   *         if the given parameter is null. */
  static boolean containIncOrDecExp(final ASTNode... ns) {
    return ns != null && Stream.of(ns).anyMatch(λ -> λ != null && iz.updating(λ));
  }

  static InfixExpression.Operator convertToInfix(final Operator ¢) {
    return ¢ == Operator.BIT_AND_ASSIGN ? InfixExpression.Operator.AND
        : ¢ == Operator.BIT_OR_ASSIGN ? InfixExpression.Operator.OR
            : ¢ == Operator.BIT_XOR_ASSIGN ? InfixExpression.Operator.XOR
                : ¢ == Operator.DIVIDE_ASSIGN ? InfixExpression.Operator.DIVIDE
                    : ¢ == Operator.LEFT_SHIFT_ASSIGN ? InfixExpression.Operator.LEFT_SHIFT
                        : ¢ == Operator.MINUS_ASSIGN ? InfixExpression.Operator.MINUS
                            : ¢ == Operator.PLUS_ASSIGN ? InfixExpression.Operator.PLUS
                                : ¢ == Operator.REMAINDER_ASSIGN ? InfixExpression.Operator.REMAINDER
                                    : ¢ == Operator.RIGHT_SHIFT_SIGNED_ASSIGN ? InfixExpression.Operator.RIGHT_SHIFT_SIGNED
                                        : ¢ == Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN ? InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED : null;
  }

  static List<Statement> decompose(final Expression x) {
    return new ASTMapReducer<List<Statement>>() {
      @Override public List<Statement> reduce() {
        return new ArrayList<>();
      }

      @Override public List<Statement> reduce(final List<Statement> $, final List<Statement> ss) {
        $.addAll(ss);
        return $;
      }

      @Override protected List<Statement> map(final Assignment ¢) {
        return listMe(¢);
      }

      @Override protected List<Statement> map(final ClassInstanceCreation ¢) {
        return listMe(¢);
      }

      @Override protected List<Statement> map(final MethodInvocation ¢) {
        return listMe(¢);
      }

      @Override protected List<Statement> map(final PostfixExpression ¢) {
        return listMe(¢);
      }

      @Override protected List<Statement> map(final PrefixExpression ¢) {
        return iz.in(¢.getOperator(), INCREMENT, DECREMENT) ? listMe(¢) : reduce();
      }

      @Override protected List<Statement> map(final SuperMethodInvocation ¢) {
        return listMe(¢);
      }
    }.map(x);
  }

  /** Compute the "de Morgan" conjugate of the operator present on an
   * {@link InfixExpression}.
   * @param x an expression whose operator is either
   *        {@link Operator#CONDITIONAL_AND} or {@link Operator#CONDITIONAL_OR}
   * @return {@link Operator#CONDITIONAL_AND} if the operator present on the
   *         parameter is {@link Operator#CONDITIONAL_OR}, or
   *         {@link Operator#CONDITIONAL_OR} if this operator is
   *         {@link Operator#CONDITIONAL_AND}
   * @see copy#deMorgan(Operator) */
  static InfixExpression.Operator deMorgan(final InfixExpression ¢) {
    return wizard.deMorgan(¢.getOperator());
  }

  /** Compute the "de Morgan" conjugate of an operator.
   * @param o must be either {@link Operator#CONDITIONAL_AND} or
   *        {@link Operator#CONDITIONAL_OR}
   * @return {@link Operator#CONDITIONAL_AND} if the parameter is
   *         {@link Operator#CONDITIONAL_OR} , or
   *         {@link Operator#CONDITIONAL_OR} if the parameter is
   *         {@link Operator#CONDITIONAL_AND}
   * @see wizard#deMorgan(InfixExpression) */
  static InfixExpression.Operator deMorgan(final InfixExpression.Operator ¢) {
    assert iz.deMorgan(¢);
    return ¢.equals(CONDITIONAL_AND) ? CONDITIONAL_OR : CONDITIONAL_AND;
  }

  static boolean disjoint(final ASTNode n, final IMarker m) {
    return disjoint(n, range(m));
  }

  static boolean disjoint(final ASTNode n, final Range r) {
    return r != null && (from(n) >= r.to || to(n) <= r.from);
  }

  /** Determines if we can be certain that a {@link Statement} ends with a
   * sequencer ({@link ReturnStatement}, {@link ThrowStatement},
   * {@link BreakStatement}, {@link ContinueStatement}).
   * @param ¢ JD
   * @return true <b>iff</b> the Statement can be verified to end with a
   *         sequencer. */
  static boolean endsWithSequencer(final Statement ¢) {
    if (¢ == null)
      return false;
    final Statement $ = hop.lastStatement(¢);
    if ($ == null)
      return false;
    switch ($.getNodeType()) {
      case BLOCK:
        return endsWithSequencer(lisp.last(statements((Block) $)));
      case BREAK_STATEMENT:
      case CONTINUE_STATEMENT:
      case RETURN_STATEMENT:
      case THROW_STATEMENT:
        return true;
      case DO_STATEMENT:
        return endsWithSequencer(((DoStatement) $).getBody());
      case LABELED_STATEMENT:
        return endsWithSequencer(((LabeledStatement) $).getBody());
      case IF_STATEMENT:
        return endsWithSequencer(then((IfStatement) $)) && endsWithSequencer(elze((IfStatement) $));
      default:
        return false;
    }
  }

  /** Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   * <p>
   * Each of the parameters may be {@code null; a {@code null is only equal
   * to{@code null
   * @param n1 JD
   * @param n2 JD
   * @return {@code true} if the parameters are the same. */
  static boolean eq(final ASTNode n1, final ASTNode n2) {
    return n1 == n2 || n1 != null && n2 != null && n1.getNodeType() == n2.getNodeType() && Trivia.cleanForm(n1).equals(Trivia.cleanForm(n2));
  }

  /** Find the first matching expression to the given boolean (b).
   * @param b JD,
   * @param xs JD
   * @return first expression from the given list (es) whose boolean value
   *         matches to the given boolean (b). */
  static Expression find(final boolean b, final List<Expression> xs) {
    return xs.stream().filter(λ -> iz.booleanLiteral(λ) && b == az.booleanLiteral(λ).booleanValue()).findFirst().orElse(null);
  }

  static VariableDeclarationFragment findFragment(final FieldDeclaration ¢) {
    return fragments(¢).stream().filter(λ -> (λ.getName() + "").equals(FieldInitializedSerialVersionUIDToHexadecimal.SERIAL_VERSION_UID)).findFirst()
        .orElse(null);
  }

  /** Gets two lists of expressions and returns the idx of the only expression
   * which is different between them. If the lists differ with other then one
   * element, -1 is returned.
   * @param es1
   * @param es2
   * @return */
  @SuppressWarnings("boxing") static int findSingleDifference(final List<? extends ASTNode> es1, final List<? extends ASTNode> es2) {
    int $ = -1;
    for (final Integer ¢ : range.from(0).to(es1.size()))
      if (!wizard.eq(es1.get(¢), es2.get(¢))) {
        if ($ >= 0)
          return -1;
        $ = ¢;
      }
    return $;
  }

  static boolean forbiddenOpOnPrimitive(final VariableDeclarationFragment f, final Statement nextStatement) {
    if (!iz.literal(f.getInitializer()) || !iz.expressionStatement(nextStatement))
      return false;
    final ExpressionStatement x = (ExpressionStatement) nextStatement;
    if (iz.methodInvocation(x.getExpression())) {
      final Expression $ = core(expression(x.getExpression()));
      return iz.simpleName($) && ((SimpleName) $).getIdentifier().equals(f.getName().getIdentifier());
    }
    if (!iz.fieldAccess(x.getExpression()))
      return false;
    final Expression e = core(((FieldAccess) x.getExpression()).getExpression());
    return iz.simpleName(e) && ((SimpleName) e).getIdentifier().equals(f.getName().getIdentifier());
  }

  static int from(final ASTNode ¢) {
    return ¢.getStartPosition();
  }

  static int from(final IMarker $) {
    try {
      return ((Integer) $.getAttribute(IMarker.CHAR_END)).intValue();
    } catch (CoreException | ClassCastException ¢) {
      note.bug(¢);
      return Integer.MIN_VALUE;
    }
  }

  @SuppressWarnings("unchecked") static List<MethodDeclaration> getMethodsSorted(final ASTNode n) {
    final Collection<MethodDeclaration> $ = new ArrayList<>();
    // noinspection SameReturnValue
    n.accept(new ASTVisitor(true) {
      @Override public boolean visit(final MethodDeclaration ¢) {
        $.add(¢);
        return false;
      }
    });
    return (List<MethodDeclaration>) $.stream().sorted((x, y) -> metrics.countStatements(x) > metrics.countStatements(y)
        || metrics.countStatements(x) == metrics.countStatements(y) && x.parameters().size() > y.parameters().size() ? -1 : 1);
  }

  static Message[] getProblems(final ASTNode $) {
    return !($ instanceof CompilationUnit) ? null : ((CompilationUnit) $).getMessages();
  }

  static Expression goInfix(final InfixExpression from, final VariableDeclarationStatement s) {
    final List<Expression> $ = hop.operands(from);
    $.stream().filter(λ -> iz.parenthesizedExpression(λ) && iz.assignment(extract.core(λ))).forEachOrdered(x -> {
      final Assignment a = az.assignment(extract.core(x));
      final SimpleName var = az.simpleName(left(a));
      fragments(s).stream().filter(λ -> (name(λ) + "").equals(var + "")).forEach(λ -> {
        λ.setInitializer(copy.of(right(a)));
        $.set($.indexOf(x), x.getAST().newSimpleName(var + ""));
      });
    });
    return subject.append(subject.pair(first($), $.get(1)).to(from.getOperator()), chop(chop($)));
  }

  static boolean incompatible(final Assignment a1, final Assignment a2) {
    return hasNull(a1, a2) || !lisp2.areEqual(a1.getOperator(), a2.getOperator()) || !wizard.eq(step.to(a1), step.to(a2));
  }

  static Operator infix2assign(final InfixExpression.Operator ¢) {
    assert ¢ != null;
    final Operator $ = infix2assign.get(¢);
    assert $ != null : "No assignment equivalent to " + ¢;
    return $;
  }

  /** @param n JD
   * @param s JD
   * @return true iff node is inside selection */
  static boolean inRange(final ASTNode n, final ITextSelection s) {
    if (n == null || s == null)
      return false;
    final int $ = from(n);
    return $ >= s.getOffset() && $ < s.getLength() + s.getOffset();
  }

  static String intToClassName(final int $) {
    try {
      return ASTNode.nodeClassForType($).getSimpleName();
    } catch (@SuppressWarnings("unused") final IllegalArgumentException __) {
      return "???";
    }
  }

  /** @param o JD
   * @return whetherone of {@link #InfixExpression.Operator.XOR},
   *         {@link #InfixExpression.Operator.OR},
   *         {@link #InfixExpression.Operator.AND}, and false otherwise */
  static boolean isBitwiseOperator(final InfixExpression.Operator ¢) {
    return in(¢, XOR, OR, AND);
  }

  static boolean isBoxedType(final String typeName) {
    return boxedTypes.contains(typeName);
  }

  /** Determine whether an InfixExpression.Operator is a comparison operator or
   * not
   * @param o JD
   * @return whetherone of {@link #InfixExpression.Operator.LESS},
   *         {@link #InfixExpression.Operator.GREATER},
   *         {@link #InfixExpression.Operator.LESS_EQUALS},
   *         {@link #InfixExpression.Operator.GREATER_EQUALS},
   *         {@link #InfixExpression.Operator.EQUALS},
   *         {@link #InfixExpression.Operator.NOT_EQUALS},
   *         {@link #InfixExpression.Operator.CONDITIONAL_OR},
   *         {@link #InfixExpression.Operator.CONDITIONAL_AND} and false
   *         otherwise */
  static boolean isComparison(final InfixExpression.Operator ¢) {
    return in(¢, LESS, GREATER, LESS_EQUALS, GREATER_EQUALS, EQUALS, //
        NOT_EQUALS, CONDITIONAL_OR, CONDITIONAL_AND);
  }

  static boolean notDefaultLiteral(final Expression ¢) {
    return !iz.nullLiteral(¢) && !iz.literal0(¢) && !literal.false¢(¢) && !iz.literal(¢, 0.0) && !iz.literal(¢, 0L);
  }

  static boolean isObject(final Type ¢) {
    if (¢ == null)
      return false;
    switch (¢ + "") {
      case "Object":
      case "java.lang.Object":
        return true;
      default:
        return false;
    }
  }

  /** Checks if an expression need parenthesis in order to interpreted correctly
   * @param x an Expression
   * @return whether or not this expression need parenthesis when put together
   *         with other expressions in infix expression. There could be non
   *         explicit parenthesis if the expression is located in an arguments
   *         list, so making it a part of infix expression require additional
   *         parenthesis */
  static boolean isParethesisNeeded(final Expression x) {
    return Stream.of(op.np).anyMatch(λ -> λ.isInstance(x));
  }

  /** Determine whether an InfixExpression.Operator is a shift operator or not
   * @param o JD
   * @return whetherone of {@link #InfixExpression.Operator.LEFT_SHIFT},
   *         {@link #InfixExpression.Operator.RIGHT_SHIFT_SIGNED},
   *         {@link #InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED} and false
   *         otherwise */
  static boolean isShift(final InfixExpression.Operator ¢) {
    return in(¢, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED);
  }

  static boolean isString(final String typeName) {
    if (typeName == null)
      return false;
    switch (typeName) {
      case "String":
      case "java.lang.String":
        return true;
      default:
        return false;
    }
  }

  static boolean isString(final Type ¢) {
    return isString(¢ + "");
  }

  static boolean isValueType(final String typeName) {
    return valueTypes.contains(typeName);
  }

  static boolean isValueType(final Type ¢) {
    return isValueType(!haz.binding(¢) ? ¢ + "" : ¢.resolveBinding().getBinaryName());
  }

  static List<Statement> listMe(final Expression ¢) {
    return as.list(¢.getAST().newExpressionStatement(copy.of(¢)));
  }

  static List<VariableDeclarationFragment> live(final VariableDeclarationFragment f, final Collection<VariableDeclarationFragment> fs) {
    final List<VariableDeclarationFragment> $ = new ArrayList<>();
    fs.stream().filter(λ -> λ != f && λ.getInitializer() != null).forEach(λ -> $.add(copy.of(λ)));
    return $;
  }

  static MethodDeclaration methodWithBinding(final String m) {
    return findFirst.instanceOf(MethodDeclaration.class).in(makeAST.CLASS_BODY_DECLARATIONS.makeParserWithBinding(m).createAST(null));
  }

  /** @param o JD
   * @return operator that produces the logical negation of the parameter */
  static InfixExpression.Operator negate(final InfixExpression.Operator ¢) {
    return ¢.equals(CONDITIONAL_AND) ? CONDITIONAL_OR //
        : ¢.equals(CONDITIONAL_OR) ? CONDITIONAL_AND //
            : ¢.equals(EQUALS) ? NOT_EQUALS
                : ¢.equals(NOT_EQUALS) ? EQUALS
                    : ¢.equals(LESS_EQUALS) ? GREATER
                        : ¢.equals(GREATER) ? LESS_EQUALS //
                            : ¢.equals(GREATER_EQUALS) ? LESS //
                                : ¢.equals(LESS) ? GREATER_EQUALS : null;
  }

  static String nodeName(final ASTNode ¢) {
    return ¢ == null ? "???" : nodeName(¢.getClass());
  }

  static String nodeName(final Class<? extends ASTNode> ¢) {
    return English.name(¢);
  }

  static <N extends ASTNode> int nodeType(final Class<N> ¢) {
    final Integer $ = classToNodeType.get(¢);
    return $ != null ? $.intValue()
        : Zero.voidAll(note.bug(fault.dump() + //
            "\n c = " + ¢ + //
            "\n c.getSimpleName() = " + ¢.getSimpleName() + //
            "\n classForNodeType.keySet() = " + wizard.classToNodeType.keySet() + //
            "\n classForNodeType = " + wizard.classToNodeType + //
            fault.done()));
  }

  static int nodeTypesCount() {
    return classToNodeType.size() + 2;
  }

  /** Determine whether a node is an infix expression whose operator is
   * non-associative.
   * @param pattern JD
   * @return whether the parameter is a node which is an infix expression whose
   *         operator is */
  static boolean nonAssociative(final ASTNode ¢) {
    return nonAssociative(az.infixExpression(¢));
  }

  static boolean nonAssociative(final InfixExpression ¢) {
    return ¢ != null && (in(¢.getOperator(), op.MINUS2, DIVIDE, REMAINDER, LEFT_SHIFT, RIGHT_SHIFT_SIGNED, RIGHT_SHIFT_UNSIGNED)
        || iz.infixPlus(¢) && !type.isNotString(¢));
  }

  /** Parenthesize an expression (if necessary).
   * @param x JD
   * @return a {@link copy#duplicate(Expression)} of the parameter wrapped in
   *         parenthesis. */
  static Expression parenthesize(final Expression ¢) {
    return iz.noParenthesisRequired(¢) ? copy.of(¢) : make.parethesized(¢);
  }

  static ASTParser parser(final int kind) {
    final ASTParser $ = ASTParser.newParser(AST.JLS8);
    setBinding($);
    $.setKind(kind);
    final Map<String, String> options = JavaCore.getOptions();
    options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
    $.setCompilerOptions(options);
    return $;
  }

  static int positivePrefixLength(final IfStatement $) {
    return metrics.length($.getExpression(), then($));
  }

  static <T> T previous(final T t, final List<T> ts) {
    if (ts == null)
      return null;
    final int $ = ts.indexOf(t);
    return $ < 1 ? null : ts.get($ - 1);
  }

  static String problems(final ASTNode ¢) {
    return !(¢ instanceof CompilationUnit) ? "???" : problems((CompilationUnit) ¢);
  }

  static String problems(final CompilationUnit u) {
    final IProblem[] v = u.getProblems();
    if (v.length == 0)
      return "???";
    final Int $ = new Int();
    return Stream.of(v).map(λ -> "\n\t\t\t" + ++$.inner + ": " + λ.getMessage()).reduce((x, y) -> x + y).get();
  }

  static Range range(final IMarker ¢) {
    return ¢ == null ? null : new Range(from(¢), to(¢));
  }

  static Range range(final ITextSelection ¢) {
    return new Range(¢.getOffset(), ¢.getOffset() + ¢.getLength());
  }

  /** replaces an ASTNode with another
   * @param n
   * @param with */
  static <N extends ASTNode> void replace(final N n, final N with, final ASTRewrite r) {
    r.replace(n, with, null);
  }

  /** String wise comparison of all the given SimpleNames
   * @param ¢ string to compare all names to
   * @param xs SimplesNames to compare by their string value to cmpTo
   * @return whether all names are the same (string wise) or false otherwise */
  static boolean same(final Expression x, final Expression... xs) {
    return Stream.of(xs).allMatch(λ -> eq(λ, x));
  }

  /** Determine whether two lists of nodes are the same, in the sense that their
   * textual representations is identical.
   * @param ns1 first list to compare
   * @param ns2 second list to compare
   * @return are the lists equal string-wise */
  @SuppressWarnings("boxing") static <N extends ASTNode> boolean same(final List<N> ns1, final List<N> ns2) {
    return ns1 == ns2 || ns1.size() == ns2.size() && range.from(0).to(ns1.size()).stream().allMatch(λ -> eq(ns1.get(λ), ns2.get(λ)));
  }

  /** Works like same, but it applies {@ link tide.clean} to remove spaces
   * Determine whether two nodes are the same, in the sense that their textual
   * representations is identical.
   * <p>
   * Each of the parameters may be {@code null; a {@code null is only equal
   * to{@code null
   * @param n1 JD
   * @param n2 JD
   * @return {@code true} if the parameters are the same.
   * @author matteo
   * @since 15/3/2017 */
  static boolean same2(final ASTNode n1, final ASTNode n2) {
    return n1 == n2 || n1 != null && n2 != null && n1.getNodeType() == n2.getNodeType()
        && tide.clean(Trivia.cleanForm(n1) + "").equals(tide.clean(Trivia.cleanForm(n2) + ""));
  }

  static int sequencerRank(final ASTNode ¢) {
    return the.index(¢.getNodeType(), BREAK_STATEMENT, CONTINUE_STATEMENT, RETURN_STATEMENT, THROW_STATEMENT);
  }

  static void setBinding(final ASTParser $) {
    $.setResolveBindings(resolveBinding.inner);
    if (resolveBinding.inner)
      $.setEnvironment(null, null, null, true);
  }
  static void setParserResolveBindings() {
    resolveBinding.inner = true;
  }
  static boolean shoudlInvert(final IfStatement s) {
    final int $ = wizard.sequencerRank(hop.lastStatement(then(s))), rankElse = wizard.sequencerRank(hop.lastStatement(elze(s)));
    return rankElse > $ || $ == rankElse && !thenIsShorter(s);
  }

  static String signAdjust(final String token) {
    return token.startsWith("-") ? token.substring(1) //
        : "-" + token.substring(as.bit(token.startsWith("+")));
  }

  static boolean thenIsShorter(final IfStatement s) {
    final Statement then = then(s), elze = elze(s);
    if (elze == null)
      return true;
    final int s1 = count.lines(then), s2 = count.lines(elze);
    if (s1 < s2)
      return true;
    if (s1 > s2)
      return false;
    assert s1 == s2;
    final int n2 = extract.statements(elze).size(), n1 = extract.statements(then).size();
    if (n1 < n2)
      return true;
    if (n1 > n2)
      return false;
    assert n1 == n2;
    final IfStatement $ = make.invert(s);
    return wizard.positivePrefixLength($) >= wizard.positivePrefixLength(make.invert($));
  }

  static int to(final ASTNode ¢) {
    return ¢.getLength() + from(¢);
  }

  static int to(final IMarker $) {
    try {
      return ((Integer) $.getAttribute(IMarker.CHAR_END)).intValue();
    } catch (CoreException | ClassCastException ¢) {
      note.bug(¢);
      return Integer.MAX_VALUE;
    }
  }

  static boolean valid(final ASTNode ¢) {
    final CompilationUnit $ = az.compilationUnit(¢.getRoot());
    return $ == null || $.getProblems().length == 0;
  }
}
