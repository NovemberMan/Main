package il.org.spartan.Leonidas.plugin.leonidas;

import com.intellij.psi.PsiElement;
import il.org.spartan.Leonidas.auxilary_layer.az;
import il.org.spartan.Leonidas.auxilary_layer.iz;
import il.org.spartan.Leonidas.auxilary_layer.step;

import java.util.*;
import java.util.stream.Collectors;


/**
 * A class responsible for the logic of matching the tree of the user to the definition of the tipper and extracting the
 * correct information of the tree of the user for the sake of future replacing.
 * @author michalcohen
 * @since 31-03-2017.
 */
public class Matcher {

    private Encapsulator root;
    private Map<Integer, List<Constraint>> constrains = new HashMap<>();

    public Matcher() {
        root = null;
    }

    public Matcher(Encapsulator r, Map<Integer, List<Constraint>> map) {
        root = r;
        buildMatcherTree(this, map);
    }

    /**
     * @param treeTemplate - the template tree generated by the TreeBuilder
     * @param treeToMatch  - the tree generated by the users' code
     * @return - true iff these two trees match by the Leonidas language.
     */
    private boolean treeMatch(Encapsulator treeTemplate, Encapsulator treeToMatch) {
        if (!iz.conforms(treeToMatch.getInner(), treeTemplate.getInner())
                || iz.block(treeToMatch.getInner()) && !iz.genericBlock(treeTemplate.getInner())
                && treeTemplate.getInner().getUserData(KeyDescriptionParameters.NO_OF_STATEMENTS) != null
                && treeTemplate.getInner().getUserData(KeyDescriptionParameters.NO_OF_STATEMENTS)
                .notConforms(az.block(treeToMatch.getInner()).getStatements().length)
                && treeTemplate.getInner().getUserData(KeyDescriptionParameters.NO_OF_STATEMENTS) != null
                && treeTemplate.getInner().getUserData(KeyDescriptionParameters.NO_OF_STATEMENTS)
                .notConforms(az.block(treeToMatch.getInner()).getStatements().length))
            return false;
        boolean res = true;
        if (treeTemplate.getAmountOfNoneWhiteSpaceChildren() < treeToMatch.getAmountOfNoneWhiteSpaceChildren()
                && !iz.generic(treeTemplate.getInner()))
            return false;
        for (Encapsulator.Iterator treeTemplateChild = treeTemplate.iterator(), treeToMatchChild = treeToMatch
                .iterator(); treeTemplateChild.hasNext()
                     && treeToMatchChild.hasNext(); treeTemplateChild.next(), treeToMatchChild.next())
            res &= treeMatch(treeTemplateChild.value(), treeToMatchChild.value());
        return res;
    }

    /**
     * @param matcher builds recursively the matchers for the constraints that are relevant to the current matcher.
     * @param map
     */
    private void buildMatcherTree(Matcher matcher, Map<Integer, List<Constraint>> map) {
        Set<Integer> l = matcher.getGenericElements();
        l.stream().forEach(i -> Optional.ofNullable(map.get(i)).ifPresent(z -> z.stream().forEach(j ->
                matcher.addConstraint(i, j))));
        matcher.getConstraintsMatchers().stream().forEach(im -> buildMatcherTree(im, map));
    }

    public Encapsulator getRoot() {
        return root;
    }

    private void setRoot(Encapsulator n) {
        root = n;
    }

    /**
     * Adds a constraint on a generic element inside the tree of the root.
     *
     * @param id - the id of the element that we constraint.
     * @param c  - the constraint
     */
    private void addConstraint(Integer id, Constraint c) {
        constrains.putIfAbsent(id, new LinkedList<>());
        constrains.get(id).add(c);
    }

    /**
     * @return the matcher elements in all the constraints applicable on the root of this matcher.
     */
    private List<Matcher> getConstraintsMatchers() {
        return constrains.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .stream()
                .map(t -> t.getMatcher())
                .collect(Collectors.toList());
    }

    /**
     * @param e the tree of the user
     * @return true iff the tree of the user matcher the root and holds through all the constraints.
     */
    public boolean match(PsiElement e) {
        Map<Integer, PsiElement> info = extractInfo(root, e);
        return treeMatch(root, Encapsulator.buildTreeFromPsi(e)) && info.keySet().stream()
                .allMatch(id -> constrains.getOrDefault(id, new LinkedList<>()).stream().allMatch(c -> c.match(info.get(id))));
    }

    /**
     * @param treeTemplate - The root of a tree already been matched.
     * @param treeToMatch  - The patterns from which we extract the IDs
     * @return a mapping between an ID to a PsiElement
     */
    private Map<Integer, PsiElement> extractInfo(Encapsulator treeTemplate, PsiElement treeToMatch) {
        Map<Integer, PsiElement> mapping = new HashMap<>();
        Encapsulator.Iterator treeTemplateChile = treeTemplate.iterator();
        for (PsiElement treeToMatchChild = treeToMatch.getFirstChild(); treeTemplateChile.hasNext() && treeToMatchChild != null; treeTemplateChile.next(), treeToMatchChild = step.nextSibling(treeToMatchChild))
			if (treeTemplateChile.value().getInner().getUserData(KeyDescriptionParameters.ID) == null)
				mapping.putAll(extractInfo(treeTemplateChile.value(), treeToMatchChild));
			else
				mapping.put(treeTemplateChile.value().getInner().getUserData(KeyDescriptionParameters.ID),
						treeToMatchChild);
        return mapping;
    }

    /**
     * @param treeToMatch - The patterns from which we extract the IDs
     * @return a mapping between an ID to a PsiElement
     */
    public Map<Integer, PsiElement> extractInfo(PsiElement treeToMatch) {
        return extractInfo(root, treeToMatch);
    }

    /**
     * @return list of Ids of all the generic elements in the tipper.
     */
    private Set<Integer> getGenericElements() {
        final Set<Integer> tmp = new HashSet<>();
        root.accept(e -> {
            if (iz.generic(e.getInner())) {
                tmp.add(e.getInner().getUserData(KeyDescriptionParameters.ID));
            }
        });
        return tmp;
    }

    /**
     * Represents a constraint on a generalized variable of the leonidas language.
     *
     * @author michalcohen
     * @since 01-04-2017.
     */
    public static class Constraint {

        private ConstraintType type;
        private Matcher matcher;

        public Constraint(ConstraintType t, Encapsulator e) {
            type = t;
            matcher = new Matcher();
            matcher.setRoot(e);
        }

        public ConstraintType getType() {
            return type;
        }

        public Matcher getMatcher() {
            return matcher;
        }

        /**
         * @param e the users tree to match.
         * @return indication of e being matched recursively to the matcher, when taking in consideration the type of the constraint.
         */
        public boolean match(PsiElement e) {
            return (type == ConstraintType.IS && matcher.match(e)) || (type == ConstraintType.IS_NOT && !matcher.match(e));
        }

        public enum ConstraintType {
            IS,
            IS_NOT
        }
    }
}

