package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makeParenthesizedExpression;
import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.makePrefixExpression;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import il.ac.technion.cs.ssdl.spartan.utils.Is;
import il.ac.technion.cs.ssdl.spartan.utils.Range;

/**
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 *
 *
 */
public class ComparisonWithBoolean extends Spartanization {
  /** Instantiates this class */
  public ComparisonWithBoolean() {
    super("Comparison With Boolean", "Eliminate reduntant comparison to boolean constant");
  }

  @Override protected final void fillRewrite(final ASTRewrite r, final AST t, final CompilationUnit cu, final IMarker m) {
    cu.accept(new ASTVisitor() {
      @Override public boolean visit(final InfixExpression n) {
        if (!inRange(m, n))
          return true;
        if (n.getOperator() != Operator.EQUALS && n.getOperator() != Operator.NOT_EQUALS)
          return true;
        ASTNode nonliteral = null;
        BooleanLiteral literal = null;
        if (Is.booleanLiteral(n.getRightOperand()) && !Is.booleanLiteral(n.getLeftOperand())) {
          nonliteral = r.createMoveTarget(n.getLeftOperand());
          literal = (BooleanLiteral) n.getRightOperand();
        } else if (!Is.booleanLiteral(n.getLeftOperand()) && !Is.booleanLiteral(n.getRightOperand()))
          return true;
        else {
          nonliteral = r.createMoveTarget(n.getRightOperand());
          literal = (BooleanLiteral) n.getLeftOperand();
        }
        r.replace(n,
            literal.booleanValue() && n.getOperator() == Operator.EQUALS
                || !literal.booleanValue() && n.getOperator() == Operator.NOT_EQUALS ? nonliteral
                    : makePrefixExpression(t, r, makeParenthesizedExpression(t, r, (Expression) nonliteral),
                        PrefixExpression.Operator.NOT),
            null);
        return true;
      }
    });
  }

  @Override protected ASTVisitor fillOpportunities(final List<Range> opportunities) {
    return new ASTVisitor() {
      @Override public boolean visit(final InfixExpression n) {
        if (n.getOperator() != Operator.EQUALS && n.getOperator() != Operator.NOT_EQUALS)
          return true;
        if (ASTNode.BOOLEAN_LITERAL == n.getRightOperand().getNodeType()
            || ASTNode.BOOLEAN_LITERAL == n.getLeftOperand().getNodeType())
          opportunities.add(new Range(n));
        return true;
      }
    };
  }
}
