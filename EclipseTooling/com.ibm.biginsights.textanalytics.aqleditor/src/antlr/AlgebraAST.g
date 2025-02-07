grammar AlgebraAST;

options {
  language = Java;
  output = AST;
  ASTLabelType = CommonTree;
}

@header {
package com.ibm.biginsights.textanalytics.aql.antlr;
  
  import java.util.Map;
  import java.util.HashMap;
}

@lexer::header {
package com.ibm.aql.antlr;
}

// START:stat

prog
  :
  stat+
  ;

stat
  :
  expr NEWLINE -> expr
  | ID '=' expr NEWLINE -> ^('=' ID expr)
  | NEWLINE ->
  ;
// END:stat

// START:expr

expr
  :
  a=multExpr
  (
    (
      '+' ^ b=multExpr
      | '-' ^ b=multExpr
    )
  )*
  ;

multExpr
  :
  a=atom ('*' ^ a=atom)*
  ;

atom
  :
  INT
  | ID
  | '(' ! expr ')' !
  ;
// END:expr

// START:tokens

ID
  :
  (
    'a'..'z'
    | 'A'..'Z'
  )+
  ;

INT
  :
  '0'..'9'+
  ;

NEWLINE
  :
  '\r'? '\n'
  ;

WS
  :
  (
    ' '
    | '\t'
    | '\n'
    | '\r'
  )+
  
   {
    skip();
   }
  ;
