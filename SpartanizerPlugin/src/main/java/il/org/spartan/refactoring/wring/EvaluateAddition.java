package il.org.spartan.refactoring.wring;


import static org.eclipse.jdt.core.dom.InfixExpression.Operator.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.refactoring.utils.*;

/**
 * Evaluate the addition of numbers according to the following rules <br/> <br/>
 * <code>
 * int + int --> int <br/>
 * double + double --> double <br/>
 * long + long --> long <br/>
 * int + double --> double <br/>
 * int + long --> long <br/>
 * long + double --> double <br/>
 * </code>
 * @author Dor Ma'ayan 
 * @since 2016
 */
public class EvaluateAddition extends Wring.ReplaceCurrentNode<InfixExpression> implements Kind.NoImpact {

  private enum Type {INT,LONG,DOUBLE,BAD}
  
  @Override public String description() {
      return "Evaluate addition of int numbers";
  }

  @Override String description(@SuppressWarnings("unused") InfixExpression __) {
    return "Evaluate addition of int numbers";
  }
  
  private static Type getEvaluatedType(InfixExpression e){
    //boolean isLong = false;
    List<Expression> operands = extract.allOperands(e);
    for (final Expression ¢ : operands){
      if(!(¢ instanceof NumberLiteral))
        return Type.BAD;
      if(isDouble(¢))
        return Type.DOUBLE;
    }
    return Type.INT;
  }
    
  private static boolean isInt(Expression e){
    return e instanceof NumberLiteral && ((NumberLiteral) e).getToken().matches("[0-9]+");
  }
  
  private static boolean isDouble(Expression e){
    return e instanceof NumberLiteral && ((NumberLiteral) e).getToken().matches("[0-9]+\\.[0-9]?");
  }
  
 /* private static boolean isLong(Expression e){
    if(!(e instanceof NumberLiteral))
      return false;
    return ((NumberLiteral) e).getToken().matches("[0-9]+");
  }*/ //TODO: Add supprot for long
  
  @Override ASTNode replacement(InfixExpression e) {
    if( e.getOperator() != PLUS )
      return null;
    switch(getEvaluatedType(e)){
      case INT :
        return replacementInt(extract.allOperands(e),e);
      case DOUBLE :
        return replacementDouble(extract.allOperands(e),e);
      default:
        return null;
    }
  }
  
 private static ASTNode replacementInt(final List<Expression> es, InfixExpression e) {
    
    int sum = 0;
    for (final Expression ¢ : es){
      if (!(¢ instanceof NumberLiteral) || !isInt(¢))
        return null;
      sum=sum + Integer.parseInt(((NumberLiteral) ¢).getToken());
    }  
    return e.getAST().newNumberLiteral(Integer.toString(sum));
  }
 
 private static ASTNode replacementDouble(final List<Expression> es, InfixExpression e) {
   double sum = 0;
   for (final Expression ¢ : es){
     if (!(¢ instanceof NumberLiteral) || !isInt(¢) && !isDouble(¢))
       return null;
     sum=sum + Double.parseDouble(((NumberLiteral) ¢).getToken());
   }  
   return e.getAST().newNumberLiteral(Double.toString(sum));
 }
  
}
