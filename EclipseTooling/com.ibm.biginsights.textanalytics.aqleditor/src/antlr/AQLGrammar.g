grammar AQLGrammar;

options {
  language = Java;
  output = AST;
}

@header {
package com.ibm.biginsights.textanalytics.aql.antlr;
  
}

@lexer::header {
package com.ibm.aql.antlr;
}

aql   : statement* -> ^(AQL statement*);

statement 
  : ( createview | include ) ';'!;

createview 
  : CREATE^ VIEW! ID
  ;
  
include
  : INCLUDE^ ID
  ;
  
INCLUDE : 'include';

CREATE  : 'create';

VIEW  : 'view';

AQL : 'aql';

ID  : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;

WS  : (' '|'\r'|'\t'|'\u000C'|'\n') {$channel=HIDDEN;}
    ;
COMMENT
    : '/*' .* '*/' {$channel=HIDDEN;}
    ;
LINE_COMMENT
    : '--' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
    