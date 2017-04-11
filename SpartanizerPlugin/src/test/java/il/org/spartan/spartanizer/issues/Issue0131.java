package il.org.spartan.spartanizer.issues;

import static il.org.spartan.spartanizer.testing.TestsUtilsTrimmer.*;

import org.junit.*;
import org.junit.runners.*;

import il.org.spartan.spartanizer.tippers.*;

/** {@link ModifierRedundant}
 * @author Yossi Gil
 * @since 2016 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings({ "static-method", "javadoc" })
public final class Issue0131 {
  @Test public void a01() {
    trimmingOf("for(int i=4; i<s.g() ; ++i){i+=9;return x;}return x;")//
        .gives("for(int i=4; i<s.g() ; ++i){i+=9;break;}return x;");
  }

  @Test public void a02() {
    trimmingOf("while(i>9)if(i==5)return x;return x;")//
        .gives("while(i>9)if(i==5)break;return x;");
  }

  @Test public void a03() {
    trimmingOf("for(int ¢=4 ; ¢<s.g() ; ++¢)return x;return x;")//
        .gives("for(int ¢=4 ; ¢<s.g() ; ++¢)break;return x;")//
        .stays();
  }

  @Test public void a04() {
    trimmingOf("for(int ¢=4 ; ¢<s.g() ; ++¢)if(t=4)return x;return x;").gives("for(int ¢=4 ; ¢<s.g() ; ++¢)if(t=4)break;return x;")//
    ;
  }

  @Test public void a05() {
    trimmingOf("while(i>5){i+=9;i++;return x;}return x;")//
        .gives("while(i>5){i+=9;i++;break;}return x;");
  }

  @Test public void a06() {
    trimmingOf("while(i>5){i+=9;return x;}return x;")//
        .gives("while(i>5){i+=9;break;}return x;")//
        .stays();
  }

  @Test public void a07() {
    trimmingOf("while(i>5)return x;return x;")//
        .gives("while(i>5)break;return x;")//
        .stays();
  }

  @Test public void a08() {
    trimmingOf("while(i>5)if(t=4)return x;return x;")//
        .gives("while(i>5)if(t=4)break;return x;");
  }

  @Test public void a09() {
    trimmingOf("for(int i=4 ; i<s.g() ; ++i)if(i==5)return x;return x;")//
        .gives("for(int i=4 ; i<s.g() ; ++i)if(i==5)break;return x;");
  }

  @Test public void a10() {
    trimmingOf("for(int i=4;i<s.g();++i){i+=9;i++;return x;}return x;")//
        .gives("for(int i=4;i<s.g();++i){i+=9;i++;break;}return x;").gives("for(int ¢=4;¢<s.g();++¢){¢+=9;¢++;break;}return x;");
  }

  @Test public void a11() {
    trimmingOf("void foo() {int t=5;int z=2;for(int i=4;i<s.g();++i){if(z==i){t+=9;return x;}y+=15;return x;}return x;}")
        .gives("void foo() {int t=5, z=2;for(int i=4;i<s.g();++i){if(z==i){t+=9;return x;}y+=15;break;}return x;}")
        .gives("void foo() {int t=5, z=2;for(int i=4;i<s.g();++i){if(z==i){t+=9;break;}y+=15;break;}return x;}")
        .gives("void foo() {int t=5, z=2;for(int ¢=4;¢<s.g();++¢){if(z==¢){t+=9;break;}y+=15;break;}return x;}")
        .gives("void foo() {for(int t=5, z=2, ¢=4;¢<s.g();++¢){if(z==¢){t+=9;break;}y+=15;break;}return x;}")//
        .gives("void foo(){for(int t=5,z=2,¢=4;¢<s.g();++¢){if(z==¢){t+=9;}else{y+=15;}break;}return x;}") //
        .gives("void foo(){for(int t=5,z=2,¢=4;¢<s.g();++¢){if(z==¢)t+=9;else y+=15;break;}return x;}") //
        .stays();
  }

  @Test public void a12() {
    trimmingOf("boolean b=false;for(int i=4;i<s.g();++i){if(i==5){t+=9;return x;}else return tr;y+=15;return x;}return x;")
        .gives("for(int i=4;i<s.g();++i){if(i==5){t+=9;return x;}else return tr;y+=15;break;}return x;")
        .gives("for(int i=4;i<s.g();++i){if(i==5){t+=9;break;}else return tr;y+=15;break;}return x;");
  }

  @Test public void a14() {
    trimmingOf("public static void main(){while(i<7){if(i==5){t+=9;return x;}else return tr;y+=15;return x;}return x;}")
        .gives("public static void main(){while(i<7){if(i==5){t+=9;return x;}else return tr;y+=15;break;}return x;}");
  }
}
