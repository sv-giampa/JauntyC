# JointyC Library
	                                
The scope of this README is to give an overview of the library.
For more details see the documentation.

## 1. Introduction.

JointyC is a Java library to write compilers. Its main target is to move
the attention of the developer from parsing to the semantic analysis and,
at the same time, to simplify this part of work.
The library is divided in two main modules:

	1. The analysis framework

	2. The JointyC Definition Language Compiler (JDLC)

The first module exposes a framework to face up to the analysis work,
composed by lexical, syntactic and semantic analysis.
The second module is based on the first one and provides a compiler for
the JointyC Definition Language (JDL), used to define recursive-descent
parsers for context-free languages, that can be adorned with contextual
information during semantic analysis, as the theory of formal languages
teaches us.


## 2. Software Engineering in the scope.

As anticipated, the most important target of JointyC is to simplify the
work of the developers, not only during construction, but also during 
maintenance. In fact, writing a recursive descent parser may result in
a difficult maintenance task. On the other hand, using a parser generator
tool, may be very invasive. It was thought the best way is to have a
simple to use library without any other dependencies or tools and
automatically re-compile the parser, without affecting heavily the
semantic analysis, when a change to the grammar is necessary. By using
the JointyC Definition Language, the parser of the designed language will
be re-compiled at every construction. The creation of the parser is moved
at runtime and totally automated. The parser is compiled only at
construction time, so the parsing proceed easily after that operation. 
The target, from the point of view of Software Engineering, is to write
a parser according to its language specifics, and to modify it when the
language specifics are changed or extended.
More details over this process and other functionalities are described
in the documentation.


## 3. The analysis framework.

The analysis framework is divided in three sub-modules: lexer, parser and
semantic analyzer. The framework provides all the interfaces that define
the functionalities for lexers, parsers and interpreters.
Lexer and parser modules define a proper final implementation, indicated
with the "Standard" prefix: the StandardLexer and the StandardParser.
The semantic module provides a standard syntax tree exploring machinery,
defined by the SemanticAnalyzer class. This class implements an iterative
algorithm which explores a syntax tree obtained by a parser and calls
the associated interpreter to do semantic actions. The three standard
structures are assembled in the StandardCompiler class, to expose a
functional compiler object basing on a parser and an interpreter passed
at construction time. So it is simple to obtain a lexer, a parser and
then a compiler for a language, starting from production rules, without
necessarily use the JDL Compiler. Moreover, it is recommended to use the
JDL Compiler, to obtain a higher maintenance and readability performance.


## 4. The JointyC Definition Language Compiler (JDL Compiler).

By using the JDL Compiler it is possible to define and compile a parser
and the associated lexer at run-time. The objective is to have a double
phase process: the design of the language's specifics, first, and the
definition of the semantic operations, later. By using, the JDL it is
possible to divide the languages in modules, and at the same time,
it is possible to design one interpreter for each language module. Then
different modules and their interpreters may be composed in a single
work. A JDL file is divided in two parts: lexicon and grammar. To define
a lexicon for the language, regular expressions are used. Each lexicon
type should be seen as a variable for the grammar. The grammar is
expressed in BNF (Backus-Naur Form), because, in many cases, the BNF is
simpler to manage during semantic analysis than the EBNF (Extended BNF).
A grammar rule cannot produce directly a ground terminal, but each
terminal symbol must be assigned to a term of the lexicon. This choice
allows to keep the grammar and the access to the syntax tree as generic
as possible.


## 5. Conclusions and recommendations.

For the explained reasons, the JointyC library could be one of the best
choices for designing a language for some project. It is emphasized
that this library is under Apache License 2.0, and then it could be
integrated also in a proprietary software, or used as a starting point
to obtain an enhanced version of it. To familiarize with JointyC and its
programming method, it is highly recommended to start exploring the
provided tutorials and documentation.

## 6. Tutorials and template project

See at https://github.com/sv-giampa/JointyC-Tutorials
		
		
		
		
		
		
		

