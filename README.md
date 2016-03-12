# Spartan Refactoring [![Build Status](https://travis-ci.org/SpartanRefactoring/spartan-refactoring.svg?branch=master)](https://travis-ci.org/mittelman/spartan-refactoring)
Spartan Refactoring is a plugin for Eclipse that automatically applies the principles of *[Spartan Programming]* to your Java code. Simply put, it reviews your code, provides suggestions and applies them in order to make your code cleaner, shorter and more comprehensible. It is written in Java.

## Background
This project was initially conceived as an academic project in the [Technion - Israel Institute of Technology], and was later developed for several years by different students and members of the Computer Science faculty.

## Compiling from source
Compiling the plug-in from source is done through Eclipse:

1. Open Eclipse and go to "*Install New Software...*". From the list of install sites, pick *The Eclipse Project Updates* and make sure you've installed all the items from the categories **Eclipse Platform**, **Eclipse Platform SDK**, **Eclipse Plugin Development Tools** and **Eclipse SDK**. Failure to install one of these will result in import errors.

2. Create a *run configuration* by running the project (Ctrl+F11), then elect to run it as an Eclipse Application.
    * If a new instance of Eclipse doesn't launch, open the run configurations for the project and make sure that in the *Program to Run* box, "Run a product" is selected and the box next to it says "org.eclipse.platform.ide".

3. Go to *File -> Export...*. Under the *Plug-in Development category*, choose the *Deployable plug-ins and fragments*, and continue until the plug-in has been built successfully.

## How the plugin works
#### Wrings
*wring (v.): To twist, squeeze, or compress*

A *wring*, in the context of this project, is a small object responsible for converting one form of code into another, under two major assumptions:

1. The latter is shorter and/or more comprehensible than the former.

2. Both forms are semantically equivalent, meaning that both versions do exactly the same thing (yet the latter may perform more efficiently).

Consider this basic programmers' mistake:
```java
if(myString.length() < 5) {
    return true;
} else {
    return false;
}
```
Which can be shortened to:
```java
if(myString.length() < 5)
    return true;
return false;
```
Or even:
```java
return myString.length() < 5;
```
Spartan Refactoring first detects this problem by finding a matching wring, then notifies the user that a spartanization can be made, and finally (when the user asks the plugin to perform the action), revises the `if` block all the way to the last example, while preserving the programmer's intention.

#### Method of operation
The plugin works by analyzing the [abstract syntax tree] (AST) generated by Eclipse for each of the compilation units in the current Java project. After the AST has been generated, and the user had asked to apply a set of wrings to the code, it is traversed using an [ASTVisitor], and the wrings are applied one by one to the tree.

After the traversal is complete, the transformations made to the AST are written back to the source code.

## Code structure
NOTE: It is highly recommended to read (or at least skim through) the "How the plugin works" subsection before reading this.

The code is contained within these Java packages:
* **org.spartan.refactoring.wring** - Contains the wrings responsible for detecting and modifying the different blocks of code.
* **org.spartan.refactoring.utils** - Utility classes directly related to the wrings, and used to process and interpret the AST
* **org.spartan.utils** - Contains more generic utility classes used throughout the project.
* **org.spartan.refactoring.builder** - Classes that extend mandatory Eclipse API classes required to integrate the plugin with the Eclipse IDE.
* **org.spartan.refactoring.handlers** - Handlers used by the Eclipse IDE
* **org.spartan.refactoring.spartanizations** - Mostly unused code that will be removed in the future.
* **org.spartan.refactoring.preferences** - Classes for managing the plugin's preferences.

## License
The project is available under the **[MIT License]**

[Spartan Programming]: http://blog.codinghorror.com/spartan-programming/
[Technion - Israel Institute of Technology]: http://www.technion.ac.il/en/
[abstract syntax tree]: https://en.wikipedia.org/wiki/Abstract_syntax_tree
[ASTVisitor]: http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTVisitor.html
[MIT License]: https://opensource.org/licenses/MIT
